package com.topwise.topos.appstore.view;

import java.util.ArrayList;

import com.topwise.topos.appstore.conn.behavior.BehaviorEx;
import com.topwise.topos.appstore.conn.behavior.BehaviorLogManager;
import com.topwise.topos.appstore.conn.behavior.UserTrack;
import com.topwise.topos.appstore.conn.http.AjaxCallBack;
import com.topwise.topos.appstore.data.Banner;
import com.topwise.topos.appstore.manager.HttpManager;
import com.topwise.topos.appstore.utils.BitmapUtil;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.utils.AsynTaskManager.ImageLoadCallBack;
import com.topwise.topos.appstore.R;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;

import org.json.JSONArray;
import org.json.JSONObject;

public class ListScrollBannerView extends View {
    private static final String TAG = "ListTopBannerView";
    private Resources mRes;
    private BannerClickListener mBannerClickListener = null;
    private float mTouchSlop;

    private Bitmap mDefaultTopImage;

    private static final int MAX_ANIM_TIME = 500;
    private static final int AUTO_FLIP_BANNER_DELAY = 5000;

    private ArrayList<BannerData> mTopBanners;
    private int mCurrentIndex = 0;
    private int mNextIndex = -1;

    private int mSeekDistence = 0;
    private float mLastMotionX = 0;
    private boolean mIsBeingDragged = false;
    private boolean mIsFling = false;
    private boolean mIsDown = false;
    private boolean mFlipBack = false;

    private Drawable mDotNormal;
    private Drawable mDotSelected;

    private int mMinDragToScroll;
    private int mDotW = 0;
    private int mDotH = 0;
    private int mDotBottomDistance = 0;
    private int mDotCenterMargin = 0;
    private Rect mSrcR = new Rect();
    private Rect mDstR = new Rect();

    private ObjectAnimator mFlingAnim = null;
    private boolean mIsAutoFlip = false;
    private Handler mAutoFlipHandler;
    
    private int mWidth;
    private int mHeight;
    
    private ImageLoadCallBack mImageLoadCallBack;
    private boolean mIsAttached = false;

    public ListScrollBannerView(Context context) {
        this(context, null, 0);
    }

    public ListScrollBannerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListScrollBannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop() * 0.5f;

        mRes = context.getResources();
        mDefaultTopImage = BitmapFactory.decodeResource(mRes, R.drawable.as_default_top_banner);

        mDotNormal = mRes.getDrawable(R.drawable.as_top_banner_indicator_normal);
        mDotSelected = mRes.getDrawable(R.drawable.as_top_banner_indicator_focused);
        mDotW = mDotNormal.getIntrinsicWidth();
        mDotH = mDotNormal.getIntrinsicHeight();
        mDotBottomDistance = mRes
                .getDimensionPixelOffset(R.dimen.top_banner_scroll_area_dot_btm_margin);
        mDotCenterMargin = mRes.getDimensionPixelOffset(R.dimen.top_banner_dot_center_margin);
        
        mImageLoadCallBack = new ImageLoadCallBack() {

            @Override
            public void onImageLoadSuccess(String imageUrl,Bitmap result) {
                if (!isAttached()) {
                    return;
                }
                
                ListScrollBannerView.this.updateImageWithUrl(imageUrl, result);
            }

            @Override
            public void onImageLoadFailed(String imageUrl, String reason) {
                LogEx.d(TAG, "imageUrl=" + imageUrl + ":reason=" + reason);
            }

            @Override
            public String getCaller() {
                return String.valueOf(ListScrollBannerView.this.hashCode());
            }

            @Override
            public boolean isNeedToDecode(String imageUrl) {
                if (!isAttached()) {
                    return false;
                }
                return ListScrollBannerView.this.checkUrl(imageUrl);
            }
        };
        
        setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
            }
        });
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        if (mTopBanners != null && mTopBanners.size() > 0) {
            int w = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            
            int specMode = MeasureSpec.getMode(heightMeasureSpec);
            int specSize = MeasureSpec.getSize(heightMeasureSpec);
            int h = 0;
            
            int hneed = getPaddingTop();
            hneed += getResources().getDimensionPixelSize(R.dimen.as_top_banner_scroll_area_height);
            hneed += getPaddingBottom();
            
            switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                h = getSuggestedMinimumHeight();
                h = hneed > h ? hneed : h;
                break;
            case MeasureSpec.AT_MOST:
                if (hneed > specSize) {
                    hneed = specSize;
                }
                int minh = getSuggestedMinimumHeight();
                h = hneed > minh ? hneed : minh;
                break;
            case MeasureSpec.EXACTLY:
                h = specSize;
                break;
            }
            
            setMeasuredDimension(w, h);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mWidth = getWidth();
        mHeight = getHeight();
        mMinDragToScroll = getWidth() / 8;
    }

    public void setBanners(ArrayList<Banner> banners) {
        if (banners == null || banners.size() == 0) {
            return;
        }

        if (mTopBanners == null) {
            mTopBanners = new ArrayList<BannerData>();
        } else {
            mTopBanners.clear();
        }
//        loadYingPuBanners(mTopBanners);

        for (int i = 0; i < banners.size(); i++) {
            Banner b = banners.get(i);
            Bitmap bmp = null;
            if (isAttached()) {
                bmp = BitmapUtil.getInstance().getBitmapAsync(b.img_url, mImageLoadCallBack);
            } 
            if (bmp == null) {
                bmp = mDefaultTopImage;
            }
            BannerData data = new BannerData(b, bmp);
            mTopBanners.add(data);
        }
        
        if (mTopBanners.size() > 1) {
            setAutoFlipBanner(true);
        }
        requestLayout();
    }

    private static boolean isYingPuLoaded = false;
    private void loadYingPuBanners(final ArrayList<ListScrollBannerView.BannerData> banners) {
        if (isYingPuLoaded) {
            return;
        }
        try {
            HttpManager.getInstance().get("http://218.241.154.135:9090/ivvi/getAllAds?marketId=1", new AjaxCallBack<String>() {
                @Override
                public void onSuccess(String s) {
                    try {
                        JSONObject main = new JSONObject(s);
                        String msg = main.getString("msg");
                        if ("success".equals(msg)) {
                            JSONArray array = main.getJSONArray("data");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject bannerJson = array.getJSONObject(i);
                                Banner b = new Banner();
                                b.id = bannerJson.getString("adid");
                                b.img_url = bannerJson.getString("banner_pic");
                                b.title = bannerJson.getString("ad_name");
                                b.target_url = bannerJson.getString("pkg");
                                b.target = "YingPu";
                                Bitmap bmp = null;
                                if (isAttached()) {
                                    bmp = BitmapUtil.getInstance().getBitmapAsync(b.img_url, mImageLoadCallBack);
                                }
                                if (bmp == null) {
                                    bmp = mDefaultTopImage;
                                }
                                ListScrollBannerView.BannerData data = new ListScrollBannerView.BannerData(b, bmp);
                                banners.add(data);
                            }
                            isYingPuLoaded = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    isYingPuLoaded = false;
                    super.onSuccess(s);
                }

                @Override
                public void onFailure(Throwable t, int errorNo, String strMsg) {
                    isYingPuLoaded = false;
                    super.onFailure(t, errorNo, strMsg);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        isYingPuLoaded = false;
    }

    public void setTopBannerFling(float distance) {
        onBannerTranslate(distance);
    }

    public interface BannerClickListener {
        public void onBannerClicked(Banner banner);
    }

    public void setBannerClickListener(BannerClickListener listener) {
        mBannerClickListener = listener;
    }

    private static class BannerData {
        public Banner mData;
        public Bitmap mImage;

        public BannerData(Banner banner, Bitmap bmp) {
            mData = banner;
            mImage = bmp;
        }
    }

    public boolean checkUrl(String url) {
        if (mTopBanners == null || mTopBanners.size() == 0) {
            return false;
        }

        for (BannerData data : mTopBanners) {
            if (data != null && data.mData != null && url.equals(data.mData.img_url)) {
                return true;
            }
        }

        return false;
    }

    public void updateImageWithUrl(String url, Bitmap bmp) {
        if (url == null || url.length() == 0 || bmp == null) {
            return;
        }
        
        if (mTopBanners == null || mTopBanners.size() == 0) {
            return;
        }

        boolean needinvalidate = false;
        for (BannerData data : mTopBanners) {
            if (data != null && data.mData != null && url.equals(data.mData.img_url)) {
                data.mImage = bmp;
                needinvalidate = true;
            }
        }
        if (needinvalidate) {
            invalidate();
        }
    }

    public void setAutoFlipBanner(boolean isAutoFlip) {
        if (mIsAutoFlip != isAutoFlip) {
            mIsAutoFlip = isAutoFlip;

            if (mIsAutoFlip) {
                if (mAutoFlipHandler == null) {
                    mAutoFlipHandler = new Handler(Looper.getMainLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what != 0) {
                                super.handleMessage(msg);
                                return;
                            }

                            if (mIsAutoFlip && isAttached() && !mIsBeingDragged) {
                                flipToNext();
                                removeMessages(0);
                                sendEmptyMessageDelayed(0, AUTO_FLIP_BANNER_DELAY);
                            } else {
                                removeMessages(0);
                            }
                        }
                    };
                }

                if (isAttached()) {
                    mAutoFlipHandler.sendEmptyMessageDelayed(0, AUTO_FLIP_BANNER_DELAY);
                }
            } else {
                if (mAutoFlipHandler != null) {
                    mAutoFlipHandler.removeMessages(0);
                }
            }
        }
    }

    private void restartAutoFlip() {
        if (mIsAutoFlip && mAutoFlipHandler != null) {
            mAutoFlipHandler.sendEmptyMessageDelayed(0, AUTO_FLIP_BANNER_DELAY);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mIsAttached = true;
        if (mIsAutoFlip && mAutoFlipHandler != null) {
            mAutoFlipHandler.sendEmptyMessageDelayed(0, AUTO_FLIP_BANNER_DELAY);
        }
        
        if (mTopBanners != null) {
            boolean needinvalidate = false;
            for (BannerData data : mTopBanners) {
                if (data != null && data.mData != null) {
                    data.mImage = BitmapUtil.getInstance().getBitmapAsync(data.mData.img_url, mImageLoadCallBack);
                    if (data.mImage == null) {
                        data.mImage = mDefaultTopImage;
                    }
                    needinvalidate = true;
                } 
            }
            
            if (needinvalidate) {
                invalidate();
            }

            try {
                BehaviorLogManager.getInstance().addBehaviorEx(new BehaviorEx(BehaviorEx.VIEW_BANNER, mTopBanners.get(0).mData.id));
            } catch (Exception e) {
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIsAttached = false;
        if (mIsAutoFlip && mAutoFlipHandler != null) {
            mAutoFlipHandler.removeMessages(0);
        }
        
        BitmapUtil.getInstance().clearCallerCallback(mImageLoadCallBack.getCaller());
        if (mTopBanners != null) {
            for (BannerData data : mTopBanners) {
                data.mData = null;
                data.mImage = null;
            }
            
            mTopBanners.clear();
        }
    }
    
    public boolean isAttached() {
        return mIsAttached;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mTopBanners == null || mTopBanners.size() == 0) {
            return super.onTouchEvent(event);
        }

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = event.getX();
                mIsBeingDragged = false;

                endFlingAnimation();

                if (mAutoFlipHandler != null) {
                    mAutoFlipHandler.removeMessages(0);
                }
                mIsDown = true;
                ViewParent parent = ListScrollBannerView.this.getParent();
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTopBanners.size() > 1) {
                    int x = (int) event.getX();
                    int xDiff = (int) (x - mLastMotionX);
                    if (!mIsBeingDragged) {
                        if (Math.abs(xDiff) > mTouchSlop) {
                            mIsBeingDragged = true;
                        }
                    }

                    if (mIsBeingDragged) {
                        mSeekDistence += xDiff;
                        mLastMotionX = x;

                        if (mSeekDistence > 0) {
                            mNextIndex = mCurrentIndex - 1;
                            if (mNextIndex < 0) {
                                mNextIndex = mTopBanners.size() - 1;
                            }
                        } else {
                            mNextIndex = mCurrentIndex + 1;
                            if (mNextIndex >= mTopBanners.size()) {
                                mNextIndex = 0;
                            }
                        }

                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsBeingDragged) {
                    if (mSeekDistence != 0) {
                        startFlingAnimation();
                    }
                    mIsBeingDragged = false;
                } else {
                    if (mIsDown) {
                        try {
                            UserTrack.getInstance().openAd(mTopBanners.get(mCurrentIndex).mData);
                            if (mBannerClickListener != null) {
                                mBannerClickListener.onBannerClicked(mTopBanners.get(mCurrentIndex).mData);
                            }
                        } catch (Exception e) {
                        }
                    }
                }
                restartAutoFlip();
                mIsDown = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged) {
                    if (mSeekDistence != 0) {
                        startFlingAnimation();
                    }
                    mIsBeingDragged = false;
                }
                restartAutoFlip();
                mIsDown = false;
                break;

        }

        return true;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mWidth == 0 || mHeight == 0) {
            return;
        }

        if (mTopBanners == null || mTopBanners.size() == 0) {
            return;
        }

        try {
            BannerData curt = mTopBanners.get(mCurrentIndex);

            int lLeft = getPaddingLeft();
            int lTop = getPaddingTop();
            int lRight = mWidth - getPaddingRight();
            int lBottom = mHeight - getPaddingBottom();
            
            if (mSeekDistence != 0) {
                BannerData next = mTopBanners.get(mNextIndex);
                if (mSeekDistence > 0) {
                    int cimgw = curt.mImage.getWidth();
                    int cimgh = curt.mImage.getHeight();
                    int drW = (mWidth - mSeekDistence) * cimgw / mWidth;
                    mSrcR.set(0, 0, drW, cimgh);
                    mDstR.set(lLeft + mSeekDistence, lTop, lRight, lBottom);
                    canvas.drawBitmap(curt.mImage, mSrcR, mDstR, null);

                    int imgW = next.mImage.getWidth();
                    int imgH = next.mImage.getHeight();
                    drW = mSeekDistence * imgW / mWidth;
                    mSrcR.set(imgW - drW, 0, imgW, imgH);
                    mDstR.set(lLeft, lTop, lLeft + mSeekDistence, lBottom);
                    canvas.drawBitmap(next.mImage, mSrcR, mDstR, null);
                } else {
                    int left = Math.abs(mSeekDistence);
                    int imgW = next.mImage.getWidth();
                    int imgH = next.mImage.getHeight();
                    int drW = left * imgW / mWidth;
                    mSrcR.set(0, 0, drW, imgH);
                    mDstR.set(lRight - left, lTop, lRight, lBottom);
                    canvas.drawBitmap(next.mImage, mSrcR, mDstR, null);

                    int cimgw = curt.mImage.getWidth();
                    int cimgh = curt.mImage.getHeight();
                    drW = (mWidth - left) * cimgw / mWidth;

                    mSrcR.set(cimgw - drW, 0, cimgw, cimgh);
                    mDstR.set(lLeft, lTop, lRight - left, lBottom);
                    canvas.drawBitmap(curt.mImage, mSrcR, mDstR, null);
                }
            } else {
                mDstR.set(lLeft, lTop, lRight, lBottom);
                canvas.drawBitmap(curt.mImage, null, mDstR, null);
            }

            int cnt = mTopBanners.size();
            if (cnt > 1) {
                int left = (mWidth - cnt * mDotW - (cnt - 1) * mDotCenterMargin) / 2 + lLeft;
                int btm = lBottom - mDotBottomDistance;
                int top = btm - mDotH;

                for (int i = 0; i < cnt; i++) {
                    mSrcR.set(left, top, left + mDotW, btm);

                    if (mCurrentIndex == i) {
                        mDotSelected.setBounds(mSrcR);
                        mDotSelected.draw(canvas);
                    } else {
                        mDotNormal.setBounds(mSrcR);
                        mDotNormal.draw(canvas);
                    }

                    left += (mDotW + mDotCenterMargin);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onBannerTranslate(float distance) {
        if (mIsFling) {
            mSeekDistence = (int) (distance + 0.5f);

            if (mSeekDistence > mWidth) {
                mSeekDistence = mWidth;
            }

            if (mSeekDistence < -mWidth) {
                mSeekDistence = -mWidth;
            }

            invalidate();
        }
    }

    private void initAnimation() {
        if (mFlingAnim == null) {
            mFlingAnim = ObjectAnimator.ofFloat(ListScrollBannerView.this, "TopBannerFling", 0f, 0f);
            mFlingAnim.setDuration(MAX_ANIM_TIME);
            mFlingAnim.addListener(new AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {
                    mIsFling = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsFling = false;
                    mSeekDistence = 0;

                    if (mFlipBack) {
                        mNextIndex = -1;
                    } else {
                        if (mNextIndex >= 0 && mNextIndex < mTopBanners.size()) {
                            mCurrentIndex = mNextIndex;
                            mNextIndex = -1;
                            BehaviorLogManager.getInstance().addBehaviorEx(
                                    new BehaviorEx(BehaviorEx.VIEW_BANNER, 
                                            mTopBanners.get(mCurrentIndex).mData.id));
                        }
                    }

                    invalidate();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mIsFling = false;
                    mSeekDistence = 0;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
        }
    }

    public void startFlingAnimation() {
        initAnimation();

        long duration = MAX_ANIM_TIME;
        if (mSeekDistence > 0) {
            if (mSeekDistence > mMinDragToScroll) {
                mFlingAnim.setFloatValues(mSeekDistence, mWidth);
                duration = MAX_ANIM_TIME * (mWidth - mSeekDistence) / mWidth;
                mFlipBack = false;
            } else {
                mFlingAnim.setFloatValues(mSeekDistence, 0);
                duration = MAX_ANIM_TIME * mSeekDistence / mWidth;
                mFlipBack = true;
            }
        } else {
            if (mSeekDistence < -mMinDragToScroll) {
                mFlingAnim.setFloatValues(mSeekDistence, -mWidth);
                duration = MAX_ANIM_TIME * (mWidth + mSeekDistence) / mWidth;
                mFlipBack = false;
            } else {
                mFlingAnim.setFloatValues(mSeekDistence, 0);
                duration = MAX_ANIM_TIME * (0 - mSeekDistence) / mWidth;
                mFlipBack = true;
            }
        }

        if (duration < 0) {
            duration = 0;
        }
        mFlingAnim.setDuration(duration);
        mFlingAnim.start();
    }

    public void endFlingAnimation() {
        if (mFlingAnim != null && mFlingAnim.isStarted()) {
            mFlingAnim.end();
        }
    }

    public void flipToNext() {
        if ((mFlingAnim != null && mFlingAnim.isStarted()) || mTopBanners.size() < 2) {
            return;
        }

        initAnimation();

        mNextIndex = mCurrentIndex + 1;
        if (mNextIndex >= mTopBanners.size()) {
            mNextIndex = 0;
        }

        mFlingAnim.setFloatValues(0, -mWidth);
        mFlingAnim.setDuration(MAX_ANIM_TIME);
        mFlipBack = false;

        mFlingAnim.start();
    }

}
