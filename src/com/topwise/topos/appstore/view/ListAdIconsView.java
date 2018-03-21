package com.topwise.topos.appstore.view;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.topwise.topos.appstore.conn.behavior.UserTrack;
import com.topwise.topos.appstore.data.AdIcon;
import com.topwise.topos.appstore.utils.BitmapUtil;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.utils.AsynTaskManager.ImageLoadCallBack;
import com.topwise.topos.appstore.R;

public class ListAdIconsView extends View {
    private static final String TAG = "ListAdIconsView";
    private ArrayList<IconView> mIconViews;
    private IconView mDownedSubView;
    
    private int mDefaultHeight;
    private int mDefaultIconWidth;
    private int mDefaultIconHeight;
    private TextPaint mPaint;
    
    private AdIconClickListener mListener;
    private ImageLoadCallBack mImageLoadCallBack;
    private Drawable mDefAdIcon;
    
    private boolean mIsAttached = false;
    
    public ListAdIconsView(Context context) {
        this(context, null, 0);
    }
    
    public ListAdIconsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public ListAdIconsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        
        Resources res = getResources();
        mDefaultHeight = res.getDimensionPixelOffset(R.dimen.zkas_list_adicon_height);
        mDefaultIconWidth = res.getDimensionPixelOffset(R.dimen.zkas_list_adicon_icon_width);
        mDefaultIconHeight = res.getDimensionPixelOffset(R.dimen.zkas_list_adicon_icon_height);
        mPaint = new TextPaint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(res.getDimension(R.dimen.zkas_list_adicon_text_size));
        mPaint.setColor(res.getColor(R.color.zkas_font_color_assistant));
        mPaint.setTextAlign(Align.CENTER);
        
        mImageLoadCallBack = new ImageLoadCallBack() {

            @Override
            public void onImageLoadSuccess(String imageUrl,Bitmap result) {
                if (!isAttached()) {
                    return;
                }
                
                ListAdIconsView.this.updateImageWithUrl(imageUrl, result);
            }

            @Override
            public void onImageLoadFailed(String imageUrl, String reason) {
                LogEx.d(TAG, "imageUrl=" + imageUrl + ":reason=" + reason);
            }

            @Override
            public String getCaller() {
                return String.valueOf(ListAdIconsView.this.hashCode());
            }

            @Override
            public boolean isNeedToDecode(String imageUrl) {
                if (!isAttached()) {
                    return false;
                }
                return ListAdIconsView.this.checkUrl(imageUrl);
            }
        };
        
