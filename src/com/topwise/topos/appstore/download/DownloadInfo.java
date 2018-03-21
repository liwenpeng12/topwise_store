package com.topwise.topos.appstore.download;

import android.os.Parcel;
import android.os.Parcelable;

import com.topwise.topos.appstore.api.AppStoreApi;
import com.topwise.topos.appstore.utils.Properties;
import com.topwise.topos.appstore.utils.Utils;

public class DownloadInfo implements Parcelable {

    public static final int STATUS_NONE = 0;
    public static final int STATUS_DOWNLOADING = 1;
    public static final int STATUS_PAUSE = 2;
    public static final int STATUS_CANCEL = 3;
    public static final int STATUS_PENDING = 4;
    public static final int STATUS_DOWNLOAD_SUCCESS = 200;
    public static final int STATUS_ERROR_UNKNOWN = 301;
    public static final int STATUS_ERROR_FILE_ERROR = 302;
    public static final int STATUS_ERROR_HTTP_ERROR = 303;
    public static final int STATUS_ERROR_NETWORK_ERROR = 404;
    public static final int STATUS_ERROR_NETWORK_ERROR_2 = 403;

    private static final String DOWNLOAD_SUFFIX = ".tmp";

    public static final String TYPE_APK = "apk";
    public static final String EXT_APK = ".apk";

    public volatile int _id;
    public volatile String uid;
    public volatile String name;
    public volatile String type;
    public volatile String url;
    public volatile String destFilePath;
    public volatile String downloadingTmpFilePath;
    public volatile long totalSize;
    public volatile long currentDownloadSize;
    public volatile int progress;
    public volatile int downloadSpeed;
    public volatile int downloadStatus;

    public DownloadInfo(String type, String name, String url) {
        if (url == null) {
            return;
        }
        if (name == null || name.length() == 0) {
            name = Utils.getMd5(url);
        }
        this.type = type;
        this.name = name.replace(" ", "");
        this.url = url;
        destFilePath = getDownloadDir(type) + this.name + getDownloadFileExt(type);
        downloadingTmpFilePath = getDownloadDir(type) + this.name + getDownloadFileExt(type) + DOWNLOAD_SUFFIX;
    }

    protected DownloadInfo(Parcel in) {
        _id = in.readInt();
        uid = in.readString();
        name = in.readString();
        type = in.readString();
        url = in.readString();
        destFilePath = in.readString();
        downloadingTmpFilePath = in.readString();
        totalSize = in.readLong();
        currentDownloadSize = in.readLong();
        progress = in.readInt();
        downloadSpeed = in.readInt();
        downloadStatus = in.readInt();
    }

    public static final Creator<DownloadInfo> CREATOR = new Creator<DownloadInfo>() {
        @Override
        public DownloadInfo createFromParcel(Parcel in) {
            return new DownloadInfo(in);
        }

        @Override
        public DownloadInfo[] newArray(int size) {
            return new DownloadInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeString(uid);
        dest.writeString(name);
        dest.writeString(type);
        dest.writeString(url);
        dest.writeString(destFilePath);
        dest.writeString(downloadingTmpFilePath);
        dest.writeLong(totalSize);
        dest.writeLong(currentDownloadSize);
        dest.writeInt(progress);
        dest.writeInt(downloadSpeed);
        dest.writeInt(downloadStatus);
    }

    public void setTotalSize(long totalSize, boolean observer) {
        this.totalSize = totalSize;
        DownloadManager.getInstance().mDownloadDBProvider.updateTotalSizeDB(this, totalSize);
        if (observer) {
            DownloadManager.getInstance().notifyObservers(this);
        }
    }

    public void setDownloadedSize(long currentDownloadSize, boolean observer) {
        this.currentDownloadSize = currentDownloadSize;
        DownloadManager.getInstance().mDownloadDBProvider.updateCurrentDownloadSizeDB(this, currentDownloadSize);
        int oldProgress = this.progress;
        if (totalSize == 0) {
            progress = 0;
        } else {
            progress = (int) ((currentDownloadSize * 100) / totalSize);
            if (progress == 0) {
                progress = 1;
            }
        }
        if (progress != oldProgress && progress < 100 && observer) {
            DownloadManager.getInstance().notifyObservers(this);
            try {
                String appId = uid.substring(0, uid.lastIndexOf("_"));
                AppStoreApi.mDownloadStateListener.get(appId).onDownloadProgressChanged(appId, progress);
                AppStoreApi.mDownloadStateListener.get(appId).onDownloadSpeedChanged(appId, downloadSpeed);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String formatDownloadSpeed(int speedKB) {
        if (speedKB > 1000) {
            int mb = speedKB / 1000;
            int kb = speedKB % 1000;
            return mb + "." + (kb > 100 ? "" : "0") + kb / 10 + "MB/s";
        } else {
            return speedKB + "KB/s";
        }
    }

    public static boolean isErrorStatus(int status) {
        if (status == DownloadInfo.STATUS_ERROR_UNKNOWN
                || status == DownloadInfo.STATUS_ERROR_HTTP_ERROR
                || status == DownloadInfo.STATUS_ERROR_FILE_ERROR
                || status == DownloadInfo.STATUS_ERROR_NETWORK_ERROR
                || status == DownloadInfo.STATUS_ERROR_NETWORK_ERROR_2) {
            return true;
        }
        return false;
    }

    private String getDownloadDir(String type) {
        if (TYPE_APK.equals(type)) {
            return Properties.APP_PATH;
        }
        return Properties.APPSTORE_PATH;
    }

    private String getDownloadFileExt(String type) {
        if (TYPE_APK.equals(type)) {
            return EXT_APK;
        }
        return "";
    }
}
