package com.topwise.topos.appstore.view.fragment.GuidPageInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.topwise.topos.appstore.R;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.List;


public class GridViewAdapter extends BaseAdapter {
    private List<Model> mDatas;
    private LayoutInflater inflater;
    private ImageLoader imageLoader= ImageLoader.getInstance();;
    /**
     * 页数下标,从0开始(当前是第几页)
     */
    private int curIndex;
    /**
     * 每一页显示的个数
     */
    private int pageSize;

    public GridViewAdapter(Context context, List<Model> mDatas, int curIndex, int pageSize) {
        initImageLoader(context);
        this.mDatas = mDatas;
        this.pageSize = pageSize;
        this.curIndex = curIndex;
        inflater = LayoutInflater.from(context);

    }


    @Override
    public int getCount() {
        return mDatas.size() > (curIndex + 1) * pageSize ? pageSize : (mDatas.size() - curIndex * pageSize);
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position + curIndex * pageSize);
    }

    @Override
    public long getItemId(int position) {
        return position + curIndex * pageSize;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_gridview, null);
            vh = new ViewHolder();
            vh.iv = (ImageView) convertView.findViewById(R.id.imageView);
            vh.tv = (TextView) convertView.findViewById(R.id.textView);
            vh.StatusSelect=(CheckBox)convertView.findViewById(R.id.StatusSelect);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        //计算一下位置
        final int pos = position + curIndex*pageSize;
//        vh.iv.setImageResource(mDatas.get(pos).getIconRes());
//        vh.tv.setText(mDatas.get(pos).getName());
        vh.tv.setText(mDatas.get(pos).getAppInfo().name);
        imageLoader.displayImage(mDatas.get(pos).getAppInfo().icon_url,vh.iv);
        vh.StatusSelect.setChecked(mDatas.get(pos).isChecked());
        return convertView;
    }

    class ViewHolder {
        public TextView tv;
        public ImageView iv;
        public CheckBox StatusSelect;
    }
    private void initImageLoader(Context context) {
        // 创建DisplayImageOptions对象
        DisplayImageOptions defaulOptions = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).build();
        // 创建ImageLoaderConfiguration对象
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(defaulOptions)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO).build();
        // ImageLoader对象的配置
        ImageLoader.getInstance().init(configuration);
    }
}