        mDefAdIcon = res.getDrawable(R.drawable.zkas_adicon_default);
    }

    public void setAdIcons(ArrayList<AdIcon> icons) {
        if (icons == null || icons.size() == 0) {
            return;
        }
        
        if (mIconViews == null) {
            mIconViews = new ArrayList<IconView>();
        } else {
            mIconViews.clear();
        }
        
        for (AdIcon icon : icons) {
            IconView v = new IconView(icon);
            if (isAttached()) {
                v.setIconBmp(BitmapUtil.getInstance().getBitmapAsync(icon.img_url, mImageLoadCallBack));
            }
            mIconViews.add(v);
        }
        
        requestLayout();
    }
    
    public boolean checkUrl(String url) {
        if (url == null || url.length() == 0) {
            return false;
        }
        
        if (mIconViews != null) {
            int size = mIconViews.size();
            for (int i = 0; i < size; i++) {
                IconView v = mIconViews.get(i);
                if (url.equals(v.mData.img_url)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public void updateImageWithUrl(String url, Bitmap bmp) {
        if (url == null || url.length() == 0 || bmp == null) {
            return;
        }
        
        try {
            if (mIconViews != null) {
                boolean needinvalidate = false;
                for (IconView v : mIconViews) {
                    if (url.equals(v.mData.img_url)) {
                        v.setIconBmp(bmp);
                        needinvalidate = true;
                    }
                }
                
                if (needinvalidate) {
                    invalidate();
                }
            }
        } catch (Exception e) {
        }
    }
    
    public void setOnAdIconClickListener(AdIconClickListener l) {
        mListener = l;
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mIsAttached = true;
        if (mIconViews != null && mIconViews.size() > 0) {
            for (IconView v : mIconViews) {
                v.setIconBmp(BitmapUtil.getInstance().getBitmapAsync(v.mData.img_url, mImageLoadCallBack));
            }
            
            invalidate();
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIsAttached = false;
        BitmapUtil.getInstance().clearCallerCallback(mImageLoadCallBack.getCaller());
        if (mIconViews != null) {
            mIconViews.clear();
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
        
        int hneed = getPaddingTop() + mDefaultHeight;
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
        
        int paddingleft = getPaddingLeft();
        int paddingright = getPaddingRight();
        int paddingtop = getPaddingTop();
        
        int size = mIconViews.size();
        int width = getWidth();
        int iconw = (width - paddingleft - paddingright) / size;
        int iconh = getHeight() - paddingtop - getPaddingBottom();
        
        int l = paddingleft;
        int t = paddingtop;
        int r = 0;
        int b = t + iconh;
        
        for (int i = 0; i < size; i++) {
            IconView v = mIconViews.get(i);
            r = l + iconw;
            
            v.layout(l, t, r, b);
            
            l = r;
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        int action = event.getAction();
        
        if (action == MotionEvent.ACTION_DOWN) {
            int size = mIconViews.size();
            for (int i = 0; i < size; i++) {
                IconView v = mIconViews.get(i);
                if (v.pointInView(x, y)) {
                    if (v.onTouchEvent(event)) {
                        mDownedSubView = v;
                        
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
        
        int size = mIconViews.size();
        for (int i = 0; i < size; i++) {
            IconView v = mIconViews.get(i);
            v.draw(canvas);
        }
    }
    
    private class IconView {
        private AdIcon mData;
        private Bitmap mBmp;
        
        protected int mWidth = 0;
        protected int mHeight = 0;
        protected int mLeft = 0;
        protected int mRight = 0;
        protected int mTop = 0;
        protected int mBottom = 0;
        
        private Rect mSrc = new Rect();
        private Rect mDst = new Rect();
        
        private boolean mIsDown = false;
        
        public IconView(AdIcon data) {
            mData = data;
        }
        
        public void setAdIcon(AdIcon data) {
            mData = data;
        }
        
        public void setIconBmp(Bitmap bmp) {
            mBmp = bmp;
            if (mBmp != null) {
                mSrc.set(0, 0, bmp.getWidth(), bmp.getHeight());
            }
        }
        
        public void layout(int l, int t, int r, int b) {
            mLeft = l;
            mTop = t;
            mRight = r;
            mBottom = b;
            
            mWidth = r - l;
            mHeight = b - t;
            
            int spacew = (mWidth - mDefaultIconWidth) >> 1;
            if (spacew < 0) {
                spacew = 0;
            }
            
            mDst.set(l + spacew, 0, r - spacew, mDefaultIconHeight);
        }
        
        public boolean onTouchEvent(MotionEvent event) {
            if (mData == null) {
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
                        UserTrack.getInstance().openAd(mData);
                        if (mListener != null) {
                            mListener.onAdIconClicked(mData);
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
        
        public void draw(Canvas canvas) {
            if (mBmp != null) {
                canvas.drawBitmap(mBmp, mSrc, mDst, null);
            } else {
                mDefAdIcon.setBounds(mDst);
                mDefAdIcon.draw(canvas);
            }
            
            if (mData != null && mData.title != null) {
                FontMetrics fm = mPaint.getFontMetrics();
                float x = mLeft + mWidth / 2;
                float y = mBottom - fm.descent;
                
                canvas.drawText(mData.title, x, y, mPaint);
            }
        }
        
        public boolean pointInView(float x, float y) {
            if (x >= mLeft && x <= mRight && y >= mTop && y <= mBottom) {
                return true;
            }
            
            return false;
        }
    }
    
    public interface AdIconClickListener {
        public void onAdIconClicked(AdIcon icon);
    }
}
