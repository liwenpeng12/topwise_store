package com.topwise.topos.appstore.conn.jsonable;

import org.json.JSONException;
import org.json.JSONObject;

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