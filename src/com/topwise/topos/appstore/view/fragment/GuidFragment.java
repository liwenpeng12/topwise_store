package com.topwise.topos.appstore.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.data.AppInfo;
import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.data.Rank;
import com.topwise.topos.appstore.download.DownloadInfo;
import com.topwise.topos.appstore.download.DownloadManager;
import com.topwise.topos.appstore.manager.AppManager;
import com.topwise.topos.appstore.manager.ManagerCallback;
import com.topwise.topos.appstore.view.fragment.GuidPageInfo.GridViewAdapter;
import com.topwise.topos.appstore.view.fragment.GuidPageInfo.Model;
import com.topwise.topos.appstore.view.fragment.GuidPageInfo.ViewPagerAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangrui on 18-1-16.
 */

public class GuidFragment extends BaseFragment {
    ViewPager mPager;
    LinearLayout mLlDot;
    private GridViewAdapter mGridadapter;
    private ViewPagerAdapter mViewPageradapter;
//    private String[] titles = {"美食", "电影", "酒店住宿", "休闲娱乐", "外卖", "自助餐", "KTV", "机票/火车票", "周边游", "美甲美睫",
//            "火锅", "生日蛋糕"};
    private List<View> mPagerList;
    private List<Model> mDatas;
    private List<AppInfo> TopAppInfos;
    private LayoutInflater inflateryu;
    private int pageCount;//总页数
    private int pageSize = 6;//每一页的个数
    private int curIndex = 0;//当前显示的事第几页
    private View rooterView;
    private TextView guide_label;
    private TextView guide_description;
    private Button guide_btn;
    private Button guide_jump;
    private int mCheckedCount=12;
    private RelativeLayout topwise_guid_page;
    private ImageView topwise_guid_background_icon;
    private int guide_max_count=12;//引导页面最大展示数量
    private int guide_kaiji_must=779;//开机必备模块的ID

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         rooterView = inflater.inflate(R.layout.as_fragment_guide1, container,false);
        //add by zhangrui start
        guide_label=(TextView)rooterView.findViewById(R.id.guide_label);
        guide_description=(TextView)rooterView.findViewById(R.id.guide_description);
        guide_btn=(Button) rooterView.findViewById(R.id.guide_btn);
        guide_jump=(Button) rooterView.findViewById(R.id.guide_jump);
        topwise_guid_background_icon=(ImageView) rooterView.findViewById(R.id.topwise_guid_background_icon);
        topwise_guid_background_icon.setBackgroundResource(R.drawable.topwise_guid_page_must);
        topwise_guid_page=(RelativeLayout) rooterView.findViewById(R.id.topwise_guid_page);
        mPager = (ViewPager) rooterView.findViewById(R.id.viewpager);
        mLlDot = (LinearLayout) rooterView.findViewById(R.id.ll_dot);

