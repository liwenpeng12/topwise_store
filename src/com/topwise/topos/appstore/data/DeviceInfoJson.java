package com.topwise.topos.appstore.data;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.telephony.TelephonyManager;


import org.json.JSONException;
import org.json.JSONObject;

import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.conn.jsonable.BaseJSONable;
import com.topwise.topos.appstore.conn.jsonable.JSONCreator;
import com.topwise.topos.appstore.utils.Utils;

public class DeviceInfoJson extends BaseJSONable {
	
	public String mImei = "";
	public String mChannel = "";
	public int mAndroidVersion;
	public String mAndroidVersionName = "";
	public String mPhoneModel = "";
	public String mOperatorName = "";
	public int mNetworkType = 0;
	public String mAndroidid = "";
	public String mImsi = "";
	public int mScreenWidth = 0;
	public int mScreenHeight = 0;
	public float mDensity = 0;
	public String mMacAddr = "";
	public String mLanguage = "";
	public boolean mIsRoot = false;
	public String mOs = "";
	public String mBrand = "";

	public DeviceInfoJson() {
	}

	public void init(Context context) {
		if (context == null) {
			return;
		}
		
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String android_id = "";
		try {
			android_id = Settings.System.getString(
					context.getContentResolver(),
					Settings.Secure.ANDROID_ID);
		} catch (Exception e) {
		}
		String deviceid = "";
		try {
			deviceid = tm.getDeviceId();
		} catch (Exception e) {
		}
		String imsi = "";
		try {
			imsi = tm.getSubscriberId();
		} catch (Exception e) {
		}
		
		int nettype = 0;
		try {
			nettype = isWifi(context) ? 0 : tm.getNetworkType();
		} catch (Exception e) {
		}
		
		int screenw = context.getResources().getDisplayMetrics().widthPixels;
		int screenh = context.getResources().getDisplayMetrics().heightPixels;
		
		mAndroidid = android_id;
		mImei = deviceid;
		mScreenWidth = screenw;
		mScreenHeight = screenh;
		mDensity = context.getResources().getDisplayMetrics().density;
		mAndroidVersion = android.os.Build.VERSION.SDK_INT;
		mAndroidVersionName = android.os.Build.VERSION.RELEASE;
		mPhoneModel = android.os.Build.MODEL;
		mIsRoot = isRootSystem();
		mImsi = imsi;
		mLanguage = context.getResources().getConfiguration().locale.getLanguage();
		mOperatorName = getOperators(context);
		mNetworkType = nettype;
		mMacAddr = getMacAddress(getLocalIpAddress(context));
		mChannel = AppStoreWrapperImpl.getInstance().getChannel();
		mOs = "Android";
		mBrand = android.os.Build.BRAND;
	}
	
	private boolean isRootSystem() {
		String buildTags = android.os.Build.TAGS;
		if (buildTags != null && buildTags.contains("test-keys")) {
			return true;
		}
		
		try {
			File file = new File("/system/app/Superuser.apk");
			if (file.exists()) {
				return true;
			}
		} catch (Throwable e) { 
		}
		
		File f = null;
		final String kSuSearchPaths[] = { "/system/bin/", "/system/xbin/",
				"/system/sbin/", "/sbin/", "/vendor/bin/" };
		try {
			for (int i = 0; i < kSuSearchPaths.length; i++) {
				f = new File(kSuSearchPaths[i] + "su");
				if (f != null && f.exists()) {
					return true;
				}
			}
		} catch (Throwable e) {
		}
		return false;
	}

	@Override
	public void writeToJSON(JSONObject dest) throws JSONException {
		dest.put("imei", mImei);
		dest.put("channel", mChannel);
		dest.put("androidVersion", mAndroidVersion);
		dest.put("androidVersionName",mAndroidVersionName);
		dest.put("phoneModel", mPhoneModel);
		dest.put("networkOperatorName", mOperatorName);
		dest.put("networkType", mNetworkType);
		dest.put("androidid", mAndroidid);
		dest.put("imsi", mImsi);
		dest.put("screenwidth", mScreenWidth);
		dest.put("screenheight", mScreenHeight);
		dest.put("density", mDensity);
		dest.put("macaddr", mMacAddr);
		dest.put("language", mLanguage);
		dest.put("isroot", mIsRoot);
		dest.put("os", mOs);
		dest.put("brand", mBrand);
	}
	
