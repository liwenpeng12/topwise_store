package com.topwise.topos.appstore.view.activity;

import android.app.Activity;
import android.os.Bundle;

import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.manager.ActivityManager;
import com.topwise.topos.appstore.utils.Utils;
import com.umeng.analytics.MobclickAgent;

public class ShortcutFolderActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.addActivity(this);
        Utils.setWhiteStatusBar(this);
        setContentView(R.layout.as_activity_shortcut_folder);
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
}
