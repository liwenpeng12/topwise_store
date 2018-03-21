package com.topwise.topos.appstore.data;

import java.io.File;
import java.util.ArrayList;

import android.graphics.drawable.Drawable;

import org.json.JSONException;
import org.json.JSONObject;

public class AppInfo extends Module {

    private static final long serialVersionUID = -1706057775118275375L;
    
    public static final int FLAG_ONLINE = 0;
    public static final int FLAG_DOWNLOADED = 1;
    public static final int FLAG_INSTALLED = 2;
    public static final int FLAG_NEED_UPGRADE = 3;
    public static final int FLAG_INSTALLING = 4;
    
    public volatile String id; // 包名即id
    public volatile int weight; // 分区内排序，越小越靠前
    public volatile String pkg; // 包名
    public volatile String icon_url; // 图标url
    public volatile String name; // 名称
    public volatile String sdesc; // 简介
    public volatile String desc; // 详细介绍
    public volatile String file_url; // 文件url
    public volatile int vercode; // 应用versionCode
    public volatile String vername; // 应用versionName
    public volatile int minSdkVersion; // 应用能支持的android最低版本号
    public volatile String size; // 大小
    public volatile boolean isfree; // 是否免流量
    public volatile int awardkey; // 下载奖励类型
    public volatile int awardvalue; // 下载奖励值
    public volatile ArrayList<Tag> tags = new ArrayList<Tag>(); // 标签
    public volatile String date; // 时间
    public volatile String downloads; // 下载量
    public volatile int type; // 应用分类
    public volatile boolean islocal; // 是否是自己运营的应用
    public volatile String adimage; // 列表中显示大图
    public volatile String adcontent; // 列表中显示的最新动态内容
    public volatile String adurl; // adcontent点击的网址
    public volatile String src; // 来源
    public volatile int flag; // 参照上面FLAG值
    public volatile String from; // 用于行为统计，统计从哪里来
    public volatile String upgrade_desc; // 升级
    public volatile String bindId; // 360sdk需要的id
    public volatile String signature_md5; // 签名的mo5信息

    public volatile ArrayList<String> bill_report_start_download = new ArrayList<>(); // 上报计费应用开始下载
    public volatile ArrayList<String> bill_report_downloaded = new ArrayList<>(); // 上报计费应用已下载完成
    public volatile ArrayList<String> bill_report_installed = new ArrayList<>(); // 上报计费应用已安装完成
    public volatile ArrayList<String> bill_report_first_launch = new ArrayList<>(); // 上报计费应用激活

    public volatile ArrayList<String> report_show = new ArrayList<>(); // 上报展示
    public volatile ArrayList<String> report_start_download = new ArrayList<>(); // 上报开始下载

    public volatile File file; // 下载到的文件
    public volatile Drawable app_icon; // 下载后的文件icon
    
    public volatile ArrayList<String> thumbnail_urls = new ArrayList<String>(); // 预览图地址数组
    
    public void setFrom(String from) {
        this.from = from;
    }
    
    public static class Tag {
        public volatile String name;
        public volatile int bgcolor;
        public volatile int txtcolor;
    }

    public JSONObject toJSON() {
        try {
            JSONObject object = new JSONObject();
            object.put("id", id); // id,即包名
            object.put("icon_url", icon_url); // 图标地址
            object.put("name", name); // 名称
            object.put("size", size); // 大小
            return object;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
