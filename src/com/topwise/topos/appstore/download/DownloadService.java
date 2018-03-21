package com.topwise.topos.appstore.download;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class DownloadService extends Service {

    public static final int MSG_DOWNLOAD_START = 1;
    public static final int MSG_DOWNLOAD_PAUSE = 2;
    public static final int MSG_DOWNLOAD_RESUME = 3;
    public static final int MSG_DOWNLOAD_CANCEL = 4;
    public static final int MSG_DOWNLOAD_STATUS_CHANGED = 5;
    public static final int MSG_DOWNLOAD_PROGRESS_CHANGED = 6;

    public static final String ACTION_DOWNLOAD_START = "start_download";
    public static final String ACTION_DOWNLOAD_PAUSE = "pause_download";
    public static final String ACTION_DOWNLOAD_RESUME = "resume_download";
    public static final String ACTION_DOWNLOAD_CANCEL = "cancel_download";

    public static final int MAX_IN_DOWNLOADING = 3;

    private DownloadManager mDM;
    private DownloadInfo mDownloadInfo;
    private DownloadHandler mDownloadHandler;

    private HashMap<String, DownloadInfo> mDownloadPendingQueue = new HashMap<String, DownloadInfo>();
    private HashMap<String, DownloadInfo> mDownloadInProgress = new HashMap<String, DownloadInfo>();

    @Override
    public void onCreate() {
        super.onCreate();
        mDM = DownloadManager.getInstance();
        mDownloadHandler = new DownloadHandler(Looper.getMainLooper());
        initDownloadQueue();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        String action = intent.getAction();
        if (action.equals(ACTION_DOWNLOAD_START)) {
            addToDownloadQueue((DownloadInfo) intent.getParcelableExtra("downloadInfo"));
        } else if (action.equals(ACTION_DOWNLOAD_PAUSE)) {
            pauseDownload(intent.getStringExtra("downloadInfoId"));
        } else if (action.equals(ACTION_DOWNLOAD_RESUME)) {
            resumeDownload(intent.getStringExtra("downloadInfoId"));
        } else if (action.equals(ACTION_DOWNLOAD_CANCEL)) {
            cancelDownload(intent.getStringExtra("downloadInfoId"));
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DownloadNotification.getInstance().clearNotificationSenders();
        DownloadNotification.getInstance().destroy();
    }

    public class DownloadHandler extends Handler {
        public DownloadHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DOWNLOAD_START: {
                    mDownloadInfo = (DownloadInfo) msg.obj;
                    DownloadNotification.getInstance().startProgressNotification(mDownloadInfo);
                    break;
                }
                case MSG_DOWNLOAD_PAUSE: {
                    mDownloadInfo = (DownloadInfo) msg.obj;
                    break;
                }
                case MSG_DOWNLOAD_RESUME: {
                    mDownloadInfo = (DownloadInfo) msg.obj;
                    break;
                }
                case MSG_DOWNLOAD_CANCEL: {
                    mDownloadInfo = (DownloadInfo) msg.obj;
                    DownloadNotification.getInstance().displayNotification(mDownloadInfo);
                    break;
                }
                case MSG_DOWNLOAD_STATUS_CHANGED: {
                    mDownloadInfo = (DownloadInfo) msg.obj;
                    handleDownloadStatusChanged(mDownloadInfo);
                    break;
                }
                case MSG_DOWNLOAD_PROGRESS_CHANGED: {
                    mDownloadInfo = (DownloadInfo) msg.obj;
                    break;
                }
            }
            super.handleMessage(msg);
        }
    }

    private void initDownloadQueue() {
        mDM.mDownloadDBProvider.initJobs();
        synchronized (mDownloadPendingQueue) {
            mDownloadPendingQueue.putAll(mDM.mDownloadDBProvider.mDownloadingJobs);
        }
    }

    public void addToDownloadQueue(DownloadInfo info) {
        LogEx.w("addToDownloadQueue");
        if (mDM.mDownloadDBProvider.mCompletedJobs.containsKey(info.uid)) {
            return;
        }
        synchronized (mDownloadPendingQueue) {
            if (!mDownloadPendingQueue.containsKey(info.uid)) {
                info.downloadStatus = DownloadInfo.STATUS_PENDING;
                mDownloadPendingQueue.put(info.uid, info);
                mDM.mDownloadDBProvider.addToDownloadQueue(info);
            }
        }
        startDownloadThread();
    }

    public void pauseDownload(String uid) {
        DownloadInfo info = mDownloadInProgress.get(uid);
        if (info == null || info.downloadStatus == DownloadInfo.STATUS_PAUSE || info.downloadStatus == DownloadInfo.STATUS_DOWNLOAD_SUCCESS) {
            return;
        }
        info.downloadStatus = DownloadInfo.STATUS_PAUSE;
        DownloadNotification.getInstance().displayNotification(info);
    }

    public void resumeDownload(String uid) {
        if (!Utils.isNetworkConnected()) {
            Toast.makeText(this, "网络没有链接", Toast.LENGTH_SHORT).show();
            return;
        }
        DownloadInfo infoInPadding;
        DownloadInfo infoInProgress;
        synchronized (mDownloadPendingQueue) {
            infoInPadding = mDownloadPendingQueue.get(uid);
            infoInProgress = mDownloadInProgress.get(uid);
        }
        if (infoInPadding == null) {
            return;
        }
        if (infoInProgress != null && infoInProgress.uid == infoInPadding.uid) {
            return;
        }
        infoInPadding.downloadStatus = DownloadInfo.STATUS_PENDING;
        mDM.notifyObservers(infoInPadding);
        startDownloadThread();
    }

    public void cancelDownload(String uid) {
        synchronized (mDownloadPendingQueue) {
            DownloadInfo info = mDownloadInProgress.get(uid);
            if (info != null) {
                info.downloadStatus = DownloadInfo.STATUS_CANCEL;
                mDownloadInProgress.remove(uid);
                mDM.notifyObservers(info);
                return;
            }
            info = mDownloadPendingQueue.get(uid);
            if (info != null) {
                info.downloadStatus = DownloadInfo.STATUS_CANCEL;
                mDownloadPendingQueue.remove(uid);
                mDM.notifyObservers(info);
            }
            if (info != null) {
                Message msg = Message.obtain();
                msg.obj = info;
                msg.what = MSG_DOWNLOAD_CANCEL;
                mDownloadHandler.sendMessage(msg);
                mDM.mDownloadDBProvider.downloadCanceled(info);
            }
        }
    }

    private void startDownloadThread() {
        synchronized (mDownloadPendingQueue) {
            LogEx.w("startDownloadThread");
            Iterator<String> it = mDownloadPendingQueue.keySet().iterator();
            ArrayList<String> needToRemoveUids = new ArrayList<>();
            while (mDownloadInProgress.size() < MAX_IN_DOWNLOADING && it.hasNext()) {
                String uid = it.next();
                DownloadInfo info = mDownloadPendingQueue.get(uid);
                if (info.downloadStatus == DownloadInfo.STATUS_PENDING) {
                    info.downloadStatus = DownloadInfo.STATUS_DOWNLOADING;
                    mDM.notifyObservers(info);
                    new DownloadThread(info, mDownloadHandler).start();
                    mDownloadInProgress.put(uid, mDownloadPendingQueue.get(uid));
                    needToRemoveUids.add(uid);
                    DownloadNotification.getInstance().displayNotification(info);
                }
            }
            for (String uid : needToRemoveUids) {
                mDownloadPendingQueue.remove(uid);
            }
        }
    }

    synchronized private void handleDownloadStatusChanged(DownloadInfo info) {
        synchronized (mDownloadPendingQueue) {
            mDownloadInProgress.remove(info.uid);
            if (info.downloadStatus != DownloadInfo.STATUS_DOWNLOAD_SUCCESS && info.downloadStatus != DownloadInfo.STATUS_CANCEL) {
                mDownloadPendingQueue.put(info.uid, info);
            }
            mDM.mDownloadDBProvider.downloadCompleted(info);
            DownloadNotification.getInstance().displayNotification(info);
            startDownloadThread();
            if (mDownloadInProgress.size() == 0 && mDownloadPendingQueue.size() == 0) {
                notifyAll();
            }
        }
    }
}
