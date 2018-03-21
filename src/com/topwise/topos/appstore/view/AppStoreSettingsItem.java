package com.topwise.topos.appstore.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.topwise.topos.appstore.manager.SettingsManager;
import com.topwise.topos.appstore.R;
/**
 * @author xiaowenhui
 */
public class AppStoreSettingsItem extends FrameLayout implements  OnCheckedChangeListener{
	private LinearLayout mNameAndSummaryContainer;
	private TextView mSettingItemName;
	private TextView mSettingItemSummary;
	private CheckSwitchButton mCheckSwitchButton;
	private boolean mIsChecked = false;
	private View mDiverLiner;
	private Context mContext;
	private TextView mTextView;
	private View mItemTopShade;
	private Runnable mSkipRunnable;
	
	public AppStoreSettingsItem(Context context) {
		super(context);
		init(context);
	}

	public AppStoreSettingsItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public AppStoreSettingsItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context){
		mContext = context;
	}
	
	public void setItemNameAndSummary(String name,String summary) throws  Exception{
		if(name == null){
			throw new Exception("item name must have value,but not be null");
		}
		mSettingItemName.setText(name);
		if(summary == null){
			mSettingItemSummary.setVisibility(GONE);
		}
		mSettingItemSummary.setText(summary);
		initCheckStatus(name);
		invalidate();
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mNameAndSummaryContainer = (LinearLayout)findViewById(R.id.settings_item_name_summary);
		mSettingItemName = (TextView)findViewById(R.id.settings_item_name);
		mSettingItemSummary = (TextView)findViewById(R.id.settings_item_summary);
		mCheckSwitchButton =(CheckSwitchButton)findViewById(R.id.mCheckSwithcButton);
		mCheckSwitchButton.setOnCheckedChangeListener(this);
		mDiverLiner =  findViewById(R.id.diver_line);
		mTextView = (TextView)findViewById(R.id.setting_item_textView);
		mItemTopShade =  findViewById(R.id.zkas_item_top_shade);
	}

	public void setTextViewButtonOnclickListener(OnClickListener l){
	    mTextView.setOnClickListener(l);
	}
	
	public void hideDiverLine(){
	    if(mDiverLiner != null){
	        mDiverLiner.setVisibility(View.GONE);
	    }
	}
	
	public void showDiverLine(){
	    if(mDiverLiner != null){
            mDiverLiner.setVisibility(View.VISIBLE);
        }
	}
	
	public void hideTopShade(){
	    if(mItemTopShade != null){
	        mItemTopShade.setVisibility(View.GONE);
        }
	}
	
	public void showTopeShade(){
	    if(mItemTopShade != null){
            mItemTopShade.setVisibility(View.VISIBLE);
        }
	}
	
	public void enableTextView(int textId){
	    mTextView.setText(textId);
	    mTextView.setVisibility(View.VISIBLE);
	    mCheckSwitchButton.setVisibility(View.GONE);
	}
	
	private void initCheckStatus(String name){
		mIsChecked = SettingsManager
				.getInstance().getSwithCheckedValue(name);
		mCheckSwitchButton.setChecked(mIsChecked);
	}
	
	@Override
	public void onCheckedChanged(CompoundButton cp, boolean checked) {
		if(mSkipRunnable == null){
		    SettingsManager
            .getInstance().setSwitchCheckedValue(mSettingItemName.getText().toString(), checked);
		}
	}
	
	public void setSkipSetting(Runnable runnable){
	    mSkipRunnable = runnable;
	    mCheckSwitchButton.setClickRunnable(mSkipRunnable);
	}
	
	public void updateChecked(){
	    initCheckStatus(mSettingItemName.getText().toString());
	}
}
