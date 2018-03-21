package com.topwise.topos.appstore.data;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.topwise.topos.appstore.AppStoreWrapper.AppUpgradeCountListener;
import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.database.DatabaseCenter;
import com.topwise.topos.appstore.utils.LogEx;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;

public class DataPool implements Serializable {

    private static DataPool mThis = null;

    public static final int TYPE_PAGE_RECOMMAND = 1; // 推荐，等于page id
    public static final int TYPE_PAGE_GAME = 2; // 游戏，等于page id
    public static final int TYPE_PAGE_APP = 3; // 应用，等于page id
    public static final int TYPE_PAGE_MUST_HAVE_GAME = 4; // 必备游戏，等于page id
    public static final int TYPE_PAGE_MUST_HAVE_APP = 5; // 必备应用，等于page id
    public static final int TYPE_PAGE_ONE_KEY = 6; // 一键装机，等于page id
    public static final int TYPE_PAGE_RANK_GAME = 7; // 游戏榜单，等于page id
    public static final int TYPE_PAGE_RANK_APP = 8; // 应用绑定，等于page id
    
    public static final int TYPE_WELCOME = 50;
    public static final int TYPE_NOTIFICATION = 51;
    public static final int TYPE_SEARCH_HOTWORD = 52;
    public static final int TYPE_NEWS_ICON = 53;
    
    public static final int TYPE_APP_LIST = 100;
    public static final int TYPE_APP_LABEL = 200;
    public static final int TYPE_APP_RANK = 300;
    public static final int TYPE_APP_BANNER = 400;
    public static final int TYPE_APP_SEARCH_RESULT = 500;
    public static final int TYPE_APP_CATEGORY = 600;
    public static final int TYPE_APP_RELATED_SIMILAR = 700; // 类似应用
    public static final int TYPE_APP_RELATED_PEOPLE_LIKE = 701; // 大家喜欢
    public static final int TYPE_APP_RELATED_PEOPLE_DOWNLOADING = 702; // 同时下载
    public static final int TYPE_APP_RELATED_HOT = 703; // 热门应用
    public static final int TYPE_APP_RANDOM_APPS = 800;
    public static final int TYPE_APP_NEED_UPGRADE = 900;
    public static final int TYPE_APP_OTHER = 9997;
    public static final int TYPE_APP_DOWNLOADED = 9998;
    public static final int TYPE_APP_INSTALLED = 9999;
    
    public static final int TYPE_BANNER_LARGE = 10000;
    public static final int TYPE_BANNER_SMALL = 10100;
    public static final int TYPE_BANNER_RANDOM = 10200;
    
    public static final int TYPE_ADICON = 20000;
    
    public static final int TYPE_LABEL = 30000;
    
    public static final int TYPE_RANK = 40000;
    
    public static final int TYPE_TYPE = 50000;

    private SparseArray<ArrayList<AppInfo>> mAppInfos = null;
    private SparseArray<ArrayList<Banner>> mBanners = null;
    private SparseArray<ArrayList<AdIcon>> mAdIcons = null;
    private SparseArray<ArrayList<Label>> mLabels = null;
    private SparseArray<ArrayList<Rank>> mRanks = null;
    
    private SparseArray<ArrayList<Module>> mModules = null;
    
    private ArrayList<Welcome> mWelcomes = null;
    private ArrayList<NotificationInfo> mNotifications = null;
    private ArrayList<Hotword> mSearchHotwords = null;
    
    private HashMap<String, NotificationBitmap> mNotificationBitmaps = null; 
    
    private ArrayList<DataObserver> mObservers = null;
    private HashMap<Integer, Integer> mTypePageMap = null;

    private Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
    
