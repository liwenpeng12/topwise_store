package com.topwise.topos.appstore.view;

import com.topwise.topos.appstore.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.ImageView;

public class PromptImageView extends ImageView {

    private int mNumber = -1;
    private TextPaint mPaint;
    private boolean mShowPrompt = false;
    private Drawable mPromptBg;
    
    public PromptImageView(Context context) {
        this(context, null, 0);
    }
    
    public PromptImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public PromptImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        
        mPaint = new TextPaint();
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Align.CENTER);
        mPaint.setColor(getResources().getColor(R.color.zkas_dlbtn_prompt_text_color));
        mPaint.setTextSize(getResources().getDimension(R.dimen.zkas_dlbtn_prompt_text_size));
        mPromptBg = getResources().getDrawable(R.drawable.zkas_prompt_number_bg);
    }

    public void showPromptNumber(int number) {
        if (number < 0) {
            return;
        }
        
        boolean needinvalidate = false;
        if (number != mNumber) {
            mNumber = number;
            needinvalidate = true;
        }
        
        if (!mShowPrompt) {
            mShowPrompt = true;
            needinvalidate = true;
        }
        
        if (needinvalidate) {
            invalidate();
        }
    }
    
    public void hidePromptNumber() {
        mShowPrompt = false;
        invalidate();
    }

    public int getPromptNumber() {
        return mNumber;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (mShowPrompt) {
            String shownumber = null;
            if (mNumber > 99) {
                shownumber = "99+";
            } else {
                shownumber = String.valueOf(mNumber);
            }
            
            FontMetrics fm = mPaint.getFontMetrics();
            
            float nw = mPaint.measureText(shownumber);
            float nh = fm.descent - fm.ascent;
            int width = getWidth();
            int height = getHeight();
            
            int bgw = mPromptBg.getIntrinsicWidth();
            int bgh = mPromptBg.getIntrinsicHeight();
            int hbgw = bgw >> 1;
            int w = (int) (nw + hbgw + 0.5f);
            if (bgw > w) {
                w = bgw;
            }
            
            float vs = (bgh - nh) / 2;
            
            mPromptBg.setBounds(width - w, 0, width, bgh);
            mPromptBg.draw(canvas);
            
            
            float x = width - (w >> 1);
            float y = bgh - vs - fm.descent;
            
            canvas.drawText(shownumber, x, y, mPaint);
        }
    }
}
