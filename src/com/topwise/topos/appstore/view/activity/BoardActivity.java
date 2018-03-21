package com.topwise.topos.appstore.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.FrameLayout;

import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.manager.ActivityManager;
import com.topwise.topos.appstore.manager.AppManager;
import com.topwise.topos.appstore.view.ActionBarView;
import com.topwise.topos.appstore.view.fragment.DownloadTaskFragment;
import com.topwise.topos.appstore.view.fragment.DownloadUpdateFragment;

public class BoardActivity extends FragmentActivity {
    private ActionBarView mActionBarView;
    private int type=0;
    private FrameLayout inside_Frame;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        mActionBarView = (ActionBarView) findViewById(R.id.as_action_bar_layout);

        mActionBarView.setOnBackButtonClickListener(new ActionBarView.BackButtonClickListener() {
            @Override
            public void onBackBtnClicked(View v) {
                finish();
            }
        });
        //保证同步
        ActivityManager.addActivity(this);
        AppManager.getInstance().loadNeedUpgradeApps();

        inside_Frame=(FrameLayout)findViewById(R.id.inside_Frame);
        Intent myintent=getIntent();
        type=(int)myintent.getExtras().getInt("download");
        switch (type){
            case 0:
                mActionBarView.setTitle(R.string.topwise_board_page_download);
               getSupportFragmentManager().beginTransaction().replace(R.id.inside_Frame, DownloadTaskFragment.getInstance()).commit();
                break;

            case 1:
                mActionBarView.setTitle(R.string.topwise_board_page_update);
                getSupportFragmentManager().beginTransaction().replace(R.id.inside_Frame, DownloadUpdateFragment.getInstance()).commit();
                break;

        }
    }

}
