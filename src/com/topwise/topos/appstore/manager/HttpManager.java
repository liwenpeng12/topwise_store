package com.topwise.topos.appstore.manager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.json.JSONObject;

import android.util.Base64;

import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.conn.http.AjaxCallBack;
import com.topwise.topos.appstore.conn.http.AjaxParams;
import com.topwise.topos.appstore.conn.http.FinalHttp;
import com.topwise.topos.appstore.conn.http.HttpHandler;
import com.topwise.topos.appstore.conn.protocol.Protocol;
import com.topwise.topos.appstore.utils.FileUtil;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.utils.Properties;
import com.topwise.topos.appstore.utils.Signature;

public class HttpManager {
    
    private static HttpManager mThis = null;

    private FinalHttp mFinalHttp = null;
    public AjaxParams mAjaxParams = null;
    public String mCommonBase64 = "";

    private HashMap<String, ProgressInfo> mDownloadings = null;
    private OnAllDownloadsStatusListener mStatusChangedListener = null;

    public HttpManager() {
        initHttp();
        mDownloadings = new HashMap<String, ProgressInfo>();
    }

    public static HttpManager getInstance() {
        if (mThis == null) {
            synchronized (HttpManager.class) {
                if (mThis == null) {
                    mThis = new HttpManager();
                }
            }
        }
        return mThis;
    }

    public Downloader createDownloader(String title, String url, String target,
            DownloadProgressListener listener) {
        return new Downloader(title, url, target, listener);
    }

    public void registerAllDownloadsStatusChangedListener(OnAllDownloadsStatusListener l) {
        mStatusChangedListener = l;
    }

    public HashMap<String, ProgressInfo> getCurrentProgressInfos() {
        return mDownloadings;
    }
    
    public void get(String url, AjaxCallBack<? extends Object> callBack) {
        mFinalHttp.get(url, mAjaxParams, callBack);
    }

    public void get(String url) {
        mFinalHttp.get(url, null, null);
    }

    public Object getSync(String url) {
        return mFinalHttp.getSync(url, mAjaxParams);
    }
    
    public void post(String url, AjaxCallBack<? extends Object> callBack) {
        mFinalHttp.post(url, mAjaxParams, callBack);
    }
    
    public void post(String url) {
        post(url, null);
    }
    
    public Object postSync(String url) {
        LogEx.d("postSync,mAjaxParams=" + mAjaxParams);
        return mFinalHttp.postSync(url, mAjaxParams);
    }
    
    public void clearCache() {
        FileUtil.clearDir(Properties.CACHE_PATH);
    }

