package com.topwise.topos.appstore.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
/**
 * 这个可以设置可否去滑动，例如字体详情页面只有一页时
 * 就可以设置不滑动
 */
public class ScrollEnableViewPager extends MyViewPager {
    private boolean mEnableScroll = true;
    public ScrollEnableViewPager(Context context) {
        super(context);
    }

    public ScrollEnableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(!mEnableScroll){
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }
    
    public void setEnableScroll(boolean enableScroll){
        mEnableScroll = enableScroll;
    }
}
