package com.topwise.topos.appstore.view;

import java.io.File;
import java.util.ArrayList;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.data.AppInfo;
import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.download.DownloadInfo;
import com.topwise.topos.appstore.download.DownloadManager;
import com.topwise.topos.appstore.manager.AppManager;
import com.topwise.topos.appstore.manager.AppManager.InstallStatusListener;
import com.topwise.topos.appstore.manager.AppManager.UninstallStatusListener;
import com.topwise.topos.appstore.utils.BitmapUtil;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.utils.Utils;
import com.topwise.topos.appstore.utils.AsynTaskManager.ImageLoadCallBack;
import com.topwise.topos.appstore.view.ListMainItemView.AppItemClickListener;
import com.topwise.topos.appstore.view.activity.AppDetailActivity;

import org.greenrobot.eventbus.EventBus;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class PopularItemView extends LinearLayout implements OnClickListener,DownloadManager.DownloadObserver,InstallStatusListener,UninstallStatusListener{
    private static final String TAG = "ListPopularItemView";
    protected ImageView mAppIcon;
    protected TextView mAppName;
    protected TextView mAppSize;
    protected ProgressButton mDownLoadBtn;
    protected Context mContext;
    protected AppInfo mAppInfo;
    protected Resources mResources;
    protected Handler mHandler;
    protected boolean mIsAttachToWindow = false;
    protected String mFrom;
    
    protected AppItemClickListener mAppItemClickListener;
    protected ImageLoadCallBack mImageLoadCallBack;
    
    public PopularItemView(Context context) {
        this(context,null);
    }

    public PopularItemView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PopularItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mResources = mContext.getResources();
        mHandler = new Handler();
        
        setOnClickListener(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }
    private void init(){
        mAppIcon = (ImageView) this.findViewById(R.id.zkas_popular_item_icon);
        mAppName = (TextView) this.findViewById(R.id.zkas_popular_item_name);
        mAppSize = (TextView) this.findViewById(R.id.zkas_popular_item_size);
        mDownLoadBtn = (ProgressButton) this.findViewById(R.id.zkas_popular_progress_btn);
        setProgressButtonOnClickListener(this);
    }
    
    public void setAppInfo(AppInfo appInfo){
        mAppInfo = appInfo;
        mImageLoadCallBack = new ImageLoadCallBack() {
            
            @Override
            public void onImageLoadSuccess(String imageUrl, Bitmap bitmap) {
                if (mAppInfo == null || mAppInfo.icon_url == null || imageUrl == null) {
                    return;
                }
                
                if(imageUrl.equals(mAppInfo.icon_url)){
                    setAppIcon(bitmap);
                }
            }
            
            @Override
            public void onImageLoadFailed(String imageUrl, String reason) {
                LogEx.d(TAG, imageUrl+":"+reason);
            }
            
            @Override
            public boolean isNeedToDecode(String imageUrl) {
                if(!mIsAttachToWindow){
                    return false;
                }
                
                if (mAppInfo == null || mAppInfo.icon_url == null || !mAppInfo.icon_url.equals(imageUrl)) {
                    return false;
                }
                
                return true;
            }
            
            @Override
            public String getCaller() {
                String caller = this.hashCode()+"";
                return caller;
            }
        };
        updateView();
    }
    
    private void updateView(){
        if(!mIsAttachToWindow){
            return;
        }
        //更新应用名称
        setAppName(mAppInfo.name);
        //更新应用大小
        setAppSize(ListMainItemView.transApkSize(mAppInfo.size, false));
        //更新icon
        updateAppIcon();
        initProgressButton();
    }
    
    private void updateAppIcon(){
      //更新ICON
        Bitmap iconBitmap = null;
        if(mAppInfo.icon_url == null || mAppInfo.icon_url.equals("")){
            if(mAppInfo.app_icon != null){
                iconBitmap = Utils.drawableToBitmap(mAppInfo.app_icon);
            }
        } else {
            iconBitmap = BitmapUtil.getInstance().getBitmapAsync(mAppInfo.icon_url, mImageLoadCallBack);
        }
        
        if(iconBitmap != null){
            setAppIcon(iconBitmap); 
        }
    }
    
    public AppInfo getAppInfo(){
        return mAppInfo;
    }
    
    public void setAppIcon(Bitmap bitmap) {
        mAppIcon.setBackground(new BitmapDrawable(mResources, bitmap));
    }
    
    private void setAppName(String appName){
        mAppName.setText(appName);
    }
    
    private void setAppName(int appNameId){
        mAppName.setText(appNameId);
    }
    
    private void setAppSize(String appSize){
        mAppSize.setText(appSize);
    }
    
    private void setProgressButtonString(String text) {
        mDownLoadBtn.setText(text);
    }
    
    private void setProgressButtonProgress(int progress) {
        mDownLoadBtn.setProgress(progress);
    }
    
    private void setProgressButtonMax(int max) {
        mDownLoadBtn.setMax(max);
    }
    
    public void setProgressButtonOnClickListener(OnClickListener l) {
        mDownLoadBtn.setOnClickListener(l);
    }
    
    private void resetProgressButton() {
        mDownLoadBtn.removeProgressState();
    }
    
    public void setFrom(String from) {
        mFrom = from;
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mIsAttachToWindow = true;
        AppManager.getInstance().registerInstallStatusListener(this);
        AppManager.getInstance().registerUninstallStatusListener(this);
        if(mAppInfo != null){
            updateView();
        }
    }
    
    public void initProgressButton() {
        resetProgressButton();
        if (mAppInfo == null) {
            setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_install));
            return;
        }
        if(mAppInfo.flag == AppInfo.FLAG_ONLINE){//是在线的
            updateDownloadState(getDownloadInfo());
            if(!isDownloadSuccess()){
                registerDownloadObServer();
            }
        } else if(mAppInfo.flag == AppInfo.FLAG_NEED_UPGRADE){//有更新（更新和打开是互斥的）
            updateDownloadState(getDownloadInfo());
            if(!isDownloadSuccess()){
                registerDownloadObServer();
            }
        } else if(mAppInfo.flag == AppInfo.FLAG_INSTALLED){
            setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_open));
        } else if(mAppInfo.flag == AppInfo.FLAG_DOWNLOADED){
            setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_install));
        } else if (mAppInfo.flag == AppInfo.FLAG_INSTALLING) {
            setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_installing));
        } else {
            setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_install));
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mDownLoadBtn){
            if(mAppInfo.flag == AppInfo.FLAG_ONLINE || mAppInfo.flag == AppInfo.FLAG_NEED_UPGRADE){
                DownloadManager.getInstance().setCount(mAppInfo.id, 0);//重置记录的下载失败的次数
                setProgressButtonMax(100);
                startDownloadApp();
            } else if(mAppInfo.flag == AppInfo.FLAG_DOWNLOADED){
                startInstallApp();
            } else if(mAppInfo.flag ==  AppInfo.FLAG_INSTALLED){
                startOpenApp();
            }else if(mAppInfo.flag ==  AppInfo.FLAG_INSTALLING){
                File file = DataPool.getInstance().getAppInfo(mAppInfo.id).file;
                if (file == null || !file.exists()) {
                    reStartDownload();
                }else{
                    //startInstallApp();
                }
            }
        } else if (v == this) {
            if (mAppInfo != null) {
                if (mAppItemClickListener != null) {
                    mAppItemClickListener.onAppItemClicked(mAppInfo);
                } else {
                    Intent intent = new Intent(getContext(), AppDetailActivity.class);
                    intent.putExtra("app_id", mAppInfo.id);
                    getContext().startActivity(intent);
                }
            }
        }
    }

    private void startInstallApp(){
        File file = DataPool.getInstance().getAppInfo(mAppInfo.id).file;
        if (file != null && file.exists()) {
            AppManager.getInstance().endDownloadApp(mAppInfo, file);
        } else {
            reStartDownload();
        }
    }
    
    private void startOpenApp(){
        if (!AppManager.getInstance().startOpenApp(mAppInfo)) {
            if (mAppInfo.flag == AppInfo.FLAG_DOWNLOADED) {
                startInstallApp();
            }
        }
    }
    private void reStartDownload(){
        mAppInfo.flag = AppInfo.FLAG_ONLINE;
        DownloadManager.getInstance().reSetAppInfo(mAppInfo);
        DataPool.getInstance().updateAppInfo(mAppInfo);
        DataPool.getInstance().restoreToOnlineFlag(mAppInfo);
        DataPool.getInstance().removeAppInfo(DataPool.TYPE_APP_DOWNLOADED, mAppInfo);
        DataPool.getInstance().removeAppInfo(DataPool.TYPE_APP_INSTALLED, mAppInfo);
        DownloadManager.getInstance().mDownloadDBProvider.deleteDownloadInfo(mAppInfo.id + "_" + mAppInfo.vercode);
        initProgressButton();
        startDownloadApp();
        EventBus.getDefault().post("EVEN_REFRESH_LIST_DATA");
    }
    
    @Override
    public void onDownloadChanged(DownloadInfo info) {
        final DownloadInfo downloadInfo = info;
        if(!DownloadManager.getInstance().mDownloadDBProvider.isDownloadInfoExist(info)){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    resetProgressButton();
                    initProgressButton();
                }
            });
        } else {
            if (isNeedToRefresh(info)) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateDownloadState(downloadInfo);
                    }
                });
            }
        }
    }

    private boolean isNeedToRefresh(DownloadInfo info) {
        boolean isRefresh = false;
        if(info != null){
            isRefresh = (DownloadInfo.TYPE_APK.equals(info.type) && info.uid.equals(getRemoteId()));
        }
        return isRefresh;
    }

    private void registerDownloadObServer(){
        DownloadManager.getInstance().registerDownloadObserver(this);
    }
    
    private void deregisterDownloadObServer(){
        DownloadManager.getInstance().unregisterDownloadObserver(this);
    }
    
    private void updateDownloadState(final DownloadInfo downloadInfo) {
        if (downloadInfo != null) {
            int progress = downloadInfo.progress;
            switch (downloadInfo.downloadStatus) {
            case DownloadInfo.STATUS_PENDING:
                setProgressButtonMax(100);
                setProgressButtonProgress(progress);
                setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_waiting));
                break;
            case DownloadInfo.STATUS_DOWNLOADING:
                setProgressButtonMax(100);
                setProgressButtonProgress(progress);
                setProgressButtonString(progress + "%");
                break;
            case DownloadInfo.STATUS_PAUSE:
                setProgressButtonMax(100);
                setProgressButtonProgress(progress);
                setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_resume));
                break;
            case DownloadInfo.STATUS_DOWNLOAD_SUCCESS:
                downLoadSuccess();
                break;
            default:
                break;
            }
            if (DownloadInfo.isErrorStatus(downloadInfo.downloadStatus)) {
                setProgressButtonMax(100);
                setProgressButtonProgress(progress);

                synchronized (DownloadManager.class) {
                    int count = DownloadManager.getInstance().getCount(mAppInfo.id);
                    count++;
                    if (count <= DownloadManager.MazFailureNum) {
                        DownloadManager.getInstance().setCount(mAppInfo.id, count);
                        DownloadManager.getInstance().getMainHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                DownloadManager.getInstance().resumeDownload(downloadInfo.uid);
                            }
                        }, 300);
                    } else {
                        setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_waiting));
                    }
                }
            }
        } else {
            if(mAppInfo.flag == AppInfo.FLAG_NEED_UPGRADE){
                setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_update));
            } else if(mAppInfo.flag == AppInfo.FLAG_ONLINE){
                setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_install));
            }
        }
    }
    
    /**
     * 如果找的到downloadinfo就不用重新生成downloadinfo信息了
     * @return
     */
    private DownloadInfo getDownloadInfo() {
        ArrayList<DownloadInfo> allDownloads = DownloadManager.getInstance().mDownloadDBProvider.getAllDownloads();
        for (DownloadInfo downloadInfo : allDownloads) {
            if (downloadInfo.uid.equals(getRemoteId())) {
                return downloadInfo;
            }
        }
        return null;
    }
    
    /**
     * 如果下载完成了我就不去注册了
     * @return
     */
    private boolean isDownloadSuccess(){
        DownloadInfo info  = getDownloadInfo();
        if(info != null){
            return info.downloadStatus == DownloadInfo.STATUS_DOWNLOAD_SUCCESS;
        } else {
            return mAppInfo.flag == AppInfo.FLAG_DOWNLOADED;
        }
    }
    
    private void startDownloadApp(){
        final DownloadInfo downloadInfo = getDownloadInfo();
        if (downloadInfo != null && downloadInfo.downloadStatus == DownloadInfo.STATUS_DOWNLOADING) {
            // 如果状态是正在下载，点击后就去暂停下载
            // 规避按钮状态
            setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_resume));
            DownloadManager.getInstance().pauseDownload(downloadInfo.uid);
        } else if (downloadInfo != null && (downloadInfo.downloadStatus == DownloadInfo.STATUS_PENDING || downloadInfo.downloadStatus == DownloadInfo.STATUS_PAUSE || DownloadInfo.isErrorStatus(downloadInfo.downloadStatus))) {
            DownloadManager.getInstance().resumeDownload(downloadInfo.uid);
        } else if (downloadInfo != null && downloadInfo.downloadStatus == DownloadInfo.STATUS_DOWNLOAD_SUCCESS) {
            downLoadSuccess();
        } else {
            downloadApp();
        }
    }
    
    private void downloadApp(){
        mAppInfo.setFrom(mFrom);
        AppManager.getInstance().startDownloadApp(mAppInfo);
    }
    
    private String getRemoteId(){
        if(null==mAppInfo){
            return "";
        }
        String remoteId = mAppInfo.id + "_" + mAppInfo.vercode;
        return remoteId;
    }
    
    private void downLoadSuccess(){
        if (mAppInfo != null) {
            DataPool.getInstance().setDownloadFlag(mAppInfo);
            mAppInfo = DataPool.getInstance().getAppInfo(mAppInfo.id);
        }
        if (mAppInfo.flag == AppInfo.FLAG_INSTALLING) {
            setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_installing));
        } else if (mAppInfo.flag == AppInfo.FLAG_INSTALLED) {
            setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_open));
        } else {
            setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_install));
        }
    }
    
    
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIsAttachToWindow = false;
        mAppInfo = null;
        resetProgressButton();
        if (mImageLoadCallBack != null) {
            BitmapUtil.getInstance().clearCallerCallback(mImageLoadCallBack.getCaller());
            mImageLoadCallBack = null;
        }
        AppManager.getInstance().unregisterInstallStatusListener(this);
        AppManager.getInstance().unregisterUninstallStatusListener(this);
    }
    
    public void setOnAppItemClickListener(AppItemClickListener l) {
        mAppItemClickListener = l;
    }

    @Override
    public void onStartInstallApp(String packageName) {
        if (mAppInfo == null || packageName == null) {
            return;
        }
        if(packageName.equals(mAppInfo.pkg)){
            resetProgressButton();
            setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_installing));
            mAppInfo.flag = AppInfo.FLAG_INSTALLING;
        }
    }

    @Override
    public void onEndInstallApp(String packageName) {
        if (mAppInfo == null || packageName == null) {
            return;
        }
        if(packageName.equals(mAppInfo.pkg)){
            mAppInfo = DataPool.getInstance().getAppInfo(mAppInfo.id);
            setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_open));
            DataPool.getInstance().setInstallFlag(mAppInfo);
            AppManager.getInstance().registerUninstallStatusListener(this);
            deregisterDownloadObServer();
        }
    }

    @Override
    public void onInstallAppFail(String packageName) {
        if (mAppInfo == null || packageName == null) {
            return;
        }
        if (packageName.equals(mAppInfo.pkg)) {
            setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_install));
            mAppInfo.flag = AppInfo.FLAG_DOWNLOADED;
        }
    }

    @Override
    public void onStartUninstallApp(String packageName) {
        if (mAppInfo == null || packageName == null) {
            return;
        }
        
        if(packageName.equals(mAppInfo.pkg)){
            resetProgressButton();
            setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_uninstalling));
        }
    }

    @Override
    public void onEndUninstallApp(String packageName) {
        if (mAppInfo == null || packageName == null) {
            return;
        }
        
        if(packageName.equals(mAppInfo.pkg)){
            mAppInfo = DataPool.getInstance().getAppInfo(mAppInfo.id);
            registerDownloadObServer();
            setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_install));
            initProgressButton();
            AppManager.getInstance().unregisterUninstallStatusListener(this);
        }
    }
}
