package com.topwise.topos.appstore.utils;

import java.io.File;
import android.graphics.Bitmap;

//这个类除了在内存里面的对象可以取出来以外,其他文件解码 和下载都要放到MyTask里面
public class BitmapUtil {
    private static final String TAG = "BitmapUtil";
    private ImageMemoryCache mMemoryCache;
    private ImageFileCache mFileCache;
    private AsynTaskManager mAsynTaskManager;
    private static BitmapUtil sInstance = null;
    public synchronized static BitmapUtil getInstance() {
        if (sInstance == null) {
            sInstance = new BitmapUtil();
        }
        return sInstance;
    }
    
    private BitmapUtil() {
        mMemoryCache = new ImageMemoryCache();
        mFileCache = new ImageFileCache();
        mAsynTaskManager = new AsynTaskManager(mMemoryCache, mFileCache);
    }
    
    public ImageFileCache getImageFileCache() {
        return mFileCache;
    }

    public Bitmap getBitmapAsync(String url, AsynTaskManager.ImageLoadCallBack callback) {
        return  getScaleBitmapAsync(url, callback, -1, -1);
    }
    public Bitmap getScaleBitmapAsync(String url, AsynTaskManager.ImageLoadCallBack callback, int width, int height) {
        if (url == null || url.isEmpty() || callback == null) {
            LogEx.d(TAG, "getScaleBitmapAsync(), url is empty! failed!");
            return null;
        }
        // 从内存缓存中获取图片
        String memoryCacheKey = url + "w=" +width+"h=" + height;
        Bitmap result = mMemoryCache.getBitmapFromCache(memoryCacheKey);
        if (result == null) {
            if (Runtime.getRuntime().totalMemory() > 512*1024*1024) {
                mMemoryCache.clearCache();
            }

            File cachefile = mFileCache.getCachedImageFile(url);
            if (cachefile != null) {
                mAsynTaskManager.pushTask(url, cachefile, callback,width,height);
                return null; 
            }

            // 从网络获取
            mAsynTaskManager.pushTask(url, callback, width, height);
        } 
        return result;
    }

    public Bitmap getBitmapAsyncFromZip(String url, AsynTaskManager.ImageLoadCallBack callback, File zipRes) {
        return  getScaleBitmapAsyncFromZip(url, callback, zipRes, -1, -1);
    }
    public Bitmap getScaleBitmapAsyncFromZip(String url, AsynTaskManager.ImageLoadCallBack callback, File zipRes, int scaleW, int scaleH) {
        if (url == null || url.isEmpty()) {
            LogEx.d(TAG, "getBitmapAsyncFromZip(), url is empty! failed!");
            return null;
        }
        int index = url.lastIndexOf('$');
        if (index == -1 || index == 0 || index == url.length() - 1) {
            LogEx.d(TAG, "getBitmapAsyncFromZip(), url error format! failed! url=" + url);
            return null;
        }
        // 从内存缓存中获取图片
        String memoryCacheKey = url + "w=" +scaleW+"h=" + scaleH;
        Bitmap result = mMemoryCache.getBitmapFromCache(memoryCacheKey);
        if (result == null) {
            if (Runtime.getRuntime().totalMemory() > 512*1024*1024) {
                mMemoryCache.clearCache();
            }

            // 从压缩包中获取图片
            mAsynTaskManager.pushTask(url, callback,zipRes,scaleW,scaleH);
        } 
        return result;
    }
    
    public Bitmap getBitmapAsyncFromFile(String url, AsynTaskManager.ImageLoadCallBack callback, File srcFile) {
        return getScaleBitmapAsyncFromFile(url, callback, srcFile, -1, -1);
    }
    public Bitmap getScaleBitmapAsyncFromFile(String url, AsynTaskManager.ImageLoadCallBack  callback, File srcFile, int scaleWidth, int scaleHeight) {
        if (url == null || url.isEmpty() || callback == null) {
            LogEx.d(TAG, "getBitmapAsyncFromFile(), url is empty! failed!");
            return null;
        }
        // 从内存缓存中获取图片
        String memoryCacheKey = url + "w=" +scaleWidth+"h=" + scaleHeight;
        Bitmap result = mMemoryCache.getBitmapFromCache(memoryCacheKey);
        if (result == null) {
            if (Runtime.getRuntime().totalMemory() > 512*1024*1024) {
                mMemoryCache.clearCache();
            }

            // 从文件中获取
            mAsynTaskManager.pushTask(url, srcFile, callback,scaleWidth,scaleHeight);
        }  
        return result;
    }

    public void recycleBitmap(String url) {
        recycleScaleBitmap(url, -1, -1);
    }

    public void recycleScaleBitmap(String url, int scaleWidth, int scaleHeight) {
        String memoryCacheKey = url + "w=" +scaleWidth+"h=" + scaleHeight;
        mMemoryCache.removeBitmapFromCache(memoryCacheKey);
    }
    
    public void clearCallerCallback(String caller) {
        mAsynTaskManager.cancleTask(caller);
    }
    
    public void clearCallerCallback(String url,String caller) {
        mAsynTaskManager.cancleTask(url,caller);
   }
    
    public boolean cleanSDcardCache() {
        return mFileCache.cleanCache();
    }
}
