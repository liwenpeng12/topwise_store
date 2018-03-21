package com.topwise.topos.appstore.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.topwise.topos.appstore.download.DownloadNotification;

public class KillNotificationsService extends Service {

    private final IBinder mBinder = new KillBinder(this);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        DownloadNotification.getInstance().destroy();
    }

    public class KillBinder extends Binder {
        public final Service service;

        public KillBinder(Service service) {
            this.service = service;
        }
    }
}
