package com.topwise.topos.appstore.view.activity;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Activity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.manager.ActivityManager;
import com.topwise.topos.appstore.utils.Properties;
import com.topwise.topos.appstore.view.ActionBarView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PackageRemoveActivity extends Activity{
    private ActionBarView mActionBarView;
    private List<PackageRemoveModel> PackageRemoveModels;
    private ListView packageList;
    private File packageFile;
    private PackageAdapter packageAdapter;
    private CheckBox totalcheckTrue;
    private Button remove_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_remove);
        ActivityManager.addActivity(this);
        packageList=(ListView)findViewById(R.id.packageList);
        remove_button=(Button) findViewById(R.id.remove_button);
        totalcheckTrue=(CheckBox) findViewById(R.id.totalcheckTrue);
        packageFile=new File(Properties.APP_PATH);
        mActionBarView = (ActionBarView) findViewById(R.id.as_action_bar_layout);
        mActionBarView.setTitle(R.string.topwise_board_page_rubish);
        mActionBarView.setOnBackButtonClickListener(new ActionBarView.BackButtonClickListener() {
            @Override
            public void onBackBtnClicked(View v) {
                finish();
            }
        });
        if(packageFile.exists()) {
            PackageRemoveModels=new ArrayList<PackageRemoveModel>();
            File[] files=packageFile.listFiles();
            for (int i = 0; i < files.length; i++) {
                if(files[i].getName().endsWith(".apk")){
                    PackageRemoveModel packageRemoveModel=new PackageRemoveModel();
                    packageRemoveModel.setDrawable(getApkIcon(PackageRemoveActivity.this,files[i].getAbsolutePath()));
                    packageRemoveModel.setIschecked(false);
                    packageRemoveModel.setFilePath(files[i].getAbsolutePath());
                    packageRemoveModel.setPackage_name(getPackageName(files[i].getAbsolutePath()));
                    packageRemoveModel.setPackage_size(Formatter.formatFileSize(PackageRemoveActivity.this,files[i].length()));
                    packageRemoveModel.setPackage_time(getTime(files[i].getAbsolutePath()));
                    PackageRemoveModels.add(packageRemoveModel);
                }

            }
        }

        if(PackageRemoveModels.size() != 0) {
            packageAdapter = new PackageAdapter(PackageRemoveActivity.this, PackageRemoveModels);
            packageList.setAdapter(packageAdapter);
        }

        totalcheckTrue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(PackageRemoveModels.size() != 0){
                        for(int k=0;k<PackageRemoveModels.size();k++){
                            PackageRemoveModels.get(k).setIschecked(true);
                        }
                        if(packageAdapter != null) packageAdapter.notifyDataSetChanged();
                    }
                }else {
                    if(PackageRemoveModels.size() != 0){
                        for(int k=0;k<PackageRemoveModels.size();k++){
                            PackageRemoveModels.get(k).setIschecked(false);
                        }
                        if(packageAdapter != null) packageAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        remove_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean packageIsDelete=false;
                if(PackageRemoveModels.size() != 0){
                    for(int k=PackageRemoveModels.size()-1;k>=0;k--){
                        Log.d("zr",PackageRemoveModels.get(k).ischecked()+"///size="+PackageRemoveModels.size()+"////k="+k);
                        if(PackageRemoveModels.get(k).ischecked()){
                            packageIsDelete=deletePackage(PackageRemoveModels.get(k).getFilePath());
                            if(packageIsDelete){
                                PackageRemoveModels.remove(k);
                                if(packageAdapter != null) packageAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                    if(packageAdapter != null) packageAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    //删除安装包
    public boolean deletePackage(String filePath){
        boolean isdelete=false;
        File packageFile=new File(filePath);
        if(packageFile.exists()){
            isdelete=packageFile.delete();
        }
        Log.d("zr",isdelete+"////isdelete:");
        return isdelete;
    }

    public String getTime(String apkPath){
        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd-HH mm:ss");
        return format2.format(new Date(new File(apkPath).lastModified()));

    }


    public Drawable getApkIcon(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath,PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            try {
                return appInfo.loadIcon(pm);
            } catch (OutOfMemoryError e) {
                Log.e("ApkIconLoader", e.toString());
            }
        }
        return null;
    }

    public String getPackageName(String FilePath) {
        String[] chafen=FilePath.split("/");
        String temp=chafen[chafen.length-1];
        String temp1=temp.substring(0, temp.lastIndexOf('.'));
        return temp1;
    }


    /**

     * 去除“第”之前的所有非汉字内容

     */

    private String clearNotChinese(String buff){
        String tmpString =buff.replaceAll("(?i)[^a-zA-Z0-9\u4E00-\u9FA5]", "");//去掉所有中英文符号
        char[] carr = tmpString.toCharArray();
        for(int i = 0; i<tmpString.length();i++){
            if(carr[i] < 0xFF){
                carr[i] = ' ' ;//过滤掉非汉字内容
            }
        }
        return String.copyValueOf(carr).trim();
    }


    private class PackageAdapter extends BaseAdapter{
        private LayoutInflater mInflater = null;
        private List<PackageRemoveModel> LPackageRemoveModels;
        private PackageAdapter(Context context,List<PackageRemoveModel> mPackageRemoveModels)
        {
            //根据context上下文加载布局，这里的是Demo17Activity本身，即this
            this.mInflater = LayoutInflater.from(context);
            LPackageRemoveModels=mPackageRemoveModels;
        }
        @Override
        public int getCount() {
            if(LPackageRemoveModels.size() != 0){
                return LPackageRemoveModels.size();
            }else {
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            return LPackageRemoveModels.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if(convertView == null)
            {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.topwise_package_remove_item, null);
                holder.mdrawable = (ImageView)convertView.findViewById(R.id.package_icon);
                holder.mpackage_name = (TextView)convertView.findViewById(R.id.package_name);
                holder.mpackage_time = (TextView)convertView.findViewById(R.id.package_time);
                holder.mpackage_size = (TextView)convertView.findViewById(R.id.package_size);
                holder.mischecked = (CheckBox) convertView.findViewById(R.id.package_checkbox);
                convertView.setTag(holder);
            }else
            {
                holder = (ViewHolder)convertView.getTag();
            }
            if(PackageRemoveModels.size() != 0) {
                Log.d("cvb",PackageRemoveModels.size()+"///position="+position);
                holder.mdrawable.setImageDrawable(LPackageRemoveModels.get(position).getDrawable());
                holder.mpackage_name.setText(LPackageRemoveModels.get(position).getPackage_name());
                holder.mpackage_time.setText(LPackageRemoveModels.get(position).getPackage_time());
                holder.mpackage_size.setText(LPackageRemoveModels.get(position).getPackage_size());
                holder.mischecked.setChecked(LPackageRemoveModels.get(position).ischecked);
                holder.mischecked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked){
                            LPackageRemoveModels.get(position).setIschecked(isChecked);
                        }else {
                            LPackageRemoveModels.get(position).setIschecked(isChecked);
                        }
                    }
                });
            }
            return convertView;
        }
    }


    public class ViewHolder{
        private ImageView mdrawable;
        private TextView mpackage_name;
        private TextView mpackage_time;
        private TextView mpackage_size;
        private CheckBox mischecked;
    }


    public class PackageRemoveModel{
        private Drawable drawable;
        private String package_name;
        private String package_time;
        private String package_size;
        private String filePath;
        private boolean ischecked;

        public Drawable getDrawable() {
            return drawable;
        }

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }

        public String getPackage_name() {
            return package_name;
        }

        public void setPackage_name(String package_name) {
            this.package_name = package_name;
        }

        public String getPackage_time() {
            return package_time;
        }

        public void setPackage_time(String package_time) {
            this.package_time = package_time;
        }

        public String getPackage_size() {
            return package_size;
        }

        public void setPackage_size(String package_size) {
            this.package_size = package_size;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public boolean ischecked() {
            return ischecked;
        }

        public void setIschecked(boolean ischecked) {
            this.ischecked = ischecked;
        }
    }
}
