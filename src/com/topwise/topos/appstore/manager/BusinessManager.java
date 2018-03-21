package com.topwise.topos.appstore.manager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.conn.behavior.BehaviorEx;
import com.topwise.topos.appstore.conn.behavior.BehaviorLogManager;
import com.topwise.topos.appstore.conn.http.AjaxCallBack;
import com.topwise.topos.appstore.conn.protocol.Protocol;
import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.data.DataPool.NotificationBitmap;
import com.topwise.topos.appstore.data.NotificationInfo;
import com.topwise.topos.appstore.data.Welcome;
import com.topwise.topos.appstore.database.SharedPreferencesCenter;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.utils.Properties;
import com.topwise.topos.appstore.R;

public class BusinessManager extends BaseManager {
    
    private static BusinessManager mThis = null;
    
    private int mNotificationPollingCount = 0;
    
    private int mFailCount = 0;
    
    public static BusinessManager getInstance() {
        if (mThis == null) {
            synchronized (BusinessManager.class) {
                if (mThis == null) {
                    mThis = new BusinessManager();
                }
            }
        }
        return mThis;
    }
    
    /**
     * 详情页随机显示的banner，这里给出banner list，界面来随机选择，建议进入详情次数mod banner个数
     * @param callback 回调
     */
    public void loadBanners(final ManagerCallback callback) {
        HttpManager.getInstance().post(Protocol.getInstance().getBannersUrl(), new AjaxCallBack<String>() {

            @Override
            public void onSuccess(final String t) {
                LogEx.d(t);
                mMainThreadHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        String res = Protocol.getInstance().parseRandomBanners(t);
                        if (callback == null) {
                            return;
                        }
                        if ("true".equals(res)) {
                            callback.onSuccess(Properties.MODULE_TYPE_BANNER_LARGE, DataPool.TYPE_BANNER_RANDOM, 0, 0, true);
                        } else {
                            callback.onFailure(Properties.MODULE_TYPE_BANNER_LARGE, DataPool.TYPE_BANNER_RANDOM, null, -1, res);
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
                        callback.onFailure(Properties.MODULE_TYPE_BANNER_LARGE, DataPool.TYPE_BANNER_RANDOM, t, errorNo, strMsg);
                    }
                    
                });
                super.onFailure(t, errorNo, strMsg);
            }
            
        });
    }
    
    /**
     * 广告（包括欢迎界面和通知）
     * @param callback 回调
     */
    public ArrayList<Welcome> loadAd(final ManagerCallback callback) {
        HttpManager.getInstance().post(Protocol.getInstance().getAdUrl(), new AjaxCallBack<String>(){

            @Override
            public void onSuccess(final String t) {
                LogEx.d(t);
                SharedPreferences sp = SharedPreferencesCenter.getInstance().getSharedPreferences();
                sp.edit().putString("welcome", t).commit();
                mMainThreadHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        String res = Protocol.getInstance().parseAd(t, true, true);
                        if ("true".equals(res)) {
                            BusinessManager.getInstance().pollingNotifications();
                            if (callback != null) {
                                callback.onSuccess(Properties.MODULE_TYPE_AD, DataPool.TYPE_WELCOME, 0, 0, true);
                            }
                        } else {
                            if (callback != null) {
                                callback.onFailure(Properties.MODULE_TYPE_AD, DataPool.TYPE_WELCOME, null, -1, res);
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
                        if (callback != null) {
                            callback.onFailure(Properties.MODULE_TYPE_AD, DataPool.TYPE_WELCOME, t, errorNo, strMsg);
                        }
                        if (mFailCount < 10) {
                            mFailCount++;
                            loadAd(null);
                        } else {
                            mFailCount = 0;
                        }
                    }
                    
                });
                super.onFailure(t, errorNo, strMsg);
            }
            
        });
        
        SharedPreferences sp = SharedPreferencesCenter.getInstance().getSharedPreferences();
        String welcomeString = sp.getString("welcome", "");
        if (welcomeString == null || "".equals(welcomeString)) {
            return null;
        }
        String res = Protocol.getInstance().parseAd(welcomeString, true, false);
        if ("true".equals(res)) {
            return DataPool.getInstance().getWelcomes();
        } else {
            return null;
        }
    }
    
    /**
     * 轮询显示通知
     */
    public void pollingNotifications() {
        final Runnable r = new Runnable() {

            @Override
            public void run() {
                ArrayList<NotificationInfo> notifications = DataPool.getInstance().getNotifications();
                LogEx.d("pollingNotifications size=" + notifications.size());
                if (notifications.size() > 0) {
                    showNotifications(notifications);
                } else {
                    if (mNotificationPollingCount < 3) {
                        mNotificationPollingCount++;
                        pollingNotifications();
                    } else {
                        mNotificationPollingCount = 0;
                    }
                }
            }
            
        };
        mMainThreadHandler.postDelayed(r, 3*1000);
    }
    
	private void showNotifications(ArrayList<NotificationInfo> notifications) {
        Context context = AppStoreWrapperImpl.getInstance().getAppContext();
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        for (int i = notifications.size()-1; i >= 0; i--) {
            NotificationInfo info = notifications.get(i);
            NotificationBitmap nb = DataPool.getInstance().getNotificationBitmap(info);
            if (info.iconurl != null && !"".equals(info.iconurl) && (nb == null || nb.icon == null)) {
                pollingNotifications();
                return;
            }
            try {
                if (parseTimeString(getNowTimeString()) < parseTimeString(info.show_time_start) 
                        || parseTimeString(getNowTimeString()) > parseTimeString(info.show_time_end)) {
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(Properties.CLICK_NOTIFICATION);
            Bundle b = new Bundle();
            b.putSerializable("info", info);
            intent.putExtra("bundle", b);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            
            Notification.Builder builder = new Notification.Builder(context);
            builder.setContentIntent(pendingIntent);
            builder.setWhen(System.currentTimeMillis() + info.delaytime); // 设置通知来到的时间
            builder.setContentTitle(info.title); // 设置通知的标题
            builder.setContentText(info.desc); // 设置通知的内容
            builder.setTicker(info.desc); // 状态栏上显示
            builder.setAutoCancel(true); // 点击后消失
            builder.setDefaults(Notification.DEFAULT_ALL);
            
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.as_notification_custom_normal);
            if (nb != null && nb.icon != null) {
                builder.setSmallIcon(android.R.drawable.sym_def_app_icon);
                builder.setLargeIcon(nb.icon);
                remoteViews.setTextViewText(R.id.title, info.title);
                remoteViews.setTextViewText(R.id.desc, info.desc);
                remoteViews.setTextViewText(R.id.time, getNowTimeStringHM());
                remoteViews.setImageViewBitmap(R.id.icon, nb.icon);
                if (nb.img != null) {
                    remoteViews.setImageViewBitmap(R.id.bigimg, nb.img);
                }
                builder.setContent(remoteViews);
            } else {
                builder.setSmallIcon(R.drawable.as_notification_statusbar_icon);
                builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.as_ic_launcher));
                if (Properties.CHANNEL_IVVI.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
                    builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.as_ic_launcher_ivvi));
                } else if (Properties.CHANNEL_COOLMART.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
                    builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.as_ic_launcher_coolmart));
                } else if (Properties.CHANNEL_SHARP.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
                    builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.as_ic_launcher_sharp));
                } else if (Properties.CHANNEL_DUOCAI.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
                    builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.as_ic_launcher_duocai));
                } else if (Properties.CHANNEL_17WO.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
                    builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.as_ic_launcher_apphome));
                }
            }
            if (nb != null && nb.img != null) {
                Notification.BigPictureStyle style = new Notification.BigPictureStyle();
                style.setBigContentTitle(info.title);
                style.setSummaryText(info.desc);
                style.bigPicture(nb.img);
                builder.setStyle(style);
            }
 
            Notification notification = builder.build();
            if (nb != null && nb.icon != null) {
                notification.bigContentView = remoteViews;
            }
            nm.notify(i, notification);
            DataPool.getInstance().removeNotification(info);
            BehaviorLogManager.getInstance().addBehaviorEx(new BehaviorEx(BehaviorEx.SHOW_NOTIFICATION, info.title));
        }
    }
	
    public static String getNowTimeString() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        return "" + hour + ":" + minute + ":" + second;
    }
    
    public static String getNowTimeStringHM() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        return "" + (hour >= 10 ? hour : "0" + hour) + ":" + (minute >= 10 ? minute : "0" + minute);
    }
    
    public static long parseTimeString(String time) throws ParseException {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.US);
        c.setTime(format.parse(time));
        return c.getTimeInMillis();
    }
}
