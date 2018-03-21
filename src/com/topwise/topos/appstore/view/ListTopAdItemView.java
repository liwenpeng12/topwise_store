package com.topwise.topos.appstore.view;

import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.data.Label;
import com.topwise.topos.appstore.data.Rank;
import com.topwise.topos.appstore.utils.BitmapUtil;
import com.topwise.topos.appstore.utils.LogEx;
import com.topwise.topos.appstore.utils.AsynTaskManager.ImageLoadCallBack;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ListTopAdItemView extends LinearLayout {
    private static final String TAG = "ListAdItemView";
    private ImageView mAdImage;
    private TextView mDescription;
    
    private Object mData;
    
    private ImageLoadCallBack mImageLoadCallBack;
    private TopAdItemClickListener mTopAdItemClickListener;
    private boolean mIsAttached = false;
    
    public ListTopAdItemView(Context context) {
        this(context, null, 0);
    }
    
    public ListTopAdItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public ListTopAdItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        
        mImageLoadCallBack = new ImageLoadCallBack() {

            @Override
            public void onImageLoadSuccess(String imageUrl,Bitmap result) {
                if (!isAttached()) {
                    return;
                }
                
                ListTopAdItemView.this.updateImageWithUrl(imageUrl, result);
            }

            @Override
            public void onImageLoadFailed(String imageUrl, String reason) {
                LogEx.d(TAG, "imageUrl=" + imageUrl + ":reason=" + reason);
            }

            @Override
            public String getCaller() {
                return String.valueOf(ListTopAdItemView.this.hashCode());
            }

            @Override
            public boolean isNeedToDecode(String imageUrl) {
                if (!isAttached()) {
                    return false;
                }
                return ListTopAdItemView.this.checkUrl(imageUrl);
            }
        };
        
        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTopAdItemClickListener != null && mData != null) {
                    String url = getClickUrl();
                    String title = getTitle();
                    if (url != null) {
                        mTopAdItemClickListener.onTopAdItemClicked(url, title);
                    }
                }
            }
        });
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        
        mAdImage = (ImageView) findViewById(R.id.zkas_id_ad_image_view);
        mDescription = (TextView) findViewById(R.id.zkas_id_ad_descrip_view);
    }
    
    public void setData(Object data) {
        if (data == null) {
            return;
        }
        
        String imgurl = null;
        String descrip = null;
        String clickurl = null;
        if (data instanceof Label) {
            Label d = (Label) data;
            
            imgurl = d.img_url;
            descrip = d.adcontent;
            clickurl = d.adurl;
        } else if (data instanceof Rank) {
            Rank k = (Rank) data;
            
            imgurl = k.img_url;
            descrip = k.adcontent;
            clickurl = k.adurl;
        } else {
            return;
        }
        
        mData = data;
        
        if (isAttached()) {
            Bitmap bmp = BitmapUtil.getInstance().getBitmapAsync(imgurl, mImageLoadCallBack);
            if (bmp != null) {
                mAdImage.setImageBitmap(bmp);
            } else {
                mAdImage.setImageResource(R.drawable.as_default_top_banner);
            }
        }
        
        if (descrip != null && descrip.length() > 0) {
            mDescription.setVisibility(View.VISIBLE);
            mDescription.setText(descrip);
            if (clickurl != null && clickurl.length() > 0) {
                mDescription.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.zkas_icon_arrow, 0);
            } else {
                mDescription.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
        } else {
            mDescription.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mIsAttached = true;
        if (mData != null) {
            setData(mData);
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIsAttached = false;
        BitmapUtil.getInstance().clearCallerCallback(mImageLoadCallBack.getCaller());
        mData = null;
        
        mAdImage.setImageBitmap(null);
        mDescription.setText("");
    }
    
    public boolean isAttached() {
        return mIsAttached;
    }
    
    public void updateImageWithUrl(String imageUrl, Bitmap bmp) {
        if (!imageUrl.equals(getImageUrl())) {
            return;
        }
        
        if (bmp != null) {
            mAdImage.setImageBitmap(bmp);
        } else {
            mAdImage.setImageResource(R.drawable.as_default_top_banner);
        }
    }
    
    private String getImageUrl() {
        if (mData == null) {
            return null;
        }
        
        if (mData instanceof Label) {
            return ((Label) mData).img_url;
        } else if (mData instanceof Rank) {
            return ((Rank) mData).img_url;
        } else {
            return null;
        }
    }
    
    private String getClickUrl() {
        if (mData == null) {
            return null;
        }
        
        if (mData instanceof Label) {
            return ((Label) mData).adurl;
        } else if (mData instanceof Rank) {
            return ((Rank) mData).adurl;
        } else {
            return null;
        }
    }
    
    private String getTitle() {
        if (mData == null) {
            return null;
        }
        
        if (mData instanceof Label) {
            return ((Label) mData).title;
        } else if (mData instanceof Rank) {
            return ((Rank) mData).title;
        } else {
            return null;
        }
    }
    
    public boolean checkUrl(String imageurl) {
        if (mData != null) {
            return imageurl.equals(getImageUrl());
        }
        
        return false;
    }
    
    public interface TopAdItemClickListener {
        public void onTopAdItemClicked(String url, String title);
    }
    
    public void setTopAdItemClickListener(TopAdItemClickListener l) {
        mTopAdItemClickListener = l;
    }
}
