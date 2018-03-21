package com.topwise.topos.appstore.conn.behavior;

import org.json.JSONException;
import org.json.JSONObject;

import com.topwise.topos.appstore.conn.jsonable.BaseJSONable;
import com.topwise.topos.appstore.conn.jsonable.JSONCreator;

public class BehaviorEx extends BaseJSONable {
    public static final String TYPE = "a";
    public static final String CONTENT = "b";
    public int type = 5; // 固定分配给应用商店的type，不得更改
    public String content = "";

    public static final int START_APP = 0;
    public static final int ENTER_WELCOME = 1;
    public static final int SKIP_WELCOME = 2;
    public static final int VIEW_WELCOME = 3;
    public static final int SHOW_NOTIFICATION = 4;
    public static final int ENTER_NOTIFICATION = 5;
    public static final int VIEW_BANNER = 6;
    public static final int ENTER_BANNER = 7;
    public static final int VIEW_ADICON = 8;
    public static final int ENTER_ADICON = 9;
    public static final int VIEW_APP = 10;
    public static final int ENTER_APP_DETAIL = 11;
    public static final int SHOW_ADS_AD = 12;
    public static final int CLICK_ADS_AD = 13;

    public BehaviorEx() {
    }
    
    public BehaviorEx(int key, String content) {
        this.content = "" + key + ";" + content;
    }

    @Override
    public void writeToJSON(JSONObject dest) throws JSONException {
        dest.put(TYPE, type);
        if (content != null && !content.equals("")) {
            dest.put(CONTENT, content);
        }
    }

    @Override
    public void readFromJSON(JSONObject source) throws JSONException {
        type = source.getInt(TYPE);
        if (source.has(CONTENT)) {
            content = source.getString(CONTENT);
        }
    }

    public static int getType(JSONObject json) {
        try {
            return json.getInt(TYPE);
        } catch (JSONException e) {
        }
        return -1;
    }

    public static JSONCreator<BehaviorEx> CREATOR = new JSONCreator<BehaviorEx>(BehaviorEx.class);
}
