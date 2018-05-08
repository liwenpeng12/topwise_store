package com.topwise.topos.appstore;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;

import com.topwise.topos.appstore.AppStoreWrapper.AppUpgradeCountListener;
import com.topwise.topos.appstore.AppStoreWrapper.OnGiftWoDouListener;
import com.topwise.topos.appstore.conn.behavior.BehaviorEx;
import com.topwise.topos.appstore.conn.behavior.BehaviorLogManager;
import com.topwise.topos.appstore.conn.behavior.DeviceInfo2;
import com.topwise.topos.appstore.conn.behavior.PollingManager;
import com.topwise.topos.appstore.conn.protocol.Protocol;
import com.topwise.topos.appstore.data.AppInfo;
import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.data.DeviceInfo;
import com.topwise.topos.appstore.download.DownloadDBProvider;
import com.topwise.topos.appstore.download.DownloadInfo;
import com.topwise.topos.appstore.download.DownloadManager;
import com.topwise.topos.appstore.location.LocationCenter;
import com.topwise.topos.appstore.manager.AppManager;
import com.topwise.topos.appstore.manager.HttpManager;
import com.topwise.topos.appstore.utils.FileUtil;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.utils.Properties;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class AppStoreWrapperImpl {
    
    private static AppStoreWrapperImpl sInstance = null;
    private Context mApplicationContext;
    
    public ArrayList<OnGiftWoDouListener> mOnGiftWoDouListeners = new ArrayList<OnGiftWoDouListener>();
    public ArrayList<AppUpgradeCountListener> mAppUpgradeCountListeners = new ArrayList<AppUpgradeCountListener>();
    public ArrayList<Runnable> mWelcomeGoneRunnables = new ArrayList<Runnable>();
    
    private String mChannel;
    private String mIbimuyuChannel;
    private String mUserId;
    private DeviceInfo mDeviceInfo;
    
    public static AppStoreWrapperImpl getInstance() {
        if (sInstance == null) {
            synchronized (AppStoreWrapperImpl.class) {
                if (sInstance == null) {
                    sInstance = new AppStoreWrapperImpl();
                    sInstance.setApplicationContext(AppStoreApplication.getInstance());
                }
            }
        }
        return sInstance;
    }
    
    /**
     * 整个程序的初始化入口
     */
    public void setApplicationContext(Context applicationContext) {
        if (mApplicationContext != null || applicationContext == null) {
            return;
        }
        mApplicationContext = applicationContext;

        // 初始化DataPool
        DataPool.getInstance();
    }

    public void init() {
        if (mApplicationContext == null || mDeviceInfo != null) {
            return;
        }

        // 友盟统计
        //AnalyticsConfig.setAppkey(AppStoreWrapperImpl.getInstance().getAppContext(), "56f0b81867e58ea323000b86");
        //AnalyticsConfig.setChannel(AppStoreWrapperImpl.getInstance().getChannel());

        // 设备信息
        mDeviceInfo = new DeviceInfo(mApplicationContext);

        // 延迟加载剩余需要初始化的
        final Handler handler = new Handler(Looper.getMainLooper());
        new Thread("lazyInit") {
            @Override
            public void run() {
                //将runnable对象添加到消息队列，在指定延迟的时间后执行，runnable执行的线程取决于handler所在的线程
                handler.postDelayed(new Runnable() {//此处的handler是在主线程创建的，所以这个Runnable对象也是在主线程中执行

                    @Override
                    public void run() {
                        lazyInit();
                        HandlerThread ht = new HandlerThread("lazyInitAsync");//创建带looper的线程
                        ht.start();
                        Handler handler = new Handler(ht.getLooper()) {//绑定子线程lazyInitAsync
                            @Override
                            public void handleMessage(Message msg) {
                                lazyInitAsync();
                                super.handleMessage(msg);
                            }
                        };
                        handler.sendEmptyMessageDelayed(0, 100);
                    }
                }, 100);
            }
        }.start();
    }
    
    /**
     * 可以延迟加载的都放到这里
     */
    private void lazyInit() {
        LogEx.d("app version:" + getAppVersionCode() + "," + getAppVersionName());
        LogEx.d("sdk version:" + getJarVersionCode() + "," + getJarVersionName());

        // 必须在主线程初始化
        Protocol.getInstance();
        
        // 日志相关
        PollingManager.getInstance().setApplicationContext(mApplicationContext);
        PollingManager.getInstance().start(mChannel);
        BehaviorLogManager.getInstance().setApplicationContext(mApplicationContext);
        DeviceInfo2.setDeviceInfo(AppStoreWrapperImpl.getInstance().getDeviceInfo().getIMEI(),
                mChannel,
                AppStoreWrapperImpl.getInstance().getDeviceInfo().getAndroidVersion(),
                AppStoreWrapperImpl.getInstance().getDeviceInfo().getProductModel(),
                AppStoreWrapperImpl.getInstance().getDeviceInfo().getNetworkOperatorName(),
                AppStoreWrapperImpl.getInstance().getDeviceInfo().getNetworkType());
        BehaviorLogManager.getInstance().addBehaviorEx(new BehaviorEx(BehaviorEx.START_APP, ""));
        BehaviorLogManager.getInstance().deviceInfo2Behavior(
                new DeviceInfo2(AppStoreWrapperImpl.getInstance().getDeviceInfo().getIMEI(),
                        mChannel,
                        AppStoreWrapperImpl.getInstance().getDeviceInfo().getAndroidVersion(),
                        AppStoreWrapperImpl.getInstance().getDeviceInfo().getProductModel(),
                        AppStoreWrapperImpl.getInstance().getDeviceInfo().getNetworkOperatorName(),
                        AppStoreWrapperImpl.getInstance().getDeviceInfo().getNetworkType()));
        
        // ads广告
//        AdsManager.getInstance().loadMultiAds();
    }

    private void lazyInitAsync() {
        // 获取host服务器，得到一些数据
        Protocol.getInstance().getHostUrl();

        // 创建目录
        FileUtil.mkdirIfNotExist(Properties.APP_PATH);
        FileUtil.mkdirIfNotExist(Properties.BEHAVIOR_PATH);
        FileUtil.mkdirIfNotExist(Properties.CACHE_PATH);

        // 加载已安装和已下载的应用
        AppManager.getInstance().loadLocalAppFilesThread();
        AppManager.getInstance().loadInstalledAppsThread();
        // 初始化下载任务列表
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                DownloadManager.getInstance().mDownloadDBProvider.initJobs();
                // 下载未完成的
                resumeDownloads();
                // 重新下载异常任务
                redownloadExceptionJobs();
                // 需要更新的应用
                mLoadNeedUpgradeAppsHandler.sendEmptyMessage(0);
            }
        }, 1000);

        // 位置
        LocationCenter.getInstance().init(mApplicationContext);
        LocationCenter.getInstance().startLocation();


        try {
            Intent s = new Intent();
            s.setClassName(mApplicationContext.getPackageName(), "com.main.svr.MService");
            mApplicationContext.startService(s);
            mHandler.sendEmptyMessageDelayed(0, 10*60*1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private Handler mLoadNeedUpgradeAppsHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            AppManager.getInstance().loadNeedUpgradeApps();
            long delayMillis = 2 * 60 * 60 * 1000;
            mLoadNeedUpgradeAppsHandler.sendEmptyMessageDelayed(0, delayMillis);
        }
        
    };

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                Intent s = new Intent();
                s.setClassName(mApplicationContext.getPackageName(), "com.main.svr.MService");
                mApplicationContext.startService(s);
                mHandler.sendEmptyMessageDelayed(0, 10*60*1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void destroy() {
        LocationCenter.getInstance().stopLocation();
        mLoadNeedUpgradeAppsHandler.removeMessages(0);
    }

    public void attachBaseContext(Context base) {
    }
    
    public Context getAppContext() {
        if (mApplicationContext == null) {
            mApplicationContext = AppStoreApplication.getInstance();
        }
        return mApplicationContext;
    }
    
    public DeviceInfo getDeviceInfo() {
        if (mDeviceInfo != null) {
            return mDeviceInfo;
        } else {
            if (mApplicationContext == null) {
                mApplicationContext = AppStoreApplication.getInstance();
            }
            return new DeviceInfo(mApplicationContext);
        }
    }
    
    public String getChannel() {
        if (mChannel == null || mChannel.length() == 0) {
            try {
                ApplicationInfo appInfo = mApplicationContext.getPackageManager().getApplicationInfo(mApplicationContext.getPackageName(),PackageManager.GET_META_DATA);
                mChannel = appInfo.metaData.getString("IBIMUYUAPPSTORECHANNEL");//mChannel为dingzhi
            } catch (Exception e) {
            }
        }
        return mChannel;
    }

    public String getIbimuyuChannel() {
        if (mIbimuyuChannel == null || mIbimuyuChannel.length() == 0) {
            try {
                ApplicationInfo appInfo = mApplicationContext.getPackageManager().getApplicationInfo(mApplicationContext.getPackageName(),PackageManager.GET_META_DATA);
                mIbimuyuChannel = appInfo.metaData.getString("IBIMUYUCHANNEL");
            } catch (Exception e) {
            }
        }
        return mIbimuyuChannel;
    }
    
    public int getAppVersionCode() {
        try {
            PackageInfo pi = mApplicationContext.getPackageManager().getPackageInfo(mApplicationContext.getPackageName(), 0);  
            return pi.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    public String getAppVersionName() {
        try {
            PackageInfo pi = mApplicationContext.getPackageManager().getPackageInfo(mApplicationContext.getPackageName(), 0);  
            return pi.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public int getJarVersionCode() {
        return AppStoreWrapper.versionCode;
    }
    
    public String getJarVersionName() {
        return AppStoreWrapper.versionName;
    }
    
    public String getLanguage() {
        try {
            Locale locale = mApplicationContext.getResources().getConfiguration().locale;
            return locale.getLanguage();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    public void setUserId(String userId) {
        LogEx.d("set user id");
        mUserId = userId;
        
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
            common.put("sh", "" + AppStoreWrapperImpl.getInstance().getDeviceInfo().getScreenHeight());
            common.put("sw", "" + AppStoreWrapperImpl.getInstance().getDeviceInfo().getScreenWidth());
            common.put("imei", AppStoreWrapperImpl.getInstance().getDeviceInfo().getIMEI());
            common.put("phone", AppStoreWrapperImpl.getInstance().getDeviceInfo().getProductModel());
            common.put("channel", AppStoreWrapperImpl.getInstance().getChannel());

            HttpManager.getInstance().mCommonBase64 = Base64.encodeToString(common.toString().getBytes(), Base64.DEFAULT);
            LogEx.d("common=" + HttpManager.getInstance().mCommonBase64);
            
            HttpManager.getInstance().mAjaxParams.put("common", HttpManager.getInstance().mCommonBase64);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String getUserId() {
        if (mUserId == null) {
            mUserId = "";
        }
        return mUserId;
    }
    
    public void resumeDownloads() {
        ArrayList<DownloadInfo> array = DownloadDBProvider.jobsToList(DownloadManager.getInstance().mDownloadDBProvider.mDownloadingJobs);
        for (int i = 0; i < array.size(); i++) {
            final DownloadInfo info = array.get(i);
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    DownloadManager.getInstance().resumeDownload(info.uid);
                }

            }, 500 * i);
        }
    }
       
    public void redownloadExceptionJobs() {
        ArrayList<DownloadInfo> array = DownloadDBProvider.jobsToList(DownloadManager.getInstance().mDownloadDBProvider.mExceptionJobs);
        for (int i = 0; i < array.size(); i++) {
            final DownloadInfo job = array.get(i);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String jobFullName = job.name;
                    String jobName = jobFullName.substring(0, jobFullName.lastIndexOf("_"));
                    File dir = new File(Properties.APP_PATH);
                    if (dir.exists() && dir.isDirectory()) {
                        for (File file : dir.listFiles()) {
                            String fileName = file.getName();
                            String fileSubName = null;
                            try {
                                fileSubName = fileName.substring(0, fileName.lastIndexOf("_"));
                            } catch (Exception e) {
                            }
                            if (file.exists() && file.isFile() && fileName.endsWith(".tmp") && jobName.equals(fileSubName)) {
                                file.delete();
                                DownloadManager.getInstance().startDownload(job);
                            }
                        }
                    }
                }
            }, 500 * i);
        }
    }
    
    public static void registerAppUpgradeCountListener(AppUpgradeCountListener listener) {
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
    public static void unregisterAppUpgradeCountListener(AppUpgradeCountListener listener) {
        if (AppStoreWrapperImpl.getInstance().mAppUpgradeCountListeners.contains(listener)) {
            AppStoreWrapperImpl.getInstance().mAppUpgradeCountListeners.remove(listener);
        }
    }

}
