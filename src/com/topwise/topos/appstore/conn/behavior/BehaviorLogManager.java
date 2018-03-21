package com.topwise.topos.appstore.conn.behavior;

import com.topwise.topos.appstore.database.SharedPreferencesCenter;
import com.topwise.topos.appstore.utils.LogEx;

import android.content.SharedPreferences;

public class BehaviorLogManager extends BaseBehaviorLogManager{
	private static final String TAG = "BehaviorLogManager";
	private static BehaviorLogManager pthis;
	public synchronized static void setBehaviorLogManager(BehaviorLogManager manager) {
		pthis = manager;
	}
	public synchronized static BehaviorLogManager getInstance() {
		if(pthis == null) {
			pthis = new BehaviorLogManager();
		}
		return pthis;
	}
	protected BehaviorLogManager() {
		super();
	}
	
	public void location2Behavior(LocationInfo2 locationInfo) {
		LogEx.d(TAG, "location2Behavior,locationInfo2 == " + locationInfo);
        SharedPreferences shared = SharedPreferencesCenter.getInstance().getSharedPreferences();
        String preLocationInfo2 = shared.getString("LocationInfo2", "");
		if( preLocationInfo2.equals(locationInfo.toString()) ) {
			LogEx.d(TAG, "location not changed");
			return;
		}
		shared.edit().putString("LocationInfo2", locationInfo.toString()).commit();
		
		Location2Behavior obj = new Location2Behavior(locationInfo);
		addBehavior(obj);
	}
	public void deviceInfo2Behavior(DeviceInfo2 deviceInfo) {
		LogEx.d(TAG, "deviceInfo2Behavior,deviceInfo2 == " + deviceInfo);
		
		DeviceInfo2Behavior obj = new DeviceInfo2Behavior(deviceInfo);
		addBehavior(obj);
	}
	public void startActivityBehavior(String intentContent) {
		LogEx.d(TAG, "startActivityBehavior,intentContent == " + intentContent);
		StartActivityBehavior obj = new StartActivityBehavior(intentContent);
		addBehavior(obj);
	}
}
