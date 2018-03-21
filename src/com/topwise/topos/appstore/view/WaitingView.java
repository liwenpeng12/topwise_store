
package com.topwise.topos.appstore.view;


import com.topwise.topos.appstore.R;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WaitingView extends LinearLayout {

    private TextView mTextView;
    private BallScaleProgress mProgressView;
    
    private RefrushClickListener mListener;
    
    private BallScaleProgress.OnRefreshClickListener mClickListener;

    public WaitingView(Context context) {
        super(context);
        init();
        addDefaultChild();
    }

    public WaitingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaitingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    private void addDefaultChild() {
        Context context = getContext();
        Resources res = getResources();
        setOrientation(VERTICAL);
        
        mProgressView = new BallScaleProgress(context);
        mProgressView.setOnRefreshClickListener(mClickListener);
        addView(mProgressView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        
        mTextView = new TextView(context);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimension(R.dimen.as_wait_view_text_size));
        mTextView.setTextColor(res.getColor(R.color.as_wait_view_text_color));
        mTextView.setSingleLine(false);
        mTextView.setGravity(Gravity.CENTER);
        
        int topmargin = res.getDimensionPixelSize(R.dimen.as_wait_textview_top_margin);
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.topMargin = topmargin;
        addView(mTextView, lp);
        
        setGravity(Gravity.CENTER);
        setBackgroundColor(getResources().getColor(R.color.as_wait_view_bg));
    }

    private void init() {
        mClickListener = new BallScaleProgress.OnRefreshClickListener() {
            
            @Override
            public void OnRefreshClicked(View v) {
                if (mListener != null) {
                    mListener.onRefushClicked(WaitingView.this);
                }
            }
        };
        
        setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
            }
        });
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        
        if (getChildCount() == 0) {
            addDefaultChild();
        } else {
            int size = getChildCount();
            for (int i = 0 ; i < size; i++) {
                View child = getChildAt(i);
                if (child instanceof TextView) {
                    mTextView = (TextView) child;
                } else if (child instanceof BallScaleProgress) {
                    mProgressView = (BallScaleProgress)child;
                }
            }
            
            if (mProgressView != null) {
                mProgressView.setOnRefreshClickListener(mClickListener);
            }
            
            if (mTextView == null || mProgressView == null) {
                removeAllViews();
                addDefaultChild();
            }
        }
    }
    
    public interface RefrushClickListener {
        public void onRefushClicked(WaitingView v);
    }
    
    public void startProgress() {
        mProgressView.setVisibility(View.VISIBLE);
        mProgressView.startProgress();
        
        if (getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE);
        } 
    }
    
    public void startProgress(String prompt) {
        if (prompt != null) {
            mTextView.setText(prompt);
        }
        
        mProgressView.setVisibility(View.VISIBLE);
        mProgressView.startProgress();
        
        if (getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE);
        } 
        mTextView.setClickable(false);
    }
    
    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        mProgressView.setVisibility(visibility);
    }
    
    public void showRefrushButton(String prompt, RefrushClickListener l) {
        if (prompt != null) {
            mTextView.setText(prompt);
        }
        
        mListener = l;
        if (getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE);
        }
        
        mProgressView.setVisibility(View.VISIBLE);
        mProgressView.setRefrush();
    }
    
    public void showPromptText(String prompt) {
        if (prompt != null) {
            mTextView.setText(prompt);
        }
        
        if (getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE);
        }
        mProgressView.setError();
    }
    
    public boolean isProgressing() {
        if (mProgressView.getVisibility() == View.VISIBLE) {
            return mProgressView.isProgressing();
        }
        
        return false;
    }
    
    public void stopProgressing() {
        if (mProgressView.getVisibility() == View.VISIBLE) {
            mProgressView.stopProgress();
        }
    }
}
