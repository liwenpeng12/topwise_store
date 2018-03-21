package com.topwise.topos.appstore.view;

import com.topwise.topos.appstore.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class ListDividerItemView extends View {
    private int mDefaultHeight;
    private Drawable mImage;
    
    public ListDividerItemView(Context context) {
        this(context, null, 0);
    }
    
    public ListDividerItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public ListDividerItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Resources res = getResources();
        mDefaultHeight = res.getDimensionPixelSize(R.dimen.zkas_list_item_interval_hight);
        mImage = res.getDrawable(R.drawable.zkas_list_item_interval);
        setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
            }
        });
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        int w = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        int specSize = MeasureSpec.getSize(heightMeasureSpec);
        int h = 0;
        
        int hneed = mDefaultHeight;
        
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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        mImage.setBounds(0, 0, getWidth(), getHeight());
        mImage.draw(canvas);
    }

}
