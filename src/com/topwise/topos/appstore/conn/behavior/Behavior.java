package com.topwise.topos.appstore.conn.behavior;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import com.topwise.topos.appstore.conn.jsonable.Streamable;

public abstract class Behavior implements Streamable{
	public int type;
	public long time;
	public Behavior(int type) {
		this.type = type;
		time = System.currentTimeMillis();
	}
	@Override
	public void readFromStream(InputStream is) throws IOException {
		DataInputStream dis = new DataInputStream(is);
		this.type = dis.readInt();
		readFromStreamNoType(is);
	}
	public void readFromStreamNoType(InputStream is) throws IOException {
		DataInputStream dis = new DataInputStream(is);
		this.time = dis.readLong();
	}

	@Override
	public void writeToStream(OutputStream dest) throws IOException {
		DataOutputStream dos = new DataOutputStream(dest);
		dos.writeInt(this.type);
		dos.writeLong(this.time);
	}
	public String toString() {
        return " type:" + BehaviorType.getTypeString(this.type) + " time:" + new Date(time).toString();
	}
}
