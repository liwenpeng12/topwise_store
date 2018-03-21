package com.topwise.topos.appstore.api;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AppStoreApiService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        AppStoreApi api = new AppStoreApi();
        return api;
    }
}
