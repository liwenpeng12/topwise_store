package com.topwise.topos.appstore.view.zkwebview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.topwise.topos.appstore.conn.protocol.H5Interface;

import java.util.HashMap;
import java.util.Map;

public class ZkWebView extends WebView {

    private LoadingView mLoadingView = null;

    public OnScrollChangeListener mListener;

    public ZkWebView(Context context) {
    	this(context, null, android.R.attr.webViewStyle);
    }

    public ZkWebView(Context context, AttributeSet attrs) {
    	this(context, attrs, android.R.attr.webViewStyle);
    }

    public ZkWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLoadingView = new LoadingView(context, attrs, defStyleAttr);
        addView(mLoadingView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        
        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setUseWideViewPort(true);
		String appCacheDir = context.getDir("cache", Context.MODE_PRIVATE).getPath();
		this.getSettings().setAppCachePath(appCacheDir);
		this.getSettings().setAllowFileAccess(true);
		this.getSettings().setAllowFileAccessFromFileURLs(true);
		this.getSettings().setAppCacheEnabled(true);
		this.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
		this.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
		this.getSettings().setLoadWithOverviewMode(true);
		this.getSettings().setSaveFormData(true);
		this.getSettings().setGeolocationEnabled(true);
		this.getSettings().setGeolocationDatabasePath("/data/data/org.itri.html5webview/databases/");     // enable Web Storage: localStorage, sessionStorage
		this.getSettings().setDomStorageEnabled(true);
		this.getSettings().setSupportZoom(true);
		this.getSettings().setTextZoom(100);
//		this.getSettings().setBlockNetworkImage(false);// 设为true不显示网页图片，提高网页加载速度，在WebViewClient.onPageFinished和WebChromeClient.onProgressChanged>100时设为false，加载图片
        CookieManager cm = CookieManager.getInstance();
        CookieManager.setAcceptFileSchemeCookies(true);
        cm.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cm.setAcceptThirdPartyCookies(this, true);
            this.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        this.addJavascriptInterface(new H5Interface(), "h5interface");
        this.requestFocus();
    }

    public void loadUrl(String url, String lastUrl) {
        Map<String,String> extraHeaders = new HashMap<String, String>();
        extraHeaders.put("Referer", lastUrl);
        loadUrl(url, extraHeaders);
    }

    public final boolean isLoadingViewShowing() {
        if (mLoadingView == null) {
            return false;
        }
        return mLoadingView.getVisibility() == View.VISIBLE;
    }

    public final void hideLoadingView() {
        if (mLoadingView == null) {
            return;
        }
        if (mLoadingView.isProgressing()) {
            mLoadingView.stopProgressing();
        }
        mLoadingView.setVisibility(View.GONE);
        View customView = this.getChildAt(0);
        if (customView != null && customView != mLoadingView) {
            customView.setVisibility(View.VISIBLE);
        }
    }

    public final void hideProgressingLoadingView() {
        if (mLoadingView == null) {
            return;
        }
        if (!mLoadingView.isProgressing()) {
            return;
        }
        hideLoadingView();
    }

    public final void showLoadingViewProgress(String prompt) {
        if (mLoadingView == null) {
            return;
        }
        mLoadingView.startProgress(prompt);
    }

    public final void showLoadingViewProgress(int resId) {
        String prompt = getResources().getString(resId);
        showLoadingViewProgress(prompt);
    }

    public final void showLoadingViewRefreshBtn(String prompt, LoadingView.OnRefreshClickListener l) {
        if (mLoadingView == null) {
            return;
        }
        if (mLoadingView.isProgressing()) {
            mLoadingView.stopProgressing();
        }
        mLoadingView.showRefreshButton(prompt, l);
    }

    public final void showLoadingViewRefreshBtn(int resId, LoadingView.OnRefreshClickListener l) {
        String prompt = getResources().getString(resId);
        showLoadingViewRefreshBtn(prompt, l);
    }

    public final void showLoadingViewPrompt(String prompt) {
        if (mLoadingView == null) {
            return;
        }
        if (mLoadingView.isProgressing()) {
            mLoadingView.stopProgressing();
        }
        mLoadingView.showPromptText(prompt);
    }

    public final void showLoadingViewPrompt(int resId) {
        String prompt = getResources().getString(resId);
        showLoadingViewPrompt(prompt);
    }
    
    public final void showCover(Drawable drawable, OnClickListener l) {
        if (mLoadingView == null) {
            return;
        }
    	mLoadingView.showCover(drawable, l);
    }
    
    public final void hideCover() {
    	mLoadingView.hideCover();
    }
    
    public final boolean isCoverShowed() {
    	return mLoadingView.isCoverShowed();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mListener == null) {
            return;
        }
        float webcontent = getContentHeight() * getScale();// webview的高度
        float webnow = getHeight() + getScrollY();// 当前webview的高度
        if (Math.abs(webcontent - webnow) < 1) {
            // 已经处于底端
            mListener.onPageEnd(l, t, oldl, oldt);
        } else if (getScrollY() == 0) {
            // 已经处于顶端
            mListener.onPageTop(l, t, oldl, oldt);
        } else {
            mListener.onScrollChanged(l, t, oldl, oldt);
        }
    }

    public void setOnScrollChangeListener(OnScrollChangeListener l) {
        mListener = l;
    }

    public interface OnScrollChangeListener {
        void onPageEnd(int l, int t, int oldl, int oldt);
        void onPageTop(int l, int t, int oldl, int oldt);
        void onScrollChanged(int l, int t, int oldl, int oldt);
    }
}
