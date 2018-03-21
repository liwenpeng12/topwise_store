package com.topwise.topos.appstore.view.activity;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.manager.ActivityManager;
import com.topwise.topos.appstore.view.ActionBarView;

public class ProtocelActivity extends Activity {
    private ActionBarView mActionBarView;
    private final int use_protocel=1;//使用许可协议界面
    private final int privacy_protected=2;//隐私保护界面
    private final int disclaimer=3;//免责声明界面
    private WebView protocel_webview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protocel);
        ActivityManager.addActivity(this);
        mActionBarView = (ActionBarView) findViewById(R.id.as_action_bar_layout);
        protocel_webview=(WebView)findViewById(R.id.protocel_webview);
        int PageType=getIntent().getIntExtra("about",0);
        if(PageType != 0) {
            switch (PageType){
                case use_protocel:
                 mActionBarView.setTitle(R.string.topwise_board_about_protocel);
                    loadHtmlFile("file:///android_asset/use_protocel.html");
                 break;
                case privacy_protected:
                    mActionBarView.setTitle(R.string.topwise_board_about_privacy_protected);
                    loadHtmlFile("file:///android_asset/privacy_protected.html");
                    break;
                case disclaimer:
                    mActionBarView.setTitle(R.string.topwise_board_about_disclaimer);
                    loadHtmlFile("file:///android_asset/disclaimer.html");
                    break;
            }
        }
        mActionBarView.setOnBackButtonClickListener(new ActionBarView.BackButtonClickListener() {
            @Override
            public void onBackBtnClicked(View v) {
                finish();
            }
        });
    }

    private void loadHtmlFile(String path){
      if(protocel_webview != null){
          //支持App内部javascript交互
          protocel_webview.getSettings().setJavaScriptEnabled(true);
          //自适应屏幕
          protocel_webview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
          protocel_webview.getSettings().setLoadWithOverviewMode(true);
          //设置可以支持缩放
//          protocel_webview.getSettings().setSupportZoom(true);
          //扩大比例的缩放
//          protocel_webview.getSettings().setUseWideViewPort(true);
          //设置是否出现缩放工具
//          protocel_webview.getSettings().setBuiltInZoomControls(true);
          protocel_webview.loadUrl(path);
      }
    }

}
