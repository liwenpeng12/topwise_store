package com.topwise.topos.appstore.manager;

import com.topwise.topos.appstore.conn.http.AjaxCallBack;
import com.topwise.topos.appstore.conn.protocol.Protocol;
import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.database.SharedPreferencesCenter;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.utils.Properties;

import org.json.JSONArray;

import java.util.ArrayList;

public class SearchManager extends BaseManager {

    private static SearchManager mThis = null;

    private static final int MAX_HISTORY_WORD = 3;
    
    public static SearchManager getInstance() {
        if (mThis == null) {
            synchronized (SearchManager.class) {
                if (mThis == null) {
                    mThis = new SearchManager();
                }
            }
        }
        return mThis;
    }
    
    /**
     * 搜索热词
     * @param callback 回调
     */
    public void loadHotwords(final ManagerCallback callback) {
        DataPool.getInstance().clearSearchHotwords();
        HttpManager.getInstance().post(Protocol.getInstance().getSearchHotwordUrl(), new AjaxCallBack<String>() {
            @Override
            public void onSuccess(final String t) {
                LogEx.d(t);
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        String res = Protocol.getInstance().parseSearchHotword(t);
                        if (callback == null) {
                            return;
                        }
                        if ("true".equals(res)) {
                            callback.onSuccess(Properties.MODULE_TYPE_HOTWORD, DataPool.TYPE_SEARCH_HOTWORD, 0, 0, true);
                        } else {
                            callback.onFailure(Properties.MODULE_TYPE_HOTWORD, DataPool.TYPE_SEARCH_HOTWORD, null, -1, res);
                        }
                    }
                });
                super.onSuccess(t);
            }

            @Override
            public void onFailure(final Throwable t, final int errorNo, final String strMsg) {
                LogEx.e("onFailure,Throwable:" + t + ",errorNo:" + errorNo + ",strMsg:" + strMsg);
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback == null) {
                            return;
                        }
                        callback.onFailure(Properties.MODULE_TYPE_HOTWORD, DataPool.TYPE_SEARCH_HOTWORD, t, errorNo, strMsg);
                    }
                });
                super.onFailure(t, errorNo, strMsg);
            }
            
        });
    }
    
    /**
     * 搜索
     * @param word 搜索词
     * @param callback 回调
     */
    public void search(final String word, final ManagerCallback callback) {
        HttpManager.getInstance().post(Protocol.getInstance().getSearchUrl(word), new AjaxCallBack<String>() {
            @Override
            public void onSuccess(final String t) {
                LogEx.d(t);
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        String res = Protocol.getInstance().parseSearchResult(t);
                        if (callback == null) {
                            return;
                        }
                        if ("true".equals(res)) {
                            callback.onSuccess(Properties.MODULE_TYPE_APP, DataPool.TYPE_APP_SEARCH_RESULT, 0, 0, true);
                        } else {
                            callback.onFailure(Properties.MODULE_TYPE_APP, DataPool.TYPE_APP_SEARCH_RESULT, null, -1, res);
                        }
//                        FirmSdkManager.getInstance().search(word, callback);
                    }
                });
                super.onSuccess(t);
            }

            @Override
            public void onFailure(final Throwable t, final int errorNo, final String strMsg) {
                LogEx.e("onFailure,Throwable:" + t + ",errorNo:" + errorNo + ",strMsg:" + strMsg);
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback == null) {
                            return;
                        }
                        callback.onFailure(Properties.MODULE_TYPE_APP, DataPool.TYPE_APP_SEARCH_RESULT, t, errorNo, strMsg);
//                        FirmSdkManager.getInstance().search(word, callback);
                    }
                });
                super.onFailure(t, errorNo, strMsg);
            }
        });
    }

    public synchronized ArrayList<String> loadSearchHistory() {
        ArrayList<String> result = new ArrayList<String>();
        try {
            String historyString = SharedPreferencesCenter.getInstance().getSharedPreferences().getString("search_history", "");
            JSONArray array = new JSONArray(historyString);
            for (int i = 0; i < array.length(); i++) {
                result.add(i, array.get(i).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void addSearchHistory(String keyword) {
        if (keyword == null || keyword.length() == 0) {
            return;
        }
        ArrayList<String> keywords = loadSearchHistory();
        if (keywords.contains(keyword)) {
            return;
        }
        keywords.add(0, keyword);
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < (keywords.size() > MAX_HISTORY_WORD ? MAX_HISTORY_WORD : keywords.size()); i++) {
            jsonArray.put(keywords.get(i));
        }
        SharedPreferencesCenter.getInstance().getSharedPreferences().edit().putString("search_history", jsonArray.toString()).commit();
    }

    public void deleteHistory(String keyword) {
        ArrayList<String> keywords = loadSearchHistory();
        if (keywords.contains(keyword)) {
            keywords.remove(keyword);
        }
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < keywords.size(); i++) {
            jsonArray.put(keywords.get(i));
        }
        SharedPreferencesCenter.getInstance().getSharedPreferences().edit().putString("search_history", jsonArray.toString()).commit();
    }

    public void clearHistory() {
        SharedPreferencesCenter.getInstance().getSharedPreferences().edit().putString("search_history", "").commit();
    }
}
