package com.topwise.topos.appstore.view;

import java.util.List;

import com.topwise.topos.appstore.data.AppInfo;
import com.topwise.topos.appstore.view.CheckPopularItemView.OnCheckedChangeListener;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class CheckListPopularItemView extends ListPopularItemView {

    public CheckListPopularItemView(Context context) {
        this(context, null, 0);
    }
    
    public CheckListPopularItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public CheckListPopularItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAppInfos(List<AppInfo> infos, List<Boolean> checkstates) {
        if (infos == null || infos.size() == 0 || checkstates == null || checkstates.size() != infos.size()) {
            return;
        }
        
        int size = infos.size();
        int i = 0;
        for (; i < size && i < MAX_APK_CNT; i++) {
            AppInfo info = infos.get(i);
            mPopularItem[i].setAppInfo(info);
            mPopularItem[i].setVisibility(View.VISIBLE);
            boolean checked = checkstates.get(i);
            ((CheckPopularItemView)mPopularItem[i]).setChecked(checked);
        }
        
        for (; i < MAX_APK_CNT; i++) {
            mPopularItem[i].setVisibility(View.INVISIBLE);
        }
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener l) {
        for (int i = 0; i < MAX_APK_CNT; i++) {
            ((CheckPopularItemView)mPopularItem[i]).setOnCheckedChangeListener(l);
        }
    }
}
