package com.topwise.topos.appstore;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.topwise.topos.appstore.data.AppUpgradeInfo;
import com.topwise.topos.appstore.database.SharedPreferencesCenter;
import com.topwise.topos.appstore.manager.ActivityManager;
import com.topwise.topos.appstore.manager.SelfUpgradeCenter;
import com.topwise.topos.appstore.service.KillNotificationsService;
import com.topwise.topos.appstore.utils.Properties;
import com.topwise.topos.appstore.utils.Utils;
import com.topwise.topos.appstore.view.WelcomeView;
import com.topwise.topos.appstore.view.dialog.DialogUtils;
import com.topwise.topos.appstore.view.fragment.GuidFragment;
import com.topwise.topos.appstore.view.fragment.MainFragment;
import com.umeng.analytics.MobclickAgent;

/**
 * 应用商店以aar方式提供给其他应用使用，主界面是MainFragment，此Activity是调试用的主Activity，在此处模拟宿主Activity的代码。
 */
public class MainActivity extends FragmentActivity{

    private static final int MSG_UI_DISMISS_WELCOME = 0;
    private static final int MSG_CHECK_APP_VERSION = 1;
    private static final int MSG_UI_DISMISS_GUIDEPAGE = 2;

    private ViewGroup mRootView;
    private WelcomeView mWelcomeView;

    private boolean isInit = false;

    private long exitTime = 0;
    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.addActivity(this);
        //初始化一些HashMap和SparseArray集合
        AppStoreWrapperImpl.getInstance().setApplicationContext(this.getApplicationContext());
        //布局内容为空
        mRootView = (ViewGroup) getLayoutInflater().inflate(R.layout.as_activity_testmain, null, false);
        setContentView(mRootView);

        Utils.setWhiteStatusBar(this);

        sp = SharedPreferencesCenter.getInstance().getSharedPreferences();

