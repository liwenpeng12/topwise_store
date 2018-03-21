package com.topwise.topos.appstore.data;

import java.util.ArrayList;

public class Rank extends Module {

    private static final long serialVersionUID = 793358821602548186L;
    
    public volatile String id;
    public volatile String img_url; // 图片地址
    public volatile String title; // 标题
    public volatile String adcontent; // 图片下面展示一段文字说明
    public volatile String adurl; // 文字说明的链接
    
    public volatile ArrayList<AppInfo> apps = new ArrayList<AppInfo>();

    @Override
    protected void finalize() throws Throwable {
        // TODO Auto-generated method stub
        apps.clear();
        super.finalize();
    }
}
