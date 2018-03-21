package com.topwise.topos.appstore.conn.jsonable;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PollingResponse extends BaseJSONable {
	public long clientID;
	public long pollingTime;
	public ArrayList<PollingAction> actions = new ArrayList<PollingAction>();
	@Override
	public void writeToJSON(JSONObject dest) throws JSONException {
		dest.put("pollingTime", pollingTime);
		JSONArray jsonArray = new JSONArray();
		for(PollingAction action:actions) {
			JSONObject object = new JSONObject();
			action.writeToJSON(object);
			jsonArray.put(object);
		}
		dest.put("actions", jsonArray);
		dest.put("clientID", clientID);
	}

	@Override
	public void readFromJSON(JSONObject source) throws JSONException {
		this.pollingTime = source.getLong("pollingTime");
		actions.clear();
		JSONArray jsonArray = source.getJSONArray("actions");
		for(int i = 0; i<jsonArray.length();i++) {
			JSONObject object = jsonArray.getJSONObject(i);
			PollingAction action = PollingAction.CREATOR.createFromJSON(object);
			actions.add(action);
		}
		if(source.has("clientID")) {
			this.clientID = source.getLong("clientID");
		}
		
	}
	public static JSONCreator<PollingResponse> CREATOR = new JSONCreator<PollingResponse>(PollingResponse.class);
}
