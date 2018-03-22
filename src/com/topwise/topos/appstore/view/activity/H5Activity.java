package com.topwise.topos.appstore.view.activity;

import com.topwise.topos.appstore.manager.ActivityManager;
import com.topwise.topos.appstore.utils.Utils;
import com.topwise.topos.appstore.view.ActionBarView;
import com.topwise.topos.appstore.view.ActionBarView.BackButtonClickListener;
import com.topwise.topos.appstore.view.zkwebview.LoadingView;
import com.topwise.topos.appstore.view.zkwebview.ZkWebView;
import com.umeng.analytics.MobclickAgent;
import com.topwise.topos.appstore.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class H5Activity extends Activity {

    private ActionBarView mActionBar;
    private ZkWebView mWebView = null;

    private String mUrl = "";

    private boolean isWebViewPageFinished = false;
    private boolean isWebViewProgressDone = false;

    @SuppressLint("SetJavaScriptEnabled")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.addActivity(this);
        Utils.setWhiteStatusBar(this);
        setContentView(R.layout.as_activity_h5);
        mUrl = getIntent().getStringExtra("url");
        mWebView = (ZkWebView) findViewById(R.id.web);
        mWebView.setWebViewClient(mClient);
        mWebView.setWebChromeClient(mWebChromeClient);
        mWebView.setDownloadListener(mDownloadListener);
        mWebView.loadUrl(mUrl);
        mWebView.showLoadingViewProgress("加载中……");

        mActionBar = (ActionBarView) findViewById(R.id.as_action_bar_layout);
        String title = getIntent().getStringExtra("title");
        if (title != null && title.length() > 0) {
            mActionBar.setTitle(title);
            mActionBar.setCloseBtnVisibility(View.VISIBLE);
            mActionBar.setOnBackButtonClickListener(new BackButtonClickListener() {

                @Override
                public void onBackBtnClicked(View v) {
                    if (mWebView.canGoBack()) {
                        mWebView.goBack();
                        return;
                    }
                    finish();
                }
            });
            mActionBar.setOnCloseButtonClickListener(new ActionBarView.CloseButtonClickListener() {
                @Override
                public void onCloseBtnClicked(View v) {
                    finish();
                }
            });
        } else {
            mActionBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
  //      MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
   //     MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
        ActivityManager.removeActivity(this);
    }

    public void reload() {
        if (!Utils.isNetworkConnected()) {
            return;
        }
        if (mWebView != null) {
            if (mWebView.isCoverShowed()) {
                mWebView.hideCover();
            }
            mWebView.showLoadingViewProgress("加载中……");

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(mUrl);
                }
            }, 1000);
        }
    }

    private void refreshWhenError() {
        if (mWebView.isCoverShowed()) {
            return;
        }
        mWebView.showLoadingViewRefreshBtn("网络连接失败，请点击此处重试", new LoadingView.OnRefreshClickListener() {
            @Override
            public void onRefreshClicked(LoadingView loadingView) {
                reload();
            }
        });
    }

    private WebViewClient mClient = new WebViewClient() {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            isWebViewPageFinished = true;
            if (isWebViewPageFinished && isWebViewProgressDone) {
                mWebView.hideProgressingLoadingView();
                isWebViewPageFinished = false;
                isWebViewProgressDone = false;
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                mWebView.loadUrl(url);
            } else {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            refreshWhenError();
            super.onReceivedError(view, errorCode, description, failingUrl);
        }


        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
            super.onReceivedSslError(view, handler, error);
        }


        @Override
        public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }
    };

    private WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (newProgress >= 100) {
                isWebViewProgressDone = true;
                if (isWebViewPageFinished && isWebViewProgressDone) {
                    mWebView.hideProgressingLoadingView();
                    isWebViewPageFinished = false;
                    isWebViewProgressDone = false;
                }
            }
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            try {
                AlertDialog.Builder b = new AlertDialog.Builder(H5Activity.this);
                b.setTitle("Alert");
                b.setMessage(message);
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                b.setCancelable(false);
                b.create().show();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return true;
        }

        //设置响应js 的Confirm()函数
        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            try {
                AlertDialog.Builder b = new AlertDialog.Builder(H5Activity.this);
                b.setTitle("Confirm");
                b.setMessage(message);
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                });
                b.create().show();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return true;
        }
    };

    private DownloadListener mDownloadListener = new DownloadListener() {
        @Override
        public void onDownloadStart(final String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            Uri uri = Uri.parse(url);
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
