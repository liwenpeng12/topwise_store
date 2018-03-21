package com.topwise.topos.appstore.download;

import com.topwise.topos.appstore.AppStoreWrapperImpl;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

public class NotificationSender {

    private int mId;
    private Builder mBuilder;
    private Notification mNotification;

    public NotificationSender(int id) {
        mId = id;
        mBuilder = new NotificationCompat.Builder(AppStoreWrapperImpl.getInstance().getAppContext());
    }

    public void setTitle(String title) {
        mBuilder.setContentTitle(title);
    }

    public void setContent(String content) {
        mBuilder.setContentText(content);
    }

    public void setIcon(int iconId, Bitmap largeIcon) {
        mBuilder.setSmallIcon(iconId);
        mBuilder.setLargeIcon(largeIcon);
    }

    public void setSmallIcon(int iconId) {
        mBuilder.setSmallIcon(iconId);
    }

    public void setLargeIcon(Bitmap largeIcon) {
        mBuilder.setLargeIcon(largeIcon);
    }

    public void setProgress(int progress) {
// TODO,临时关掉通知上的进度条       mBuilder.setProgress(100, progress, false);
    }

    public void setAutoCancel(boolean cancel) {
        mBuilder.setAutoCancel(cancel);
    }

    public void setOngoing(boolean ongoing) {
        mBuilder.setOngoing(ongoing);
    }

    public void setDeleteIntent(Context context, Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setDeleteIntent(pendingIntent);
    }

    public int getId() {
        return mId;
    }

    public void sendNotification(Context context, PendingIntent pendingIntent) {
        mBuilder.setContentIntent(pendingIntent);
        mNotification = mBuilder.getNotification();
        notifyNotification(context, mNotification);
    }

    public void sendLargeNotification(Context context, PendingIntent pendingIntent, Bitmap bitmap) {
        NotificationCompat.BigPictureStyle pictureStyle = new NotificationCompat.BigPictureStyle();
        mBuilder.setStyle(pictureStyle);
        pictureStyle.bigPicture(bitmap);
        sendNotification(context, pendingIntent);
    }

    public void notifyNotification(Context context) {
        notifyNotification(context, mId, mBuilder.getNotification());
    }
    public void notifyNotification(Context context, Notification notification) {
        notifyNotification(context, mId, notification);
    }
    public void notifyNotification(Context context, int id, Notification notification) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(id, notification);
    }

    public void cancelNotification(Context context) {
        cancelNotification(context, mId);
    }
    public void cancelNotification(Context context, int id) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(id);
    }

}
