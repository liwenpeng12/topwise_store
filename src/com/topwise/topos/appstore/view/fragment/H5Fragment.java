package com.topwise.topos.appstore.view.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.utils.Utils;
import com.topwise.topos.appstore.view.activity.H5Activity;
import com.topwise.topos.appstore.view.zkwebview.LoadingView;
import com.topwise.topos.appstore.view.zkwebview.ZkWebView;

public class H5Fragment extends BaseFragment {

    private String mUrl = "http://app.moyumedia.com/news/home";

    private ZkWebView mWebView = null;

    private boolean isWebViewPageFinished = false;
    private boolean isWebViewProgressDone = false;

    private boolean isWebViewClicked = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mUrl = "http://app.moyumedia.com/news/home?channel=" + AppStoreWrapperImpl.getInstance().getChannel()
                + "&androidid=" + AppStoreWrapperImpl.getInstance().getDeviceInfo().getAndroidId()
                + "&product=" + AppStoreWrapperImpl.getInstance().getDeviceInfo().getProductModel();

        LogEx.d(mUrl);
        View rootView = inflater.inflate(R.layout.as_fragment_h5, container, false);
        mWebView = (ZkWebView) rootView.findViewById(R.id.web);


        initWebView();
        mWebView.showLoadingViewProgress("加载中……");

        RelativeLayout refreshBg = (RelativeLayout) rootView.findViewById(R.id.refresh_bg);
        final ImageView refreshImg = (ImageView) rootView.findViewById(R.id.refresh_img);
        refreshBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isWebViewClicked = false;
                RotateAnimation anim = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                anim.setFillAfter(true);
                anim.setDuration(1000);
                refreshImg.startAnimation(anim);
                reload();
            }
        });
        refreshBg.setVisibility(View.GONE);// 暂时去掉，网页中有刷新
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mWebView.destroy();
    }

    private void initWebView() {
        mWebView.setWebViewClient(mClient);
        mWebView.setWebChromeClient(mChromeClient);
        mWebView.loadUrl(mUrl);
        mWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isWebViewClicked = true;
                return false;
            }
        });
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
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            LogEx.d("view,errorCode=" + errorCode + ",description=" + description + ",failingUrl=" + failingUrl);
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

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            LogEx.d("view,shouldOverrideUrlLoading:" + url);
            if (isWebViewClicked) {
                try {
                    Intent intent = new Intent(getContext(), H5Activity.class);
                    intent.putExtra("url", url);
                    intent.putExtra("title", " ");
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mWebView.loadUrl(url);
            }
            return true;
        }
    };

    private WebChromeClient mChromeClient = new WebChromeClient() {
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
                AlertDialog.Builder b = new AlertDialog.Builder(getContext());
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
                AlertDialog.Builder b = new AlertDialog.Builder(getContext());
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
}
