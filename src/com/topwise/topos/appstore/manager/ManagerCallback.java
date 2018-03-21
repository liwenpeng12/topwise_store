package com.topwise.topos.appstore.manager;

public interface ManagerCallback {
    void onSuccess(String moduleType, int dataType, int page, int num, boolean end);
    void onFailure(String moduleType, int dataType, Throwable t, int errorNo, String strMsg);
}
