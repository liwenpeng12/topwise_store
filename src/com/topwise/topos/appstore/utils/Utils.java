package com.topwise.topos.appstore.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.os.storage.StorageManager;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.manager.AppManager;
import com.topwise.topos.appstore.view.activity.ShortcutFolderActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

    public static float dp2px(Context context, float dpVal) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }


    /**
     * 获得内置SD卡的目录
     */
    public static String getInternalStoragePath() {
        try {
            Context context = AppStoreWrapperImpl.getInstance().getAppContext();
            String strInternalStoragePath = null;
            StorageManager storageManager = (StorageManager) context
                    .getSystemService(Context.STORAGE_SERVICE);
            if (null == storageManager) {
                return "/sdcard/";
            }

            Object[] volumeStorages = getVolumeList(storageManager);
            if (null == volumeStorages) {
                return "/sdcard/";
            }

            int nVolumeCount = volumeStorages.length;
            String strStorageState = null;
            for (int i = 0; i < nVolumeCount; i++) {
                if (!storageVolumeIsRemovable(volumeStorages[i])) {
                    String path = getVolumeStoragesPath(volumeStorages[i]);
                    strStorageState = getStrStorageState(path, storageManager);
                    if (null == strStorageState) {
                        break;
                    }

                    if (strStorageState.equals(Environment.MEDIA_MOUNTED)) {
                        strInternalStoragePath = path;
                    }
                }
            }
            if (strInternalStoragePath == null || strInternalStoragePath.length() == 0) {
                return "/sdcard/";
            }
            LogEx.d(strInternalStoragePath);
            return strInternalStoragePath;
        } catch (Exception e) {
            e.printStackTrace();
            return "/sdcard/";
        }
    }
    private static Object[] getVolumeList(StorageManager storageManager) {
        Object[] volumeStorages = null;
        try {
            Method method = StorageManager.class.getMethod("getVolumeList");
            method.setAccessible(true);
            volumeStorages = (Object[]) method.invoke(storageManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return volumeStorages;
    }
    private static boolean storageVolumeIsRemovable(Object storageVolume) {
        boolean value = false;
        try {
            Class<?> StorageVolumeClass = Class.forName("android.os.storage.StorageVolume");
            Method isRemovable = StorageVolumeClass.getMethod("isRemovable");
            value = (Boolean) isRemovable.invoke(storageVolume);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
    private static String getVolumeStoragesPath(Object storageVolume) {
        String path = null;
        try {
            Class<?> StorageVolumeClass = Class.forName("android.os.storage.StorageVolume");
            Method getPath = StorageVolumeClass.getMethod("getPath");
            path = (String) getPath.invoke(storageVolume);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }
    private static String getStrStorageState(String volumeStoragesPath, StorageManager storageManager) {
        String strStorageState = null;
        try {
            Method getVolumeState = StorageManager.class.getMethod("getVolumeState", String.class);
            strStorageState = (String) getVolumeState.invoke(storageManager, volumeStoragesPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strStorageState;
    }

    /**
     * 网络是否连接
     */
    public static boolean isNetworkConnected() {
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

    /**
     * 是否是WIFI连接
     */
    public static boolean isWifiConnected() {
        ConnectivityManager cm = (ConnectivityManager) AppStoreWrapperImpl.getInstance().getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null) {
            return wifiInfo.isConnected();
        }
        return false;
    }

    /**
     * 文件大小转为"1.2M"这样的字符串
     */
    public static String LengthToString(long length) {
        if (length / 1024 <= 0) {
            return "" + length + "B";
        } else if (length / 1024 > 0 && length / (1024 * 1024) <= 0) {
            return "" + length / 1024 + "KB";
        } else {
            return "" + length / (1024 * 1024) + "." + (length % (1024 * 1024)) / 1024 / 10 + "MB";
        }
    }

    /**
     * 字符串空格转为%20
     */
    public static String fillSpace(String orgName) {
        if (orgName.contains(" ")) {
            return orgName.replace(" ", "%20");
        }
        return orgName;
    }

    /**
     * 用于获取一个String的md5值
     */
    public static String getMd5(String str) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] bs = md5.digest(str.getBytes());
        StringBuilder sb = new StringBuilder(40);
        for (byte x : bs) {
            if ((x & 0xff) >> 4 == 0) {
                sb.append("0").append(Integer.toHexString(x & 0xff));
            } else {
                sb.append(Integer.toHexString(x & 0xff));
            }
        }
        return sb.toString();
    }

    /**
     * 生成应用信息的info html
     */
    public static String buildHtml(String useinfo, String size, boolean isfree, Resources res) {
        String html = "<font color='#000000'>" + useinfo + "  </font>";
        if (isfree) {
            html = html + "<font color='#fa5153'>" + "<strike>" + size +
                    "</strike>  " + res.getString(R.string.as_listitem_free_m) +
                    "</font>";
        } else {
            html = html + "<font color='#fa5153'>" + size +
                    "</font>";
        }
        return html;
    }

    /**
     * drawable转bitmap
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        // 取 drawable 的长宽  
        int w = drawable.getIntrinsicWidth();  
        int h = drawable.getIntrinsicHeight();  
  
        // 取 drawable 的颜色格式  
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888  
                : Bitmap.Config.RGB_565;  
        // 建立对应 bitmap  
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);  
        // 建立对应 bitmap 的画布  
        Canvas canvas = new Canvas(bitmap);  
        drawable.setBounds(0, 0, w, h);  
        // 把 drawable 内容画到画布中  
        drawable.draw(canvas);  
        return bitmap;  
    }

    /**
     * 安装apk
     */
    public static void installApk(final Context context, final String filePath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (installAppSilent(context, filePath) != 0) {
                    installAppNormal(context, filePath);
                }
            }
        }).start();
    }
    private static int installAppSilent(Context context, String filePath) {
        File file = new File(filePath);
        if (filePath == null || filePath.length() == 0 || file == null || !file.exists() || !file.isFile()) {
            return 1;
        }

        String[] args = { "pm", "install", "-r", "-i", context.getPackageName(), "--user", "0", filePath };
        ProcessBuilder processBuilder = new ProcessBuilder(args);

        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();
        int result;
        try {
            process = processBuilder.start();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;

            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }

            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = 2;
        } catch (Exception e) {
            e.printStackTrace();
            result = 2;
        } finally {
            try {
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }

        if (successMsg.toString().contains("Success") || successMsg.toString().contains("success")) {
            result = 0;
        } else {
            result = 2;
        }
        LogEx.d("successMsg:" + successMsg + ", ErrorMsg:" + errorMsg);
        return result;
    }
    private static int installAppNormal(Context context, String filePath) {
        File file = new File(filePath);
        if (filePath == null || filePath.length() == 0 || file == null || !file.exists() || !file.isFile()) {
            return 1;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + file.getAbsolutePath()), "application/vnd.android.package-archive");
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
        if (Properties.CHANNEL_IVVI.equals(AppStoreWrapperImpl.getInstance().getChannel())
                || Properties.CHANNEL_SHARP.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
            Intent broadcast = new Intent(Properties.INSTALL_SILENT_FAIL);
            broadcast.putExtra("packageName", AppManager.getInstance().parseLocalAppFile(file).pkg);
            context.sendBroadcast(broadcast);
        }
        return 0;
    }

    /**
     * 添加快捷方式
     * @return
     */
    public static boolean addShortcut(Context context) {
        Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        // 是否允许重复创建
        shortcut.putExtra("duplicate", false);
        // 设置名称
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getResources().getString(R.string.shortcut_folder_title));
        // 设置图标
        Parcelable icon = Intent.ShortcutIconResource.fromContext(context, R.drawable.as_ic_launcher);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        // 点击快捷方式的操作
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setClass(context, ShortcutFolderActivity.class);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        // 广播通知桌面去创建
        context.sendBroadcast(shortcut);
        return true;
    }

    /**
     * 是否存在快捷方式
     */
    public static boolean hasInstallShortcut(Context context) {
        boolean hasInstall = false;
        final String AUTHORITY = "com.android.launcher2.settings";
        Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
                + "/favorites?notify=true");
        // 这里总是failed to find provider info
        // com.android.launcher2.settings和com.android.launcher.settings都不行
        Cursor cursor = context.getContentResolver().query(CONTENT_URI,
                new String[] { "title", "iconResource" }, "title=?",
                new String[] { context.getResources().getString(R.string.shortcut_folder_title) }, null);
        if (cursor != null && cursor.getCount() > 0) {
            hasInstall = true;
        }
        return hasInstall;
    }

    public static void setWhiteStatusBar(Activity activity) {
        try {
            //Hook
            Class<?> c = Class.forName("android.view.View");
            //view.java : public static final int SYSTEM_UI_FLAG_LIGHT_STATUS_BAR = 0x00002000;
            Field field = c.getField("SYSTEM_UI_FLAG_LIGHT_STATUS_BAR");//正常现实上方状态栏
            int property = (Integer) field.get(c);
            activity.getWindow().getDecorView().setSystemUiVisibility(property);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
                activity.getWindow().setNavigationBarColor(0xfff0f0f0);
                View decorView = activity.getWindow().getDecorView();
                int lightFlag = Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1 ? 0x4000 : 0x10;
                decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | lightFlag);
                try {
                    Method method = activity.getWindow().getClass().getMethod("setNavigationDividerEnable", new Class[] {boolean.class});
                    method.invoke(activity.getWindow(), new Object[]{true});
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
        }
    }

    public static void printSignature(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature[] signs = packageInfo.signatures;
            Signature sign = signs[0];
            int hashcode = sign.hashCode();
            LogEx.e("Sign", context.getPackageName() + "Signature printSignaturehashcode = " + hashcode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
