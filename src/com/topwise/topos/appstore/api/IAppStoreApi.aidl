package com.topwise.topos.appstore.api;

import com.topwise.topos.appstore.api.ISearchCallback;
import com.topwise.topos.appstore.api.IDownloadStateListener;

interface IAppStoreApi {
    /**
     * 搜索应用
     * @param keyword 搜索关键字
     * @param callback 搜索结果回调
     */
    void searchApp(String keyword, ISearchCallback callback);
    /**
     * 开始下载应用
     * @param id 应用id（即应用包名）
     * @param IDownloadStateListener 下载状态监听
     * @return 是否成功开始下载
     */
    boolean startDownloadApp(String id, IDownloadStateListener l);
    /**
     * 取消下载应用
     * @param id 应用id（即应用包名）
     * @return 是否成功取消下载
     */
    boolean cancelDownloadApp(String id);
    /**
     * 继续下载应用
     * @param id 应用id（即应用包名）
     * @return 是否成功继续下载
     */
    boolean resumeDownloadApp(String id);
    /**
     * 暂停下载应用
     * @param id 应用id（即应用包名）
     * @return 是否成功暂停下载
     */
    boolean pauseDownloadApp(String id);
    /**
     * 获取当前应用状态
     * @param id 应用id（即应用包名）
     * @return 0 - 未下载，1 - 已下载，2 - 已安装，3 - 待更新，4 - 安装中
     */
    int getAppState(String id);
    /**
     * 获取当前应用升级描述
     * @param id 应用id（即应用包名）
     * @return 应用新版本描述
     */
    String getAppUpgradeDesc(String id);
}