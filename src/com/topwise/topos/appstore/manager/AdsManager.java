package com.topwise.topos.appstore.manager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.conn.behavior.BehaviorEx;
import com.topwise.topos.appstore.conn.behavior.BehaviorLogManager;
import com.topwise.topos.appstore.data.Banner;
import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.utils.LogEx;
import com.main.ads.MainSDK;
import com.main.ads.ad.NativeAd;
import com.main.ads.ad.NativeAdsManager;
import com.main.ads.ad.SplashAdView;
import com.main.ads.ad.SplashAdViewCallBack;
import com.main.ads.base.AdErrorBase;
import com.main.ads.base.AdListenerBase;
import com.main.ads.base.AdsManagerListenerBase;
import com.main.ads.base.NativeAdBase;

import java.util.ArrayList;
import java.util.HashMap;

public class AdsManager extends BaseManager {

    private static AdsManager mThis = null;

    private String mPlacementId = null;
    private String mZKAppId = null;
    private String mZKWelcomeAdId = null;

    private SplashAdView mSplashAdView = null;

    public static AdsManager getInstance() {
        if (mThis == null) {
            synchronized (AdsManager.class) {
                if (mThis == null) {
                    mThis = new AdsManager();
                }
            }
        }
        return mThis;
    }

    public AdsManager() {
      //  MainSDK.init(AppStoreWrapperImpl.getInstance().getAppContext());
        try {
            ApplicationInfo appInfo = AppStoreWrapperImpl.getInstance().getAppContext().getPackageManager().getApplicationInfo(AppStoreWrapperImpl.getInstance().getAppContext().getPackageName(), PackageManager.GET_META_DATA);
            mPlacementId = appInfo.metaData.getString("ADS_PLACEMENTID");
            mZKAppId = "" + appInfo.metaData.getInt("ZK_APPID");
            mZKWelcomeAdId = "" + appInfo.metaData.getInt("ZK_WELCOME_ADID");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SplashAdView createSplashAdView(Context context, SplashAdViewCallBack callBack) {
        try {
            mSplashAdView = new SplashAdView(context);
            mSplashAdView.setAdLoadCallBack(callBack);
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(MainSDK.KEY_ZK_ADID, mZKWelcomeAdId);
            map.put(MainSDK.KEY_ZK_APPID, mZKAppId);
            mSplashAdView.setAdIds(map, AppStoreWrapperImpl.getInstance().getIbimuyuChannel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mSplashAdView;
    }

    public void loadSingleAd(final Runnable runnable) {
        if (mPlacementId == null) {
            mPlacementId = "1735948206647098_1791363837772201";
        }
        //原生广告
        NativeAd nativeAdZk = new NativeAd(AppStoreWrapperImpl.getInstance().getAppContext(), mPlacementId);
        nativeAdZk.setAdListener(new AdListenerBase() {

            @Override
            public void onError(NativeAdBase adBase, AdErrorBase adErrorBase) {
                LogEx.d("onError," + adErrorBase.getErrorMessage());
            }

            @Override
            public void onAdLoaded(NativeAdBase adBase) {
                NativeAdBase ad =  adBase;
                Banner b = new Banner();
                b.title = ad.getAdTitle();
                b.desc = ad.getAdBody();
                b.img_url = ad.getAdCoverImage().getUrl();
                b.is_ads = true;
                b.adsAd = ad;
                DataPool.getInstance().addBanner(DataPool.TYPE_BANNER_RANDOM, b);
                runnable.run();
            }

            @Override
            public void onAdClicked(NativeAdBase adBase) {
                BehaviorLogManager.getInstance().addBehaviorEx(new BehaviorEx(BehaviorEx.CLICK_ADS_AD, (adBase).getAdTitle()));
            }
        });
        nativeAdZk.loadAd();
    }

    public void loadMultiAds() {
        if (mPlacementId == null) {
            mPlacementId = "1735948206647098_1791363837772201";
        }
        final NativeAdsManager adsManager = new NativeAdsManager(AppStoreWrapperImpl.getInstance().getAppContext(), mPlacementId, 10);
        adsManager.setListener(new AdsManagerListenerBase() {

            @Override
            public void onAdsLoaded() {
                ArrayList<NativeAdBase> ads =  adsManager.getAllNativeAds();
                if (ads == null || ads.size() == 0) {
                    return;
                }
                int i = 0;
                for (NativeAdBase ad : ads) {
                    Banner b = new Banner();
                    b.title = ad.getAdTitle();
                    b.desc = ad.getAdBody();
                    b.img_url = ad.getAdCoverImage().getUrl();
                    b.is_ads = true;
                    b.adsAd = ad;
                    DataPool.getInstance().addBanner(DataPool.TYPE_BANNER_LARGE, b);
                    i++;
                }
            }

            @Override
            public void onAdError(AdErrorBase adErrorBase) {
                LogEx.d("onAdError," + adErrorBase.getErrorMessage());
            }
        });
        adsManager.loadAds();
    }

}
