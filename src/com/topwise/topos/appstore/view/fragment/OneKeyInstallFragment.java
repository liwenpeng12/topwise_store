package com.topwise.topos.appstore.view.fragment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.data.AppInfo;
import com.topwise.topos.appstore.data.Label;
import com.topwise.topos.appstore.data.Module;
import com.topwise.topos.appstore.data.Rank;
import com.topwise.topos.appstore.manager.AppManager;
import com.topwise.topos.appstore.utils.Properties;
import com.topwise.topos.appstore.view.CheckListPopularItemView;
import com.topwise.topos.appstore.view.CheckPopularItemView.OnCheckedChangeListener;
import com.topwise.topos.appstore.view.ListMainItemView;
import com.topwise.topos.appstore.view.ListPopularItemView;
import com.umeng.analytics.MobclickAgent;

import android.app.ActionBar.LayoutParams;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

public class OneKeyInstallFragment extends MainListFragment {
    private OnCheckedChangeListener mOnCheckedChangeListener;
    ArrayList<AppInfo> mSelectedAppInfos = new ArrayList<AppInfo>();
    private TextView mSelectedAppInfoView;
    private Button mInstallAllBtn;
    private DecimalFormat mDF = new DecimalFormat("#.00");
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mOnCheckedChangeListener = new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(View buttonView, AppInfo appinfo, boolean isChecked) {
                boolean changed = false;
                if (isChecked) {
                    if (!mSelectedAppInfos.contains(appinfo)) {
                        mSelectedAppInfos.add(appinfo);
                        changed = true;
                    }
                } else {
                    if (mSelectedAppInfos.contains(appinfo)) {
                        mSelectedAppInfos.remove(appinfo);
                        changed = true;
                    }
                }
                
                if (changed) {
                    updateSelectedAppInfo();
                }
            }
        };
        
        setPageId(Properties.PAGE_ONE_KEY);
        onNoMore();
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    
    @Override
    protected ListAdapter getListAdapter() {
        if (mAdapter == null) {
            mAdapter = new OnKeyInstallAdapter();
        }
        
        super.getListAdapter();
        return mAdapter;
    }
    
    protected View getCustomContentView() {
        View listview = super.getCustomContentView();
        
        LinearLayout cview = new LinearLayout(getActivity());
        cview.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
        lp.weight = 1;
        cview.addView(listview, lp);
        
        View btnbar = mInflator.inflate(R.layout.zkas_onkey_install_btm_bar_layout, cview, false);
        mSelectedAppInfoView = (TextView) btnbar.findViewById(R.id.zkas_id_all_app_size_info);
        mInstallAllBtn = (Button) btnbar.findViewById(R.id.zkas_id_install_all_btn);
        mInstallAllBtn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                installAllSelectedApp();
            }
        });
        
        cview.addView(btnbar);
        
        return cview;
    }
    
    private void updateSelectedAppInfo() {
        int cnt = mSelectedAppInfos.size();
        double apkbytes = 0;
        int dlsize = 0;
        for (int i = 0; i < cnt; i++) {
            AppInfo info = mSelectedAppInfos.get(i);
            if (info.flag == AppInfo.FLAG_ONLINE || info.flag == AppInfo.FLAG_NEED_UPGRADE) {
                try {
                    double bytesize = Double.valueOf(info.size);
                    apkbytes += bytesize;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                
                dlsize++;
            } else if (info.flag == AppInfo.FLAG_DOWNLOADED) {
                dlsize++;
            }
        }
        
        String apksize = "";
        if (apkbytes > 0.1f) {
            if (apkbytes > ListMainItemView.GB_SIZE) {
                double s = apkbytes / ListMainItemView.GB_SIZE;
                apksize = mDF.format(s) + "TB";
            } else if (apkbytes > ListMainItemView.MB_SIZE) {
                double s = apkbytes / ListMainItemView.MB_SIZE;
                apksize =  mDF.format(s) + "GB";
            } else if (apkbytes > ListMainItemView.KB_SIZE) {
                double s = apkbytes / ListMainItemView.KB_SIZE;
                apksize =  mDF.format(s) + "MB";
            } else {
                apksize =  mDF.format(apkbytes) + "KB";
            }
        }
        
        String apksinfo = getString(R.string.as_all_apk_count, dlsize) + apksize;
        mSelectedAppInfoView.setText(apksinfo);
    }
    
    private void installAllSelectedApp() {
        int cnt = mSelectedAppInfos.size();
        for (int i = 0; i < cnt; i++) {
            AppInfo info = mSelectedAppInfos.get(i);
            if (info.flag == AppInfo.FLAG_DOWNLOADED) {
                AppManager.getInstance().startInstallApp(info, info.file);
            } else if (info.flag == AppInfo.FLAG_ONLINE || info.flag == AppInfo.FLAG_NEED_UPGRADE) {
                AppManager.getInstance().startDownloadApp(info);
            }
        }
    }
    
    protected void setDataToList() {
        Object datas = getDatas();
        if (datas != null) {
            ((OnKeyInstallAdapter)mAdapter).setModuleDatas((ArrayList<Module>)datas);
        }
    }
    
    @Override
    protected void onNetworkStateChanged(boolean networkavailiable) {
        super.onNetworkStateChanged(networkavailiable);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("OneKeyInstallFragment");
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("OneKeyInstallFragment");
    }
    
    @Override
    public void onAppItemClicked(AppInfo info) {
    }
    
    private class OnKeyInstallAdapter extends MainListAdapter {
        
        public void setModuleDatas(ArrayList<Module> datas) {
            initDataArray();
            mSelectedAppInfos.clear();
            
            if (datas == null || datas.size() == 0) {
                notifyDataSetChanged();
                return;
            }
            
            int size = datas.size();
            for (int i = 0; i < size; i++) {
                Module module = datas.get(i);
                if (module == null) {
                    continue;
                }
                
                if (module instanceof Label) {
                    if (i > 0) {
                        DataWapper s = new DataWapper();
                        s.mDataType = DataWapper.TYPE_DIVIDER_ITEM;
                        s.mRealData = null;
                        mDatas.add(s);
                    }
                    
                    Label l = (Label) module;
                    if (l.title != null && l.title.length() > 0) {
                        DataWapper t = new DataWapper();
                        t.mDataType = DataWapper.TYPE_TITLE_ITEM;
                        t.mRealData = l.title;
                        mDatas.add(t);
                    }
                    
                    if (l.apps != null && l.apps.size() > 0) {
                        int len = l.apps.size();
                        if (len > ListPopularItemView.MAX_APK_CNT) {
                            for (int j = 0; j < len; j += ListPopularItemView.MAX_APK_CNT) {
                                DataWapper a = new DataWapper();
                                a.mDataType = DataWapper.TYPE_CHECKABLE_RANK_ITEM;
                                int end = j + ListPopularItemView.MAX_APK_CNT;
                                if (end > len) {
                                    end = len;
                                }
                                a.mRealData = l.apps.subList(j, end);
                                mDatas.add(a);
                            }
                        } else {
                            DataWapper a = new DataWapper();
                            a.mDataType = DataWapper.TYPE_CHECKABLE_RANK_ITEM;
                            a.mRealData = l.apps;
                            mDatas.add(a);
                        }
                        
                        mSelectedAppInfos.addAll(l.apps);
                    }
                } else if (module instanceof Rank) {
                    if (i > 0) {
                        DataWapper s = new DataWapper();
                        s.mDataType = DataWapper.TYPE_DIVIDER_ITEM;
                        s.mRealData = null;
                        mDatas.add(s);
                    }

                    Rank r = (Rank) module;
                    if (r.title != null && r.title.length() > 0) {
                        DataWapper t = new DataWapper();
                        t.mDataType = DataWapper.TYPE_TITLE_ITEM;
                        t.mRealData = r.title;
                        mDatas.add(t);
                    }
                    
                    if (r.apps != null && r.apps.size() > 0) {
                        int len = r.apps.size();
                        if (len > ListPopularItemView.MAX_APK_CNT) {
                            for (int j = 0; j < len; j += ListPopularItemView.MAX_APK_CNT) {
                                DataWapper a = new DataWapper();
                                a.mDataType = DataWapper.TYPE_CHECKABLE_RANK_ITEM;
                                int end = j + ListPopularItemView.MAX_APK_CNT;
                                if (end > len) {
                                    end = len;
                                }
                                a.mRealData = r.apps.subList(j, end);
                                mDatas.add(a);
                            }
                        } else {
                            DataWapper a = new DataWapper();
                            a.mDataType = DataWapper.TYPE_CHECKABLE_RANK_ITEM;
                            a.mRealData = r.apps;
                            mDatas.add(a);
                        }
                        
                        mSelectedAppInfos.addAll(r.apps);
                    }
                } else if (module instanceof AppInfo) {
                    if (i > 0 && !(datas.get(i - 1) instanceof AppInfo)) {
                        DataWapper s = new DataWapper();
                        s.mDataType = DataWapper.TYPE_DIVIDER_ITEM;
                        s.mRealData = null;
                        mDatas.add(s);
                    }
                    
                    DataWapper a = new DataWapper();
                    a.mDataType = DataWapper.TYPE_CHECKABLE_RANK_ITEM;
                    ArrayList<AppInfo> array = new ArrayList<AppInfo>();
                    array.add((AppInfo) module);
                    a.mRealData = array;
                    mDatas.add(a);
                    mSelectedAppInfos.add((AppInfo) module);
                }
            }
            
            updateSelectedAppInfo();
            notifyDataSetChanged();
        }
        
        public void updateViewData(View v, int type, Object data) {
            if (data == null) {
                return;
            }
            
            if (type == DataWapper.TYPE_CHECKABLE_RANK_ITEM) {
                CheckListPopularItemView view = (CheckListPopularItemView) v;
                
                List<AppInfo> darray = (List<AppInfo>)data;
                List<Boolean> barray = new ArrayList<Boolean>(3);
                for (AppInfo af : darray) {
                    if (mSelectedAppInfos.contains(af)) {
                        barray.add(true);
                    } else {
                        barray.add(false);
                    }
                }
                
                view.setAppInfos(darray, barray);
                view.setOnCheckedChangeListener(mOnCheckedChangeListener);
                view.setOnAppItemClickListener(mAppItemClickListener);
                view.setFrom(mFrom);
            } else {
                super.updateViewData(v, type, data);
            }
        }
    }
}
