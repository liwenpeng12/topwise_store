
package com.topwise.topos.appstore.view.fragment;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.data.AppInfo;
import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.manager.AppManager;
import com.topwise.topos.appstore.manager.ManagerCallback;
import com.topwise.topos.appstore.manager.NetworkManager;
import com.topwise.topos.appstore.view.ListPopularItemView;
import com.umeng.analytics.MobclickAgent;

public class AppRelatedFragment extends MainListFragment {
    private AppInfo mAppInfo;
    private ArrayList<AppInfo> mPeopleDownloadList = new ArrayList<AppInfo>();
    private ArrayList<AppInfo> mHotList = new ArrayList<AppInfo>();
    private Context mContext;
    private View mTopShade;

    public AppRelatedFragment() {
    }

    public void setAppInfo(AppInfo appInfo){
        mAppInfo = appInfo;

        mManagerCallback = new ManagerCallback() {

            @Override
            public void onSuccess(String moduleType, int dataType, int page, int num, boolean end) {
                if (isDestroyed()) {
                    return;
                }
                if (dataType == DataPool.TYPE_APP_RELATED_PEOPLE_DOWNLOADING) {
                    if (mPeopleDownloadList == null) {
                        return;
                    }
                    mPeopleDownloadList.clear();
                    mPeopleDownloadList = DataPool.getInstance().getAppInfos(dataType);
                    ((RelatedMainListAdapter) mAdapter).addDataToList(mPeopleDownloadList, dataType, mContext.getString(R.string.as_listitem_people_download_title));
                    if (end) {
                        onNoMore();
                    } else {
                        onGetMoreCompleted();
                    }

                    AppManager.getInstance().loadRelated(mAppInfo, 3, mManagerCallback);
                } else if (dataType == DataPool.TYPE_APP_RELATED_HOT) {
                    if (mHotList == null) {
                        return;
                    }
                    mHotList.clear();
                    mHotList = DataPool.getInstance().getAppInfos(dataType);
                    ((RelatedMainListAdapter) mAdapter).addDataToList(mHotList, dataType, mContext.getString(R.string.as_listitem_hot_title));
                    ((RelatedMainListAdapter) mAdapter).setDataToList();
                    hideWaitView();
                    if (end) {
                        onNoMore();
                    } else {
                        onGetMoreCompleted();
                    }
                }
            }

            @Override
            public void onFailure(String moduleType, int dataType, Throwable t, int errorNo, String strMsg) {
                if ((dataType == DataPool.TYPE_APP_RELATED_PEOPLE_DOWNLOADING
                  || dataType == DataPool.TYPE_APP_RELATED_HOT)
                  && !isDestroyed()) {
                    if (mFirstLoading) {
                        if (NetworkManager.getInstance().isNetworkAvailable()) {
                            showWaitViewRefushBtn(R.string.as_list_load_failed_prompt,
                                R.string.as_refresh_btn_again, mRefushBtnListener);
                        } else {
                            showWaitViewPrompt(R.string.as_network_unavailable);
                        }
                    } else {
                        onGetMoreFailed();
                    }
                }
            }
        };
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = this.getActivity();
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mTopShade =  v.findViewById(R.id.zkas_item_top_shade);
        mTopShade.setVisibility(View.VISIBLE);
        return v;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("AppRelatedFragment");
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("AppRelatedFragment"); 
    }

    @Override
    protected void loadData() {
        AppManager.getInstance().loadRelated(mAppInfo, 2, mManagerCallback);
        onNoMore();
    }

    @Override
    protected Object getDatas() {
        ArrayList<AppInfo> infoList = DataPool.getInstance().getAppInfos(getDataType());
        return infoList;
    }

    @Override
    protected void setDataToList() {

    }
    
    @Override
    public void setFrom(String from) {
    }

