package com.topwise.topos.appstore.conn.behavior;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CommonBehavior extends Behavior{
	String data;
	public CommonBehavior() {
		this("");
	}
	public CommonBehavior(String data) {
		super(BehaviorType.BEHAVIOR_TYPE_COMMON);
		this.data = data;
	}
	@Override
	public void writeToStream(OutputStream os) throws IOException {
		super.writeToStream(os);
		DataOutputStream dos = new DataOutputStream(os);
		dos.writeUTF(this.data);
	}
	public void readFromStreamNoType(InputStream is) throws IOException {
		super.readFromStreamNoType(is);
		DataInputStream dis = new DataInputStream(is);
		this.data = dis.readUTF();
	}
	public String toString() {
		return super.toString() + " data:" + data;
	}
}
