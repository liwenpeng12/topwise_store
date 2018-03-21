package com.topwise.topos.appstore;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.topwise.topos.appstore.manager.ActivityManager;

public class AppStoreApplication extends Application {
    public static AppStoreApplication sInstance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
//        com.ak.firm.shell.FirmSdk.initSdk(this,false);
    }

    //先执行attachBaseContext，再执行onCreate
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //nothing to do
        AppStoreWrapperImpl.getInstance().attachBaseContext(base);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //结束所有应活动
        ActivityManager.finishAll();
        //结束当前正在运行中的java虚拟机
        System.exit(0);
    }

    public static final AppStoreApplication getInstance() {
        return sInstance;
    }
}
