package com.topwise.topos.appstore.view;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.conn.behavior.BehaviorEx;
import com.topwise.topos.appstore.conn.behavior.BehaviorLogManager;
import com.topwise.topos.appstore.data.AppInfo;
import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.data.AppInfo.Tag;
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
import com.topwise.topos.appstore.view.widget.MyTagHandler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.Html;
import android.text.Spannable;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;


@SuppressLint("NewApi")
public class ListAppItemView extends RelativeLayout implements DownloadManager.DownloadObserver, OnClickListener, InstallStatusListener, UninstallStatusListener {
    private static final String TAG = "ListAppItemView";

    private ImageView mAppIconView;
    private TextView mAppNameView;
    private TextView mAppInfoView;
    private String mFrom;

    private ProgressButton mDownloadBtn;
    private LinearLayout mAppTagContainer;

    private Resources mResources;
    private AppInfo mAppInfo;
    private ImageLoadCallBack mImageLoadCallBack;
    private AppItemClickListener mAppItemClickListener;

    public static final String APK_DOWNLOAD_INFO_FOR_FREE = "<html><font color=\"#757575\">%1$s</font>&nbsp;<font color=\"#FA5153\"><strike>%2$s</strike></font>&nbsp;<font color=\"#FA5153\">免流量</font></html>";
    public static final String APK_DOWNLOAD_INFO = "<html><font color=\"#757575\">%1$s</font>&nbsp;<font color=\"#757575\">%2$s</font></html>";

    private static final int WAN_NUMBER = 10000;
    private static final int YI_NUMBER = 100000000;
    private boolean mIsAttached = false;

    public ListAppItemView(Context context) {
        this(context, null, 0);
    }

    public ListAppItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListAppItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mResources = getResources();
        mImageLoadCallBack = new ImageLoadCallBack() {

            @Override
            public void onImageLoadSuccess(String imageUrl, Bitmap result) {
                if (!isAttached()) {
                    return;
                }
                ListAppItemView.this.updateImageWithUrl(imageUrl, result);
            }

            @Override
            public void onImageLoadFailed(String imageUrl, String reason) {
                LogEx.d(TAG, "imageUrl=" + imageUrl + ":reason=" + reason);
            }

            @Override
            public String getCaller() {
                return String.valueOf(ListAppItemView.this.hashCode());
            }

            @Override
            public boolean isNeedToDecode(String imageUrl) {
                if (!isAttached()) {
                    return false;
                }
                return ListAppItemView.this.checkUrl(imageUrl);
            }
        };

        setOnClickListener(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAppIconView = (ImageView) findViewById(R.id.zkas_id_list_item_app_icon);
        mAppNameView = (TextView) findViewById(R.id.zkas_id_list_item_app_name);
        mAppInfoView = (TextView) findViewById(R.id.zkas_id_list_item_app_used_info);
        mDownloadBtn = (ProgressButton) findViewById(R.id.zkas_id_list_item_progress_btn);
        mAppTagContainer = (LinearLayout) findViewById(R.id.zkas_id_list_item_app_tag_container);
    }

    public void setAppInfo(AppInfo info) {
        mAppInfo = info;
        if (isAttached() && mAppInfo != null) {
            setAppName(mAppInfo.name);
            Bitmap bmp = null;
            if (mAppInfo.icon_url != null && mAppInfo.icon_url.length() > 0) {
                bmp = BitmapUtil.getInstance().getBitmapAsync(mAppInfo.icon_url, mImageLoadCallBack);
            } else {
                if (mAppInfo.app_icon != null) {
                    bmp = Utils.drawableToBitmap(mAppInfo.app_icon);
                }
            }
            setAppIcon(bmp);

            String txt = null;
            if (mAppInfo.isfree) {
                txt = String.format(APK_DOWNLOAD_INFO_FOR_FREE, transUserNumber(mAppInfo.downloads), ListMainItemView.transApkSize(mAppInfo.size, false));
            } else {
                txt = String.format(APK_DOWNLOAD_INFO, transUserNumber(mAppInfo.downloads), ListMainItemView.transApkSize(mAppInfo.size, false));
            }
            if (info.bindId != null && info.bindId.length() > 0) {
                txt = String.format(APK_DOWNLOAD_INFO, transUserNumber(mAppInfo.downloads), ListMainItemView.transApkSize(mAppInfo.size, true));
            }
            setAppInformation(txt);

            if (mAppInfo.tags != null && mAppInfo.tags.size() > 0) {
                mAppTagContainer.removeAllViews();
                for (Tag t : mAppInfo.tags) {
                    addAppTag(t);
                }
            }
            resetProgressButton();
            initProgressButton();
        }
    }

