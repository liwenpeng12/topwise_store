package com.topwise.topos.appstore.view.fragment;

import java.util.ArrayList;

import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.data.AppInfo;
import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.data.DataPool.DataObserver;
import com.topwise.topos.appstore.manager.AppManager;
import com.topwise.topos.appstore.manager.ManagerCallback;
import com.topwise.topos.appstore.manager.SettingsManager;
import com.topwise.topos.appstore.service.InstallService;
import com.topwise.topos.appstore.utils.Properties;
import com.topwise.topos.appstore.utils.Utils;
import com.topwise.topos.appstore.view.AppStoreSettingsItem;
import com.topwise.topos.appstore.view.ArrayListAdapter;
import com.topwise.topos.appstore.view.ListAppItemView;
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class DownloadUpdateFragment extends BaseFragment {

    private static DownloadUpdateFragment mInstance;
    private ListView mUpgradeList;
    private ListView mRecommendlist;
    private UpdradeAdapter mUpdradeAdapter;
    private RecommendAdapter mRecommendAdapter;
    private ArrayList<AppInfo> mNeedUpgradeList;
    private View mNoTaskNoNetworkLayout;
    private View mBottomLayout;
    private AppStoreSettingsItem mAutoInstallItem;
    private TextView mAppText;
    private TextView mAppFind;
    private ArrayList<AppInfo> mRecommendData;
    private View headview;
    private TextView mChangeRecommendData;
    private View HeadView;
    private Button mSettingautoins;
    private DataObserver mObserver;
    
    private Handler mMainThreadHandler;

    public static DownloadUpdateFragment getInstance() {
        if (mInstance == null) {
            mInstance = new DownloadUpdateFragment();
        }
        return mInstance;
    }

    @SuppressLint("NewApi")
    private View getHeadView() {
        if (headview == null) {
            headview = LayoutInflater.from(getActivity()).inflate(
                    R.layout.zkas_download_headview_layout, null);
            ImageView m = (ImageView) headview.findViewById(R.id.notask_small);
            m.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.zkas_download_upgrade_prompt));
            m.setScaleType(ScaleType.CENTER);
            TextView n = (TextView) headview.findViewById(R.id.notask_small_text);
            n.setText(R.string.no_upgrade_prompt);
            mChangeRecommendData = (TextView) headview.findViewById(R.id.change);
            mChangeRecommendData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AppManager.getInstance().loadRandomApps(new ManagerCallback() {

                        @Override
                        public void onSuccess(String moduleType, int dataType, int page, int num,
                                boolean end) {
                            mRecommendData = DataPool.getInstance().getAppInfos(DataPool.TYPE_APP_RANDOM_APPS);
                            mRecommendAdapter.notifyDataSetChanged();

                        }

                        @Override
                        public void onFailure(String moduleType, int dataType, Throwable t,
                                int errorNo,
                                String strMsg) {
                        }
                    });
                }
            });

        }
        headview.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            }
        });
        return headview;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.zkas_download_update_fragment, container, false);
        HeadView = LayoutInflater.from(getActivity()).inflate(R.layout.zkas_download_autoinstill, null);
        mSettingautoins = (Button) HeadView.findViewById(R.id.button_settingautoins);
        mSettingautoins.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setAction(InstallService.ACCESSIBILITY_INTENT_ACTION);
                startActivity(i);
            }
        });
        mUpgradeList = (ListView) mView.findViewById(R.id.upgradeList);
        if (Properties.CHANNEL_IVVI.equals(AppStoreWrapperImpl.getInstance().getChannel())
                || Properties.CHANNEL_COOLMART.equals(AppStoreWrapperImpl.getInstance().getChannel())
                || Properties.CHANNEL_SHARP.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
            SettingsManager.mEnableAutoInstall = true;
        }
        if(SettingsManager.mEnableAutoInstall){
            if(mUpgradeList.getHeaderViewsCount()>0){
                mUpgradeList.removeHeaderView(HeadView);
                mUpdradeAdapter.notifyDataSetChanged();
            }
        }else{
            if(mUpgradeList.getHeaderViewsCount()<1){
                mUpgradeList.addHeaderView(HeadView);
                mUpdradeAdapter.notifyDataSetChanged();
            }
        }
        mUpgradeList.setAdapter(mUpdradeAdapter);
        mRecommendlist = (ListView) mView.findViewById(R.id.recommendlist);
        mNoTaskNoNetworkLayout = mView.findViewById(R.id.notask_nonetlayout);
        mChangeRecommendData = (TextView) mView.findViewById(R.id.findapp);
        mBottomLayout = mView.findViewById(R.id.otherapp_bottom);
        mAppText = (TextView) mView.findViewById(R.id.apptext);
        mAppFind = (TextView) mView.findViewById(R.id.findapp);
        mRecommendlist.addHeaderView(getHeadView());
        mRecommendlist.setAdapter(mRecommendAdapter);
        switchView();
        mIsDestroy = false;
        return mView;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainThreadHandler = new Handler(Looper.getMainLooper());

        mUpdradeAdapter = new UpdradeAdapter(getActivity());
        mRecommendAdapter = new RecommendAdapter(getActivity());
        
        mNeedUpgradeList = DataPool.getInstance().getAppInfos(DataPool.TYPE_APP_NEED_UPGRADE);

        AppManager.getInstance().loadRandomApps(new ManagerCallback() {

            @Override
            public void onSuccess(String moduleType, int dataType, int page, int num, boolean end) {
                mRecommendData = DataPool.getInstance().getAppInfos(DataPool.TYPE_APP_RANDOM_APPS);
                mRecommendAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String moduleType, int dataType, Throwable t, int errorNo,
                    String strMsg) {
            }
        });

        mObserver = new DataObserver() {
            @Override
            public void onChanged(final int type) {
                if(isDestroyed()){
                    return;
                }
                mMainThreadHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        if (type == DataPool.TYPE_APP_NEED_UPGRADE) {
                            if (mNeedUpgradeList != null) {
                                mNeedUpgradeList = DataPool.getInstance().getAppInfos(
                                DataPool.TYPE_APP_NEED_UPGRADE);
                                switchView(); 
                            }                 
                        }
                        if (type == DataPool.TYPE_APP_RANDOM_APPS) {
                            mRecommendData = DataPool.getInstance().getAppInfos(DataPool.TYPE_APP_RANDOM_APPS);
                            mRecommendAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }

        };
        DataPool.getInstance().registerDataObserver(mObserver);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("DownloadUpdateFragment");
        if (Properties.CHANNEL_IVVI.equals(AppStoreWrapperImpl.getInstance().getChannel())
                || Properties.CHANNEL_COOLMART.equals(AppStoreWrapperImpl.getInstance().getChannel())
                || Properties.CHANNEL_SHARP.equals(AppStoreWrapperImpl.getInstance().getChannel())) {
            SettingsManager.mEnableAutoInstall = true;
        }
        if(SettingsManager.mEnableAutoInstall){
            if(mUpgradeList.getHeaderViewsCount()>0){
                mUpgradeList.removeHeaderView(HeadView);
                mUpdradeAdapter.notifyDataSetChanged();
            }
        }if(!SettingsManager.mEnableAutoInstall){
            if(mUpgradeList.getHeaderViewsCount()<1){
                mUpgradeList.addHeaderView(HeadView);
                mUpdradeAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("DownloadUpdateFragment");
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        DataPool.getInstance().unregisterDataObserver(mObserver);
    }
    
    public class UpdradeAdapter extends ArrayListAdapter {

        private Context mContext;

        public UpdradeAdapter(Context context) {
            super(context);
            mContext = context;
        }

        @Override
        public int getCount() {
            if (mNeedUpgradeList != null && mNeedUpgradeList.size() != 0) {
                return mNeedUpgradeList.size();
            } else
                return 0;
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View mView;
         //   if (position == 0) {
              //  AppStoreSettingsItem mAppStoreSettingsItem = null;
             //   if (mAppStoreSettingsItem == null) {
                  //  mAppStoreSettingsItem = (AppStoreSettingsItem) LayoutInflater.from(
                           // getActivity()).inflate(R.layout.zkas_settings_item_layout, null);
               //     try {
                   //     mAppStoreSettingsItem.setItemNameAndSummary("自动安装应用", "安装应用时,省去繁琐点击");
                 //   } catch (Exception e) {
                   //     e.printStackTrace();
                 //   }
             //   }
               // mView = mAppStoreSettingsItem;
        //    }

          //  else if (position == 1) {
               // mView = new ListDividerItemView(getActivity());
       //     }
         //   else {
                ListAppItemView mListMainItemView = null;
                if (mListMainItemView == null) {
                    mListMainItemView = (ListAppItemView) LayoutInflater.from(getActivity()).inflate(R.layout.zkas_list_item_universal_layout, parent, false);
                    try {



                        mListMainItemView.setAppInfo(mNeedUpgradeList.get(position));
                    } catch (Exception e) {
                    }
                }
                mView = mListMainItemView;
                mListMainItemView.setFrom("升级");
          //  }
            return mView;
        }
    }

    public class RecommendAdapter extends ArrayListAdapter {

        private Context mContext;

        public RecommendAdapter(Context context) {
            super(context);
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListAppItemView mListMainItemView = (ListAppItemView) convertView;
            if (mListMainItemView == null) {
                mListMainItemView = (ListAppItemView) LayoutInflater.from(getActivity()).inflate(
                R.layout.zkas_list_item_universal_layout, parent, false);
            }
            mListMainItemView.setAppInfo(mRecommendData.get(position));
            mListMainItemView.setFrom("热门推荐");
            return mListMainItemView;
        }

        @Override
        public int getCount() {
            if (mRecommendData != null && mRecommendData.size() != 0) {
            return mRecommendData.size();
            }
            return 0;
        }
    }
    
    private void switchView() {
        int cntneedupgrade = mNeedUpgradeList != null ? mNeedUpgradeList.size() : 0;
        if (cntneedupgrade == 0) {
            if (Utils.isNetworkConnected()) {
                mUpgradeList.setVisibility(View.GONE);
                mRecommendlist.setVisibility(View.VISIBLE);
                mNoTaskNoNetworkLayout.setVisibility(View.GONE);
                mRecommendAdapter.notifyDataSetChanged();
            } else {
                mUpgradeList.setVisibility(View.GONE);
                mRecommendlist.setVisibility(View.GONE);
                mNoTaskNoNetworkLayout.setVisibility(View.VISIBLE);
            }
            mAppText.setText("寻找更多新应用吧");
            mAppFind.setText("发现应用");
            mAppFind.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });
        } else {
            mUpgradeList.setVisibility(View.VISIBLE);
            mUpdradeAdapter.notifyDataSetChanged();
            mRecommendlist.setVisibility(View.GONE);
            mNoTaskNoNetworkLayout.setVisibility(View.GONE);
            mAppText.setText("一共有" + cntneedupgrade + "个应用");
            mAppFind.setVisibility(View.INVISIBLE);
            mAppFind.setText(R.string.upgrade_all);
            mAppFind.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAppFind.setTextColor(getResources().getColor(R.color.as_tab_bar_tab_text_color_normal));
                    // 全部升级
                    for(int i =0;i<mNeedUpgradeList.size();i++){
                        AppManager.getInstance().startDownloadApp(mNeedUpgradeList.get(i));
                    }
                }
            });
        }
    }

}
