package com.topwise.topos.appstore.conn.behavior;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.HashMap;

public class BehaviorCreator {
	static HashMap<Integer,Class<? extends Behavior>> typeClassMap = new HashMap<Integer,Class<? extends Behavior>>();
	static {
		typeClassMap.put(BehaviorType.BEHAVIOR_TYPE_END, EndBehavior.class);
		typeClassMap.put(BehaviorType.BEHAVIOR_TYPE_COMMON, CommonBehavior.class);
		typeClassMap.put(BehaviorType.BEHAVIOR_TYPE_LOCATION2, Location2Behavior.class);
		typeClassMap.put(BehaviorType.BEHAVIOR_TYPE_DEVICEINFO2, DeviceInfo2Behavior.class);
		typeClassMap.put(BehaviorType.BEHAVIOR_TYPE_STARTACTIVITY, StartActivityBehavior.class);
	}
	public static Behavior readBehaviorFromStream(InputStream is) throws Exception{
		DataInputStream dis = new DataInputStream(is);
		int type = dis.readInt();
		Behavior behavior = null;
		Class<? extends Behavior> classBehavior = typeClassMap.get(type);
		if(classBehavior != null) {
			behavior = classBehavior.newInstance();
		}
		if(behavior != null){
			behavior.readFromStreamNoType(is);
			behavior.type = type;
		}
		return behavior;
	}
}
