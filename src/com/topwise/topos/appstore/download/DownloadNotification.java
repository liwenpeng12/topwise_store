package com.topwise.topos.appstore.download;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;

import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.utils.Properties;
import com.topwise.topos.appstore.view.activity.DownloadActivity;

public class DownloadNotification {

    public static final int REFRESH_NOTIFICATION_INTERVAL_TIME = 1500;

    private static DownloadNotification mThis = null;

    private Context mContext = null;

    private SparseArray<NotificationSender> mNotificationSenders = new SparseArray<>();

    private PendingIntent mPendingIntent;

    public static DownloadNotification getInstance() {
        if (mThis == null) {
            synchronized (DownloadNotification.class) {
                if (mThis == null) {
                    mThis = new DownloadNotification();
                }
            }
        }
        return mThis;
    }

    public DownloadNotification() {
        mContext = AppStoreWrapperImpl.getInstance().getAppContext();
        Intent intent = new Intent(mContext, DownloadActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("mode", 0);
        mPendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void destroy() {
        for (int i = 0; i < mNotificationSenders.size(); i++) {
            if (mNotificationSenders.get(mNotificationSenders.keyAt(i)) != null) {
                mNotificationSenders.get(mNotificationSenders.keyAt(i)).cancelNotification(mContext);
            }
        }
    }

    public synchronized void displayNotification(final DownloadInfo info) {
        if (info == null) {
            return;
        }
        NotificationSender sender = getNotificationSender(info._id);

        String appName = info.name;
        if (appName != null) {
            appName = appName.contains("_") ? appName.substring(0, appName.lastIndexOf("_")) : appName;
        }
        String notifyTitle = "下载文件:" + appName;

        String notifyContent = "";
        if (info.downloadStatus == DownloadInfo.STATUS_DOWNLOADING) {
            notifyContent = "正在下载";
        } else if (info.downloadStatus == DownloadInfo.STATUS_PAUSE) {
            notifyContent = "暂停下载";
        } else if (info.downloadStatus == DownloadInfo.STATUS_DOWNLOAD_SUCCESS) {
            notifyContent = "下载完成";
            if (sender != null) {
                sender.cancelNotification(mContext);
            }
        } else if (info.downloadStatus == DownloadInfo.STATUS_CANCEL) {
            notifyContent = "取消下载";
            if (sender != null) {
                sender.cancelNotification(mContext);
            }
        } else {
            notifyContent = "下载错误";
        }
        if (sender == null) {
            sender = new NotificationSender(info._id);
            addNotificationSender(info._id, sender);
        }
        sender.setSmallIcon(R.drawable.as_notification_statusbar_icon);
        sender.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.as_ic_launcher));
        if (Properties.CHANNEL_IVVI.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
            sender.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.as_ic_launcher_ivvi));
        } else if (Properties.CHANNEL_COOLMART.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
            sender.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.as_ic_launcher_coolmart));
        } else if (Properties.CHANNEL_SHARP.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
            sender.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.as_ic_launcher_sharp));
        } else if (Properties.CHANNEL_DUOCAI.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
            sender.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.as_ic_launcher_duocai));
        } else if (Properties.CHANNEL_17WO.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
            sender.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.as_ic_launcher_apphome));
        }
        sender.setTitle(notifyTitle);
        sender.setContent(notifyContent);
        sender.setProgress(info.progress);
        if (info.downloadStatus == DownloadInfo.STATUS_DOWNLOADING) {
            sender.setAutoCancel(false);
            sender.setOngoing(true);
        } else {
            sender.setAutoCancel(true);
            sender.setOngoing(false);
        }
        sender.sendNotification(mContext, mPendingIntent);
        if (info.downloadStatus == DownloadInfo.STATUS_CANCEL || info.downloadStatus == DownloadInfo.STATUS_DOWNLOAD_SUCCESS) {
            sender.cancelNotification(mContext, info._id);
        }
    }

    public void startProgressNotification(DownloadInfo info) {
        if (info == null) {
            return;
        }
        NotificationSender sender = getNotificationSender(info._id);
        if (sender == null) {
            sender = new NotificationSender((int) info._id);
            String appName = info.name;
            appName = appName.contains("_") ? appName.substring(0, appName.lastIndexOf("_")) : appName;
            String notifyTitle = "下载文件:" + appName;
            sender.setSmallIcon(R.drawable.as_notification_statusbar_icon);
            sender.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.as_ic_launcher));
            if (Properties.CHANNEL_IVVI.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
                sender.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.as_ic_launcher_ivvi));
            } else if (Properties.CHANNEL_COOLMART.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
                sender.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.as_ic_launcher_coolmart));
            } else if (Properties.CHANNEL_SHARP.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
                sender.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.as_ic_launcher_sharp));
            } else if (Properties.CHANNEL_DUOCAI.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
                sender.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.as_ic_launcher_duocai));
            } else if (Properties.CHANNEL_17WO.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
                sender.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.as_ic_launcher_apphome));
            }
            sender.setTitle(notifyTitle);
            sender.setContent("正在下载");
            addNotificationSender(info._id, sender);
        }
        sender.setAutoCancel(false);
        sender.setOngoing(true);
        sender.setProgress(info.progress);
        sender.sendNotification(mContext, mPendingIntent);
        Message msg = mHandler.obtainMessage(0);
        msg.obj = info;
        mHandler.sendMessageDelayed(msg, REFRESH_NOTIFICATION_INTERVAL_TIME);
    }

    private void refreshProgressNotification(DownloadInfo info) {
        NotificationSender sender = getNotificationSender(info._id);
        if (info.downloadStatus == DownloadInfo.STATUS_DOWNLOADING && sender.getId() == info._id) {
            sender.setAutoCancel(false);
            sender.setOngoing(true);
            sender.setProgress(info.progress);
            sender.setContent("正在下载, 下载速度: " + DownloadInfo.formatDownloadSpeed(info.downloadSpeed));
            sender.notifyNotification(mContext);
            Message msg = mHandler.obtainMessage(0);
            msg.obj = info;
            mHandler.sendMessageDelayed(msg, REFRESH_NOTIFICATION_INTERVAL_TIME);
        } else if (info.downloadStatus == DownloadInfo.STATUS_DOWNLOAD_SUCCESS && sender.getId() == info._id) {
            sender.setOngoing(false);
            sender.setAutoCancel(true);
            sender.setProgress(info.progress);
            sender.notifyNotification(mContext);
        }
    }

    Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            refreshProgressNotification((DownloadInfo) msg.obj);
        }
    };

    private void addNotificationSender(int id, NotificationSender sender) {
        synchronized (mNotificationSenders) {
            mNotificationSenders.put(id, sender);
        }
    }

    private NotificationSender getNotificationSender(int id) {
        synchronized (mNotificationSenders) {
            return mNotificationSenders.get(id);
        }
    }

    public void clearNotificationSenders() {
        if (mNotificationSenders != null) {
            mNotificationSenders.clear();
            ;
        }
    }
}
