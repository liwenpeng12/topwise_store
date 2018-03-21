package com.topwise.topos.appstore.view.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.data.AppInfo;
import com.topwise.topos.appstore.data.AppInfo.Tag;
import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.manager.ActivityManager;
import com.topwise.topos.appstore.manager.AppManager;
import com.topwise.topos.appstore.manager.ManagerCallback;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.utils.Utils;
import com.topwise.topos.appstore.view.ActionBarView;
import com.topwise.topos.appstore.view.ActionBarView.BackButtonClickListener;
import com.topwise.topos.appstore.view.ActionBarView.TabClickListener;
import com.topwise.topos.appstore.view.ActionBarView.ZTab;
import com.topwise.topos.appstore.view.ListAppItemView;
import com.topwise.topos.appstore.view.ListMainItemView.AppItemClickListener;
import com.topwise.topos.appstore.view.fragment.AppDetailFragment;
import com.topwise.topos.appstore.view.fragment.AppRelatedFragment;
import com.umeng.analytics.MobclickAgent;

public class AppDetailActivity extends FragmentActivity implements OnClickListener{
    private static final String TAG = "AppDetailActivity";
    private LinearLayout mItemContainer;
    private LayoutInflater mInflater;
    private ListAppItemView mListAppItemView;
    private ActionBarView mActionBarView;
    private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private TextView mNoAppText;
    private Fragment mAppdetailFragment;
    private Fragment mRelatedFragment;
    private AppInfo mAppInfo;
    private String mAppId;
    private static final int[] TITLE_RES_ID = {
            R.string.as_detail_tab_detail, R.string.as_detail_tab_related
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.addActivity(this);
        Utils.setWhiteStatusBar(this);
        setContentView(R.layout.as_activity_appdetail);
        mActionBarView = (ActionBarView) this.findViewById(R.id.as_action_bar_layout);
        mActionBarView.setTitle(R.string.as_settings_actionbar_title);
        mActionBarView.setOnBackButtonClickListener(new BackButtonClickListener() {
            @Override
            public void onBackBtnClicked(View v) {
                finish();
            }
        });

        mInflater = LayoutInflater.from(this);
        mItemContainer = (LinearLayout)this. findViewById(R.id.zkas_action_bar_list_item);
        mNoAppText = (TextView) findViewById(R.id.no_app);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                loadAppInfo();
            }
        },100);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.removeActivity(this);
    }

    private void initDefalutDetailTag(){
        Tag secTag = new Tag();
        secTag.name = getString(R.string.as_detail_tab_tag_sec);
        secTag.txtcolor = Color.WHITE;
        secTag.bgcolor = getResources().getColor(R.color.zkas_sec_free_tag_color);
        mListAppItemView.addAppTag(secTag);
        
        Tag freeTag = new Tag();
        freeTag.name = getString(R.string.as_detail_tab_tag_free);
        freeTag.txtcolor = Color.WHITE;
        freeTag.bgcolor = getResources().getColor(R.color.zkas_sec_free_tag_color);
        mListAppItemView.addAppTag(freeTag);
    }
    
    private void loadAppInfo(){
        Intent intent = getIntent();
        mAppId = intent.getStringExtra("app_id");
        if (mAppId == null || mAppId.length() == 0) {
            Uri uri = intent.getData();
            if (uri != null) {
                String uriString = uri.toString();
                if (uriString.contains("ibimuyuappstore.com")) {
                    mAppId = uriString.substring(uriString.indexOf("pkg=") + 4, uriString.length());
                } else if (uriString.contains("market://details")) {
                    mAppId = uriString.substring(uriString.indexOf("id=") + 3, uriString.length());
                }
            }
        }
        LogEx.d(mAppId);
        mAppInfo = DataPool.getInstance().getAppInfo(mAppId);
        if(mAppInfo == null){
            mAppInfo = new AppInfo();
        }
        mAppInfo.id = mAppId;
        AppManager.getInstance().loadAppDetail(mAppInfo, mAppDetailCallback);
    }

    private ManagerCallback mAppDetailCallback = new ManagerCallback() {
        @Override
        public void onSuccess(String moduleType, int dataType, int page, int num, boolean end) {
            mAppInfo = DataPool.getInstance().getAppInfo(mAppId);
            mNoAppText.setVisibility(View.GONE);
            initAppItem();
            initViewpager();
            initTabViews();
        }

        @Override
        public void onFailure(String moduleType, int dataType, Throwable t, int errorNo, String strMsg) {
            LogEx.e(TAG, "loadAppInfo : errorNo "+errorNo + " strMsg= " + strMsg +  " throwable =" + t);
            if (strMsg != null && strMsg.contains("host must not be null")) {
                AppManager.getInstance().loadAppDetail(mAppInfo, mAppDetailCallback);
            } else if ("Exception".equals(strMsg)) {
                mNoAppText.setVisibility(View.VISIBLE);
            } else {
                initAppItem();
                initViewpager();
                initTabViews();
            }
        }
    };
    
    private void initAppItem(){
        View line = mInflater.inflate(R.layout.line2,mItemContainer,false);
        mItemContainer.addView(line);

        mListAppItemView = (ListAppItemView) mInflater.inflate(R.layout.zkas_list_item_universal_layout, mItemContainer, false);
        mItemContainer.addView(mListAppItemView,1);
        mListAppItemView.setDividerVisibility(false);

        mListAppItemView.setAppInfo(mAppInfo);
        mListAppItemView.setOnAppItemClickListener(new AppItemClickListener() {
            
            @Override
            public void onAppItemClicked(AppInfo info) {
            }
        });
        
        mListAppItemView.post(new Runnable() {
            
            @Override
            public void run() {
                initDefalutDetailTag();
            }
        });  
        mListAppItemView.setFrom("详情");
    }
    
    private void initTabViews() {
        for(int i = 0;i < TITLE_RES_ID.length;i++){
            ZTab tab = new ZTab((String) getResources().getString(TITLE_RES_ID[i]));
            mActionBarView.addTab(tab);
        }
        mActionBarView.setOnTabClickListener(new TabClickListener() {
            
            @Override
            public void onTabClicked(ZTab t, int index) {
                mViewPager.setCurrentItem(index);
            }
        });
        mActionBarView.setSelectedTab(0);
        mViewPager.setCurrentItem(0);
        mPagerAdapter.notifyDataSetChanged();
    }
    
    private void initViewpager(){
        ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                mActionBarView.setSelectedTab(position);
                super.onPageSelected(position);
            }

        };
        
        mViewPager = (ViewPager) findViewById(R.id.appdetail_pager);
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(pageChangeListener);
    }
    
    private class PagerAdapter  extends  FragmentPagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    if (mAppdetailFragment == null) {
                        mAppdetailFragment = new AppDetailFragment();
                        ((AppDetailFragment)mAppdetailFragment).setAppInfo(mAppInfo);
                    }
                    return  mAppdetailFragment;
                case 1:
                    if (mRelatedFragment == null) {
                        mRelatedFragment = new AppRelatedFragment();
                        ((AppRelatedFragment)mRelatedFragment).setAppInfo(mAppInfo);
                    }
                    return  mRelatedFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return TITLE_RES_ID.length;
        }
    }

    @Override
    public void onClick(View v) {
        if(v == mListAppItemView){
            
        }
    }
}
