package com.topwise.topos.appstore.view;

import java.util.List;

import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.data.AppInfo;
import com.topwise.topos.appstore.view.ListMainItemView.AppItemClickListener;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class ListPopularItemView extends FrameLayout {

    public static final int MAX_APK_CNT = 3;
    
    protected PopularItemView[] mPopularItem = new PopularItemView[MAX_APK_CNT];
    protected static final int[] ITEM_IDS = {
        R.id.zkas_id_list_popular_fst_sub_item,
        R.id.zkas_id_list_popular_snd_sub_item,
        R.id.zkas_id_list_popular_trd_sub_item,
    };
    
    protected Drawable mDivider;
    
    public ListPopularItemView(Context context) {
        this(context, null, 0);
    }
    
    public ListPopularItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public ListPopularItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        
        mDivider = getResources().getDrawable(R.drawable.zkas_list_divider);
        setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
            }
        });
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        
        for (int i = 0; i < ITEM_IDS.length; i++) {
            mPopularItem[i] = (PopularItemView) findViewById(ITEM_IDS[i]);
        }
    }

    public void setAppInfos(List<AppInfo> infos) {
        if (infos == null || infos.size() == 0) {
            return;
        }
        
        int size = infos.size();
        int i = 0;
        for (; i < size && i < MAX_APK_CNT; i++) {
            AppInfo info = infos.get(i);
            mPopularItem[i].setAppInfo(info);
            mPopularItem[i].setVisibility(View.VISIBLE);
        }
        
        for (; i < MAX_APK_CNT; i++) {
            mPopularItem[i].setVisibility(View.INVISIBLE);
        }
    }
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        
        int h = mDivider.getIntrinsicHeight();
        mDivider.setBounds(0, getHeight() - h, getWidth(), getHeight());
        mDivider.draw(canvas);
    }
    
    public void setOnAppItemClickListener(AppItemClickListener l) {
        for (int i = 0; i < mPopularItem.length; i++) {
            if (mPopularItem[i] != null) {
                mPopularItem[i].setOnAppItemClickListener(l);
            }
        }
    }
    
    public void setFrom(String from) {
        for (int i = 0; i < mPopularItem.length; i++) {
            if (mPopularItem[i] != null) {
                mPopularItem[i].setFrom(from);
            }
        }
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }
}
