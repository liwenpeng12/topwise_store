package com.topwise.topos.appstore.view.fragment;

import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.data.Rank;
import com.topwise.topos.appstore.manager.AppManager;
import com.umeng.analytics.MobclickAgent;

public class RankFragment extends MainListFragment {

    private int mRankId;
    public void setRankId(int id) {
        mRankId = id;
    }
    
    @Override
    protected void loadData() {
        AppManager.getInstance().loadRank(mRankId, mManagerCallback);
        onNoMore();
    }
    
    @Override
    protected Object getDatas() {
        Rank rank = DataPool.getInstance().getRank(getDataType(),  "" + mRankId);
        return rank;
    }
    
    @Override
    protected void setDataToList() {
        ((MainListAdapter)mAdapter).setRankData((Rank)getDatas());
    }
    
    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("RankFragment");
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("RankFragment");
    }
}
