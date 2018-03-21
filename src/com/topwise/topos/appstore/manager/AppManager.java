package com.topwise.topos.appstore.manager;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.topwise.topos.appstore.AppStoreWrapper.AppUpgradeCountListener;
import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.api.AppStoreApi;
import com.topwise.topos.appstore.conn.behavior.UserTrack;
import com.topwise.topos.appstore.conn.http.AjaxCallBack;
import com.topwise.topos.appstore.conn.protocol.Protocol;
import com.topwise.topos.appstore.data.AppInfo;
import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.download.DownloadInfo;
import com.topwise.topos.appstore.download.DownloadManager;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.utils.Properties;
import com.topwise.topos.appstore.utils.Utils;

public class AppManager extends BaseManager {

    private static AppManager mThis = null;
    
    private DownloadManager mDownloadManager;
    
    private ArrayList<InstallStatusListener> mInstallStatusListeners = new ArrayList<InstallStatusListener>();
    private ArrayList<UninstallStatusListener> mUninstallStatusListeners = new ArrayList<UninstallStatusListener>();
    
    public static AppManager getInstance() {
        if (mThis == null) {
            synchronized (AppManager.class) {
                if (mThis == null) {
                    mThis = new AppManager();
                }
            }
        }
        return mThis;
    }
    
    protected AppManager(){
        mDownloadManager = DownloadManager.getInstance();
    }
    
    /**
     * label
     * @param labelId label id
     * @param callback 回调
     */
    public void loadLabel(int labelId, final ManagerCallback callback) {
        final int dataType = DataPool.TYPE_LABEL + labelId;
        HttpManager.getInstance().post(Protocol.getInstance().getLabelUrl(labelId), new AjaxCallBack<String>() {

            @Override
            public void onSuccess(final String t) {
                LogEx.d(t);
                mMainThreadHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        String res = Protocol.getInstance().parseLabel(t, dataType);
                        if (callback == null) {
                            return;
                        }
                        if ("true".equals(res)) {
                            callback.onSuccess(Properties.MODULE_TYPE_LABEL, dataType, 0, 0, true);
                        } else {
                            callback.onFailure(Properties.MODULE_TYPE_LABEL, dataType, null, -1, res);
                        }
                    }
                    
                });
                super.onSuccess(t);
            }

