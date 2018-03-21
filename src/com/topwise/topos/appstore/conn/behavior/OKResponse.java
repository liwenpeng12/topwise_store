package com.topwise.topos.appstore.conn.behavior;

import org.json.JSONException;
import org.json.JSONObject;

import com.topwise.topos.appstore.conn.jsonable.BaseJSONable;
import com.topwise.topos.appstore.conn.jsonable.JSONCreator;

public class OKResponse extends BaseJSONable {
	public int ok;

	@Override
	public void writeToJSON(JSONObject dest) throws JSONException {
		dest.put("ok", ok);
	}

	@Override
	public void readFromJSON(JSONObject source) throws JSONException {
		ok = source.getInt("ok");
	}
	public static JSONCreator<OKResponse> CREATOR = new JSONCreator<OKResponse>(OKResponse.class);
}