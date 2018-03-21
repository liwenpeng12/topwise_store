package com.topwise.topos.appstore.manager;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.topwise.topos.appstore.conn.http.AjaxCallBack;
import com.topwise.topos.appstore.conn.protocol.Protocol;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.utils.Properties;
import com.topwise.topos.appstore.utils.SPEngine;

public class PageModuleManager extends BaseManager {

    private static PageModuleManager mThis = null;

    private int mFailCount = 0;

    public static PageModuleManager getInstance() {
        if (mThis == null) {
            synchronized (PageModuleManager.class) {
                if (mThis == null) {
                    mThis = new PageModuleManager();
                }
            }
        }
        return mThis;
    }

    /**
     * 页面
     *
     * @param pageId   页面编码，1-推荐，2-游戏，3-应用
     * @param callback 回调
     */
    public void loadPage(final int pageId, final ManagerCallback callback) {
        final int datatype = pageId; // page module的datatype等于page id
        HttpManager.getInstance().post(Protocol.getInstance().getPageUrl(pageId), new AjaxCallBack<String>() {

            @Override
            public void onSuccess(final String t) {
                LogEx.d(t);
                mMainThreadHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        String res = Protocol.getInstance().parsePage(t, pageId);
                        if (callback == null) {
                            return;
                        }
                        if ("true".equals(res)) {
                            savePageInfo(t, pageId);
                            callback.onSuccess(Properties.MODULE_TYPE_PAGE, datatype, 0, 0, true);
                        } else {
                            callback.onFailure(Properties.MODULE_TYPE_PAGE, datatype, null, -1, res);
                        }
                    }

                });
                super.onSuccess(t);
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onFailure(final Throwable t, final int errorNo, final String strMsg) {
                LogEx.e("onFailure,Throwable:" + t + ",errorNo:" + errorNo + ",strMsg:" + strMsg);

                if (t.getClass().equals(org.apache.http.conn.ConnectTimeoutException.class)
                        || t.getClass().equals(org.apache.http.client.HttpResponseException.class)
                        || t.getClass().equals(org.apache.http.conn.ConnectionPoolTimeoutException.class)) {
                    mFailCount++;
                    if (mFailCount > 3) {
                        Protocol.getInstance().changeServer();
                        mFailCount = 0;
                    }
                }

//                mMainThreadHandler.post(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        if (callback == null) {
//                            return;
//                        }
//                        callback.onFailure(Properties.MODULE_TYPE_PAGE, datatype, t, errorNo, strMsg);
//                    }
//
//                });
                //先去文件缓存中取一次数据
                loadPageInfoFromFileCache(callback, Properties.MODULE_TYPE_PAGE, datatype, t, errorNo, strMsg);


                super.onFailure(t, errorNo, strMsg);
            }

        });
    }

    public void savePageInfo(final String t, final int pageId) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                SPEngine.getSPEngine().setPageInfo(t, "" + pageId);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
            }
        }.execute();
    }

    public void loadPageInfoFromFileCache(final ManagerCallback callback, final String moduleType, final int dataType, final Throwable t, final int errorNo, final String strMsg) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                String t = SPEngine.getSPEngine().getPageInfo("" + dataType);
                if (TextUtils.isEmpty(t)) {
                    return false;
                }
                String res = Protocol.getInstance().parsePage(t, dataType);
                if ("true".equals(res)) {
                    return true;
                }
                return false;
            }

            @Override
            protected void onPostExecute(final Boolean result) {
                super.onPostExecute(result);
                if (null != callback) {
                    mMainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (result) {
                                callback.onSuccess(Properties.MODULE_TYPE_PAGE, dataType, 0, 0, true);
                            } else {
                                callback.onFailure(Properties.MODULE_TYPE_PAGE, dataType, t, errorNo, strMsg);
                            }
                        }

                    });
                }
            }
        }.execute();
    }

}
