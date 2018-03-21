package com.topwise.topos.appstore.utils;

public class Properties {
    
    // app tag
    public static final String APP_TAG = "AppStore";

    // 渠道
    public static final String CHANNEL_17WO = "17wo";
    public static final String CHANNEL_IVVI = "ivvi";
    public static final String CHANNEL_ALPHAGO = "alphago";
    public static final String CHANNEL_COOLMART = "coolmart";
    public static final String CHANNEL_SHARP = "sharp";
    public static final String CHANNEL_DUOCAI = "duocai";
    public static final String CHANNEL_DINGZHI = "dingzhi";

    // 路径
    public static final String TEXT_PATH = Utils.getInternalStoragePath();
    public static final String APPSTORE_PATH = TEXT_PATH + "/ibimuyu/AppStore/";
    public static final String APP_PATH = APPSTORE_PATH + "apps/";
    public static final String BEHAVIOR_PATH = APPSTORE_PATH + ".behavior/";
    public static final String CACHE_PATH = APPSTORE_PATH + ".cache/";

    // 一起沃包名
    public static final String PACKAGENAME_17WO = "com.unicom.yiqiwo";
    
    // intent
    public static final String INTENT_H5_ACTIVITY = "ibimuyu.intent.action.H5";
    public static final String INTENT_SEARCH_ACTIVITY = "ibimuyu.intent.action.SEARCH_ACTIVITY";
    public static final String INTENT_DOWNLOAD_ACTIVITY = "ibimuyu.intent.action.DOWNLOAD_ACTIVITY";
    
    public static final String CLICK_NOTIFICATION = "ibimuyu.appstore.receiver.action.CLICK_NOTIFICATION";
    public static final String INSTALL_SILENT_FAIL = "ibimuyu.appstore.receiver.action.INSTALL_SILENT_FAIL";

    // 页内个数
    public static final int PAGE_NUM = 10;
    
    // module标记
    public static final String MODULE_TYPE_PAGE = "page";
    public static final String MODULE_TYPE_APP = "app";
    public static final String MODULE_TYPE_LABEL = "label";
    public static final String MODULE_TYPE_RANK = "rank";
    public static final String MODULE_TYPE_TYPE = "type";
    public static final String MODULE_TYPE_BANNER_LARGE = "lbanner";
    public static final String MODULE_TYPE_BANNER_SMALL = "sbanner";
    public static final String MODULE_TYPE_ADICON = "adicons";
    public static final String MODULE_TYPE_HOTWORD = "hotword";
    public static final String MODULE_TYPE_AD = "ad";
    
    // page标记
    public static final int PAGE_RECOMMEND = 1; // 推荐
    public static final int PAGE_GAME = 2; // 游戏
    public static final int PAGE_APP = 3; // 应用
    public static final int PAGE_MUST_HAVE_GAME = 4; // 必备游戏
    public static final int PAGE_MUST_HAVE_APP = 5; // 必备应用
    public static final int PAGE_ONE_KEY = 6; // 一键装机
    public static final int PAGE_RANK_GAME = 7; // 游戏榜单
    public static final int PAGE_RANK_APP = 8; // 应用绑定
    public static final int PAGE_RANK = 12; // 首页排行榜

    // 分类标记
    public static final int TYPE_APP_XITONGGONGJU = 1001; // 系统工具
    public static final int TYPE_APP_ZHUTIMEIHUA = 1002; // 主题美化
    public static final int TYPE_APP_SHEJIAOLIAOTIAN = 1003; // 社交聊天
    public static final int TYPE_APP_WANGLUOGONGJU = 1004; // 网络工具
    public static final int TYPE_APP_MEITIYULE = 1005; // 媒体娱乐
    public static final int TYPE_APP_ZHUOMIANCHAJIAN = 1006; // 桌面插件
    public static final int TYPE_APP_ZIXUNYUEDU = 1007; // 资讯阅读
    public static final int TYPE_APP_CHUXINGGOUWU = 1008; // 出行购物
    public static final int TYPE_APP_SHENGHUAZHUSHOU = 1009; // 生活助手
    public static final int TYPE_APP_SHIYONGGONGJU = 1010; // 实用工具
    public static final int TYPE_APP_CAIJINGTOUZI = 1011; // 财经投资
    public static final int TYPE_APP_OTHER = 1012; // 其他
    public static final int TYPE_GAME_ZHANGSHANGWANGYOU = 1073; // 掌上网游
    public static final int TYPE_GAME_OTHER = 1074; // 其他
    public static final int TYPE_GAME_XIUXIANYOUXI = 1075; // 休闲游戏
    public static final int TYPE_GAME_YIZHIYOUXI = 1076; // 益智游戏
    public static final int TYPE_GAME_QIPAIYOUXI = 1077; // 棋牌游戏
    public static final int TYPE_GAME_TIYUYUNDONG = 1078; // 体育运动
    public static final int TYPE_GAME_DONGZUOSHEJI = 1079; // 动作射击
    
}
