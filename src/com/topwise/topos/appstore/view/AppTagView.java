package com.topwise.topos.appstore.view;

import com.topwise.topos.appstore.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Path;
import android.graphics.Paint.FontMetricsInt;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

@SuppressLint({ "NewApi", "ResourceAsColor" })
public class AppTagView extends View {

    private String mText;
    private int mBgColor;
    
    private TextPaint mPaint;
    private Path mClipPath = new Path();
    
    public AppTagView(Context context) {
        this(context, null, 0);
    }
    
    public AppTagView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public AppTagView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new TextPaint();
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Align.CENTER);
        mPaint.setTextSize(getResources().getDimension(R.dimen.zkas_list_item_app_tag_text_size));
        mPaint.setColor(Color.WHITE);
        setPadding(6, 2, 6, 2);
    }
    
    public void setText(String text) {
        mText = text;
        
        requestLayout();
    }
    
    public void setTextColor(int color) {
        mPaint.setColor(color);
        invalidate();
    }
    
    @Override
    public void setBackgroundColor(int color) {
        mBgColor = color;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        if (mText != null && mText.length() > 0) {
        
            FontMetricsInt fm = mPaint.getFontMetricsInt();
            
            int specModeH = MeasureSpec.getMode(heightMeasureSpec);
            int specSizeH = MeasureSpec.getSize(heightMeasureSpec);
            int h = 0;
            
            int hneed = getPaddingTop() + getPaddingBottom() + (fm.descent - fm.ascent);
            switch (specModeH) {
            case MeasureSpec.UNSPECIFIED:
                h = getSuggestedMinimumHeight();
                h = hneed > h ? hneed : h;
                break;
            case MeasureSpec.AT_MOST:
                if (hneed > specSizeH) {
                    hneed = specSizeH;
                }
                int minh = getSuggestedMinimumHeight();
                h = hneed > minh ? hneed : minh;
                break;
            case MeasureSpec.EXACTLY:
                h = specSizeH;
                break;
            }
            
            int specModeW = MeasureSpec.getMode(widthMeasureSpec);
            int specSizeW = MeasureSpec.getSize(widthMeasureSpec);
            int w = 0;
            
            int wneed = (int) mPaint.measureText(mText) + getPaddingLeft() + getPaddingRight();
            switch (specModeW) {
            case MeasureSpec.UNSPECIFIED:
                w = getSuggestedMinimumWidth();
                w = wneed > w ? wneed : w;
                break;
            case MeasureSpec.AT_MOST:
                if (wneed > specSizeW) {
                    wneed = specSizeW;
                    setVisibility(View.GONE);
                }
                int minw = getSuggestedMinimumWidth();
                w = wneed > minw ? wneed : minw;
                break;
            case MeasureSpec.EXACTLY:
                w = specSizeW;
                if (w < wneed) {
                    setVisibility(View.GONE);
                }
                break;
            }
            
            setMeasuredDimension(w, h);
        }
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mClipPath.reset();
            try {
                mClipPath.addRoundRect(0, 0,  getWidth(), getHeight(), 8, 8, Path.Direction.CW);
            } catch (Throwable e) {
                mClipPath.addRect(0, 0,  getWidth(), getHeight(), Path.Direction.CW);
            }
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int save = canvas.save();
        
        canvas.clipPath(mClipPath);
        canvas.drawColor(mBgColor);
        
        if (mText != null && mText.length() > 0) {
            FontMetrics fm = mPaint.getFontMetrics();
            float x = getPaddingLeft() + (getWidth() - getPaddingLeft() - getPaddingRight()) / 2;
            float y = getHeight() - getPaddingBottom() - fm.descent;
            canvas.drawText(mText, x, y, mPaint);
        }
        
        canvas.restoreToCount(save);
    }
    
}
