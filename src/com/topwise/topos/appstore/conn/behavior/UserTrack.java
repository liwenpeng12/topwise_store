package com.topwise.topos.appstore.conn.behavior;

import org.json.JSONException;
import org.json.JSONObject;

import com.topwise.topos.appstore.conn.protocol.Protocol;
import com.topwise.topos.appstore.data.Ad;
import com.topwise.topos.appstore.data.AdIcon;
import com.topwise.topos.appstore.data.AppInfo;
import com.topwise.topos.appstore.data.Banner;
import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.data.NotificationInfo;
import com.topwise.topos.appstore.data.Welcome;
import com.topwise.topos.appstore.manager.AppManager;
import com.topwise.topos.appstore.manager.HttpManager;
import com.topwise.topos.appstore.utils.Utils;

import android.util.Base64;

public class UserTrack {
    private static UserTrack mThis = null;
    
    public static UserTrack getInstance() {
        if (mThis == null) {
            synchronized (UserTrack.class) {
                if (mThis == null) {
                    mThis = new UserTrack();
                }
            }
        }
        return mThis;
    }

    public void reportAppShowed(AppInfo info) {
        BehaviorLogManager.getInstance().addBehaviorEx(new BehaviorEx(BehaviorEx.VIEW_APP, info.id + "@" + info.from));
        for (int i = 0; i < info.report_show.size(); i++) {
            HttpManager.getInstance().get(info.report_show.get(i));
        }
        if (info.bindId != null && info.bindId.length() > 0) {
//            com.ak.firm.shell.FirmSdk.onAppShowed(AppStoreWrapperImpl.getInstance().getAppContext(), info.bindId);
        }
    }

    public void reportAppClicked(AppInfo info) {
        BehaviorLogManager.getInstance().addBehaviorEx(new BehaviorEx(BehaviorEx.ENTER_APP_DETAIL, info.id));
        if (info.bindId != null && info.bindId.length() > 0) {
//            com.ak.firm.shell.FirmSdk.onAppClicked(AppStoreWrapperImpl.getInstance().getAppContext(), info.bindId);
        }
    }
    
