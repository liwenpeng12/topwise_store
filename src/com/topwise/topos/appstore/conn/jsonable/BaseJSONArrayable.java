package com.topwise.topos.appstore.conn.jsonable;

import org.json.JSONArray;
import org.json.JSONException;

public abstract class BaseJSONArrayable extends JSONHelper implements JSONArrayable{
	@Override
	public String toString() {
		JSONArray obj = new JSONArray();
		try {
			this.writeToJSON(obj);
		} catch (JSONException e) {
		}
		return obj.toString();
	}
}
