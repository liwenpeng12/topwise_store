package com.topwise.topos.appstore.view;

import com.topwise.topos.appstore.R;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

public class ListLableTitleView extends TextView {

    public ListLableTitleView(Context context) {
        this(context, null, 0);
    }
    
    public ListLableTitleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public ListLableTitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        
        Resources res = getResources();
        int textsize = res.getDimensionPixelSize(R.dimen.zkas_list_item_title_text_size);
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
        
        int color = res.getColor(R.color.zkas_list_item_title_text_color);
        setTextColor(color);
        
        setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        setSingleLine(true);
        setEllipsize(TruncateAt.END);
        
        int horzPadding = res.getDimensionPixelSize(R.dimen.zkas_list_item_title_horz_padding);
        setPadding(horzPadding, 0, horzPadding, 0);
        setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                
            }
        });
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        
        LayoutParams lp = getLayoutParams();
        lp.width = LayoutParams.MATCH_PARENT;
        lp.height = getResources().getDimensionPixelSize(R.dimen.zkas_list_item_title_hight);
        setLayoutParams(lp);
    }

}
