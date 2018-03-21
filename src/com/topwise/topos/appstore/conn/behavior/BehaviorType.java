package com.topwise.topos.appstore.conn.behavior;

public class BehaviorType {
	public static final int BEHAVIOR_TYPE_END = -1;
	public static final int BEHAVIOR_TYPE_COMMON = 0;
	public static final int BEHAVIOR_TYPE_SCREENON = 3;
	public static final int BEHAVIOR_TYPE_SCREENOFF = 6;
	public static final int BEHAVIOR_TYPE_LOCATION2 = 10;
	public static final int BEHAVIOR_TYPE_DEVICEINFO2 = 11;
	public static final int BEHAVIOR_TYPE_STARTACTIVITY = 12;
	public static String getTypeString(int type) {
		switch(type) {
		case BEHAVIOR_TYPE_END:
			return "BEHAVIOR_TYPE_END";
		case BEHAVIOR_TYPE_COMMON:
			return "BEHAVIOR_TYPE_COMMON";
		case BEHAVIOR_TYPE_SCREENON:
			return "BEHAVIOR_TYPE_SCREENON";
		case BEHAVIOR_TYPE_SCREENOFF:
			return "BEHAVIOR_TYPE_SCREENOFF";
		case BEHAVIOR_TYPE_LOCATION2:
			return "BEHAVIOR_TYPE_LOCATION2";
		case BEHAVIOR_TYPE_DEVICEINFO2:
			return "BEHAVIOR_TYPE_DEVICEINFO2";
		case BEHAVIOR_TYPE_STARTACTIVITY:
			return "BEHAVIOR_TYPE_STARTACTIVITY";
		}
		return "BEHAVIOR_TYPE_UNKNOWN";
	}
}
