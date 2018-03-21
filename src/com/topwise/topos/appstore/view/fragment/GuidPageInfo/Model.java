package com.topwise.topos.appstore.view.fragment.GuidPageInfo;


import com.topwise.topos.appstore.data.AppInfo;

public class Model {
    private AppInfo appInfo;
    private boolean isChecked;
    public Model(AppInfo appInfo,boolean isChecked) {
        this.appInfo = appInfo;
        this.isChecked=isChecked;
    }

    public AppInfo getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(AppInfo appInfo) {
        this.appInfo = appInfo;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

}
