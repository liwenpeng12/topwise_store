package com.topwise.topos.appstore.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.manager.AppManager;
import com.topwise.topos.appstore.utils.LogEx;

public class AppStoreService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        LogEx.d("onCreate");
        mLoadNeedUpgradeAppsHandler.sendEmptyMessageDelayed(0, 1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogEx.d("onStartCommand");
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private Handler mLoadNeedUpgradeAppsHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            if (AppStoreWrapperImpl.getInstance().getAppContext() == null) {
                return;
            }
            AppManager.getInstance().loadNeedUpgradeApps();
            long delayMillis = 2 * 60 * 60 * 1000;
            mLoadNeedUpgradeAppsHandler.sendEmptyMessageDelayed(0, delayMillis);
        }

    };

}
