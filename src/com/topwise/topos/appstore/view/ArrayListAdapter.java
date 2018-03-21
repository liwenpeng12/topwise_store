package com.topwise.topos.appstore.view;

import java.util.ArrayList;

import android.app.Fragment;
import android.content.Context;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;

public abstract class ArrayListAdapter<T> extends BaseAdapter {
    
    protected ArrayList<T> mList;
    protected Fragment mFragment;
    protected Context  mContext;
    
    protected int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;
    private int firstVisibleItem;
    private int visibleItemCount;
    private int totalItemCount;
    
    public ArrayListAdapter(Fragment fragment){
        mFragment = fragment;
        mContext = fragment.getActivity();
    }
    
    public ArrayListAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        if(mList != null)
            return mList.size();
        else
            return 0;
    }

    @Override
    public Object getItem(int position) {
        //return mList == null ? null : mList.get(position);
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    
    public void setList(ArrayList<T> list){
        this.mList = list;
        notifyDataSetChanged();
    }
    
    public ArrayList<T> getList(){
        if(mList==null){
            mList = new ArrayList<T>();
        }
        return mList;
    }
    
    public void setList(T[] list){
        ArrayList<T> arrayList = new ArrayList<T>(list.length);  
        for (T t : list) {  
            arrayList.add(t);  
        }  
        setList(arrayList);
    }
    
    
    public void removeAll(ArrayList<T> objs) {
        if (mList == null) {
            return;
        }
        for (T o : objs) {
            mList.remove(o);
        }
    }

    public void setScrollState(int scrolllState) {
        mScrollState = scrolllState;
    }

    public void setScrollPosition(int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        this.firstVisibleItem = firstVisibleItem;
        this.visibleItemCount = visibleItemCount;
        this.totalItemCount = totalItemCount;
    }
}
