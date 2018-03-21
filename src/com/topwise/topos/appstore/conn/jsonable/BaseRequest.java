package com.topwise.topos.appstore.conn.jsonable;

import org.json.JSONException;
import org.json.JSONObject;

import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.conn.behavior.DeviceInfo2;
import com.topwise.topos.appstore.conn.behavior.LocationInfo2;
import com.topwise.topos.appstore.utils.Properties;

public class BaseRequest extends BaseJSONable{
	public int versionCode = AppStoreWrapperImpl.getInstance().getAppVersionCode();
	public String version = AppStoreWrapperImpl.getInstance().getAppVersionName();
	public String originalVersion = "";
	public boolean isWifi = false;
	public DeviceInfo2 deviceInfo = DeviceInfo2.getDeviceInfo();
	public LocationInfo2 locationInfo = LocationInfo2.getLocationInfo();
	public long clientID = 0;
	public String appTag = Properties.APP_TAG;
	public String controlVersion ="0";
	public String themeId = "";
	public BaseRequest() {
	}
	
	public BaseRequest(boolean isWifi,long clientID) {
		this.isWifi = isWifi;
		this.clientID = clientID;
	}
	
	public BaseRequest(boolean isWifi,long clientID,String controlVersion,String themeId) {
		this.isWifi = isWifi;
		this.clientID = clientID;
		this.controlVersion = controlVersion;
		this.themeId = themeId;
	}
	@Override
	public void writeToJSON(JSONObject dest) throws JSONException {
		dest.put("versionCode", versionCode);
		dest.put("version", version);
		dest.put("isWifi", isWifi);
		
		JSONObject obj = new JSONObject();
		deviceInfo.writeToJSON(obj);
		dest.put("deviceInfo", obj);
		
		obj = new JSONObject();
		locationInfo.writeToJSON(obj);
		dest.put("locationInfo", obj);
		dest.put("originalVersion",originalVersion);
		
		dest.put("clientID", clientID);
		dest.put("appTag", appTag);
		
		dest.put("controlVersion", controlVersion);
		dest.put("themeId", themeId);
	}
	@Override
	public void readFromJSON(JSONObject source) throws JSONException {
		if(source.has("versionCode")) {
			versionCode = source.getInt("versionCode");
		}
		version = source.getString("version");
		isWifi = source.getBoolean("isWifi");
		
		JSONObject obj = source.getJSONObject("deviceInfo");
		deviceInfo = DeviceInfo2.CREATOR.createFromJSON(obj);
		
		obj = source.getJSONObject("locationInfo");
		locationInfo = LocationInfo2.CREATOR.createFromJSON(obj);
		if(source.has("originalVersion")) {
			originalVersion = source.getString("originalVersion");
		}
		if(source.has("clientID")) {
			clientID = source.getLong("clientID");
		}
		if(source.has("appTag")) {
			appTag = source.getString("appTag");
		}
		if(source.has("controlVersion")) {
			controlVersion = source.getString("controlVersion");
		}
		if(source.has("themeId")) {
			themeId = source.getString("themeId");
		}
	}
	public static JSONCreator<BaseRequest> CREATOR = new JSONCreator<BaseRequest>(BaseRequest.class);
}
