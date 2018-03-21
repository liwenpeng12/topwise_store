package com.topwise.topos.appstore.data;

import com.main.ads.base.NativeAdBase;

import java.io.Serializable;

public class Banner implements Serializable {

    private static final long serialVersionUID = -5520578220504781367L;
    
    public volatile String id;
    public volatile int module_weight; // 分区排序，即当前banner属于页面中哪一行
    public volatile int weight; // 分区内排序，越小越靠前
    public volatile String title; // 名称
    public volatile String desc; // 描述
    public volatile String img_url; // 展示图片
    public volatile String target; // 落地页类型，h5：h5页面；type：分类；label：列表；app：单个app；rank：榜单；page：复杂页面（page id是与服务器约定好固定的）
    public volatile String target_url; // h5类型中的落地页网址，非h5时等于id
    public volatile String target_name; // 落地页有多个时的tab标题
    public volatile boolean large_banner; // true：大banner；false：小banner

    public volatile boolean is_ads; // 是否是掌酷ads广告。重要：ads广告显示时必须调用adsAd.registerViewForInteraction(View)，传入广告所在的view，托管点击事件
    public volatile NativeAdBase adsAd; // 掌酷ads广告内容。重要：ads广告显示时必须调用adsAd.registerViewForInteraction(View)，传入广告所在的view，托管点击事件
}
