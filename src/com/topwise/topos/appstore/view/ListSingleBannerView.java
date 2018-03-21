package com.topwise.topos.appstore.view;

import com.topwise.topos.appstore.conn.behavior.UserTrack;
import com.topwise.topos.appstore.data.Banner;
import com.topwise.topos.appstore.utils.BitmapUtil;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.utils.AsynTaskManager.ImageLoadCallBack;
import com.topwise.topos.appstore.view.ListScrollBannerView.BannerClickListener;
import com.topwise.topos.appstore.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

public class ListSingleBannerView extends View {
    private static final String TAG = "ListSingleBannerView";
    private Banner mBanner;
    private Bitmap mBannerImage;
    private Paint mDefColorPaint;
    
    private TextPaint mTitlePaint;
    private TextPaint mDescripPaint;
    private BannerClickListener mBannerClickListener;
    
    private int mTitleAreaHeight;
    private int mBannerImageHeight;
    private int mDescriptionAreaHeight;
    private int mTextHorzPadding;
    
    private Rect mDst = new Rect();
    private ImageLoadCallBack mImageLoadCallBack;
    private boolean mIsAttached = false;
    
    public ListSingleBannerView(Context context) {
        this(context, null, 0);
    }
    
    public ListSingleBannerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public ListSingleBannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        
        Resources res = getResources();
        mTitleAreaHeight = res.getDimensionPixelSize(R.dimen.zkas_list_item_title_hight);
        mBannerImageHeight = res.getDimensionPixelSize(R.dimen.as_top_banner_scroll_area_height);
        mDescriptionAreaHeight = res.getDimensionPixelSize(R.dimen.zkas_banner_description_height);
        mTextHorzPadding = res.getDimensionPixelSize(R.dimen.zkas_list_item_horizontal_padding);
        
        mTitlePaint = new TextPaint();
        mTitlePaint.setAntiAlias(true);
        mTitlePaint.setTextAlign(Align.LEFT);
        mTitlePaint.setTextSize(res.getDimension(R.dimen.zkas_list_item_title_text_size));
        mTitlePaint.setColor(res.getColor(R.color.zkas_font_color_assistant));
        
        mDescripPaint = new TextPaint();
        mDescripPaint.setAntiAlias(true);
        mDescripPaint.setTextAlign(Align.LEFT);
        mDescripPaint.setTextSize(res.getDimension(R.dimen.zkas_banner_descrip_text_size));
        mDescripPaint.setColor(res.getColor(R.color.zkas_font_color_assistant));
        
        int dfcolor = res.getColor(R.color.zkas_banner_default_color);
        mDefColorPaint = new Paint();
        mDefColorPaint.setColor(dfcolor);
        
        setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (mBanner != null) {
                    UserTrack.getInstance().openAd(mBanner);
                    if (mBannerClickListener != null) {
                        mBannerClickListener.onBannerClicked(mBanner);
                    } 
                }
            }
        });
        
        mImageLoadCallBack = new ImageLoadCallBack() {

            @Override
            public void onImageLoadSuccess(String imageUrl,Bitmap result) {
                if (!isAttached()) {
                    return;
                }
                
                ListSingleBannerView.this.updateImageWithUrl(result);
            }

            @Override
            public void onImageLoadFailed(String imageUrl, String reason) {
                LogEx.d(TAG, "imageUrl=" + imageUrl + ":reason=" + reason);
            }

            @Override
            public String getCaller() {
                return String.valueOf(ListSingleBannerView.this.hashCode());
            }

            @Override
            public boolean isNeedToDecode(String imageUrl) {
                if (!isAttached()) {
                    return false;
                }
                return ListSingleBannerView.this.checkUrl(imageUrl);
            }
        };
    }

    public void setBanner(Banner banner) {
        mBanner = banner;
        
        if (isAttached()) {
            mBannerImage = BitmapUtil.getInstance().getBitmapAsync(mBanner.img_url, mImageLoadCallBack);
            invalidate();
        } else {
            mBannerImage = null;
        }
    }
    
    public void updateImageWithUrl(Bitmap bmp) {
        if (mBanner != null) {
            mBannerImage = bmp;
            invalidate();
        }
    }
    
    public boolean checkUrl(String url) {
        if (url == null || url.length() == 0) {
            return false;
        }
        
        if (mBanner != null && url.equals(mBanner.img_url)) {
            return true;
        }
        return false;
    }
    
    public void setBannerClickListener(BannerClickListener listener) {
        mBannerClickListener = listener;
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mIsAttached = true;
        if (mBanner != null) {
            mBannerImage = BitmapUtil.getInstance().getBitmapAsync(mBanner.img_url, mImageLoadCallBack);
            if (mBannerImage != null) {
                invalidate();
            }
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIsAttached = false;
        BitmapUtil.getInstance().clearCallerCallback(mImageLoadCallBack.getCaller());
        mBannerImage = null;
        mBanner = null;
    }
    
    public boolean isAttached() {
        return mIsAttached;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        if (mBanner != null) {
            int w = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            
            int specMode = MeasureSpec.getMode(heightMeasureSpec);
            int specSize = MeasureSpec.getSize(heightMeasureSpec);
            int h = 0;
            
            int hneed = getPaddingTop();
            if (mBanner.title != null && mBanner.title.length() > 0) {
                hneed += mTitleAreaHeight;
            }
            
            if (mBanner.desc != null && mBanner.desc.length() > 0) {
                hneed += mDescriptionAreaHeight;
            }
            hneed += mBannerImageHeight;
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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int areastart = 0;
        if (mBanner.title != null && mBanner.title.length() > 0) {
            FontMetrics fm = mTitlePaint.getFontMetrics();
            float x = getPaddingLeft() + mTextHorzPadding;
            float space = (mTitleAreaHeight - (fm.descent - fm.ascent)) / 2;
            float y = mTitleAreaHeight - space - fm.descent;
            canvas.drawText(mBanner.title, x, y, mTitlePaint);
            
            areastart += mTitleAreaHeight;
        }
        
        if (mBannerImage != null) {
            mDst.set(0, areastart, getWidth(), areastart + mBannerImageHeight);
            canvas.drawBitmap(mBannerImage, null, mDst, null);
            
            areastart += mBannerImageHeight;
        } else {
            mDst.set(0, areastart, getWidth(), areastart + mBannerImageHeight);
            canvas.drawRect(mDst, mDefColorPaint);
            areastart += mBannerImageHeight;
        }
        
        if (mBanner.desc != null && mBanner.desc.length() > 0) {
            FontMetrics fm = mDescripPaint.getFontMetrics();
            float x = getPaddingLeft() + mTextHorzPadding;
            float space = (mDescriptionAreaHeight - (fm.descent - fm.ascent)) / 2;
            float y = areastart + mDescriptionAreaHeight - space - fm.descent;
            float textWidth = mDescripPaint.measureText(mBanner.desc);
            float maxWidth = getResources().getDisplayMetrics().widthPixels - 24;
            if (textWidth > maxWidth) {
                int subIndex = mDescripPaint.breakText(mBanner.desc, 0, mBanner.desc.length(), true, maxWidth, null);
                String text = mBanner.desc.substring(0, subIndex - 3) + "...";
                canvas.drawText(text, x, y, mDescripPaint);
            } else {
                canvas.drawText(mBanner.desc, x, y, mDescripPaint);
            }
        }
    }
}
