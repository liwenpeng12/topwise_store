package com.topwise.topos.appstore.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.conn.http.AjaxCallBack;
import com.topwise.topos.appstore.conn.protocol.Protocol;
import com.topwise.topos.appstore.data.AppUpgradeInfo;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.utils.Properties;
import com.topwise.topos.appstore.utils.Utils;

import java.io.File;

public class SelfUpgradeCenter extends BaseManager {

    private static SelfUpgradeCenter mThis = null;

    private AppUpgradeInfo mAppInfo = null;
    private boolean mIsChecking = false;

    private BroadcastReceiver mDownloadApkFileFinishReceiver;
    public static final String DOWNLOAD_APK_FINISHED_ACTION = "broadcast.action.appstore.APK_DOWNLOAD_FINISH";

    public static SelfUpgradeCenter getInstance() {
        if (mThis == null) {
            synchronized (SelfUpgradeCenter.class) {
                if (mThis == null) {
                    mThis = new SelfUpgradeCenter();
                }
            }
        }
        return mThis;
    }

    public void checkUpdate(final UpdateCallback callback) {
        mIsChecking = true;
        HttpManager.getInstance().post(Protocol.getInstance().getSelfUpgradeUrl(), new AjaxCallBack<String>() {
            @Override
            public void onSuccess(final String t) {
                LogEx.d(t);
                mMainThreadHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        mAppInfo = new AppUpgradeInfo();
                        String res = Protocol.getInstance().parseSelfUpgrade(t, mAppInfo);
                        if ("true".equals(res)) {
                            callback.onSuccess(mAppInfo);
                        } else {
                            mAppInfo = null;
                            callback.onFailure(null, 0, res);
                        }
                    }

                });
                mIsChecking = false;
                super.onSuccess(t);
            }

            @Override
            public void onFailure(final Throwable t, final int errorNo, final String strMsg) {
                LogEx.e("onFailure,Throwable:" + t + ",errorNo:" + errorNo + ",strMsg:" + strMsg);
                mMainThreadHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        callback.onFailure(t, errorNo, strMsg);
                    }
                });
                mIsChecking = false;
                super.onFailure(t, errorNo, strMsg);
            }
        });
    }

    public boolean isChecking() {
        return mIsChecking;
    }

    public AppUpgradeInfo getSelfUpgradeInfo() {
        return mAppInfo;
    }

    public void downloadSelfUpgradeApk(AppUpgradeInfo info){
        File file = new File(Properties.CACHE_PATH + System.currentTimeMillis() + ".apk");
        HttpManager.getInstance().createDownloader(info.verName, info.url, file.getAbsolutePath(), new HttpManager.DownloadProgressListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onLoading(long count, long current) {
            }

            @Override
            public void onSuccess(File file) {
                Utils.installApk(AppStoreWrapperImpl.getInstance().getAppContext(), file.getAbsolutePath());
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
            }
        }).startDownload();
    }

    public void registerDownloadApkReceiver(Context context) {
        if (mDownloadApkFileFinishReceiver == null) {
            mDownloadApkFileFinishReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    String path = intent.getStringExtra("DES");
                    LogEx.d("receive broadcast for download apk finished!, path=" + path);
                    if (path == null || path.length() == 0) {
                        return;
                    }

                    File file = new File(path);
                    if (file.exists()) {
                        Utils.installApk(context, file.getAbsolutePath());
                    }
                }
            };

            IntentFilter filter = new IntentFilter(DOWNLOAD_APK_FINISHED_ACTION);
            context.registerReceiver(mDownloadApkFileFinishReceiver, filter);
        }
    }

    public void unregisterDownloadApkReceiver(Context context) {
        try {
            context.unregisterReceiver(mDownloadApkFileFinishReceiver);
            mDownloadApkFileFinishReceiver = null;
        } catch (Exception e) {
        }
    }

    public interface UpdateCallback {
        void onSuccess(AppUpgradeInfo appInfo);
        void onFailure(Throwable t, int errorNo, final String strMsg);
    }
}
