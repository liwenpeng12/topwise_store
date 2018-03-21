package com.topwise.topos.appstore.view.fragment;


import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.view.BallScaleProgress;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public abstract class BaseListLoadingFragment extends BaseLoadingFrangment implements 
        OnScrollListener {
    
    protected ListView mListView;

    protected LinearLayout mFooterView;
    
    //加载进度提示区域mFooterView
    protected BallScaleProgress mLoadingProgressbar;
    //加载进度提示标签
    protected TextView mLoadingText;

    protected boolean mStartLoading = false;
    protected boolean mNoMoreData = false;
    
    protected int mTopBottomPadding = 0;
    protected int mNormalPadding = 0;
    
    public BaseListLoadingFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View v = super.onCreateView(inflater, container, savedInstanceState);
         return v;
    }
    
    @Override
    protected View getCustomContentView() {
        mTopBottomPadding = getResources().getDimensionPixelSize(R.dimen.main_list_top_bottom_item_padding);
        mNormalPadding = getResources().getDimensionPixelSize(R.dimen.main_list_item_vertical_padding);
        
        mListView = new ListView(getActivity());
        mListView.setDivider(null);
        mListView.setBackgroundColor(getResources().getColor(R.color.as_list_bg_color));
        mListView.addFooterView(getFooterView());
        
        ListAdapter adapter = getListAdapter();
        if (adapter != null) {
            mListView.setAdapter(adapter);
            mListView.removeFooterView(mFooterView);
        }
        mListView.setOnScrollListener(this);
        return mListView;
    }

    protected abstract ListAdapter getListAdapter();

    protected void addListFooterView() {
        if (mListView.getFooterViewsCount() < 1) {
            mFooterView = getFooterView();
            if (mFooterView != null) {
                mListView.addFooterView(mFooterView); 
            }
        }
    }

    protected void removeFootView() {
        if (mListView != null && mListView.getFooterViewsCount() > 0 && mFooterView != null) {
            mListView.removeFooterView(mFooterView);
            mFooterView = null;
        }
    }
    
    protected void hideFootView() {
        if (mListView != null && mFooterView != null) {
            mFooterView.setVisibility(View.GONE);
            mLoadingProgressbar.stopProgress();
        }
    }

    protected LinearLayout getFooterView() {
        if (mFooterView == null) {
            mFooterView = (LinearLayout) mInflator.inflate(R.layout.as_list_footer, mListView, false);
            mLoadingProgressbar = (BallScaleProgress) mFooterView.findViewById(R.id.as_footprogress);
            mLoadingText = (TextView) mFooterView.findViewById(R.id.as_loading_text);
            
            mFooterView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    doGetMore();
                }
            });
        }
        return mFooterView;
    }

    private void showRefreshText(String text) {
        addListFooterView();
        if (mFooterView != null) {
            mFooterView.setVisibility(View.VISIBLE);
            mLoadingProgressbar.setVisibility(View.VISIBLE); // 隐藏下方的进度条
            mLoadingProgressbar.setRefrush();
            mLoadingText.setText(text);
            mFooterView.setClickable(true);
        }
    }

    private void showFooterProgressBar(String text) {
        addListFooterView();
        if (mFooterView != null) {
            mFooterView.setVisibility(View.VISIBLE);
            mLoadingProgressbar.setVisibility(View.VISIBLE); // 隐藏更多提示TextView
            mLoadingProgressbar.startProgress();
            mLoadingText.setText(text);
            mFooterView.setClickable(false);
        }
    }
    
    private void showErrorText(String text) {
        addListFooterView();
        if (mFooterView != null) {
            mFooterView.setVisibility(View.VISIBLE);
            mLoadingProgressbar.setVisibility(View.VISIBLE); // 隐藏下方的进度条
            mLoadingProgressbar.setError();
            mLoadingText.setText(text);
            mFooterView.setClickable(false);
        }
    }


    private void doGetMore() {
        showFooterProgressBar(getString(R.string.loading));
        getMoreForList();
        
        mStartLoading = true;
    }

 
    public abstract void getMoreForList();

    protected void onGetMoreCompleted() {
        mStartLoading = false;
        hideFootView();
    }

    protected void onGetMoreFailed() {
        mStartLoading = false;
        if (!mNoMoreData) {
            showRefreshText(getString(R.string.click_to_load_more));
        }
    }
    
    protected void onNoMore() {
        mNoMoreData = true;
        
        removeFootView();
    }
    
    protected void onNetworkStateChanged(boolean networkavailiable) {
        if (mListView != null && mFooterView != null) {
            if (networkavailiable) {
                if (mFooterView.getVisibility() == View.VISIBLE) {
                    doGetMore();
                }
            } else {
                showErrorText(getString(R.string.as_network_unavailable));
            }
        }
    }
    
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mNoMoreData) {
            return;
        }
        
        if (totalItemCount > 0 && firstVisibleItem + visibleItemCount == (totalItemCount - 1)) {
            if (!mStartLoading) {
                doGetMore();
            }
        }
       
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {}

}
