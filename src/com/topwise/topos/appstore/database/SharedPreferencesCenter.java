
package com.topwise.topos.appstore.database;

import android.content.Context;
import android.content.SharedPreferences;

import com.topwise.topos.appstore.AppStoreWrapperImpl;

import java.util.HashMap;

public class SharedPreferencesCenter {
    
    private static final String FILE_NAME = "TSConfigs";
    
    private static HashMap<String, SharedPreferencesCenter> mSharedPreferencesMap = new HashMap<String, SharedPreferencesCenter>();
    private String mIndex = "";

    //有参
    public synchronized static SharedPreferencesCenter getInstance(String index) {
        if (mSharedPreferencesMap.get(index) == null) {
            SharedPreferencesCenter instance = new SharedPreferencesCenter();
            instance.mIndex = index;
            mSharedPreferencesMap.put(index, instance);
        }
        return mSharedPreferencesMap.get(index);
    }

    //无参
    public synchronized static SharedPreferencesCenter getInstance() {
        return getInstance("");
    }

    public SharedPreferences getSharedPreferences() {
        return AppStoreWrapperImpl.getInstance().getAppContext().getSharedPreferences(FILE_NAME + mIndex, Context.MODE_PRIVATE);
    }

}
