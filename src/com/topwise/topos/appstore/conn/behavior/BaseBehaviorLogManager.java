package com.topwise.topos.appstore.conn.behavior;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import com.topwise.topos.appstore.conn.easyhttp.EasyHttp;
import com.topwise.topos.appstore.conn.jsonable.PollingAction;
import com.topwise.topos.appstore.utils.FileUtil;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.utils.Properties;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public abstract class BaseBehaviorLogManager {
    
    private static final String TAG = "BehaviorLogManagerBase";
    public static final String BEHAVIOR_LOG_FILE = "behavior_log_file";
    public static final String BROADCAST_REMOTE_LOG = "com.ibimuyu.android.action.behaviorlog";
    public static final int MSG_WRITELOG = 1;
    
    public Context mContext;
    protected HandlerThread mWorkThread;
    protected Handler mWorkHandler;
    private BehaviorReceiver mBehaviorReceiver;

    protected BaseBehaviorLogManager() {
        mBehaviorReceiver = new BehaviorReceiver();
    }

    public void setApplicationContext(Context context) {
        if (mContext != null) {
            return;
        }
        mContext = context;
        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_REMOTE_LOG);
        context.registerReceiver(mBehaviorReceiver, filter);
    }

    public void setWorkThread(HandlerThread thread) {
        mWorkThread = thread;
    }

    public Handler getWorkerHandler() {
        Looper looper = Looper.getMainLooper();
        if (mWorkThread != null) {
            looper = mWorkThread.getLooper();
        }
        if (mWorkHandler == null) {
            mWorkHandler = new WorkHandler(looper);
        }
        return mWorkHandler;
    }

    public void postLog2Service(PollingAction action) {
        LogEx.d(TAG, "postLog2Service,action == " + action);
        File file = new File(Properties.BEHAVIOR_PATH + "/" + BEHAVIOR_LOG_FILE);
        if (!file.exists() || file.length() == 0) {
            LogEx.d(TAG, "has no log");
            return;
        }
        if (!file.isFile()) {
            file.delete();
            LogEx.d(TAG, "has no log");
            return;
        }
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            LogEx.d(TAG, "start transfer log");
            OKResponse okResponse = EasyHttp.post(action.url, new InputStream[] {
                is
            }, OKResponse.CREATOR);
            LogEx.d(TAG, "OKResponse == " + okResponse);
            if (okResponse.ok == 0) {
                // 删除文件内容//
                is.close();
                is = null;
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogEx.e(TAG, "e == " + e);
            try {
                // 凡是大于1M的日志,如果传输失败,直接删除,防止恶性循环//
                if (is != null) {
                    if (is.available() > 1024 * 1024) {
                        file.delete();
                    }
                }
            } catch (Exception e1) {
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
                is = null;
            }
        }
        LogEx.d(TAG, "postLog2Service +");
    }

    public void addCommonBehavior(String data) {
//        LogEx.d(TAG, "addCommonBehavior,data == " + data);
        CommonBehavior obj = new CommonBehavior(data);
        addBehavior(obj);
    }

    public void addBehaviorEx(BehaviorEx behaviorEx) {
        JSONObject dest = new JSONObject();
        try {
            behaviorEx.writeToJSON(dest);
        } catch (JSONException e) {
            return;
        }
        addCommonBehavior(dest.toString());
    }

    public void addBehavior(Behavior behavior) {
        try {
            Message msg = Message.obtain();
            msg.what = MSG_WRITELOG;
            msg.obj = behavior;
            Handler handler = this.getWorkerHandler();
            if (handler.getLooper().equals(Looper.myLooper())) {
                handler.handleMessage(msg);
            } else {
                handler.sendMessage(msg);
            }
        } catch (Exception e) {
            LogEx.w(TAG, "addBehavior() catch Exception!");
            e.printStackTrace();
        }
    }

    private File getLogFile() {
        File file = null;
        try {
            file = new File(Properties.BEHAVIOR_PATH + "/" + BEHAVIOR_LOG_FILE);
            if (file.isDirectory()) {
                file.delete();
            }
            if (!file.exists()) {
                FileUtil.createNewFile(file);
                // 先写上开头//
                FileOutputStream fis = null;
                try {
                    fis = new FileOutputStream(file, true);
                    DeviceInfo2.getStaticDeviceInfo().writeToStream(fis);
                } finally {
                    if (fis != null) {
                        fis.flush();
                        fis.close();
                    }
                }
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            LogEx.e(TAG, "getLogFile e == " + e);
            // 出现任何异常,知己删除文件//
            if (file != null) {
                file.delete();
            }
            return null;
        }
    }

    void writeLog(Behavior behavior) throws Exception {
        File file = getLogFile();
        if (file == null) {
            LogEx.e(TAG, "getLogFile error");
            return;
        }
        FileOutputStream fis = null;
        try {
            fis = new FileOutputStream(file, true);
            behavior.writeToStream(fis);
        } finally {
            if (fis != null) {
                fis.flush();
                fis.close();
            }
        }
    }

    class WorkHandler extends Handler {
        public WorkHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WRITELOG:
                    try {
                        writeLog((Behavior) msg.obj);
                    } catch (Exception e) {

                    }
                    break;
            }
        }
    }

    static class BehaviorReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogEx.d(TAG, "BehaviorReceiver.onReceive-");
            try {
                if (!BROADCAST_REMOTE_LOG.equals(intent.getAction())) {
                    return;
                }
                byte[] behaviorData = intent.getByteArrayExtra("behavior");
                ByteArrayInputStream is = new ByteArrayInputStream(behaviorData);
                Behavior behavior = BehaviorCreator.readBehaviorFromStream(is);
                BehaviorLogManager.getInstance().addBehavior(behavior);
            } catch (Exception e) {
                return;
            }
            LogEx.d(TAG, "BehaviorReceiver.onReceive+");
        }
    }
}
