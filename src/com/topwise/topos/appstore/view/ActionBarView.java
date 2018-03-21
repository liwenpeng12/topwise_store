package com.topwise.topos.appstore.view;

import java.util.ArrayList;

import com.topwise.topos.appstore.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class ActionBarView extends LinearLayout {
    private ViewGroup mTabBarContainer;
    private LinearLayout mMenuContainer;
    private LinearLayout mTabContainer;
    private ImageView mBackBtn;
    private ImageView mCloseBtn;
    private TextView mTitleView;

    private View.OnClickListener mMenuClickListener;
    private View.OnClickListener mTabClickLisetener;
    private View.OnClickListener mBackBtnClickListener;
    private View.OnClickListener mCloseBtnClickListener;

    private ArrayList<ZTab> mTabs;
    private int mCurrentIndex = -1;

    private BackButtonClickListener mUserSetBackBtnClickListener;
    private CloseButtonClickListener mCloseButtonClickListener;
    private TopMenuClickListener mUserSetMenuClickListener;
    private TabClickListener mUserSetTabClickListener;


    public ActionBarView(Context context) {
        super(context);
    }

    public ActionBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActionBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        initViews();
    }


    private void initViews() {
        mBackBtnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUserSetBackBtnClickListener != null) {
                    mUserSetBackBtnClickListener.onBackBtnClicked(v);
                }
            }
        };

        mCloseBtnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCloseButtonClickListener != null) {
                    mCloseButtonClickListener.onCloseBtnClicked(v);
                }
            }
        };

        mMenuClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                TopMenu menu = (TopMenu) v.getTag();
                if (mUserSetMenuClickListener != null) {
                    mUserSetMenuClickListener.onTopMenuClicked(menu);
                }
            }
        };

        mTabClickLisetener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ZTab tab = (ZTab) v.getTag();
                setSelectedTab(tab.getIndex());
                if (mUserSetTabClickListener != null) {
                    mUserSetTabClickListener.onTabClicked(tab, tab.getIndex());
                }
            }
        };


        mBackBtn = (ImageView) findViewById(R.id.as_title_bar_back_btn);
        mBackBtn.setOnClickListener(mBackBtnClickListener);

        mCloseBtn = (ImageView) findViewById(R.id.as_title_bar_close_btn);
        mCloseBtn.setOnClickListener(mCloseBtnClickListener);

        mTitleView = (TextView) findViewById(R.id.as_title_bar_title_textview);
        mMenuContainer = (LinearLayout) findViewById(R.id.as_title_bar_menu_container);

        mTabBarContainer = (ViewGroup) findViewById(R.id.as_tab_bar_container);
        mTabContainer = (LinearLayout) findViewById(R.id.as_tab_bar_tabs_container);
    }

    public interface BackButtonClickListener {
        public void onBackBtnClicked(View v);
    }

    public void setOnBackButtonClickListener(BackButtonClickListener l) {
        mUserSetBackBtnClickListener = l;
    }

    public interface CloseButtonClickListener {
        public void onCloseBtnClicked(View v);
    }

    public void setOnCloseButtonClickListener(CloseButtonClickListener l) {
        mCloseButtonClickListener = l;
    }

    public void setCloseBtnVisibility(int visibility) {
        mCloseBtn.setVisibility(visibility);
    }

    public void setTitle(String title) {
        if (mTitleView != null) {
            mTitleView.setText(title);
        }
    }

    public void setTitle(int id) {
        String title = getResources().getString(id);
        setTitle(title);
    }

    public static class TopMenu {
        private Drawable mIcon;
        private Object mTag;
        private int mId;
        private String mText;

        public TopMenu(String text) {
            mText = text;
        }

        public TopMenu(Drawable icon) {
            mIcon = icon;
        }

        public void setId(int id) {
            mId = id;
        }

        public int getId() {
            return mId;
        }

        public void setTag(Object tag) {
            mTag = tag;
        }

        public Object getTag() {
            return mTag;
        }

        public void setText(String text) {
            mText = text;
        }

        public String getText() {
            return mText;
        }
    }

    public interface TopMenuClickListener {
        public void onTopMenuClicked(TopMenu menu);
    }

    public void addTopMenu(TopMenu menu) {
        if (menu != null) {
            View menuview = null;
            if (menu.mIcon != null) {
                ImageView v = new ImageView(getContext());
                v.setImageDrawable(menu.mIcon);
                v.setScaleType(ScaleType.CENTER);
                menuview = v;
            } else if (menu.mText != null) {
                TextView v = new TextView(getContext());
                v.setText(menu.mText);
                v.setTextColor(getResources().getColor(R.color.zkas_menu_item_text_color));
                v.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        getResources().getDimension(R.dimen.as_title_bar_menu_item_text_size));
                v.setPadding(getResources().getDimensionPixelSize(R.dimen.as_title_bar_horizontal_padding),
                        0, getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin), 0);
                menuview = v;
            }
            menuview.setOnClickListener(mMenuClickListener);
            menuview.setTag(menu);

            LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            menuview.setLayoutParams(lp);
            mMenuContainer.addView(menuview, lp);
        }
    }


    public void setPadding(TopMenu menu, int left, int top, int right, int bottom) {
        if (menu == null) {
            return;
        }
        int cnt = mMenuContainer.getChildCount();
        for (int i = 0; i < cnt; i++) {
            View menuview = mMenuContainer.getChildAt(i);
            TopMenu m = (TopMenu) menuview.getTag();
            if (m == menu) {
                menuview.setPadding(left, top, right, bottom);
                break;
            }
        }
    }

    public void hideTopMenu(TopMenu menu) {
        if (menu == null) {
            return;
        }
        int cnt = mMenuContainer.getChildCount();
        for (int i = 0; i < cnt; i++) {
            View menuview = mMenuContainer.getChildAt(i);
            TopMenu m = (TopMenu) menuview.getTag();
            if (m == menu) {
                menuview.setVisibility(View.GONE);
            }
        }
    }

    public void hideTopMenu(int id) {
        if (id == 0) {
            return;
        }

        int cnt = mMenuContainer.getChildCount();
        for (int i = 0; i < cnt; i++) {
            View menuview = mMenuContainer.getChildAt(i);
            TopMenu m = (TopMenu) menuview.getTag();
            if (m.getId() == id) {
                menuview.setVisibility(View.GONE);
            }
        }
    }

    public void showTopMenu(TopMenu menu) {
        if (menu == null) {
            return;
        }
        int cnt = mMenuContainer.getChildCount();
        for (int i = 0; i < cnt; i++) {
            View menuview = mMenuContainer.getChildAt(i);
            TopMenu m = (TopMenu) menuview.getTag();
            if (m == menu) {
                menuview.setVisibility(View.VISIBLE);
            }
        }
    }

    public void showTopMenu(int id) {
        if (id == 0) {
            return;
        }
        int cnt = mMenuContainer.getChildCount();
        for (int i = 0; i < cnt; i++) {
            View menuview = mMenuContainer.getChildAt(i);
            TopMenu m = (TopMenu) menuview.getTag();
            if (m.getId() == id) {
                menuview.setVisibility(View.VISIBLE);
            }
        }
    }

    public void hideAllTopMenus() {
        mMenuContainer.setVisibility(View.GONE);
    }

    public void showAllTopMenus() {
        mMenuContainer.setVisibility(View.VISIBLE);
    }

    public void setOnTopMenuClickListener(TopMenuClickListener l) {
        mUserSetMenuClickListener = l;
    }

    public static class ZTab {
        private String mTitle;
        private Object mTag;
        private int mIndex;

        public ZTab(String tab) {
            mTitle = tab;
        }

        public void setTag(Object tag) {
            mTag = tag;
        }

        public Object getTag() {
            return mTag;
        }

        public int getIndex() {
            return mIndex;
        }

        private void setIndex(int index) {
            mIndex = index;
        }
    }

    public int addTab(ZTab tab) {
        if (tab == null) {
            return -1;
        }

        if (mTabs == null) {
            mTabs = new ArrayList<ZTab>();
        }

        if (!mTabs.contains(tab)) {
            mTabBarContainer.setVisibility(View.VISIBLE);

            mTabs.add(tab);
            int index = mTabs.size() - 1;
            tab.setIndex(index);

            TextView tabview = new TextView(getContext());

            tabview.setText(tab.mTitle);
            tabview.setGravity(Gravity.CENTER);

            float size = getResources().getDimension(R.dimen.as_tab_bar_tab_text_size);
            tabview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
            int colors = getResources().getColor(R.color.as_tab_bar_tab_text_color_normal);
            tabview.setTextColor(colors);
            tabview.setOnClickListener(mTabClickLisetener);
            tabview.setTag(tab);

            LayoutParams lp = new LayoutParams(0, LayoutParams.MATCH_PARENT);
            lp.weight = 1;
            mTabContainer.addView(tabview, lp);
            requestLayout();

            return index;
        } else {
            return -1;
        }
    }

    public void hideAllTabs() {
        mTabBarContainer.setVisibility(View.GONE);
    }

    public void showAllTabs() {
        mTabBarContainer.setVisibility(View.VISIBLE);
    }

    public interface TabClickListener {
        public void onTabClicked(ZTab t, int index);
    }

    public void setOnTabClickListener(TabClickListener l) {
        mUserSetTabClickListener = l;
    }

    public boolean setSelectedTab(int index) {
        if (mTabs.size() <= index || index < 0) {
            return false;
        }

        if (mCurrentIndex != index) {
            int old = mCurrentIndex;
            mCurrentIndex = index;

            TextView tabview = (TextView) mTabContainer.getChildAt(index);
            tabview.setTextColor(getResources().getColor(R.color.as_tab_bar_tab_text_color_selected));
            tabview.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.as_tab_bar_tab_selected);

            if (old != -1 && old >= 0 && old < mTabs.size()) {
                TextView oldtabview = (TextView) mTabContainer.getChildAt(old);
                oldtabview.setTextColor(getResources().getColor(R.color.as_tab_bar_tab_text_color_normal));
                oldtabview.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.as_tab_bar_tab_normal);
            }
        }

        return true;
    }

    public ZTab getSelectedTab() {
        if (mTabs.size() <= mCurrentIndex || mCurrentIndex < 0) {
            return null;
        }

        return mTabs.get(mCurrentIndex);
    }

    public int getSelectedTabIndex() {
        return mCurrentIndex;
    }
}
