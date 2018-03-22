package com.topwise.topos.appstore.view.activity;

import com.topwise.topos.appstore.manager.ActivityManager;
import com.topwise.topos.appstore.manager.SettingsManager;
import com.topwise.topos.appstore.utils.Utils;
import com.topwise.topos.appstore.view.ActionBarView;
import com.topwise.topos.appstore.view.AppStoreSettingsItem;
import com.topwise.topos.appstore.view.ActionBarView.BackButtonClickListener;
import com.topwise.topos.appstore.view.dialog.DialogUtils;
import com.topwise.topos.appstore.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SettingsActivity extends Activity implements OnClickListener {
    private LinearLayout mContainer;
    private LayoutInflater mInflater;
    private View mBack;
    private AppStoreSettingsItem mClearItem;
//    private AppStoreSettingsItem mSelfUpgradeItem;
    private int mListItemHeight;
    private ActionBarView mActionBarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.addActivity(this);
        Utils.setWhiteStatusBar(this);
        setContentView(R.layout.zkas_appstore_settings_layout);
        mContainer = (LinearLayout) findViewById(R.id.settings_container);
        mInflater = LayoutInflater.from(this);
        SettingsManager.getInstance().initSettingValues();
        mListItemHeight = getResources().getDimensionPixelSize(R.dimen.zkas_list_item_height);
        mActionBarView = (ActionBarView) findViewById(R.id.as_action_bar_layout);
        mActionBarView.setTitle(R.string.as_settings_actionbar_title);
        mActionBarView.setOnBackButtonClickListener(new BackButtonClickListener() {
            @Override
            public void onBackBtnClicked(View v) {
                finish();
            }
        });
        initSettingItem();
    }

    private void initSettingItem() {
        final Resources res = getResources();
        String[] itemNames = res.getStringArray(R.array.setting_items_name);
        String[] itemSummary = res.getStringArray(R.array.setting_items_summary);

        for (int i = 0; i < itemNames.length; i++) {
            AppStoreSettingsItem item = (AppStoreSettingsItem) mInflater.inflate(R.layout.zkas_settings_item_layout, null);
            if (i == itemNames.length - 1) {
                item.hideDiverLine();
            }
//			if(i == 0){
//			    item.showTopeShade();
//			    item.setSkipSetting(new Runnable() {
//                    @Override
//                    public void run() {
//                        Intent i = new Intent(InstallService.ACCESSIBILITY_INTENT_ACTION);
//                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(i);
//                    }
//                });
//			}
            try {
                if (itemSummary[i].equals("null") || itemSummary[i].equals("")) {
                    itemSummary[i] = null;
                }
                item.setItemNameAndSummary(itemNames[i], itemSummary[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mContainer.addView(item, i);
            item.getLayoutParams().height = mListItemHeight;
        }

        mClearItem = (AppStoreSettingsItem) findViewById(R.id.clear_cache);
        String clearName = getResources().getString(R.string.as_settings_clear_cache_name);
        String clearSummery = getResources().getString(R.string.as_settings_clear_cache_summary);
        try {
            mClearItem.setItemNameAndSummary(clearName, clearSummery);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mClearItem.getLayoutParams().height = mListItemHeight;
        mClearItem.enableTextView(R.string.as_settings_clear_button);
        mClearItem.setTextViewButtonOnclickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtils.setOkCancelDialog(SettingsActivity.this,
                        res.getString(R.string.as_settings_clearcache_title),
                        res.getString(R.string.as_settings_clearcache_content),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        break;
                                    case DialogInterface.BUTTON_POSITIVE:
                                        new Thread("as_clear_cache") {
                                            public void run() {
                                                SettingsManager.getInstance().clearCache();
                                            }

                                            ;
                                        }.start();
                                        Toast.makeText(SettingsActivity.this, R.string.settings_clear_cache_done_prompt, Toast.LENGTH_LONG).show();
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });
            }
        });

//        mSelfUpgradeItem = (AppStoreSettingsItem) findViewById(R.id.self_upgrade);
//        mSelfUpgradeItem.getLayoutParams().height = mListItemHeight;
//        mSelfUpgradeItem.hideDiverLine();
//        mSelfUpgradeItem.enableTextView(R.string.check_version);
//        String upgradeItemName = getResources().getString(R.string.version_prompt) + AppStoreWrapperImpl.getInstance().getAppVersionName();
//        if (SelfUpgradeCenter.getInstance().getSelfUpgradeInfo() != null) {
//            upgradeItemName = getResources().getString(R.string.new_update) + ": v" + SelfUpgradeCenter.getInstance().getSelfUpgradeInfo().verName;
//            mSelfUpgradeItem.enableTextView(R.string.update_now);
//        }
//        try {
//            mSelfUpgradeItem.setItemNameAndSummary(upgradeItemName, null);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        mSelfUpgradeItem.setTextViewButtonOnclickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(SettingsActivity.this, R.string.getting_update, Toast.LENGTH_SHORT).show();
//                if (!SelfUpgradeCenter.getInstance().isChecking()) {
//                    SelfUpgradeCenter.getInstance().checkUpdate(mCheckVersionCallback);
//                }
//            }
//        });
    }

//    private SelfUpgradeCenter.UpdateCallback mCheckVersionCallback = new SelfUpgradeCenter.UpdateCallback() {
//        @Override
//        public void onSuccess(final AppUpgradeInfo appInfo) {
//            String promptMsg = getString(R.string.update_version) + appInfo.verName + "\n"
//                    + getString(R.string.update_date) + appInfo.date + "\n\n"
//                    + getString(R.string.update_content) + "\n" + appInfo.desc;
//            DialogUtils.setNormalDialog(SettingsActivity.this, getString(R.string.version_delay_install_text),
//                    getString(R.string.version_install_text), getString(R.string.new_update), promptMsg,
//                    new Runnable() {
//                        @Override
//                        public void run() {
//                            SelfUpgradeCenter.getInstance().registerDownloadApkReceiver(SettingsActivity.this);
//                            SelfUpgradeCenter.getInstance().downloadSelfUpgradeApk(appInfo);
//                        }
//                    });
//        }
//
//        @Override
//        public void onFailure(Throwable t, int errorNo, String strMsg) {
//            Toast.makeText(SettingsActivity.this, strMsg, Toast.LENGTH_SHORT).show();
//        }
//    };

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

    @Override
    public void onClick(View v) {
        if (v == mBack) {
            this.finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
     //   MobclickAgent.onResume(this);
        if (mContainer != null) {
            for (int i = 0; i < mContainer.getChildCount(); i++) {
                if (mContainer.getChildAt(i) != null && mContainer.getChildAt(i) instanceof AppStoreSettingsItem) {
                    AppStoreSettingsItem item = (AppStoreSettingsItem) mContainer.getChildAt(i);
                    item.updateChecked();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
     //   MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.removeActivity(this);
    }
}
