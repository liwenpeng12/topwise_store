package com.topwise.topos.appstore.conn.jsonable;

import org.json.JSONArray;
import org.json.JSONException;

import com.topwise.topos.appstore.conn.jsonable.JSONArrayable.Creator;

public class JSONArrayCreator<T extends JSONArrayable> implements Creator<T>{
	public Class<T> mClassT;
	public JSONArrayCreator(Class<T> classT) {
		mClassT = classT;
	}
	@Override
	public T createFromJSON(JSONArray source) throws JSONException {
		T t;
		try {
			t = mClassT.newInstance();
		} catch (Exception e) {
			throw new JSONException("newInstance failed!");
		} 
		t.readFromJSON(source);
		return t;
	}
}
