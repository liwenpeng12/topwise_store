package com.topwise.topos.appstore.view;

import com.topwise.topos.appstore.R;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

public class ListMoreItemView extends TextView {

    private MoreItemClickListener mMoreItemClickListener;
    private int mDefaultHeight;
    private String mType;
    private String mGroupId;
    private String mTitle;
    
    public ListMoreItemView(Context context) {
        this(context, null, 0);
    }
    
    public ListMoreItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public ListMoreItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        
        Resources res = getResources();
        setText(R.string.as_list_more);
        setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimension(R.dimen.zkas_list_item_more_text_size));
        setTextColor(res.getColor(R.color.zkas_list_item_more_text_color));
        setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.zkas_icon_arrow, 0);
        setCompoundDrawablePadding(res.getDimensionPixelSize(R.dimen.zkas_list_item_more_text_icon_margin));
        setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (mMoreItemClickListener != null && mType != null && mGroupId != null) {
                    mMoreItemClickListener.onMoreItemClicked(mType, mGroupId, mTitle);
                }
            }
        });
        setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        int padding = res.getDimensionPixelSize(R.dimen.zkas_list_item_horizontal_padding);
        setPadding(padding, 0, padding, 0);
        mDefaultHeight = res.getDimensionPixelSize(R.dimen.zkas_list_item_more_hight);
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
    
    public interface MoreItemClickListener {
        public void onMoreItemClicked(String type, String id, String title);
    }
    
    public void setOnMoreItemClickListener(MoreItemClickListener l) {
        mMoreItemClickListener = l;
    }
    
    public void setGroupId(String type, String id, String title) {
        mType = type;
        mGroupId = id;
        mTitle = title;
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mGroupId = null;
        mType = null;
    }
}
