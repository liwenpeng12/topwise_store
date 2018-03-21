package com.topwise.topos.appstore.conn.behavior;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Location2Behavior extends Behavior {
	public LocationInfo2 locationInfo;
	public Location2Behavior() {
		this(new LocationInfo2());
	}
	public Location2Behavior(LocationInfo2 locationInfo) {
		super(BehaviorType.BEHAVIOR_TYPE_LOCATION2);
		this.locationInfo = locationInfo;
	}
	@Override
	public void writeToStream(OutputStream os) throws IOException {
		super.writeToStream(os);
		this.locationInfo.writeToStream(os);
	}
	public void readFromStreamNoType(InputStream is) throws IOException {
		super.readFromStreamNoType(is);
		this.locationInfo.readFromStream(is);
	}
	public String toString() {
		return super.toString() + "locationInfo2:" + locationInfo;
	}
}
