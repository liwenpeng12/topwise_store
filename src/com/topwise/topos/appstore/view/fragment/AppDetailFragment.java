package com.topwise.topos.appstore.view.fragment;

import java.util.ArrayList;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.topwise.topos.appstore.conn.behavior.BehaviorEx;
import com.topwise.topos.appstore.conn.behavior.BehaviorLogManager;
import com.topwise.topos.appstore.data.AppInfo;
import com.topwise.topos.appstore.data.Banner;
import com.topwise.topos.appstore.data.DataPool;
import com.topwise.topos.appstore.manager.AppManager;
import com.topwise.topos.appstore.manager.BusinessManager;
import com.topwise.topos.appstore.manager.ManagerCallback;
import com.topwise.topos.appstore.manager.NetworkManager;
import com.topwise.topos.appstore.manager.NetworkManager.NetworkListener;
import com.topwise.topos.appstore.utils.AsynTaskManager;
import com.topwise.topos.appstore.utils.BitmapUtil;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.utils.AsynTaskManager.ImageLoadCallBack;
import com.topwise.topos.appstore.view.ListLableTitleView;
import com.topwise.topos.appstore.view.ListMainItemView;
import com.topwise.topos.appstore.view.ListMainItemView.AppItemClickListener;
import com.topwise.topos.appstore.view.WaitingView.RefrushClickListener;
import com.topwise.topos.appstore.view.activity.AppDetailActivity;
import com.topwise.topos.appstore.view.PopularItemView;
import com.topwise.topos.appstore.view.WaitingView;
import com.topwise.topos.appstore.view.widget.ScrollEnableViewPager;
import com.topwise.topos.appstore.view.widget.StretchyTextView;
import com.topwise.topos.appstore.view.widget.MyViewPager.OnPageChangeListener;
import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.R;
import com.umeng.analytics.MobclickAgent;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class AppDetailFragment extends BaseFragment implements OnClickListener {
    private static final String TAG = "AppDetailFragment";
    private static final float ViewPager_THUMB_MARGIN_FACTOR_HDPI = 0.2963f;
    private WaitingView mWaitingView;

    private StretchyTextView mStretchyTextView;
    private ScrollEnableViewPager mPreviewViewpager;
    private PreviewPagerAdpter mPreviewPagerAdpter;
    private View[] mViews;
    private ImageView[] mImageViews;

    private int mImageWidth;
    private int mImageHeight;
    private int mViewpagerMargin;
    private Context mContext;
    private ViewGroup mFragmentView;
    private AppInfo mAppInfo;
    private int mPreviewNum;
    private LayoutInflater mInflater;
    private LinearLayout mRelatedContainer;
    private LinearLayout mPopularRootContainer;
    private FrameLayout mPopularAppContainer;
    private TextView mPopularClickView;
    private LinearLayout mBannerContainer;
    private TextView mAppDetailIntrTitle;
    private TextView mBannerName;
    private TextView mBannerDesc;
    private ImageView mBannerImg;
    private Banner mBanner;
    private boolean mHasLoadData = false;
    private String mCallbackCaller = String.valueOf(AppDetailFragment.this.hashCode());
    private ImageLoadCallBack mBannerImageCallback = null;
    
    private AsynTaskManager.ImageLoadCallBack mPreviewcallback = new AsynTaskManager.ImageLoadCallBack() {
        @Override
        public void onImageLoadSuccess(String imageUrl, Bitmap bitmap) {
            if (isDestroyed()) {
                return;
            }

            for (int position = 0; position < mAppInfo.thumbnail_urls.size(); position++) {
                if (imageUrl.equals(mAppInfo.thumbnail_urls.get(position))) {
                    invalidatePreviewImage(position, bitmap);
                }
            }

        }

        @Override
        public void onImageLoadFailed(String imageUrl, String reason) {
            LogEx.d(TAG, imageUrl + ":" + reason);
        }

        @Override
        public boolean isNeedToDecode(String imageUrl) {
            if (isDestroyed()) {
                return false;
            }

            for (int position = 0; position < mAppInfo.thumbnail_urls.size(); position++) {
                if (imageUrl.equals(mAppInfo.thumbnail_urls.get(position))) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String getCaller() {
            return mCallbackCaller;
        }
    };

    private ManagerCallback mManagerCallback = new ManagerCallback() {
        @Override
        public void onSuccess(String moduleType, int dataType, int page, int num, boolean end) {
            if (isDestroyed()) {
                return;
            }

            ArrayList<AppInfo> relatedAppinfoList = DataPool.getInstance().getAppInfos(dataType);
            if (dataType == DataPool.TYPE_APP_RELATED_SIMILAR) {
                updateRalatedViews(relatedAppinfoList);
            } else if (dataType == DataPool.TYPE_APP_RELATED_PEOPLE_LIKE) {
                updatePopularAppView(relatedAppinfoList);
            }
        }

        @Override
        public void onFailure(String moduleType, int dataType, Throwable t, int errorNo,
                String strMsg) {
        }
    };

    private NetworkListener mNetworkListener = new NetworkListener() {
        @Override
        public void onNetworkConnected() {
            if (isDestroyed()) {
                return;
            }

            if (mPreviewPagerAdpter == null) {
                setAppInfo(mAppInfo);
            } else if (!mHasLoadData) {
                initRelatedAndPopularApp();
            }
        }

        @Override
        public void onNetworkDisconnected() {
            if (isDestroyed()) {
                return;
            }
            Toast.makeText(getActivity(), R.string.as_network_unavailable, Toast.LENGTH_SHORT)
                    .show();
        }
    };

    private RefrushClickListener mRefushBtnListener = new RefrushClickListener() {
        @Override
        public void onRefushClicked(WaitingView v) {
            if (mPreviewPagerAdpter == null) {
                setAppInfo(mAppInfo);
            }
        }
    };

    public AppDetailFragment() {

    }

    public void setAppInfo(AppInfo info) {
        mAppInfo = info;
        AppManager.getInstance().loadAppDetail(mAppInfo, new ManagerCallback() {

            @Override
            public void onSuccess(String moduleType, int dataType, int page, int num, boolean end) {
                if (isDestroyed()) {
                    return;
                }
                mAppInfo = DataPool.getInstance().getAppInfo(mAppInfo.id);
                mPreviewNum = mAppInfo.thumbnail_urls.size();
                setViewsContent(mAppInfo);
            }

            @Override
            public void onFailure(String moduleType, int dataType, Throwable t, int errorNo, String strMsg) {
                if (isDestroyed()) {
                    return;
                }
                if (NetworkManager.getInstance().isNetworkAvailable()) {
                    if (mAppInfo.bindId != null && mAppInfo.bindId.length() > 0) {
                        hideWaitingView();
                    } else {
                        mWaitingView.showRefrushButton(getString(R.string.as_list_load_failed_prompt)  + getString(R.string.as_refresh_btn_again), mRefushBtnListener);
                    }
                } else {
                    mWaitingView.showPromptText(getString(R.string.as_network_unavailable));
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable
    ViewGroup container, @Nullable
    Bundle savedInstanceState) {
        mIsDestroy = false;
        mContext = this.getActivity();
        mInflater = LayoutInflater.from(mContext);
        mImageWidth = mContext.getResources().getDimensionPixelSize(
                R.dimen.zkas_detail_viewpager_image_width);
        ;
        mImageHeight = mContext.getResources().getDimensionPixelSize(
                R.dimen.zkas_detail_viewpager_image_height);
        ;
        mFragmentView = (ViewGroup) inflater.inflate(R.layout.zkas_fragment_detail_layout, container, false);
        initViews();
        NetworkManager.getInstance().registerNetworkListener(mNetworkListener);
        return mFragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("AppDetailFragment");
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("AppDetailFragment");
    }

    private void initViews() {
        initWaitView();
        mAppDetailIntrTitle = (TextView) mFragmentView.findViewById(R.id.zkas_appdetail_intr_title);
        mRelatedContainer = (LinearLayout) mFragmentView
                .findViewById(R.id.zkas_appdetail_related_app_container);
        mPopularRootContainer = (LinearLayout) mFragmentView
                .findViewById(R.id.zkas_appdetail_popular_app_container);
        mPopularAppContainer = (FrameLayout) mFragmentView
                .findViewById(R.id.zkas_appdetail_popular_items_container);
        mPopularClickView = (TextView) mFragmentView
                .findViewById(R.id.zkas_appdetail_popular_click_view);
        mPopularClickView.setOnClickListener(this);
        mPopularClickView.setVisibility(View.GONE);

        mStretchyTextView = (StretchyTextView) mFragmentView
                .findViewById(R.id.zkas_appdetail_intr_content);
        mPreviewViewpager = (ScrollEnableViewPager) mFragmentView
                .findViewById(R.id.zkas_appdetail_preview_image_viewpager);

        if (mBannerContainer == null) {
            mBannerContainer = (LinearLayout) mFragmentView
                    .findViewById(R.id.zkas_appdetail_banner_container);
            mBannerContainer.setOnClickListener(this);
            mBannerName = (TextView) mFragmentView.findViewById(R.id.zkas_appdetail_banner_name);
            mBannerDesc = (TextView) mFragmentView.findViewById(R.id.zkas_appdetail_banner_intr);
            mBannerImg = (ImageView) mFragmentView.findViewById(R.id.zkas_appdetail_banner_img);
        }

        initPreviewpager();
    }

    private void initWaitView() {
        mWaitingView = (WaitingView) mFragmentView.findViewById(R.id.as_wait_view);
        if (NetworkManager.getInstance().isNetworkAvailable()) {
            mWaitingView.startProgress(getString(R.string.as_list_loading_prompt));
        } else {
            mWaitingView.showPromptText(getString(R.string.as_network_unavailable));
        }
    }

    private void initPreviewpager() {
        mViewpagerMargin = -(int) ((AppStoreWrapperImpl.getInstance().getDeviceInfo()
                .getScreenWidth() - mImageWidth) * 0.90f);
        if (AppStoreWrapperImpl.getInstance().getDeviceInfo().getScreenWidth() == 480) {
            mViewpagerMargin = (int) (-1
                    * AppStoreWrapperImpl.getInstance().getDeviceInfo().getScreenWidth() * ViewPager_THUMB_MARGIN_FACTOR_HDPI);
        }

        mPreviewViewpager.getLayoutParams().height = mContext.getResources().getDimensionPixelSize(
                R.dimen.zkas_detail_viewpager_height);
        mPreviewViewpager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mPreviewViewpager.setOffscreenPageLimit(2);
        mPreviewViewpager.setPageMargin(mViewpagerMargin);
    }

    /**
     * 初始化类似应用模块
     */
    private void initRelatedAndPopularApp() {
        AppManager.getInstance().loadRelated(mAppInfo, 0, mManagerCallback);
        AppManager.getInstance().loadRelated(mAppInfo, 1, mManagerCallback);
    }

    private void doAffterHideWaiting() {
        if (!mHasLoadData) {
            mHasLoadData = true;
            mFragmentView.findViewById(R.id.zkas_id_detail_container).setVisibility(View.VISIBLE);
            initRelatedAndPopularApp();
            initDetailBanner();
        }
    }

    /**
     * 更新相关应用的view
     * 
     * @param appInfoList
     */
    private void updateRalatedViews(ArrayList<AppInfo> appInfoList) {
        if (appInfoList == null) {
            mRelatedContainer.setVisibility(View.GONE);
            return;
        }
        if (appInfoList.size() == 0) {
            mRelatedContainer.setVisibility(View.GONE);
            return;
        }
        mRelatedContainer.setVisibility(View.VISIBLE);
        int length = appInfoList.size() > 6 ? 6 : appInfoList.size();
        if (length > 0) {
            ListLableTitleView titleview = new ListLableTitleView(getActivity());
            titleview.setText(getString(R.string.as_listitem_related_title));
            mRelatedContainer.addView(titleview);
        }

        for (int i = 0; i < length; i++) {
            ListMainItemView item = (ListMainItemView) mInflater.inflate(
                    R.layout.zkas_list_item_mulit_line_layout, mRelatedContainer, false);
            item.setOnAppItemClickListener(new AppItemClickListener() {
                @Override
                public void onAppItemClicked(AppInfo info) {
                    startDetailAcitivity(info);
                }
            });
            mRelatedContainer.addView(item);
            updateItemContent(item, appInfoList.get(i));
        }
    }

    private void updateItemContent(final ListMainItemView item, final AppInfo info) {
        item.setAppInfo(info);
        item.setFrom("类似应用");
    }

    /**
     * 更新大家喜欢的view
     * 
     * @param appInfoList
     */
    public void updatePopularAppView(ArrayList<AppInfo> appInfoList) {
        if (appInfoList == null) {
            mPopularRootContainer.setVisibility(View.GONE);
            return;
        }
        if (appInfoList.size() == 0) {
            mPopularRootContainer.setVisibility(View.GONE);
            return;
        }
        mPopularRootContainer.setVisibility(View.VISIBLE);
        int length = appInfoList.size() > 3 ? 3 : appInfoList.size();
        for (int i = 0; i < length; i++) {
            PopularItemView item = (PopularItemView) mInflater.inflate(
                    R.layout.zkas_popular_item_layout, mPopularAppContainer, false);
            item.setOnAppItemClickListener(new AppItemClickListener() {
                @Override
                public void onAppItemClicked(AppInfo info) {
                    startDetailAcitivity(info);
                }
            });
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            if (i == 0) {
                lp.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
                item.setGravity(Gravity.CENTER);
            } else if (i == 1) {
                lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
                item.setGravity(Gravity.CENTER);
            } else if (i == 2) {
                lp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
                item.setGravity(Gravity.CENTER);
            }

            mPopularAppContainer.addView(item, lp);
            updateItemContent(item, appInfoList.get(i));
        }
    }

    public void updateItemContent(PopularItemView item, AppInfo info) {
        item.setAppInfo(info);
        item.setFrom("大家喜欢");
    }

    private void initDetailBanner() {
        BusinessManager.getInstance().loadBanners(new ManagerCallback() {
            @Override
            public void onSuccess(String moduleType, int dataType, int page, int num, boolean end) {
                if (isDestroyed()) {
                    return;
                }
                ArrayList<Banner> banners = DataPool.getInstance().getBanners(dataType);
                if (banners != null && banners.size() > 0) {
                    int random = (int) (Math.random() * banners.size());
                    mBanner = banners.get(random);
                    updateDetailBannerView(mBanner);
                }
            }

            @Override
            public void onFailure(String moduleType, int dataType, Throwable t, int errorNo,
                    String strMsg) {

            }
        });
    }

    private void updateDetailBannerView(final Banner bannerInfo) {
        if (bannerInfo == null) {
            mBannerContainer.setVisibility(View.GONE);
        }
        mBannerContainer.setVisibility(View.VISIBLE);
        mBannerName.setText(bannerInfo.title);
        if (bannerInfo.desc == null || bannerInfo.desc.length() == 0) {
            mBannerDesc.setVisibility(View.GONE);
        } else {
            mBannerDesc.setVisibility(View.VISIBLE);
            mBannerDesc.setText(bannerInfo.desc);
        }
        if (mBannerImageCallback == null) {
            mBannerImageCallback = new ImageLoadCallBack() {

                @Override
                public void onImageLoadSuccess(String imageUrl, Bitmap bitmap) {
                    if (isDestroyed()) {
                        return;
                    }
                    if (imageUrl.equals(bannerInfo.img_url)) {
                        mBannerImg.setImageBitmap(bitmap);
                    }
                }

                @Override
                public void onImageLoadFailed(String imageUrl, String reason) {
                    LogEx.d(TAG, imageUrl + ":" + reason);
                }

                @Override
                public boolean isNeedToDecode(String imageUrl) {
                    if (isDestroyed()) {
                        return false;
                    }

                    if (imageUrl.equals(bannerInfo.img_url)) {
                        return true;
                    }
                    return false;
                }

                @Override
                public String getCaller() {
                    return mCallbackCaller;
                }
            };
        }
        Bitmap bitmap = BitmapUtil.getInstance().getBitmapAsync(bannerInfo.img_url,
                mBannerImageCallback);
        if (bitmap != null) {
            mBannerImg.setImageBitmap(bitmap);
        }
        
        BehaviorLogManager.getInstance().addBehaviorEx(
                new BehaviorEx(BehaviorEx.VIEW_BANNER, bannerInfo.id));
    }

    private void setViewsContent(AppInfo info) {
        if (info.desc != null) {
            mStretchyTextView.setTextContent(info.desc);
            mAppDetailIntrTitle.setVisibility(View.VISIBLE);
        }
        setPreviewImages(info);
        if (mPreviewPagerAdpter == null) {
            mPreviewPagerAdpter = new PreviewPagerAdpter(mViews);
        } else {
            mPreviewPagerAdpter.setViews(mViews);
        }

        if (mPreviewViewpager.getAdapter() == null) {
            mPreviewViewpager.setAdapter(mPreviewPagerAdpter);
        } else {
            mPreviewPagerAdpter.notifyDataSetChanged();
        }
        setPreviewViewPagerFirstItemDestX();
        mPreviewViewpager.setCurrentItem(0, false);
    }

    private void setPreviewImages(AppInfo info) {
        if(mPreviewNum == 0 && mPreviewViewpager != null) {
            mPreviewViewpager.setVisibility(View.GONE);
            hideWaitingView();
        }
        if (mPreviewNum > 0) {
            mViews = new View[mPreviewNum];
            mImageViews = new ImageView[mPreviewNum];
            if (info != null) {
                for (int i = 0; i < mPreviewNum; i++) {
                    invalidPreview(i);
                }
            }
        }
    }

    private void invalidPreview(final int position) {
        if (mViews != null) {
            if (mViews[position] == null) {
                mViews[position] = View.inflate(mContext,
                        R.layout.zkas_appdetail_image_layout, null);
            }
            if (mImageViews[position] == null) {
                mImageViews[position] = (ImageView) mViews[position]
                        .findViewById(R.id.imageItem);
            }
        }

        Bitmap bitmap = BitmapUtil.getInstance().getBitmapAsync(
                mAppInfo.thumbnail_urls.get(position), mPreviewcallback);
        invalidatePreviewImage(position, bitmap);
    }

    private void invalidatePreviewImage(int position, Bitmap bitmap) {
        if (position == 0) {
            hideWaitingView();
        }
        if (mImageViews != null && mImageViews[position] != null) {
            LayoutParams lp = mImageViews[position].getLayoutParams();
            lp.width = mImageWidth;
            lp.height = mImageHeight;
            if (bitmap != null) {
                mImageViews[position].setScaleType(ScaleType.FIT_XY);
                mImageViews[position].setBackground(null);
                mImageViews[position].setImageBitmap(bitmap);
            } else {
                mImageViews[position].setImageResource(R.drawable.as_detail_image_bg);
            }
        }
    }

    public void showWaitViewProgress(String prompt) {
        if (mWaitingView != null) {
            mWaitingView.startProgress(prompt);
        }
    }

    public void hideWaitingView() {
        if (mWaitingView != null) {
            mWaitingView.setVisibility(View.GONE);
        }
        doAffterHideWaiting();
    }

    protected void setPreviewViewPagerFirstItemDestX() {
        mPreviewViewpager.setFirstItemDestX(-mContext.getResources().getDimensionPixelOffset(
                R.dimen.zkas_detail_viewpager_first_item_magrin));
    }

    public class PreviewPagerAdpter extends PagerAdapter {
        private View mPreViews[];

        public PreviewPagerAdpter(View[] views) {
            mPreViews = views;
        }

        public void setViews(View[] views) {
            mPreViews = views;
        }

        @Override
        public void destroyItem(View container, int arg1, Object object) {
            ((ViewGroup) container).removeView((View) object);

        }

        @Override
        public void finishUpdate(View arg0) {

        }

        @Override
        public int getCount() {
            if (mPreViews == null) {
                return 0;
            }
            return mPreViews.length;
        }

        @Override
        public Object instantiateItem(View container, int position) {
            if (mPreViews == null) {
                return null;
            }
            ((ViewGroup) container).addView(mPreViews[position]);
            return mPreViews[position];
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {

        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {

        }

        /**
         * 这里防止删除主题不刷新
         */
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mPopularClickView) {

        } else if (v == mBannerContainer) {
            MainListFragment.dealBannerClicked(getActivity(), mBanner, mFragmentView);
        }
    }

    private void clearCache() {
        NetworkManager.getInstance().unregisterNetworkListener(mNetworkListener);
        BitmapUtil.getInstance().clearCallerCallback(mCallbackCaller);
        mImageViews = null;
        mViews = null;
        mPreviewcallback = null;
        mManagerCallback = null;
        mBannerImageCallback = null;
    }

    public void startDetailAcitivity(AppInfo info) {
        BehaviorLogManager.getInstance().addBehaviorEx(
                new BehaviorEx(BehaviorEx.ENTER_APP_DETAIL, info.id + "@" + info.from));
        
        Intent mIntent = new Intent(mContext, AppDetailActivity.class);
        mIntent.putExtra("app_id", info.id);
        startActivity(mIntent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        clearCache();
    }
}
