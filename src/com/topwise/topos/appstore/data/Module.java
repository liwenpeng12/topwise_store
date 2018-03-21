package com.topwise.topos.appstore.data;

import java.io.Serializable;

public class Module implements Serializable {

    private static final long serialVersionUID = 3495471754751097007L;
    
    public volatile String id; // 等于module_weight
    public volatile int module_weight; // 列表中的位置

}
