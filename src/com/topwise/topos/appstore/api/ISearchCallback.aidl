package com.topwise.topos.appstore.api;

interface ISearchCallback {
    /**
     * 搜索成功
     * @param json 应用列表
     */
    void onSearchSuccess(String json);
    /**
     * 搜索失败
     * @param error 失败原因
     */
    void onSearchFail(String error);
}