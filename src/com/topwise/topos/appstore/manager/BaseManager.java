package com.topwise.topos.appstore.manager;

import android.os.Handler;
import android.os.Looper;

public class BaseManager {
    
    protected Handler mMainThreadHandler = new Handler(Looper.getMainLooper());

}
