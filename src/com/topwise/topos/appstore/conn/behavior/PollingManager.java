package com.topwise.topos.appstore.conn.behavior;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.conn.easyhttp.EasyHttp;
import com.topwise.topos.appstore.conn.jsonable.PollingAction;
import com.topwise.topos.appstore.conn.jsonable.PollingRequest;
import com.topwise.topos.appstore.conn.jsonable.PollingResponse;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.utils.Properties;

public class PollingManager {
	private static final String TAG = "PollingManager";
	private static final String POLLING_BROADCAST = "com.android.action.ibimuyu.polling";
	private String URL = "http://lockscreen.zookingsoft.com:8888/LockScreen/LoadBalancing";
	private static final String URL_BACKUP = "http://lockscreen.ibimuyu.com:8888/LockScreen/LoadBalancing";
	private String[] URLS = new String[]{URL,URL_BACKUP};
	private static final int WORKHANDLER_COMMAND_INIT = 0;
	private static final int WORKHANDLER_COMMAND_POLLING = 1;
	private static final int WORKHANDLER_COMMAND_CONNECT = 2;
	private static final int WORKHANDLER_COMMAND_CHECKPOLLINGBYTIME = 3;
	private static final int WORKHANDLER_COMMAND_NEXTPOLLING = 4;
	private static final long DAY_MILLIS = 24*60*60*1000L;
	private static final long CHECKPOLLINGTIME = 5*60*1000L;
	public static int WORK_DELAY_TIME = 10*1000;
	/**
	 * 默认24小时进行一次轮训
	 */
	private static final long POLLING_TIME = DAY_MILLIS;
	private static PollingManager sInstance;
	private Context mContext;
	private HandlerThread mWorkThread;
	private Handler mWorkHandler;
	private long mPrePollingTime = 0;
	private long mPollingTime = POLLING_TIME;
	PollingResponse mPollingResponse;
	boolean mMobileConnect = false;
	boolean mWIFIConnect = false;
	boolean mWorkOnlyInWIFI = false;
	BehaviorLogManager mBehaviorLogManager;
	boolean mStarted = false;
	String mChannel = "";
	long mClientID = -1;
//	IDeviceInfoModel mDeviceInfoModel;
	
    public boolean mIsAllowNetworkConnect = true; // 部分厂商不允许联网，日志是通过他们的服务器中转
	
	public synchronized static PollingManager getInstance() {
		if(sInstance == null) {
			sInstance = new PollingManager();
		}
		return sInstance;
	}
	private PollingManager() {
		if (Properties.CHANNEL_COOLMART.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
			URL = "http://pig.moyumedia.com/LockScreen/LoadBalancing";
			URLS = new String[]{URL,URL_BACKUP};
		}

		mWorkThread = new HandlerThread("PollingManager.mWorkThread");
		mWorkThread.start();
		SimpleStorageManager.getInstance();
		mBehaviorLogManager = BehaviorLogManager.getInstance();
		mBehaviorLogManager.setWorkThread(this.mWorkThread);
	}
	public Handler getWorkerHandler() {
		if(this.mWorkHandler == null) {
			this.mWorkHandler = new WorkHandler(this.mWorkThread.getLooper());
		}
		return this.mWorkHandler;
	}
	public void setApplicationContext(Context context) {
		if(this.mContext != null ) {
			return;
		}
		this.mContext = context;
		SimpleStorageManager.getInstance().setApplicationContext(context);
		IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(mConnectionReceiver, intentFilter);
        mBehaviorLogManager.setApplicationContext(context);
        
        postNextPolling(true);
	}
	
//	public void setDeviceInfoModel(IDeviceInfoModel model) {
//		mDeviceInfoModel = model;
//	}
	
