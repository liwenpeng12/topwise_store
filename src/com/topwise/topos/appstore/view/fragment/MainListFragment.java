package com.topwise.topos.appstore.view.fragment;

import java.util.ArrayList;
import java.util.List;

import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.conn.behavior.BehaviorEx;
import com.topwise.topos.appstore.conn.behavior.BehaviorLogManager;
import com.topwise.topos.appstore.data.AdIcon;
import com.topwise.topos.appstore.data.AdIconModule;
import com.topwise.topos.appstore.data.AppInfo;
import com.topwise.topos.appstore.data.Banner;
import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.data.LBannerModule;
import com.topwise.topos.appstore.data.Label;
import com.topwise.topos.appstore.data.Module;
import com.topwise.topos.appstore.data.Rank;
import com.topwise.topos.appstore.data.SBannerModule;
import com.topwise.topos.appstore.data.DataPool.DataObserver;
import com.topwise.topos.appstore.manager.ManagerCallback;
import com.topwise.topos.appstore.manager.NetworkManager;
import com.topwise.topos.appstore.manager.NetworkManager.NetworkListener;
import com.topwise.topos.appstore.manager.PageModuleManager;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.view.ListAdIconsView;
import com.topwise.topos.appstore.view.ListAdIconsView.AdIconClickListener;
import com.topwise.topos.appstore.view.ListLableTitleView;
import com.topwise.topos.appstore.view.ListMainItemView;
import com.topwise.topos.appstore.view.ListMainItemView.AppItemClickListener;
import com.topwise.topos.appstore.view.ListMoreItemView;
import com.topwise.topos.appstore.view.ListMoreItemView.MoreItemClickListener;
import com.topwise.topos.appstore.view.CheckListPopularItemView;
import com.topwise.topos.appstore.view.ListPopularItemView;
import com.topwise.topos.appstore.view.ListScrollBannerView;
import com.topwise.topos.appstore.view.ListSingleBannerView;
import com.topwise.topos.appstore.view.ListTopAdItemView;
import com.topwise.topos.appstore.view.ListTopAdItemView.TopAdItemClickListener;
import com.topwise.topos.appstore.view.ListTwoBannerView;
import com.topwise.topos.appstore.view.WaitingView;
import com.topwise.topos.appstore.view.ListScrollBannerView.BannerClickListener;
import com.topwise.topos.appstore.view.WaitingView.RefrushClickListener;
import com.topwise.topos.appstore.view.activity.AppDetailActivity;
import com.topwise.topos.appstore.view.activity.GroupActivity;
import com.topwise.topos.appstore.view.activity.H5Activity;
import com.topwise.topos.appstore.view.activity.SortActivity;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class MainListFragment extends BaseListLoadingFragment
        implements AppItemClickListener, AdIconClickListener, BannerClickListener,
        MoreItemClickListener, TopAdItemClickListener {

    private static final String TAG = "MainListBaseFragment";

    private ViewGroup mFragmentView;

    protected BaseAdapter mAdapter;
    protected ManagerCallback mManagerCallback;
    protected DataObserver mDataObserver;
    protected RefrushClickListener mRefushBtnListener;
    protected NetworkListener mNetworkListener;
    protected boolean mFirstLoading = false;
    protected String mFrom;

    protected int mDataType = -1;
    protected int mPageId = -1;

    public MainListFragment() {
        mDataObserver = new DataObserver() {

            @Override
            public void onChanged(int type) {
                if (isDestroyed()) {
                    return;
                }

                if (type == getDataType()) {
                    setDataToList();
                }
            }
        };

        mRefushBtnListener = new RefrushClickListener() {

            @Override
            public void onRefushClicked(WaitingView v) {
                if (isDestroyed()) {
                    return;
                }
                showWaitViewProgress(R.string.as_list_loading_prompt);
                mFirstLoading = true;

                loadData();
            }
        };

        mManagerCallback = new ManagerCallback() {

            @Override
            public void onSuccess(String moduleType, int dataType, int page, int num, boolean end) {
                if (mDataType == -1) {
                    mDataType = dataType;
                    DataPool.getInstance().registerDataObserver(mDataObserver);
                }

                mFirstLoading = false;
                hideWaitView();
                setDataToList();

                if (end) {
                    onNoMore();
                } else {
                    onGetMoreCompleted();
                }
            }

            @Override
            public void onFailure(String moduleType, int dataType, Throwable t, int errorNo, String strMsg) {
                if (isDestroyed()) {
                    return;
                }

                if (mFirstLoading) {
                    if (setLastDataToList(dataType)) {
                        hideWaitView();
                        return;
                    }

                    showWaitViewRefushBtn(R.string.as_list_load_failed_prompt, R.string.as_refresh_btn_again, mRefushBtnListener);
                } else {
                    onGetMoreFailed();
                }
            }
        };

        mNetworkListener = new NetworkListener() {

            @Override
            public void onNetworkDisconnected() {
                if (isDestroyed()) {
                    return;
                }

//                Toast.makeText(getActivity(), R.string.as_network_unavailable, Toast.LENGTH_SHORT).show();
                onNetworkStateChanged(false);
            }

            @Override
            public void onNetworkConnected() {
                if (isDestroyed()) {
                    return;
                }

                if (mFirstLoading) {
                    showWaitViewProgress(R.string.as_list_loading_prompt);
                    loadData();
                } else {
                    onNetworkStateChanged(true);
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFragmentView = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
        if(!EventBus.getDefault().isRegistered(this))//避免Evenbus注册重复导致报停
        EventBus.getDefault().register(this);
        loadData();
        mFirstLoading = true;
        showWaitViewProgress(R.string.as_list_loading_prompt);
        NetworkManager.getInstance().registerNetworkListener(mNetworkListener);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(mConnectionReceiver, intentFilter);

        return mFragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
//        MobclickAgent.onPageStart("MainListFragment");
    }

    @Override
    public void onPause() {
        super.onPause();
   //     MobclickAgent.onPageEnd("MainListFragment");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        try {
            getActivity().unregisterReceiver(mConnectionReceiver);
        } catch (Exception e) {
        }
    }

    @Subscribe
    public void onEvent(String s) {
        switch (s) {
            case "EVEN_REFRESH_LIST_DATA":
                setDataToList();
                break;
        }
    }

    private BroadcastReceiver mConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE);
            NetworkInfo mobNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mobNetInfo.isConnected() || wifiNetInfo.isConnected()) {
                loadData();
            }
        }
    };

    public void setPageId(int id) {
        mPageId = id;
    }

    protected void loadData() {
        if (mPageId != -1) {
            PageModuleManager.getInstance().loadPage(mPageId, mManagerCallback);
        } else {
            LogEx.e(TAG, "Pageid is -1!");
        }
    }


    protected int getDataType() {
        return mDataType;
    }

    protected Object getDatas() {
        return DataPool.getInstance().getModules(getDataType());
    }

    protected void setDataToList() {
        Object datas = getDatas();
        if (datas != null) {
            ((MainListAdapter) mAdapter).setModuleDatas((ArrayList<Module>) datas);
        }
    }

    protected boolean setLastDataToList(int dataType) {
        if (dataType == -1) {
            return false;
        }
        Object datas = DataPool.getInstance().getModules(dataType);
        if (datas != null) {
            ((MainListAdapter) mAdapter).setModuleDatas((ArrayList<Module>) datas);
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        DataPool.getInstance().unregisterDataObserver(mDataObserver);
        ((MainListAdapter) mAdapter).onDestroy();
        NetworkManager.getInstance().unregisterNetworkListener(mNetworkListener);
    }

    @Override
    protected ListAdapter getListAdapter() {
        if (mAdapter == null) {
            mAdapter = new MainListAdapter();
        }

        ((MainListAdapter) mAdapter).setFrom(mFrom);
        ((MainListAdapter) mAdapter).setOnAdIconClickListener(this);
        ((MainListAdapter) mAdapter).setOnAppItemClickListener(this);
        ((MainListAdapter) mAdapter).setOnBannerClickListener(this);
        ((MainListAdapter) mAdapter).setOnMoreItemClickListener(this);
        ((MainListAdapter) mAdapter).setOnTopAdItemClickListener(this);

        return mAdapter;
    }

    @Override
    public void getMoreForList() {
        loadData();
    }

    @Override
    public void onAppItemClicked(AppInfo info) {
        BehaviorLogManager.getInstance().addBehaviorEx(new BehaviorEx(BehaviorEx.ENTER_APP_DETAIL, info.id));
        Intent intent = new Intent(getActivity(), AppDetailActivity.class);
        intent.putExtra("app_id", info.id);
        startActivity(intent);
    }

    @Override
    public void onAdIconClicked(AdIcon icon) {
        BehaviorLogManager.getInstance().addBehaviorEx(
                new BehaviorEx(BehaviorEx.ENTER_ADICON, icon.id));

        if ("type".equals(icon.target)
                || "label".equals(icon.target)
                || "rank".equals(icon.target)) {
            Intent intent = new Intent(getActivity(), GroupActivity.class);
            intent.putExtra("title", icon.title);
            intent.putExtra("type", icon.target);

            try {
                int[] ids = new int[1];
                ids[0] = Integer.valueOf(icon.target_url);
                intent.putExtra("id", ids);
            } catch (Exception e) {
                LogEx.e(TAG, "onAdIconClicked(), target is" + icon.target + ", icon.target_url="
                        + icon.target_url + ", exception is " + e);
            }

            startActivity(intent);
        } else if ("app".equals(icon.target)) {
            Intent intent = new Intent(getActivity(), AppDetailActivity.class);
            intent.putExtra("app_id", icon.target_url);
            startActivity(intent);
        } else if ("h5".equals(icon.target)) {
            Intent intent = new Intent(getActivity(), H5Activity.class);
            intent.putExtra("url", icon.target_url);
            intent.putExtra("title", icon.title);
            startActivity(intent);
        } else if ("page".equals(icon.target)) {
            Intent intent = new Intent(getActivity(), GroupActivity.class);
            intent.putExtra("title", icon.title);
            intent.putExtra("type", icon.target);

            try {
                String[] idstring = icon.target_url.trim().split(";");
                if (idstring != null && idstring.length > 0) {
                    int[] ids = new int[idstring.length];
                    for (int i = 0; i < idstring.length; i++) {
                        ids[i] = Integer.valueOf(idstring[i]);
                    }
                    intent.putExtra("id", ids);
                }
            } catch (Exception e) {
                LogEx.e(TAG, "onAdIconClicked(), target is page, target_url=" + icon.target_url
                        + ", exception is " + e);
            }

            try {
                if (icon.target_name != null && icon.target_name.length() > 0) {
                    String[] tabnames = icon.target_name.trim().split(";");
                    if (tabnames != null && tabnames.length > 0) {
                        intent.putExtra("tabname", tabnames);
                    }
                }
            } catch (Exception e) {
                LogEx.e(TAG, "onAdIconClicked(), target is page, target_name=" + icon.target_name
                        + ", exception is " + e);
            }

            startActivity(intent);
        } else if ("category".equals(icon.target)) {
            Intent intent = new Intent(getActivity(), SortActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBannerClicked(Banner banner) {
        dealBannerClicked(getActivity(), banner, mFragmentView);
    }

    public static void dealBannerClicked(final Context context, final Banner banner, ViewGroup viewGroup) {
        BehaviorLogManager.getInstance().addBehaviorEx(
                new BehaviorEx(BehaviorEx.ENTER_BANNER, banner.id));

        if ("type".equals(banner.target)
                || "label".equals(banner.target)
                || "rank".equals(banner.target)) {
            Intent intent = new Intent(context, GroupActivity.class);
            intent.putExtra("title", banner.title);
            intent.putExtra("type", banner.target);

            try {
                int[] ids = new int[1];
                ids[0] = Integer.valueOf(banner.target_url);
                intent.putExtra("id", ids);
            } catch (Exception e) {
                LogEx.e(TAG, "onBannerClicked(), target is" + banner.target
                        + ", banner.target_url="
                        + banner.target_url + ", exception is " + e);
            }

            context.startActivity(intent);
        } else if ("app".equals(banner.target)) {
            Intent intent = new Intent(context, AppDetailActivity.class);
            intent.putExtra("app_id", banner.target_url);
            context.startActivity(intent);
        } else if ("h5".equals(banner.target)) {
            Intent intent = new Intent(context, H5Activity.class);
            intent.putExtra("url", banner.target_url);
            intent.putExtra("title", banner.title);
            context.startActivity(intent);
        } else if ("page".equals(banner.target)) {
            Intent intent = new Intent(context, GroupActivity.class);
            intent.putExtra("title", banner.title);
            intent.putExtra("type", banner.target);

            try {
                String[] idstring = banner.target_url.trim().split(";");
                if (idstring != null && idstring.length > 0) {
                    int[] ids = new int[idstring.length];
                    for (int i = 0; i < idstring.length; i++) {
                        ids[i] = Integer.valueOf(idstring[i]);
                    }
                    intent.putExtra("id", ids);
                }
            } catch (Exception e) {
                LogEx.e(TAG, "onBannerClicked(), target is page, target_url=" + banner.target_url
                        + ", exception is " + e);
            }

            try {
                if (banner.target_name != null && banner.target_name.length() > 0) {
                    String[] tabnames = banner.target_name.trim().split(";");
                    if (tabnames != null && tabnames.length > 0) {
                        intent.putExtra("tabname", tabnames);
                    }
                }
            } catch (Exception e) {
                LogEx.e(TAG, "onBannerClicked(), target is page, target_name=" + banner.target_name
                        + ", exception is " + e);
            }

            context.startActivity(intent);
        } else if ("YingPu".equals(banner.target)) {
//            com.moviebook.ivvi.sdk.YpSdk mSdk = com.moviebook.ivvi.sdk.YpSdk.getSdk();
//            mSdk.initLayout(context, viewGroup);
//            mSdk.openAd(banner.id);
//            com.moviebook.ivvi.sdk.YpSdk.adlistener = new com.moviebook.ivvi.sdk.AdListener() {
//                @Override
//                public void AdClose() {
//                    Intent intent = new Intent(context, AppDetailActivity.class);
//                    intent.putExtra("app_id", banner.target_url);
//                    context.startActivity(intent);
//                }
//            };
        }
    }

    public void setFrom(String from) {
        mFrom = from;
        if (mAdapter != null) {
            ((MainListAdapter) mAdapter).setFrom(from);
        }
    }

    //每一项的更多
    @Override
    public void onMoreItemClicked(String type, String id, String title) {
        if (type.equals("label") || type.equals("rank")) {
            Intent intent = new Intent(getActivity(), GroupActivity.class);
            intent.putExtra("title", title);
            intent.putExtra("type", type);

            try {
                int[] ids = new int[1];
                ids[0] = Integer.valueOf(id);
                intent.putExtra("id", ids);
            } catch (Exception e) {
                LogEx.e(TAG, "onMoreItemClicked(), type is" + type + ", id="
                        + id + ", exception is " + e);
            }

            startActivity(intent);
        }
    }

    @Override
    public void onTopAdItemClicked(String url, String title) {
        if (url != null && url.length() > 0) {
            Intent intent = new Intent(getActivity(), H5Activity.class);
            intent.putExtra("url", url);
            intent.putExtra("title", title);
            startActivity(intent);
        }
    }

    public static class MainListAdapter extends BaseAdapter {

        public class DataWapper {
            public static final int TYPE_SCROLL_BANNER = 0;
            public static final int TYPE_TWO_BANNER = 1;
            public static final int TYPE_SINGLE_BANNER = 2;
            public static final int TYPE_AD_ICONS = 3;
            public static final int TYPE_APP_ITEM = 4;
            public static final int TYPE_RANK_ITEM = 5;
            public static final int TYPE_TITLE_ITEM = 6;
            public static final int TYPE_MORE_ITEM = 7;
            public static final int TYPE_DIVIDER_ITEM = 8;
            public static final int TYPE_TOP_AD_ITEM = 9;
            public static final int TYPE_CHECKABLE_RANK_ITEM = 10;

            public static final int ITEM_TYPE_CNT = 11;

            public Object mRealData;
            public int mDataType;
        }

        protected ArrayList<DataWapper> mDatas;
        protected AppItemClickListener mAppItemClickListener;
        protected BannerClickListener mBannerClickListener;
        protected AdIconClickListener mAdIconClickListener;
        protected MoreItemClickListener mMoreItemClickListener;
        protected TopAdItemClickListener mTopAdItemClickListener;
        protected String mFrom;

        public void onDestroy() {
            if (mDatas != null) {
                mDatas.clear();
                mDatas = null;
            }
        }

        public void setFrom(String from) {
            mFrom = from;
        }

        public void setModuleDatas(ArrayList<Module> datas) {
            initDataArray();

            if (datas == null || datas.size() == 0) {
                notifyDataSetChanged();
                return;
            }

            boolean isbusinessinit = false;
            int size = datas.size();
            for (int i = 0; i < size; i++) {
                Module module = datas.get(i);
                if (module == null) {
                    continue;
                }

                if (module instanceof LBannerModule) {//大Banner
                    LBannerModule m = (LBannerModule) module;
                    if (m.banners != null) {
                        DataWapper data = new DataWapper();

                        if (m.banners.size() == 1) {
                            if (!isbusinessinit) {
                                DataWapper s = new DataWapper();
                                s.mDataType = DataWapper.TYPE_DIVIDER_ITEM;
                                s.mRealData = null;
                                mDatas.add(s);
                            }

                            data.mDataType = DataWapper.TYPE_SINGLE_BANNER;
                            data.mRealData = m.banners.get(0);
                        } else {
                            data.mDataType = DataWapper.TYPE_SCROLL_BANNER;
                            data.mRealData = m.banners;
                            isbusinessinit = true;
                        }
                        mDatas.add(data);
                    }
                } else if (module instanceof SBannerModule) {//小banner
                    SBannerModule m = (SBannerModule) module;
                    if (m.banners != null && m.banners.size() > 0) {

                        if (!isbusinessinit) {
                            DataWapper s = new DataWapper();
                            s.mDataType = DataWapper.TYPE_DIVIDER_ITEM;
                            s.mRealData = null;
                            mDatas.add(s);
                        }

                        DataWapper data = new DataWapper();
                        data.mRealData = m.banners;
                        data.mDataType = DataWapper.TYPE_TWO_BANNER;
                        mDatas.add(data);
                    }
                } else if (module instanceof AdIconModule) {//圆形按钮
                    AdIconModule m = (AdIconModule) module;
                    if (m.adicons != null && m.adicons.size() > 0) {
                        if (!isbusinessinit) {
                            DataWapper s = new DataWapper();
                            s.mDataType = DataWapper.TYPE_DIVIDER_ITEM;
                            s.mRealData = null;
                            mDatas.add(s);
                        }

                        DataWapper data = new DataWapper();
                        data.mRealData = m.adicons;
                        data.mDataType = DataWapper.TYPE_AD_ICONS;
                        mDatas.add(data);
                    }
                } else if (module instanceof Label) {//文字描述
                    if (i > 0) {
                        DataWapper s = new DataWapper();
                        s.mDataType = DataWapper.TYPE_DIVIDER_ITEM;
                        s.mRealData = null;
                        mDatas.add(s);
                    }

                    isbusinessinit = false;

                    Label l = (Label) module;
                    if (l.title != null && l.title.length() > 0) {
                        DataWapper t = new DataWapper();
                        t.mDataType = DataWapper.TYPE_TITLE_ITEM;
                        t.mRealData = l.title;
                        mDatas.add(t);
                    }

                    if (l.apps != null && l.apps.size() > 0) {
                        int len = l.apps.size();
                        for (int j = 0; j < len; j++) {
                            AppInfo info = l.apps.get(j);
                            if (info != null) {
                                DataWapper a = new DataWapper();
                                a.mDataType = DataWapper.TYPE_APP_ITEM;
                                a.mRealData = info;
                                mDatas.add(a);
                            }
                        }
                    }

                    if (l.id != null && l.id.length() > 0) {
                        DataWapper a = new DataWapper();
                        a.mDataType = DataWapper.TYPE_MORE_ITEM;
                        a.mRealData = l;
                        mDatas.add(a);
                    }

                } else if (module instanceof AppInfo) {//应用信息
                    if (i > 0 && !(datas.get(i - 1) instanceof AppInfo)) {
                        DataWapper s = new DataWapper();
                        s.mDataType = DataWapper.TYPE_DIVIDER_ITEM;
                        s.mRealData = null;
                        mDatas.add(s);
                    }

                    isbusinessinit = false;

                    DataWapper a = new DataWapper();
                    a.mDataType = DataWapper.TYPE_APP_ITEM;
                    a.mRealData = module;
                    mDatas.add(a);
                } else if (module instanceof Rank) {//一个应用类
                    if (i > 0) {
                        DataWapper s = new DataWapper();
                        s.mDataType = DataWapper.TYPE_DIVIDER_ITEM;
                        s.mRealData = null;
                        mDatas.add(s);
                    }

                    isbusinessinit = false;

                    Rank r = (Rank) module;
                    if (r.title != null && r.title.length() > 0) {
                        DataWapper t = new DataWapper();
                        t.mDataType = DataWapper.TYPE_TITLE_ITEM;
                        t.mRealData = r.title;
                        mDatas.add(t);
                    }

                    if (r.apps != null && r.apps.size() > 0) {
                        DataWapper t = new DataWapper();
                        t.mDataType = DataWapper.TYPE_RANK_ITEM;
                        t.mRealData = r.apps;
                        mDatas.add(t);

                        if (r.apps.size() > ListPopularItemView.MAX_APK_CNT) {
                            for (int j = ListPopularItemView.MAX_APK_CNT; j < r.apps.size(); j++) {
                                AppInfo info = r.apps.get(j);
                                if (info != null) {
                                    DataWapper a = new DataWapper();
                                    a.mDataType = DataWapper.TYPE_APP_ITEM;
                                    a.mRealData = info;
                                    mDatas.add(a);
                                }
                            }
                        }
                    }

                    if (r.id != null && r.id.length() > 0) {
                        DataWapper a = new DataWapper();
                        a.mDataType = DataWapper.TYPE_MORE_ITEM;
                        a.mRealData = r;
                        mDatas.add(a);
                    }
                }
            }

            notifyDataSetChanged();
        }

        public void setLabelData(Label label) {
            initDataArray();

            if (label == null) {
                notifyDataSetChanged();
                return;
            }

            if (label.img_url != null && label.img_url.length() > 0) {
                DataWapper a = new DataWapper();
                a.mDataType = DataWapper.TYPE_TOP_AD_ITEM;
                a.mRealData = label;
                mDatas.add(a);
            }

            if (label.apps != null) {
                for (AppInfo info : label.apps) {
                    if (info != null) {
                        DataWapper a = new DataWapper();
                        a.mDataType = DataWapper.TYPE_APP_ITEM;
                        a.mRealData = info;
                        mDatas.add(a);
                    }
                }
            }

            notifyDataSetChanged();
        }

        public void setLabelDatas(ArrayList<Label> labels) {
            initDataArray();

            if (labels == null || labels.size() == 0) {
                notifyDataSetChanged();
                return;
            }

            if (labels.size() == 1) {
                setLabelData(labels.get(0));
                return;
            }

            for (Label l : labels) {
                if (l.title != null && l.title.length() > 0) {
                    DataWapper t = new DataWapper();
                    t.mDataType = DataWapper.TYPE_TITLE_ITEM;
                    t.mRealData = l.title;
                    mDatas.add(t);
                }

                if (l.apps != null && l.apps.size() > 0) {
                    int len = l.apps.size();
                    for (int j = 0; j < len; j++) {
                        AppInfo info = l.apps.get(j);
                        if (info != null) {
                            DataWapper a = new DataWapper();
                            a.mDataType = DataWapper.TYPE_APP_ITEM;
                            a.mRealData = info;
                            mDatas.add(a);
                        }
                    }
                }

                DataWapper s = new DataWapper();
                s.mDataType = DataWapper.TYPE_DIVIDER_ITEM;
                s.mRealData = null;
                mDatas.add(s);
            }
        }

        public void setRankData(Rank rank) {
            initDataArray();

            if (rank == null) {
                notifyDataSetChanged();
                return;
            }

            if (rank.img_url != null && rank.img_url.length() > 0) {
                DataWapper a = new DataWapper();
                a.mDataType = DataWapper.TYPE_TOP_AD_ITEM;
                a.mRealData = rank;
                mDatas.add(a);
            }

            if (rank.apps != null) {
                DataWapper t = new DataWapper();
                t.mDataType = DataWapper.TYPE_RANK_ITEM;
                t.mRealData = rank.apps;
                mDatas.add(t);

                if (rank.apps.size() > ListPopularItemView.MAX_APK_CNT) {
                    for (int j = ListPopularItemView.MAX_APK_CNT; j < rank.apps.size(); j++) {
                        AppInfo info = rank.apps.get(j);
                        if (info != null) {
                            DataWapper a = new DataWapper();
                            a.mDataType = DataWapper.TYPE_APP_ITEM;
                            a.mRealData = info;
                            mDatas.add(a);
                        }
                    }
                }
            }

            notifyDataSetChanged();
        }

        public void setRankDatas(ArrayList<Rank> datas) {
            initDataArray();

            if (datas == null || datas.size() == 0) {
                notifyDataSetChanged();
                return;
            }

            if (datas.size() == 1) {
                setRankData(datas.get(0));
                return;
            }

            for (Rank rank : datas) {
                Rank r = rank;
                if (r.title != null && r.title.length() > 0) {
                    DataWapper t = new DataWapper();
                    t.mDataType = DataWapper.TYPE_TITLE_ITEM;
                    t.mRealData = r.title;
                    mDatas.add(t);
                }

                if (r.apps != null && r.apps.size() > 0) {
                    DataWapper t = new DataWapper();
                    t.mDataType = DataWapper.TYPE_RANK_ITEM;
                    t.mRealData = r.apps;
                    mDatas.add(t);

                    if (r.apps.size() > ListPopularItemView.MAX_APK_CNT) {
                        for (int j = ListPopularItemView.MAX_APK_CNT; j < r.apps.size(); j++) {
                            AppInfo info = r.apps.get(j);
                            if (info != null) {
                                DataWapper a = new DataWapper();
                                a.mDataType = DataWapper.TYPE_APP_ITEM;
                                a.mRealData = info;
                                mDatas.add(a);
                            }
                        }
                    }
                }

                DataWapper s = new DataWapper();
                s.mDataType = DataWapper.TYPE_DIVIDER_ITEM;
                s.mRealData = null;
                mDatas.add(s);
            }

            notifyDataSetChanged();
        }

        public void setAppDatas(ArrayList<AppInfo> infos) {
            initDataArray();

            if (infos == null || infos.size() == 0) {
                notifyDataSetChanged();
                return;
            }

            for (AppInfo info : infos) {
                if (info != null) {
                    DataWapper a = new DataWapper();
                    a.mDataType = DataWapper.TYPE_APP_ITEM;
                    a.mRealData = info;
                    mDatas.add(a);
                }
            }

            notifyDataSetChanged();
        }

        public void setOnAppItemClickListener(AppItemClickListener l) {
            mAppItemClickListener = l;
        }

        public void setOnBannerClickListener(BannerClickListener l) {
            mBannerClickListener = l;
        }

        public void setOnAdIconClickListener(AdIconClickListener l) {
            mAdIconClickListener = l;
        }

        public void setOnMoreItemClickListener(MoreItemClickListener l) {
            mMoreItemClickListener = l;
        }

        public void setOnTopAdItemClickListener(TopAdItemClickListener l) {
            mTopAdItemClickListener = l;
        }

        protected void initDataArray() {
            if (mDatas == null) {
                mDatas = new ArrayList<DataWapper>();
            } else {
                mDatas.clear();
            }
        }

        @Override
        public int getCount() {
            if (mDatas == null) {
                return 0;
            }
            return mDatas.size();
        }

        @Override
        public Object getItem(int position) {
            if (mDatas == null || position >= mDatas.size()) {
                return null;
            }
            return mDatas.get(position).mRealData;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int type = getItemViewType(position);
            if (convertView == null) {
                convertView = createViewByType(type, parent);
            }

            updateViewData(convertView, type, getItem(position));
            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public int getViewTypeCount() {
            return DataWapper.ITEM_TYPE_CNT;
        }

        @Override
        public int getItemViewType(int position) {
            if (mDatas == null || position >= mDatas.size()) {
                return -1;
            }
            return mDatas.get(position).mDataType;
        }

        public View createViewByType(int type, ViewGroup parent) {
            Context context = parent.getContext();
            int paddingbtm = parent.getResources().getDimensionPixelSize(R.dimen.as_list_group_center_margin);
            int paddinghorz = parent.getResources().getDimensionPixelSize(R.dimen.zkas_list_item_horizontal_padding);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            View v = null;
            switch (type) {
                case DataWapper.TYPE_SCROLL_BANNER:
                    v = new ListScrollBannerView(context);
                    v.setPadding(0, 0, 0, paddingbtm);
                    break;
                case DataWapper.TYPE_TWO_BANNER:
                    v = new ListTwoBannerView(context);
                    v.setPadding(paddinghorz, 0, paddinghorz, paddingbtm);
                    break;
                case DataWapper.TYPE_SINGLE_BANNER:
                    v = new ListSingleBannerView(context);
                    break;
                case DataWapper.TYPE_AD_ICONS:
                    v = new ListAdIconsView(context);
                    v.setPadding(0, 0, 0, paddingbtm);
                    break;
                case DataWapper.TYPE_TITLE_ITEM:
                    v = new ListLableTitleView(context);
                    break;
                case DataWapper.TYPE_APP_ITEM:
                    v = inflater.inflate(R.layout.zkas_list_item_mulit_line_layout, parent, false);
                    break;
                case DataWapper.TYPE_MORE_ITEM:
                    v = new ListMoreItemView(context);
                    break;
                case DataWapper.TYPE_DIVIDER_ITEM:
                    //v = new ListDividerItemView(context);
                    v = inflater.inflate(R.layout.zkas_list_item_interval, parent, false);
                    break;
                case DataWapper.TYPE_RANK_ITEM:
                    v = inflater.inflate(R.layout.zkas_list_item_rank_layout, parent, false);
                    break;
                case DataWapper.TYPE_TOP_AD_ITEM:
                    v = inflater.inflate(R.layout.zkas_list_item_ad_view_layout, parent, false);
                    break;
                case DataWapper.TYPE_CHECKABLE_RANK_ITEM:
                    v = inflater.inflate(R.layout.zkas_list_item_checkable_populars_layout, parent, false);
                    break;
            }

            if (v != null) {
                AbsListView.LayoutParams lp = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                v.setLayoutParams(lp);
            }

            return v;
        }

        public void updateViewData(View v, int type, Object data) {
            if (data == null) {
                return;
            }

            switch (type) {
                case DataWapper.TYPE_SCROLL_BANNER: {
                    ListScrollBannerView view = (ListScrollBannerView) v;
                    ArrayList<Banner> bs = (ArrayList<Banner>) data;
                    view.setBanners(bs);
                    view.setBannerClickListener(mBannerClickListener);
                }
                break;
                case DataWapper.TYPE_TWO_BANNER: {
                    ListTwoBannerView view = (ListTwoBannerView) v;
                    try {
                        ArrayList<Banner> bs = (ArrayList<Banner>) data;
                        Banner lb = bs.get(0);
                        if (lb != null) {
                            view.setLeftBanner(lb);
                            BehaviorLogManager.getInstance().addBehaviorEx(
                                    new BehaviorEx(BehaviorEx.VIEW_BANNER, lb.id));
                        } else {
                            return;
                        }

                        Banner rb = bs.get(1);
                        if (rb != null) {
                            view.setRightBanner(rb);
                            BehaviorLogManager.getInstance().addBehaviorEx(
                                    new BehaviorEx(BehaviorEx.VIEW_BANNER, rb.id));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    view.setBannerClickListener(mBannerClickListener);
                }
                break;
                case DataWapper.TYPE_SINGLE_BANNER: {
                    ListSingleBannerView view = (ListSingleBannerView) v;
                    view.setBanner((Banner) data);
                    view.setBannerClickListener(mBannerClickListener);
                    BehaviorLogManager.getInstance().addBehaviorEx(
                            new BehaviorEx(BehaviorEx.VIEW_BANNER, ((Banner) data).id));
                }
                break;
                case DataWapper.TYPE_AD_ICONS: {
                    ListAdIconsView view = (ListAdIconsView) v;
                    ArrayList<AdIcon> icons = (ArrayList<AdIcon>) data;
                    view.setAdIcons(icons);
                    view.setOnAdIconClickListener(mAdIconClickListener);
                    for (AdIcon icon : icons) {
                        BehaviorLogManager.getInstance().addBehaviorEx(
                                new BehaviorEx(BehaviorEx.VIEW_ADICON, icon.id));
                    }
                }
                break;
                case DataWapper.TYPE_TITLE_ITEM: {
                    ListLableTitleView view = (ListLableTitleView) v;
                    view.setText((String) data);
                }
                break;
                case DataWapper.TYPE_APP_ITEM: {
                    ListMainItemView view = (ListMainItemView) v;
                    view.setAppInfo((AppInfo) data);
                    view.setOnAppItemClickListener(mAppItemClickListener);
                    view.setFrom(mFrom);
                }
                break;
                case DataWapper.TYPE_MORE_ITEM: {
                    ListMoreItemView view = (ListMoreItemView) v;

                    if (data instanceof Label) {
                        view.setGroupId("label", ((Label) data).id, ((Label) data).title);
                    } else if (data instanceof Rank) {
                        view.setGroupId("rank", ((Rank) data).id, ((Rank) data).title);
                    }

                    view.setOnMoreItemClickListener(mMoreItemClickListener);
                }
                break;
                case DataWapper.TYPE_DIVIDER_ITEM: {
                }
                break;
                case DataWapper.TYPE_RANK_ITEM: {
                    ListPopularItemView view = (ListPopularItemView) v;
                    view.setAppInfos((ArrayList<AppInfo>) data);
                    view.setOnAppItemClickListener(mAppItemClickListener);
                    view.setFrom(mFrom);
                }
                break;
                case DataWapper.TYPE_TOP_AD_ITEM: {
                    ListTopAdItemView view = (ListTopAdItemView) v;
                    view.setData(data);
                    view.setTopAdItemClickListener(mTopAdItemClickListener);
                }
                break;
                case DataWapper.TYPE_CHECKABLE_RANK_ITEM: {
                    CheckListPopularItemView view = (CheckListPopularItemView) v;
                    view.setAppInfos((List<AppInfo>) data);
                    view.setOnAppItemClickListener(mAppItemClickListener);
                    view.setFrom(mFrom);
                }
                break;
            }
        }
    }
}
