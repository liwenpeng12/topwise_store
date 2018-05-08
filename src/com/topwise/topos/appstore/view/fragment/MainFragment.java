package com.topwise.topos.appstore.view.fragment;

import java.util.ArrayList;

import com.topwise.topos.appstore.conn.protocol.Protocol;
import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.data.Hotword;
import com.topwise.topos.appstore.utils.MyStatusBarUtil;
import com.topwise.topos.appstore.utils.Utils;
import com.topwise.topos.appstore.view.activity.DownloadActivity;
import com.topwise.topos.appstore.manager.ManagerCallback;
import com.topwise.topos.appstore.manager.SearchManager;
import com.topwise.topos.appstore.utils.AsynTaskManager;
import com.topwise.topos.appstore.utils.BitmapUtil;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.utils.Properties;
import com.topwise.topos.appstore.view.PromptImageView;
import com.topwise.topos.appstore.view.activity.H5Activity;
import com.topwise.topos.appstore.view.activity.SearchActivity;
import com.topwise.topos.appstore.view.widget.MyViewPager;
import com.topwise.topos.appstore.view.widget.MyViewPager.OnPageChangeListener;
import com.topwise.topos.appstore.AppStoreWrapper.AppUpgradeCountListener;
import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.R;
import com.umeng.analytics.MobclickAgent;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainFragment extends BaseFragment  implements AppUpgradeCountListener {
    private static final String TAG = "MainFragment";
    private ViewGroup mRootView;
    private PromptImageView mDwldBtn;
    private MyViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private ManagerCallback mManagerCallback;

    private static final int HOT_WORD_CHANGE_DELAY = 5000;

    private TextView mTabTextViews[] = new TextView[3];
    private static final int TITLE_RES_IDS[] = {
            R.string.as_tab_recommend_text, R.string.as_tab_software_text, R.string.as_tab_game_text,
    };

    private ImageView[] mTabImageViews = new ImageView[5];
    private static int[] TAB_RES_IDS = new int[]{
            R.drawable.as_tab_recommend, R.drawable.as_tab_app, R.drawable.as_tab_game, R.drawable.as_tab_rank, R.drawable.as_tab_news
    };
    private static int[] TAB_RES_HIGHLIGHT_IDS = new int[]{
            R.drawable.as_tab_recommend_selected, R.drawable.as_tab_app_selected, R.drawable.as_tab_game_selected, R.drawable.as_tab_rank_selected, R.drawable.as_tab_news_selected
    };

    private String mHotwordPrefix;
    private TextView mSearchView;
    private ArrayList<String> mHotwords;
    private int mHotwordIndex = -1;
    private Handler mMainThreadHandler;

    private LinearLayout mTopAreaLayout;
    private View line;



    public MainFragment() {
        super();

        mHotwords = new ArrayList<String>();
        mMainThreadHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                int size = mHotwords.size();
                if (size > 0) {
                    mHotwordIndex++;
                    mHotwordIndex %= size;

                    String hw = mHotwords.get(mHotwordIndex);
                    mSearchView.setText(mHotwordPrefix + " " + hw);

                    sendEmptyMessageDelayed(0, HOT_WORD_CHANGE_DELAY);
                }
            }
        };

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mRootView = (ViewGroup) inflater.inflate(R.layout.as_fragment_main, container, false);

        mTopAreaLayout = (LinearLayout) mRootView.findViewById(R.id.top_area);
        line = mRootView.findViewById(R.id.line);

        mSearchView = (TextView) mRootView.findViewById(R.id.zkas_id_search_edit);
        mSearchView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });

        ImageView searchBtn = (ImageView) mRootView.findViewById(R.id.zkas_id_search_btn);
        searchBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                if (mHotwords.size() > 0) {
                    intent.putExtra("keyword", mHotwords.get(mHotwordIndex));
                }
                startActivity(intent);
            }
        });

        //下载按钮
        mDwldBtn = (PromptImageView) mRootView.findViewById(R.id.zkas_id_download_manage_btn);
        mDwldBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DownloadActivity.class);
                if (mDwldBtn.getPromptNumber() > 0) {
                    intent.putExtra("mode", 1);
                } else {
                    intent.putExtra("mode", 0);
                }
                startActivity(intent);
            }
        });

        mViewPager = (MyViewPager) mRootView.findViewById(R.id.zkas_id_main_viewpager);
        mViewPager.setOnPageChangeListener(mOnPageChangeListener);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        mPagerAdapter = new PagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mPagerAdapter.notifyDataSetChanged();

        LinearLayout textTabLayout = (LinearLayout) mRootView.findViewById(R.id.main_tab_bar_area);
        LinearLayout imageTabLayout = (LinearLayout) mRootView.findViewById(R.id.tab_icon_layout);
        if (isUseImageTab()) {//TabHost不同的样式
            mViewPager.setScrollEnabled(false);
            textTabLayout.setVisibility(View.GONE);
            mViewPager.setOffscreenPageLimit(5);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            for (int i = 0; i < mPagerAdapter.getCount(); i++) {
                mTabImageViews[i] = new ImageView(getContext());
                mTabImageViews[i].setId(i);
                mTabImageViews[i].setOnClickListener(mOnClickListener);
                mTabImageViews[i].setScaleType(ImageView.ScaleType.CENTER);
                llp.weight = 1;
                imageTabLayout.addView(mTabImageViews[i], llp);
            }
            setImageTabSelector(0);
        } else {
            imageTabLayout.setVisibility(View.GONE);
            mViewPager.setOffscreenPageLimit(3);
            mTabTextViews[0] = (TextView) mRootView.findViewById(R.id.zkas_id_main_tab_recommend);
            mTabTextViews[1] = (TextView) mRootView.findViewById(R.id.zkas_id_main_tab_software);
            mTabTextViews[2] = (TextView) mRootView.findViewById(R.id.zkas_id_main_tab_game);
            for (int i = 0; i < mPagerAdapter.getCount(); i++) {
                mTabTextViews[i].setOnClickListener(mOnClickListener);
            }
            setTabSelector(0);
        }


        AppStoreWrapperImpl.registerAppUpgradeCountListener(this);

        if (mManagerCallback == null) {
            mManagerCallback = new ManagerCallback() {

                @Override
                public void onSuccess(String moduleType, int dataType, int page, int num,
                                      boolean end) {
                    if (isDestroyed()) {
                        return;
                    }
                    mHotwords.clear();
                    mHotwordIndex = -1;
                    ArrayList<Hotword> hotwords = DataPool.getInstance().getSearchHotwords();
                    if (hotwords != null && hotwords.size() > 0) {
                        for (Hotword h : hotwords) {
                            if (h.recommend) {
                                mHotwords.add(h.hotword);
                            }
                        }
                    }
                    if (mHotwords.size() > 0) {
                        String hw = mHotwords.get(0);
                        mSearchView.setText(mHotwordPrefix + " " + hw);
                        mHotwordIndex = 0;
                        if (mHotwords.size() > 1) {
                            mMainThreadHandler.sendEmptyMessageDelayed(0, HOT_WORD_CHANGE_DELAY);
                        }
                    } else {
                        mMainThreadHandler.removeMessages(0);
                    }
                }

                @Override
                public void onFailure(String moduleType, int dataType, Throwable t, int errorNo,
                                      String strMsg) {
                    LogEx.d(TAG, "ManagerCallback.onFailure(), strMsg=" + strMsg);
                }
            };
        }
        SearchManager.getInstance().loadHotwords(mManagerCallback);
        mHotwordPrefix = getString(R.string.as_search_hint_text);

        updateNewsIcon();
        DataPool.getInstance().registerDataObserver(mDataObserver);
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
//        MobclickAgent.onPageStart("MainFragment");
    }

    @Override
    public void onPause() {
        super.onPause();
     //   MobclickAgent.onPageEnd("MainFragment");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AppStoreWrapperImpl.unregisterAppUpgradeCountListener(this);
        mMainThreadHandler.removeMessages(0);
        DataPool.getInstance().unregisterDataObserver(mDataObserver);
    }

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.zkas_id_main_tab_recommend) {
                setTabSelector(0);
            } else if (v.getId() == R.id.zkas_id_main_tab_software) {
                setTabSelector(1);
            } else if (v.getId() == R.id.zkas_id_main_tab_game) {
                setTabSelector(2);
            }
            switch (v.getId()) {
                case 0: {
                    setImageTabSelector(0);
                    break;
                }
                case 1: {
                    setImageTabSelector(1);
                    break;
                }
                case 2: {
                    setImageTabSelector(2);
                    break;
                }
                case 3: {
                    setImageTabSelector(3);
                    break;
                }
                case 4: {
                    setImageTabSelector(4);
                    break;
                }
            }
        }
    };

    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            if (isUseImageTab()) {
                setImageTabSelector(position);
            } else {
                setTabSelector(position);
            }

            switch (position) {
                case 0:
                case 1:
                case 2:
                case 3:
                    Utils.setWhiteStatusBar(getActivity());
                    line.setVisibility(View.VISIBLE);
                    break;
                case 4:
                    MyStatusBarUtil.setWindowStatusBarColor(getActivity(), R.color.white);
                    line.setVisibility(View.GONE);
                    break;
            }

        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    private void setTabSelector(int index) {
        for (int i = 0; i < mPagerAdapter.getCount(); i++) {
            if (index == i) {
                mTabTextViews[i].setTextColor(getResources().getColor(R.color.as_tab_bar_tab_text_color_selected));
                mTabTextViews[i].setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.as_tab_bar_tab_selected);
                mViewPager.setCurrentItem(i);
            } else {
                mTabTextViews[i].setTextColor(getResources().getColor(R.color.as_tab_bar_tab_text_color_normal));
                mTabTextViews[i].setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.as_tab_bar_tab_normal);
            }
        }
    }

    private void setImageTabSelector(int index) {
        for (int i = 0; i < mPagerAdapter.getCount(); i++) {
            if (index == i) {
                mTabImageViews[i].setImageResource(TAB_RES_HIGHLIGHT_IDS[i]);
                mViewPager.setCurrentItem(i);
            } else {
                mTabImageViews[i].setImageResource(TAB_RES_IDS[i]);
            }
        }
        //以下代码是为了将第5页的Fragment变成不要搜索框的独立Fragment
//        if (index == 4) {
//            mTopAreaLayout.setVisibility(View.GONE);
//        } else {
//            mTopAreaLayout.setVisibility(View.VISIBLE);
//        }
    }

    private static boolean isUseImageTab() {
        if (Properties.CHANNEL_COOLMART.equals(AppStoreWrapperImpl.getInstance().getChannel())
                || Properties.CHANNEL_ALPHAGO.equals(AppStoreWrapperImpl.getInstance().getChannel())
                || Properties.CHANNEL_SHARP.equals(AppStoreWrapperImpl.getInstance().getChannel())
                || Properties.CHANNEL_DINGZHI.equals(AppStoreWrapperImpl.getInstance().getChannel())
                || Properties.CHANNEL_DUOCAI.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
            return true;
        }
        return false;
    }

    private class PagerAdapter extends FragmentPagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            switch (position) {
                case 0: {
                    MainListFragment f = new MainListFragment();
                    f.setPageId(Properties.PAGE_RECOMMEND);
                    f.setFrom(getPageTitle(position).toString());

                    return f;
                }
                case 1: {
                    MainListFragment f = new MainListFragment();
                    f.setPageId(Properties.PAGE_APP);
                    f.setFrom(getPageTitle(position).toString());
                    return f;
                }
                case 2: {
                    MainListFragment f = new MainListFragment();
                    f.setPageId(Properties.PAGE_GAME);
                    f.setFrom(getPageTitle(position).toString());
                    return f;
                }
                case 3: {
                    MainListFragment f = new MainListFragment();
                    f.setPageId(Properties.PAGE_RANK);
                    f.setFrom(getPageTitle(position).toString());
                    return f;
                }
                case 4: {
                    BoardFragment f = new BoardFragment();
                    return f;
                }
            }
            return null;
        }

        @Override
        public int getCount() {
            if (isUseImageTab()) {
                if (Properties.CHANNEL_DUOCAI.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
                    return TAB_RES_IDS.length -1;
                }
                return TAB_RES_IDS.length;
            }
            return TITLE_RES_IDS.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (isUseImageTab()) {
                return getString(TAB_RES_IDS[position]);
            }
            return getString(TITLE_RES_IDS[position]);
        }
    }

    @Override
    public void appUpgradeCount(int count) {
        if (count == 0) {
            mDwldBtn.hidePromptNumber();
        } else {
            mDwldBtn.showPromptNumber(count);
        }
    }

    private DataPool.DataObserver mDataObserver = new DataPool.DataObserver() {
        @Override
        public void onChanged(int type) {
            if (type == DataPool.TYPE_NEWS_ICON) {
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateNewsIcon();
                    }
                });
            }
        }
    };

    private void updateNewsIcon() {
        final ImageView newsIcon = (ImageView) mRootView.findViewById(R.id.news_icon);
        newsIcon.setVisibility(View.GONE);
        if (Protocol.getInstance().mHomePageIconUrl == null || Protocol.getInstance().mHomePageIconUrl.length() == 0
                || Protocol.getInstance().mHomePageUrl == null || Protocol.getInstance().mHomePageUrl.length() == 0) {
            return;
        }
        if (mRootView == null) {
            mMainThreadHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateNewsIcon();
                }
            }, 5 * 1000);
        }

        Bitmap bitmap = BitmapUtil.getInstance().getBitmapAsync(Protocol.getInstance().mHomePageIconUrl, new AsynTaskManager.ImageLoadCallBack() {
            @Override
            public boolean isNeedToDecode(String imageUrl) {
                if (isDestroyed()) {
                    return false;
                }
                return true;
            }

            @Override
            public void onImageLoadSuccess(String imageUrl, Bitmap bitmap) {
                if (isDestroyed()) {
                    return;
                }
                newsIcon.setImageBitmap(bitmap);
                newsIcon.setVisibility(View.VISIBLE);
                newsIcon.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(), H5Activity.class);
                        i.putExtra("url", Protocol.getInstance().mHomePageUrl);
                        i.putExtra("title", " ");
                        startActivity(i);
                    }
                });
            }

            @Override
            public void onImageLoadFailed(String imageUrl, String reason) {
            }

            @Override
            public String getCaller() {
                return "newsIcon";
            }
        });
        if (bitmap != null && !bitmap.isRecycled()) {
            if (isDestroyed()) {
                return;
            }
            newsIcon.setImageBitmap(bitmap);
            newsIcon.setVisibility(View.VISIBLE);
            newsIcon.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getActivity(), H5Activity.class);
                    i.putExtra("url", Protocol.getInstance().mHomePageUrl);
                    i.putExtra("title", " ");
                    startActivity(i);
                }
            });
        }
    }
}