    private void initHttp() {
        mFinalHttp = FinalHttp.getInstance();
        mFinalHttp.addHeader("Accept-Charset", "UTF-8");// 配置http请求头
        mFinalHttp.configCookieStore(null);
        mFinalHttp.configRequestExecutionRetryCount(3);// 请求错误重试次数
        mFinalHttp.configTimeout(30 * 1000);
        mFinalHttp.configUserAgent(AppStoreWrapperImpl.getInstance().getDeviceInfo().getWebViewUserAgent());// 配置客户端信息
        
        mAjaxParams = new AjaxParams();
        mAjaxParams.put("pkey", "" + Protocol.getInstance().pkey);
        
        try {
            JSONObject common = new JSONObject();
            common.put("apiver", Protocol.PROTOCOL_VERSION);
            common.put("apppkg", AppStoreWrapperImpl.getInstance().getAppContext().getPackageName());
            common.put("appver", "" + AppStoreWrapperImpl.getInstance().getAppVersionCode());
            common.put("sdkver", "" + AppStoreWrapperImpl.getInstance().getJarVersionCode());
            common.put("net", AppStoreWrapperImpl.getInstance().getDeviceInfo().getNetworkTypeString());
            common.put("lng", AppStoreWrapperImpl.getInstance().getLanguage());
            common.put("uid", AppStoreWrapperImpl.getInstance().getUserId());
            common.put("androidver", "" + AppStoreWrapperImpl.getInstance().getDeviceInfo().getAndroidVersion());
            common.put("andvername", AppStoreWrapperImpl.getInstance().getDeviceInfo().getAndroidVersionName());
            common.put("sh", "" + AppStoreWrapperImpl.getInstance().getDeviceInfo().getScreenHeight());
            common.put("sw", "" + AppStoreWrapperImpl.getInstance().getDeviceInfo().getScreenWidth());
            common.put("imei", AppStoreWrapperImpl.getInstance().getDeviceInfo().getIMEI());
            common.put("phone", AppStoreWrapperImpl.getInstance().getDeviceInfo().getProductModel());
            common.put("channel", AppStoreWrapperImpl.getInstance().getChannel());
            common.put("mac", AppStoreWrapperImpl.getInstance().getDeviceInfo().getMac());
            common.put("sn", AppStoreWrapperImpl.getInstance().getDeviceInfo().getSerialNo());
            common.put("imsi", AppStoreWrapperImpl.getInstance().getDeviceInfo().getIMSI());
            common.put("androidid", AppStoreWrapperImpl.getInstance().getDeviceInfo().getAndroidId());
            common.put("dpi", "" + AppStoreWrapperImpl.getInstance().getDeviceInfo().getDensityDpi());
            common.put("density", "" + AppStoreWrapperImpl.getInstance().getDeviceInfo().getDensity());
            common.put("carrier", AppStoreWrapperImpl.getInstance().getDeviceInfo().getCarrierName());
            common.put("brand", AppStoreWrapperImpl.getInstance().getDeviceInfo().getBrand());
            common.put("ua", AppStoreWrapperImpl.getInstance().getDeviceInfo().getWebViewUserAgent());
            common.put("bdid", "" + AppStoreWrapperImpl.getInstance().getDeviceInfo().getCellId());
            common.put("isroot", "" + AppStoreWrapperImpl.getInstance().getDeviceInfo().isRootSystem());
            common.put("iswifi", "" + AppStoreWrapperImpl.getInstance().getDeviceInfo().isWifi());
            common.put("appvername", AppStoreWrapperImpl.getInstance().getAppVersionName());
            common.put("signature", Signature.getSignatureInfo(AppStoreWrapperImpl.getInstance().getAppContext()).SHA1);

            mCommonBase64 = Base64.encodeToString(common.toString().getBytes(), Base64.DEFAULT);
            LogEx.d("common=" + mCommonBase64);
            
            mAjaxParams.put("common", mCommonBase64);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class Downloader {
        public String mTitle = null;
        private String mUrl = null;
        private String mTarget = null;
        private DownloadProgressListener mListener = null;
        private HttpHandler<File> mHttpHandler = null;

        public Downloader(String title, String url, String target, DownloadProgressListener listener) {
            mTitle = title;
            mUrl = url;
            mTarget = target;
            mListener = listener;
            try {
                FileUtil.createNewFile(new File(target));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void startDownload() {
            mHttpHandler = mFinalHttp.download(mUrl, mTarget, true, callback);
            mDownloadings.put(mUrl, new ProgressInfo(mTitle, mUrl, 100, 0));
            if (mStatusChangedListener != null) {
                mStatusChangedListener.onStatusChanged(mDownloadings);
            }
        }

        public void pauseDownload() {
            mHttpHandler.stop();
        }

        public void resumeDownload() {
            mHttpHandler = mFinalHttp.download(mUrl, mTarget, true, callback);
        }

        public void stopDownload() {
            mHttpHandler.stop();
        }

        AjaxCallBack<File> callback = new AjaxCallBack<File>() {

            @Override
            public boolean isProgress() {
                // TODO Auto-generated method stub
                return super.isProgress();
            }

            @Override
            public int getRate() {
                // TODO Auto-generated method stub
                return super.getRate();
            }

            @Override
            public AjaxCallBack<File> progress(boolean progress, int rate) {
                // TODO Auto-generated method stub
            	LogEx.d("AjaxCallBack progress, progress = " + progress + ", rate = " + rate);
                return super.progress(progress, rate);
            }

            @Override
            public void onStart() {
                // TODO Auto-generated method stub
                LogEx.d("AjaxCallBack onStart");
                if (mListener != null) {
                    mListener.onStart();
                }
                super.onStart();
            }

            @Override
            public void onLoading(long count, long current) {
                // TODO Auto-generated method stub
            	LogEx.d("AjaxCallBack onLoading, count = " + count + ", current  = " + current);
                if (mListener != null) {
                    mListener.onLoading(count, current);
                }
                ProgressInfo info = mDownloadings.get(mUrl);
                if (info != null) {
                    info.count = count;
                    info.current = current;
                    mDownloadings.put(mUrl, info);
                    if (mStatusChangedListener != null) {
                        mStatusChangedListener.onStatusChanged(mDownloadings);
                    }
                }
                super.onLoading(count, current);
            }

            @Override
            public void onSuccess(File t) {
                // TODO Auto-generated method stub
            	LogEx.d("AjaxCallBack onSuccess, file name = " + t.getName());
                if (mListener != null) {
                    mListener.onSuccess(t);
                }
                mDownloadings.remove(mUrl);
                if (mStatusChangedListener != null) {
                    mStatusChangedListener.onStatusChanged(mDownloadings);
                }
                super.onSuccess(t);
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                // TODO Auto-generated method stub
                LogEx.e("onFailure,Throwable:" + t + ",errorNo:" + errorNo + ",strMsg:" + strMsg);
                if (mListener != null) {
                    mListener.onFailure(t, errorNo, strMsg);
                }
                super.onFailure(t, errorNo, strMsg);
            }

        };
    }
    
    public class ProgressInfo {
        public String title;
        public String url;
        public long count;
        public long current;
        
        public ProgressInfo(long count, long current) {
            this.count = count;
            this.current = current;
        }

        public ProgressInfo(String title, String url, long count, long current) {
            this.title = title;
            this.url = url;
            this.count = count;
            this.current = current;
        }
    }
    
    public interface DownloadProgressListener {
        void onStart();
        void onLoading(long count, long current);
        void onSuccess(File file);
        void onFailure(Throwable t, int errorNo, String strMsg);
    }
    
    public interface OnAllDownloadsStatusListener {
        void onStatusChanged(HashMap<String, ProgressInfo> progressInfos);
    }

}
