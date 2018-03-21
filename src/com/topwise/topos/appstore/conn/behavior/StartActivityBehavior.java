package com.topwise.topos.appstore.conn.behavior;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StartActivityBehavior extends Behavior{
	String intentContent;
	public StartActivityBehavior() {
		this("");
	}
	public StartActivityBehavior(String intentContent) {
		super(BehaviorType.BEHAVIOR_TYPE_STARTACTIVITY);
		this.intentContent = intentContent;
	}
	@Override
	public void writeToStream(OutputStream os) throws IOException {
		super.writeToStream(os);
		DataOutputStream dos = new DataOutputStream(os);
		dos.writeUTF(this.intentContent);
	}
	public void readFromStreamNoType(InputStream is) throws IOException {
		super.readFromStreamNoType(is);
		DataInputStream dis = new DataInputStream(is);
		this.intentContent = dis.readUTF();
	}
	public String toString() {
		return super.toString() + " intentContent:" + intentContent;
	}
}
