package com.topwise.topos.appstore.conn.behavior;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DeviceInfo2Behavior extends Behavior {
	public DeviceInfo2 deviceInfo;
	public DeviceInfo2Behavior() {
		this(new DeviceInfo2());
	}
	public DeviceInfo2Behavior(DeviceInfo2 deviceInfo) {
		super(BehaviorType.BEHAVIOR_TYPE_DEVICEINFO2);
		this.deviceInfo = deviceInfo;
	}
	@Override
	public void writeToStream(OutputStream os) throws IOException {
		super.writeToStream(os);
		this.deviceInfo.writeToStream(os);
	}
	public void readFromStreamNoType(InputStream is) throws IOException {
		super.readFromStreamNoType(is);
		this.deviceInfo.readFromStream(is);
	}
	public String toString() {
		return super.toString() + "deviceInfo2:" + deviceInfo;
	}
}
