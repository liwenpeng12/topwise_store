package com.topwise.topos.appstore.view.activity;

import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.manager.ActivityManager;
import com.topwise.topos.appstore.utils.Utils;
import com.topwise.topos.appstore.view.ActionBarView;
import com.topwise.topos.appstore.view.ActionBarView.BackButtonClickListener;
import com.topwise.topos.appstore.view.ActionBarView.TabClickListener;
import com.topwise.topos.appstore.view.ActionBarView.ZTab;
import com.topwise.topos.appstore.view.fragment.AppListFragment;
import com.topwise.topos.appstore.view.fragment.LabelFragment;
import com.topwise.topos.appstore.view.fragment.MainListFragment;
import com.topwise.topos.appstore.view.fragment.OneKeyInstallFragment;
import com.topwise.topos.appstore.view.fragment.RankFragment;
import com.umeng.analytics.MobclickAgent;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

public class GroupActivity extends FragmentActivity {

    private ActionBarView mActionbar;
    private ViewPager mViewPager;
    private ViewGroup mSingleFragmentContainer;
    
    private String[] mTabNames;
    private int[] mIds;
    
    private static final String TAG_LABEL_FRAGMENT = "LabelFragment";
    private static final String TAG_RANK_FRAGMENT = "RankFragment";
    private static final String TAG_APP_FRAGMENT = "AppListFragment";
    private static final String TAG_PAGE_FRAGMENT = "PageFragment";
    private static final String TAG_ONEKEY_FRAGMENT = "OneKeyInstallFragment";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.addActivity(this);
        Utils.setWhiteStatusBar(this);
        setContentView(R.layout.zkas_group_list_fragment_layout);
        mActionbar = (ActionBarView) findViewById(R.id.as_action_bar_layout);
        mViewPager = (ViewPager) findViewById(R.id.zkas_id_group_viewpager);
        mSingleFragmentContainer = (ViewGroup) findViewById(R.id.zkas_id_group_fragment_container);
        
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        mActionbar.setTitle(title);
        mActionbar.setOnBackButtonClickListener(new BackButtonClickListener() {
            @Override
            public void onBackBtnClicked(View v) {
                GroupActivity.this.finish();
            }
        });
        
        String necessary = getString(R.string.as_necessary);
        if (title.equals(necessary)) {
//            TopMenu menu = new TopMenu(getString(R.string.as_onekey_install_all));
//            mActionbar.addTopMenu(menu);
//            mActionbar.setOnTopMenuClickListener(new TopMenuClickListener() {
//
//                @Override
//                public void onTopMenuClicked(TopMenu menu) {
//                    String title = menu.getText();
//                    Intent intent = new Intent(GroupActivity.this, GroupActivity.class);
//                    intent.putExtra("title", title);
//                    startActivity(intent);
//                }
//            });
        }
        
        String type = intent.getStringExtra("type");
        mIds = intent.getIntArrayExtra("id");
        mTabNames = intent.getStringArrayExtra("tabname");
        
        FragmentManager fm = getSupportFragmentManager();
        if (type != null && mIds != null && mIds.length > 0) {
            if (type.equals("label")) {
                mSingleFragmentContainer.setVisibility(View.VISIBLE);
                FragmentTransaction ft = fm.beginTransaction();
                LabelFragment f = new LabelFragment();
                f.setLabelId(mIds[0]);
                f.setFrom(title);
                ft.replace(R.id.zkas_id_group_fragment_container, f, TAG_LABEL_FRAGMENT);
                ft.commitAllowingStateLoss();
            } else if (type.equals("rank")) {
                mSingleFragmentContainer.setVisibility(View.VISIBLE);
                FragmentTransaction ft = fm.beginTransaction();
                RankFragment f = new RankFragment();
                f.setRankId(mIds[0]);
                f.setFrom(title);
                ft.replace(R.id.zkas_id_group_fragment_container, f, TAG_RANK_FRAGMENT);
                ft.commitAllowingStateLoss();
            } else if (type.equals("type")) {
                mSingleFragmentContainer.setVisibility(View.VISIBLE);
                FragmentTransaction ft = fm.beginTransaction();
                AppListFragment f = new AppListFragment();
                f.setType(mIds[0]);
                f.setFrom(title);
                ft.replace(R.id.zkas_id_group_fragment_container, f, TAG_APP_FRAGMENT);
                ft.commitAllowingStateLoss();
            } else if (type.equals("page")) {
                if (mIds.length > 1) {
                    mViewPager.setVisibility(View.VISIBLE);
                    mViewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
                    
                    for (int i = 0; i < mIds.length; i++) {
                        String tabn = i < mTabNames.length ? mTabNames[i] : "";
                        ActionBarView.ZTab t = new ActionBarView.ZTab(tabn);
                        mActionbar.addTab(t);
                    }
                    
                    mActionbar.setOnTabClickListener(new TabClickListener() {
                        
                        @Override
                        public void onTabClicked(ZTab t, int index) {
                            mViewPager.setCurrentItem(index);
                        }
                    });
                    
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
                            mActionbar.setSelectedTab(position);
                            super.onPageSelected(position);
                        }
                    };
                    mViewPager.setOnPageChangeListener(mPageChangeListener);
                    mActionbar.setSelectedTab(0);
                } else {
                    mSingleFragmentContainer.setVisibility(View.VISIBLE);
                    FragmentTransaction ft = fm.beginTransaction();
                    MainListFragment f = new MainListFragment();
                    f.setFrom(title);
                    f.setPageId(mIds[0]);
                    ft.replace(R.id.zkas_id_group_fragment_container, f, TAG_PAGE_FRAGMENT);
                    ft.commitAllowingStateLoss();
                }
            }
        } else if (title.equals(getString(R.string.as_onekey_install_all))) {
            mSingleFragmentContainer.setVisibility(View.VISIBLE);
            FragmentTransaction ft = fm.beginTransaction();
            MainListFragment f = new OneKeyInstallFragment();
            f.setFrom(title);
            ft.replace(R.id.zkas_id_group_fragment_container, f, TAG_ONEKEY_FRAGMENT);
            ft.commitAllowingStateLoss();
        }
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
    
    private class PagerAdapter extends FragmentPagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            MainListFragment f = new MainListFragment();
            f.setPageId(mIds[position]);
            f.setFrom(getPageTitle(position).toString());
            return f;
        }

        @Override
        public int getCount() {
            return mIds.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return position < mTabNames.length ? mTabNames[position] : ("" + position);
        }
    }
}
