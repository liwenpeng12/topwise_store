package com.topwise.topos.appstore.data;

import java.io.Serializable;

public class AdIcon implements Serializable {

    private static final long serialVersionUID = 6260611216581731233L;
    
    public volatile String id;
    public volatile int module_weight; // 分区排序，即当前AdIcon属于页面中哪一行
    public volatile int weight; // 分区内排序，越小越靠前
    public volatile String title; // 名称
    public volatile String img_url; // 展示图片
    public volatile String target; // 落地页类型，h5：h5页面；type：分类结果；label：列表；app：单个app；rank：榜单；page：复杂页面（page id是与服务器约定好固定的）; category: 分类界面
    public volatile String target_url; // h5类型中的落地页网址，非h5时等于id
    public volatile String target_name; // 落地页有多个时的tab标题
}