            @Override
            public void onFailure(final Throwable t, final int errorNo, final String strMsg) {
                LogEx.e("onFailure,Throwable:" + t + ",errorNo:" + errorNo + ",strMsg:" + strMsg);
                mMainThreadHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        if (callback == null) {
                            return;
                        }
                        callback.onFailure(Properties.MODULE_TYPE_LABEL, dataType, t, errorNo, strMsg);
                    }
                    
                });
                super.onFailure(t, errorNo, strMsg);
            }
            
        });
    }
    
    /**
     * 榜单
     * @param rankId 榜单id
     * @param callback 回调
     */
    public void loadRank(int rankId, final ManagerCallback callback) {
        final int dataType = DataPool.TYPE_RANK + rankId;
        HttpManager.getInstance().post(Protocol.getInstance().getRankUrl(rankId), new AjaxCallBack<String>() {

            @Override
            public void onSuccess(final String t) {
                LogEx.d(t);
                mMainThreadHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        String res = Protocol.getInstance().parseRank(t, dataType);
                        if (callback == null) {
                            return;
                        }
                        if ("true".equals(res)) {
                            callback.onSuccess(Properties.MODULE_TYPE_RANK, dataType, 0, 0, true);
                        } else {
                            callback.onFailure(Properties.MODULE_TYPE_RANK, dataType, null, -1, res);
                        }
                    }
                    
                });
                super.onSuccess(t);
            }

            @Override
            public void onFailure(final Throwable t, final int errorNo, final String strMsg) {
                LogEx.e("onFailure,Throwable:" + t + ",errorNo:" + errorNo + ",strMsg:" + strMsg);
                mMainThreadHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        if (callback == null) {
                            return;
                        }
                        callback.onFailure(Properties.MODULE_TYPE_RANK, dataType, t, errorNo, strMsg);
                    }
                    
                });
                super.onFailure(t, errorNo, strMsg);
            }
            
        });
    }
    
    /**
     * 分类
     * @param typeId 分类id
     * @param callback 回调
     */
    public void loadType(int typeId, final ManagerCallback callback) {
        final int dataType = DataPool.TYPE_TYPE + typeId;
        final int page = DataPool.getInstance().getTypePage(dataType);
        HttpManager.getInstance().post(Protocol.getInstance().getTypeUrl(typeId, page), new AjaxCallBack<String>() {

            @Override
            public void onSuccess(final String t) {
                LogEx.d(t);
                mMainThreadHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        ArrayList<AppInfo> infos = new ArrayList<AppInfo>();
                        String res = Protocol.getInstance().parseType(t, dataType, infos);
                        if ("true".equals(res)) {
                            if (infos.size() < Properties.PAGE_NUM) {
                                if (callback != null) {
                                    callback.onSuccess(Properties.MODULE_TYPE_TYPE, dataType, page, infos.size(), true);
                                }
                            } else {
                                if (callback != null) {
                                    callback.onSuccess(Properties.MODULE_TYPE_TYPE, dataType, page, infos.size(), false);
                                }
                            }
                            DataPool.getInstance().addTypePage(dataType, page+1);
                        } else {
                            if (callback != null) {
                                callback.onFailure(Properties.MODULE_TYPE_TYPE, dataType, null, -1, res);
                            }
                        }
                    }
                    
                });

                super.onSuccess(t);
            }

            @Override
            public void onFailure(final Throwable t, final int errorNo, final String strMsg) {
                LogEx.e("onFailure,Throwable:" + t + ",errorNo:" + errorNo + ",strMsg:" + strMsg);
                mMainThreadHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        if (callback == null) {
                            return;
                        }
                        callback.onFailure(Properties.MODULE_TYPE_TYPE, dataType, t, errorNo, strMsg);
                    }
                    
                });
                super.onFailure(t, errorNo, strMsg);
            }
            
        });
    }
    
    /**
     * 应用详情
     * @param info 应用info
     * @param callback 回调
     */
    public void loadAppDetail(AppInfo info, final ManagerCallback callback) {
        if (info == null || info.id == null) {
            return;
        }
        HttpManager.getInstance().post(Protocol.getInstance().getAppDetailUrl(info.id), new AjaxCallBack<String>() {

            @Override
            public void onSuccess(final String t) {
                LogEx.d(t);
                mMainThreadHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        String res = Protocol.getInstance().parseAppDetail(t);
                        if (callback == null) {
                            return;
                        }
                        if ("true".equals(res)) {
                            callback.onSuccess(Properties.MODULE_TYPE_APP, -1, 0, 0, true);
                        } else {
                            callback.onFailure(Properties.MODULE_TYPE_APP, -1, null, -1, res);
                        }
                    }
                    
                });
                super.onSuccess(t);
            }

            @Override
            public void onFailure(final Throwable t, final int errorNo, final String strMsg) {
                LogEx.e("onFailure,Throwable:" + t + ",errorNo:" + errorNo + ",strMsg:" + strMsg);
                mMainThreadHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        if (callback == null) {
                            return;
                        }
                        callback.onFailure(Properties.MODULE_TYPE_APP, -1, t, errorNo, strMsg);
                    }
                    
                });
                super.onFailure(t, errorNo, strMsg);
            }
            
        });
    }
    
    /**
     * 相关应用，4种相关，一次调用，回调4次，0-类似应用 1-大家喜欢 2-同时下载 3-热门应用
     * @param info 应用info
     * @param callback 回调
     */
    public void loadRelated(AppInfo info, final int relatedType, final ManagerCallback callback) {
        if (info == null || info.id == null) {
            return;
        }
        HttpManager.getInstance().post(Protocol.getInstance().getRelatedUrl(relatedType, info.id, info.type), new AjaxCallBack<String>() {

            @Override
            public void onSuccess(final String t) {
                LogEx.d(t);
                mMainThreadHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        String res = Protocol.getInstance().parseRelated(t, relatedType);
                        if (callback == null) {
                            return;
                        }
                        if ("true".equals(res)) {
                            callback.onSuccess(Properties.MODULE_TYPE_APP, DataPool.TYPE_APP_RELATED_SIMILAR + relatedType, 0, 0, true);
                        } else {
                            callback.onFailure(Properties.MODULE_TYPE_APP, DataPool.TYPE_APP_RELATED_SIMILAR + relatedType, null, -1, res);
                        }
                    }

                });
                super.onSuccess(t);
            }

            @Override
            public void onFailure(final Throwable t, final int errorNo, final String strMsg) {
                LogEx.e("onFailure,Throwable:" + t + ",errorNo:" + errorNo + ",strMsg:" + strMsg);
                mMainThreadHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        if (callback == null) {
                            return;
                        }
                        callback.onFailure(Properties.MODULE_TYPE_APP, DataPool.TYPE_APP_RELATED_SIMILAR + relatedType, t, errorNo, strMsg);
                    }

                });
                super.onFailure(t, errorNo, strMsg);
            }
        });
    }
    
    /**
     * 随机的推荐应用
     * @param callback 回调
     */
    public void loadRandomApps(final ManagerCallback callback) {
        HttpManager.getInstance().post(Protocol.getInstance().getRandomAppsUrl(), new AjaxCallBack<String>() {

            @Override
            public void onSuccess(final String t) {
                LogEx.d(t);

                mMainThreadHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        String res = Protocol.getInstance().parseRandomApps(t);
                        if (callback == null) {
                            return;
                        }
                        if ("true".equals(res)) {
                            callback.onSuccess(Properties.MODULE_TYPE_APP, DataPool.TYPE_APP_RANDOM_APPS, 0, 0, true);
                        } else {
                            callback.onFailure(Properties.MODULE_TYPE_APP, DataPool.TYPE_APP_RANDOM_APPS, null, -1, res);
                        }
                    }

                });
                super.onSuccess(t);
            }

            @Override
            public void onFailure(final Throwable t, final int errorNo, final String strMsg) {
                LogEx.e("onFailure,Throwable:" + t + ",errorNo:" + errorNo + ",strMsg:" + strMsg);

                mMainThreadHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        if (callback == null) {
                            return;
                        }
                        callback.onFailure(Properties.MODULE_TYPE_APP, DataPool.TYPE_APP_RANDOM_APPS, t, errorNo, strMsg);
                    }

                });
                super.onFailure(t, errorNo, strMsg);
            }
            
        });
    }

    /**
     * 需要升级的app列表
     */
    public synchronized void loadNeedUpgradeApps() {
        PackageManager pm = AppStoreWrapperImpl.getInstance().getAppContext().getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        JSONArray array = new JSONArray();
        for (PackageInfo p : packages) {
//            if ((p.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                array.put(p.packageName);
//            }
        }
        HttpManager.getInstance().post(Protocol.getInstance().getAppUpgradeUrl(array), new AjaxCallBack<String>() {

            @Override
            public void onSuccess(final String t) {
                LogEx.d(t);
                mMainThreadHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        String res = Protocol.getInstance().parseAppUpgrade(t);
                        if ("true".equals(res)) {
                            final ArrayList<AppInfo> appInfos = DataPool.getInstance().getAppInfos(DataPool.TYPE_APP_NEED_UPGRADE);
                            ArrayList<AppUpgradeCountListener> listeners = AppStoreWrapperImpl.getInstance().mAppUpgradeCountListeners;
                            if (appInfos != null) {
                                if (listeners != null) {
                                    for (int i = 0; i < listeners.size(); i++) {
                                        LogEx.d("need upgrade app count:" + appInfos.size());
                                        listeners.get(i).appUpgradeCount(appInfos.size());
                                    }
                                }
                                
                                if (SettingsManager.getInstance().getSettingValueAutoDownLoadUpdateInWifi() && Utils.isWifiConnected()) {
                                    for (int i = 0; i < appInfos.size(); i++) {
                                        final AppInfo info = appInfos.get(i);
                                        mMainThreadHandler.postDelayed(new Runnable() {

                                            @Override
                                            public void run() {
                                                startDownloadApp(info);
                                            }
                                            
                                        }, 1000*i);
                                    }
                                }
                            }
                        }
                    }
                    
                });
                super.onSuccess(t);
            }

            @Override
            public void onFailure(final Throwable t, final int errorNo, final String strMsg) {
                LogEx.e("onFailure,Throwable:" + t + ",errorNo:" + errorNo + ",strMsg:" + strMsg);
            }
            
        });
    }
    
    /**
     * 加载所有已安装应用
     * @return 应用类别
     */
    public void loadInstalledAppsThread() {
        new Thread("loadInstalledApps") {
            @Override
            public void run() {
                PackageManager pm = AppStoreWrapperImpl.getInstance().getAppContext().getPackageManager();
                List<PackageInfo> packages = pm.getInstalledPackages(0);
                for (PackageInfo p : packages) {
                    AppInfo info = new AppInfo();
                    info.id = info.pkg = p.packageName;
                    info.name = pm.getApplicationLabel(p.applicationInfo).toString();
                    info.app_icon = pm.getApplicationIcon(p.applicationInfo);
                    info.vercode = p.versionCode;
                    info.vername = p.versionName;
                    info.flag = AppInfo.FLAG_INSTALLED;
                    DataPool.getInstance().setInstallFlag(info);
                    DataPool.getInstance().addAppInfo(DataPool.TYPE_APP_INSTALLED, info);
                }
            }
        }.start();
    }
    
    /**
     * 通过包名找到已安装应用
     * @param packageName
     * @return
     */
    public static AppInfo parseInstalledApp(String packageName) {
        AppInfo info = DataPool.getInstance().getAppInfo(packageName);
        if(info == null){
            info = new AppInfo();
        }
        info.id = info.pkg = packageName;
        PackageManager pm = AppStoreWrapperImpl.getInstance().getAppContext().getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        for (PackageInfo p : packages) {
            if (packageName.equals(p.packageName)) {
                info.name = pm.getApplicationLabel(p.applicationInfo).toString();
                info.app_icon = pm.getApplicationIcon(p.applicationInfo);
                info.vercode = p.versionCode;
                info.vername = p.versionName;
                info.flag = AppInfo.FLAG_INSTALLED;
                break;
            }
        }
        return info;
    }
    
    /**
     * 加载所有已下载但未安装的文件
     * @return 应用列表
     */
    public void loadLocalAppFilesThread() {
        new Thread("loadLocalAppFiles") {

            @Override
            public void run() {
                File dir = new File(Properties.APP_PATH);
                if (!dir.exists() || !dir.isDirectory() || dir.listFiles() == null || dir.listFiles().length == 0) {
                    return;
                }
                for (File file : dir.listFiles()) {
                    String fileName = file.getName();
                    String prefix = fileName.substring(fileName.lastIndexOf(".") + 1);
                    if (!prefix.equals("apk")) {
                        continue;
                    }
                    AppInfo info = parseLocalAppFile(file);
                    if (DataPool.getInstance().getAppInfoInType(DataPool.TYPE_APP_INSTALLED, info.id) == null) {
                        DataPool.getInstance().setDownloadFlag(info);
                    }
                    DataPool.getInstance().addAppInfo(DataPool.TYPE_APP_DOWNLOADED, info);
                }
            }
            
        }.start();
    }
    
    /**
     * 解析apk文件的信息
     * @param file apk文件
     * @return app信息
     */
    public AppInfo parseLocalAppFile(File file) {
        if (!file.exists()) {
            return null;
        }
        AppInfo info = new AppInfo();
        DataPool.getInstance().putFileToAppInfo(info, file);
        info.size = Utils.LengthToString(file.length());
        info.date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(file.lastModified()));
        info.flag = AppInfo.FLAG_DOWNLOADED;
        
        PackageManager pm = AppStoreWrapperImpl.getInstance().getAppContext().getPackageManager();
        PackageInfo p = pm.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
        if (p != null) {
            try {
                info.id = info.pkg = p.packageName;
                parseLocalApkFileLabelAndIcon(info.file.getAbsolutePath(), info);
                info.vercode = p.versionCode;
                info.vername = p.versionName; 
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return info;
    }
    private void parseLocalApkFileLabelAndIcon(String apkPath, AppInfo appInfo) {
        String PATH_PackageParser = "android.content.pm.PackageParser";
        String PATH_AssetManager = "android.content.res.AssetManager";
        try {
            Class<?> pkgParserCls = Class.forName(PATH_PackageParser);
            Class[] typeArgs = new Class[1];
            typeArgs[0] = String.class;
            Constructor<?> pkgParserCt = pkgParserCls.getConstructor(typeArgs);
            Object[] valueArgs = new Object[1];
            valueArgs[0] = apkPath;
            Object pkgParser = pkgParserCt.newInstance(valueArgs);
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();
            typeArgs = new Class[4];
            typeArgs[0] = File.class;
            typeArgs[1] = String.class;
            typeArgs[2] = DisplayMetrics.class;
            typeArgs[3] = Integer.TYPE;
            Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", typeArgs);
            valueArgs = new Object[4];
            valueArgs[0] = new File(apkPath);
            valueArgs[1] = apkPath;
            valueArgs[2] = metrics;
            valueArgs[3] = 0;
            Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);
            Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");
            ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);
            Class<?> assetMagCls = Class.forName(PATH_AssetManager);
            Constructor<?> assetMagCt = assetMagCls.getConstructor((Class[]) null);
            Object assetMag = assetMagCt.newInstance((Object[]) null);
            typeArgs = new Class[1];
            typeArgs[0] = String.class;
            Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath", typeArgs);
            valueArgs = new Object[1];
            valueArgs[0] = apkPath;
            assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
            Resources res = AppStoreWrapperImpl.getInstance().getAppContext().getResources();
            typeArgs = new Class[3];
            typeArgs[0] = assetMag.getClass();
            typeArgs[1] = res.getDisplayMetrics().getClass();
            typeArgs[2] = res.getConfiguration().getClass();
            Constructor<Resources> resCt = Resources.class.getConstructor(typeArgs);
            valueArgs = new Object[3];
            valueArgs[0] = assetMag;
            valueArgs[1] = res.getDisplayMetrics();
            valueArgs[2] = res.getConfiguration();
            res = (Resources) resCt.newInstance(valueArgs);
            if (info.labelRes != 0) {
                appInfo.name = res.getText(info.labelRes).toString();
            }
            if (appInfo.name == null) {
                appInfo.name = (info.nonLocalizedLabel != null) ? info.nonLocalizedLabel.toString() : info.packageName;
            }
            if (info.icon != 0) {
                appInfo.app_icon = res.getDrawable(info.icon);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 开始下载应用 
     * @param info 应用信息
     */
    public synchronized void startDownloadApp(final AppInfo info) {
        LogEx.w("startDownloadApp");
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (info.id) {
                    if (info.bindId == null || info.bindId.length() == 0) {
                        LogEx.d(info.name + ",pkg=" + info.pkg + ",vercode=" + info.vercode + ",flag=" + info.flag);
                        String t = (String) HttpManager.getInstance().getSync(Protocol.getInstance().getDownloadUrl(info.name, info.pkg, info.vercode, info.flag == AppInfo.FLAG_NEED_UPGRADE ? true : false));
                        Protocol.getInstance().parseDownloadUrl(t, info);
                    }
                    if (info.name == null) {
                        info.name = "" + System.currentTimeMillis();
                    }
                    DownloadInfo downloadInfo = new DownloadInfo(DownloadInfo.TYPE_APK, info.name.replaceAll(" ", "") + "_" + info.vercode, info.file_url);
                    downloadInfo.uid = info.id + "_" + info.vercode;
                    mDownloadManager.startDownload(downloadInfo);
                    UserTrack.getInstance().startDownloadApp(info, info.from);
                }
            }
        }).start();
    }
    
    /**
     * 下载应用完成
     * @param info 应用信息
     * @param file 应用文件
     */
    public void endDownloadApp(final AppInfo info, final File file) {
        if (info == null) {
            startInstallApp(null, file);
            return;
        }
        DataPool.getInstance().putFileToAppInfo(info, file);
        DataPool.getInstance().setDownloadFlag(info);
        DataPool.getInstance().addAppInfo(DataPool.TYPE_APP_DOWNLOADED, info);
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                startInstallApp(info, file);
            }
        });

        try {
            AppStoreApi.mDownloadStateListener.get(info.id).onDownloadStateChanged(info.id, AppInfo.FLAG_DOWNLOADED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        UserTrack.getInstance().downloadAppSuccess(info);
    }
    
    /**
     * 安装应用
     * @param info 应用信息
     * @param file 应用文件
     */
    public void startInstallApp(AppInfo info, final File file) {
        synchronized (AppManager.this) {
            if (file == null || !file.exists()) {
                DataPool.getInstance().restoreToOnlineFlag(info);
                mDownloadManager.mDownloadDBProvider.deleteDownloadInfo(info.id + "_" + info.vercode);
                return;
            }

            if (info != null) {
                info.flag = AppInfo.FLAG_INSTALLING;
                for(int j = 0; j < mInstallStatusListeners.size(); j++){
                    mInstallStatusListeners.get(j).onStartInstallApp(info.pkg);
                }
                try {
                    AppStoreApi.mDownloadStateListener.get(info.id).onDownloadStateChanged(info.id, AppInfo.FLAG_INSTALLING);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                UserTrack.getInstance().startInstallApp(info);
            }

            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    LogEx.d("startInstallApp,file=" + file.getAbsolutePath());
                    Utils.installApk(AppStoreWrapperImpl.getInstance().getAppContext(), file.getAbsolutePath());
                }
            });
        }
    }
    
    /**
     * 安装应用完成
     * @param packageName 包名
     */
    public void endInstallApp(String packageName) {
        synchronized (AppManager.this) {
            AppInfo info = parseInstalledApp(packageName);
            AppInfo appInfo = DataPool.getInstance().getAppInfo(info.id);
            if (appInfo != null && appInfo.name != null && appInfo.name.length() != 0) {
                info = appInfo;
            }
            DataPool.getInstance().addAppInfo(DataPool.TYPE_APP_INSTALLED, info);
            DataPool.getInstance().removeAppInfo(DataPool.TYPE_APP_NEED_UPGRADE, info);
            DataPool.getInstance().setInstallFlag(info);
            
            if (SettingsManager.getInstance().getSettingValueDeleteInstalledApk()) {
                if (getAppLocalFlag(info) == AppInfo.FLAG_INSTALLED) {
                    deleteAppFile(info, info.file);
                }
            }
            
            for (int j = 0; j < mInstallStatusListeners.size(); j++) {
                mInstallStatusListeners.get(j).onEndInstallApp(packageName);
            }
            try {
                AppStoreApi.mDownloadStateListener.get(info.id).onDownloadStateChanged(info.id, AppInfo.FLAG_INSTALLED);
            } catch (Exception e) {
                e.printStackTrace();
            }
            UserTrack.getInstance().installAppSuccess(info);
        }
    }

    public void installAppFail(String packageName) {
        synchronized (AppManager.this) {
            AppInfo info = parseInstalledApp(packageName);
            AppInfo appInfo = DataPool.getInstance().getAppInfo(info.id);
            if (appInfo != null && appInfo.name != null && appInfo.name.length() != 0) {
                info = appInfo;
            }
            DataPool.getInstance().setDownloadFlagForce(info);
            for (int j = 0; j < mInstallStatusListeners.size(); j++) {
                mInstallStatusListeners.get(j).onInstallAppFail(packageName);
            }
        }
    }
    
    /**
     * 删除安装文件，本方法内做判断：应用已安装，则只删除文件；应用未安装，则删除文件并恢复应用flag为在线
     * @param info 应用信息
     * @param file 应用文件
     */
    public void deleteAppFile(AppInfo info, File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (info.flag == AppInfo.FLAG_INSTALLED || info.flag == AppInfo.FLAG_NEED_UPGRADE) {
            file.delete();
        } else {
            file.delete();  
            DataPool.getInstance().restoreToOnlineFlag(info);
        }
        DataPool.getInstance().removeAppInfo(DataPool.TYPE_APP_DOWNLOADED, info);
    }
    
    /**
     * 卸载应用
     * @param info 应用信息
     */
    public void startUninstallApp(AppInfo info) {
        synchronized (AppManager.this) {
            for(int i = 0; i< mUninstallStatusListeners.size(); i++){ 
                mUninstallStatusListeners.get(i).onStartUninstallApp(info.pkg);
            }
        }
    }
    
    /**
     * 卸载应用完成
     * @param packageName 包名
     */
    public void endUninstallApp(String packageName) {
        synchronized (AppManager.this) {
            AppInfo info = parseInstalledApp(packageName);
            if (info.file != null && info.file.exists()) {                
                DataPool.getInstance().addAppInfo(DataPool.TYPE_APP_DOWNLOADED, info);
                DataPool.getInstance().setDownloadFlag(info);
            } else {
                DataPool.getInstance().addAppInfo(DataPool.TYPE_APP_OTHER, info);
                DataPool.getInstance().restoreToOnlineFlag(info);
            }
            DataPool.getInstance().removeAppInfo(DataPool.TYPE_APP_INSTALLED, info);

            mDownloadManager.mDownloadDBProvider.deleteDownloadInfo(info.id + "_" + info.vercode);
            
            for (int i = 0; i < mUninstallStatusListeners.size(); i++) {
                 mUninstallStatusListeners.get(i).onEndUninstallApp(packageName);
            }
        }
    }

    /**
     * 打开应用
     * @param appInfo 应用信息
     * @return 是否成功
     */
    public boolean startOpenApp(AppInfo appInfo) {
        try {
            try {
                PackageManager pm = AppStoreWrapperImpl.getInstance().getAppContext().getPackageManager();
                pm.setApplicationEnabledSetting(appInfo.pkg, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);
            } catch (Exception e) {
            }

            Intent intent = AppStoreWrapperImpl.getInstance().getAppContext().getPackageManager().getLaunchIntentForPackage(appInfo.pkg);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            AppStoreWrapperImpl.getInstance().getAppContext().startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取包名对应的应用的状态
     * @return 状态
     */
    public int getAppLocalFlag(AppInfo appInfo) {
        PackageManager pm = AppStoreWrapperImpl.getInstance().getAppContext().getPackageManager();
        try {
            PackageInfo p = pm.getPackageInfo(appInfo.pkg, 0);
            if (appInfo.vercode > p.versionCode /*|| (appInfo.islocal && appInfo.vercode >= p.versionCode)*/) {
                return AppInfo.FLAG_NEED_UPGRADE;
            }
            return AppInfo.FLAG_INSTALLED;
        } catch (Exception e) {
        }

        ArrayList<AppInfo> localAppInfos = DataPool.getInstance().getAppInfos(DataPool.TYPE_APP_DOWNLOADED);
        if (localAppInfos != null) {
            for (AppInfo info : localAppInfos) {
                if (info.id.equals(appInfo.pkg)) {
                    if (info.file != null) {
                        DataPool.getInstance().putFileToAppInfo(appInfo, info.file);
                    }
                    if (info.flag == AppInfo.FLAG_INSTALLING) {
                        return AppInfo.FLAG_INSTALLING;
                    }
                    return AppInfo.FLAG_DOWNLOADED;
                }
            }
        }
        
//        File dir = new File(Properties.APP_PATH);
//        if (dir.exists() && dir.isDirectory()) {
//            for (File file : dir.listFiles()) {
//                String fileName = file.getName();
//                String prefix = fileName.substring(fileName.lastIndexOf(".") + 1);
//                if (!prefix.equals("apk")) {
//                    continue;
//                }
//                PackageInfo p = pm.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
//                if (p != null && p.packageName.equals(packageName)) {
//                    return AppInfo.FLAG_DOWNLOADED;
//                }
//            }
//        }
        return AppInfo.FLAG_ONLINE;
    }
    
    public void registerInstallStatusListener(InstallStatusListener l) {
        synchronized (AppManager.this) {
            if(!mInstallStatusListeners.contains(l)){
                mInstallStatusListeners.add(l);
            }
        }
    }
    
    public void unregisterInstallStatusListener(InstallStatusListener l) {
        synchronized (AppManager.this) {
            mInstallStatusListeners.remove(l);
        }
    }
    
    public interface InstallStatusListener {
        void onStartInstallApp(String packageName);
        void onEndInstallApp(String packageName);
        void onInstallAppFail(String packageName);
    }
    
    public void registerUninstallStatusListener(UninstallStatusListener l) {
        synchronized (AppManager.this) {
            if(!mUninstallStatusListeners.contains(l)){
                mUninstallStatusListeners.add(l);
            } 
        }
    }
    
    public void unregisterUninstallStatusListener(UninstallStatusListener l) {
        synchronized (AppManager.this) {
            mUninstallStatusListeners.remove(l);
        }
    }
    
    public interface UninstallStatusListener {
        void onStartUninstallApp(String packageName);
        void onEndUninstallApp(String packageName);
    }

}
