package com.topwise.topos.appstore.view;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.conn.behavior.BehaviorEx;
import com.topwise.topos.appstore.conn.behavior.BehaviorLogManager;
import com.topwise.topos.appstore.conn.behavior.UserTrack;
import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.data.Welcome;
import com.topwise.topos.appstore.manager.AdsManager;
import com.topwise.topos.appstore.manager.BusinessManager;
import com.topwise.topos.appstore.manager.HttpManager;
import com.topwise.topos.appstore.manager.ManagerCallback;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.utils.Properties;
import com.topwise.topos.appstore.utils.Utils;
import com.topwise.topos.appstore.view.activity.AppDetailActivity;
import com.topwise.topos.appstore.view.activity.GroupActivity;
import com.topwise.topos.appstore.view.activity.H5Activity;
import com.main.ads.ad.SplashAdView;
import com.main.ads.ad.SplashAdViewCallBack;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WelcomeView extends RelativeLayout {

    private Context mContext;

    private FrameLayout mWelcomeAdContainer;
    private ImageView mWelcomeImg;
    private SplashAdView mSplashAdView;
    private View mSkipLayout;
    private View mAdTagLayout;
    private TextView mSkipTextView;

    private Welcome mWelcome;

    private WelcomeGoneListener mWelcomeGoneListener = null;

    private boolean isAttachedToWindow = false;

    public WelcomeView(Context context, WelcomeGoneListener l) {
        this(context, null, 0);
        mWelcomeGoneListener = l;
    }

    public WelcomeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WelcomeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        ViewGroup welcomeLayout = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.as_welcome_layout, this, true);
        mWelcomeAdContainer = (FrameLayout) welcomeLayout.findViewById(R.id.welcome_ad_container);
        mWelcomeImg = (ImageView) welcomeLayout.findViewById(R.id.welcome_ad);

        ImageView logoIcon = (ImageView) welcomeLayout.findViewById(R.id.logo_icon);
        //根据渠道号设置欢迎界面对应的图片
        if (Properties.CHANNEL_IVVI.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
            logoIcon.setImageResource(R.drawable.as_welcome_logo_ivvi);
        } else if (Properties.CHANNEL_COOLMART.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
            logoIcon.setImageResource(R.drawable.as_welcome_logo_coolmart);
        } else if (Properties.CHANNEL_SHARP.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
            logoIcon.setImageResource(R.drawable.as_welcome_logo_sharp);
        } else if (Properties.CHANNEL_DUOCAI.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
            logoIcon.setImageResource(R.drawable.as_welcome_logo_duocai);
        } else if (Properties.CHANNEL_17WO.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
            logoIcon.setImageResource(R.drawable.as_welcome_logo_apphome);
        }

        TextView versionText = (TextView) welcomeLayout.findViewById(R.id.version_text);
        versionText.setText("Version V" + AppStoreWrapperImpl.getInstance().getAppVersionName());

        mSkipLayout = welcomeLayout.findViewById(R.id.skip_layout);
        mSkipLayout.setVisibility(View.GONE);
        final long curTime = System.currentTimeMillis();
        mSkipLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BehaviorLogManager.getInstance().addBehaviorEx(new BehaviorEx(BehaviorEx.SKIP_WELCOME, (mWelcome == null ? "" : mWelcome.title) + "," + (System.currentTimeMillis()-curTime)));
                if (mWelcomeGoneListener != null) {
                    mWelcomeGoneListener.onWelcomeGoneDelayed(0);
                }
            }
        });
        mSkipTextView = (TextView) welcomeLayout.findViewById(R.id.skip_txt);

        mAdTagLayout = welcomeLayout.findViewById(R.id.adtag_layout);
        mAdTagLayout.setVisibility(View.GONE);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                ArrayList<Welcome> welcomes = BusinessManager.getInstance().loadAd(new ManagerCallback() {
                    @Override
                    public void onSuccess(String moduleType, int dataType, int page, int num, boolean end) {
                        ArrayList<Welcome> welcomes = DataPool.getInstance().getWelcomes();
                        if (welcomes != null && welcomes.size() > 0) {
                            setWelcomeView(welcomes);
                            mSkipLayout.setVisibility(View.VISIBLE);
                            mAdTagLayout.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(String moduleType, int dataType, Throwable t, int errorNo, String strMsg) {
                    }
                });

                if (welcomes != null && welcomes.size() > 0) {
                    setWelcomeView(welcomes);
                    mSkipLayout.setVisibility(View.VISIBLE);
                    mAdTagLayout.setVisibility(View.VISIBLE);
                }
            }
        }, 100);

        mSplashAdView = AdsManager.getInstance().createSplashAdView(mContext, new SplashAdViewCallBack() {
            @Override
            public void onAdPresent(String s) {
                LogEx.d(s);
                mSkipLayout.setVisibility(View.VISIBLE);
                mAdTagLayout.setVisibility(View.VISIBLE);
                if (mWelcomeGoneListener != null) {
                    mWelcomeGoneListener.onWelcomeGoneDelayed(5 * 1000);
                }
            }

            @Override
            public void onAdFailed(String s, String s1) {
                LogEx.d(s, s1);
            }

            @Override
            public void onAdDismissed(String s) {
                LogEx.d(s);
            }

            @Override
            public void onAdClick(String s) {
                if (mWelcomeGoneListener != null) {
                    mWelcomeGoneListener.onWelcomeGoneDelayed(0);
                }
            }
        });
        if (mSplashAdView != null) {
            mWelcomeAdContainer.addView(mSplashAdView);
            if (mWelcomeGoneListener != null) {
                mWelcomeGoneListener.onWelcomeGoneDelayed(3 * 1000);
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedToWindow = true;
        if (mSplashAdView != null) {
            mSplashAdView.onStart();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttachedToWindow = false;
        if (mSplashAdView != null) {
            mSplashAdView.onPause();
            mSplashAdView.destroy();
        }
    }

    public interface WelcomeGoneListener {
        void onWelcomeGoneDelayed(long timeMills);
    }

    private void setWelcomeView(ArrayList<Welcome> welcomes) {
        for (Welcome w : welcomes) {
            if (parseMillis(w.show_time_start) <= parseMillis(getNowTimeString()) && parseMillis(getNowTimeString()) <= parseMillis(w.show_time_end)) {
                mWelcome = w;
                break;
            }
        }
        if (mWelcome == null) {
            return;
        }
        final long curTime = System.currentTimeMillis();
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BehaviorLogManager.getInstance().addBehaviorEx(new BehaviorEx(BehaviorEx.ENTER_WELCOME, (mWelcome == null ? "" : mWelcome.title) + "," + (System.currentTimeMillis()-curTime)));
                startWelcomeActivity(mWelcome);
                if (mWelcomeGoneListener != null) {
                    mWelcomeGoneListener.onWelcomeGoneDelayed(0);
                }
            }
        });
        if (mWelcomeGoneListener != null) {
            if (mWelcome.timeout <= 0) {
                mWelcome.timeout = 2;
            }
            mWelcomeGoneListener.onWelcomeGoneDelayed(mWelcome.timeout * 1000);

            mSkipTextView.setText(mContext.getString(R.string.skip) + (mWelcome.timeout > 0 ? mWelcome.timeout : ""));
            final Handler handler = new Handler(Looper.getMainLooper());
            final Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    mWelcome.timeout--;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mSkipTextView.setText(mContext.getString(R.string.skip) + (mWelcome.timeout > 0 ? mWelcome.timeout : ""));
                        }
                    });
                    if (mWelcome.timeout <= 0) {
                        timer.cancel();
                    }
                }
            };
            timer.schedule(timerTask, 1000, 1000);
        }

        String fileName = Properties.CACHE_PATH + Utils.getMd5(mWelcome.imgurl) + ".jpg";
        mWelcome.img_file_path = fileName;

        if (mWelcome.img_file_path != null && !"".equals(mWelcome.img_file_path)) {
            Bitmap bitmap = BitmapFactory.decodeFile(mWelcome.img_file_path);
            if (bitmap != null && isAttachedToWindow) {
                mWelcomeImg.setImageBitmap(bitmap);
                return;
            }
        }

        HttpManager.getInstance().createDownloader(mWelcome.title, mWelcome.imgurl, fileName, new HttpManager.DownloadProgressListener() {

            @Override
            public void onStart() {
            }

            @Override
            public void onLoading(long count, long current) {
            }

            @Override
            public void onSuccess(File file) {
                mWelcome.img_file_path = file.getAbsolutePath();
                Bitmap bitmap = BitmapFactory.decodeFile(mWelcome.img_file_path);
                if (bitmap != null && isAttachedToWindow) {
                    mWelcomeImg.setImageBitmap(bitmap);
                }
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
            }
        }).startDownload();
    }

    private long parseMillis(String timeString) {
        try {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
            c.setTime(format.parse(timeString));
            return c.getTimeInMillis();
        } catch (Exception e) {
            return 0;
        }
    }

    private static String getNowTimeString() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        return "" + (hour >= 10 ? hour : "0" + hour) + ":" + (minute >= 10 ? minute : "0" + minute) + ":" + (second >= 10 ? second : "0" + second);
    }

    private void startWelcomeActivity(Welcome info) {
        UserTrack.getInstance().openAd(info);
        if (info.event == 0) {
            return;
        } else if (info.event == 1) {
            Intent intent = new Intent(mContext, H5Activity.class);
            intent.putExtra("url", info.event_data.url);
            intent.putExtra("title", info.title);
            mContext.startActivity(intent);
        } else if (info.event == 2) {
            Intent intent = null;
            List<PackageInfo> packages = mContext.getPackageManager().getInstalledPackages(0);
            boolean hasApk = false;
            for (PackageInfo p : packages) {
                if (p.packageName.contains(info.event_data.packagename)) {
                    if (p.versionCode >= info.event_data.versionCode) {
                        hasApk = true;
                    }
                    break;
                }
            }
            if (hasApk) {
                if (info.event_data.intent != null && !"".equals(info.event_data.intent)) {
                    intent = new Intent(info.event_data.intent);
                } else {
                    intent = mContext.getPackageManager().getLaunchIntentForPackage(info.event_data.packagename);
                }
            } else {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(info.event_data.apk_url));
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        } else if (info.event == 3) {
            if ("app".equals(info.event_data.type)) {
                Intent intent = new Intent(mContext, AppDetailActivity.class);
                intent.putExtra("app_id", info.event_data.type_id);
                mContext.startActivity(intent);
            } else if ("page".equals(info.event_data.type)) {
                Intent intent = new Intent(mContext, GroupActivity.class);
                intent.putExtra("title", info.title);
                intent.putExtra("type", "page");

                try {
                    int[] ids = new int[1];
                    ids[0] = Integer.valueOf(info.event_data.type_id);
                    intent.putExtra("id", ids);
                } catch (Exception e) {
                }
                mContext.startActivity(intent);
            } else if ("label".equals(info.event_data.type)) {
                Intent intent = new Intent(mContext, GroupActivity.class);
                intent.putExtra("title", info.title);
                intent.putExtra("type", "label");

                try {
                    int[] ids = new int[1];
                    ids[0] = Integer.valueOf(info.event_data.type_id);
                    intent.putExtra("id", ids);
                } catch (Exception e) {
                }
                mContext.startActivity(intent);
            } else if ("rank".equals(info.event_data.type)) {
                Intent intent = new Intent(mContext, GroupActivity.class);
                intent.putExtra("title", info.title);
                intent.putExtra("type", "rank");

                try {
                    int[] ids = new int[1];
                    ids[0] = Integer.valueOf(info.event_data.type_id);
                    intent.putExtra("id", ids);
                } catch (Exception e) {
                }
                mContext.startActivity(intent);
            } else if ("type".equals(info.event_data.type)) {
                Intent intent = new Intent(mContext, GroupActivity.class);
                intent.putExtra("title", info.title);
                intent.putExtra("type", "type");

                try {
                    int[] ids = new int[1];
                    ids[0] = Integer.valueOf(info.event_data.type_id);
                    intent.putExtra("id", ids);
                } catch (Exception e) {
                }
                mContext.startActivity(intent);
            }
        }
    }

}
