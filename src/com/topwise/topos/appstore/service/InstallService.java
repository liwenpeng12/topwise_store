package com.topwise.topos.appstore.service;

import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.database.SharedPreferencesCenter;
import com.topwise.topos.appstore.manager.SettingsManager;
import com.topwise.topos.appstore.utils.LogEx;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class InstallService extends AccessibilityService {
    
    // 辅助功能界面intent action
    public static final String ACCESSIBILITY_INTENT_ACTION = Settings.ACTION_ACCESSIBILITY_SETTINGS;
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        LogEx.d("onAccessibilityEvent");
        if (event.getSource() != null) {
            int eventType = event.getEventType();
            if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED || eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                iterateNodesAndHandle(event.getSource());
            }
        }
    }
    
    private boolean iterateNodesAndHandle(AccessibilityNodeInfo node) {
        if (node != null) {
            if ("android.widget.Button".equals(node.getClassName())) {
                if (getString(R.string.next).equals(node.getText().toString())
                        || getString(R.string.install).equals(node.getText().toString())
                        || getString(R.string.done).equals(node.getText().toString())) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    return true;
                }
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                AccessibilityNodeInfo childNode = node.getChild(i);
                if (iterateNodesAndHandle(childNode)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onInterrupt() {
        // TODO Auto-generated method stub
        LogEx.d("onInterrupt");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        LogEx.d("onServiceConnected");
        if (AppStoreWrapperImpl.getInstance().getAppContext() == null) {
            return;
        }
        SettingsManager.mEnableAutoInstall = true;
        SharedPreferences.Editor editor = SharedPreferencesCenter.getInstance().getSharedPreferences().edit();
        editor.putBoolean(SettingsManager.AUTO_INSTALL, SettingsManager.mEnableAutoInstall);
        editor.commit();
        
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.notificationTimeout = 100;
        info.packageNames = new String[] {"com.android.packageinstaller"};
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        setServiceInfo(info);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // TODO Auto-generated method stub
        LogEx.d("onUnbind");
        if (AppStoreWrapperImpl.getInstance().getAppContext() == null) {
            return true;
        }
        SettingsManager.mEnableAutoInstall = false;
        SharedPreferences.Editor editor = SharedPreferencesCenter.getInstance().getSharedPreferences().edit();
        editor.putBoolean(SettingsManager.AUTO_INSTALL, SettingsManager.mEnableAutoInstall);
        editor.commit();
        return super.onUnbind(intent);
    }

}
