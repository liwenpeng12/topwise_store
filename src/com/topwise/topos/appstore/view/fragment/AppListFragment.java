package com.topwise.topos.appstore.view.fragment;

import java.util.ArrayList;

import com.topwise.topos.appstore.data.AppInfo;
import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.manager.AppManager;
import com.umeng.analytics.MobclickAgent;

public class AppListFragment extends MainListFragment {

    private int mTypeId;
    public void setType(int id) {
        mTypeId = id;
    }
    
    @Override
    protected void loadData() {
        AppManager.getInstance().loadType(mTypeId, mManagerCallback);
    }

    @Override
    protected Object getDatas() {
        ArrayList<AppInfo> apps = DataPool.getInstance().getAppInfos(getDataType());
        return apps;
    }
    
    
    @Override
    protected void setDataToList() {
        ((MainListAdapter)mAdapter).setAppDatas((ArrayList<AppInfo>)getDatas());
    }
    
    @Override
    public void onResume() {
        super.onResume();
    //    MobclickAgent.onPageStart("AppListFragment");
    }

    @Override
    public void onPause() {
        super.onPause();
   //     MobclickAgent.onPageEnd("AppListFragment");
    }
}
