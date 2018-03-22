package com.topwise.topos.appstore.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.conn.behavior.UserTrack;
import com.topwise.topos.appstore.data.AppInfo;
import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.data.Hotword;
import com.topwise.topos.appstore.manager.ActivityManager;
import com.topwise.topos.appstore.manager.ManagerCallback;
import com.topwise.topos.appstore.manager.SearchManager;
import com.topwise.topos.appstore.utils.Utils;
import com.topwise.topos.appstore.view.ListMainItemView.AppItemClickListener;
import com.topwise.topos.appstore.view.WaitingView;
import com.topwise.topos.appstore.view.fragment.MainListFragment.MainListAdapter;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

public class SearchActivity extends Activity {

    private static final int MAX_KEYWORD_SHOW = 5;
    
    private ManagerCallback mKeyWordCallback;
    private ManagerCallback mSearchCallback;
    
    private ArrayList<String> mHotsoftWare = new ArrayList<String>();
    private ArrayList<String> mHotGame = new ArrayList<String>();
    
    private ListView mListView;
    private MainListAdapter mAdapter;
    
    private ImageView mBackBtn;
    private WaitingView mWaitView;
    private EditText mSearchEdit;
    private ImageView mSearchBtn;
    private ImageView mSearchClearBtn;
    private TextView mHistoryClearBtn;
//    private ZkWebView mNewsWebView;
    private ScrollView mScrollView;

    private LinearLayout mKeywordContainer;
    private LinearLayout mSoftwaveKeyword;
    private LinearLayout mGameKeyword;
    private LinearLayout mHistoryContainer;
    
    private OnClickListener mKeywordClickListener;
    private OnClickListener mHistoryWordClickListener;
    
    private LayoutInflater mInflater;

    private boolean mWebViewScroll = false;

    private boolean isWebViewPageFinished = false;
    private boolean isWebViewProgressDone = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.addActivity(this);
        Utils.setWhiteStatusBar(this);
        setContentView(R.layout.zkas_search_activity_layout);
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        
        mBackBtn = (ImageView) findViewById(R.id.as_title_bar_back_btn);
        mListView = (ListView) findViewById(R.id.zkas_id_search_result_list);
        mWaitView = (WaitingView) findViewById(R.id.zkas_id_search_wait_view);
        mSearchEdit = (EditText) findViewById(R.id.zkas_id_search_edit);
        mSearchClearBtn = (ImageView) findViewById(R.id.search_clear);
        mSearchClearBtn.setVisibility(View.INVISIBLE);
        mSearchBtn = (ImageView) findViewById(R.id.zkas_id_search_large_btn);
        mHistoryClearBtn = (TextView) findViewById(R.id.history_clear);
        mHistoryClearBtn.setVisibility(View.GONE);
        mKeywordContainer = (LinearLayout) findViewById(R.id.zkas_id_keyword_container);
        mSoftwaveKeyword = (LinearLayout) findViewById(R.id.zkas_id_keyword_left_area_container);
        mGameKeyword = (LinearLayout) findViewById(R.id.zkas_id_keyword_right_area_container);
        mHistoryContainer = (LinearLayout) findViewById(R.id.history_container);
        mScrollView = (ScrollView) findViewById(R.id.search_scroll_view);
//        mNewsWebView = (ZkWebView) findViewById(R.id.news_web);
//        initWebView();
//        mNewsWebView.showLoadingViewProgress("加载中……");
        
        mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        
        loadKeyword();
        loadHistoryWord();
        
