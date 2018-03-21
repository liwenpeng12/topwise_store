package com.topwise.topos.appstore.manager;

import java.util.ArrayList;
import java.util.List;

import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.conn.behavior.BehaviorEx;
import com.topwise.topos.appstore.conn.behavior.BehaviorLogManager;
import com.topwise.topos.appstore.conn.behavior.UserTrack;
import com.topwise.topos.appstore.data.AppInfo;
import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.data.NotificationInfo;
import com.topwise.topos.appstore.database.SharedPreferencesCenter;
import com.topwise.topos.appstore.manager.NetworkManager.NetworkListener;
import com.topwise.topos.appstore.service.AppStoreService;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.utils.Properties;
import com.topwise.topos.appstore.utils.Utils;
import com.topwise.topos.appstore.view.activity.AppDetailActivity;
import com.topwise.topos.appstore.view.activity.GroupActivity;
import com.topwise.topos.appstore.view.activity.H5Activity;
import com.topwise.topos.appstore.view.activity.SearchActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;

public class ReceiverCenter extends BroadcastReceiver {

    private Handler mHandler = null;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        LogEx.i("action=" + intent.getAction());
        if (AppStoreWrapperImpl.getInstance().getAppContext() == null) {
            return;
        }
        if (Properties.CHANNEL_IVVI.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
            if (!SharedPreferencesCenter.getInstance().getSharedPreferences().getBoolean("prompt", false)) {
                SharedPreferencesCenter.getInstance().getSharedPreferences().edit().putBoolean("prompt", false).commit();
                return;
            }
        }
        context.startService(new Intent(context, AppStoreService.class));

//        try {
//            Intent s = new Intent();
//            s.setClassName("com.yulong.android.coolshow", "com.ibimuyu.framework.lockscreen.common.LockService");
//            context.startService(s);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            Intent s = new Intent();
//            s.setClassName("com.zookingsoft.themestore", "com.ibimuyu.framework.lockscreen.common.LockService");
//            context.startService(s);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }

