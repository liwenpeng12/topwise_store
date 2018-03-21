package com.topwise.topos.appstore.conn.jsonable;

import org.json.JSONArray;
import org.json.JSONException;

public interface JSONArrayable {
	public void writeToJSON(JSONArray dest) throws JSONException;
	public void readFromJSON(JSONArray source) throws JSONException;
    public interface Creator<T extends JSONArrayable> {
        public T createFromJSON(JSONArray source) throws JSONException;
    }
}
