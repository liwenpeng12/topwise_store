package com.topwise.topos.appstore.manager;

import android.graphics.Color;

//import com.ak.firm.res.AppReqBuilder;
//import com.ak.firm.res.AppResListener;
//import com.ak.firm.shell.FirmSdk;
import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.data.AppInfo;
import com.topwise.topos.appstore.data.DataPool;

import org.json.JSONArray;
import org.json.JSONObject;

public class FirmSdkManager extends BaseManager {

    private static FirmSdkManager mThis = null;

    public static FirmSdkManager getInstance() {
        if (mThis == null) {
            synchronized (FirmSdkManager.class) {
                if (mThis == null) {
                    mThis = new FirmSdkManager();
                }
            }
        }
        return mThis;
    }

    public void search(String keyword, final ManagerCallback callback) {
        /*
        AppReqBuilder reqBuilder = new AppReqBuilder("test", "110123", "aF5QjCWBqc", 10);
        reqBuilder.setSearchWord(keyword);
        reqBuilder.setPage(1);
        FirmSdk.requestAppRes(AppStoreWrapperImpl.getInstance().getAppContext(), reqBuilder, new AppResListener() {
            @Override
            public void onAppResSuccess(final JSONObject jsonObject) {
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        String res = parseSearchResult(jsonObject);
                        if (callback == null) {
                            return;
                        }
                        if ("true".equals(res)) {
                            callback.onSuccess(Properties.MODULE_TYPE_APP, DataPool.TYPE_APP_SEARCH_RESULT, 0, 0, true);
                        } else {
                            callback.onFailure(Properties.MODULE_TYPE_APP, DataPool.TYPE_APP_SEARCH_RESULT, null, -1, res);
                        }
                    }
                });
            }

            @Override
            public void onAppResFailed(final int i, final String s) {

            }
        });
        */
    }

    private String parseSearchResult(JSONObject jsonObject) {
        try {
            JSONArray apps = jsonObject.getJSONArray("data");
            for (int i = 0; i < apps.length(); i++) {
                JSONObject app = apps.getJSONObject(i);
                AppInfo info = new AppInfo();
                info.id = info.pkg = app.getString("apkid");
                info.bindId = app.getString("bindid");
                info.name = app.getString("name") + "[推广]";
                info.vername = app.getString("version_name");
                info.vercode = app.getInt("version_code");
                info.minSdkVersion = app.getInt("os_version");
                info.size = app.getString("size");
                info.signature_md5 = app.getString("signature_md5");
                info.downloads = app.getString("download_times");
                AppInfo.Tag tag = new AppInfo.Tag();
                tag.name = app.getString("category");
                tag.bgcolor = AppStoreWrapperImpl.getInstance().getAppContext().getResources().getColor(R.color.zkas_sec_free_tag_color);
                tag.txtcolor = Color.WHITE;
                info.tags.add(tag);
                info.icon_url = app.getString("logo_url");
                info.file_url = app.getString("down_url");
                DataPool.getInstance().addAppInfo(DataPool.TYPE_APP_SEARCH_RESULT, info);
            }
            return "" + true;
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        }
    }
}