	@Override
	public void readFromJSON(JSONObject source) throws JSONException {
		mImei = source.getString("imei");
		mChannel = source.getString("channel");
		mAndroidVersion = source.getInt("androidVersion");
		mPhoneModel = source.getString("phoneModel");
		if (source.has("androidVersionName")) {
			mAndroidVersionName = source.getString("androidVersionName");
		}
		if(source.has("networkOperatorName")) {
			mOperatorName = source.getString("networkOperatorName");
		}
		if(source.has("networkType")) {
			mNetworkType = source.getInt("networkType");
		}
		
		if(source.has("androidid")) {
			mAndroidid = source.getString("androidid");
		}
		
		if(source.has("imsi")) {
			mImsi = source.getString("imsi");
		}
		
		if(source.has("screenwidth")) {
			mScreenWidth = source.getInt("screenwidth");
		}
		
		if(source.has("screenheight")) {
			mScreenHeight = source.getInt("screenheight");
		}
		
		if(source.has("density")) {
			mDensity = (float) source.getDouble("density");
		}
		
		if(source.has("macaddr")) {
			mMacAddr = source.getString("macaddr");
		}
		
		if(source.has("language")) {
			mLanguage = source.getString("language");
		}
		
		if(source.has("isroot")) {
			mIsRoot = source.getBoolean("isroot");
		}
		if (source.has("os")) {
			mOs = source.getString("os");
		}
		if (source.has("brand")) {
			mBrand = source.getString("brand");
		}
	}
	

	public static JSONCreator<DeviceInfoJson> CREATOR = new JSONCreator<DeviceInfoJson>(DeviceInfoJson.class);
	
	private static String getLocalIpAddress(Context context) {
		if (Utils.isNetworkConnected()) {
			try {
				for (Enumeration<NetworkInterface> en = NetworkInterface
						.getNetworkInterfaces(); en.hasMoreElements();) {
					NetworkInterface intf = en.nextElement();
					for (Enumeration<InetAddress> enumIpAddr = intf
							.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress() 
						 && !inetAddress.isLinkLocalAddress()
						 && inetAddress instanceof Inet4Address) {
							String ipaddr = inetAddress.getHostAddress().toString();
							return ipaddr;
						}
					}
				}
			} catch (Exception ex) {
			}
		}
		
		return "";
	}
	
	private static String getMacAddress(String ipaddr) {
		String mac_s = "";
		
		if (ipaddr != null && ipaddr.length() > 0) {
			try {
				byte[] mac;
				NetworkInterface ne = NetworkInterface.getByInetAddress(InetAddress
						.getByName(ipaddr));
				mac = ne.getHardwareAddress();
				mac_s = byte2hex(mac);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mac_s;
	}

	private static String byte2hex(byte[] b) {
		StringBuffer hs = new StringBuffer(b.length);
		String stmp = "";
		int len = b.length;
		for (int n = 0; n < len; n++) {
			stmp = Integer.toHexString(b[n] & 0xFF);
			if (stmp.length() == 1) {
				hs = hs.append("0").append(stmp);
			} else {
				hs = hs.append(stmp);
			}
		}
		return String.valueOf(hs);
	}
	
	private static String getOperators(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String OperatorsName = tm.getNetworkOperatorName();
		String IMSI = null;
		try {
			IMSI = tm.getSubscriberId();
		} catch (Exception e) {
		}
		if (IMSI != null) {
			if (IMSI.startsWith("46000") 
			 || IMSI.startsWith("46002")
			 || IMSI.startsWith("46007")) {
				OperatorsName = "CHINA_MOBILE";
			} else if (IMSI.startsWith("46001") || IMSI.startsWith("46006")) {
				OperatorsName = "CHINA_UNICOM";
			} else if (IMSI.startsWith("46003") || IMSI.startsWith("46005")) {
				OperatorsName = "CHINA_TELECOM";
			}
		}
		return OperatorsName;
	}
	
	private static boolean isWifi(Context mContext) {
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			return true;
		}
		return false;
	}
}
