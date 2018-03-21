package com.topwise.topos.appstore.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.topwise.topos.appstore.AppStoreWrapper;
import com.topwise.topos.appstore.AppStoreWrapperImpl;
import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.download.DownloadInfo;
import com.topwise.topos.appstore.download.DownloadManager;
import com.topwise.topos.appstore.utils.Properties;
import com.topwise.topos.appstore.view.activity.AboutActivity;
import com.topwise.topos.appstore.view.activity.BoardActivity;
import com.topwise.topos.appstore.view.activity.PackageRemoveActivity;
import com.topwise.topos.appstore.view.activity.SettingsActivity;
import com.topwise.topos.appstore.view.activity.StoreOptionActivity;

import java.io.File;

public class BoardFragment extends Fragment implements AppStoreWrapper.AppUpgradeCountListener,DownloadManager.DownloadObserver{
     private View rootView;
     private RelativeLayout board_download;
     private RelativeLayout board_rubish;
     private RelativeLayout board_app_update;
     private RelativeLayout board_app_remove;
     private RelativeLayout board_settings;
     private RelativeLayout board_about;
     private RelativeLayout theme_skins;
     private RelativeLayout shopping_option;
    private TextView download_task;
    private TextView download_update;
    private TextView board_download_package_count;
    private int count=0;//正在下载应用任务的数量
    private int AppPackageCount=0;//安装包的数量
    private File file;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView=LayoutInflater.from(getActivity()).inflate(R.layout.fragment_board,null);
        board_download=(RelativeLayout)rootView.findViewById(R.id.board_download);
        board_rubish=(RelativeLayout)rootView.findViewById(R.id.board_rubish);
        board_app_update=(RelativeLayout)rootView.findViewById(R.id.board_app_update);
        board_app_remove=(RelativeLayout)rootView.findViewById(R.id.board_app_remove);
        board_settings=(RelativeLayout)rootView.findViewById(R.id.board_settings);
        board_about=(RelativeLayout)rootView.findViewById(R.id.board_about);
        theme_skins=(RelativeLayout)rootView.findViewById(R.id.theme_skins);
        shopping_option=(RelativeLayout)rootView.findViewById(R.id.shopping_option);
        download_task=(TextView) rootView.findViewById(R.id.download_task);
        download_update=(TextView) rootView.findViewById(R.id.download_update);
        board_download_package_count=(TextView) rootView.findViewById(R.id.board_download_package_count);
        file=new File(Properties.APP_PATH);
        board_download.setOnClickListener(mylistener);
        board_rubish.setOnClickListener(mylistener);
        board_app_update.setOnClickListener(mylistener);
        board_app_remove.setOnClickListener(mylistener);
        board_settings.setOnClickListener(mylistener);
        board_about.setOnClickListener(mylistener);
//        theme_skins.setOnClickListener(mylistener);
        theme_skins.setVisibility(View.GONE);
        shopping_option.setOnClickListener(mylistener);
        AppStoreWrapperImpl.registerAppUpgradeCountListener(this);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        AppPackageCount=getAppPackageCount();
        board_download_package_count.setText(AppPackageCount+getResources().getString(R.string.topwise_board_page_clear));
        count=DownloadManager.getInstance().mDownloadDBProvider.mDownloadingJobs.size();
        download_task.setText(getActivity().getResources().getString(R.string.topwise_board_page_download1)+count+getActivity().getResources().getString(R.string.topwise_board_page_download2));
        DownloadManager.getInstance().registerDownloadObserver(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        DownloadManager.getInstance().unregisterDownloadObserver(this);
    }

    private View.OnClickListener mylistener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          switch (v.getId()){
              case R.id.board_download:
                  getActivity().startActivity(new Intent(getContext(),BoardActivity.class).putExtra("download",0));
                  break;
              case R.id.board_rubish:
                  getActivity().startActivity(new Intent(getContext(),PackageRemoveActivity.class));
                  break;
              case R.id.board_app_update:
                  getActivity().startActivity(new Intent(getContext(),BoardActivity.class).putExtra("download",1));
                  break;
              case R.id.board_app_remove:
                  Toast.makeText(getActivity(),getResources().getString(R.string.topwise_board_page_no_model),Toast.LENGTH_SHORT).show();
                  break;
              case R.id.board_settings:
                  startActivity(new Intent(getActivity(), SettingsActivity.class));
                  break;
              case R.id.board_about:
                  startActivity(new Intent(getActivity(),AboutActivity.class));
                  break;
              case R.id.theme_skins:
                  Toast.makeText(getActivity(),getResources().getString(R.string.topwise_board_page_no_model),Toast.LENGTH_SHORT).show();
                  break;
              case R.id.shopping_option:
                  getActivity().startActivity(new Intent(getActivity(),StoreOptionActivity.class));
                  break;
          }
        }
    };

    @Override
    public void appUpgradeCount(int count) {
        download_update.setText(count+getActivity().getResources().getString(R.string.topwise_board_page_update1));
    }

    @Override
    public void onDownloadChanged(DownloadInfo info) {
        count=DownloadManager.getInstance().mDownloadDBProvider.mDownloadingJobs.size();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                download_task.setText(getActivity().getResources().getString(R.string.topwise_board_page_download1)+count+getActivity().getResources().getString(R.string.topwise_board_page_download2));
            }
        });
    }
    public int getAppPackageCount(){
        int AppCount=0;
        if(file.exists()){
            File[] files=file.listFiles();
            for(int o=0;o<files.length;o++){
               if(files[o].getName().endsWith(".apk")){
                   AppCount++;
               }
            }
        }
        return AppCount;
    }
}
