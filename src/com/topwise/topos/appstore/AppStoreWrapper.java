package com.topwise.topos.appstore;

import java.util.ArrayList;

import com.topwise.topos.appstore.data.AppInfo;
import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.manager.AppManager;
import com.topwise.topos.appstore.utils.Properties;

import android.content.Context;
import android.content.Intent;

public class AppStoreWrapper {
    
    /**
     * 引擎版本号
     */
    public static int versionCode = 22;
    /**
     * 引擎版本号名称
     */
    public static String versionName = "1.0.22";
    
    /**
     * 构造方法
     * @param applicationContext 宿主应用的context
     */
    public AppStoreWrapper(Context applicationContext) {
        AppStoreWrapperImpl.getInstance().setApplicationContext(applicationContext);
        AppStoreWrapperImpl.getInstance().init();
    }

    public void destroy() {
        AppStoreWrapperImpl.getInstance().destroy();
    }
    
    /**
     * 传入用户id
     * @param userId 用户id
     */
    public void setUserId(String userId) {
        AppStoreWrapperImpl.getInstance().setUserId(userId);
    }
    
    /**
     * 传入AppStore activity界面的intent action
     * @param action AppStore activity界面的intent action
     */
    public void setAppStorePageIntentAction(String action) {
        
    }
    
    /**
     * 下载界面intent action
     * @return 下载界面intent action
     */
    public String getDownloadActivityIntentAction() {
        return Properties.INTENT_DOWNLOAD_ACTIVITY;
    }
    
    /**
     * 搜索界面intent action
     * @return 搜索界面intent action
     */
    public String getSearchActivityIntentAction() {
        return Properties.INTENT_SEARCH_ACTIVITY;
    }
    
    /**
     * 直接进入搜索结果列表界面
     * @param context 上下文
     * @param keyword 搜索关键字
     */
    public void startSearchResultActivity(Context context, String keyword) {
        if (context == null || keyword == null || "".equals(keyword)) {
            return;
        }
        Intent intent = new Intent(Properties.INTENT_SEARCH_ACTIVITY);
        intent.putExtra("keyword", keyword);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    
    /**
     * 注册应用更新个数监听
     * @param listener 监听
     */
    public void setAppUpgradeCountListener(AppUpgradeCountListener listener) {
        int count = 0;
        ArrayList<AppInfo> appInfos = DataPool.getInstance().getAppInfos(DataPool.TYPE_APP_NEED_UPGRADE);
        if (appInfos != null) {
            count = appInfos.size();
        }
        listener.appUpgradeCount(count);
        if (count == 0) {
            AppManager.getInstance().loadNeedUpgradeApps();
        }
        if (AppStoreWrapperImpl.getInstance().mAppUpgradeCountListeners.contains(listener)) {
            return;
        }
        AppStoreWrapperImpl.getInstance().mAppUpgradeCountListeners.add(listener);
    }
    
    /**
     * 应用更新个数监听
     */
    public static interface AppUpgradeCountListener {
        /**
         * 应用更新个数监听F
         * @param count 需更新的个数
         */
        public void appUpgradeCount(int count);
    }
    
    /**
     * 设置欢迎界面消失回调
     * @param r 回调
     */
    public void setWelcomeGoneRunnable(Runnable r) {
        if (AppStoreWrapperImpl.getInstance().mWelcomeGoneRunnables.contains(r)) {
            return;
        }
        AppStoreWrapperImpl.getInstance().mWelcomeGoneRunnables.add(r);
    }
    
    /**
     * 注册送沃豆回调
     * @param listener 送沃豆回调
     */
    public void setOnNeedGiftWoDouListener(OnGiftWoDouListener listener) {
        if (AppStoreWrapperImpl.getInstance().mOnGiftWoDouListeners.contains(listener)) {
            return;
        }
        AppStoreWrapperImpl.getInstance().mOnGiftWoDouListeners.add(listener);
    }
    
    /**
     * 赠送沃豆的结果
     * @param packageName 应用包名
     * @param result 赠送结果
     */
    public void giftWoDouResult(String packageName, boolean result) {
        
    }
    
    /**
     * 送沃豆回调，引擎通知宿主应用
     */
    public static interface OnGiftWoDouListener {
        /**
         * 送沃豆回调，引擎通知宿主应用
         * @param packageName 应用包名
         * @param wodou 沃豆数量
         */
        public void needGiftWoDou(String packageName, int wodou);
    }
    
}
