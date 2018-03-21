package com.topwise.topos.appstore.api;

import android.os.RemoteException;

import com.topwise.topos.appstore.conn.protocol.Protocol;
import com.topwise.topos.appstore.data.AppInfo;
import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.database.SharedPreferencesCenter;
import com.topwise.topos.appstore.download.DownloadInfo;
import com.topwise.topos.appstore.download.DownloadManager;
import com.topwise.topos.appstore.manager.AppManager;
import com.topwise.topos.appstore.manager.ManagerCallback;
import com.topwise.topos.appstore.manager.SearchManager;
import com.topwise.topos.appstore.utils.LogEx;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;

public class AppStoreApi extends IAppStoreApi.Stub {

    public static HashMap<String, IDownloadStateListener> mDownloadStateListener = new HashMap<>();

    @Override
    public void searchApp(String keyword, final ISearchCallback callback) throws RemoteException {
        LogEx.d("searchApp=" + keyword);
        if (keyword.contains("#")) {
            keyword = keyword.substring(0, keyword.indexOf("#"));
        }
        if (!SharedPreferencesCenter.getInstance().getSharedPreferences().getBoolean("prompt", false)) {
            callback.onSearchFail("permission denied,need to agree permissions first.");
            return;
        }
        SearchManager.getInstance().search(keyword, new ManagerCallback() {
            @Override
            public void onSuccess(String moduleType, int dataType, int page, int num, boolean end) {
                LogEx.d("searchApp success");
                ArrayList<AppInfo> appInfos = DataPool.getInstance().getAppInfos(dataType);
                if (appInfos == null || appInfos.size() == 0) {
                    try {
                        callback.onSearchFail("no result");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
                JSONArray array = new JSONArray();
                for (AppInfo info : appInfos) {
                    array.put(info.toJSON());
                }
                try {
                    callback.onSearchSuccess(array.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String moduleType, int dataType, Throwable t, int errorNo, String strMsg) {
                LogEx.d("searchApp fail, " + strMsg);
                if (t.getClass().equals(org.apache.http.conn.ConnectTimeoutException.class)
                        || t.getClass().equals(org.apache.http.client.HttpResponseException.class)
                        || t.getClass().equals(org.apache.http.conn.ConnectionPoolTimeoutException.class)) {
                    Protocol.getInstance().changeServer();
                }
                try {
                    callback.onSearchFail(strMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean startDownloadApp(String id, IDownloadStateListener l) throws RemoteException {
        LogEx.d("startDownloadApp," + id);
        mDownloadStateListener.put(id, l);
        AppInfo info = DataPool.getInstance().getAppInfo(id);
        if (info == null) {
            return false;
        }
        AppManager.getInstance().startDownloadApp(info);
        return true;
    }

    @Override
    public boolean cancelDownloadApp(String id) throws RemoteException {
        LogEx.d("cancelDownloadApp," + id);
        AppInfo info = DataPool.getInstance().getAppInfo(id);
        if (info == null) {
            return false;
        }
        String remoteId = info.id + "_" + info.vercode;
        ArrayList<DownloadInfo> allDownloads = DownloadManager.getInstance().mDownloadDBProvider.getAllDownloads();
        for (DownloadInfo downloadInfo : allDownloads) {
            if (downloadInfo.uid.equals(remoteId)) {
                DownloadManager.getInstance().cancelDownload(downloadInfo.uid);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean resumeDownloadApp(String id) throws RemoteException {
        LogEx.d("resumeDownloadApp," + id);
        AppInfo info = DataPool.getInstance().getAppInfo(id);
        if (info == null) {
            return false;
        }
        String remoteId = info.id + "_" + info.vercode;
        ArrayList<DownloadInfo> allDownloads = DownloadManager.getInstance().mDownloadDBProvider.getAllDownloads();
        for (DownloadInfo downloadInfo : allDownloads) {
            if (downloadInfo.uid.equals(remoteId)) {
                DownloadManager.getInstance().resumeDownload(downloadInfo.uid);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean pauseDownloadApp(String id) throws RemoteException {
        LogEx.d("pauseDownloadApp," + id);
        AppInfo info = DataPool.getInstance().getAppInfo(id);
        if (info == null) {
            return false;
        }
        String remoteId = info.id + "_" + info.vercode;
        ArrayList<DownloadInfo> allDownloads = DownloadManager.getInstance().mDownloadDBProvider.getAllDownloads();
        for (DownloadInfo downloadInfo : allDownloads) {
            if (downloadInfo.uid.equals(remoteId)) {
                DownloadManager.getInstance().pauseDownload(downloadInfo.uid);
                return true;
            }
        }
        return false;
    }

    @Override
    public int getAppState(String id) throws RemoteException {
        LogEx.d("getAppState," + id);
        AppInfo info = DataPool.getInstance().getAppInfo(id);
        if (info == null) {
            return -1;
        }
        return info.flag;
    }

    @Override
    public String getAppUpgradeDesc(String id) throws RemoteException {
        LogEx.d("getAppState," + id);
        AppInfo info = DataPool.getInstance().getAppInfo(id);
        if (info == null) {
            LogEx.d("getAppState,null");
            return null;
        }
        LogEx.d("getAppState," + info.upgrade_desc);
        return info.upgrade_desc;
    }
}