    @SuppressLint("UseSparseArrays")
    private DataPool() {
        mAppInfos = new SparseArray<ArrayList<AppInfo>>();
        mBanners = new SparseArray<ArrayList<Banner>>();
        mAdIcons = new SparseArray<ArrayList<AdIcon>>();
        mLabels = new SparseArray<ArrayList<Label>>();
        mRanks = new SparseArray<ArrayList<Rank>>();
        
        mModules = new SparseArray<ArrayList<Module>>();
        
        mWelcomes = new ArrayList<Welcome>();
        mNotifications = new ArrayList<NotificationInfo>();
        mSearchHotwords = new ArrayList<Hotword>();
        
        mNotificationBitmaps = new HashMap<String, NotificationBitmap>();
        
        mObservers = new ArrayList<DataObserver>();
        mTypePageMap = new HashMap<Integer, Integer>();
    }

    public static DataPool getInstance() {
        if (mThis == null) {
            synchronized (DataPool.class) {
                if (mThis == null) {
                    mThis = new DataPool();
                }
            }
        }
        return mThis;
    }
    
    public void sendObserver(final int type) {
            for (int i = 0; i < mObservers.size(); i++) {
                final int index = i;
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(index < mObservers.size())
                         mObservers.get(index).onChanged(type);
                    }
                });
            }
    }
    
    /**
     * 闪屏广告
     */
    public void addWelcome(Welcome info, boolean isSendObserver) {
        synchronized (DataPool.class) {
            if (info == null) {
                return;
            }
            mWelcomes.add(info);
            if (isSendObserver) {
                sendObserver(TYPE_WELCOME);
            }
        }
    }
    public ArrayList<Welcome> getWelcomes() {
        return mWelcomes;
    }
    public void clearWelcomes() {
        synchronized (DataPool.class) {
            mWelcomes.clear();
            sendObserver(TYPE_WELCOME);
        }
    }
    
    /**
     * 通知
     */
    public void addNotification(NotificationInfo info, boolean isSendObserver) {
        synchronized (DataPool.class) {
            if (info == null) {
                return;
            }
            boolean isExist = false;
            for (int i = 0; i < mNotifications.size(); i++) {
                if (mNotifications.get(i).title.equals(info.title)) {
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                mNotifications.add(info);
            }
            if (isSendObserver) {
                sendObserver(TYPE_NOTIFICATION);
            }
        }
    }
    public void setNotificationBitmap(NotificationInfo info, Bitmap icon, Bitmap img) {
        synchronized (DataPool.class) {
            if (info == null) {
                return;
            }
            NotificationBitmap nb = mNotificationBitmaps.get(info.title);
            if (nb == null) {
                nb = new NotificationBitmap();
            }
            if (icon != null) {
                nb.icon = icon;
            }
            if (img != null) {
                nb.img = img;
            }
            mNotificationBitmaps.put(info.title, nb);
        }
    }
    public ArrayList<NotificationInfo> getNotifications() {
        synchronized (DataPool.class) {
            return mNotifications;
        }
    }
    public NotificationBitmap getNotificationBitmap(NotificationInfo info) {
        return mNotificationBitmaps.get(info.title);
    }
    public void removeNotification(NotificationInfo info) {
        synchronized (DataPool.class) {
            if (info == null) {
                return;
            }
            if (mNotifications.size() == 0) {
                return;
            }
            int index = 0;
            for (int i = 0; i < mNotifications.size(); i++) {
                if (mNotifications.get(i).title.equals(info.title)) {
                    index = i;
                    break;
                }
            }
            mNotifications.remove(index);
        }
    }
    public void clearNotifications() {
        synchronized (DataPool.class) {
            mNotifications.clear();
            sendObserver(TYPE_NOTIFICATION);
        }
    }
    public class NotificationBitmap {
        public Bitmap icon;
        public Bitmap img;
    }
    
    /**
     * 搜索热词
     */
    public void addSearchHotword(Hotword word, boolean isSendObserver) {
        synchronized (DataPool.class) {
            if (word == null) {
                return;
            }
            mSearchHotwords.add(word);
            if (isSendObserver) {
                sendObserver(TYPE_SEARCH_HOTWORD);
            }
        }
    }
    public ArrayList<Hotword> getSearchHotwords() {
        return mSearchHotwords;
    }
    public void clearSearchHotwords() {
        synchronized (DataPool.class) {
            mSearchHotwords.clear();
            sendObserver(TYPE_SEARCH_HOTWORD);
        }
    }
    
    /**
     * 设置所有类型数据的flag
     */
    public void setDownloadFlag(AppInfo info) {
        for (int i = 0; i < mAppInfos.size(); i++) {
            int type = mAppInfos.keyAt(i);
            ArrayList<AppInfo> array = mAppInfos.get(type);
            for (int j = 0; j < array.size(); j++) {
                if (array.get(j).id.equals(info.id)) {
                    if (array.get(j).flag != AppInfo.FLAG_INSTALLING && array.get(j).flag != AppInfo.FLAG_INSTALLED) {
                        array.get(j).flag = AppInfo.FLAG_DOWNLOADED;
                    }
                    if (info.file != null) {
                        array.get(j).file = info.file;
                    }
                }
            }
            sendObserver(type);
        }
    }
    public void setDownloadFlagForce(AppInfo info) {
        for (int i = 0; i < mAppInfos.size(); i++) {
            int type = mAppInfos.keyAt(i);
            ArrayList<AppInfo> array = mAppInfos.get(type);
            for (int j = 0; j < array.size(); j++) {
                if (array.get(j).id.equals(info.id)) {
                    array.get(j).flag = AppInfo.FLAG_DOWNLOADED;
                    if (info.file != null) {
                        array.get(j).file = info.file;
                    }
                }
            }
            sendObserver(type);
        }
    }
    public void setInstallFlag(AppInfo info) {
        for (int i = 0; i < mAppInfos.size(); i++) {
            int type = mAppInfos.keyAt(i);
            ArrayList<AppInfo> array = mAppInfos.get(type);

            for (int j = 0; j < array.size(); j++) {
                if (array.get(j).id.equals(info.id)) {
                    array.get(j).flag = AppInfo.FLAG_INSTALLED;
                }
            }
            sendObserver(type);
        }
    }
    public void setNeedUpgradeFlag(AppInfo info) {
        for (int i = 0; i < mAppInfos.size(); i++) {
            int type = mAppInfos.keyAt(i);
            ArrayList<AppInfo> array = mAppInfos.get(type);
            for (int j = 0; j < array.size(); j++) {
                if (array.get(j).id.equals(info.id)) {
                    array.get(j).flag = AppInfo.FLAG_NEED_UPGRADE;
                }
            }
            sendObserver(type);
        }
    }
    public void restoreToOnlineFlag(AppInfo info) {
        for (int i = 0; i < mAppInfos.size(); i++) {
            int type = mAppInfos.keyAt(i);
            ArrayList<AppInfo> array = mAppInfos.get(type);
            for (int j = 0; j < array.size(); j++) {
                if (array.get(j).id.equals(info.id)) {
                    array.get(j).flag = AppInfo.FLAG_ONLINE;
                    DatabaseCenter dbc = new DatabaseCenter(AppStoreWrapperImpl.getInstance().getAppContext(), DatabaseCenter.IsNewTable.TABLE_NAME);
                    Cursor c = dbc.query(null, DatabaseCenter.IsNewTable.COLUMN_UID + "=\'" + info.id + "\'", null, null);
                    ContentValues values = new ContentValues();
                    values.put(DatabaseCenter.IsNewTable.COLUMN_UID, info.id);
                    values.put(DatabaseCenter.IsNewTable.COLUMN_ISNEW, "0");
                    if (c.getCount() == 0) {
                        dbc.insert(values);
                    } else {
                        dbc.update(values, DatabaseCenter.IsNewTable.COLUMN_UID + "=\'" + info.id + "\'", null);
                    }
                    c.close();
                    dbc.close();
                }
            }
            sendObserver(type);
        }
    }
    
    /**
     * app & game
     */
    public String addAppInfoEx(int type, AppInfo info, boolean isSendObserver) {
        synchronized (DataPool.class) {
            if (info == null) {
                return null;
            }
            if (info.id == null) {
                info.id = info.pkg;
            }
            if (info.id == null) {
                return null;
            }
            ArrayList<AppInfo> array = mAppInfos.get(type);
            if (array == null) {
                array = new ArrayList<AppInfo>();
                mAppInfos.put(type, array);
            }
            if(!isExistInArray(info,array)){
                array.add(info);
                if (isSendObserver) {
                    sendObserver(type);
                }
            }
            return info.id;
        }
    }
    private boolean isExistInArray(AppInfo info ,ArrayList<AppInfo> array){
        boolean isExist = false;
        for(int i = 0;i < array.size();i++) {
            if(info.id.equals(array.get(i).id)){
                isExist = true;
                break;
            }
        }
        return isExist;
    }
    public void addAppInfo(final int type, final AppInfo info) {
        addAppInfoEx(type, info, true);
    }
    public void updateAppInfo(final AppInfo info) {
        synchronized (DataPool.class) {
            if (info == null) {
                return;
            }
            if (info.id == null) {
                info.id = info.pkg;
            }
            if (info.id == null) {
                return;
            }
            for (int i = TYPE_APP_LIST; i < TYPE_APP_INSTALLED; i++) {
                ArrayList<AppInfo> array = mAppInfos.get(i);
                if (array != null && array.size() != 0) {
                    for(int j = 0; j < array.size(); j++) {
                        if(info.id.equals(array.get(j).id)){
                            array.remove(j);
                            array.add(info);
                        }
                    }
                }
            }
            addAppInfo(DataPool.TYPE_APP_OTHER, info);
        }
    }
    public AppInfo getAppInfo(String uid) {
        synchronized (DataPool.class) {
            if (uid == null || "".equals(uid)) {
                return null;
            }
            
            for (int i = 0; i < mAppInfos.size(); i++) {
                int key = mAppInfos.keyAt(i);
                ArrayList<AppInfo> array = mAppInfos.get(key);
                for (AppInfo info : array) {
                    if (info.id.equals(uid)) {
                        return info;
                    }
                }
            }
            return null;
        }
    }
    public AppInfo getAppInfoInType(int type, String uid) {
        synchronized (DataPool.class) {
            if (uid == null || "".equals(uid)) {
                return null;
            }
            ArrayList<AppInfo> array = mAppInfos.get(type);
            if (array == null) {
                return null;
            }
            for (AppInfo info : array) {
                if (info.id.equals(uid)) {
                    return info;
                }
            }
            return null;
        }
    }
    public void removeAppInfo(int type, AppInfo info) {
        synchronized (DataPool.class) {
            ArrayList<AppInfo> array = mAppInfos.get(type);
            if (array == null) {
                return;
            }
            for (AppInfo element : array) {
                if (element.id.equals(info.id)) {
                    array.remove(element);
                    sendObserver(type);
                    break;
                }
            }
            
            ArrayList<AppUpgradeCountListener> listeners = AppStoreWrapperImpl.getInstance().mAppUpgradeCountListeners;
            if (type == DataPool.TYPE_APP_NEED_UPGRADE && listeners != null) {
                for (int i = 0; i < listeners.size(); i++) {
                    LogEx.d("need upgrade app count:" + array.size());
                    listeners.get(i).appUpgradeCount(array.size());
                }
            }
        }
    }
    public ArrayList<AppInfo> getAppInfos(int type) {
        synchronized (DataPool.class) {
            return mAppInfos.get(type);
        }
    }
    public void clearAppInfos(int type) {
        synchronized (DataPool.class) {
            ArrayList<AppInfo> array = mAppInfos.get(type);
            if (array != null) {
                array.clear();
            }
            mAppInfos.remove(type);
            sendObserver(type);
        }
    }
    public void clearAllAppInfos() {
        synchronized (DataPool.class) {
            for (int i = 0; i < mAppInfos.size(); i++) {
                int type = mAppInfos.keyAt(i);
                ArrayList<AppInfo> array = mAppInfos.get(type);
                if (array != null) {
                    array.clear();
                    array = null;
                }
                mAppInfos.remove(type);
                sendObserver(type);
            }
            mAppInfos.clear();
        }
    }
    public void putFileToAppInfo(AppInfo info, File file) {
        info.file = file;
        for (int i = 0; i < mAppInfos.size(); i++) {
            int type = mAppInfos.keyAt(i);
            ArrayList<AppInfo> array = mAppInfos.get(type);
            for (int j = 0; j < array.size(); j++) {
                if (array.get(j).id.equals(info.id)) {
                    array.get(j).file = file;
                }
            }
            sendObserver(type);
        }
    }
    
    /**
     * banner
     */
    private String addBannerEx(int type, Banner info, boolean isSendObserver) {
        synchronized (DataPool.class) {
            if (info == null) {
                return null;
            }
            if (info.id == null) {
                info.id = info.target_url;
            }
            ArrayList<Banner> array = mBanners.get(type);
            if (array == null) {
                array = new ArrayList<Banner>();
                mBanners.put(type, array);
            }
            array.add(info);
            if (isSendObserver) {
                sendObserver(type);
            }
            return info.id;
        }
    }
    public void addBanner(int type, Banner info) {
        addBannerEx(type, info, true);
    }
    public Banner getBanner(int type, String id) {
        synchronized (DataPool.class) {
            if (id == null || "".equals(id)) {
                return null;
            }
            ArrayList<Banner> array = mBanners.get(type);
            if (array == null) {
                return null;
            }
            for (Banner info : array) {
                if (info.id.equals(id)) {
                    return info;
                }
            }
            return null;
        }
    }
    public void removeBanner(int type, Banner info) {
        synchronized (DataPool.class) {
            ArrayList<Banner> array = mBanners.get(type);
            array.remove(info);
            sendObserver(type);
        }
    }
    public void addBanners(int type, ArrayList<Banner> infos) {
        for (Banner info : infos) {
            addBannerEx(type, info, false);
        }
        sendObserver(type);
    }
    public synchronized ArrayList<Banner> getBanners(int type) {
        synchronized (DataPool.class) {
            return mBanners.get(type);
        }
    }
    public void clearBanners(int type) {
        synchronized (DataPool.class) {
            ArrayList<Banner> array = mBanners.get(type);
            if (array != null) {
                array.clear();
                sendObserver(type);
            }
        }
    }
    public void clearAllBanners() {
        synchronized (DataPool.class) {
            for (int i = 0; i < mBanners.size(); i++) {
                int type = mBanners.keyAt(i);
                ArrayList<Banner> array = mBanners.get(type);
                if (array != null) {
                    array.clear();
                    array = null;
                }
                mBanners.remove(type);
                sendObserver(type);
            }
            mBanners.clear();
        }
    }
    
    /**
     * adicon
     */
    private String addAdIconEx(int type, AdIcon info, boolean isSendObserver) {
        synchronized (DataPool.class) {
            if (info == null) {
                return null;
            }
            if (info.id == null) {
                info.id = info.target_url;
            }
            ArrayList<AdIcon> array = mAdIcons.get(type);
            if (array == null) {
                array = new ArrayList<AdIcon>();
                mAdIcons.put(type, array);
            }
            array.add(info);
            if (isSendObserver) {
                sendObserver(type);
            }
            return info.id;
        }
    }
    public void addAdIcon(int type, AdIcon info) {
        addAdIconEx(type, info, true);
    }
    public AdIcon getAdIcon(int type, String id) {
        synchronized (DataPool.class) {
            if (id == null || "".equals(id)) {
                return null;
            }
            ArrayList<AdIcon> array = mAdIcons.get(type);
            if (array == null) {
                return null;
            }
            for (AdIcon info : array) {
                if (info.id.equals(id)) {
                    return info;
                }
            }
            return null;
        }
    }
    public void removeAdIcon(int type, AdIcon info) {
        synchronized (DataPool.class) {
            ArrayList<AdIcon> array = mAdIcons.get(type);
            array.remove(info);
            sendObserver(type);
        }
    }
    public void addAdIcons(int type, ArrayList<AdIcon> infos) {
        for (AdIcon info : infos) {
            addAdIconEx(type, info, false);
        }
        sendObserver(type);
    }
    public synchronized ArrayList<AdIcon> getAdIcons(int type) {
        synchronized (DataPool.class) {
            return mAdIcons.get(type);
        }
    }
    public void clearAdIcons(int type) {
        synchronized (DataPool.class) {
            ArrayList<AdIcon> array = mAdIcons.get(type);
            if (array != null) {
                array.clear();
                sendObserver(type);
            }
        }
    }
    public void clearAllAdIcons() {
        synchronized (DataPool.class) {
            for (int i = 0; i < mAdIcons.size(); i++) {
                int type = mAdIcons.keyAt(i);
                ArrayList<AdIcon> array = mAdIcons.get(type);
                if (array != null) {
                    array.clear();
                    array = null;
                }
                mAdIcons.remove(type);
                sendObserver(type);
            }
            mAdIcons.clear();
        }
    }
    
    /**
     * label
     */
    private String addLabelEx(int type, Label info, boolean isSendObserver) {
        synchronized (DataPool.class) {
            if (info == null) {
                return null;
            }
            if (info.id == null) {
                info.id = info.img_url;
            }
            ArrayList<Label> array = mLabels.get(type);
            if (array == null) {
                array = new ArrayList<Label>();
                mLabels.put(type, array);
            }
            array.add(info);
            if (isSendObserver) {
                sendObserver(type);
            }
            return info.id;
        }
    }
    public void addLabel(int type, Label info) {
        addLabelEx(type, info, true);
    }
    public Label getLabel(int type, String id) {
        synchronized (DataPool.class) {
            if (id == null || "".equals(id)) {
                return null;
            }
            ArrayList<Label> array = mLabels.get(type);
            if (array == null) {
                return null;
            }
            for (Label info : array) {
                if (info.id.equals(id)) {
                    return info;
                }
            }
            return null;
        }
    }
    public void removeLabel(int type, Label info) {
        synchronized (DataPool.class) {
            ArrayList<Label> array = mLabels.get(type);
            array.remove(info);
            sendObserver(type);
        }
    }
    public void addLabels(int type, ArrayList<Label> infos) {
        for (Label info : infos) {
            addLabelEx(type, info, false);
        }
        sendObserver(type);
    }
    public synchronized ArrayList<Label> getLabels(int type) {
        synchronized (DataPool.class) {
            return mLabels.get(type);
        }
    }
    public void clearLabels(int type) {
        synchronized (DataPool.class) {
            ArrayList<Label> array = mLabels.get(type);
            if (array != null) {
                array.clear();
                sendObserver(type);
            }
        }
    }
    public void clearAllLabels() {
        synchronized (DataPool.class) {
            for (int i = 0; i < mLabels.size(); i++) {
                int type = mLabels.keyAt(i);
                ArrayList<Label> array = mLabels.get(type);
                if (array != null) {
                    array.clear();
                    array = null;
                }
                mLabels.remove(type);
                sendObserver(type);
            }
            mLabels.clear();
        }
    }
    
    /**
     * rank
     */
    private String addRankEx(int type, Rank info, boolean isSendObserver) {
        synchronized (DataPool.class) {
            if (info == null) {
                return null;
            }
            if (info.id == null) {
                info.id = info.img_url;
            }
            ArrayList<Rank> array = mRanks.get(type);
            if (array == null) {
                array = new ArrayList<Rank>();
                mRanks.put(type, array);
            }
            array.add(info);
            if (isSendObserver) {
                sendObserver(type);
            }
            return info.id;
        }
    }
    public void addRank(int type, Rank info) {
        addRankEx(type, info, true);
    }
    public Rank getRank(int type, String id) {
        synchronized (DataPool.class) {
            if (id == null || "".equals(id)) {
                return null;
            }
            ArrayList<Rank> array = mRanks.get(type);
            if (array == null) {
                return null;
            }
            for (Rank info : array) {
                if (info.id.equals(id)) {
                    return info;
                }
            }
            return null;
        }
    }
    public void removeRank(int type, Rank info) {
        synchronized (DataPool.class) {
            ArrayList<Rank> array = mRanks.get(type);
            array.remove(info);
            sendObserver(type);
        }
    }
    public void addRanks(int type, ArrayList<Rank> infos) {
        for (Rank info : infos) {
            addRankEx(type, info, false);
        }
        sendObserver(type);
    }
    public synchronized ArrayList<Rank> getRanks(int type) {
        synchronized (DataPool.class) {
            return mRanks.get(type);
        }
    }
    public void clearRanks(int type) {
        synchronized (DataPool.class) {
            ArrayList<Rank> array = mRanks.get(type);
            if (array != null) {
                array.clear();
                sendObserver(type);
            }
        }
    }
    public void clearAllRanks() {
        synchronized (DataPool.class) {
            for (int i = 0; i < mRanks.size(); i++) {
                int type = mRanks.keyAt(i);
                ArrayList<Rank> array = mRanks.get(type);
                if (array != null) {
                    array.clear();
                    array = null;
                }
                mRanks.remove(type);
                sendObserver(type);
            }
            mRanks.clear();
        }
    }
    
    /**
     * module
     */
    private String addModuleEx(int type, Module info, boolean isSendObserver) {
        synchronized (DataPool.class) {
            if (info == null) {
                return null;
            }
            if (info.id == null) {
                info.id = "" + info.module_weight;
            }
            ArrayList<Module> array = mModules.get(type);
            if (array == null) {
                array = new ArrayList<Module>();
                mModules.put(type, array);
            }
            array.add(info);
            if (isSendObserver) {
                sendObserver(type);
            }
            return info.id;
        }
    }
    public void addModule(int type, Module info) {
        addModuleEx(type, info, true);
    }
    public Module getModule(int type, String id) {
        synchronized (DataPool.class) {
            if (id == null || "".equals(id)) {
                return null;
            }
            ArrayList<Module> array = mModules.get(type);
            if (array == null) {
                return null;
            }
            for (Module info : array) {
                if (info.id.equals(id)) {
                    return info;
                }
            }
            return null;
        }
    }
    public void removeModule(int type, Module info) {
        synchronized (DataPool.class) {
            ArrayList<Module> array = mModules.get(type);
            array.remove(info);
            sendObserver(type);
        }
    }
    public void addModules(int type, ArrayList<Module> infos) {
        for (Module info : infos) {
            addModuleEx(type, info, false);
        }
        sendObserver(type);
    }
    public synchronized ArrayList<Module> getModules(int type) {
        synchronized (DataPool.class) {
            return mModules.get(type);
        }
    }
    public void clearModules(int type) {
        synchronized (DataPool.class) {
            ArrayList<Module> array = mModules.get(type);
            if (array != null) {
                array.clear();
                sendObserver(type);
            }
        }
    }
    public void clearAllModules() {
        synchronized (DataPool.class) {
            for (int i = 0; i < mModules.size(); i++) {
                int type = mModules.keyAt(i);
                ArrayList<Module> array = mModules.get(type);
                if (array != null) {
                    array.clear();
                    array = null;
                }
                mModules.remove(type);
                sendObserver(type);
            }
            mModules.clear();
        }
    }
    
    /**
     * 数据监听器
     */
    public void registerDataObserver(DataObserver observer) {
        mObservers.add(observer);
    }
    public void unregisterDataObserver(DataObserver observer) {
        mObservers.remove(observer);
    }
    public interface DataObserver {
        void onChanged(int type);
    }
    
    /**
     * type和page配对
     */
    public void addTypePage(int type, int page) {
        mTypePageMap.put(type, page);
    }
    public int getTypePage(int type) {
        if (mTypePageMap.get(type) == null) {
            mTypePageMap.put(type, 0);
        }
        return mTypePageMap.get(type);
    }
    public void clearTypePageMap() {
        mTypePageMap.clear();
    }

}
