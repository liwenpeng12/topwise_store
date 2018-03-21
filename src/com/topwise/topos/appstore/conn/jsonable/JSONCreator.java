package com.topwise.topos.appstore.conn.jsonable;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONCreator<T extends JSONable> implements JSONable.Creator<T>{
	Class<T> mClassT;
	public JSONCreator(Class<T> classT) {
		this.mClassT = classT;
	}
    public T createFromJSON(JSONObject source) throws JSONException {
		T object;
		try {
			object = mClassT.newInstance();
		} catch (Exception e) {
			throw new JSONException("newInstance failed!");
		}
		object.readFromJSON(source);
		return object;
    }
}