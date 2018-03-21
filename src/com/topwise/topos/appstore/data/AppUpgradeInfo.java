package com.topwise.topos.appstore.data;

import java.io.Serializable;

public class AppUpgradeInfo implements Serializable {

    private static final long serialVersionUID = 3216706886169081815L;
    
    public volatile int verCode; // 版本号
    public volatile String verName; // 版本名称
    public volatile String date; // 添加/更新时间
    public volatile String desc; // 应用简介
    public volatile String size; // 大小
    public volatile String url; // 下载地址
    public volatile boolean isForceUpgrade; // 是否强制升级
}
