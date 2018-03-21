package com.topwise.topos.appstore.view.zkwebview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LoadingView extends FrameLayout {

    private TextView mTextView;
    private BallScaleProgress mProgressView;
    private ImageView mCoverView;

    private OnRefreshClickListener mListener;
    
    private boolean mIsCoverShowed = false;

    public LoadingView(Context context) {
        this(context, null, 0);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        addDefaultChild();
    }

    private void addDefaultChild() {
    	LinearLayout layout = new LinearLayout(getContext());
    	layout.setOrientation(LinearLayout.VERTICAL);
    	layout.setGravity(Gravity.CENTER);
        setBackgroundColor(0xFFFFFFFF);

        mProgressView = new BallScaleProgress(getContext());
        mProgressView.setOnRefreshClickListener(mClickListener);
        layout.addView(mProgressView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        mTextView = new TextView(getContext());
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 16 * getContext().getResources().getDisplayMetrics().density);
        mTextView.setTextColor(0x8A000000);
        mTextView.setSingleLine(false);
        mTextView.setGravity(Gravity.CENTER);
        layout.addView(mTextView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        
        addView(layout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        
        mCoverView = new ImageView(getContext());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (getChildCount() == 0) {
            addDefaultChild();
        } else {
            int size = getChildCount();
            for (int i = 0; i < size; i++) {
                View child = getChildAt(i);
                if (child instanceof TextView) {
                    mTextView = (TextView) child;
                } else if (child instanceof BallScaleProgress) {
                    mProgressView = (BallScaleProgress) child;
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

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        setBackgroundColor(0xFFFFFFFF);
        mProgressView.setVisibility(visibility);
    }

    private BallScaleProgress.OnRefreshClickListener mClickListener = new BallScaleProgress.OnRefreshClickListener() {

        @Override
        public void onRefreshClicked(View v) {
            if (mListener != null) {
                mListener.onRefreshClicked(LoadingView.this);
            }
        }
    };

    public interface OnRefreshClickListener {
        public void onRefreshClicked(LoadingView v);
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

    public void stopProgressing() {
        if (mProgressView.getVisibility() == View.VISIBLE) {
            mProgressView.stopProgress();
        }
    }

    public boolean isProgressing() {
        if (mProgressView.getVisibility() == View.VISIBLE) {
            return mProgressView.isProgressing();
        }
        return false;
    }

    public void showRefreshButton(String prompt, OnRefreshClickListener l) {
        if (prompt != null) {
            mTextView.setText(prompt);
        }
        mListener = l;
        if (getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE);
        }
        mProgressView.setVisibility(View.VISIBLE);
        mProgressView.setRefresh();
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
    
    public void showCover(Drawable drawable, OnClickListener l) {
        if (getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE);
        }
    	try {
    		mCoverView.setImageDrawable(drawable);
    		mCoverView.setScaleType(ImageView.ScaleType.FIT_START);
        	mCoverView.setOnClickListener(l);
        	addView(mCoverView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        	mIsCoverShowed = true;
    	} catch (Exception e) {
    	}
    }
    
    public void hideCover() {
    	removeView(mCoverView);
    	mIsCoverShowed = false;
    }
    
    public boolean isCoverShowed() {
    	return mIsCoverShowed;
    }

}
