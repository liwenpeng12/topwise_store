package com.topwise.topos.appstore.data;

import java.io.Serializable;

public class Hotword implements Serializable {

    private static final long serialVersionUID = -488533891324126593L;
    
    public volatile String type; // 热词所属类型
    public volatile String id; // id
    public volatile int weight; // 排序
    public volatile boolean recommend; // 推荐词（轮播）
    public volatile String hotword; // 关键词
}
