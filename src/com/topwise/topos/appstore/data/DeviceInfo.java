package com.topwise.topos.appstore.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

import com.topwise.topos.appstore.utils.Utils;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class DeviceInfo {
    
    private TelephonyManager mTM = null;
    private Context mContext = null;
    
    public DeviceInfo(Context context) {
        mContext = context;
        mTM = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }
    
    public int getScreenWidth() {
        return mContext.getResources().getDisplayMetrics().widthPixels;
    }
    
    public int getScreenHeight() {
        int width = mContext.getResources().getDisplayMetrics().widthPixels;
        int height = width * 16 / 9;
        // 如果是特殊类型的机器
        if (width == 1200) {
            height = 1920;
        }
        if (width == 480) {
            height = mContext.getResources().getDisplayMetrics().heightPixels;
        }
        return height;
    }

    public float getDensity() {
        return mContext.getResources().getDisplayMetrics().density;
    }

    public int getDensityDpi() {
        return mContext.getResources().getDisplayMetrics().densityDpi;
    }
    
    public String getIMEI() {
        try {
            String imei = mTM.getDeviceId();
            return imei == null ? "" : imei;
        } catch (Exception e) {
            return "";
        } catch (Throwable e) {
            return "";
        }
    }
    
    public String getIMSI() {
        try {
            return mTM.getSubscriberId();
        }catch (Exception e){}
        return "";
    }

    public int getCellId() {
        try {
            CellLocation cellLocation = mTM.getCellLocation();
            if (cellLocation instanceof GsmCellLocation) {
                return ((GsmCellLocation) cellLocation).getCid();
            } else if (cellLocation instanceof CdmaCellLocation) {
                return ((CdmaCellLocation) cellLocation).getBaseStationId();
            }
        }catch (Exception e){}
        return 0;
    }
    
    public int getNetworkType() {
        return mTM.getNetworkType();
    }
    
    public String getNetworkTypeString() {
        if (mTM.getNetworkType() == TelephonyManager.NETWORK_TYPE_UNKNOWN) {
            return "wifi";
        } else {
            return "" + mTM.getNetworkType();
        }
    }

    public boolean isWifi() {
        try {
            ConnectivityManager connectMgr = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netinfo = connectMgr.getActiveNetworkInfo();
            if (netinfo != null && netinfo.isConnected() && netinfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getNetworkOperatorName() {
        return mTM.getNetworkOperatorName();
    }

    /**
     * 获取运营商编号，对应手助协议carrier字段
     * @return 运营商编号, 如：46001
     */
    public String getCarrierName() {
        String carrier = "";
        if (mTM.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA && mTM.getSimState() == TelephonyManager.SIM_STATE_READY) {
            carrier = mTM.getNetworkOperator();
        } else if (mTM.getSimState() == TelephonyManager.SIM_STATE_READY) {
            carrier = mTM.getSimOperator();
        }
        return carrier;
    }

    public int getAndroidVersion() {
        return Build.VERSION.SDK_INT;
    }

    public String getAndroidVersionName() {
        return Build.VERSION.RELEASE;
    }
    
    public String getProductModel() {
        return Build.MODEL;
    }

    public String getSerialNo() {
        return Build.SERIAL;
    }

    public String getBrand() {
        return Build.BRAND;
    }

    public String getAndroidId() {
        return Settings.System.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public String getIp() {
        if (Utils.isNetworkConnected()) {
            try {
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                    NetworkInterface intf = en.nextElement();
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress instanceof Inet4Address) {
                            String ipaddr = inetAddress.getHostAddress().toString();
                            return ipaddr;
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return "";
    }

    public String getMac() {
        String mac_s = "";
        String ipaddr = getIp();
        if (ipaddr != null && ipaddr.length() > 0) {
            try {
                byte[] mac;
                NetworkInterface ne = NetworkInterface.getByInetAddress(InetAddress.getByName(ipaddr));
                mac = ne.getHardwareAddress();
                if (mac != null && mac.length > 0) {
                    mac_s = bytes2HexString(mac);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mac_s;
    }
    public static String bytes2HexString(byte[] b) {
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex;
        }
        return ret;
    }

    public String getWebViewUserAgent() {
        return System.getProperty("http.agent");
    }

    public boolean isRootSystem() {
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

}