    /**
     * app开始下载
     * @param info 应用信息
     * @param pageName 页面名称（activity名或者fragment名）
     */
    public void startDownloadApp(AppInfo info, String pageName) {
        HttpManager.getInstance().mAjaxParams.clear();
        HttpManager.getInstance().mAjaxParams.put("common", HttpManager.getInstance().mCommonBase64);
        HttpManager.getInstance().mAjaxParams.put("pkey", "" + Protocol.getInstance().pkey);
        if (pageName == null) {
            pageName = "";
        }
        JSONObject log = new JSONObject();
        try {
            log.put("type", "app_dl_start");
            log.put("id", info.id);
            log.put("page", pageName);
            log.put("time", System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String logBase64 = Base64.encodeToString(log.toString().getBytes(), Base64.DEFAULT);
        HttpManager.getInstance().mAjaxParams.put("log", logBase64);
        String tm = "" + System.currentTimeMillis()/1000;
        HttpManager.getInstance().mAjaxParams.put("tm", tm);
        String secret = Utils.getMd5(HttpManager.getInstance().mCommonBase64 + logBase64 + Protocol.getInstance().pkey + Protocol.getInstance().psecret + tm);
        HttpManager.getInstance().mAjaxParams.put("secret", secret);
        HttpManager.getInstance().post(Protocol.getInstance().getServerUrl() + "/log");

        if (info.flag == AppInfo.FLAG_ONLINE) {
            // 新下载应用统计，更新应用不统计
            for (int i = 0; i < info.report_start_download.size(); i++) {
                HttpManager.getInstance().get(info.report_start_download.get(i));
            }
            for (int i = 0; i < info.bill_report_start_download.size(); i++) {
                HttpManager.getInstance().get(info.bill_report_start_download.get(i));
            }
        }

        if (info.bindId != null && info.bindId.length() > 0) {
//            com.ak.firm.shell.FirmSdk.onAppDownloadStart(AppStoreWrapperImpl.getInstance().getAppContext(), info.bindId);
        }
    }
    
    /**
     * app下载完成
     * @param info 应用信息
     */
    public void downloadAppSuccess(AppInfo info) {
        HttpManager.getInstance().mAjaxParams.clear();
        HttpManager.getInstance().mAjaxParams.put("common", HttpManager.getInstance().mCommonBase64);
        HttpManager.getInstance().mAjaxParams.put("pkey", "" + Protocol.getInstance().pkey);
        JSONObject log = new JSONObject();
        try {
            log.put("type", "app_dl_end");
            log.put("id", info.id);
            log.put("time", System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String logBase64 = Base64.encodeToString(log.toString().getBytes(), Base64.DEFAULT);
        HttpManager.getInstance().mAjaxParams.put("log", logBase64);
        String tm = "" + System.currentTimeMillis()/1000;
        HttpManager.getInstance().mAjaxParams.put("tm", tm);
        String secret = Utils.getMd5(HttpManager.getInstance().mCommonBase64 + logBase64 + Protocol.getInstance().pkey + Protocol.getInstance().psecret + tm);
        HttpManager.getInstance().mAjaxParams.put("secret", secret);
        HttpManager.getInstance().post(Protocol.getInstance().getServerUrl() + "/log");

        for (int i = 0; i < info.bill_report_downloaded.size(); i++) {
            HttpManager.getInstance().get(info.bill_report_downloaded.get(i));
        }

        if (info.bindId != null && info.bindId.length() > 0) {
//            com.ak.firm.shell.FirmSdk.onAppDownloadCompleted(AppStoreWrapperImpl.getInstance().getAppContext(), info.bindId);
        }
    }
    
    /**
     * app点击安装
     * @param info 应用信息
     */
    public void startInstallApp(AppInfo info) {
        HttpManager.getInstance().mAjaxParams.clear();
        HttpManager.getInstance().mAjaxParams.put("common", HttpManager.getInstance().mCommonBase64);
        HttpManager.getInstance().mAjaxParams.put("pkey", "" + Protocol.getInstance().pkey);
        JSONObject log = new JSONObject();
        try {
            log.put("type", "app_instrall_start");
            log.put("id", info.id);
            log.put("time", System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String logBase64 = Base64.encodeToString(log.toString().getBytes(), Base64.DEFAULT);
        HttpManager.getInstance().mAjaxParams.put("log", logBase64);
        String tm = "" + System.currentTimeMillis()/1000;
        HttpManager.getInstance().mAjaxParams.put("tm", tm);
        String secret = Utils.getMd5(HttpManager.getInstance().mCommonBase64 + logBase64 + Protocol.getInstance().pkey + Protocol.getInstance().psecret + tm);
        HttpManager.getInstance().mAjaxParams.put("secret", secret);
        HttpManager.getInstance().post(Protocol.getInstance().getServerUrl() + "/log");
    }
    
    /**
     * app安装完成
     * @param info 应用信息
     */
    public void installAppSuccess(AppInfo info) {
        HttpManager.getInstance().mAjaxParams.clear();
        HttpManager.getInstance().mAjaxParams.put("common", HttpManager.getInstance().mCommonBase64);
        HttpManager.getInstance().mAjaxParams.put("pkey", "" + Protocol.getInstance().pkey);
        JSONObject log = new JSONObject();
        try {
            log.put("type", "app_instrall_end");
            log.put("id", info.pkg);
            log.put("time", System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String logBase64 = Base64.encodeToString(log.toString().getBytes(), Base64.DEFAULT);
        HttpManager.getInstance().mAjaxParams.put("log", logBase64);
        String tm = "" + System.currentTimeMillis()/1000;
        HttpManager.getInstance().mAjaxParams.put("tm", tm);
        String secret = Utils.getMd5(HttpManager.getInstance().mCommonBase64 + logBase64 + Protocol.getInstance().pkey + Protocol.getInstance().psecret + tm);
        HttpManager.getInstance().mAjaxParams.put("secret", secret);
        HttpManager.getInstance().post(Protocol.getInstance().getServerUrl() + "/log");

        for (int i = 0; i < info.bill_report_installed.size(); i++) {
            HttpManager.getInstance().get(info.bill_report_installed.get(i));
        }

        if (info.bindId != null && info.bindId.length() > 0) {
//            com.ak.firm.shell.FirmSdk.onAppInstalled(AppStoreWrapperImpl.getInstance().getAppContext(), info.bindId);
        }
    }

    /**
     * 收到系统发出的激活应用的广播，上报激活
     * @param packageName 包名
     */
    public void reportFirstLaunchApp(String packageName) {
        AppInfo info = AppManager.parseInstalledApp(packageName);
        AppInfo appInfo = DataPool.getInstance().getAppInfo(info.id);
        if (appInfo != null && appInfo.name != null && appInfo.name.length() != 0) {
            info = appInfo;
        }
        if (info == null) {
            return;
        }
        for (int i = 0; i < info.bill_report_first_launch.size(); i++) {
            HttpManager.getInstance().get(info.bill_report_first_launch.get(i));
        }
        if (info.bindId != null && info.bindId.length() > 0) {
//            com.ak.firm.shell.FirmSdk.onAppActived(AppStoreWrapperImpl.getInstance().getAppContext(), info.bindId);
        }
    }
    
    /**
     * ad/push点击查看详情
     * @param ad 广告
     */
    public void openAd(Ad ad) {
        HttpManager.getInstance().mAjaxParams.clear();
        HttpManager.getInstance().mAjaxParams.put("common", HttpManager.getInstance().mCommonBase64);
        HttpManager.getInstance().mAjaxParams.put("pkey", "" + Protocol.getInstance().pkey);
        JSONObject log = new JSONObject();
        try {
            log.put("type", "adevent");
            log.put("title", ad.title);
            if (ad.getClass().equals(Welcome.class)) {
                log.put("source", "welcome");
            } else if (ad.getClass().equals(NotificationInfo.class)) {
                log.put("source", "notification");
            } else {
                log.put("source", "ad");
            }
            if (ad.event == 1) {
                log.put("target", "h5");
                log.put("targetid", ad.event_data.url);
            } else if (ad.event == 2) {
                log.put("target", "app");
                log.put("targetid", ad.event_data.packagename);
            } else if (ad.event == 3) {
                log.put("target", ad.event_data.type);
                log.put("targetid", ad.event_data.type_id);
            }
            log.put("time", System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String logBase64 = Base64.encodeToString(log.toString().getBytes(), Base64.DEFAULT);
        HttpManager.getInstance().mAjaxParams.put("log", logBase64);
        String tm = "" + System.currentTimeMillis()/1000;
        HttpManager.getInstance().mAjaxParams.put("tm", tm);
        String secret = Utils.getMd5(HttpManager.getInstance().mCommonBase64 + logBase64 + Protocol.getInstance().pkey + Protocol.getInstance().psecret + tm);
        HttpManager.getInstance().mAjaxParams.put("secret", secret);
        HttpManager.getInstance().post(Protocol.getInstance().getServerUrl() + "/log");
    }
    
    /**
     * ad/push点击查看详情
     * @param banner banner
     */
    public void openAd(Banner banner) {
        HttpManager.getInstance().mAjaxParams.clear();
        HttpManager.getInstance().mAjaxParams.put("common", HttpManager.getInstance().mCommonBase64);
        HttpManager.getInstance().mAjaxParams.put("pkey", "" + Protocol.getInstance().pkey);
        JSONObject log = new JSONObject();
        try {
            log.put("type", "adevent");
            log.put("title", banner.title);
            log.put("target", banner.target);
            log.put("targetid", banner.target_url);
            log.put("time", System.currentTimeMillis());
            log.put("source", "banner");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String logBase64 = Base64.encodeToString(log.toString().getBytes(), Base64.DEFAULT);
        HttpManager.getInstance().mAjaxParams.put("log", logBase64);
        String tm = "" + System.currentTimeMillis()/1000;
        HttpManager.getInstance().mAjaxParams.put("tm", tm);
        String secret = Utils.getMd5(HttpManager.getInstance().mCommonBase64 + logBase64 + Protocol.getInstance().pkey + Protocol.getInstance().psecret + tm);
        HttpManager.getInstance().mAjaxParams.put("secret", secret);
        HttpManager.getInstance().post(Protocol.getInstance().getServerUrl() + "/log");
    }
    
    /**
     * ad/push点击查看详情
     * @param adicon ad icon
     */
    public void openAd(AdIcon adicon) {
        HttpManager.getInstance().mAjaxParams.clear();
        HttpManager.getInstance().mAjaxParams.put("common", HttpManager.getInstance().mCommonBase64);
        HttpManager.getInstance().mAjaxParams.put("pkey", "" + Protocol.getInstance().pkey);
        JSONObject log = new JSONObject();
        try {
            log.put("type", "adevent");
            log.put("title", adicon.title);
            log.put("target", adicon.target);
            log.put("targetid", adicon.target_url);
            log.put("time", System.currentTimeMillis());
            log.put("source", "adicon");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String logBase64 = Base64.encodeToString(log.toString().getBytes(), Base64.DEFAULT);
        HttpManager.getInstance().mAjaxParams.put("log", logBase64);
        String tm = "" + System.currentTimeMillis()/1000;
        HttpManager.getInstance().mAjaxParams.put("tm", tm);
        String secret = Utils.getMd5(HttpManager.getInstance().mCommonBase64 + logBase64 + Protocol.getInstance().pkey + Protocol.getInstance().psecret + tm);
        HttpManager.getInstance().mAjaxParams.put("secret", secret);
        HttpManager.getInstance().post(Protocol.getInstance().getServerUrl() + "/log");
    }
}