        AppManager.getInstance().loadRank(guide_kaiji_must, new ManagerCallback() {
            @Override
            public void onSuccess(String moduleType, int dataType, int page, int num, boolean end) {
                initDatas(dataType);
                initUI();
            }

            @Override
            public void onFailure(String moduleType, int dataType, Throwable t, int errorNo, String strMsg) {

            }
        });
        //add by zhangrui end
        return rooterView;
    }
    private void setOvalLayout() {
        for (int i = 0; i < pageCount; i++) {
            mLlDot.addView(inflateryu.inflate(R.layout.dot, null));
        }
        //默认显示第一页
        if(mLlDot.getChildAt(0) !=  null)
        mLlDot.getChildAt(0).findViewById(R.id.v_dot).setBackgroundResource(R.drawable.dot_selected);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //取消选中
                mLlDot.getChildAt(curIndex).findViewById(R.id.v_dot).setBackgroundResource(R.drawable.dot_normal);
                //选中
                mLlDot.getChildAt(position).findViewById(R.id.v_dot).setBackgroundResource(R.drawable.dot_selected);

                curIndex = position;
                switch (position){
                    case 0:
                        topwise_guid_background_icon.setBackgroundResource(R.drawable.topwise_guid_page_must);
//                        topwise_guid_page.setBackgroundResource(R.drawable.topwise_guid_page_must);
                        guide_label.setText(getActivity().getResources().getString(R.string.topwise_guide_tuijian));
                        guide_btn.setText(getActivity().getResources().getString(R.string.topwise_guide_step));
                        break;

                    case 1:
                        topwise_guid_background_icon.setBackgroundResource(R.drawable.topwise_guid_page_game);
//                        topwise_guid_page.setBackgroundResource(R.drawable.topwise_guid_page_game);
                        guide_label.setText(getActivity().getResources().getString(R.string.topwise_guide_game));
                        guide_btn.setText(getActivity().getResources().getString(R.string.topwise_guide_finish));
                        break;
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }



    private void initUI(){
        inflateryu = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //总页数=总数/每页的个数，取整
        pageCount = (int) Math.ceil(mDatas.size() * 1.0 / pageSize);

        mPagerList = new ArrayList<>();
        for (int i = 0; i < pageCount; i++) {
            //每个页面都是inflate出的一个新实例
            final GridView gridView = (GridView) inflateryu.inflate(R.layout.gridview, null);
            mGridadapter=new GridViewAdapter(getActivity(), mDatas, i, pageSize);
            gridView.setAdapter(mGridadapter);
            mPagerList.add(gridView);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    int pos = position + curIndex * pageSize;
                    if(mDatas.get(pos).isChecked())
                        mDatas.get(pos).setChecked(false);
                    else
                        mDatas.get(pos).setChecked(true);
                    mGridadapter.notifyDataSetChanged();
                    mCheckedCount=computeCheckedAppCount();
                    if(mViewPageradapter != null)mViewPageradapter.notifyDataSetChanged();
//                    Toast.makeText(getActivity(), mDatas.get(pos).getName()+"///"+pos, Toast.LENGTH_SHORT).show();
                    if(mCheckedCount == 0) {
                        guide_description.setText(getActivity().getResources().getString(R.string.topwise_guide_lebel_decreption));
                    }else{
                        String mDescript=getActivity().getResources().getString(R.string.topwise_guide_lebel_decreption_finish1)+mCheckedCount+getActivity().getResources().getString(R.string.topwise_guide_lebel_decreption_finish2);
                        guide_description.setText(mDescript);
                    }
                }
            });
        }
        //设置viewpageAdapter
        mViewPageradapter=new ViewPagerAdapter(mPagerList);
        mPager.setAdapter(mViewPageradapter);
        //设置小圆点
        setOvalLayout();
        guide_jump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new MainFragment()).commit();
            }
        });

        guide_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(guide_btn.getText().equals(getActivity().getResources().getString(R.string.topwise_guide_finish))){
                    if(mDatas.size() != 0){
                      for(int mcount=0;mcount<mCheckedCount;mcount++){
                          handleAppInfo(mDatas.get(mcount).getAppInfo());
                          Log.d("zxc","下载"+mcount);
                      }
                    }
                    String guideToast=getActivity().getResources().getString(R.string.topwise_guide_toast_part_one)+mCheckedCount+getActivity().getResources().getString(R.string.topwise_guide_toast_part_two);
                    Toast.makeText(getActivity(),guideToast,Toast.LENGTH_SHORT).show();
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new MainFragment()).commit();
                }else if(guide_btn.getText().equals(getActivity().getResources().getString(R.string.topwise_guide_step))){
                    mPager.setCurrentItem(1);
                }
            }
        });
    }

    /**
     * 初始化数据源
     */

    private void initDatas(int dataType) {
        mDatas = new ArrayList<Model>();
        Rank myrank = DataPool.getInstance().getRank(dataType,  "" + guide_kaiji_must);
        TopAppInfos=myrank.apps;
        if(TopAppInfos != null && TopAppInfos.size() != 0) {
            for (int i = 0; i < TopAppInfos.size(); i++) {
                Log.d("zr","name="+TopAppInfos.get(i).name+"/////icon_url="+TopAppInfos.get(i).icon_url);
                mDatas.add(new Model(TopAppInfos.get(i),true));
            }
        }else {
            TopAppInfos = new ArrayList<AppInfo>();
            Log.d("zr", "TopAppInfos.size() == 0");
        }

        guide_description.setText(getActivity().getResources().getString(R.string.topwise_guide_lebel_decreption_init));
    }

    public int computeCheckedAppCount(){
        int mCount=0;
        for (int i = 0; i < TopAppInfos.size(); i++) {
            if(mDatas.get(i).isChecked()){
                mCount++;
            }
        }
        return mCount;
    }

    /**
     * 处理APP信息的入口，将mAppInfo加入列表中
     */
    public void handleAppInfo(AppInfo mAppInfo) {
        if (null != mAppInfo) {

            //增加处理 FLAG_INSTALLING
            if (mAppInfo.flag == AppInfo.FLAG_INSTALLING) {
                mAppInfo.flag = AppInfo.FLAG_DOWNLOADED;
            }
            if (mAppInfo.flag == AppInfo.FLAG_ONLINE || mAppInfo.flag == AppInfo.FLAG_NEED_UPGRADE) {

                startDownloadApp(mAppInfo);
            } else if (mAppInfo.flag == AppInfo.FLAG_DOWNLOADED) {

                startInstallApp(mAppInfo);
            } else if (mAppInfo.flag == AppInfo.FLAG_INSTALLED) {

                startOpenApp(mAppInfo);
            }
        }
    }

    private void startDownloadApp(AppInfo appInfo) {
        DownloadInfo downloadInfo = getDownloadInfo(appInfo);

        String key = getRemoteId(appInfo);

        if (downloadInfo != null && downloadInfo.downloadStatus == DownloadInfo.STATUS_DOWNLOADING) {
            // 如果状态是正在下载，点击后就去暂停下载
            // 规避按钮状态
            //setProgressButtonString(mResources.getString(R.string.as_listitem_download_button_resume));

            DownloadManager.getInstance().pauseDownload(downloadInfo.uid);
        }else if (downloadInfo != null && (downloadInfo.downloadStatus == DownloadInfo.STATUS_PENDING ||
                downloadInfo.downloadStatus == DownloadInfo.STATUS_PAUSE ||
                DownloadInfo.isErrorStatus(downloadInfo.downloadStatus))) {

            DownloadManager.getInstance().resumeDownload(downloadInfo.uid);
        }else {
            downloadApp(appInfo);
        }
    }

    /**
     * 安装，文件不存在删除下载信息，跳转下载
     */
    private void startInstallApp(AppInfo mAppInfo) {
        File file = DataPool.getInstance().getAppInfo(mAppInfo.id).file;
        if (file != null && file.exists()) {
            AppManager.getInstance().endDownloadApp(mAppInfo, file);
        } else {
            DataPool.getInstance().restoreToOnlineFlag(mAppInfo);
            //setProgressButtonMax(100);
            DownloadInfo minfo = getDownloadInfo(mAppInfo);
            if (minfo != null) {
                DownloadManager.getInstance().mDownloadDBProvider.deleteDownloadInfo(minfo.uid);
            }
            startDownloadApp(mAppInfo);
        }
    }

    /**
     * 打开app,打开失败，已经是下载状态，跳转安装
     */
    private void startOpenApp(AppInfo mAppInfo) {
        if (!AppManager.getInstance().startOpenApp(mAppInfo)) {
            if (mAppInfo.flag == AppInfo.FLAG_DOWNLOADED) {
                startInstallApp(mAppInfo);
            }
        }
    }



    /**
     * AppInfo  》 DownloadInfo
     */
    private DownloadInfo getDownloadInfo(AppInfo mAppInfo) {
        String mUid = getRemoteId(mAppInfo);
        ArrayList<DownloadInfo> allDownloads = DownloadManager.getInstance().mDownloadDBProvider.getAllDownloads();
        for (DownloadInfo downloadInfo : allDownloads) {
            if (downloadInfo.uid.equals(mUid)) {
                return downloadInfo;
            }
        }
        return null;
    }

    /**
     * 根据 AppInfo 找到 DownloadInfo的 uid (拼接规则)
     */
    public static String getRemoteId(AppInfo mAppInfo) {
        String remoteId = "";
        if (null != mAppInfo) {
            remoteId = mAppInfo.id + "_" + mAppInfo.vercode;
        }
        return remoteId;
    }

    private boolean isDownloadSuccess(AppInfo mAppInfo) {
        DownloadInfo info = getDownloadInfo(mAppInfo);
        if (info != null) {
            return info.downloadStatus == DownloadInfo.STATUS_DOWNLOAD_SUCCESS;
        } else {
            return mAppInfo.flag == AppInfo.FLAG_DOWNLOADED;
        }
    }



    private void downloadApp(AppInfo mAppInfo) {
        if (null != mAppInfo) {
            AppManager.getInstance().startDownloadApp(mAppInfo);
        }
    }

}

