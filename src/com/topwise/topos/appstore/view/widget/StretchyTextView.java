package com.topwise.topos.appstore.view.widget;

import android.content.Context;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.topwise.topos.appstore.R;
public class StretchyTextView extends LinearLayout implements OnClickListener {
    //默认显示的最大行数  
    private static final int DEFAULT_MAX_LINE_COUNT = 2;  
    //当前展开标志显示的状态  
    private static final int SPREADTEXT_STATE_NONE = 0;
    private static final int SPREADTEXT_STATE_RETRACT = 1;  
    private static final int SPREADTEXT_STATE_EXPAND = 2;  
    private TextView mContentText; 
    private ImageView mStretchyButton;
  
    private int mState;  
    private boolean mFlag = false;  
    private int maxLineCount = DEFAULT_MAX_LINE_COUNT;
    private SwitchRunnable mSwitchRunnable;
    private Context mContext;
    public StretchyTextView(Context context) {
        this(context,null);
    }

    public StretchyTextView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public StretchyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    private void init(Context context){
        mContentText = (TextView) findViewById(R.id.content_textview);
        mStretchyButton = (ImageView) findViewById(R.id.stretchy_button);
        mStretchyButton.setOnClickListener(this);
        mSwitchRunnable = new SwitchRunnable();
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init(mContext);
    }
    public void setTextContent(CharSequence charSequence){
        if(mContentText != null){
            mContentText.setText(Html.fromHtml(charSequence.toString()));
            mContentText.setMovementMethod(ScrollingMovementMethod.getInstance());
            //mContentText.setText(charSequence);
            mFlag = false;
            mState = SPREADTEXT_STATE_EXPAND;
            requestLayout();
        }
    }
    
    @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            if(!mFlag){
                mFlag = !mFlag;
                if(mContentText.getLineCount() < DEFAULT_MAX_LINE_COUNT){
                    mState = SPREADTEXT_STATE_NONE;
                    mStretchyButton.setVisibility(View.GONE);
                    mContentText.setMaxLines(DEFAULT_MAX_LINE_COUNT + 1);
                } else {
                    post(mSwitchRunnable);
                }
                
            }
        }
    
    @Override
    public void onClick(View v) {
        mFlag = false;
        requestLayout();
    }

    private class SwitchRunnable implements Runnable {
        @Override  
        public void run() {  
            if (mState == SPREADTEXT_STATE_EXPAND) {
                mContentText.setMaxLines(maxLineCount);
                mStretchyButton.setVisibility(View.VISIBLE);
                mState = SPREADTEXT_STATE_RETRACT;
                mStretchyButton.setBackgroundResource(R.drawable.zkas_expand_bg);
            } else if (mState == SPREADTEXT_STATE_RETRACT) {
                mContentText.setMaxLines(Integer.MAX_VALUE);
                mStretchyButton.setVisibility(View.VISIBLE);
                mState = SPREADTEXT_STATE_EXPAND;
                mStretchyButton.setBackgroundResource(R.drawable.zkas_retract_bg);
            }  
        }  
    }
}
