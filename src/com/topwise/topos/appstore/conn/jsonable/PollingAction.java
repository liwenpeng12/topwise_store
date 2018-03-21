package com.topwise.topos.appstore.conn.jsonable;

import org.json.JSONException;
import org.json.JSONObject;

public class PollingAction extends BaseJSONable{
	public String action = "";
	public String url = "";
	public String param = "";
	@Override
	public void writeToJSON(JSONObject dest) throws JSONException {
		dest.put("action", action);
		dest.put("url", url);
		dest.put("param", param);
	}
	@Override
	public void readFromJSON(JSONObject source) throws JSONException {
		action = source.getString("action");
		url = source.getString("url");
		param = source.getString("param");
	}
	public static Creator<PollingAction> CREATOR = new Creator<PollingAction>() {
		@Override
		public PollingAction createFromJSON(JSONObject source) throws JSONException {
			PollingAction object = new PollingAction();
			object.readFromJSON(source);
			return object;
		}
	};
}