        mAdapter = new MainListAdapter();
        mAdapter.setFrom(getString(R.string.title_search));
        mAdapter.setOnAppItemClickListener(new AppItemClickListener() {
            
            @Override
            public void onAppItemClicked(AppInfo info) {
                UserTrack.getInstance().reportAppClicked(info);
                Intent intent = new Intent(SearchActivity.this, AppDetailActivity.class);
                intent.putExtra("app_id", info.id);
                startActivity(intent);
            }
        });
        mListView.setAdapter(mAdapter);
        mListView.setDivider(null);
        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(mListView.getWindowToken(), 0); //强制隐藏键盘
                }
                return false;
            }
        });
        
        mBackBtn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        mSearchEdit.setOnEditorActionListener(new OnEditorActionListener() {
            
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String kw = mSearchEdit.getText().toString();
                    if (kw != null && kw.length() > 0) {
                        search(kw);
                    }
                }
                return false;
            }
        });
        
        mSearchEdit.addTextChangedListener(mTextWatcher);

        mSearchBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String kw = mSearchEdit.getText().toString();
                if (kw != null && kw.length() > 0) {
                    search(kw);
                }
            }
        });

        mSearchClearBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchEdit.setText("");
            }
        });

        mHistoryClearBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchManager.getInstance().clearHistory();
                mHistoryClearBtn.setVisibility(View.GONE);
                mHistoryContainer.removeAllViews();
            }
        });
        
        Intent intent = getIntent();
        String keyword = intent.getStringExtra("keyword");
        if (keyword == null || keyword.length() == 0) {
            Uri uri = intent.getData();
            if (uri != null) {
                String uriString = uri.toString();
                keyword = uriString.substring(uriString.indexOf("key=") + 4, uriString.length());
            }
        }
        if (keyword != null && keyword.length() > 0) {
            search(keyword);
            mSearchEdit.setText(keyword);
            mSearchClearBtn.setVisibility(View.VISIBLE);
        }

        mScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE :
                        if (/*mNewsWebView.getScrollY() == 0*/true) {
                            mWebViewScroll = false;
                        }
                        View view = ((ScrollView) v).getChildAt(0);
                        if (view.getMeasuredHeight() <= v.getScrollY() + v.getHeight()) {
                            mWebViewScroll = true;
                        }
                        break;
                    default :
                        break;
                }
                return false;
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
   //     MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
   //     MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSearchEdit.removeTextChangedListener(mTextWatcher);
        ActivityManager.removeActivity(this);
    }

    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                mKeywordContainer.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);
                mWaitView.setVisibility(View.GONE);
                mSearchClearBtn.setVisibility(View.INVISIBLE);
            } else {
                boolean hide = (mListView.getVisibility() == View.VISIBLE) || (mWaitView.getVisibility() == View.VISIBLE);
                mKeywordContainer.setVisibility(hide ? View.GONE : View.VISIBLE);
                mSearchClearBtn.setVisibility(View.VISIBLE);
            }
        }
    };

