package com.topwise.topos.appstore.view;

import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.data.AppInfo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;

public class CheckPopularItemView extends PopularItemView implements Checkable {

    private boolean mChecked = false;
    private boolean mBroadcasting;
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private Drawable mCheckedFlag;
    private Drawable mUnCheckedFlag;
    
    public CheckPopularItemView(Context context) {
        this(context, null, 0);
    }
    
    public CheckPopularItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public CheckPopularItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        
        mCheckedFlag = getResources().getDrawable(R.drawable.zkas_checked);
        mUnCheckedFlag = getResources().getDrawable(R.drawable.zkas_uncheck);
    }

    @Override
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();

            if (mBroadcasting) {
                return;
            }

            mBroadcasting = true;
            if (mOnCheckedChangeListener != null) {
                mOnCheckedChangeListener.onCheckedChanged(this, mAppInfo, mChecked);
            }

            mBroadcasting = false;  
            invalidate();
        }
    }
    
    @Override
    public boolean performClick() {
        toggle();
        return super.performClick();
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }
    
    public interface OnCheckedChangeListener {
        void onCheckedChanged(View buttonView, AppInfo appinfo, boolean isChecked);
    }
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        
        Drawable flag = null;
        if (mChecked) {
            flag = mCheckedFlag;
        } else {
            flag = mUnCheckedFlag;
        }
        
        int flagw = flag.getIntrinsicWidth();
        int flagh = flag.getIntrinsicHeight();
        int width = getWidth();
        
        flag.setBounds(width - flagw, 0, width, flagh);
        flag.draw(canvas);
    }
}
