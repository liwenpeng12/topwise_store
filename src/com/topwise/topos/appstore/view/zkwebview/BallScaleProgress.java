package com.topwise.topos.appstore.view.zkwebview;

import com.topwise.topos.appstore.R;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class BallScaleProgress extends View {

    private Paint mBallPaint;
    private OnRefreshClickListener mOnRefreshClickListener;
    
    private static final int MAX_BALL_COUNT = 3;
    private static final int ANIM_PER_DELAY = 200;
    
    private AnimatorSet mBallAnimations;
    private BallAnimatorUpdateListener[] mAnimUpdateListeners = new BallAnimatorUpdateListener[MAX_BALL_COUNT];
    
    private ValueAnimator mStateChangeAnimation;
    private BallAnimatorUpdateListener mStateAnimUpdateListener;
    private RefreshAnimatorListener mStateChangeAnimListener;
    
    private int mDefaultBallSize = 0;
    private boolean mIsProgressing = false;
    private boolean mIsStateAnimate = false;
    
    private Drawable mRefreshBtnBg;
    private Drawable mErrorBtnBg;
    private Drawable mStateDrawable;
    
    private boolean mIsAttached = false;
    
    public BallScaleProgress(Context context) {
        this(context, null, 0);
    }
    
    public BallScaleProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public BallScaleProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        
        mBallPaint = new Paint();
        mBallPaint.setColor(0xFFFA5153);
        
        mBallAnimations = new AnimatorSet();
        Animator[] array = new Animator[MAX_BALL_COUNT];
        for (int i = 0; i < MAX_BALL_COUNT; i++) {
            ValueAnimator a = ValueAnimator.ofFloat(0,1);
            a.setInterpolator(new LinearInterpolator());
            a.setDuration(1000);
            a.setRepeatCount(-1);
            
            mAnimUpdateListeners[i] = new BallAnimatorUpdateListener();
            a.addUpdateListener(mAnimUpdateListeners[i]);
            a.setStartDelay((i + 1) * ANIM_PER_DELAY);
            array[i] = a;
        }
        
        mBallAnimations.playTogether(array);
        
        mStateChangeAnimation = ValueAnimator.ofFloat(0f, 1f);
        mStateChangeAnimation.setDuration(300);
        mStateAnimUpdateListener = new BallAnimatorUpdateListener();
        mStateChangeAnimation.addUpdateListener(mStateAnimUpdateListener);
        mStateChangeAnimListener = new RefreshAnimatorListener();
        mStateChangeAnimation.addListener(mStateChangeAnimListener);

        mRefreshBtnBg = getResources().getDrawable(R.drawable.as_ball_progress_refushed);
        mErrorBtnBg = getResources().getDrawable(R.drawable.as_ball_progress_refushed);
        mDefaultBallSize = mRefreshBtnBg.getIntrinsicWidth();
        
        super.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (!mIsProgressing && mOnRefreshClickListener != null && mStateDrawable == mRefreshBtnBg) {
                    mOnRefreshClickListener.onRefreshClicked(v);
                }
            }
        });
    }
    
    @Override
    public void setOnClickListener(OnClickListener l) {
    }
    
    private class BallAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        private float mFraction = 0.0f;
        private float mScaleStart = 0f;
        private float mScaleEnd = 1f;
        private int mAlphaStart = 255;
        private int mAlphaEnd = 0;

        public int getAlpha() {
            return (int) (mAlphaStart + mFraction * (mAlphaEnd - mAlphaStart));
        }
        
        public float getScale() {
            return mScaleStart + mFraction * (mScaleEnd - mScaleStart);
        }
        
        public void setScaleValue(float start, float end) {
            mScaleStart = start;
            mScaleEnd = end;
        }
        
        @Override
        public void onAnimationUpdate(ValueAnimator arg0) {
            mFraction = arg0.getAnimatedFraction();
            postInvalidate();
        }
    }
    
    private class RefreshAnimatorListener implements AnimatorListener {
        public static final int ST_UNKOWN = 0;
        public static final int ST_SHOW = 1;
        public static final int ST_HIDE = 2;
        
        private int mState = ST_UNKOWN;
        public void setState(int state) {
            mState = state;
        }
        
        public int getState() {
            return mState;
        }
        
        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (mState == ST_SHOW) {
                if (mBallAnimations.isStarted()) {
                    mBallAnimations.end();
                }
                mIsProgressing = false;
            } else if (mState == ST_HIDE) {
                if (!mBallAnimations.isStarted()) {
                    mBallAnimations.start();
                    mIsProgressing = true;
                }
                mStateDrawable = null;
            }
            mState = ST_UNKOWN;
            mIsStateAnimate = false;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            mState = ST_UNKOWN;
            mIsStateAnimate = false;
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    }

    public void setBallColor(int color) {
        mBallPaint.setColor(color);
    }
    
    public boolean isProgressing() {
        return mIsProgressing;
    }
    
    public void startProgress() {
        if (!mIsProgressing) {
            if (isAttached() && getWidth() > 0 && mStateDrawable != null) {
                mStateChangeAnimation.cancel();
                mStateChangeAnimation.setFloatValues(1f, 0f);
                mStateAnimUpdateListener.setScaleValue(1f, 0f);
                mStateChangeAnimListener.setState(RefreshAnimatorListener.ST_HIDE);
                mStateChangeAnimation.start();
                mIsStateAnimate = true;
            } else {
                mBallAnimations.start();
                mIsProgressing = true;
            }
        }
    }
    
    public void stopProgress() {
        if (mIsProgressing && mBallAnimations.isStarted()) {
            mBallAnimations.end();
            mIsProgressing = false;
        }
        
        if (mIsStateAnimate && mStateChangeAnimation.isStarted()) {
            mStateChangeAnimation.cancel();
        }
    }
    
    public void setRefresh() {
        if (mIsStateAnimate && mStateChangeAnimListener.getState() == RefreshAnimatorListener.ST_SHOW) {
            mStateDrawable = mRefreshBtnBg;
            return;
        }
        
        mStateDrawable = mRefreshBtnBg;
        mStateChangeAnimation.cancel();
        mStateChangeAnimation.setFloatValues(0f, 1f);
        mStateAnimUpdateListener.setScaleValue(0f, 1f);
        mStateChangeAnimListener.setState(RefreshAnimatorListener.ST_SHOW);
        mStateChangeAnimation.start();
        mIsStateAnimate = true;
    }
    
    public void setError() {
        if (mIsStateAnimate && mStateChangeAnimListener.getState() == RefreshAnimatorListener.ST_SHOW) {
            mStateDrawable = mErrorBtnBg;
            return;
        }
        
        mStateDrawable = mErrorBtnBg;
        mStateChangeAnimation.cancel();
        mStateChangeAnimation.setFloatValues(0f, 1f);
        mStateAnimUpdateListener.setScaleValue(0f, 1f);
        mStateChangeAnimListener.setState(RefreshAnimatorListener.ST_SHOW);
        mStateChangeAnimation.start();
        mIsStateAnimate = true;
    }
    
    
    public interface OnRefreshClickListener {
        public void onRefreshClicked(View v);
    }
    
    public void setOnRefreshClickListener(OnRefreshClickListener l) {
        mOnRefreshClickListener = l;
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mIsAttached = true;
        if (mIsProgressing && !mBallAnimations.isStarted()) {
            mBallAnimations.start();
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIsAttached = false;
        if (mIsProgressing && mBallAnimations.isStarted()) {
            mBallAnimations.cancel();
            mIsProgressing = false;
        }
        
        if (mIsStateAnimate && mStateChangeAnimation.isStarted()) {
            mStateChangeAnimation.cancel();
        }
    }
    
    public boolean isAttached() {
        return mIsAttached;
    }
    
    @Override
    public void setVisibility(int visibility) {
        if (getVisibility() != visibility) {
            super.setVisibility(visibility);
            if (visibility == GONE || visibility == INVISIBLE) {
                if (mIsProgressing && mBallAnimations.isStarted()) {
                    mBallAnimations.end();
                }
                
                if (mIsStateAnimate && mStateChangeAnimation.isStarted()) {
                    mStateChangeAnimation.cancel();
                }
            } else {
                if (mIsProgressing && !mBallAnimations.isStarted()) {
                    mBallAnimations.start();
                }
            }
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        int paddingleft = getPaddingLeft();
        int paddingright = getPaddingRight();
        int paddingtop = getPaddingTop();
        int paddingbtm = getPaddingBottom();
        
        int wneed = paddingleft + paddingright + mDefaultBallSize;
        int w = measureSize(wneed, widthMeasureSpec, getSuggestedMinimumWidth());
        if (w < wneed) {
            mDefaultBallSize = w - paddingleft - paddingright;
            if (mDefaultBallSize < 0) {
                mDefaultBallSize = 0;
            }
        }
        
        int hneed = paddingtop + paddingbtm + mDefaultBallSize;
        int h = measureSize(hneed, heightMeasureSpec, getSuggestedMinimumHeight());
        if (h < hneed) {
            mDefaultBallSize = h - paddingtop - paddingbtm;
            if (mDefaultBallSize < 0) {
                mDefaultBallSize = 0;
            }
        }
        
        setMeasuredDimension(w, h);
    }
    
    private int measureSize(int need, int measureSpec, int min) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        
        int retvalue = need;
        switch (specMode) {
        case MeasureSpec.UNSPECIFIED:
            retvalue = need > min ? need : min;
            break;
        case MeasureSpec.AT_MOST:
            if (need > specSize) {
                need = specSize;
            }
            retvalue = need > min ? need : min;
            break;
        case MeasureSpec.EXACTLY:
            retvalue = specSize;
            break;
        }
        
        return retvalue;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (mDefaultBallSize <= 0) {
            return;
        }
        
        int paddingleft = getPaddingLeft();
        int paddingtop = getPaddingTop();
        
        if (mIsProgressing) {
            for (int i = 0; i < MAX_BALL_COUNT; i++) {
                int alpha = mAnimUpdateListeners[i].getAlpha();
                mBallPaint.setAlpha(alpha);
                
                float scale = mAnimUpdateListeners[i].getScale();
                
                int save = canvas.save();
                
                float x = ((float)mDefaultBallSize) / 2 + (float)paddingleft;
                float y = ((float)mDefaultBallSize) / 2 + (float)paddingtop;
                float radius = ((float)mDefaultBallSize) / 2;
                
                canvas.scale(scale, scale, x, y);
                canvas.drawCircle(x, y, radius, mBallPaint);
                
                canvas.restoreToCount(save);
            }
        } 
        
        if (mStateDrawable != null) {
            float scale = mStateAnimUpdateListener.getScale();
            float xw = scale * ((float)mDefaultBallSize / 2);
            float yh = scale * ((float)mDefaultBallSize / 2);
            
            float x = ((float)mDefaultBallSize) / 2 + (float)paddingleft;
            float y = ((float)mDefaultBallSize) / 2 + (float)paddingtop;
            
            mStateDrawable.setBounds((int)(x - xw), (int)(y - yh), (int)(x + xw), (int)(y + yh));
            mStateDrawable.draw(canvas);
        } 
    }
}