//    private void initWebView() {
//        if (Protocol.getInstance().mSearchBannerUrl == null || Protocol.getInstance().mSearchBannerUrl.length() == 0) {
//            mNewsWebView.setVisibility(View.GONE);
//            return;
//        }
//        mNewsWebView.setWebViewClient(mClient);
//        mNewsWebView.setWebChromeClient(mChromeClient);
//        mNewsWebView.loadUrl(Protocol.getInstance().mSearchBannerUrl/*"https://m.baidu.com/?from=844b&vit=fps"*/);
//        mNewsWebView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent ev) {
//                if (mWebViewScroll) {
//                    ((ZkWebView)v).requestDisallowInterceptTouchEvent(true);
//                    return false;
//                }
//                return true;
//            }
//        });
//        mNewsWebView.setOnScrollChangeListener(new ZkWebView.OnScrollChangeListener() {
//            @Override
//            public void onPageEnd(int l, int t, int oldl, int oldt) {
//            }
//
//            @Override
//            public void onPageTop(int l, int t, int oldl, int oldt) {
//                mWebViewScroll = false;
//            }
//
//            @Override
//            public void onScrollChanged(int l, int t, int oldl, int oldt) {
//            }
//        });
//    }
    
    private void loadKeyword() {
        if (mKeyWordCallback == null) {
            mKeyWordCallback = new ManagerCallback() {
                
                @Override
                public void onSuccess(String moduleType, int dataType, int page, int num, boolean end) {
                    addKeyword();
                }
                
                @Override
                public void onFailure(String moduleType, int dataType, Throwable t, int errorNo, String strMsg) {
                    
                }
            };
        }
        SearchManager.getInstance().loadHotwords(mKeyWordCallback);
    }
    
    private void addKeyword() {
        if (mKeywordClickListener == null) {
            mKeywordClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v instanceof TextView) {
                        String kw = ((TextView)v).getText().toString();
                        search(kw);
                        mSearchEdit.setText(kw);
                        mSearchEdit.requestFocus();
                        mSearchEdit.setSelection(kw.length());
                        mSearchClearBtn.setVisibility(View.VISIBLE);
                    }
                }
            };
        }
        
        ArrayList<Hotword> keywords = DataPool.getInstance().getSearchHotwords();
        if (keywords != null && keywords.size() > 0) {
            for (Hotword hw : keywords) {
                if ("game".equals(hw.type)) {
                    mHotGame.add(hw.hotword);
                } else {
                    mHotsoftWare.add(hw.hotword);
                }
            }
            
            int skw = mHotsoftWare.size();
            if (skw > 0) {
                for (int i = 0; i < skw && i < MAX_KEYWORD_SHOW; i++) {
                    addKeywordView(mHotsoftWare.get(i), mSoftwaveKeyword);
                }
            }
            
            int gkw = mHotGame.size();
            if (gkw > 0) {
                for (int i = 0; i < gkw && i < MAX_KEYWORD_SHOW; i++) {
                    addKeywordView(mHotGame.get(i), mGameKeyword);
                }
            }
        }
    }
    
    private void addKeywordView(String kw, ViewGroup parent) {
        TextView t = (TextView)mInflater.inflate(R.layout.zkas_search_keyword_item_layout, parent, false);
        t.setText(kw);
        t.setOnClickListener(mKeywordClickListener);
        parent.addView(t);
    }

    private void loadHistoryWord() {
        if (mHistoryWordClickListener == null) {
            mHistoryWordClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v instanceof TextView) {
                        String kw = ((TextView)v).getText().toString();
                        search(kw);
                        mSearchEdit.setText(kw);
                        mSearchEdit.requestFocus();
                        mSearchEdit.setSelection(kw.length());
                        mSearchClearBtn.setVisibility(View.VISIBLE);
                    }
                }
            };
        }
        ArrayList<String> keywords = SearchManager.getInstance().loadSearchHistory();
        if (keywords.size() == 0) {
            return;
        }
        mHistoryClearBtn.setVisibility(View.VISIBLE);
        mHistoryContainer.removeAllViews();
        for (String keyword : keywords) {
            addHistoryWordView(keyword, mHistoryContainer);
        }
    }

    private void addHistoryWordView(String kw, ViewGroup parent) {
        RelativeLayout layout = (RelativeLayout) mInflater.inflate(R.layout.as_search_history_item_layout, parent, false);
        TextView text = (TextView)  layout.findViewById(R.id.history_word);
        text.setText(kw);
        text.setOnClickListener(mHistoryWordClickListener);
        parent.addView(layout);
    }
    
    private void search(String kw) {
        if (mSearchCallback == null) {
            mSearchCallback = new ManagerCallback() {
                
                @Override
                public void onSuccess(String moduleType, int dataType, int page, int num, boolean end) {
                    loadSearchResult(dataType);
                }
                
                @Override
                public void onFailure(String moduleType, int dataType, Throwable t, int errorNo, String strMsg) {
                    String prmopt = getString(R.string.as_search_failed_prompt_text);
                    mWaitView.showPromptText(prmopt);
                }
            };
        }
        SearchManager.getInstance().search(kw, mSearchCallback);

        mKeywordContainer.setVisibility(View.GONE);
        mListView.setVisibility(View.GONE);
        mWaitView.startProgress(getString(R.string.as_search_progress_prompt_text));

        SearchManager.getInstance().addSearchHistory(kw);
        loadHistoryWord();
    }
    
    private void loadSearchResult(int dataType) {
        ArrayList<AppInfo> searchresult = DataPool.getInstance().getAppInfos(dataType);
        if (searchresult != null && searchresult.size() > 0) {
            mAdapter.setAppDatas(searchresult);
            mWaitView.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        } else {
            mWaitView.showPromptText(getString(R.string.as_search_empty_prompt_text));
        }
    }

