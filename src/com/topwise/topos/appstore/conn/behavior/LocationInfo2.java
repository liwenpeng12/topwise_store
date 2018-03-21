package com.topwise.topos.appstore.conn.behavior;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import com.topwise.topos.appstore.conn.jsonable.BaseJSONable;
import com.topwise.topos.appstore.conn.jsonable.JSONable;
import com.topwise.topos.appstore.conn.jsonable.Streamable;

/**
 * 兼容LocationInfo
 * @author ganggang
 *
 */
public class LocationInfo2 extends BaseJSONable implements Streamable{
	static LocationInfo2 mLocationInfo = new LocationInfo2();
	public String city = "";
	public String addr = "";
	public double longitude;
	public double latitude;
	public LocationInfo2() {
	}
	public LocationInfo2(String city,String addr,double longitude,double latitude) {
		this.city = city;
		this.addr = addr;
		this.longitude = longitude;
		this.latitude = latitude;
	}
	public LocationInfo2(LocationInfo2 info) {
		this.city = info.city;
		this.addr = info.addr;
		this.longitude = info.longitude;
		this.latitude = info.latitude;
	}
	public static LocationInfo2 getStaticLocationInfo() {
		return mLocationInfo;
	}
	public static LocationInfo2 getLocationInfo() {
		return new LocationInfo2(mLocationInfo);
	}
	public static void setLocationInfo(String city,String addr,double longitude,double latitude) {
		mLocationInfo = new LocationInfo2(city,addr,longitude,latitude);
	}
	@Override
	public void writeToJSON(JSONObject dest) throws JSONException {
		dest.put("city", city);
		dest.put("addr", addr);
		dest.put("longitude", longitude);
		dest.put("latitude", latitude);
	}

	@Override
	public void readFromJSON(JSONObject source) throws JSONException {
		city = source.getString("city");
		if(source.has("addr")) {
			this.addr = source.getString("addr");
		}
		if(source.has("longitude")) {
			this.longitude = source.getDouble("longitude");
		}
		if(source.has("latitude")) {
			this.latitude = source.getDouble("latitude");
		}
	}
	public static JSONable.Creator<LocationInfo2> CREATOR = new JSONable.Creator<LocationInfo2>() {
		@Override
		public LocationInfo2 createFromJSON(JSONObject source) throws JSONException {
			LocationInfo2 object = new LocationInfo2();
			object.readFromJSON(source);
			return object;
		}
	};
	@Override
	public void writeToStream(OutputStream os) throws IOException {
		DataOutputStream dos = new DataOutputStream(os);
		dos.writeUTF(this.city);
		dos.writeUTF(this.addr);
		dos.writeDouble(this.longitude);
		dos.writeDouble(this.latitude);
	}
	@Override
	public void readFromStream(InputStream is) throws IOException {
		DataInputStream dis = new DataInputStream(is);
		this.city = dis.readUTF();
		this.addr = dis.readUTF();
		this.longitude = dis.readDouble();
		this.latitude = dis.readDouble();
	}
}
