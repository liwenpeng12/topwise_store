package com.topwise.topos.appstore.conn.protocol;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.conn.behavior.UserTrack;
import com.topwise.topos.appstore.data.AdIcon;
import com.topwise.topos.appstore.data.AdIconModule;
import com.topwise.topos.appstore.data.AppInfo;
import com.topwise.topos.appstore.data.AppUpgradeInfo;
import com.topwise.topos.appstore.data.Banner;
import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.data.Hotword;
import com.topwise.topos.appstore.data.LBannerModule;
import com.topwise.topos.appstore.data.Label;
import com.topwise.topos.appstore.data.NotificationInfo;
import com.topwise.topos.appstore.data.Rank;
import com.topwise.topos.appstore.data.SBannerModule;
import com.topwise.topos.appstore.data.Welcome;
import com.topwise.topos.appstore.data.AppInfo.Tag;
import com.topwise.topos.appstore.location.LocationCenter;
import com.topwise.topos.appstore.manager.AppManager;
import com.topwise.topos.appstore.manager.HttpManager;
import com.topwise.topos.appstore.utils.AESUtility;
import com.topwise.topos.appstore.utils.AsynTaskManager.ImageLoadCallBack;
import com.topwise.topos.appstore.utils.BitmapUtil;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.utils.Properties;
import com.topwise.topos.appstore.utils.Utils;
import com.topwise.topos.appstore.R;

public class Protocol {
    
    public static final String PROTOCOL_VERSION = "6";
    
    private String URL_CTRL = "http://appstore.topwisesz.com";
//    private String URL_CTRL = "http://appstore.topwisesz.com/query/";
    private String URL_SERVER = "";
    private String URL_SERVER_BAK = "";
    private String mUrl = "http://appstore.topwisesz.com";
    
    private int mReconnectCount = 0;
    
    public int pkey = 0;
    public String psecret = "";

    public String mHomePageIconUrl = "";
    public String mHomePageUrl = "";
    public String mSearchBannerUrl = "";
    
    private static Protocol mThis = null;

    private Protocol() {
        try {
            ApplicationInfo appInfo = AppStoreWrapperImpl.getInstance().getAppContext().getPackageManager().getApplicationInfo(AppStoreWrapperImpl.getInstance().getAppContext().getPackageName(),PackageManager.GET_META_DATA);
            pkey = appInfo.metaData.getInt("PKEY", 1001);
            psecret = appInfo.metaData.getString("PSECRET", "17wosdkwoju");
            AESUtility.PKEY = "" + pkey;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static Protocol getInstance() {
        if (mThis == null) {
            synchronized (Protocol.class) {
                if (mThis == null) {
                    mThis = new Protocol();
                }
            }
        }
        return mThis;
    }
    
    public void changeServer() {
        if (mUrl == null || "".equals(mUrl)) {
            getHostUrl();
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        if (URL_SERVER.equals(mUrl)) {
            mUrl = URL_SERVER_BAK;
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    mUrl = URL_SERVER;
                }
                
            }, 2*60*60*1000);
            return;
        }
        if (URL_SERVER_BAK.equals(mUrl)) {
            mUrl = URL_SERVER;
            return;
        }
    }
    
    public String getServerUrl() {
        if (mUrl == null || "".equals(mUrl)) {
            mUrl = URL_SERVER;
        }
        if (mUrl == null || "".equals(mUrl)) {
            getHostUrl();
        }
        return mUrl;
    }
    
