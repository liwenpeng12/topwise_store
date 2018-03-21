package com.topwise.topos.appstore.api;

interface IDownloadStateListener {
    /**
     * 下载状态
     * @param state 0 - 未下载，1 - 已下载，2 - 已安装，3 - 待更新，4 - 安装中，5 - 暂停下载，6 - 继续下载，7 - 取消下载
     */
    void onDownloadStateChanged(String id, int state);
    /**
     * 下载进度
     * @param progress 进度 1至100
     */
    void onDownloadProgressChanged(String id, int progress);
    /**
     * 下载速度
     * @param speed 速度
     */
    void onDownloadSpeedChanged(String id, int speed);
}