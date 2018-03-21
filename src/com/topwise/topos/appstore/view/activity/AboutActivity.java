package com.topwise.topos.appstore.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.data.AppUpgradeInfo;
import com.topwise.topos.appstore.manager.ActivityManager;
import com.topwise.topos.appstore.manager.SelfUpgradeCenter;
import com.topwise.topos.appstore.view.ActionBarView;
import com.topwise.topos.appstore.view.AppStoreSettingsItem;
import com.topwise.topos.appstore.view.dialog.DialogUtils;

public class AboutActivity extends Activity implements View.OnClickListener{
    private ActionBarView mActionBarView;
    private AppStoreSettingsItem mSelfUpgradeItem;
    private RelativeLayout use_protocel;
    private RelativeLayout privacy_protected;
    private RelativeLayout disclaimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ActivityManager.addActivity(this);
        mActionBarView = (ActionBarView) findViewById(R.id.as_action_bar_layout);
        mActionBarView.setTitle(R.string.topwise_board_about);
        mSelfUpgradeItem = (AppStoreSettingsItem) findViewById(R.id.self_upgrade);
        use_protocel=(RelativeLayout)findViewById(R.id.use_protocel);
        privacy_protected=(RelativeLayout)findViewById(R.id.privacy_protected);
        disclaimer=(RelativeLayout)findViewById(R.id.disclaimer);
        use_protocel.setOnClickListener(this);
        privacy_protected.setOnClickListener(this);
        disclaimer.setOnClickListener(this);
        mActionBarView.setOnBackButtonClickListener(new ActionBarView.BackButtonClickListener() {
            @Override
            public void onBackBtnClicked(View v) {
                finish();
            }
        });

        mSelfUpgradeItem.getLayoutParams().height = 40;
        mSelfUpgradeItem.hideDiverLine();
        mSelfUpgradeItem.enableTextView(R.string.check_version);
        String upgradeItemName = getResources().getString(R.string.version_prompt) + AppStoreWrapperImpl.getInstance().getAppVersionName();
        if (SelfUpgradeCenter.getInstance().getSelfUpgradeInfo() != null) {
            upgradeItemName = getResources().getString(R.string.new_update) + ": v" + SelfUpgradeCenter.getInstance().getSelfUpgradeInfo().verName;
            mSelfUpgradeItem.enableTextView(R.string.update_now);
        }
        try {
            mSelfUpgradeItem.setItemNameAndSummary(upgradeItemName, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSelfUpgradeItem.setTextViewButtonOnclickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AboutActivity.this, R.string.getting_update, Toast.LENGTH_SHORT).show();
                if (!SelfUpgradeCenter.getInstance().isChecking()) {
                    SelfUpgradeCenter.getInstance().checkUpdate(mCheckVersionCallback);
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.use_protocel:
                startActivity(new Intent(AboutActivity.this,ProtocelActivity.class).putExtra("about",1));
                break;
            case R.id.privacy_protected:
                startActivity(new Intent(AboutActivity.this,ProtocelActivity.class).putExtra("about",2));
                break;
            case R.id.disclaimer:
                startActivity(new Intent(AboutActivity.this,ProtocelActivity.class).putExtra("about",3));
                break;
        }
    }

    private SelfUpgradeCenter.UpdateCallback mCheckVersionCallback = new SelfUpgradeCenter.UpdateCallback() {
        @Override
        public void onSuccess(final AppUpgradeInfo appInfo) {
            String promptMsg = getString(R.string.update_version) + appInfo.verName + "\n"
                    + getString(R.string.update_date) + appInfo.date + "\n\n"
                    + getString(R.string.update_content) + "\n" + appInfo.desc;
            DialogUtils.setNormalDialog(AboutActivity.this, getString(R.string.version_delay_install_text),
                    getString(R.string.version_install_text), getString(R.string.new_update), promptMsg,
                    new Runnable() {
                        @Override
                        public void run() {
                            SelfUpgradeCenter.getInstance().registerDownloadApkReceiver(AboutActivity.this);
                            SelfUpgradeCenter.getInstance().downloadSelfUpgradeApk(appInfo);
                        }
                    });
        }

        @Override
        public void onFailure(Throwable t, int errorNo, String strMsg) {
            Toast.makeText(AboutActivity.this, strMsg, Toast.LENGTH_SHORT).show();
        }
    };

}