//    public void reload() {
//        if (!Utils.isNetworkConnected()) {
//            return;
//        }
//        if (mNewsWebView != null && Protocol.getInstance().mSearchBannerUrl != null && Protocol.getInstance().mSearchBannerUrl.length() > 0) {
//            if (mNewsWebView.isCoverShowed()) {
//                mNewsWebView.hideCover();
//            }
//            mNewsWebView.showLoadingViewProgress("加载中……");
//
//            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mNewsWebView.loadUrl(Protocol.getInstance().mSearchBannerUrl);
//                }
//            }, 1000);
//        }
//    }

//    private void refreshWhenError() {
//        if (mNewsWebView.isCoverShowed()) {
//            return;
//        }
//        mNewsWebView.showLoadingViewRefreshBtn("网络连接失败，请点击此处重试", new LoadingView.OnRefreshClickListener() {
//            @Override
//            public void onRefreshClicked(LoadingView loadingView) {
//                reload();
//            }
//        });
//    }

//    private WebViewClient mClient = new WebViewClient() {
//
//        @Override
//        public void onPageFinished(WebView view, String url) {
//            super.onPageFinished(view, url);
//            isWebViewPageFinished = true;
//            if (isWebViewPageFinished && isWebViewProgressDone) {
//                mNewsWebView.hideProgressingLoadingView();
//                isWebViewPageFinished = false;
//                isWebViewProgressDone = false;
//            }
//        }
//
//        @Override
//        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//            LogEx.d("view,errorCode=" + errorCode + ",description=" + description + ",failingUrl=" + failingUrl);
//            refreshWhenError();
//            super.onReceivedError(view, errorCode, description, failingUrl);
//        }
//
//
//        @Override
//        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//            handler.proceed();
//            super.onReceivedSslError(view, handler, error);
//        }
//
//
//        @Override
//        public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
//            super.onPageStarted(view, url, favicon);
//        }
//
//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            boolean b = false;
//            if (b) {
//                return true;
//            } else {
//                LogEx.d("view,shouldOverrideUrlLoading:" + url);
//                try {
//                    Intent intent = new Intent(SearchActivity.this, H5Activity.class);
//                    intent.putExtra("url", url);
//                    intent.putExtra("title", " ");
//                    startActivity(intent);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            return true;
//        }
//
//    };
//
//    private WebChromeClient mChromeClient = new WebChromeClient() {
//        @Override
//        public void onProgressChanged(WebView view, int newProgress) {
//            super.onProgressChanged(view, newProgress);
//            if (newProgress >= 100) {
//                isWebViewProgressDone = true;
//                if (isWebViewPageFinished && isWebViewProgressDone) {
//                    mNewsWebView.hideProgressingLoadingView();
//                    isWebViewPageFinished = false;
//                    isWebViewProgressDone = false;
//                }
//            }
//        }
//
//        @Override
//        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
//            try {
//                AlertDialog.Builder b = new AlertDialog.Builder(SearchActivity.this);
//                b.setTitle("Alert");
//                b.setMessage(message);
//                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        result.confirm();
//                    }
//                });
//                b.setCancelable(false);
//                b.create().show();
//            } catch (Throwable e) {
//                e.printStackTrace();
//            }
//            return true;
//        }
//
//        //设置响应js 的Confirm()函数
//        @Override
//        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
//            try {
//                AlertDialog.Builder b = new AlertDialog.Builder(SearchActivity.this);
//                b.setTitle("Confirm");
//                b.setMessage(message);
//                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        result.confirm();
//                    }
//                });
//                b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        result.cancel();
//                    }
//                });
//                b.create().show();
//            } catch (Throwable e) {
//                e.printStackTrace();
//            }
//            return true;
//        }
//    };
}
