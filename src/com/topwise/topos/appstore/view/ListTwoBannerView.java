package com.topwise.topos.appstore.view;

import com.topwise.topos.appstore.conn.behavior.UserTrack;
import com.topwise.topos.appstore.data.Banner;
import com.topwise.topos.appstore.utils.BitmapUtil;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.utils.AsynTaskManager.ImageLoadCallBack;
import com.topwise.topos.appstore.view.ListScrollBannerView.BannerClickListener;
import com.topwise.topos.appstore.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ListTwoBannerView extends View {
    private static final String TAG = "ListTwoBannerView";
    private BannerImage mLeftBanner = null;
    private BannerImage mRightBanner = null;
    private BannerClickListener mBannerClickListener = null;
    
    private int mNormalBannerHeight;
    private int mBannersCenterMargin;
    private BannerImage mDownedSubView = null;
    private ImageLoadCallBack mImageLoadCallBack;
    private boolean mIsAttached = false;
    
    public ListTwoBannerView(Context context) {
        this(context, null, 0);
    }
    
    public ListTwoBannerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public ListTwoBannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        
        Resources res = context.getResources();
        mBannersCenterMargin = res.getDimensionPixelSize(R.dimen.zkas_banner_center_margin);
        mNormalBannerHeight = res.getDimensionPixelSize(R.dimen.zkas_banner_normal_height);

        mImageLoadCallBack = new ImageLoadCallBack() {

            @Override
            public void onImageLoadSuccess(String imageUrl,Bitmap result) {
                if (!isAttached()) {
                    return;
                }
                
                ListTwoBannerView.this.updateImageWithUrl(imageUrl, result);
            }

            @Override
            public void onImageLoadFailed(String imageUrl, String reason) {
                LogEx.d(TAG, "imageUrl=" + imageUrl + ":reason=" + reason);
            }

            @Override
            public String getCaller() {
                return String.valueOf(ListTwoBannerView.this.hashCode());
            }

            @Override
            public boolean isNeedToDecode(String imageUrl) {
                if (!isAttached()) {
                    return false;
                }
                return ListTwoBannerView.this.checkUrl(imageUrl);
            }
        };
        
        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }
    
    public void setLeftBanner(Banner b) {
        if (mLeftBanner == null) {
            mLeftBanner = new BannerImage(this);
        }
        
        Bitmap bmp = null;
        if (isAttached()) {
            bmp = BitmapUtil.getInstance().getBitmapAsync(b.img_url, mImageLoadCallBack);
        } else {
            bmp = null;
        }
        mLeftBanner.updateBanner(b, bmp, mBannerClickListener);
    }
    
    public void setRightBanner(Banner b) {
        if (mRightBanner == null) {
            mRightBanner = new BannerImage(this);
        }
        
        Bitmap bmp = null;
        if (isAttached()) {
            bmp = BitmapUtil.getInstance().getBitmapAsync(b.img_url, mImageLoadCallBack);
        } else {
            bmp = null;
        }
        mRightBanner.updateBanner(b, bmp, mBannerClickListener);
    }
    
    public void setBannerClickListener(BannerClickListener listener) {
        mBannerClickListener = listener;
        
        if (mLeftBanner != null) {
            mLeftBanner.setBannerClickListener(mBannerClickListener);
        }
        
        if (mRightBanner != null) {
            mRightBanner.setBannerClickListener(mBannerClickListener);
        }
    }
    
    public boolean checkUrl(String url) {
        if (url == null || url.length() == 0) {
            return false;
        }
        
        if (mLeftBanner != null && mLeftBanner.checkUrl(url)) {
            return true;
        }
        
        if (mRightBanner != null && mRightBanner.checkUrl(url)) {
            return true;
        }
        
        return false;
    }
    
    public void updateImageWithUrl(String url, Bitmap bmp) {
        if (url == null || url.length() == 0 || bmp == null) {
            return;
        }
        
        if (mLeftBanner != null && url.equals(mLeftBanner.mBanner.img_url)) {
            mLeftBanner.updateBitmapWithUrl(bmp);
        }
        
        if (mRightBanner != null && url.equals(mRightBanner.mBanner.img_url)) {
            mRightBanner.updateBitmapWithUrl(bmp);
        }
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mIsAttached = true;
        if (mLeftBanner != null) {
            Bitmap bmp = BitmapUtil.getInstance().getBitmapAsync(mLeftBanner.mBanner.img_url, mImageLoadCallBack);
            if (bmp != null) {
                mLeftBanner.updateBitmapWithUrl(bmp);
            }
        }
        
        if (mRightBanner != null) {
            Bitmap bmp = BitmapUtil.getInstance().getBitmapAsync(mRightBanner.mBanner.img_url, mImageLoadCallBack);
            if (bmp != null) {
                mRightBanner.updateBitmapWithUrl(bmp);
            }
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIsAttached = false;
        BitmapUtil.getInstance().clearCallerCallback(mImageLoadCallBack.getCaller());
        if (mLeftBanner != null) {
            mLeftBanner.mImage = null;
            mLeftBanner = null;
        }
        
        if (mRightBanner != null) {
            mRightBanner.mImage = null;
            mRightBanner = null;
        }
    }
    
    public boolean isAttached() {
        return mIsAttached;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        int w = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        int specSize = MeasureSpec.getSize(heightMeasureSpec);
        int h = 0;
        
        int hneed = getPaddingTop();
        if (mLeftBanner != null || mRightBanner != null) {
            hneed += mNormalBannerHeight;
        }
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
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        
        int width = getWidth();
        int pl = getPaddingLeft();
        int pr = getPaddingRight();
        int normalbw = (width - mBannersCenterMargin - pl - pr) >> 1;
        int pt = getPaddingTop();
        
        if (mLeftBanner != null) {
            mLeftBanner.layout(pl, pt, pl + normalbw, pt + mNormalBannerHeight);
        }
        
        if (mRightBanner != null) {
            mRightBanner.layout(pl + normalbw + mBannersCenterMargin, pt, width - pr, pt + mNormalBannerHeight);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        int action = event.getAction();
        
        if (action == MotionEvent.ACTION_DOWN) {
            if (mLeftBanner != null) {
                if (mLeftBanner.pointInView(x, y)) {
                    if (mLeftBanner.onTouchEvent(event)) {
                        mDownedSubView = mLeftBanner;
                        return true;
                    }
                }
            }
            
            if (mRightBanner != null) {
                if (mRightBanner.pointInView(x, y)) {
                    if (mRightBanner.onTouchEvent(event)) {
                        mDownedSubView = mRightBanner;
                        return true;
                    }
                }
            }
        } else {
            if (mDownedSubView != null) {
                return mDownedSubView.onTouchEvent(event);
            } 
        }
        
        return super.onTouchEvent(event);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (mLeftBanner != null) {
            mLeftBanner.draw(canvas);
        }
        
        if (mRightBanner != null) {
            mRightBanner.draw(canvas);
        }
    }
    
    public static class BannerImage {
        private Banner mBanner;
        private Bitmap mImage;
        private BannerClickListener mListener;
        private boolean mIsDown = false;
        private Rect mRect = new Rect();
        private Path mClipPath = new Path();
        
        private View mContainer;
        private int mDefColor;
        
        protected int mWidth = 0;
        protected int mHeight = 0;
        protected int mLeft = 0;
        protected int mRight = 0;
        protected int mTop = 0;
        protected int mBottom = 0;
        
        public BannerImage(View container) {
            mContainer = container;
            mDefColor = mContainer.getResources().getColor(R.color.zkas_banner_default_color);
        }
        
        public void updateBanner(Banner b, Bitmap bmp,BannerClickListener l) {
            mBanner = b;
            mImage = bmp;            
            mListener = l;
            if (mContainer != null) {
                if (mWidth == 0 || mHeight == 0) {
                    mContainer.requestLayout();
                } else {
                    mContainer.invalidate();
                }
            }
        }
        
        public void setBannerClickListener(BannerClickListener l) {
            mListener = l;
        }
        
        public boolean checkUrl(String url) {
            if (mBanner != null) {
                return url.equals(mBanner.img_url);
            }
            
            return false;
        }
        
        public boolean updateBitmapWithUrl(Bitmap bmp) {
            if (mBanner != null) {
                mImage = bmp;
                
                if (mContainer != null) {
                    mContainer.invalidate();
                }
                return true;
            }
            
            return false;
        }
        
        public void draw(Canvas canvas) {
            if (mWidth == 0 || mHeight == 0) {
                return;
            }
            
            int saved = canvas.save();
            canvas.clipPath(mClipPath);
            
            if (mImage != null) {
                mRect.set(mLeft, mTop, mRight, mBottom);
                canvas.drawBitmap(mImage, null, mRect, null);
            } else {
                canvas.drawColor(mDefColor);
            }
            
            canvas.restoreToCount(saved);
        }
        
        public final boolean onTouchEvent(MotionEvent event) {
            if (mBanner == null) {
                return false;
            }
            
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mIsDown = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mIsDown && !pointInView(event.getX(), event.getY())) {
                        mIsDown = false;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mIsDown) {
                        UserTrack.getInstance().openAd(mBanner);
                        if (mListener != null) {
                            mListener.onBannerClicked(mBanner);
                        }
                        mIsDown = false;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    mIsDown = false;
                    break;
            }
            
            return true;
        }
        
        @SuppressLint("NewApi")
		public void layout(int l, int t, int r, int b) {
            mLeft = l;
            mTop = t;
            mRight = r;
            mBottom = b;
            
            mWidth = r - l;
            mHeight = b - t;
            
            mClipPath.reset();
            try {
                mClipPath.addRoundRect(mLeft, mTop, mRight, mBottom, 12, 12, Direction.CW);
            } catch (Throwable e) {
                mClipPath.addRect(mLeft, mTop, mRight, mBottom, Direction.CW);
            }
        }
        
        public boolean pointInView(float x, float y) {
            if (x >= mLeft && x <= mRight && y >= mTop && y <= mBottom) {
                return true;
            }
            
            return false;
        }
    }
}
