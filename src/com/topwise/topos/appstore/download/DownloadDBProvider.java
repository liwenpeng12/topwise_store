package com.topwise.topos.appstore.download;

import android.content.ContentValues;
import android.database.Cursor;

import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.data.AppInfo;
import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.manager.AppManager;
import com.topwise.topos.appstore.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DownloadDBProvider {

    public HashMap<String, DownloadInfo> mDownloadingJobs;
    public HashMap<String, DownloadInfo> mCompletedJobs;
    public HashMap<String, DownloadInfo> mExceptionJobs;

    private DownloadManager mDM;

    public DownloadDBProvider(DownloadManager dm) {
        mDM = dm;
        mDownloadingJobs = new HashMap<>();
        mCompletedJobs = new HashMap<>();
        mExceptionJobs = new HashMap<>();
    }

    public void initJobs() {
        mDownloadingJobs.clear();
        mCompletedJobs.clear();
        mExceptionJobs.clear();
        DownloadDatabase database = new DownloadDatabase(AppStoreWrapperImpl.getInstance().getAppContext(), DownloadDatabase.Downloads.TABLE_NAME);
        Cursor c = database.query(null, null, null, null);
        if (c == null) {
            return;
        }
        int _idIndex = c.getColumnIndexOrThrow(DownloadDatabase.Downloads._ID);
        int uidIndex = c.getColumnIndexOrThrow(DownloadDatabase.Downloads.COLUMN_UID);
        int nameIndex = c.getColumnIndexOrThrow(DownloadDatabase.Downloads.COLUMN_NAME);
        int typeIndex = c.getColumnIndexOrThrow(DownloadDatabase.Downloads.COLUMN_TYPE);
        int downloadUrlIndex = c.getColumnIndexOrThrow(DownloadDatabase.Downloads.COLUMN_DOWNLOAD_URL);
        int filePathIndex = c.getColumnIndexOrThrow(DownloadDatabase.Downloads.COLUMN_FILE_PATH);
        int totalSizeIndex = c.getColumnIndexOrThrow(DownloadDatabase.Downloads.COLUMN_TOTAL_SIZE);
        int downloadSizeIndex = c.getColumnIndexOrThrow(DownloadDatabase.Downloads.COLUMN_DOWNLOAD_SIZE);
        int downloadStatusIndex = c.getColumnIndexOrThrow(DownloadDatabase.Downloads.COLUMN_DOWNLOAD_STATUS);
        try {
            while (c.moveToNext()) {
                String type = c.getString(typeIndex);
                String name = c.getString(nameIndex);
                String downloadUrl = c.getString(downloadUrlIndex);
                DownloadInfo info = new DownloadInfo(type, name, downloadUrl);
                info._id = c.getInt(_idIndex);
                info.uid = c.getString(uidIndex);
                info.destFilePath = c.getString(filePathIndex);
                info.downloadStatus = c.getInt(downloadStatusIndex);
                info.setTotalSize(c.getLong(totalSizeIndex), false);
                info.setDownloadedSize(c.getLong(downloadSizeIndex), false);
                if (info.totalSize <= 0
                        || info.downloadStatus == DownloadInfo.STATUS_NONE
                        || info.downloadStatus == DownloadInfo.STATUS_CANCEL
                        || info.downloadStatus == DownloadInfo.STATUS_ERROR_UNKNOWN
                        || info.downloadStatus == DownloadInfo.STATUS_ERROR_FILE_ERROR
                        || info.downloadStatus == DownloadInfo.STATUS_ERROR_HTTP_ERROR) {
                    deleteDownloadInfo(info.uid);
                    FileUtil.deleteFile(info.destFilePath);
                    FileUtil.deleteFile(info.downloadingTmpFilePath);
                    if (info.downloadStatus == DownloadInfo.STATUS_ERROR_UNKNOWN
                            || info.downloadStatus == DownloadInfo.STATUS_ERROR_FILE_ERROR
                            || info.downloadStatus == DownloadInfo.STATUS_ERROR_HTTP_ERROR) {
                        mExceptionJobs.put(info.uid, info);
                    }
                    continue;
                } else if (info.downloadStatus == DownloadInfo.STATUS_DOWNLOAD_SUCCESS) {
                    mCompletedJobs.put(info.uid, info);
                } else {
                    mDownloadingJobs.put(info.uid, info);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
            if (database != null) {
                database.close();
            }
        }
    }

    public void addToDownloadQueue(DownloadInfo info) {
        mExceptionJobs.remove(info.uid);
        mCompletedJobs.remove(info.uid);
        mDownloadingJobs.remove(info.uid);

        DownloadDatabase database = new DownloadDatabase(AppStoreWrapperImpl.getInstance().getAppContext(), DownloadDatabase.Downloads.TABLE_NAME);
        Cursor c = database.query(null, DownloadDatabase.Downloads.COLUMN_UID + "=\'" + info.uid + "\'", null, null);
        if (c != null && c.getCount() > 0) {
            database.delete(DownloadDatabase.Downloads.COLUMN_UID + "=\'" + info.uid + "\'", null);
        }
        ContentValues values = new ContentValues();
        values.put(DownloadDatabase.Downloads.COLUMN_UID, info.uid);
        values.put(DownloadDatabase.Downloads.COLUMN_NAME, info.name);
        values.put(DownloadDatabase.Downloads.COLUMN_FILE_PATH, info.destFilePath);
        values.put(DownloadDatabase.Downloads.COLUMN_DOWNLOAD_URL, info.url);
        values.put(DownloadDatabase.Downloads.COLUMN_DOWNLOAD_SIZE, info.currentDownloadSize);
        values.put(DownloadDatabase.Downloads.COLUMN_TOTAL_SIZE, info.totalSize);
        values.put(DownloadDatabase.Downloads.COLUMN_TYPE, info.type);
        values.put(DownloadDatabase.Downloads.COLUMN_DOWNLOAD_STATUS, info.downloadStatus);
        info._id = (int) database.insert(values);
        if (c != null) {
            c.close();
        }
        database.close();
        mDownloadingJobs.put(info.uid, info);
        mDM.notifyObservers(info);
    }

    public void downloadCompleted(DownloadInfo info) {
        if (info.downloadStatus == DownloadInfo.STATUS_DOWNLOAD_SUCCESS) {
            mExceptionJobs.remove(info.uid);
            mDownloadingJobs.remove(info.uid);
            mCompletedJobs.remove(info.uid);

            File file = new File(info.downloadingTmpFilePath);
            File newFile = new File(info.destFilePath);
            file.renameTo(newFile);

            ContentValues values = new ContentValues();
            values.put(DownloadDatabase.Downloads.COLUMN_DOWNLOAD_STATUS, info.downloadStatus);
            DownloadDatabase database = new DownloadDatabase(AppStoreWrapperImpl.getInstance().getAppContext(), DownloadDatabase.Downloads.TABLE_NAME);
            database.update(values, DownloadDatabase.Downloads.COLUMN_UID + "=\'" + info.uid + "\'", null);
            database.close();
            mCompletedJobs.put(info.uid, info);

            if (DownloadInfo.TYPE_APK.equals(info.type)) {
                String appId = info.uid.substring(0, info.uid.lastIndexOf("_"));
                AppInfo appInfo = DataPool.getInstance().getAppInfo(appId);
                AppManager.getInstance().endDownloadApp(appInfo, newFile);
            }
        } else if (info.downloadStatus == DownloadInfo.STATUS_CANCEL) {
            downloadCanceled(info);
        }
        mDM.notifyObservers(info);
    }

    public void downloadCanceled(DownloadInfo info) {
        if (info == null) {
            return;
        }
        mExceptionJobs.remove(info.uid);
        mDownloadingJobs.remove(info.uid);
        mCompletedJobs.remove(info.uid);

        FileUtil.deleteFile(info.downloadingTmpFilePath);

        DownloadDatabase database = new DownloadDatabase(AppStoreWrapperImpl.getInstance().getAppContext(), DownloadDatabase.Downloads.TABLE_NAME);
        database.delete(DownloadDatabase.Downloads.COLUMN_UID + "=\'" + info.uid + "\'", null);
        database.close();
        mDM.notifyObservers(info);
    }

    public void downloadException(DownloadInfo info) {
        mDownloadingJobs.remove(info.uid);
        mCompletedJobs.remove(info.uid);
        mExceptionJobs.remove(info.uid);

        ContentValues values = new ContentValues();
        values.put(DownloadDatabase.Downloads.COLUMN_DOWNLOAD_STATUS, info.downloadStatus);
        DownloadDatabase database = new DownloadDatabase(AppStoreWrapperImpl.getInstance().getAppContext(), DownloadDatabase.Downloads.TABLE_NAME);
        database.update(values, DownloadDatabase.Downloads.COLUMN_UID + "=\'" + info.uid + "\'", null);
        database.close();
        mExceptionJobs.put(info.uid, info);
        mDM.notifyObservers(info);
    }

    public void deleteDownloadInfo(String  downloadInfoId) {
        mExceptionJobs.remove(downloadInfoId);
        mDownloadingJobs.remove(downloadInfoId);
        mCompletedJobs.remove(downloadInfoId);

        DownloadDatabase database = new DownloadDatabase(AppStoreWrapperImpl.getInstance().getAppContext(), DownloadDatabase.Downloads.TABLE_NAME);
        database.delete(DownloadDatabase.Downloads.COLUMN_UID + "=\'" + downloadInfoId + "\'", null);
        database.close();
    }

    public void clearCompletedJobs() {
        mCompletedJobs.clear();
        DownloadDatabase database = new DownloadDatabase(AppStoreWrapperImpl.getInstance().getAppContext(), DownloadDatabase.Downloads.TABLE_NAME);
        database.delete(DownloadDatabase.Downloads.COLUMN_DOWNLOAD_STATUS + "=\'" + DownloadInfo.STATUS_DOWNLOAD_SUCCESS + "\'", null);
        database.close();
    }

    public int getDownloadInfoStatus(DownloadInfo info) {
        DownloadDatabase database = new DownloadDatabase(AppStoreWrapperImpl.getInstance().getAppContext(), DownloadDatabase.Downloads.TABLE_NAME);
        Cursor c = database.query(null, DownloadDatabase.Downloads.COLUMN_UID + "=\'" + info.uid + "\'", null, null);
        if (c == null || c.getCount() == 0) {
            info.downloadStatus = DownloadInfo.STATUS_NONE;
        } else {
            int downloadStatusIndex = c.getColumnIndexOrThrow(DownloadDatabase.Downloads.COLUMN_DOWNLOAD_STATUS);
            while (c.moveToNext()) {
                info.downloadStatus = c.getInt(downloadStatusIndex);
                break;
            }
        }
        if (c != null) {
            c.close();
        }
        database.close();
        return info.downloadStatus;
    }

    public ArrayList<DownloadInfo> getAllDownloads() {
        ArrayList<DownloadInfo> all = new ArrayList<>();
        Iterator it = mDownloadingJobs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            DownloadInfo info = (DownloadInfo) entry.getValue();
            all.add(info);
        }
        Iterator it1 = mCompletedJobs.entrySet().iterator();
        while (it1.hasNext()) {
            Map.Entry entry = (Map.Entry) it1.next();
            DownloadInfo info = (DownloadInfo) entry.getValue();
            all.add(info);
        }
        return all;
    }

    public boolean isDownloadInfoExist(DownloadInfo info) {
        return  getAllDownloads().contains(info);
    }

    public void updateTotalSizeDB(DownloadInfo info, long totalBytes) {
        ContentValues values = new ContentValues();
        values.put(DownloadDatabase.Downloads.COLUMN_TOTAL_SIZE, totalBytes);
        DownloadDatabase database = new DownloadDatabase(AppStoreWrapperImpl.getInstance().getAppContext(), DownloadDatabase.Downloads.TABLE_NAME);
        database.update(values, DownloadDatabase.Downloads.COLUMN_UID + "=\'" + info.uid + "\'", null);
        database.close();
    }

    public void updateCurrentDownloadSizeDB(DownloadInfo info, long currentBytes) {
        ContentValues values = new ContentValues();
        values.put(DownloadDatabase.Downloads.COLUMN_DOWNLOAD_SIZE, currentBytes);
        DownloadDatabase database = new DownloadDatabase(AppStoreWrapperImpl.getInstance().getAppContext(), DownloadDatabase.Downloads.TABLE_NAME);
        database.update(values, DownloadDatabase.Downloads.COLUMN_UID + "=\'" + info.uid + "\'", null);
        database.close();
    }

    public static ArrayList<DownloadInfo> jobsToList(HashMap<String, DownloadInfo> jobs) {
        ArrayList<DownloadInfo> list = new ArrayList<>();
        Iterator it = jobs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            DownloadInfo info = (DownloadInfo) entry.getValue();
            list.add(info);
        }
        return list;
    }

}
