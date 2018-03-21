package com.topwise.topos.appstore.view.activity;


import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.manager.AppManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.manager.ActivityManager;

import com.topwise.topos.appstore.utils.Utils;
import com.topwise.topos.appstore.view.ActionBarView;
import com.topwise.topos.appstore.view.ActionBarView.BackButtonClickListener;
import com.topwise.topos.appstore.view.ActionBarView.TabClickListener;
import com.topwise.topos.appstore.view.ActionBarView.TopMenu;
import com.topwise.topos.appstore.view.ActionBarView.TopMenuClickListener;
import com.topwise.topos.appstore.view.ActionBarView.ZTab;
import com.topwise.topos.appstore.view.fragment.DownloadTaskFragment;
import com.topwise.topos.appstore.view.fragment.DownloadUpdateFragment;
import com.umeng.analytics.MobclickAgent;

public class DownloadActivity extends FragmentActivity {

    private static int[] TAB_RES_DOWNLOAD = new int[]{
            R.string.sa_tab_download, R.string.sa_tab_update
    };
    private PagerAdapter mPagerAdapter = null;
    private ActionBarView mActionbarView = null;
    private ViewPager mViewPager;

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.addActivity(this);
        Utils.setWhiteStatusBar(this);
        setContentView(R.layout.zkas_download_activity_layout);

        AppManager.getInstance().loadNeedUpgradeApps();

        mActionbarView = (ActionBarView) findViewById(R.id.as_action_bar_layout);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mActionbarView.setTitle(getString(R.string.as_manger));
        mActionbarView.setOnBackButtonClickListener(new BackButtonClickListener() {
            @Override
            public void onBackBtnClicked(View v) {
                DownloadActivity.this.finish();
            }
        });

        initTabViews();

        ViewPager.SimpleOnPageChangeListener mPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {

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
                mActionbarView.setSelectedTab(position);
                super.onPageSelected(position);
            }
        };

        mViewPager.setOnPageChangeListener(mPageChangeListener);

        TopMenu menu = new TopMenu(this.getResources().getDrawable(R.drawable.zkas_setting_btn_bg));
        mActionbarView.addTopMenu(menu);
        mActionbarView.setPadding(menu, 0, 0, (int) Utils.dp2px(AppStoreWrapperImpl.getInstance().getAppContext(), 5f), 0);
        mActionbarView.setOnTopMenuClickListener(new TopMenuClickListener() {

            @Override
            public void onTopMenuClicked(TopMenu menu) {
                Intent intent = new Intent(DownloadActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });


    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        if (getIntent().getIntExtra("mode", 0) == 0) {
            mActionbarView.setSelectedTab(0);
            mViewPager.setCurrentItem(0);
        }
    }

    ;

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


    private void initTabViews() {
        int cnt = mPagerAdapter.getCount();
        for (int i = 0; i < cnt; i++) {
            ZTab tab = new ZTab((String) mPagerAdapter.getPageTitle(i));
            mActionbarView.addTab(tab);
        }
        mActionbarView.setOnTabClickListener(new TabClickListener() {

            @Override
            public void onTabClicked(ZTab t, int index) {
                if (mViewPager != null) {
                    mViewPager.setCurrentItem(index);
                }
            }
        });

        if (getIntent().getIntExtra("mode", 0) == 0) {
            mActionbarView.setSelectedTab(0);
            mViewPager.setCurrentItem(0);
        } else {
            mActionbarView.setSelectedTab(1);
            mViewPager.setCurrentItem(1);
        }
        mPagerAdapter.notifyDataSetChanged();
    }


    private class PagerAdapter extends FragmentPagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return DownloadTaskFragment.getInstance();
                case 1:
                    return DownloadUpdateFragment.getInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            return TAB_RES_DOWNLOAD.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(TAB_RES_DOWNLOAD[position]);
        }
    }


    public static interface RecommendPageListenter {
        public void goToRecommendPage();
    }


}