    public void getHostUrl() {
        JSONObject data = new JSONObject();
        try {
            data.put("channel", AppStoreWrapperImpl.getInstance().getChannel());//dingzhi
            data.put("appver", AppStoreWrapperImpl.getInstance().getAppVersionCode());
            data.put("imei", AppStoreWrapperImpl.getInstance().getDeviceInfo().getIMEI());
            data.put("apiver", Protocol.PROTOCOL_VERSION);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //将渠道号，app版本，IMEI号，api版本利用添加到JsonObject对象中
        //base64进行加密
        String dataBase64 = Base64.encodeToString(data.toString().getBytes(), Base64.NO_WRAP);
        //AjaxParam参数携带加密后的信息对象
        HttpManager.getInstance().mAjaxParams.put("data", dataBase64);
        String tm = "" + System.currentTimeMillis()/1000;
        HttpManager.getInstance().mAjaxParams.put("tm", tm);
        //md5加密
        String secret = Utils.getMd5(dataBase64 + pkey + psecret + tm);
        //最终加密后的数据
        HttpManager.getInstance().mAjaxParams.put("secret", secret);
        
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (Properties.CHANNEL_COOLMART.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
                    URL_CTRL = "http://cat.moyumedia.com/query/";
                }
                //Fianl post请求传参
                String t = (String) HttpManager.getInstance().postSync(URL_CTRL);
                Log.d("zr","getHostUrl的"+URL_CTRL);
                Log.d("zr","getHostUrl的"+t);
                Log.d("liwenpeng","得到的参数T:"+t);
                LogEx.d(t);
                //抛出异常，不会往下走
                try {
                    JSONObject mainJson = new JSONObject(t);
                    Log.d("liwenpeng","mainJson"+mainJson);
                    if (mainJson.getBoolean("result")) {
                        String encodeDataString = mainJson.getString("data");
                        //解密
                        String dataString = AESUtility.decode(encodeDataString, AESUtility.PKEY,
                                AESUtility.PSECRETS.get(AESUtility.PKEY),
                                AESUtility.IV.get(AESUtility.PKEY));

                        JSONObject data = new JSONObject(dataString);
                        //解析
                        URL_SERVER = data.getString("master");
                        Log.d("liwenpeng","URL_SERVER:"+URL_SERVER);
                        URL_SERVER_BAK = data.getString("slave1");
                        Log.d("liwenpeng","URL_SERVER_BAK:"+URL_SERVER_BAK);
                        try {
                            JSONObject newsFlow = data.getJSONObject("newsflow");
                            Log.d("liwenpeng","newsFlow:"+newsFlow);
                            JSONObject homePage = newsFlow.getJSONObject("homepage");
                            mHomePageIconUrl = homePage.getString("icon");
                            mHomePageUrl = homePage.getString("url");
                            JSONObject searchbotom = newsFlow.getJSONObject("searchbotom");
                            mSearchBannerUrl = searchbotom.getString("url");
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    DataPool.getInstance().sendObserver(DataPool.TYPE_NEWS_ICON);
                                }
                            }, 500);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    Log.d("liwenpeng","e"+e.toString());
                    e.printStackTrace();
                }            
            }
            
        }).start();

        while (mReconnectCount < 10) {
            if (URL_SERVER != null && !"".equals(URL_SERVER)) {
                mReconnectCount = 0;
                break;
            }
            mReconnectCount++;
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 页面
     * @param pageId 页面id
     * @return url
     */
    public String getPageUrl(int pageId) {
        JSONObject query = new JSONObject();
        try {
            query.put("type", Properties.MODULE_TYPE_PAGE);
            query.put("id", pageId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String queryBase64 = Base64.encodeToString(query.toString().getBytes(), Base64.DEFAULT);
        HttpManager.getInstance().mAjaxParams.put("query", queryBase64);
        String tm = "" + System.currentTimeMillis()/1000;
        HttpManager.getInstance().mAjaxParams.put("tm", tm);
        String secret = Utils.getMd5(HttpManager.getInstance().mCommonBase64 + pkey + psecret + queryBase64 + tm);
        HttpManager.getInstance().mAjaxParams.put("secret", secret);
        return getServerUrl() + "/query";
    }
    
    /**
     * 应用详情
     * @param packageName 应用包名
     * @return url
     */
    public String getAppDetailUrl(String packageName) {
        JSONObject query = new JSONObject();
        try {
            query.put("type", Properties.MODULE_TYPE_APP);
            query.put("id", packageName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String queryBase64 = Base64.encodeToString(query.toString().getBytes(), Base64.DEFAULT);
        HttpManager.getInstance().mAjaxParams.put("query", queryBase64);
        String tm = "" + System.currentTimeMillis()/1000;
        HttpManager.getInstance().mAjaxParams.put("tm", tm);
        String secret = Utils.getMd5(HttpManager.getInstance().mCommonBase64 + pkey + psecret + queryBase64 + tm);
        HttpManager.getInstance().mAjaxParams.put("secret", secret);
        return getServerUrl() + "/query";
    }
    
    /**
     * label
     * @param labelId label id
     * @return url
     */
    public String getLabelUrl(int labelId) {
        JSONObject query = new JSONObject();
        try {
            query.put("type", Properties.MODULE_TYPE_LABEL);
            query.put("id", labelId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String queryBase64 = Base64.encodeToString(query.toString().getBytes(), Base64.DEFAULT);
        HttpManager.getInstance().mAjaxParams.put("query", queryBase64);
        String tm = "" + System.currentTimeMillis()/1000;
        HttpManager.getInstance().mAjaxParams.put("tm", tm);
        String secret = Utils.getMd5(HttpManager.getInstance().mCommonBase64 + pkey + psecret + queryBase64 + tm);
        HttpManager.getInstance().mAjaxParams.put("secret", secret);
        return getServerUrl() + "/query";
    }
    
    /**
     * 榜单
     * @param rankId 榜单id
     * @return url
     */
    public String getRankUrl(int rankId) {
        JSONObject query = new JSONObject();
        try {
            query.put("type", Properties.MODULE_TYPE_RANK);
            query.put("id", rankId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String queryBase64 = Base64.encodeToString(query.toString().getBytes(), Base64.DEFAULT);
        HttpManager.getInstance().mAjaxParams.put("query", queryBase64);
        String tm = "" + System.currentTimeMillis()/1000;
        HttpManager.getInstance().mAjaxParams.put("tm", tm);
        String secret = Utils.getMd5(HttpManager.getInstance().mCommonBase64 + pkey + psecret + queryBase64 + tm);
        HttpManager.getInstance().mAjaxParams.put("secret", secret);
        return getServerUrl() + "/query";
    }
    
    /**
     * 分类
     * @param typeId 分类id
     * @param pageNum 当前请求页，页数从0开始
     * @return url
     */
    public String getTypeUrl(int typeId, int pageNum) {
        JSONObject query = new JSONObject();
        try {
            query.put("type", Properties.MODULE_TYPE_TYPE);
            query.put("id", typeId);
            query.put("start", pageNum * Properties.PAGE_NUM);
            query.put("count", Properties.PAGE_NUM);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String queryBase64 = Base64.encodeToString(query.toString().getBytes(), Base64.DEFAULT);
        HttpManager.getInstance().mAjaxParams.put("query", queryBase64);
        String tm = "" + System.currentTimeMillis()/1000;
        HttpManager.getInstance().mAjaxParams.put("tm", tm);
        String secret = Utils.getMd5(HttpManager.getInstance().mCommonBase64 + pkey + psecret + queryBase64 + tm);
        HttpManager.getInstance().mAjaxParams.put("secret", secret);
        return getServerUrl() + "/query";
    }
    
    /**
     * 应用升级
     * @param packageNames 包名列表
     * @return url
     */
    public String getAppUpgradeUrl(JSONArray packageNames) {
        JSONObject query = new JSONObject();
        try {
            query.put("type", "update");
            query.put("id", packageNames);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String queryBase64 = Base64.encodeToString(query.toString().getBytes(), Base64.DEFAULT);
        HttpManager.getInstance().mAjaxParams.put("query", queryBase64);
        String tm = "" + System.currentTimeMillis()/1000;
        HttpManager.getInstance().mAjaxParams.put("tm", tm);
        String secret = Utils.getMd5(HttpManager.getInstance().mCommonBase64 + pkey + psecret + queryBase64 + tm);
        HttpManager.getInstance().mAjaxParams.put("secret", secret);
        return getServerUrl() + "/query";
    }
    
    /**
     * 相关应用
     * @param relatedType 相关类型 0-类似应用 1-大家喜欢 2-同时下载 3-热门应用
     * @param appId 应用id
     * @param typeId 分类id
     * @return url
     */
    public String getRelatedUrl(int relatedType, String appId, int typeId) {
        JSONObject query = new JSONObject();
        try {
            query.put("type", "alike");
            query.put("api", relatedType);
            query.put("id", appId);
            query.put("apptype", typeId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String queryBase64 = Base64.encodeToString(query.toString().getBytes(), Base64.DEFAULT);
        HttpManager.getInstance().mAjaxParams.put("query", queryBase64);
        String tm = "" + System.currentTimeMillis()/1000;
        HttpManager.getInstance().mAjaxParams.put("tm", tm);
        String secret = Utils.getMd5(HttpManager.getInstance().mCommonBase64 + pkey + psecret + queryBase64 + tm);
        HttpManager.getInstance().mAjaxParams.put("secret", secret);
        return getServerUrl() + "/query";
    }
    
    /**
     * 搜索
     * @param key 关键字
     * @return url
     */
    public String getSearchUrl(String key) {
        JSONObject query = new JSONObject();
        try {
            query.put("type", "search");
            query.put("key", key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String queryBase64 = Base64.encodeToString(query.toString().getBytes(), Base64.DEFAULT);
        HttpManager.getInstance().mAjaxParams.put("query", queryBase64);
        String tm = "" + System.currentTimeMillis()/1000;
        HttpManager.getInstance().mAjaxParams.put("tm", tm);
        String secret = Utils.getMd5(HttpManager.getInstance().mCommonBase64 + pkey + psecret + queryBase64 + tm);
        HttpManager.getInstance().mAjaxParams.put("secret", secret);
        return getServerUrl() + "/query";
    }
    
    /**
     * 搜索热词
     * @return url
     */
    public String getSearchHotwordUrl() {
        JSONObject query = new JSONObject();
        try {
            query.put("type", "hotkey");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String queryBase64 = Base64.encodeToString(query.toString().getBytes(), Base64.DEFAULT);
        HttpManager.getInstance().mAjaxParams.put("query", queryBase64);
        String tm = "" + System.currentTimeMillis()/1000;
        HttpManager.getInstance().mAjaxParams.put("tm", tm);
        String secret = Utils.getMd5(HttpManager.getInstance().mCommonBase64 + pkey + psecret + queryBase64 + tm);
        HttpManager.getInstance().mAjaxParams.put("secret", secret);
        return getServerUrl() + "/query";
    }
    
    /**
     * 广告页面，包括欢迎界面和通知
     * @return url
     */
    public String getAdUrl() {
        JSONObject query = new JSONObject();
        try {
            query.put("type", "ad");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String queryBase64 = Base64.encodeToString(query.toString().getBytes(), Base64.DEFAULT);
        HttpManager.getInstance().mAjaxParams.put("query", queryBase64);
        String tm = "" + System.currentTimeMillis()/1000;
        HttpManager.getInstance().mAjaxParams.put("tm", tm);
        String secret = Utils.getMd5(HttpManager.getInstance().mCommonBase64 + pkey + psecret + queryBase64 + tm);
        HttpManager.getInstance().mAjaxParams.put("secret", secret);
        return getServerUrl() + "/query";
    }
    
    /**
     * 用于详情页的随机显示banner
     * @return url
     */
    public String getBannersUrl() {
        JSONObject query = new JSONObject();
        try {
            query.put("type", "lbanner");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String queryBase64 = Base64.encodeToString(query.toString().getBytes(), Base64.DEFAULT);
        HttpManager.getInstance().mAjaxParams.put("query", queryBase64);
        String tm = "" + System.currentTimeMillis()/1000;
        HttpManager.getInstance().mAjaxParams.put("tm", tm);
        String secret = Utils.getMd5(HttpManager.getInstance().mCommonBase64 + pkey + psecret + queryBase64 + tm);
        HttpManager.getInstance().mAjaxParams.put("secret", secret);
        return getServerUrl() + "/query";
    }
    
    /**
     * 随机的推荐应用
     * @return url
     */
    public String getRandomAppsUrl() {
        JSONObject query = new JSONObject();
        try {
            query.put("type", "recommend");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String queryBase64 = Base64.encodeToString(query.toString().getBytes(), Base64.DEFAULT);
        HttpManager.getInstance().mAjaxParams.put("query", queryBase64);
        String tm = "" + System.currentTimeMillis()/1000;
        HttpManager.getInstance().mAjaxParams.put("tm", tm);
        String secret = Utils.getMd5(HttpManager.getInstance().mCommonBase64 + pkey + psecret + queryBase64 + tm);
        HttpManager.getInstance().mAjaxParams.put("secret", secret);
//        Log.d("cvbb","requestUrl="+getServerUrl() + "/query"+"//////tm="+tm+"////secret="+secret);
        Log.d("zr","getRandomAppsUrl="+getServerUrl() +HttpManager.getInstance().mAjaxParams.toString());
        return getServerUrl() + "/query";
    }

    /**
     * 自升级
     * @return url
     */
    public String getSelfUpgradeUrl() {
        JSONObject query = new JSONObject();
        try {
            query.put("type", "selfupdate");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String queryBase64 = Base64.encodeToString(query.toString().getBytes(), Base64.DEFAULT);
        HttpManager.getInstance().mAjaxParams.put("query", queryBase64);
        String tm = "" + System.currentTimeMillis()/1000;
        HttpManager.getInstance().mAjaxParams.put("tm", tm);
        String secret = Utils.getMd5(HttpManager.getInstance().mCommonBase64 + pkey + psecret + queryBase64 + tm);
        HttpManager.getInstance().mAjaxParams.put("secret", secret);
        return getServerUrl() + "/query";
    }

    /**
     * 获取真实下载链接
     * @param appName app名称
     * @param packageName app包名
     * @param versionCode 要升级的版本号
     * @param isUpdate 是更新还是新下载
     * @return url
     */
    public String getDownloadUrl(String appName, String packageName, int versionCode, boolean isUpdate) {
        JSONObject param = new JSONObject();
        try {
            param.put("apiver", PROTOCOL_VERSION);
            param.put("apppkg", AppStoreWrapperImpl.getInstance().getAppContext().getPackageName());
            param.put("appver", "" + AppStoreWrapperImpl.getInstance().getAppVersionCode());
            param.put("imei", AppStoreWrapperImpl.getInstance().getDeviceInfo().getIMEI());
            param.put("imsi", AppStoreWrapperImpl.getInstance().getDeviceInfo().getIMSI());
            param.put("andid", AppStoreWrapperImpl.getInstance().getDeviceInfo().getAndroidId());
            param.put("sn", AppStoreWrapperImpl.getInstance().getDeviceInfo().getSerialNo());
            param.put("sw", "" + AppStoreWrapperImpl.getInstance().getDeviceInfo().getScreenWidth());
            param.put("sh", "" + AppStoreWrapperImpl.getInstance().getDeviceInfo().getScreenHeight());
            param.put("dpi", "" + AppStoreWrapperImpl.getInstance().getDeviceInfo().getDensityDpi());
            param.put("density", "" + AppStoreWrapperImpl.getInstance().getDeviceInfo().getDensity());
            param.put("andvercode", "" + AppStoreWrapperImpl.getInstance().getDeviceInfo().getAndroidVersion());
            param.put("andvername", AppStoreWrapperImpl.getInstance().getDeviceInfo().getAndroidVersionName());
            param.put("mac", AppStoreWrapperImpl.getInstance().getDeviceInfo().getMac());
            param.put("city", LocationCenter.getInstance().getAddress().getLocality());
            param.put("lat", "" + LocationCenter.getInstance().getLocation().getLatitude());
            param.put("lng", "" + LocationCenter.getInstance().getLocation().getLongitude());
            param.put("ua", AppStoreWrapperImpl.getInstance().getDeviceInfo().getWebViewUserAgent());
            param.put("ip", AppStoreWrapperImpl.getInstance().getDeviceInfo().getIp());
            param.put("model", AppStoreWrapperImpl.getInstance().getDeviceInfo().getProductModel());
            param.put("net", "" + AppStoreWrapperImpl.getInstance().getDeviceInfo().getNetworkType());
            param.put("carrier", AppStoreWrapperImpl.getInstance().getDeviceInfo().getCarrierName());
            param.put("bdid", "" + AppStoreWrapperImpl.getInstance().getDeviceInfo().getCellId());
            param.put("dltype", isUpdate ? "update" : "new");
            param.put("channel", AppStoreWrapperImpl.getInstance().getChannel());
            param.put("brand", AppStoreWrapperImpl.getInstance().getDeviceInfo().getBrand());
            param.put("iswifi", "" + AppStoreWrapperImpl.getInstance().getDeviceInfo().isWifi());
            param.put("appvername", AppStoreWrapperImpl.getInstance().getAppVersionName());
            JSONObject app = new JSONObject();
            app.put("name", appName);
            app.put("pkg", packageName);
            app.put("vercode", "" + versionCode);
            param.put("app", app);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String c = "" + pkey;
        HttpManager.getInstance().mAjaxParams.put("c", c);
        String d = Base64.encodeToString(param.toString().getBytes(), Base64.NO_WRAP);
        HttpManager.getInstance().mAjaxParams.put("d", d);
        String p = packageName;
        HttpManager.getInstance().mAjaxParams.put("p", p);
        String t = "" + System.currentTimeMillis()/1000;
        HttpManager.getInstance().mAjaxParams.put("t", t);
        String v = "" + versionCode;
        HttpManager.getInstance().mAjaxParams.put("v", v);
        HttpManager.getInstance().mAjaxParams.put("s", Utils.getMd5(c + d + p + psecret + t + v));
        return getServerUrl() + "/download/app";
    }

    public String parseStatisResult(String t) {
        try {
            JSONObject mainJson = new JSONObject(t);
            boolean result = mainJson.getBoolean("result");
            if (!result) {
                return mainJson.getString("msg");
            }
            return "" + true;
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        }
    }
    
    public String parseSearchHotword(String t) {
        try {
            JSONObject mainJson = new JSONObject(t);
            boolean result = mainJson.getBoolean("result");
            if (!result) {
                return mainJson.getString("msg");
            }
            JSONArray words = mainJson.getJSONArray("data");
            for (int i = 0; i < words.length(); i++) {
                JSONObject word = words.getJSONObject(i);
                Hotword hotword = new Hotword();
                hotword.type = word.getString("type");
                hotword.id = word.getString("id");
                hotword.weight = word.getInt("weight");
                hotword.recommend = word.getInt("command") == 0 ? false : true;
                hotword.hotword = word.getString("keyword");
                DataPool.getInstance().addSearchHotword(hotword, true);
            }
            return "" + true;
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        }
    }
    
    public String parseIsPaid(String t, boolean isPaid) {
        try {
            JSONObject mainJson = new JSONObject(t);
            boolean result = mainJson.getBoolean("result");
            if (!result) {
                return mainJson.getString("msg");
            }
            isPaid = mainJson.getBoolean("charged");
            return "" + true;
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        }
    }
    
    public String parsePayOrder(String t, String order) {
        try {
            JSONObject mainJson = new JSONObject(t);
            boolean result = mainJson.getBoolean("result");
            if (!result) {
                return mainJson.getString("msg");
            }
            order = mainJson.getString("orderno");
            return "" + true;
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        }
    }
    
    public String parseChargeStatis(String t) {
        try {
            JSONObject mainJson = new JSONObject(t);
            boolean result = mainJson.getBoolean("result");
            if (!result) {
                return mainJson.getString("msg");
            }
            return "" + true;
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        }
    }
    
    public String parseSelfUpgrade(String t, AppUpgradeInfo info) {
        try {
            JSONObject mainJson = new JSONObject(t);
            boolean result = mainJson.getBoolean("result");
            if (!result) {
                return mainJson.getString("msg");
            }
            JSONObject app = mainJson.getJSONObject("data");
            int vercode = app.getInt("vercode");
//            if (vercode <= AppStoreWrapperImpl.getInstance().getAppVersionCode()) {
//                return AppStoreWrapperImpl.getInstance().getAppContext().getString(R.string.as_already_newest);
//            }
            info.verCode = app.getInt("vercode");
            info.verName = app.getString("vername");
            info.date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(app.getLong("ts")));
            info.size = Utils.LengthToString(app.getLong("size"));
            info.url = app.getString("url");
            info.isForceUpgrade = app.getBoolean("force");
            info.desc = "";
            JSONArray descArray = app.getJSONArray("desc");
            for (int i = 0; i < descArray.length(); i++) {
                info.desc += descArray.get(i).toString() + "\r\n";
            }
            return "" + true;
        } catch (Exception e) {
            e.printStackTrace();
            return AppStoreWrapperImpl.getInstance().getAppContext().getString(R.string.as_already_newest);
        }
    }
    
    public String parseHistory(String t, AppUpgradeInfo info) {
        try {
            JSONObject mainJson = new JSONObject(t);
            boolean result = mainJson.getBoolean("result");
            if (!result) {
                return mainJson.getString("msg");
            }
            return "" + true;
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        }
    }
    
    private AppInfo parseAppInfoElement(JSONObject jsonObject, AppInfo info) {
        try {
            info.id = info.pkg = jsonObject.getString("pkg");
            try {
                info.weight = jsonObject.getInt("weight");
            } catch (JSONException e) {
            }
            info.icon_url = jsonObject.getString("icon");
            info.name = jsonObject.getString("name");
            info.sdesc = jsonObject.getString("sdesc");
            info.file_url = jsonObject.getString("fileurl");
            info.vercode = jsonObject.getInt("vercode");
            info.vername = jsonObject.getString("vername");
            info.size = jsonObject.getString("size");
            info.isfree = jsonObject.getInt("isfree") == 0 ? false : true;
            info.awardkey = jsonObject.getInt("awardkey");
            info.awardvalue = jsonObject.getInt("awardvalue");
            info.date = jsonObject.getString("updatetime");
            info.downloads = jsonObject.getString("downloads");
            info.type = jsonObject.getInt("type");
            info.islocal = jsonObject.getInt("islocal") == 0 ? false : true;
            try {
                info.upgrade_desc = jsonObject.getString("appupdate");
            } catch (JSONException e) {
            }
            try {
                info.adimage = jsonObject.getString("adimage");
                info.adcontent = jsonObject.getString("adcontent");
                info.adurl = jsonObject.getString("adurl");
            } catch (JSONException e) {
            }
            try {
                JSONArray tags = jsonObject.getJSONArray("tags");
                info.tags.clear();
                for (int i = 0; i < tags.length(); i++) {
                    JSONObject tagData = tags.getJSONObject(i);
                    Tag tag = new Tag();
                    tag.name = tagData.getString("name");
//                    tag.color = Integer.parseInt(tagData.getString("color").substring(1), 16);
                    tag.bgcolor = Color.parseColor(tagData.getString("bgcolor"));
                    tag.txtcolor = Color.parseColor(tagData.getString("color"));
                    info.tags.add(tag);
                }
            } catch (JSONException e) {
            }
            try {
                info.report_show.clear();
                info.report_start_download.clear();
                JSONArray events = jsonObject.getJSONArray("dlevents");
                for (int i = 0; i < events.length(); i++) {
                    JSONObject event = events.getJSONObject(i);
                    if (event.getInt("type") == 9) {
                        info.report_show.add(event.getString("url"));
                    } else if (event.getInt("type") == 0) {
                        info.report_start_download.add(event.getString("url"));
                    }
                }
            } catch (JSONException e) {
            }
            info.flag = AppManager.getInstance().getAppLocalFlag(info);
            UserTrack.getInstance().reportAppShowed(info);
            return info;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private Banner parseBannerElement(JSONObject jsonObject, boolean isLarge) {
        try {
            Banner info = new Banner();
            info.large_banner = isLarge;
            info.title = jsonObject.getString("title");
            info.img_url = jsonObject.getString("img");
            info.target = jsonObject.getString("target");
            info.weight = jsonObject.getInt("weight");
            info.id = info.target_url = jsonObject.getString("targetid");
            if (info.target.equals("page") && info.target_url.equals("0")) {
                info.target = "category";
            }
            try {
                info.desc = info.target_name = jsonObject.getString("desc");;
            } catch (JSONException e) {
            }
            return info;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public String parsePage(String t, int pageId) {
        int datatype = pageId; // page module的datatype等于page id
        DataPool.getInstance().clearModules(datatype);
        try {
            JSONObject mainJson = new JSONObject(t);
            boolean result = mainJson.getBoolean("result");
            if (!result) {
                return mainJson.getString("msg");
            }
            JSONArray datas = mainJson.getJSONArray("data");
            for (int i = 0; i < datas.length(); i++) {
                JSONObject data = datas.getJSONObject(i);
                if ("lbanner".equals(data.getString("module"))) {
                    LBannerModule lbanners = new LBannerModule();
                    int module_weight = data.getInt("weight");
                    lbanners.module_weight = module_weight;
                    JSONArray module_datas = data.getJSONArray("data");
                    for (int j = 0; j < module_datas.length(); j++) {
                        JSONObject module = module_datas.getJSONObject(j);
                        Banner info = parseBannerElement(module, true);
                        info.module_weight = module_weight;
                        lbanners.banners.add(info);
                        DataPool.getInstance().addBanner(DataPool.TYPE_BANNER_LARGE, info);
                    }
                    DataPool.getInstance().addModule(datatype, lbanners);
                } else if ("sbanner".equals(data.getString("module"))) {
                    SBannerModule sbanners = new SBannerModule();
                    int module_weight = data.getInt("weight");
                    sbanners.module_weight = module_weight;
                    JSONArray module_datas = data.getJSONArray("data");
                    for (int j = 0; j < module_datas.length(); j++) {
                        JSONObject module = module_datas.getJSONObject(j);
                        Banner info = parseBannerElement(module, false);
                        info.module_weight = module_weight;
                        sbanners.banners.add(info);
                        DataPool.getInstance().addBanner(DataPool.TYPE_BANNER_SMALL, info);
                    }
                    DataPool.getInstance().addModule(datatype, sbanners);
                } else if ("adicons".equals(data.getString("module"))) {
                    AdIconModule adicons = new AdIconModule();
                    int module_weight = data.getInt("weight");
                    adicons.module_weight = module_weight;
                    JSONArray module_datas = data.getJSONArray("data");
                    for (int j = 0; j < module_datas.length(); j++) {
                        JSONObject module = module_datas.getJSONObject(j);
                        AdIcon info = new AdIcon();
                        info.module_weight = module_weight;
                        info.title = module.getString("title");
                        info.img_url = module.getString("img");
                        info.target = module.getString("target");
                        info.weight = module.getInt("weight");
                        info.id = info.target_url = module.getString("targetid");
                        if (info.target.equals("page") && info.target_url.equals("0")) {
                            info.target = "category";
                        }
                        try {
                            info.target_name = module.getString("desc");
                        } catch (JSONException e) {
                        }
                        adicons.adicons.add(info);
                        DataPool.getInstance().addAdIcon(DataPool.TYPE_ADICON, info);
                    }
                    DataPool.getInstance().addModule(datatype, adicons);
                } else if ("app".equals(data.getString("module"))) {
                    int module_weight = data.getInt("weight");
                    JSONObject module_data = data.getJSONObject("data");
                    AppInfo info = new AppInfo();
                    parseAppInfoElement(module_data, info);
                    info.module_weight = module_weight;
                    info.weight = module_weight;
                    DataPool.getInstance().addAppInfo(DataPool.TYPE_APP_LIST, info);
                    DataPool.getInstance().addModule(datatype, info);
                } else if ("label".equals(data.getString("module"))) {
                    int module_weight = data.getInt("weight");
                    JSONObject module_data = data.getJSONObject("data");
                    Label info = new Label();
                    info.module_weight = module_weight;
                    info.id = module_data.getString("id");
                    info.img_url = module_data.getString("img");
                    info.title = module_data.getString("title");
                    JSONArray apps = module_data.getJSONArray("apps");
                    for (int k = 0; k < apps.length(); k++) {
                        JSONObject app = apps.getJSONObject(k);
                        AppInfo appInfo = new AppInfo();
                        parseAppInfoElement(app, appInfo);
                        appInfo.module_weight = module_weight;
                        info.apps.add(appInfo);
                        DataPool.getInstance().addAppInfo(DataPool.TYPE_APP_LABEL, appInfo);
                    }
                    DataPool.getInstance().addModule(datatype, info);
                } else if ("rank".equals(data.getString("module"))) {
                    int module_weight = data.getInt("weight");
                    JSONObject module_data = data.getJSONObject("data");
                    Rank info = new Rank();
                    info.module_weight = module_weight;
                    info.id = module_data.getString("id");
                    info.img_url = module_data.getString("img");
                    info.title = module_data.getString("title");
                    JSONArray apps = module_data.getJSONArray("apps");
                    for (int k = 0; k < apps.length(); k++) {
                        JSONObject app = apps.getJSONObject(k);
                        AppInfo appInfo = new AppInfo();
                        parseAppInfoElement(app, appInfo);
                        appInfo.module_weight = module_weight;
                        info.apps.add(appInfo);
                        DataPool.getInstance().addAppInfo(DataPool.TYPE_APP_RANK, appInfo);
                    }
                    DataPool.getInstance().addModule(datatype, info);
                }
            }
            return "" + true;
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        }
    }
    
    public String parseAppDetail(String t) {
        try {
            JSONObject mainJson = new JSONObject(t);
            boolean result = mainJson.getBoolean("result");
            if (!result) {
                return mainJson.getString("msg");
            }
            JSONObject detail = mainJson.getJSONObject("data");
            AppInfo info = DataPool.getInstance().getAppInfo(detail.getString("pkg"));
            if (info == null) {
                info = new AppInfo();
            }
            parseAppInfoElement(detail, info);
            info.desc = detail.getString("desc");
            info.src = detail.getString("src");
            
            JSONArray previews = detail.getJSONArray("images");
            info.thumbnail_urls.clear();
            for (int j = 0; j < previews.length(); j++) {
                JSONObject preview = previews.getJSONObject(j);
                String url = preview.getString("url");
                info.thumbnail_urls.add(url);
            }
            Collections.sort(info.thumbnail_urls, new Comparator<String>() {
                @Override
                public int compare(String lhs, String rhs) {
                    return lhs.compareTo(rhs);
                }
            });
            DataPool.getInstance().addAppInfo(DataPool.TYPE_APP_OTHER, info);
            return "" + true;
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        }
    }
    
    public String parseLabel(String t, int dataType) {
        try {
            JSONObject mainJson = new JSONObject(t);
            boolean result = mainJson.getBoolean("result");
            if (!result) {
                return mainJson.getString("msg");
            }
            JSONObject data = mainJson.getJSONObject("data");
            Label info = new Label();
            info.id = data.getString("id");
            info.img_url = data.getString("img");
            info.title = data.getString("title");
            info.adcontent = data.getString("adcontent");
            info.adurl = data.getString("adurl");
            JSONArray apps = data.getJSONArray("apps");
            for (int k = 0; k < apps.length(); k++) {
                JSONObject app = apps.getJSONObject(k);
                AppInfo appInfo = new AppInfo();
                parseAppInfoElement(app, appInfo);
                info.apps.add(appInfo);
            }
            DataPool.getInstance().addLabel(dataType, info);
            return "" + true;
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        }
    }
    
    public String parseRank(String t, int dataType) {
        try {
            JSONObject mainJson = new JSONObject(t);
            boolean result = mainJson.getBoolean("result");
            if (!result) {
                return mainJson.getString("msg");
            }
            JSONObject data = mainJson.getJSONObject("data");
            Rank info = new Rank();
            info.id = data.getString("id");
            info.img_url = data.getString("img");
            info.title = data.getString("title");
            info.adcontent = data.getString("adcontent");
            info.adurl = data.getString("adurl");
            JSONArray apps = data.getJSONArray("apps");
            for (int k = 0; k < apps.length(); k++) {
                JSONObject app = apps.getJSONObject(k);
                AppInfo appInfo = new AppInfo();
                parseAppInfoElement(app, appInfo);
                info.apps.add(appInfo);
            }
            DataPool.getInstance().addRank(dataType, info);
            return "" + true;
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        }
    }
    
    public String parseType(String t, int dataType, ArrayList<AppInfo> infos) {
        try {
            JSONObject mainJson = new JSONObject(t);
            boolean result = mainJson.getBoolean("result");
            if (!result) {
                return mainJson.getString("msg");
            }
            JSONArray apps = mainJson.getJSONArray("data");
            for (int i = 0; i < apps.length(); i++) {
                JSONObject app = apps.getJSONObject(i);
                AppInfo info = new AppInfo();
                parseAppInfoElement(app, info);
                infos.add(info);
                DataPool.getInstance().addAppInfo(dataType, info);
            }
            return "" + true;
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        }
    }
    
    public String parseAppUpgrade(String t) {
        DataPool.getInstance().clearAppInfos(DataPool.TYPE_APP_NEED_UPGRADE);
        try {
            JSONObject mainJson = new JSONObject(t);
            boolean result = mainJson.getBoolean("result");
            if (!result) {
                return mainJson.getString("msg");
            }
            JSONArray apps = mainJson.getJSONArray("data");
            for (int i = 0; i < apps.length(); i++) {
                JSONObject app = apps.getJSONObject(i);
                AppInfo info = new AppInfo();
                parseAppInfoElement(app, info);
                PackageManager pm = AppStoreWrapperImpl.getInstance().getAppContext().getPackageManager();
                PackageInfo p = null;
                try {
                    p = pm.getPackageInfo(info.pkg, PackageManager.GET_ACTIVITIES);
                } catch (Exception e) {
                    LogEx.w(info.pkg + " getPackageInfo exception:" + e.getMessage());
                }
                if (p != null && (info.vercode > p.versionCode /*|| (info.islocal && info.vercode >= p.versionCode)*/)) {
                    DataPool.getInstance().addAppInfo(DataPool.TYPE_APP_NEED_UPGRADE, info);
                    DataPool.getInstance().setNeedUpgradeFlag(info);
                } else {
                    DataPool.getInstance().addAppInfo(DataPool.TYPE_APP_OTHER, info);
                }
            }
            return "" + true;
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        }
    }
    
    public String parseRelated(String t, int relatedType) {
        DataPool.getInstance().clearAppInfos(DataPool.TYPE_APP_RELATED_SIMILAR + relatedType);
        try {
            JSONObject mainJson = new JSONObject(t);
            boolean result = mainJson.getBoolean("result");
            if (!result) {
                return mainJson.getString("msg");
            }
            JSONArray apps = mainJson.getJSONArray("data");
            for (int i = 0; i < apps.length(); i++) {
                JSONObject app = apps.getJSONObject(i);
                AppInfo info = new AppInfo();
                parseAppInfoElement(app, info);
                DataPool.getInstance().addAppInfo(DataPool.TYPE_APP_RELATED_SIMILAR + relatedType, info);
            }
            return "" + true;
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        }
    }
    
    public String parseSearchResult(String t) {
        DataPool.getInstance().clearAppInfos(DataPool.TYPE_APP_SEARCH_RESULT);
        try {
            JSONObject mainJson = new JSONObject(t);
            boolean result = mainJson.getBoolean("result");
            if (!result) {
                return mainJson.getString("msg");
            }
            JSONArray apps = mainJson.getJSONArray("data");
            for (int i = 0; i < apps.length(); i++) {
                JSONObject app = apps.getJSONObject(i);
                AppInfo info = new AppInfo();
                parseAppInfoElement(app, info);
                DataPool.getInstance().addAppInfo(DataPool.TYPE_APP_SEARCH_RESULT, info);
            }
            return "" + true;
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        }
    }
    
    public String parseAd(String t, boolean parseWelcome, boolean parseNotification) {
        if (parseWelcome) {
            DataPool.getInstance().clearWelcomes();
        }
        if (parseNotification) {
            DataPool.getInstance().clearNotifications();
        }
        try {
            JSONObject mainJson = new JSONObject(t);
            boolean result = mainJson.getBoolean("result");
            if (!result) {
                return mainJson.getString("msg");
            }
            JSONArray datas = mainJson.getJSONArray("data");
            for (int i = 0; i < datas.length(); i ++) {
                JSONObject data = datas.getJSONObject(i);
                if (data.getInt("adflag") == 0 && parseWelcome) {
                    final Welcome welcome = new Welcome();
                    welcome.title = data.getString("adtitle");
                    welcome.desc = data.getString("addesc");
                    welcome.imgurl = data.getString("imgurl");
                    welcome.valid_date_start = data.getString("valid_date_start");
                    welcome.valid_date_end = data.getString("valid_date_end");
                    welcome.show_time_start = data.getString("show_time_start");
                    welcome.show_time_end = data.getString("show_time_end");
                    welcome.timeout = data.getInt("timeout");
                    welcome.event = data.getInt("event");
                    JSONObject event_data = new JSONObject(data.getString("event_data"));
                    if (welcome.event == 1) {
                        welcome.event_data.url = event_data.getString("url");
                    } else if (welcome.event == 2) {
                        welcome.event_data.packagename = event_data.getString("packagename");
                        welcome.event_data.versionCode = event_data.getInt("version");
                        welcome.event_data.intent = event_data.getString("intent");
                        welcome.event_data.apk_url = event_data.getString("apk_url");
                    } else if (welcome.event == 3) {
                        welcome.event_data.type = event_data.getString("type");
                        welcome.event_data.type_id = event_data.getString("id");
                    }
                    welcome.template = data.getString("template");
                    DataPool.getInstance().addWelcome(welcome, true);
                } else if (data.getInt("adflag") == 1 & parseNotification) {
                    final NotificationInfo notification = new NotificationInfo();
                    notification.title = data.getString("adtitle");
                    notification.desc = data.getString("addesc");
                    notification.imgurl = data.getString("imgurl");
                    if (notification.imgurl != null && !"".equals(notification.imgurl)) {
                        BitmapUtil.getInstance().getBitmapAsync(notification.imgurl, new ImageLoadCallBack() {

                            @Override
                            public boolean isNeedToDecode(String imageUrl) {
                                return true;
                            }

                            @Override
                            public void onImageLoadSuccess(String imageUrl, Bitmap bitmap) {
                                DataPool.getInstance().setNotificationBitmap(notification, null, bitmap);
                            }

                            @Override
                            public void onImageLoadFailed(String imageUrl, String reason) {
                            }

                            @Override
                            public String getCaller() {
                                return "notification_img";
                            }
                            
                        });
                    }
                    notification.iconurl = data.getString("adicon");
                    if (notification.iconurl != null && !"".equals(notification.iconurl)) {
                        BitmapUtil.getInstance().getBitmapAsync(notification.iconurl, new ImageLoadCallBack() {

                            @Override
                            public boolean isNeedToDecode(String imageUrl) {
                                return true;
                            }

                            @Override
                            public void onImageLoadSuccess(String imageUrl, Bitmap bitmap) {
                                DataPool.getInstance().setNotificationBitmap(notification, bitmap, null);
                            }

                            @Override
                            public void onImageLoadFailed(String imageUrl, String reason) {
                                
                            }

                            @Override
                            public String getCaller() {
                                return "notification_icon";
                            }
                            
                        });
                    }
                    notification.delaytime = data.getLong("delay");
                    notification.h5type = data.getInt("h5type");
                    try {
                        notification.intenturi = data.getString("intent");
                    } catch (JSONException e) {
                    }
                    notification.valid_date_start = data.getString("valid_date_start");
                    notification.valid_date_end = data.getString("valid_date_end");
                    notification.show_time_start = data.getString("show_time_start");
                    notification.show_time_end = data.getString("show_time_end");
                    notification.timeout = data.getInt("timeout");
                    notification.event = data.getInt("event");
                    JSONObject event_data = new JSONObject(data.getString("event_data"));
                    if (notification.event == 1) {
                        notification.event_data.url = event_data.getString("url");
                    } else if (notification.event == 2) {
                        notification.event_data.packagename = event_data.getString("packagename");
                        notification.event_data.versionCode = event_data.getInt("version");
                        notification.event_data.intent = event_data.getString("intent");
                        notification.event_data.apk_url = event_data.getString("apk_url");
                    } else if (notification.event == 3) {
                        notification.event_data.type = event_data.getString("type");
                        notification.event_data.type_id = event_data.getString("id");
                    }
                    notification.template = data.getString("template");
                    DataPool.getInstance().addNotification(notification, true);
                }
            }
            return "" + true;
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        }
    }
    
    public String parseRandomBanners(String t) {
        DataPool.getInstance().clearBanners(DataPool.TYPE_BANNER_RANDOM);
        try {
            JSONObject mainJson = new JSONObject(t);
            boolean result = mainJson.getBoolean("result");
            if (!result) {
                return mainJson.getString("msg");
            }
            JSONArray datas = mainJson.getJSONArray("data");
            for (int i = 0; i < datas.length(); i++) {
                JSONObject data = datas.getJSONObject(i);
                Banner info = parseBannerElement(data, true);
                DataPool.getInstance().addBanner(DataPool.TYPE_BANNER_RANDOM, info);
            }
            return "" + true;
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        }
    }
    
    public String parseRandomApps(String t) {
        Log.d("zr","parseRandomApps="+t);
        DataPool.getInstance().clearAppInfos(DataPool.TYPE_APP_RANDOM_APPS);
        try {
            JSONObject mainJson = new JSONObject(t);
            boolean result = mainJson.getBoolean("result");
            if (!result) {
                return mainJson.getString("msg");
            }
            JSONArray apps = mainJson.getJSONArray("data");
            for (int i = 0; i < apps.length(); i++) {
                JSONObject app = apps.getJSONObject(i);
                AppInfo info = new AppInfo();
                parseAppInfoElement(app, info);
                DataPool.getInstance().addAppInfo(DataPool.TYPE_APP_RANDOM_APPS, info);
            }
            return "" + true;
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        }
    }

    public String parseDownloadUrl(String t, AppInfo appInfo) {
        try {
            JSONObject mainJson = new JSONObject(t);
            boolean result = mainJson.getBoolean("result");
            if (!result) {
                return mainJson.getString("msg");
            }
            JSONObject data = mainJson.getJSONObject("data");
            String downloadInfoBase64 = data.getString("d");
            byte[] downloadInfoBytes = Base64.decode(downloadInfoBase64, Base64.DEFAULT);
            JSONObject downloadInfo = new JSONObject(new String(downloadInfoBytes, "UTF-8"));
            String bill_dl_url = downloadInfo.getString("dlurl");
            if (bill_dl_url != null && bill_dl_url.length() > 0) {
                appInfo.file_url = bill_dl_url;
                JSONArray dlevents = downloadInfo.getJSONArray("dlevents");
                appInfo.bill_report_start_download.clear();
                appInfo.bill_report_downloaded.clear();
                appInfo.bill_report_installed.clear();
                appInfo.bill_report_first_launch.clear();
                for (int i = 0; i < dlevents.length(); i++) {
                    JSONObject dlevent = dlevents.getJSONObject(i);
                    if (dlevent.getInt("type") == 0) {
                        appInfo.bill_report_start_download.add(dlevent.getString("url"));
                    } else if (dlevent.getInt("type") == 1) {
                        appInfo.bill_report_downloaded.add(dlevent.getString("url"));
                    } else if (dlevent.getInt("type") == 2) {
                        appInfo.bill_report_installed.add(dlevent.getString("url"));
                    } else if (dlevent.getInt("type") == 3) {
                        appInfo.bill_report_first_launch.add(dlevent.getString("url"));
                    }
                }
                DataPool.getInstance().updateAppInfo(appInfo);
                return "" + true;
            }
            String local_dl_url = downloadInfo.getString("localurl");
            if (local_dl_url != null && local_dl_url.length() > 0) {
                appInfo.file_url = local_dl_url;
                JSONArray dlevents = downloadInfo.getJSONArray("localevents");
                for (int i = 0; i < dlevents.length(); i++) {
                    JSONObject dlevent = dlevents.getJSONObject(i);
                    if (dlevent.getInt("type") == 0) {
                        appInfo.bill_report_start_download.add(dlevent.getString("url"));
                    } else if (dlevent.getInt("type") == 1) {
                        appInfo.bill_report_downloaded.add(dlevent.getString("url"));
                    } else if (dlevent.getInt("type") == 2) {
                        appInfo.bill_report_installed.add(dlevent.getString("url"));
                    } else if (dlevent.getInt("type") == 3) {
                        appInfo.bill_report_first_launch.add(dlevent.getString("url"));
                    }
                }
                DataPool.getInstance().updateAppInfo(appInfo);
                return "" + true;
            }
            DataPool.getInstance().updateAppInfo(appInfo);
            return "" + true;
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        }
    }
}
