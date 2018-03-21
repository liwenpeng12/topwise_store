package com.topwise.topos.appstore.download;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.api.AppStoreApi;
import com.topwise.topos.appstore.data.AppInfo;
import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.manager.AppManager;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.view.ListAppItemView;
import com.topwise.topos.appstore.view.PopularItemView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class DownloadManager {
    public static final int MazFailureNum = 20;
    private static DownloadManager mThis = null;

    private Handler mMainThreadHandler = null;

    private ArrayList<DownloadObserver> mObservers;

    public DownloadDBProvider mDownloadDBProvider = null;
    private HashMap<String, Integer> faileCount = null;

    public static DownloadManager getInstance() {
        if (mThis == null) {
            synchronized (DownloadManager.class) {
                if (mThis == null) {
                    mThis = new DownloadManager();
                }
            }
        }
        return mThis;
    }

    public DownloadManager() {
        mMainThreadHandler = new Handler(Looper.getMainLooper());
        mDownloadDBProvider = new DownloadDBProvider(this);
        mObservers = new ArrayList<>();
        faileCount = new HashMap<>();
    }

    public void onDestroy() {
        if (mObservers != null) {
            mObservers.clear();
        }
    }

    public void startDownload(final DownloadInfo info) {
        LogEx.w("startDownload");
        int status = mDownloadDBProvider.getDownloadInfoStatus(info);
        if (status == DownloadInfo.STATUS_DOWNLOAD_SUCCESS) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    // Toast.makeText(AppStoreWrapperImpl.getInstance().getAppContext(), "该文件已经下载了", Toast.LENGTH_SHORT).show();
                    String appId = info.uid.substring(0, info.uid.lastIndexOf("_"));
                    AppInfo appInfo = DataPool.getInstance().getAppInfo(appId);
                    AppManager.getInstance().endDownloadApp(appInfo, new File(info.destFilePath));
                }
            });
            return;
        } else if (status == DownloadInfo.STATUS_DOWNLOADING || status == DownloadInfo.STATUS_PENDING) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
//                    Toast.makeText(AppStoreWrapperImpl.getInstance().getAppContext(), "该文件正在下载中", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        } else if (status == DownloadInfo.STATUS_ERROR_UNKNOWN
                || status == DownloadInfo.STATUS_ERROR_FILE_ERROR
                || status == DownloadInfo.STATUS_ERROR_HTTP_ERROR) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AppStoreWrapperImpl.getInstance().getAppContext(), "下载失败", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        } else {
            Intent intent = new Intent(AppStoreWrapperImpl.getInstance().getAppContext(), DownloadService.class);
            intent.setAction(DownloadService.ACTION_DOWNLOAD_START);
            intent.putExtra("downloadInfo", info);
            AppStoreWrapperImpl.getInstance().getAppContext().startService(intent);
            return;
        }
    }

    public void pauseDownload(String downloadInfoId) {
        Intent intent = new Intent(AppStoreWrapperImpl.getInstance().getAppContext(), DownloadService.class);
        intent.setAction(DownloadService.ACTION_DOWNLOAD_PAUSE);
        intent.putExtra("downloadInfoId", downloadInfoId);
        AppStoreWrapperImpl.getInstance().getAppContext().startService(intent);
        try {
            String appId = downloadInfoId.substring(0, downloadInfoId.lastIndexOf("_"));
            AppStoreApi.mDownloadStateListener.get(appId).onDownloadStateChanged(appId, 5);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resumeDownload(String downloadInfoId) {
        Intent intent = new Intent(AppStoreWrapperImpl.getInstance().getAppContext(), DownloadService.class);
        intent.setAction(DownloadService.ACTION_DOWNLOAD_RESUME);
        intent.putExtra("downloadInfoId", downloadInfoId);
        AppStoreWrapperImpl.getInstance().getAppContext().startService(intent);
        try {
            String appId = downloadInfoId.substring(0, downloadInfoId.lastIndexOf("_"));
            AppStoreApi.mDownloadStateListener.get(appId).onDownloadStateChanged(appId, 6);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelDownload(String downloadInfoId) {
        Intent intent = new Intent(AppStoreWrapperImpl.getInstance().getAppContext(), DownloadService.class);
        intent.setAction(DownloadService.ACTION_DOWNLOAD_CANCEL);
        intent.putExtra("downloadInfoId", downloadInfoId);
        AppStoreWrapperImpl.getInstance().getAppContext().startService(intent);
        try {
            String appId = downloadInfoId.substring(0, downloadInfoId.lastIndexOf("_"));
            AppStoreApi.mDownloadStateListener.get(appId).onDownloadStateChanged(appId, 7);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerDownloadObserver(DownloadObserver o) {
        synchronized (DownloadObserver.class) {
            if (!mObservers.contains(o)) {
                mObservers.add(o);
            }
        }
    }

    public void unregisterDownloadObserver(DownloadObserver o) {
        synchronized (DownloadObserver.class) {
            mObservers.remove(o);
        }
    }

    public void notifyObservers(DownloadInfo info) {
        synchronized (DownloadObserver.class) {
            for (DownloadObserver o : mObservers) {
                o.onDownloadChanged(info);
            }
        }
    }
    public Handler getMainHandler(){
        return mMainThreadHandler;
    }

    public void reSetAppInfo(AppInfo appInfo) {
        for (DownloadObserver o : mObservers) {
            if (o instanceof ListAppItemView) {
                ListAppItemView itemView = (ListAppItemView) o;
                if (null != itemView.getAppInfo() && itemView.getAppInfo().pkg.equals(appInfo.pkg)) {
                    itemView.setAppInfo(appInfo);
                    continue;
                }
            }
            if (o instanceof PopularItemView) {
                PopularItemView itemView = (PopularItemView) o;
                if (null != itemView.getAppInfo() && itemView.getAppInfo().pkg.equals(appInfo.pkg)) {
                    itemView.setAppInfo(appInfo);
                    continue;
                }
            }
        }
    }

    public interface DownloadObserver {
        void onDownloadChanged(DownloadInfo info);
    }

    //记录失败的次数
    public void setCount(String id, int count) {
        faileCount.put(id, count);
    }

    public int getCount(String id) {
        if (faileCount.containsKey(id)) {
            return faileCount.get(id);
        }
        return 0;
    }
}
