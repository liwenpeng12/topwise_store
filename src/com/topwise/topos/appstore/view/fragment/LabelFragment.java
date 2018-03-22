package com.topwise.topos.appstore.view.fragment;


import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.data.Label;
import com.topwise.topos.appstore.manager.AppManager;
import com.umeng.analytics.MobclickAgent;

public class LabelFragment extends MainListFragment {
    private int mLabelId;
    public void setLabelId(int id) {
        mLabelId = id;
    }
    
    @Override
    protected void loadData() {
        AppManager.getInstance().loadLabel(mLabelId, mManagerCallback);
        onNoMore();
    }

    @Override
    protected Object getDatas() {
        Label label = DataPool.getInstance().getLabel(getDataType(), "" + mLabelId);
        return label;
    }
    
    @Override
    protected void setDataToList() {
        ((MainListAdapter)mAdapter).setLabelData((Label)getDatas());
    }
    
    @Override
    public void onResume() {
        super.onResume();
    //    MobclickAgent.onPageStart("LabelFragment");
    }

    @Override
    public void onPause() {
        super.onPause();
    //    MobclickAgent.onPageEnd("LabelFragment");
    }
}
