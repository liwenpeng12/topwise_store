package com.topwise.topos.appstore.download;

import android.os.Message;
import android.os.Process;
import android.os.StatFs;
import android.widget.Toast;

import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.utils.Utils;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class DownloadThread extends Thread {

    private static final int BUFFER_SIZE = 4 * 1024;
    private static final int PROGRESS_UPDATE_INTERVAL_TIME = 50;

    private DownloadInfo mDownloadInfo;
    private DownloadService.DownloadHandler mDownloadHandler;

    private long mLastUpdateProgressTime = 0;
    private int mRedirectCount = 0;

    public DownloadThread(DownloadInfo info, DownloadService.DownloadHandler handler) {
        mDownloadInfo = info;
        mDownloadHandler = handler;
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        try {
            downloadFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class DownloadingInfo {
        public String mFileName;
        public FileOutputStream mStream;
        public String mRequestUri;
        public String mNewUri;
        public long mTotalBytes = -1;
        public long mCurrentBytes = 0;
        public long mLastCurrentBytes = 0;
        public boolean mContinuingDownload = false;

        public DownloadingInfo(DownloadInfo downloadInfo) {
            mFileName = downloadInfo.downloadingTmpFilePath;
            mRequestUri = downloadInfo.url;
            mTotalBytes = downloadInfo.totalSize;
            mCurrentBytes = downloadInfo.currentDownloadSize;
        }
    }

    public void downloadFile() throws IOException {
        LogEx.w("downloadFile");
        if (mDownloadInfo.totalSize > sdcardFreeSpaceByte()) {
            mDownloadHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AppStoreWrapperImpl.getInstance().getAppContext(), "SD卡已经满了", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        DownloadingInfo info = new DownloadingInfo(mDownloadInfo);
        int finalStatus = DownloadInfo.STATUS_ERROR_UNKNOWN;
        try {
            URI uri = new URI(Utils.fillSpace(info.mRequestUri));
            // 设置一些基本参数
            HttpParams params =new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            HttpProtocolParams.setUseExpectContinue(params, true);
            HttpProtocolParams.setUserAgent(params, AppStoreWrapperImpl.getInstance().getDeviceInfo().getWebViewUserAgent());
            /* 从连接池中取连接的超时时间 */
            ConnManagerParams.setTimeout(params, 1000);
            /* 连接超时 */
            HttpConnectionParams.setConnectionTimeout(params, 2000);
            /* 请求超时 */
            HttpConnectionParams.setSoTimeout(params, 4000);
            // 设置我们的HttpClient支持HTTP和HTTPS两种模式
            SchemeRegistry schReg =new SchemeRegistry();
            schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
            // 使用线程安全的连接管理来创建HttpClient
            ClientConnectionManager conMgr =new ThreadSafeClientConnManager(params, schReg);
            HttpClient client = new DefaultHttpClient(conMgr, params);
            HttpParams httpParams = client.getParams();
            httpParams.setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
//            HttpConnectionParams.setConnectionTimeout(httpParams, 60000);
//            HttpConnectionParams.setSoTimeout(httpParams, 60000);
            HttpClientParams.setCookiePolicy(httpParams, CookiePolicy.BROWSER_COMPATIBILITY);
            HttpUriRequest request = new HttpGet(uri);
            boolean finished = false;
            while (!finished) {
                try {
                    Message message = mDownloadHandler.obtainMessage(DownloadService.MSG_DOWNLOAD_START);
                    message.obj = mDownloadInfo;
                    mDownloadHandler.sendMessage(message);
                    executeDownload(info, client, request);
                    finished = true;
                } catch (RetryDownload e) {
                    e.printStackTrace();
                    LogEx.w(e.getMessage());
                } finally {
                    request.abort();
                    request = null;
                }
            }
            if (info.mCurrentBytes != info.mTotalBytes) {
                finalStatus = DownloadInfo.STATUS_ERROR_FILE_ERROR;
            } else {
                finalStatus = DownloadInfo.STATUS_DOWNLOAD_SUCCESS;
            }
        } catch (StopRequestException e) {
            e.printStackTrace();
            LogEx.w("Aborting request for download: " + e.getMessage());
            finalStatus = e.mFinalStatus;
        } catch (Throwable t) {
            t.printStackTrace();
            LogEx.w(t.getMessage());
            finalStatus = DownloadInfo.STATUS_ERROR_UNKNOWN;
        } finally {
            try {
                info.mStream.close();
                info.mStream = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            notifyDownloadStatusChanged(info, finalStatus);
        }
    }

    private void notifyDownloadStatusChanged(DownloadingInfo info, int finalStatus) {
        mDownloadInfo.downloadStatus = finalStatus;
        mDownloadInfo.setDownloadedSize(info.mCurrentBytes, true);
        Message message = mDownloadHandler.obtainMessage(DownloadService.MSG_DOWNLOAD_STATUS_CHANGED);
        message.obj = mDownloadInfo;
        mDownloadHandler.sendMessage(message);
    }

    private void executeDownload(DownloadingInfo info, HttpClient client, HttpUriRequest request) throws StopRequestException, RetryDownload {
        LogEx.w("executeDownload");
        byte data[] = new byte[BUFFER_SIZE];
        setupDestFile(info);
        addRequestHeaders(info, request);
        HttpResponse response = sendRequest(client, request);
        handleExceptionStatus(info, response);
        processResponseHeaders(info, response);
        InputStream entityStream = openResponseEntity(response);
        transferData(info, data, entityStream);
        try {
            entityStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private InputStream openResponseEntity(HttpResponse response) throws StopRequestException {
        try {
            return response.getEntity().getContent();
        } catch (IOException e) {
            throw new StopRequestException(DownloadInfo.STATUS_ERROR_HTTP_ERROR, "while getting entity: " + e.toString(), e);
        }
    }

    private HttpResponse sendRequest(HttpClient client, HttpUriRequest request) throws StopRequestException {
        try {
            LogEx.w("sendRequest");
            return client.execute(request);
        } catch (IllegalArgumentException e) {
            throw new StopRequestException(DownloadInfo.STATUS_ERROR_HTTP_ERROR, "while trying to execute request: " + e.toString(), e);
        } catch (IOException e) {
            throw new StopRequestException(DownloadInfo.STATUS_ERROR_HTTP_ERROR, "while trying to execute request: " + e.toString(), e);
        }
    }

    private void handleExceptionStatus(DownloadingInfo info, HttpResponse response) throws StopRequestException, RetryDownload {
        LogEx.w("handleExceptionStatus");
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 301 || statusCode == 302 || statusCode == 303 || statusCode == 307) {
            handleRedirect(info, response, statusCode);
        }
        int expectedStatus = info.mContinuingDownload ? 206 : DownloadInfo.STATUS_DOWNLOAD_SUCCESS;
        if (statusCode != expectedStatus) {
            throw new StopRequestException(statusCode, "method handleExceptionalStatus(), http error " + statusCode + ", mContinuingDownload: " + info.mContinuingDownload);
        }
    }

    private void handleRedirect(DownloadingInfo info, HttpResponse response, int statusCode) throws StopRequestException, RetryDownload {
        if (mRedirectCount > 5) {
            throw new StopRequestException(DownloadInfo.STATUS_ERROR_HTTP_ERROR, "too many redirects");
        }
        Header header = response.getFirstHeader("Location");
        if (header == null) {
            return;
        }
        String newUri;
        try {
            newUri = new URI(info.mRequestUri).resolve(new URI(header.getValue())).toString();
        } catch (URISyntaxException ex) {
            throw new StopRequestException(DownloadInfo.STATUS_ERROR_HTTP_ERROR, "Couldn't resolve redirect URI");
        }
        ++mRedirectCount;
        info.mRequestUri = newUri;
        if (statusCode == 301 || statusCode == 303) {
            info.mNewUri = newUri;
        }
        throw new RetryDownload();
    }

    private void addRequestHeaders(DownloadingInfo info, HttpUriRequest request) {
        LogEx.w("addRequestHeaders");
        if (info.mContinuingDownload) {
            request.addHeader("Range", "bytes=" + info.mCurrentBytes + "-");
        }
    }

    private void processResponseHeaders(DownloadingInfo info, HttpResponse response) throws StopRequestException {
        LogEx.w("processResponseHeaders");
        if (info.mContinuingDownload) {
            return;
        }
        readResponseHeaders(info, response);
        try {
            info.mStream = new FileOutputStream(info.mFileName);
        } catch (FileNotFoundException exc) {
            throw new StopRequestException(DownloadInfo.STATUS_ERROR_FILE_ERROR, "method processResponseHeaders(), while opening destination file: " + exc.toString(), exc);
        }
    }

    private void readResponseHeaders(DownloadingInfo info, HttpResponse response) throws StopRequestException {
        String headerTransferEncoding = null;
        Header header = response.getFirstHeader("Transfer-Encoding");
        if (header != null) {
            headerTransferEncoding = header.getValue();
        }
        if (headerTransferEncoding == null) {
            header = response.getFirstHeader("Content-Length");
            if (header != null) {
                info.mTotalBytes = Long.parseLong(header.getValue());
                mDownloadInfo.setTotalSize(info.mTotalBytes, true);
                mDownloadInfo.setDownloadedSize(0, true);
            }
        } else {
            throw new StopRequestException(DownloadInfo.STATUS_ERROR_HTTP_ERROR, "can't know size of download, giving up");
        }
    }

    private void setupDestFile(DownloadingInfo info) throws StopRequestException {
        LogEx.w("setupDestFile");
        if (info.mFileName != null && info.mFileName.length() != 0) {
            File f = new File(info.mFileName);
            //如果文件存在才是断点续传
            if (f.exists()) {
                if (f.length() == 0) {
                    f.delete();
                } else {
                    try {
                        info.mStream = new FileOutputStream(info.mFileName, true);
                    } catch (FileNotFoundException exc) {
                        throw new StopRequestException(DownloadInfo.STATUS_ERROR_FILE_ERROR, "method setupDestinationFile()#0, while opening destination for resuming: " + exc.toString(), exc);
                    }
                    info.mCurrentBytes = (int) f.length();
                    if (mDownloadInfo.totalSize > 0) {
                        if (info.mCurrentBytes == mDownloadInfo.totalSize) {
                            throw new StopRequestException(DownloadInfo.STATUS_DOWNLOAD_SUCCESS, "method setupDestinationFile()#1, encounter the '100%' error");
                        }
                    }
                    info.mContinuingDownload = true;
                }
            }
        }
    }

    private void transferData(DownloadingInfo info, byte[] data, InputStream entityStream) throws StopRequestException {
        for (; ; ) {
            int bytesRead = readFromResponse(data, entityStream);
            if (bytesRead == -1) {
                return;
            }
            writeDataToDestination(info, data, bytesRead);
            info.mCurrentBytes += bytesRead;
            publishProgress(info);
            checkPausedOrCanceled();
            Message message = mDownloadHandler.obtainMessage(DownloadService.MSG_DOWNLOAD_PROGRESS_CHANGED);
            message.obj = mDownloadInfo;
            mDownloadHandler.sendMessage(message);
        }
    }

    private void publishProgress(DownloadingInfo info) {
        long now = System.currentTimeMillis();
        long downloadBytes = info.mCurrentBytes - info.mLastCurrentBytes;
        long timeDiff = now - mLastUpdateProgressTime;
        if (downloadBytes > BUFFER_SIZE && timeDiff > PROGRESS_UPDATE_INTERVAL_TIME) {
            mDownloadInfo.downloadSpeed = (int) (downloadBytes / timeDiff);
            mDownloadInfo.setDownloadedSize(info.mCurrentBytes, true);
            info.mLastCurrentBytes = info.mCurrentBytes;
            mLastUpdateProgressTime = now;
        }
    }

    private int readFromResponse(byte[] data, InputStream entityStream) throws StopRequestException {
        try {
            return entityStream.read(data);
        } catch (IOException ex) {
            throw new StopRequestException(DownloadInfo.STATUS_ERROR_HTTP_ERROR, "while reading response: " + ex.toString(), ex);
        }
    }

    private void writeDataToDestination(DownloadingInfo info, byte[] data, int bytesRead) throws StopRequestException {
        for (; ; ) {
            try {
                if (info.mStream == null) {
                    info.mStream = new FileOutputStream(info.mFileName, true);
                }
                info.mStream.write(data, 0, bytesRead);
                return;
            } catch (IOException ex) {
                if (info.mStream != null) {
                    throw new StopRequestException(DownloadInfo.STATUS_ERROR_FILE_ERROR, "Error Write Destination File.");
                }
            }
        }
    }

    private void checkPausedOrCanceled() throws StopRequestException {
        synchronized (mDownloadInfo) {
            if (mDownloadInfo.downloadStatus == DownloadInfo.STATUS_PAUSE) {
                throw new StopRequestException(DownloadInfo.STATUS_PAUSE, "download paused by owner");
            }
            if (mDownloadInfo.downloadStatus == DownloadInfo.STATUS_CANCEL) {
                throw new StopRequestException(DownloadInfo.STATUS_CANCEL, "download canceled");
            }
        }
    }

    private double sdcardFreeSpaceByte() {
        StatFs stat = new StatFs(Utils.getInternalStoragePath());
        return ((double) stat.getAvailableBlocks() * (double) stat.getBlockSize());
    }

    private class StopRequestException extends Exception {
        public int mFinalStatus;

        public StopRequestException(int finalStatus, String message) {
            super(message);
            mFinalStatus = finalStatus;
        }

        public StopRequestException(int finalStatus, String message, Throwable throwable) {
            super(message, throwable);
            mFinalStatus = finalStatus;
        }
    }

    private class RetryDownload extends Throwable {
    }

}
