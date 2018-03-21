package com.topwise.topos.appstore.view;

import java.text.DecimalFormat;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.topwise.topos.appstore.data.AppInfo;
import com.topwise.topos.appstore.data.AppInfo.Tag;
import com.topwise.topos.appstore.utils.BitmapUtil;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.utils.AsynTaskManager.ImageLoadCallBack;
import com.topwise.topos.appstore.view.activity.AppDetailActivity;
import com.topwise.topos.appstore.view.activity.H5Activity;
import com.topwise.topos.appstore.R;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class ListMainItemView extends LinearLayout {
    private static final String TAG = "ListMainItemView";

    private ListAppItemView mAppItemView;
    private ImageView mPreviewImage;
    private LinearLayout mTextLinkContainer;
    private TextView mTextLinkTextView;
    private AppItemClickListener mAppItemClickListener;
    private AppInfo mAppInfo;
    private ImageLoadCallBack mImageLoadCallBack; 
    private OnClickListener mTextLinkClickListener;

    public static final double KB_SIZE = 1024;
    public static final double MB_SIZE = 1024 * 1024;
    public static final double GB_SIZE = 1024 * 1024 * 1024;
    private boolean mIsAttached = false;
    
    public ListMainItemView(Context context) {
        this(context, null, 0);
    }
    public ListMainItemView(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }
    public ListMainItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mImageLoadCallBack = new ImageLoadCallBack() {

            @Override
            public void onImageLoadSuccess(String imageUrl,Bitmap result) {
                if (!isAttached()) {
                    return;
                }
                
                ListMainItemView.this.updateImageWithUrl(imageUrl, result);
            }

            @Override
            public void onImageLoadFailed(String imageUrl, String reason) {
                LogEx.d(TAG, "imageUrl=" + imageUrl + ":reason=" + reason);
            }

            @Override
            public String getCaller() {
                return String.valueOf(ListMainItemView.this.hashCode());
            }

            @Override
            public boolean isNeedToDecode(String imageUrl) {
                if (!isAttached()) {
                    return false;
                }
                return ListMainItemView.this.checkUrl(imageUrl);
            }
        };
        
        setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (mAppItemClickListener != null && mAppInfo != null) {
                    mAppItemClickListener.onAppItemClicked(mAppInfo);
                } else if (mAppInfo != null) {
                    Intent intent = new Intent(v.getContext(), AppDetailActivity.class);
                    intent.putExtra("app_id", mAppInfo.id);
                    v.getContext().startActivity(intent);
                }
            }
        });
        
        mTextLinkClickListener = new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (mAppInfo != null && mAppInfo.adurl != null && mAppInfo.adurl.length() > 0) {
                    Intent intent = new Intent(v.getContext(), H5Activity.class);
                    intent.putExtra("url", mAppInfo.adurl);
                    v.getContext().startActivity(intent);
                }
            }
        };
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        
        mAppItemView = (ListAppItemView) findViewById(R.id.zkas_id_list_item_app_container);
        mPreviewImage = (ImageView) findViewById(R.id.zkas_id_list_item_preview_image);
        mTextLinkContainer = (LinearLayout) findViewById(R.id.zkas_id_list_item_text_link_container);
        mTextLinkTextView = (TextView) findViewById(R.id.zkas_id_list_item_text_link_text);
        
        findViewById(R.id.zkas_id_app_item_btm_divider).setVisibility(View.GONE);
    }
    
    public void setAppInfo(AppInfo info) {
        mAppInfo = info;
        if (isAttached() && mAppInfo != null) {
            mAppItemView.setAppInfo(mAppInfo);
            
            if (mAppInfo.adimage != null && mAppInfo.adimage.length() > 0) {
                Bitmap bmp = BitmapUtil.getInstance().getBitmapAsync(mAppInfo.adimage, mImageLoadCallBack);
                if (bmp == null) {
                    
                }
                setPreviewImage(bmp);
            } else {
                hidePreviewArea();
            }
            
            if (mAppInfo.adcontent != null && mAppInfo.adcontent.length() > 0) {
                setTextLinkText(mAppInfo.adcontent);
                setTextLinkOnClickListener(mTextLinkClickListener);
            } else {
                hideTextLinkArea();
            }
        }
    }
    
    public AppInfo getAppInfo() {
        return mAppInfo;
    }
    
    public static String transApkSize(String size, boolean is360Sdk) {
        try {
            DecimalFormat df = new DecimalFormat("#.00");
            double bytesize = Double.valueOf(size);
            if (is360Sdk) {
                bytesize = bytesize / 1000;
            }
            if (bytesize > GB_SIZE) {
                double s = bytesize / GB_SIZE;
                return df.format(s) + "TB";
            } else if (bytesize > MB_SIZE) {
                double s = bytesize / MB_SIZE;
                return df.format(s) + "GB";
            } else if (bytesize > KB_SIZE) {
                double s = bytesize / KB_SIZE;
                return df.format(s) + "MB";
            } else {
                return size + "KB";
            }
        } catch (Exception e) {
            return "";
        }
    }
    
    public void setAppIcon(Bitmap bmp) {
        mAppItemView.setAppIcon(bmp);
    }
    
    public void setAppName(String name) {
        mAppItemView.setAppName(name);
    }
    
    public void setAppInformation(String info) {
        mAppItemView.setAppInformation(info);
    }
    
    public void addAppTag(AppInfo.Tag tag) {
        mAppItemView.addAppTag(tag);
    }
    
    public void cleanAllAppTags() {
        mAppItemView.cleanAllAppTags();
    }
    
    public void setProgressButtonString(String text) {
        mAppItemView.setProgressButtonString(text);
    }
    
    public void setProgressButtonProgress(int progress) {
        mAppItemView.setProgressButtonProgress(progress);
    }
    
    public void setProgressButtonMax(int max) {
        mAppItemView.setProgressButtonMax(max);
    }
    
    public void setProgressButtonOnClickListener(OnClickListener l) {
        mAppItemView.setProgressButtonOnClickListener(l);
    }
    
    public void resetProgressButton() {
        mAppItemView.resetProgressButton();
    }
    
    public void setFrom(String from) {
        mAppItemView.setFrom(from);
    }
    
    public void setPreviewImage(Bitmap image) {
        if (image != null) {
            mPreviewImage.setImageBitmap(image);
        } else {
            mPreviewImage.setImageResource(R.drawable.zkas_app_default_preview);
        }
        mPreviewImage.setVisibility(View.VISIBLE);
    }
    
    public void hidePreviewArea() {
        mPreviewImage.setImageBitmap(null);
        mPreviewImage.setVisibility(View.GONE);
    }
    
    public void setTextLinkTag(Tag tag) {
        if (tag == null) {
            return;
        }
        
        boolean finded = false;
        AppTagView tagview = null;
        int cnt = mTextLinkContainer.getChildCount();
        for (int i = cnt - 1; i >= 0; i--) {
            View v = mTextLinkContainer.getChildAt(i);
            if (v instanceof AppTagView) {
                tagview = (AppTagView) v;
                finded = true;
                break;
            }
        }
        
        if (tagview == null) {
            tagview = new AppTagView(getContext());
        }
        tagview.setText(tag.name);
        tagview.setBackgroundColor(tag.bgcolor);
        
        if (!finded) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            tagview.setLayoutParams(lp);
            mTextLinkContainer.addView(tagview, 0);
        }
    }
    
    public void setTextLinkText(String text) {
        mTextLinkTextView.setText(text);
        mTextLinkContainer.setVisibility(View.VISIBLE);
    }
    
    public void setTextLinkOnClickListener(OnClickListener l) {
        mTextLinkContainer.setOnClickListener(l);
    }
    
    public void hideTextLinkArea() {
        mTextLinkContainer.setVisibility(View.GONE);
    }
    
    
    public void updateImageWithUrl(String url, Bitmap bmp) {
        if (url == null || url.length() == 0 || mAppInfo == null || bmp == null) {
            return;
        }
        
        if (url.equals(mAppInfo.adimage)) {
            mPreviewImage.setImageBitmap(bmp);
        }
    }

    public boolean checkUrl(String url) {
        if (url == null || url.length() == 0 || mAppInfo == null) {
            return false;
        }
        
        if (url.equals(mAppInfo.adimage)) {
            return true;
        }
        return false;
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mIsAttached = true;
        if (mAppInfo != null) {
            setAppInfo(mAppInfo);
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIsAttached = false;
        BitmapUtil.getInstance().clearCallerCallback(mImageLoadCallBack.getCaller());
        mPreviewImage.setImageResource(R.drawable.zkas_app_default_preview);
        hidePreviewArea();
        hideTextLinkArea();
    }
    
    public boolean isAttached() {
        return mIsAttached;
    }
    
    public interface AppItemClickListener {
        public void onAppItemClicked(AppInfo info);
    }
    
    public void setOnAppItemClickListener(AppItemClickListener l) {
        mAppItemClickListener = l;
        mAppItemView.setOnAppItemClickListener(l);
    }
    
    public void setDividerVisibility(boolean visible) {
        findViewById(R.id.zkas_id_main_item_btm_divider).setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }
}
