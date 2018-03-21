package com.topwise.topos.appstore.conn.jsonable;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class BaseJSONable extends JSONHelper implements JSONable{
	@Override
	public String toString() {
		JSONObject obj = new JSONObject();
		try {
			this.writeToJSON(obj);
		} catch (JSONException e) {
		}
		return obj.toString();
	}
}
