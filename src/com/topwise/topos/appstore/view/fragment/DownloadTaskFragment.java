package com.topwise.topos.appstore.view.fragment;

import java.util.ArrayList;

import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.data.AppInfo;
import com.topwise.topos.appstore.data.DataPool;

import com.topwise.topos.appstore.download.DownloadDBProvider;
import com.topwise.topos.appstore.download.DownloadInfo;
import com.topwise.topos.appstore.download.DownloadManager;
import com.topwise.topos.appstore.manager.AppManager;
import com.topwise.topos.appstore.manager.ManagerCallback;
import com.topwise.topos.appstore.utils.Utils;
import com.topwise.topos.appstore.view.ArrayListAdapter;
import com.topwise.topos.appstore.view.ListAppItemView;
import com.topwise.topos.appstore.view.WaitingView;
import com.umeng.analytics.MobclickAgent;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class DownloadTaskFragment extends BaseFragment implements DownloadManager.DownloadObserver {

    private static DownloadTaskFragment instance;
    private ExpandableListView mExpandableList;
    private BaseExpandableListAdapter mAdapter;
    private RecommendAdapter mRecommendAdapter;
    private DownloadManager mDownloadManager;
    private View mNoTaskNoNetworkLayout;
    private View mBottomLayout;
    private TextView mChangeRecommendData;
    private TextView mfindApp;
    private ListView mRecommendlist;
    private ArrayList<AppInfo> mRecommendData;
    private ImageView downloadbottomshadow;
    private ArrayList<String> groupString = new ArrayList<String>();
    private View headview;
    private WaitingView mWaitingView;
    private Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
    private ArrayList<AppInfoTag> mAppInfoTag = new ArrayList<AppInfoTag>();
    private DataPool.DataObserver mObserver;

    private View getHeadView() {
        if (headview == null) {
            headview = LayoutInflater.from(getActivity()).inflate(
                    R.layout.zkas_download_headview_layout, null);
            mChangeRecommendData = (TextView) headview.findViewById(R.id.change);
            mChangeRecommendData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppManager.getInstance().loadRandomApps(new ManagerCallback() {
                        @Override
                        public void onSuccess(String moduleType, int dataType, int page, int num,
                                              boolean end) {
                            if (isDestroyed()) {
                                return;
                            }
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

            public void onClick(View v) {
            }
        });
        return headview;
    }

    public static DownloadTaskFragment getInstance() {
        if (instance == null) {
            instance = new DownloadTaskFragment();
        }
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View mView = inflater.inflate(R.layout.zkas_download_task_fragment, container, false);
        mExpandableList = (ExpandableListView) mView.findViewById(R.id.downloadList);
        mNoTaskNoNetworkLayout = mView.findViewById(R.id.notask_nonetlayout);
        mChangeRecommendData = (TextView) mView.findViewById(R.id.change);
        mBottomLayout = mView.findViewById(R.id.otherapp_bottom);
        mfindApp = (TextView) mView.findViewById(R.id.findapp);
        mWaitingView = (WaitingView) mView.findViewById(R.id.as_wait_view);
        downloadbottomshadow = (ImageView) mView.findViewById(R.id.downloadbottom_shadow);
        mRecommendlist = (ListView) mView.findViewById(R.id.recommendlist);
        mRecommendlist.addHeaderView(getHeadView());
        mAdapter = new DownloadExpandableListAdapter();
        mRecommendAdapter = new RecommendAdapter(getActivity());
        mExpandableList.setAdapter(mAdapter);
        mRecommendlist.setAdapter(mRecommendAdapter);
        updateViews();
        ensureSomeGroupIsExpanded();

        mfindApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        return mView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groupString.add(getString(R.string.as_inprogress));
        groupString.add(getString(R.string.as_completedprogress));
        mDownloadManager = DownloadManager.getInstance();
        mDownloadManager.registerDownloadObserver(this);
        mAppInfoTag = new ArrayList<AppInfoTag>();

        mObserver = new DataPool.DataObserver() {
            @Override
            public void onChanged(final int type) {
                if(isDestroyed()){
                    return;
                }
                mMainThreadHandler.post(new Runnable() {

                    @Override
                    public void run() {
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
        MobclickAgent.onPageStart("DownloadTaskFragment");
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("DownloadTaskFragment");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDownloadManager.unregisterDownloadObserver(this);
        mAppInfoTag.clear();
        mAppInfoTag = null;
        DataPool.getInstance().unregisterDataObserver(mObserver);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void updateViews() {
        int downloadingCount = mDownloadManager.mDownloadDBProvider.mDownloadingJobs == null ? 0 : mDownloadManager.mDownloadDBProvider.mDownloadingJobs.size();
        int completedCount = mDownloadManager.mDownloadDBProvider.mCompletedJobs == null ? 0 : mDownloadManager.mDownloadDBProvider.mCompletedJobs.size();
        if (downloadingCount == 0 && completedCount == 0) {
            groupString.clear();
            mExpandableList.setVisibility(View.GONE);
            mBottomLayout.setVisibility(View.VISIBLE);
            downloadbottomshadow.setVisibility(View.VISIBLE);
            if (Utils.isNetworkConnected()) {
                mWaitingView.setVisibility(View.VISIBLE);
                mWaitingView.startProgress(getString(R.string.loading));
                mNoTaskNoNetworkLayout.setVisibility(View.GONE);
                mRecommendlist.setVisibility(View.VISIBLE);
                AppManager.getInstance().loadRandomApps(new ManagerCallback() {
                    @Override
                    public void onSuccess(String moduleType, int dataType, int page, int num,
                                          boolean end) {
                        mWaitingView.stopProgressing();
                        mWaitingView.setVisibility(View.GONE);
                        mRecommendData = DataPool.getInstance().getAppInfos(DataPool.TYPE_APP_RANDOM_APPS);
                        mRecommendAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(String moduleType, int dataType, Throwable t, int errorNo, String strMsg) {
                    }
                });
            } else {
                mNoTaskNoNetworkLayout.setVisibility(View.VISIBLE);
                mRecommendlist.setVisibility(View.GONE);
            }
        } else {
            groupString.clear();
            groupString.add(getString(R.string.as_inprogress));
            groupString.add(getString(R.string.as_completedprogress));
            mBottomLayout.setVisibility(View.GONE);
            mNoTaskNoNetworkLayout.setVisibility(View.GONE);
            mRecommendlist.setVisibility(View.GONE);
            downloadbottomshadow.setVisibility(View.GONE);
            mExpandableList.setVisibility(View.VISIBLE);
            mAdapter.notifyDataSetChanged();
            ensureSomeGroupIsExpanded();
        }
    }

    public class DownloadExpandableListAdapter extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            if (groupString != null && groupString.size() != 0) {
                return groupString.size() + 2;
            }
            return 0;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            if (groupPosition == 1 || groupPosition == 3) {
                return 0;
            }
            if (groupPosition == 0) {
                return mDownloadManager.mDownloadDBProvider.mDownloadingJobs.size();
            }
            if (groupPosition == 2) {
                return mDownloadManager.mDownloadDBProvider.mCompletedJobs.size();
            }
            return 0;
        }

        @Override
        public Object getGroup(int groupPosition) {
            if (groupPosition == 1 || groupPosition == 3) {
                return null;
            }
            if (groupPosition == 0 && groupString != null && groupString.size() != 0) {
                return groupString.get(0);
            }
            if (groupPosition == 2 && groupString != null && groupString.size() != 0) {
                return groupString.get(1);
            }
            return null;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            if (groupPosition == 1 || groupPosition == 3) {
                return null;
            }
            if (groupPosition == 0 && DownloadManager.getInstance().mDownloadDBProvider.mDownloadingJobs.size() != 0) {
                ArrayList<DownloadInfo> list = DownloadDBProvider.jobsToList(DownloadManager.getInstance().mDownloadDBProvider.mDownloadingJobs);
                return list.get(childPosition);
            }
            if (groupPosition == 2 && DownloadManager.getInstance().mDownloadDBProvider.mCompletedJobs.size() != 0) {
                ArrayList<DownloadInfo> list = DownloadDBProvider.jobsToList(DownloadManager.getInstance().mDownloadDBProvider.mCompletedJobs);
                return list.get(childPosition);
            }
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {

            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (groupPosition == 1 || groupPosition == 3) {

//                return new ListDividerItemView(getActivity());
                return LayoutInflater.from(getContext()).inflate(R.layout.zkas_list_item_interval, parent, false);
            }
            View view = LayoutInflater.from(getActivity()).inflate(
                    R.layout.zkas_download_generic, parent, false);
            TextView textview = (TextView) view.findViewById(R.id.generic_textview);
            TextView cleanview = (TextView) view.findViewById(R.id.cleanbutton);
            if (groupPosition == 0) {
                textview.setText(groupString.get(0) + "(" + mDownloadManager.mDownloadDBProvider.mDownloadingJobs.size() + ")");
                cleanview.setVisibility(View.GONE);
            }
            if (groupPosition == 2) {
                textview.setText(groupString.get(1) + "(" + mDownloadManager.mDownloadDBProvider.mCompletedJobs.size() + ")");
                cleanview.setVisibility(View.VISIBLE);
            }
            cleanview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDownloadManager.mDownloadDBProvider.clearCompletedJobs();
                    updateViews();
                }
            });
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            return view;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            final DownloadItemViewHolder mDownloadItemViewHolder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                convertView = inflater.inflate(R.layout.zkas_download_row, parent, false);
                mDownloadItemViewHolder = new DownloadItemViewHolder();
                mDownloadItemViewHolder.layoutDownloadingMainItem = (ListAppItemView) convertView.findViewById(R.id.zkas_id_list_item_app_container);
                mDownloadItemViewHolder.layoutDownloadingMainItem.setDividerVisibility(false);
                mDownloadItemViewHolder.layoutDownloadingEx = convertView.findViewById(R.id.downloadrow_ex);
                mDownloadItemViewHolder.layoutDeDownload = convertView.findViewById(R.id.deletedownload);
                mDownloadItemViewHolder.ListMainItemViewGoneLine = (ImageView) convertView.findViewById(R.id.zkas_id_main_item_btm_divider);
                mDownloadItemViewHolder.ListMainItemViewGoneLine.setVisibility(View.GONE);
                mDownloadItemViewHolder.PinchButton = (ImageView) convertView.findViewById(R.id.expandbutton);
                convertView.setTag(mDownloadItemViewHolder);
            } else {
                mDownloadItemViewHolder = (DownloadItemViewHolder) convertView.getTag();
            }

            ArrayList<DownloadInfo> downloadInfos = new ArrayList<>();
            if (groupPosition == 0) {
                downloadInfos = DownloadDBProvider.jobsToList(mDownloadManager.mDownloadDBProvider.mDownloadingJobs);
            } else if (groupPosition == 2) {
                downloadInfos = DownloadDBProvider.jobsToList(mDownloadManager.mDownloadDBProvider.mCompletedJobs);
            }

            if (downloadInfos == null) {
                return convertView;
            }

            if (childPosition >= downloadInfos.size()) {
                return convertView;
            }

            String remoteid = downloadInfos.get(childPosition).uid;
            final String packageName = remoteid.substring(0, remoteid.lastIndexOf("_"));
            String filename = downloadInfos.get(childPosition).name;
            String name = filename.substring(0, filename.lastIndexOf("_"));
            AppInfo info = DataPool.getInstance().getAppInfo(packageName);
            if (info != null && (info.name == null || "".equals(name))) {
                info.name = name;
            }
            if (info == null || info.downloads == null || "".equals(info.downloads)) {
                if (info == null) {
                    info = new AppInfo();
                    info.id = info.pkg = packageName;
                }
                AppManager.getInstance().loadAppDetail(info, new ManagerCallback() {

                    @Override
                    public void onSuccess(String moduleType, int dataType, int page, int num,
                                          boolean end) {
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(String moduleType, int dataType, Throwable t,
                                          int errorNo, String strMsg) {

                    }

                });
            }

            if (info != null) {
                mDownloadItemViewHolder.layoutDownloadingMainItem.setAppInfo(info);
                if (groupPosition == 0) {
                    AppInfoTag mTag = new AppInfoTag();
                    mTag.appId = info.id;
                    mTag.tag = true;
                    mAppInfoTag.add(childPosition, mTag);
                }
            }
            final DownloadInfo mdownload = downloadInfos.get(childPosition);
            mDownloadItemViewHolder.layoutDeDownload.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mdownload.downloadStatus == DownloadInfo.STATUS_DOWNLOAD_SUCCESS) {
                        mDownloadManager.mDownloadDBProvider.deleteDownloadInfo(mdownload.uid);
                    } else {
                        mDownloadManager.cancelDownload(mdownload.uid);
                        mDownloadManager.mDownloadDBProvider.deleteDownloadInfo(mdownload.uid);
                    }
                    for (int i = 0; i < mAppInfoTag.size(); i++) {
                        mAppInfoTag.get(i).tag = true;
                        String remoteid = mdownload.uid;
                        String packageName = remoteid.substring(0, remoteid.lastIndexOf("_"));
                        if (packageName.equals(mAppInfoTag.get(i).appId))
                            mAppInfoTag.remove(i);
                    }
                    updateViews();
                }
            });

            if (groupString != null && groupString.size() != 0 && groupPosition == 2) {
                mDownloadItemViewHolder.layoutDownloadingEx.setVisibility(View.GONE);
                mDownloadItemViewHolder.PinchButton.setVisibility(View.GONE);
            } else if (groupString != null && groupString.size() != 0 && groupPosition == 0) {
                mDownloadItemViewHolder.PinchButton.setVisibility(View.VISIBLE);
            }
            mDownloadItemViewHolder.layoutDownloadingEx.setVisibility(View.GONE);
            mDownloadItemViewHolder.PinchButton.setImageDrawable(getResources().getDrawable(R.drawable.zkas_expand_bg));
            mDownloadItemViewHolder.PinchButton.setScaleType(ImageView.ScaleType.CENTER);
            mDownloadItemViewHolder.PinchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppInfoTag mAppInfo = mAppInfoTag.get(childPosition);
                    if (mAppInfo.tag) {
                        mDownloadItemViewHolder.PinchButton.setImageDrawable(getResources()
                                .getDrawable(R.drawable.zkas_retract_bg));
                        mDownloadItemViewHolder.PinchButton
                                .setScaleType(ImageView.ScaleType.CENTER);
                        mDownloadItemViewHolder.layoutDownloadingEx.setVisibility(View.VISIBLE);
                        mAppInfo.tag = false;
                    } else if (!mAppInfo.tag) {
                        mDownloadItemViewHolder.PinchButton.setImageDrawable(getResources()
                                .getDrawable(R.drawable.zkas_expand_bg));
                        mDownloadItemViewHolder.PinchButton
                                .setScaleType(ImageView.ScaleType.CENTER);
                        mDownloadItemViewHolder.layoutDownloadingEx.setVisibility(View.GONE);
                        mAppInfo.tag = true;
                    }
                }
            });
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public class DownloadItemViewHolder {
            ListAppItemView layoutDownloadingMainItem;
            ImageView ListMainItemViewGoneLine;
            View layoutDownloadingEx;
            View layoutDeDownload;
            ImageView PinchButton;
        }

    }

    public class RecommendAdapter extends ArrayListAdapter {

        private Context mContext;
        private LayoutInflater mInflator;

        public RecommendAdapter(Context context) {
            super(context);
            mContext = context;
            mInflator = LayoutInflater.from(mContext);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListAppItemView mListMainItemView = (ListAppItemView) convertView;
            if (mListMainItemView == null) {
                mListMainItemView = (ListAppItemView) mInflator.inflate(
                        R.layout.zkas_list_item_universal_layout, parent, false);
            }
            mListMainItemView.setAppInfo(mRecommendData.get(position));
            mListMainItemView.setFrom(getString(R.string.as_listitem_hot_title2));
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

    @Override
    public void onDownloadChanged(DownloadInfo info) {
        if (isDestroyed()) {
            return;
        }
        if (info.downloadStatus != DownloadInfo.STATUS_DOWNLOADING) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateViews();
                }
            });
        }
    }

    private void ensureSomeGroupIsExpanded() {
        mExpandableList.post(new Runnable() {
            public void run() {
                mExpandableList.expandGroup(0);
                mExpandableList.expandGroup(1);
                mExpandableList.expandGroup(2);
                mExpandableList.expandGroup(3);
            }
        });
    }

    public class AppInfoTag {
        public String appId;
        public boolean tag = true;
    }

}
