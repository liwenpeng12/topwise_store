package com.topwise.topos.appstore.conn.jsonable;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.topwise.topos.appstore.conn.jsonable.JSONable.Creator;

public class JSONHelper {
	public static <T extends JSONable> JSONArray list2JSONArray(List<T> array) throws JSONException {
		JSONArray jsonArray = new JSONArray();
		for(int i = 0 ;i < array.size();i++) {
			JSONObject jsonObjct = new JSONObject();
			array.get(i).writeToJSON(jsonObjct);
			jsonArray.put(jsonObjct);
		}
		return jsonArray;
	}
	
	public  static <T extends JSONable> List<T> jSONArray2List(JSONArray jsonArray,Creator<T> creater,List<T> list) throws JSONException {
		for(int i = 0;  i< jsonArray.length();i++) {
			JSONObject object = jsonArray.getJSONObject(i);
			T t = creater.createFromJSON(object);
			list.add(t);
		}
		return list;
	}
	public  static <T extends JSONable> List<T> jSONArray2List(JSONArray jsonArray,Creator<T> creater) throws JSONException {
		List<T> list = new ArrayList<T>();
		return jSONArray2List(jsonArray,creater,list);
	}
}
