package com.topwise.topos.appstore.manager;

import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.database.SharedPreferencesCenter;
import com.topwise.topos.appstore.utils.FileUtil;
import com.topwise.topos.appstore.utils.Properties;

import android.content.SharedPreferences;

public class SettingsManager {
	/**
	 * 自动安装apk
	 */
	public static String AUTO_INSTALL = "auto_install";
	/**
	 * 删除安装后的apk
	 */
	public static String DELETE_INSTALLED_APK = "delete_installed_apk";
	/**
	 * wifi下自动下载更新apk
	 */
	public static String WIFI_DOWNLOAD_UPDATE = "wifi_downloaded_update";
	private static SettingsManager pthis;
	public static boolean mEnableAutoInstall = false;
	private static boolean mEnableDeleteInstalledApk = true ;
	private static boolean mEnableAutoDownLoadUpdateForWifi = true;
	private SettingsManager(){
	    initSettingValues();
	}
	
	public static SettingsManager getInstance() {
		if(pthis == null){
			pthis = new SettingsManager();
		}
		return pthis;
	}
	public void initSettingValues(){
		mEnableAutoInstall = SharedPreferencesCenter.getInstance().getSharedPreferences().getBoolean(AUTO_INSTALL, false);
		mEnableDeleteInstalledApk = SharedPreferencesCenter.getInstance().getSharedPreferences().getBoolean(DELETE_INSTALLED_APK, true);
		mEnableAutoDownLoadUpdateForWifi = SharedPreferencesCenter.getInstance().getSharedPreferences().getBoolean(WIFI_DOWNLOAD_UPDATE, false);
	}
	
	private String getItemKey(String itemName,boolean checked){
		if(itemName.equals(AppStoreWrapperImpl.getInstance().getAppContext().getResources().getString(R.string.as_settings_auto_install_app_name))){
		    mEnableAutoInstall = checked;
			return AUTO_INSTALL;
		} else if(itemName.equals(AppStoreWrapperImpl.getInstance().getAppContext().getResources().getString(R.string.as_settings_installed_delete_apk_name))){
		    mEnableDeleteInstalledApk = checked;
			return DELETE_INSTALLED_APK;
		} else if(itemName.equals(AppStoreWrapperImpl.getInstance().getAppContext().getResources().getString(R.string.as_settings_wifi_auto_download_name))){
		    mEnableAutoDownLoadUpdateForWifi = checked;
			return WIFI_DOWNLOAD_UPDATE;
		}
		return null;
	}
	
	public void setSwitchCheckedValue(String itemName,boolean checked){
		SharedPreferences.Editor editor = SharedPreferencesCenter.getInstance().getSharedPreferences().edit();
		editor.putBoolean(getItemKey(itemName,checked), checked);
		editor.commit();
	}
	
	public boolean getSwithCheckedValue(String itemName){
		if(itemName.equals(AppStoreWrapperImpl.getInstance().getAppContext().getResources().getString(R.string.as_settings_auto_install_app_name))
				|| itemName.equals(AUTO_INSTALL)){
			return mEnableAutoInstall;
		} else if(itemName.equals(AppStoreWrapperImpl.getInstance().getAppContext().getResources().getString(R.string.as_settings_installed_delete_apk_name))
				|| itemName.equals(DELETE_INSTALLED_APK)){
			return mEnableDeleteInstalledApk;
		} else if(itemName.equals(AppStoreWrapperImpl.getInstance().getAppContext().getResources().getString(R.string.as_settings_wifi_auto_download_name))
				|| itemName.equals(WIFI_DOWNLOAD_UPDATE)){
			return mEnableAutoDownLoadUpdateForWifi;
		}
		return false;
	}
	/**
	 * 下载后是否自动安装apk
	 * @return
	 */
	public boolean getSettingValueAutoInstall(){
	    return mEnableAutoInstall;
	}
	/**
	 * 安装后是否自动删除已经apk文件
	 * @return
	 */
	public boolean getSettingValueDeleteInstalledApk(){
	    return mEnableDeleteInstalledApk;
	}
	/**
	 * wifi下自动下载需要更新的apk
	 * @return
	 */
	public boolean getSettingValueAutoDownLoadUpdateInWifi(){
	    return mEnableAutoDownLoadUpdateForWifi;
	}
	/**
	 * 清空缓存
	 */
	public void clearCache() {
	    FileUtil.clearDir(Properties.CACHE_PATH);
	}
}