    @Override
    protected ListAdapter getListAdapter() {
        if (mAdapter == null) {
            mAdapter = new RelatedMainListAdapter();
            ((RelatedMainListAdapter) mAdapter).setOnAdIconClickListener(this);
            ((RelatedMainListAdapter) mAdapter).setOnAppItemClickListener(this);
            ((RelatedMainListAdapter) mAdapter).setOnBannerClickListener(this);
            ((RelatedMainListAdapter) mAdapter).setOnMoreItemClickListener(this);
            ((RelatedMainListAdapter) mAdapter).setOnTopAdItemClickListener(this);
        }
        return mAdapter;
    }

    public static class RelatedMainListAdapter extends MainListAdapter {
        private ArrayList<DataWapper> mPeopelDownloadDatas;
        private ArrayList<DataWapper> mHotDatas;

        public void addDataToList(ArrayList<AppInfo> datalist, int type, String title) {
            if (type == DataPool.TYPE_APP_RELATED_PEOPLE_DOWNLOADING) {
                if (mPeopelDownloadDatas == null) {
                    mPeopelDownloadDatas = new ArrayList<DataWapper>();
                } else {
                    mPeopelDownloadDatas.clear();
                }

                if (datalist == null || datalist.size() == 0) {
                    return;
                }
                DataWapper t = new DataWapper();
                if (title != null && title.length() > 0) {
                    // 加title
                    t.mDataType = DataWapper.TYPE_TITLE_ITEM;
                    t.mRealData = title;
                    mPeopelDownloadDatas.add(t);
                    
                    for (AppInfo info : datalist) {
                        info.from = title;
                    }
                }

                t = new DataWapper();
                t.mDataType = DataWapper.TYPE_RANK_ITEM;
                t.mRealData = datalist;
                mPeopelDownloadDatas.add(t);
            } else if (type == DataPool.TYPE_APP_RELATED_HOT) {
                if (mHotDatas == null) {
                    mHotDatas = new ArrayList<DataWapper>();
                } else {
                    mHotDatas.clear();
                }

                if (datalist == null || datalist.size() == 0) {
                    return;
                }

                DataWapper t = new DataWapper();
                if (title != null && title.length() > 0) {
                    // 加title
                    t.mDataType = DataWapper.TYPE_TITLE_ITEM;
                    t.mRealData = title;
                    mHotDatas.add(t);
                    
                    for (AppInfo info : datalist) {
                        info.from = title;
                    }
                }

                t = new DataWapper();
                t.mDataType = DataWapper.TYPE_RANK_ITEM;
                t.mRealData = datalist;
                mHotDatas.add(t);
            }
        }

        public void setDataToList() {
            if (mDatas == null) {
                mDatas = new ArrayList<DataWapper>();
            } else {
                mDatas.clear();
            }
            if ((mPeopelDownloadDatas == null || mPeopelDownloadDatas.size() == 0)
                    && (mHotDatas == null || mHotDatas.size() == 0)) {
                notifyDataSetChanged();
                return;
            }

            if (mPeopelDownloadDatas != null && mPeopelDownloadDatas.size() != 0) {
                mDatas.addAll(mPeopelDownloadDatas);
                
                if (mHotDatas != null && mHotDatas.size() != 0) {
                    DataWapper s = new DataWapper();
                    s.mDataType = DataWapper.TYPE_DIVIDER_ITEM;
                    s.mRealData = null;
                    mDatas.add(s);
                }
            }
            
            if (mHotDatas != null && mHotDatas.size() != 0) {
                mDatas.addAll(mHotDatas);
            }
            notifyDataSetChanged();
        }
        
        public void updateViewData(View v, int type, Object data) {
            super.updateViewData(v, type, data);
            if (type == DataWapper.TYPE_RANK_ITEM) {
                try {
                    ListPopularItemView view = (ListPopularItemView) v;
                    view.setFrom(((ArrayList<AppInfo>)data).get(0).from);
                } catch (Exception e) {
                }
            }
        }

        @Override
        public void onDestroy() {
            if (mPeopelDownloadDatas != null) {
                mPeopelDownloadDatas.clear();
                mPeopelDownloadDatas = null;
            }
            if (mHotDatas != null) {
                mHotDatas.clear();
                mHotDatas = null;
            }
            super.onDestroy();
        }
    }
}
