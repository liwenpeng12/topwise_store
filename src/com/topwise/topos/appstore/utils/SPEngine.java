package com.topwise.topos.appstore.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.topwise.topos.appstore.AppStoreWrapperImpl;


public class SPEngine {
    private SharedPreferences sharedPreferences;
    private Editor editor;
    private static SPEngine spEngine = new SPEngine();

    public static SPEngine getSPEngine() {
        return spEngine;
    }

    private SPEngine() {
        sharedPreferences = AppStoreWrapperImpl.getInstance().getAppContext().getSharedPreferences("hard_disk_cache", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        loadInfo();
    }

    /**
     * 加载需要进入内存的信息
     */
    private void loadInfo() {

    }

    final String KEY_PAGEINFO = "KEY_PAGEINFO";


    public String getPageInfo(String flag) {
        return sharedPreferences.getString(KEY_PAGEINFO + flag, null);
    }

    public void setPageInfo(String constellaion, String flag) {
        editor.putString(KEY_PAGEINFO + flag, constellaion).apply();
    }


}



