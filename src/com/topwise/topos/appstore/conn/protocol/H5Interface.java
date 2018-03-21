package com.topwise.topos.appstore.conn.protocol;

import android.content.Intent;
import android.webkit.JavascriptInterface;

import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.data.AppInfo;
import com.topwise.topos.appstore.data.DeviceInfoJson;
import com.topwise.topos.appstore.manager.AppManager;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.view.activity.AppDetailActivity;

public class H5Interface {

    private static DeviceInfoJson mDeviceInfo = null;

    public H5Interface() {
    }

    @JavascriptInterface
    public String getDeviceInfo() {
        if (mDeviceInfo == null) {
            mDeviceInfo = new DeviceInfoJson();
            mDeviceInfo.init(AppStoreWrapperImpl.getInstance().getAppContext());
        }
        LogEx.d("getDeviceInfo:" + mDeviceInfo.toString());
        return mDeviceInfo.toString();
    }

    /**
     * 获取meid
     * @return meid
     */
    @JavascriptInterface
    public String getMeid() {
        return AppStoreWrapperImpl.getInstance().getDeviceInfo().getIMEI();
    }
    
    /**
     * 获取当前系统语言
     * @return 当前系统语言
     */
    @JavascriptInterface
    public String getDeviceLanguage() {
        return AppStoreWrapperImpl.getInstance().getLanguage();
    }
    
    /**
     * 获取渠道号
     * @return 渠道号
     */
    @JavascriptInterface
    public String getChannel() {
        return AppStoreWrapperImpl.getInstance().getChannel();
    }
    
    /**
     * 启动一个intent
     * @param intentUri intent字符串
     */
    @JavascriptInterface
    public void startIntent(String intentUri) {
        try {
            AppStoreWrapperImpl.getInstance().getAppContext().startActivity(Intent.parseUri(intentUri, 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置H5界面标题
     * @param title 标题
     */
    @JavascriptInterface
    public void setActionBarTitle(String title) {
        
    }
    
    /**
     * 下载应用
     * @param packageName 包名
     */
    @JavascriptInterface
    public void downloadApp(String packageName) {
        AppInfo info = new AppInfo();
        info.id = info.pkg = packageName;
        AppManager.getInstance().startDownloadApp(info);
    }
    
    /**
     * 进入应用详情
     * @param packageName 包名
     */
    @JavascriptInterface
    public void viewAppDetail(String packageName) {
        Intent intent = new Intent(AppStoreWrapperImpl.getInstance().getAppContext(), AppDetailActivity.class);
        intent.putExtra("app_id", packageName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        AppStoreWrapperImpl.getInstance().getAppContext().startActivity(intent);
    }
    
    /**
     * 分享网址
     * @param url 网址
     */
    @JavascriptInterface
    public void shareUrl(String url) {
        
    }

    /**
     * 拓美游戏账号信息
     * @param userId 用户唯一id
     * @param userJson 用户信息封装到json中，拓美定义json格式，双方线下确认格式。json中需包含拓美可以得到的所有用户信息，包括但不限于用户名，手机号，年龄等。
     */
    @JavascriptInterface
    public void tuomeiUserInfo(String userId, String userJson) {
        LogEx.d("tuomeiUserInfo:userId=" + userId + ",userJson=" + userJson);
    }

    /**
     * 拓美支付信息
     * @param userId 用户唯一id
     * @param time 支付时间，格式：YYYY-MM-DD HH:MM:SS
     * @param cent 支付金额，单位：分
     * @param subject 支付内容标题
     * @param content 支付内容
     */
    @JavascriptInterface
    public void tuomeiPay(String userId, String time, int cent, String subject, String content) {
        LogEx.d("tuomeiPay:userId=" + userId + ",time=" + time + ",cent=" + cent + ",subject=" + subject + ",content=" + content);
    }

}
