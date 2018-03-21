package com.topwise.topos.appstore.view.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.data.Hotword;
import com.topwise.topos.appstore.manager.ActivityManager;
import com.topwise.topos.appstore.manager.ManagerCallback;
import com.topwise.topos.appstore.manager.SearchManager;
import com.topwise.topos.appstore.utils.Properties;
import com.topwise.topos.appstore.view.fragment.MainListFragment;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

public class PageActivity extends FragmentActivity {

    private TextView mSearchView;
    private ArrayList<String> mHotwords = new ArrayList<String>();
    private int mHotwordIndex = -1;
    private static final int HOT_WORD_CHANGE_DELAY = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.addActivity(this);
        setContentView(R.layout.as_activity_page);

        mMainThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                initUI();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        initAsync();
                    }
                }).run();
            }
        }, 500);
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
        mMainThreadHandler.removeMessages(0);
        ActivityManager.removeActivity(this);
    }

    private void initUI() {
        int pageId = Properties.PAGE_RECOMMEND;
        try {
            Uri uri = getIntent().getData();
            String uriString = uri.toString();
            String id = uriString.substring(uriString.indexOf("pageid=") + 7, uriString.length());
            pageId = Integer.parseInt(id);
        } catch (Throwable t) {
        }

        MainListFragment f = new MainListFragment();
        f.setPageId(pageId);
        f.setFrom(getString(R.string.as_tab_recommend_text));
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.main_fragment_container, f);
        ft.commitAllowingStateLoss();

        mSearchView = (TextView) findViewById(R.id.zkas_id_search_edit);
        mSearchView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PageActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        ImageView searchBtn = (ImageView) findViewById(R.id.zkas_id_search_btn);
        searchBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PageActivity.this, SearchActivity.class);
                if (mHotwords.size() > 0) {
                    intent.putExtra("keyword", mHotwords.get(mHotwordIndex));
                }
                startActivity(intent);
            }
        });
    }

    private void initAsync() {
        SearchManager.getInstance().loadHotwords(new ManagerCallback() {
            @Override
            public void onSuccess(String moduleType, int dataType, int page, int num, boolean end) {
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
                    mSearchView.setText(getString(R.string.as_search_hint_text) + " " + hw);
                    mHotwordIndex = 0;
                    if (mHotwords.size() > 1) {
                        mMainThreadHandler.sendEmptyMessageDelayed(0, HOT_WORD_CHANGE_DELAY);
                    }
                } else {
                    mMainThreadHandler.removeMessages(0);
                }
            }

            @Override
            public void onFailure(String moduleType, int dataType, Throwable t, int errorNo, String strMsg) {
            }
        });
    }

    private Handler mMainThreadHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int size = mHotwords.size();
            if (size > 0) {
                mHotwordIndex++;
                mHotwordIndex %= size;

                String hw = mHotwords.get(mHotwordIndex);
                mSearchView.setText(getString(R.string.as_search_hint_text) + " " + hw);

                sendEmptyMessageDelayed(0, HOT_WORD_CHANGE_DELAY);
            }
        }
    };
}
