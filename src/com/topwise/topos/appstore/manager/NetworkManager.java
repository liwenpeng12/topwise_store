package com.topwise.topos.appstore.manager;

import java.util.ArrayList;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.topwise.topos.appstore.AppStoreWrapperImpl;

public class NetworkManager {
    
    private static NetworkManager mThis = null;
    
    public ArrayList<NetworkListener> mListeners = new ArrayList<NetworkListener>();
    
    public static NetworkManager getInstance() {
        if (mThis == null) {
            synchronized (NetworkManager.class) {
                if (mThis == null) {
                    mThis = new NetworkManager();
                }
            }
        }
        return mThis;
    }
    
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) AppStoreWrapperImpl.getInstance().getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = cm.getActiveNetworkInfo();
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo moInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isConn = false;
        if (activeInfo != null) {
            isConn |= activeInfo.isConnected();
        }
        if (wifiInfo != null) {
            isConn |= wifiInfo.isConnected();
        }
        if (moInfo != null) {
            isConn |= moInfo.isConnected();
        }
        return isConn;
    }
    
    public void registerNetworkListener(NetworkListener l) {
        if (!mListeners.contains(l)) {
            mListeners.add(l);
        }
    }
    
    public void unregisterNetworkListener(NetworkListener l) {
        if (mListeners.contains(l)) {
            mListeners.remove(l);
        }
    }

    public interface NetworkListener {
        public void onNetworkConnected();
        public void onNetworkDisconnected();
    }
}