        LogEx.i("action=" + intent.getAction());
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo moInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            ArrayList<NetworkListener> networkListeners = NetworkManager.getInstance().mListeners;
            if (!moInfo.isConnected() && !wifiInfo.isConnected()) {
                if (networkListeners != null && networkListeners.size() != 0) {
                    for (NetworkListener l : networkListeners) {
                        l.onNetworkDisconnected();
                    }
                }
            } else {
                AppManager.getInstance().loadNeedUpgradeApps();
                if (networkListeners != null && networkListeners.size() != 0) {
                    for (NetworkListener l : networkListeners) {
                        l.onNetworkConnected();
                    }
                }
            }
        } else if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                final ArrayList<AppInfo> appInfos = DataPool.getInstance().getAppInfos(DataPool.TYPE_APP_NEED_UPGRADE);
                if (appInfos != null) {
                    if (SettingsManager.getInstance().getSettingValueAutoDownLoadUpdateInWifi() && Utils.isWifiConnected()) {
                        for (int i = 0; i < appInfos.size(); i++) {
                            final AppInfo appInfo = appInfos.get(i);
                            new Handler().postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    AppManager.getInstance().startDownloadApp(appInfo);
                                }
                                
                            }, 1000*i);
                        }
                        
                        ArrayList<NetworkListener> networkListeners = NetworkManager.getInstance().mListeners;
                        if (networkListeners != null && networkListeners.size() != 0) {
                            for (NetworkListener l : networkListeners) {
                                l.onNetworkConnected();
                            }
                        }
                        
                        AppManager.getInstance().loadNeedUpgradeApps();
                    }
                }
            }
        } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    String packageName = intent.getData().getSchemeSpecificPart();
                    if (!packageName.equals(context.getApplicationInfo().packageName)) {
                        AppManager.getInstance().endInstallApp(packageName);
                    }
                }
            });
        } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    String packageName = intent.getData().getSchemeSpecificPart();
                    if (!packageName.equals(context.getApplicationInfo().packageName)) {
                        AppManager.getInstance().endUninstallApp(packageName);
                    }
                }
            });
        } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    String packageName = intent.getData().getSchemeSpecificPart();
                    if (!packageName.equals(context.getApplicationInfo().packageName)) {
                        AppManager.getInstance().endInstallApp(packageName);
                    }
                }
            });
        } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_FIRST_LAUNCH)) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    String packageName = intent.getData().getSchemeSpecificPart();
                    if (!packageName.equals(context.getApplicationInfo().packageName)) {
                        UserTrack.getInstance().reportFirstLaunchApp(packageName);
                    }
                }
            });
        } else if (intent.getAction().equals(Properties.INSTALL_SILENT_FAIL)) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    String packageName = intent.getStringExtra("packageName");
                    if (packageName != null && !packageName.equals(context.getApplicationInfo().packageName)) {
                        AppManager.getInstance().installAppFail(packageName);
                    }
                }
            });
        } else if (intent.getAction().equals(Properties.CLICK_NOTIFICATION)) {
            NotificationInfo info = (NotificationInfo) intent.getBundleExtra("bundle").get("info");
            if (info == null) {
                return;
            }
            BehaviorLogManager.getInstance().addBehaviorEx(new BehaviorEx(BehaviorEx.ENTER_NOTIFICATION, info.title));
            
            if (info.intenturi != null && !"".equals(info.intenturi)) {
                try {
                    context.startActivity(Intent.parseUri(info.intenturi, 0));
                    return;
                } catch (Exception e) {
                }
            }
            
            Intent pendingIntent = new Intent();
            if (info.event == 0) {
                pendingIntent = context.getPackageManager().getLaunchIntentForPackage(Properties.PACKAGENAME_17WO);
            } else if (info.event == 1) {
                if (info.h5type == 0) {
                    pendingIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(info.event_data.url));
                } else {
                    pendingIntent = new Intent(context, H5Activity.class);
                    pendingIntent.putExtra("url", info.event_data.url);
                    pendingIntent.putExtra("title", info.title);
                }
            } else if (info.event == 2) {
                List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
                boolean hasApk = false;
                for (PackageInfo p : packages) {
                    if (p.packageName.contains(info.event_data.packagename)) {
                        if (p.versionCode >= info.event_data.versionCode) {
                            hasApk = true;
                        }
                        break;
                    }
                }
                if (hasApk) {
                    if (info.event_data.intent != null && !"".equals(info.event_data.intent)) {
                        pendingIntent = new Intent(info.event_data.intent);
                    } else {
                        pendingIntent = context.getPackageManager().getLaunchIntentForPackage(info.event_data.packagename);
                    }
                } else {
                    pendingIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(info.event_data.apk_url));
                }
            } else if (info.event == 3) {
                if ("app".equals(info.event_data.type)) {
                    pendingIntent = new Intent(context, AppDetailActivity.class);
                    pendingIntent.putExtra("app_id", info.event_data.type_id);
                } else if ("page".equals(info.event_data.type)) {
                    pendingIntent = new Intent(context, GroupActivity.class);
                    pendingIntent.putExtra("title", info.title);
                    pendingIntent.putExtra("type", "page");

                    try {
                        int[] ids = new int[1];
                        ids[0] = Integer.valueOf(info.event_data.type_id);
                        pendingIntent.putExtra("id", ids);
                    } catch (Exception e) {
                    }
                } else if ("label".equals(info.event_data.type)) {
                    pendingIntent = new Intent(context, GroupActivity.class);
                    pendingIntent.putExtra("title", info.title);
                    pendingIntent.putExtra("type", "label");
                    
                    try {
                        int[] ids = new int[1];
                        ids[0] = Integer.valueOf(info.event_data.type_id);
                        pendingIntent.putExtra("id", ids);
                    } catch (Exception e) {
                    }
                } else if ("rank".equals(info.event_data.type)) {
                    pendingIntent = new Intent(context, GroupActivity.class);
                    pendingIntent.putExtra("title", info.title);
                    pendingIntent.putExtra("type", "rank");
                    
                    try {
                        int[] ids = new int[1];
                        ids[0] = Integer.valueOf(info.event_data.type_id);
                        pendingIntent.putExtra("id", ids);
                    } catch (Exception e) {
                    }
                } else if ("type".equals(info.event_data.type)) {
                    pendingIntent = new Intent(context, GroupActivity.class);
                    pendingIntent.putExtra("title", info.title);
                    pendingIntent.putExtra("type", "type");
                    
                    try {
                        int[] ids = new int[1];
                        ids[0] = Integer.valueOf(info.event_data.type_id);
                        pendingIntent.putExtra("id", ids);
                    } catch (Exception e) {
                    }
                } else if ("search".equals(info.event_data.type)) {
                    pendingIntent = new Intent(context, SearchActivity.class);
                    if (info.event_data.type_id != null && info.event_data.type_id.length() > 0) {
                        intent.putExtra("keyword", info.event_data.type_id);
                    }
                }
            }
            pendingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(pendingIntent);
        }
    }

}
