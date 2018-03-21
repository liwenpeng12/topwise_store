package com.topwise.topos.appstore.conn.jsonable;

import org.json.JSONException;
import org.json.JSONObject;

public interface JSONable {
	public void writeToJSON(JSONObject dest) throws JSONException;
	public void readFromJSON(JSONObject source) throws JSONException;
    public interface Creator<T extends JSONable> {
        public T createFromJSON(JSONObject source) throws JSONException;
    }
}
