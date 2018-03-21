
package com.topwise.topos.appstore.view.activity;

import com.topwise.topos.appstore.manager.ActivityManager;
import com.topwise.topos.appstore.utils.Properties;
import com.topwise.topos.appstore.utils.Utils;
import com.topwise.topos.appstore.view.ActionBarView;
import com.topwise.topos.appstore.view.ActionBarView.BackButtonClickListener;
import com.topwise.topos.appstore.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SortActivity extends Activity implements OnClickListener {

    private ActionBarView mActionBarView;
    private LinearLayout mSoftwaveCategore;
    private LinearLayout mGameCategore;

    private static int[] TITLE_RES_SOFTWARE = {
            R.string.as_xitonggongju, R.string.as_zhutimeihua, 
            R.string.as_shejiaoliaotian, R.string.as_meitiyule,
            R.string.as_zixunyuedu, R.string.as_chuxinggouwu,
            R.string.as_shenghuozhushou, R.string.as_shiyonggongju,
            R.string.as_caijingtouzi, R.string.as_qita
    };

    private static int[] TITLE_RES_GAME = {
            R.string.as_zhangshangwangyou, R.string.as_xiuxianyouxi, 
            R.string.as_yizhiyouxi, R.string.as_qipaiyouxi, 
            R.string.as_tiyuyundong, R.string.as_dongzuosheji,
            R.string.as_qita
    };

    private static int[] SOFTWARETAG = {
            Properties.TYPE_APP_XITONGGONGJU,
            Properties.TYPE_APP_ZHUTIMEIHUA,
            Properties.TYPE_APP_SHEJIAOLIAOTIAN,
            Properties.TYPE_APP_MEITIYULE,
            Properties.TYPE_APP_ZIXUNYUEDU,
            Properties.TYPE_APP_CHUXINGGOUWU,
            Properties.TYPE_APP_SHENGHUAZHUSHOU,
            Properties.TYPE_APP_SHIYONGGONGJU,
            Properties.TYPE_APP_CAIJINGTOUZI,
            Properties.TYPE_APP_OTHER
    };

    private static int[] GAMETAG = {
            Properties.TYPE_GAME_ZHANGSHANGWANGYOU,
            Properties.TYPE_GAME_XIUXIANYOUXI,
            Properties.TYPE_GAME_YIZHIYOUXI,
            Properties.TYPE_GAME_QIPAIYOUXI,
            Properties.TYPE_GAME_TIYUYUNDONG,
            Properties.TYPE_GAME_DONGZUOSHEJI,
            Properties.TYPE_GAME_OTHER,
    };

    private LayoutInflater mInflater;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.addActivity(this);
        Utils.setWhiteStatusBar(this);
        setContentView(R.layout.zkas_sort_activity_layout);
        mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        iniViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.removeActivity(this);
    }

    private void iniViews() {
        mActionBarView = (ActionBarView) findViewById(R.id.as_action_bar_layout);
        mActionBarView.setTitle(getString(R.string.as_sort));
        mActionBarView.setOnBackButtonClickListener(new BackButtonClickListener() {
            @Override
            public void onBackBtnClicked(View v) {
                SortActivity.this.finish();
            }
        });

        mSoftwaveCategore = (LinearLayout) findViewById(R.id.zkas_id_category_left_area_container);
        mGameCategore = (LinearLayout) findViewById(R.id.zkas_id_category_right_area_container);

        addCategoryView();
    }

    private void addCategoryView() {
        for (int i = 0; i < SOFTWARETAG.length; i++) {
            addKeywordView(getString(TITLE_RES_SOFTWARE[i]), mSoftwaveCategore, SOFTWARETAG[i]);
        }

        for (int i = 0; i < GAMETAG.length; i++) {
            addKeywordView(getString(TITLE_RES_GAME[i]), mGameCategore, GAMETAG[i]);
        }
    }

    private void addKeywordView(String kw, ViewGroup parent, int type) {
        TextView t = (TextView) mInflater.inflate(R.layout.zkas_search_keyword_item_layout, parent,
                false);
        t.setText(kw);
        t.setOnClickListener(this);
        t.setTag(type);
        t.getLayoutParams().width = LayoutParams.MATCH_PARENT;
        parent.addView(t);
    }

    @Override
    public void onClick(View v) {
        String title = ((TextView) v).getText().toString();
        int typeid = (Integer) v.getTag();

        Intent intent = new Intent(this, GroupActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("type", "type");
        intent.putExtra("id", new int[] {
            typeid
        });
        startActivity(intent);
    }
}
