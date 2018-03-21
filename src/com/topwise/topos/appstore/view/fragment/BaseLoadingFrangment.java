
package com.topwise.topos.appstore.view.fragment;

import com.topwise.topos.appstore.view.WaitingView;
import com.topwise.topos.appstore.view.WaitingView.RefrushClickListener;
import com.topwise.topos.appstore.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

/***
 *
***/

public abstract class BaseLoadingFrangment extends BaseFragment {

    protected LayoutInflater mInflator;

    protected FrameLayout mBaseViewGroup;

    protected WaitingView mWaitingView;
    
    private boolean mIsProgressing = false;
    
    public BaseLoadingFrangment() {
    }

    // 刷新布局

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        mInflator = inflater;
        if (mBaseViewGroup == null) {
            mBaseViewGroup = (FrameLayout) mInflator.inflate(R.layout.as_dataloading_fragment,
                    container, false);
            mWaitingView = (WaitingView) mBaseViewGroup.getChildAt(0);
    
            View customview = getCustomContentView();
            if (customview != null) {
                customview.setVisibility(View.GONE);
                mBaseViewGroup.addView(customview, 0,
                        new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            }
        }
        return mBaseViewGroup;
    }
    
   

    protected abstract View getCustomContentView();

    @Override
    public void onPause() {
        super.onPause();
        mIsProgressing = mWaitingView.isProgressing();
        mWaitingView.stopProgressing();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (mIsProgressing) {
            mWaitingView.startProgress();
        }
    }
    
    public final boolean isWaitViewShowing() {
        return mWaitingView.getVisibility() == View.VISIBLE;
    }
    
    public final void hideWaitView() {
        if (mWaitingView != null) {
            mWaitingView.setVisibility(View.GONE);
            mIsProgressing = false;
            
            View customview = mBaseViewGroup.getChildAt(0);
            if (customview != null && customview != mWaitingView) {
                customview.setVisibility(View.VISIBLE);
            }
        }
    }

    public final void showWaitViewProgress(String prompt) {
        if (mWaitingView != null) {
            mWaitingView.startProgress(prompt);
            mIsProgressing = true;
        }
    }

    public final void showWaitViewProgress(int txtid) {
        String prompt = getString(txtid);
        if (prompt == null) {
            prompt = "";
        }
        showWaitViewProgress(prompt);
    }

    public final void showWaitViewRefushBtn(String prompt, String btntext, RefrushClickListener l) {
        if (mWaitingView != null) {
            String showtext = prompt == null ? "" : prompt;
            if (btntext != null) {
                showtext += btntext;
            }
            mWaitingView.showRefrushButton(showtext, l);
            mIsProgressing = false;
        }
    }

    public final void showWaitViewRefushBtn(int promptid, int btntextid, RefrushClickListener l) {
        if (mWaitingView != null) {
            String prompt = getString(promptid);
            String btntext = getString(btntextid);

            showWaitViewRefushBtn(prompt, btntext, l);
        }
    }

    public final void showWaitViewPrompt(String prompt) {
        if (mWaitingView != null) {
            mWaitingView.showPromptText(prompt);
            mIsProgressing = false;
        }
    }

    public final void showWaitViewPrompt(int txtid) {
        String prompt = getString(txtid);
        showWaitViewPrompt(prompt);
    }
}
