package com.topwise.topos.appstore.conn.behavior;

import java.util.HashMap;

import com.topwise.topos.appstore.database.SharedPreferencesCenter;
import com.topwise.topos.appstore.utils.Properties;

import android.content.Context;
import android.content.SharedPreferences;

public class SimpleStorageManager {
    
    private static final String FILE_NAME = "ibimuyu" + Properties.APP_TAG + "_info";
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private static HashMap<String, SimpleStorageManager> mSimpleStorageManagerMap = new HashMap<String, SimpleStorageManager>();
    private String mIndex = "";

    public synchronized static SimpleStorageManager getInstance(String index) {
        if (mSimpleStorageManagerMap.get(index) == null) {
            SimpleStorageManager instance = new SimpleStorageManager();
            instance.mIndex = index;
            mSimpleStorageManagerMap.put(index, instance);
        }
        return mSimpleStorageManagerMap.get(index);
    }

    public synchronized static SimpleStorageManager getInstance() {
        return getInstance("");
    }

    public void setApplicationContext(Context context) {
        if (this.mContext != null) {
            return;
        }
        this.mContext = context;
        mSharedPreferences = SharedPreferencesCenter.getInstance(FILE_NAME + mIndex)
                .getSharedPreferences();
    }

    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

}