        boolean isPrompt = sp.getBoolean("prompt", false);
        if (!Properties.CHANNEL_IVVI.equals(AppStoreWrapperImpl.getInstance().getChannel()) && !Properties.CHANNEL_SHARP.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
            SharedPreferencesCenter.getInstance().getSharedPreferences().edit().putBoolean("prompt", true).commit();
            isPrompt = true;
        }
        //这个是附在手机底端的权限提示框,当prompt属性是false的时候会显示,需要配置
        if (!isPrompt) {
            DialogUtils.setCheckBoxDialog(this, getString(R.string.quit), getString(R.string.accept),
                    getString(R.string.prompt),
                    getString(R.string.prompt_permission), getString(R.string.checkboxtitle), null,
                    new Runnable() {
                        @Override
                        public void run() {
                            mUIHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    checkPermissions();
                                }
                            });
                        }
                    });
        } else {
            checkPermissions();
        }
        //进程杀死时通知也跟着关闭
        bindService(new Intent(this, KillNotificationsService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //有盟数据分析
//        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //有盟数据分析
   //     MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUIHandler.removeMessages(MSG_UI_DISMISS_WELCOME);
        mUIHandler.removeMessages(MSG_CHECK_APP_VERSION);
        mUIHandler.removeMessages(MSG_UI_DISMISS_GUIDEPAGE);
        AppStoreWrapperImpl.getInstance().destroy();
        //进程杀死时通知也跟着关闭
        unbindService(mConnection);
        ActivityManager.removeActivity(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), getString(R.string.back_toast),
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    //进程杀死时通知也跟着关闭
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            ((KillNotificationsService.KillBinder) binder).service.startService(new Intent(MainActivity.this, KillNotificationsService.class));
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
        }
    };

    private void init() {
        AppStoreWrapperImpl.getInstance().init();
        initUI();
    }

    //判断当前是否有网络链接
    private boolean isHaveNetWork(){
        boolean ishaveNetwork=false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
// 获取NetworkInfo对象
        NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
        for(int i=0;i<networkInfo.length;i++){
            if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED){
                ishaveNetwork = true;
            }
        }
        return ishaveNetwork;
    }

    private void lazyInitMainThread() {
        // 主fragment
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.main_fragment_container, new MainFragment());
        ft.addToBackStack("main");
        ft.commitAllowingStateLoss();
    }

    private void lazyInitGuidThread() {
        // Guidfragment
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.main_fragment_container, new GuidFragment());
        ft.addToBackStack("guide");
        ft.commitAllowingStateLoss();
    }

    //为了系统拉活我发的广播,系统收到,不做任何处理,如果收不到,帮忙做个拉活().
    private void lazyInitAsync() {
        new AppServiceWatch(this, this.getPackageName(), this.getLocalClassName()).startWatchService();
    }

    private void initUI() {
        // 欢迎界面,回调
        mWelcomeView = new WelcomeView(this, new WelcomeView.WelcomeGoneListener() {
            @Override
            public void onWelcomeGoneDelayed(long timeMills) {
                mUIHandler.removeMessages(MSG_UI_DISMISS_WELCOME);
                mUIHandler.sendEmptyMessageDelayed(MSG_UI_DISMISS_WELCOME, timeMills);
            }
        });
        mRootView.addView(mWelcomeView);
        mUIHandler.sendEmptyMessageDelayed(MSG_UI_DISMISS_WELCOME, 3*1000);
    }

    public Handler mUIHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UI_DISMISS_WELCOME:
                    if (isDestroyed()) {
                        return;
                    }
                    mWelcomeView.setVisibility(View.GONE);
                    mRootView.removeView(mWelcomeView);
                    mUIHandler.sendEmptyMessageDelayed(MSG_CHECK_APP_VERSION, 2*1000);
                    if (!isInit) {
                        //首次进入应用的SharedPreferences
                        boolean isFirstRun = sp.getBoolean("isFirstRun", true);
                        Editor editor = sp.edit();
                        if (isFirstRun && isHaveNetWork()){
                            //进入到引导界面
                            lazyInitGuidThread();
                            editor.putBoolean("isFirstRun", false);
                            editor.commit();
                        }else{
                            //进到主界面
                            lazyInitMainThread();
                        }
                        //
                        HandlerThread ht = new HandlerThread("lazyInitAsync");
                        ht.start();

                        Handler handler = new Handler(ht.getLooper()) {
                            @Override
                            public void handleMessage(Message msg) {
                                lazyInitAsync();
                                super.handleMessage(msg);
                            }
                        };
                        handler.sendEmptyMessage(0);
                        isInit = true;
                    }
                    break;
                case MSG_CHECK_APP_VERSION:
                    SelfUpgradeCenter.getInstance().checkUpdate(mCheckVersionCallback);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private SelfUpgradeCenter.UpdateCallback mCheckVersionCallback = new SelfUpgradeCenter.UpdateCallback() {
        @Override
        public void onSuccess(final AppUpgradeInfo appInfo) {
            if (appInfo.isForceUpgrade) {
                SelfUpgradeCenter.getInstance().registerDownloadApkReceiver(MainActivity.this);
                SelfUpgradeCenter.getInstance().downloadSelfUpgradeApk(appInfo);
                return;
            }
            String promptMsg = getString(R.string.update_version) + appInfo.verName + "\n"
                    + getString(R.string.update_date) + appInfo.date + "\n\n"
                    + getString(R.string.update_content) + "\n" + appInfo.desc;
            DialogUtils.setNormalDialog(MainActivity.this,getString(R.string.version_delay_install_text),
                    getString(R.string.version_install_text),getString(R.string.new_update),promptMsg,
                    new Runnable() {
                        @Override
                        public void run() {
                            SelfUpgradeCenter.getInstance().registerDownloadApkReceiver(MainActivity.this);
                            SelfUpgradeCenter.getInstance().downloadSelfUpgradeApk(appInfo);
                        }
                    });
        }

        @Override
        public void onFailure(Throwable t, int errorNo, String strMsg) {
        }
    };

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            init();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults == null || grantResults.length == 0) {
                permissionNotGrant();
                return;
            }
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    continue;
                } else {
                    permissionNotGrant();
                    return;
                }
            }
            init();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void permissionNotGrant() {
        DialogUtils.setOkCancelDialog(MainActivity.this, getString(R.string.prompt), getString(R.string.permission_not_grant), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    try {
                        Intent intent = new Intent();
                        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                        intent.setData(Uri.fromParts("package",getPackageName(), null));
                        startActivity(intent);
                    } catch (Exception e) {
                    }
                    finish();
                } else {
                    finish();
                }
            }
        });
    }
}

