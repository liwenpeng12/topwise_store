package com.topwise.topos.appstore.view;

import com.topwise.topos.appstore.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

public class ProgressButton extends View {

    private String mText = null;
    private int mMax = -1;
    private int mProgress = -1;
    private boolean mIsInProgress = false;
    private Drawable mProgressDrawable = null;
    private Drawable foreground = null;

    private TextPaint mPaint = null;
    private Rect mRect = new Rect();

    private int mNormalTextColor;
    private int mOpenTextColor;
    private int mProgressTextColor;

    public ProgressButton(Context context) {
        this(context, null, 0);
    }

    public ProgressButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        Resources res = getContext().getResources();

        mPaint = new TextPaint();
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Align.CENTER);

        mNormalTextColor = res.getColor(R.color.as_normal_text_color);
        mOpenTextColor = res.getColor(R.color.zkas_progress_btn_open_text_color);
        mProgressTextColor = res.getColor(R.color.zkas_progress_btn_progressing_text_color);

        float textsize = res.getDimension(R.dimen.zkas_progress_btn_normal_text_size);
        mPaint.setTextSize(textsize);
        mPaint.setColor(mNormalTextColor);

        setBackgroundResource(R.drawable.as_progress_btn_foreground);
    }

    public void setText(String text) {
        mText = text;
        if (text.equals(getContext().getString(R.string.as_listitem_download_button_open))) {
            mPaint.setColor(mOpenTextColor);
            setBackgroundResource(R.drawable.as_btn_open);
        } else {
            if (!mIsInProgress) {
                mPaint.setColor(mNormalTextColor);
                setBackgroundResource(R.drawable.as_progress_btn_foreground);
            }
        }
        invalidate();
    }

    public void setText(int id) {
        String text = null;
        try {
            text = getContext().getResources().getString(id);
        } catch (Exception e) {
        }
        setText(text);
    }

    public void setProgress(int progress) {
        if (mProgress != progress) {
            if (progress < 0) {
                progress = 0;
            } else if (progress > mMax) {
                progress = mMax;
            }

            mProgress = progress;
            invalidate();
        }
    }

    public int getProgress() {
        return mProgress;
    }

    public void setMax(int max) {
        if (max < 0) {
            return;
        }

        if (mMax != max) {
            mMax = max;
            initProgressState();
        }
    }

    public void removeProgressState() {
        mMax = -1;
        mProgress = -1;

        if (mIsInProgress) {
            mIsInProgress = false;

            mPaint.setColor(mNormalTextColor);
            setBackgroundResource(R.drawable.as_progress_btn_foreground);
            mProgressDrawable = null;
            foreground = null;
        }
    }

    private void initProgressState() {
        if (!mIsInProgress) {
            mIsInProgress = true;

            mPaint.setColor(mProgressTextColor);
            setBackgroundResource(R.drawable.as_progress_btn_foreground);
            mProgressDrawable = getResources().getDrawable(R.drawable.zkas_progress_btn_bg_normal);
            foreground = getResources().getDrawable(R.drawable.as_progress_btn_foreground);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if (mIsInProgress) {
            int pw = (width - 1 * 2) * mProgress / mMax;
            if (pw > 0) {
                mRect.set(1, 0, pw, height);
                mProgressDrawable.setBounds(mRect);
                mProgressDrawable.draw(canvas);
            }
        }

        if (mText != null && mText.length() > 0) {
            FontMetrics fm = mPaint.getFontMetrics();
            float space = ((float) height - (fm.descent - fm.ascent)) / 2;
            float x = ((float) width) / 2;
            float y = height - space - fm.descent;

            canvas.drawText(mText, x, y, mPaint);
        }

        if (mIsInProgress) {
            mRect.set(0, 0, width, height);
            foreground.setBounds(mRect);
            foreground.draw(canvas);
        }

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        removeProgressState();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
