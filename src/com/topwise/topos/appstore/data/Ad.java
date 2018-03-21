package com.topwise.topos.appstore.data;

import java.io.Serializable;

public class Ad implements Serializable {

    private static final long serialVersionUID = -7305438023699499763L;
    
    public volatile String title; // 名称
    public volatile String desc; // 详细内容描述
    public volatile String imgurl; // 展示图片地址
    public volatile String img_file_path; // 下载后的展示图片地址
    public volatile String iconurl; // 展示图标地址（通知栏需要）
    public volatile String intenturi; // notification的intent
    public volatile String valid_date_start; // 有效日期 开始
    public volatile String valid_date_end;  // 有效日期 结束
    public volatile String show_time_start; // 展示时段 开始
    public volatile String show_time_end; // 展示时段 结束
    public volatile int timeout = 3; // 跳转时间 s
    public volatile long delaytime; // 延迟显示时间 ms
    public volatile int h5type; // h5跳转类型，0 - 手机默认浏览器，1 - 应用内置浏览器
    public volatile String template; // 使用模板，目前全是default
    
    /**
     * 0 无事件
     * 1 打开URL {'url':'要打开的链接'}
     * 2 启动APP {'packagename':'包名', 'version':'版本', 'indent':'indent', 'url':'应用下载地址'}
     * 3 应用内跳转 {'mode':'跳转模块', 'mtype':'mtype 0：主题 1：壁纸', 'code':'跳转的code,cpid,identity'} 跳转模块说明 分类列表：label 详情页：detail 专辑列表：album
     */
    public volatile int event; // 点击事件类型
    public volatile AdStrategy event_data = new AdStrategy(); // 点击事件数据
    
    public class AdStrategy implements Serializable {

        private static final long serialVersionUID = 8901787445773016982L;
        
        public volatile String url; // 要打开的链接
        
        public volatile String packagename; // 包名
        public volatile int versionCode; // 版本
        public volatile String intent; // intent
        public volatile String apk_url; // 应用下载地址

        public volatile String type; // 跳转模块 app|page|label|rank|type|search
        public volatile String type_id; // 模块id，type为search时id为关键字
    }

}