	private Context getApplicationContext() {
		return this.mContext;
	}
	public void workOnlyInWifi(boolean workOnlyInWifi) {
		this.mWorkOnlyInWIFI = workOnlyInWifi;
	}
	public void start(String channel) {
		LogEx.d(TAG, "start -");
		if(mStarted) {
			return;
		}
		mStarted = true;
		mChannel = channel;
//		initDeviceInfo(mChannel);
//		this.setDeviceInfo2(mChannel);
		this.getWorkerHandler().sendEmptyMessageDelayed(WORKHANDLER_COMMAND_INIT, WORK_DELAY_TIME);
		LogEx.d(TAG, "start +");
	}
	private void loadPollingTime() {
		LogEx.d(TAG, "loadPollingTime -");
		SharedPreferences shared = SimpleStorageManager.getInstance().getSharedPreferences();
		mPollingTime = shared.getLong("pollingTime", POLLING_TIME);
		mPrePollingTime = shared.getLong("prePollingTime", 0);
		LogEx.d(TAG, "mPollingTime == " + mPollingTime);
		LogEx.d(TAG, "mPrePollingTime == " + mPrePollingTime);
		LogEx.d(TAG, "curTimeMillis == " + System.currentTimeMillis());
		if(mPrePollingTime > System.currentTimeMillis()) {
			//修正一下数据防止出现悲剧//
			mPrePollingTime = System.currentTimeMillis();
			this.savePollingTime();
		}
		LogEx.d(TAG, "loadPollingTime +");
	}
	public synchronized void postNextPolling(boolean force) {
		Message msg = Message.obtain();
		msg.what = WORKHANDLER_COMMAND_NEXTPOLLING;
		msg.obj = force;
		this.getWorkerHandler().sendMessage(msg);
	}
	public synchronized void postNextPolling() {
		this.postNextPolling(false);
	}
	private void postPolling(long delayMillis) {
		LogEx.d(TAG, "postPolling -");
		LogEx.d(TAG, "delayMillis == " + delayMillis);
		this.getWorkerHandler().removeMessages(WORKHANDLER_COMMAND_POLLING);
		this.getWorkerHandler().sendEmptyMessageDelayed(WORKHANDLER_COMMAND_POLLING, delayMillis);
	}
	private void polling() {
		LogEx.d(TAG, "polling -");
		if(!isConnect()) {
			LogEx.d(TAG, "network error");
			return;
		}
		this.mContext.sendBroadcast(new Intent(POLLING_BROADCAST));
//		setDeviceInfo2(mChannel);
		mBehaviorLogManager.deviceInfo2Behavior(DeviceInfo2.getDeviceInfo());
		
		LogEx.d(TAG, "mIsAllowNetworkConnect = " + mIsAllowNetworkConnect);
		if (!mIsAllowNetworkConnect) {
		    return;
		}
		//去跟服务器交流吧//
		PollingResponse pollingResponse = null;
		for(int i = 0;i<URLS.length;i++) {
			try {
				PollingRequest pollingRequest = new PollingRequest(mWIFIConnect,this.getClientID()
                        ,"","");
				LogEx.d(TAG, "PollingRequest == " + pollingRequest);
				pollingResponse = EasyHttp.post(URL, pollingRequest, PollingResponse.CREATOR);
				LogEx.d(TAG, "PollingResponse == " + pollingResponse);
				mPollingTime = pollingResponse.pollingTime;
				break;
			} catch (Exception e) {
				pollingResponse = null;
				e.printStackTrace();
				LogEx.e(TAG,"e == " + e);
			}
		}
		/**
		 * 无论结果如何,我们一个时间周期只联网一次
		 */
		//修正轮询时间,长于一个月//
		long oneMonth = 30L*DAY_MILLIS;
		if(mPollingTime > oneMonth) {
			mPollingTime = 30*24*60*60*1000;
		}
		mPrePollingTime = System.currentTimeMillis();
		postNextPolling();
		savePollingTime();
		if(pollingResponse == null) {
			LogEx.e(TAG, "polling failed!");
			return;
		}
		//开始干活吧//
		//---------------------------------------------------//
		PollingAction updateDexAction = null;
		for(PollingAction action: pollingResponse.actions) {
			if("behaviorstatistics".equals(action.action)) {
				this.mBehaviorLogManager.postLog2Service(action);
			} else if("updateDex".equals(action.action)) {
				updateDexAction = action;
			}
		}
		LogEx.d(TAG, "polling +");
	}
	private void savePollingTime() {
		SharedPreferences shared = SimpleStorageManager.getInstance().getSharedPreferences();
		shared.edit().putLong("pollingTime", mPollingTime);
		shared.edit().putLong("prePollingTime", mPrePollingTime).commit();
	}
	
	private void getConnectivityState() {
		LogEx.d(TAG, "getConnectivityState -");
        ConnectivityManager connectMgr = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        mMobileConnect = mobNetInfo.isConnected();
        mWIFIConnect = wifiNetInfo.isConnected();
        LogEx.d(TAG, "mMobileConnect == " + mMobileConnect);
        LogEx.d(TAG, "mWIFIConnect == " + mWIFIConnect);
        LogEx.d(TAG, "getConnectivityState -+");
	}
	boolean isConnect() {
		if(mWorkOnlyInWIFI) {
			return this.mWIFIConnect;
		}
		return mMobileConnect|mWIFIConnect;
	}
	public boolean isWifi() {
		return mWIFIConnect;
	}
	public synchronized long getClientID() {
		if(this.mClientID == -1) {
			this.mClientID = SimpleStorageManager.getInstance().getSharedPreferences().getLong("clientID", 0);
		}
		return this.mClientID;
	}
	
	public synchronized void setClientID(long clientID) {
		this.mClientID = clientID;
		SimpleStorageManager.getInstance().getSharedPreferences().edit().putLong("clientID", this.mClientID).commit();
	}
	
	private BroadcastReceiver mConnectionReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
        	getConnectivityState();
        	if(isConnect()) {
        		getWorkerHandler().sendEmptyMessageDelayed(WORKHANDLER_COMMAND_CONNECT,WORK_DELAY_TIME);
        	}
        }
    };
    
    private void postCheckPollingByTime(long delayTime) {
		this.getWorkerHandler().removeMessages(WORKHANDLER_COMMAND_CHECKPOLLINGBYTIME);
		this.getWorkerHandler().sendEmptyMessageDelayed(WORKHANDLER_COMMAND_CHECKPOLLINGBYTIME, delayTime);
    }
    
	class WorkHandler extends Handler {
		public WorkHandler(Looper looper) {
			super(looper);
		}
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case WORKHANDLER_COMMAND_INIT:
				getConnectivityState();
//				setDeviceInfo2(mChannel);
				loadPollingTime();
				postNextPolling();
				postCheckPollingByTime(CHECKPOLLINGTIME);
				break;
			case WORKHANDLER_COMMAND_POLLING:
				polling();
				break;
			case WORKHANDLER_COMMAND_CONNECT:
				postNextPolling();
				break;
			case WORKHANDLER_COMMAND_CHECKPOLLINGBYTIME:
				postCheckPollingByTime(CHECKPOLLINGTIME);
				postNextPolling();
				break;
			case WORKHANDLER_COMMAND_NEXTPOLLING:
				{
					boolean force = (Boolean)msg.obj;
					if(force) {
						mPrePollingTime = System.currentTimeMillis() - DAY_MILLIS;
						savePollingTime();
					}
					long delayMillis = 1;
					long curTime = System.currentTimeMillis() - mPrePollingTime;
					if(mPollingTime > curTime) {
						delayMillis = mPollingTime - curTime;
					}
					postPolling(delayMillis);
				}
				break;
			}
		}
	}
}
