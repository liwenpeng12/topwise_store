package com.topwise.topos.appstore;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class AppServiceWatch {
	public static final String INTENT_ACTION = "com.yulong.action.YLUIWatchService";
	public static final String INTENT_CONTENT_PACKAGENAME = "packageName";
	public static final String INTENT_CONTENT_CLASSNAME = "className";
	public static final String INTENT_CONTENT_EVENT = "event";
	public static final int EVENT_TIME_TICK = 2;
	public static final int EVENT_TIME_STOP = 3;
	public static final int EVENT_TIME_START = 4;
	public static final String INTENT_CONTENT_TYPE = "type";
	public static final int COMPONENT_TYPE_SERVICE = 1;
	public static final String INTENT_CONTENT_TIME = "time";

	private Context mContext;
	private String mPackageName;
	private String mClassName;

	public AppServiceWatch(Context context, String packageName, String className) {
		mContext = context;
		mPackageName = packageName;
		mClassName = className;
	}

	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(Message msg) {
			timeTick();
		};
	};

	public void startWatchService() {
		Intent intent = new Intent();
		intent.setAction(INTENT_ACTION);
		intent.putExtra(INTENT_CONTENT_EVENT, EVENT_TIME_START);
		intent.putExtra(INTENT_CONTENT_TYPE, COMPONENT_TYPE_SERVICE);
		intent.putExtra(INTENT_CONTENT_PACKAGENAME, mPackageName);
		intent.putExtra(INTENT_CONTENT_CLASSNAME, mClassName);
		mContext.sendBroadcast(intent);
		
		timeTick();
	}
	
	public void stop() {
		mHandler.removeMessages(0);
	}

	private void timeTick() {
		mHandler.removeMessages(0);
		
		Intent intent = new Intent();
		intent.setAction(INTENT_ACTION);
		intent.putExtra(INTENT_CONTENT_EVENT, EVENT_TIME_TICK);
		intent.putExtra(INTENT_CONTENT_TYPE, COMPONENT_TYPE_SERVICE);
		intent.putExtra(INTENT_CONTENT_TIME, 601 * 1000);
		intent.putExtra(INTENT_CONTENT_PACKAGENAME, mPackageName);
		intent.putExtra(INTENT_CONTENT_CLASSNAME, mClassName);

		mContext.sendBroadcast(intent);
		mHandler.sendEmptyMessageDelayed(0, 600 * 1000);
	}
}
