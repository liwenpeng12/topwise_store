package com.topwise.topos.appstore.conn.behavior;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
 * json属性上兼容DeviceInfo
 * stream属性上无法兼容DeviceInfo
 * @author ganggang
 *
 */
public class DeviceInfo2 extends BaseJSONable implements Streamable{
	static DeviceInfo2 mDeviceInfo = new DeviceInfo2();
	public String imei = "";
	public String channel = "";
	public int androidVersion;
	public String phoneModel = "";
	public String networkOperatorName = "";
	public int networkType = 0;
	public DeviceInfo2() {
	}
	public DeviceInfo2(String imei,String channel,int androidVersion,String phoneModel,String networkOperatorName,int networkType) {
		setValue(imei,channel,androidVersion,phoneModel,networkOperatorName,networkType);
	}
	public DeviceInfo2(DeviceInfo2 info) {
		setValue(info.imei,info.channel,info.androidVersion,info.phoneModel,info.networkOperatorName,info.networkType);
	}
	public static DeviceInfo2 getStaticDeviceInfo() {
		return mDeviceInfo;
	}
	public static DeviceInfo2 getDeviceInfo() {
		return new DeviceInfo2(mDeviceInfo);
	}
	public static void setDeviceInfo(String imei,String channel,int androidVersion,String phoneModel,String networkOperatorName,int networkType) {
		mDeviceInfo.setValue(imei,channel,androidVersion,phoneModel,networkOperatorName,networkType);;
	}
	public void setValue(String imei,String channel,int androidVersion,String phoneModel,String networkOperatorName,int networkType) {
		this.imei = imei;
		this.channel = channel;
		this.androidVersion = androidVersion;
		this.phoneModel = phoneModel;
		this.networkOperatorName = networkOperatorName;
		this.networkType = networkType;
	}
	public InputStream toInputStream() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
		this.writeToStream(bos);
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		return bis;
	}
	@Override
	public void readFromStream(InputStream is) throws IOException {
		DataInputStream dis = new DataInputStream(is);
		this.imei = dis.readUTF();
		this.channel = dis.readUTF();
		this.androidVersion = dis.readInt();
		this.phoneModel = dis.readUTF();
		this.networkOperatorName = dis.readUTF();
		this.networkType = dis.readInt();
	}

	@Override
	public void writeToStream(OutputStream dest) throws IOException {
		DataOutputStream dos = new DataOutputStream(dest);
		dos.writeUTF(this.imei);
		dos.writeUTF(this.channel);
		dos.writeInt(this.androidVersion);
		dos.writeUTF(this.phoneModel);
		dos.writeUTF(this.networkOperatorName);
		dos.writeInt(this.networkType);
	}
	@Override
	public void writeToJSON(JSONObject dest) throws JSONException {
		dest.put("imei", this.imei);
		dest.put("channel", this.channel);
		dest.put("androidVersion", this.androidVersion);
		dest.put("phoneModel", this.phoneModel);
		dest.put("networkOperatorName", this.networkOperatorName);
		dest.put("networkType", this.networkType);
	}
	@Override
	public void readFromJSON(JSONObject source) throws JSONException {
		this.imei = source.getString("imei");
		this.channel = source.getString("channel");
		this.androidVersion = source.getInt("androidVersion");
		this.phoneModel = source.getString("phoneModel");
		if(source.has("networkOperatorName")) {
			this.networkOperatorName = source.getString("networkOperatorName");
		}
		if(source.has("networkType")) {
			this.networkType = source.getInt("networkType");
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		if( !(o instanceof DeviceInfo2) ) {
			return false;
		}
		DeviceInfo2 dst = (DeviceInfo2)o;
		if(!this.imei.equals(dst.imei)) {
			return false;
		}
		if(!this.channel.equals(dst.channel)) {
			return false;
		}
		if(this.androidVersion != dst.androidVersion) {
			return false;
		}
		if(!this.phoneModel.equals(dst.phoneModel)) {
			return false;
		}
		if(!this.networkOperatorName.equals(dst.networkOperatorName)) {
			return false;
		}
		if(this.networkType != dst.networkType) {
			return false;
		}
		return true;
	}
	
	public static JSONable.Creator<DeviceInfo2> CREATOR = new JSONable.Creator<DeviceInfo2>() {
		@Override
		public DeviceInfo2 createFromJSON(JSONObject source) throws JSONException {
			DeviceInfo2 object = new DeviceInfo2();
			object.readFromJSON(source);
			return object;
		}
	};
}