    private String transUserNumber(String number) {
        String retvalue = "人使用";
        if (number == null || "".equals(number)) {
            return "";
        }
        try {
            int num = Integer.valueOf(number);
            if (num < WAN_NUMBER) {
                return number + retvalue;
            }

            if (num < YI_NUMBER) {
                float n = ((float) num / (float) WAN_NUMBER) + 0.5f;
                int ns = (int) n;
                return ns + "万" + retvalue;
            }

            if (num > YI_NUMBER) {
                float n = ((float) num / (float) YI_NUMBER);
                DecimalFormat df = new DecimalFormat("#.00");
                String r = df.format(n) + "亿";

                return r + retvalue;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return number;
        }
        return "";
    }

    public AppInfo getAppInfo() {
        return mAppInfo;
    }

    public void setAppIcon(Bitmap bmp) {
        if (bmp != null) {
            mAppIconView.setBackground(new BitmapDrawable(mResources, bmp));
        } else {
            mAppIconView.setBackgroundResource(R.drawable.zkas_app_default_icon_small);
        }
    }

    public void setAppName(String name) {
        mAppNameView.setText(name);
    }

    public void setAppInformation(String info) {
        mAppInfoView.setText(Html.fromHtml(info, null, new MyTagHandler()));
        mAppInfoView.setMovementMethod(MyMovementMethod.getInstance());
        mAppInfoView.setOnClickListener(this);
    }

    public void addAppTag(AppInfo.Tag tag) {
        if (tag == null) {
            return;
        }

        AppTagView v = new AppTagView(getContext());
        v.setText(tag.name);
        v.setTextColor(tag.txtcolor);
        v.setBackgroundColor(tag.bgcolor);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.rightMargin = getResources().getDimensionPixelSize(R.dimen.zkas_list_item_app_tag_right_margin);
        mAppTagContainer.addView(v, 0, lp);
    }

    public void cleanAllAppTags() {
        mAppTagContainer.removeAllViews();
    }

    public void setProgressButtonString(String text) {
        mDownloadBtn.setText(text);
    }

    public void setProgressButtonProgress(int progress) {
        mDownloadBtn.setProgress(progress);
    }

    public void setProgressButtonMax(int max) {
        mDownloadBtn.setMax(max);
    }

    public void setProgressButtonOnClickListener(OnClickListener l) {
        mDownloadBtn.setOnClickListener(l);
    }

    public void resetProgressButton() {
        mDownloadBtn.removeProgressState();
    }

    private void startInstallApp() {
        File file = DataPool.getInstance().getAppInfo(mAppInfo.id).file;
        if (file != null && file.exists()) {
            AppManager.getInstance().endDownloadApp(mAppInfo, file);
        } else {
            reStartDownload();
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

    private void startOpenApp() {
        if (!AppManager.getInstance().startOpenApp(mAppInfo)) {
            if (mAppInfo.flag == AppInfo.FLAG_DOWNLOADED) {
                startInstallApp();
            }
        }
    }

    public void updateImageWithUrl(String url, Bitmap bmp) {
        if (url == null || url.length() == 0 || mAppInfo == null || bmp == null) {
            return;
        }

        if (url.equals(mAppInfo.icon_url)) {
            mAppIconView.setBackground(new BitmapDrawable(mResources, bmp));
        }
    }

    public boolean checkUrl(String url) {
        if (url == null || url.length() == 0 || mAppInfo == null) {
            return false;
        }

        if (url.equals(mAppInfo.icon_url)) {
            return true;
        }
        return false;
    }

    public void setFrom(String from) {
        mFrom = from;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mIsAttached = true;
        AppManager.getInstance().registerInstallStatusListener(this);
        AppManager.getInstance().registerUninstallStatusListener(this);
        if (mAppInfo != null) {
            setAppInfo(mAppInfo);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIsAttached = false;
        BitmapUtil.getInstance().clearCallerCallback(mImageLoadCallBack.getCaller());
        AppManager.getInstance().unregisterInstallStatusListener(this);
        AppManager.getInstance().unregisterUninstallStatusListener(this);
    }

    public boolean isAttached() {
        return mIsAttached;
    }

    private void initProgressButton() {
        resetProgressButton();
        setProgressButtonOnClickListener(this);
//        if (mAppInfo == null) {
//            setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_install));
//            return;
//        }


        if (mAppInfo.flag == AppInfo.FLAG_ONLINE) {// 是在线的
            updateDownloadState(getDownloadInfo());
            if (!isDownloadSuccess()) {
                registerDownloadObServer();
            }
        } else if (mAppInfo.flag == AppInfo.FLAG_NEED_UPGRADE) {// 有更新（更新和打开是互斥的）
            updateDownloadState(getDownloadInfo());
            if (!isDownloadSuccess()) {
                registerDownloadObServer();
            }
        } else if (mAppInfo.flag == AppInfo.FLAG_INSTALLED) {
            setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_open));
        } else if (mAppInfo.flag == AppInfo.FLAG_DOWNLOADED) {
            setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_install));
        } else if (mAppInfo.flag == AppInfo.FLAG_INSTALLING) {
            setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_installing));
        } else {
            setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_install));
        }
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
            if (mAppInfo.flag == AppInfo.FLAG_NEED_UPGRADE) {
                setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_update));
            } else if (mAppInfo.flag == AppInfo.FLAG_ONLINE) {
                setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_install));
            }
        }
    }


    @Override
    public void onClick(View v) {
        if (v == mDownloadBtn) {
            if (mAppInfo.flag == AppInfo.FLAG_ONLINE || mAppInfo.flag == AppInfo.FLAG_NEED_UPGRADE) {
                DownloadManager.getInstance().setCount(mAppInfo.id, 0);//重置记录的下载失败的次数
                setProgressButtonMax(100);
                startDownloadApp();
            } else if (mAppInfo.flag == AppInfo.FLAG_DOWNLOADED) {
                startInstallApp();
            } else if (mAppInfo.flag == AppInfo.FLAG_INSTALLED) {
                startOpenApp();
            } else if (mAppInfo.flag == AppInfo.FLAG_INSTALLING) {
                File file = DataPool.getInstance().getAppInfo(mAppInfo.id).file;
                if (file == null || !file.exists()) {
                    reStartDownload();
                }else{
                    //startInstallApp();
                }
            }
        } else if (v == this || v == mAppInfoView) {
            if (mAppInfo != null) {
                if (mAppItemClickListener != null) {
                    mAppItemClickListener.onAppItemClicked(mAppInfo);
                } else {
                    Intent intent = new Intent(getContext(), AppDetailActivity.class);
                    intent.putExtra("app_id", mAppInfo.id);
                    getContext().startActivity(intent);
                    try {
                        BehaviorLogManager.getInstance().addBehaviorEx(new BehaviorEx(BehaviorEx.ENTER_APP_DETAIL, mAppInfo.id + "@" + mAppInfo.from));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onDownloadChanged(DownloadInfo info) {
        final DownloadInfo downloadInfo = info;
        if (!DownloadManager.getInstance().mDownloadDBProvider.isDownloadInfoExist(info)) {
            post(new Runnable() {
                @Override
                public void run() {
                    resetProgressButton();
                    initProgressButton();
                }
            });
        } else {
            if (isNeedToRefresh(info)) {
                post(new Runnable() {
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
        if (info != null) {
            isRefresh = (DownloadInfo.TYPE_APK.equals(info.type) && info.uid.equals(getRemoteId()));
        }
        return isRefresh;
    }

    private void registerDownloadObServer() {
        DownloadManager.getInstance().registerDownloadObserver(this);
    }

    private void deregisterDownloadObServer() {
        DownloadManager.getInstance().unregisterDownloadObserver(this);
    }


    /**
     * 如果找的到downloadinfo就不用重新生成downloadinfo信息了
     *
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
     *
     * @return
     */
    private boolean isDownloadSuccess() {
        DownloadInfo info = getDownloadInfo();
        if (info != null) {
            return info.downloadStatus == DownloadInfo.STATUS_DOWNLOAD_SUCCESS;
        } else {
            return mAppInfo.flag == AppInfo.FLAG_DOWNLOADED;
        }
    }

    private void startDownloadApp() {
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

    private void downloadApp() {
        mAppInfo.setFrom(mFrom);
        AppManager.getInstance().startDownloadApp(mAppInfo);
    }

    private String getRemoteId() {
        String remoteId = mAppInfo.id + "_" + mAppInfo.vercode;
        return remoteId;
    }

    private void downLoadSuccess() {
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

    public void setOnAppItemClickListener(AppItemClickListener l) {
        mAppItemClickListener = l;
    }

    public void setDividerVisibility(boolean visible) {
        findViewById(R.id.zkas_id_app_item_btm_divider).setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onStartInstallApp(String packageName) {
        if (mAppInfo == null || packageName == null) {
            return;
        }
        if (packageName.equals(mAppInfo.pkg)) {
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
        if (packageName.equals(mAppInfo.pkg)) {
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

        if (packageName.equals(mAppInfo.pkg)) {
            resetProgressButton();
            setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_uninstalling));
        }
    }

    @Override
    public void onEndUninstallApp(String packageName) {
        if (mAppInfo == null || packageName == null) {
            return;
        }

        if (packageName.equals(mAppInfo.pkg)) {
            mAppInfo = DataPool.getInstance().getAppInfo(mAppInfo.id);
            registerDownloadObServer();
            setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_install));
            initProgressButton();
            AppManager.getInstance().unregisterUninstallStatusListener(this);
        }
    }

    private static class MyMovementMethod extends ScrollingMovementMethod {
        static MyMovementMethod sInstance = null;

        public static MyMovementMethod getInstance() {
            if (sInstance == null) {
                sInstance = new MyMovementMethod();
            }
            return sInstance;
        }

        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP) {
                widget.performClick();
            }
            return true;
        }
    }
}
