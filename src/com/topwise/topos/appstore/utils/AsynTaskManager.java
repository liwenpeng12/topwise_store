package com.topwise.topos.appstore.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Handler;
import android.os.Looper;

public class AsynTaskManager {
    private static final String TAG = "AsynTaskManager";
    /**
     * 处理本地任务的线程个数
     */
    
    private static final int TYPE_LOACAL = 0;
    private static final int TYPE_NETWORK = 1;
    
    private static final int FILE_CACHE_AND_ZIPFILE_THREAD_COUNT = 4;
    private static final int DOWNLOAD_THREAD_COUNT = 2;
    
    private ArrayList<MyTask> mAllTasks = new ArrayList<AsynTaskManager.MyTask>();
    private ImageMemoryCache mMemoryCache;
    private ImageFileCache mFileCache;
    /**
     * 主线程handler，用来post回调任务的
     */
    private Handler mMainHandler;

    private int mSaveBmpFileRepeatTimes = 0;
    
    public AsynTaskManager(ImageMemoryCache mmcache, ImageFileCache filecache) {
        mMemoryCache = mmcache;
        mFileCache = filecache;
        mMainHandler = new Handler();
        for(int i = 0;i < FILE_CACHE_AND_ZIPFILE_THREAD_COUNT; i++){
            new TaskThread(TYPE_LOACAL,"TASKTHREAD_FILE").start();
        }
        for(int i = 0;i<DOWNLOAD_THREAD_COUNT;i++){
            new TaskThread(TYPE_NETWORK,"TASKTHREAD_DOWNLOAD").start();
        }
    }
    /**
     * 取消任务
     * @param url
     * @param caller
     */
    public void cancleTask(String url,String caller){
        synchronized (mAllTasks) {
            for(int i = mAllTasks.size() -1;i >= 0;i--){
                MyTask myTask = mAllTasks.get(i);
                if(!myTask.url.equals(url)) {
                    continue;
                }
                for(int k = myTask.callBack.size() - 1;k >= 0;k--){
                    SoftReference<ImageLoadCallBack> softCallback = myTask.callBack.get(k);
                    ImageLoadCallBack imageLoadCallBack = softCallback.get();
                    //引用失效的任务顺便干掉//
                    if(imageLoadCallBack == null || imageLoadCallBack.getCaller().equals(caller)){
                        myTask.callBack.remove(softCallback);
                    }
                }
                if(myTask.callBack.size() == 0){
                    mAllTasks.remove(i);
                }
            }
        }
    }
    
    public void cancleTask(String caller){
        synchronized (mAllTasks) {
            for(int i = mAllTasks.size() -1;i >= 0;i--){
                MyTask myTask = mAllTasks.get(i);
                for(int k = myTask.callBack.size() - 1;k >= 0;k--){
                    SoftReference<ImageLoadCallBack> softCallback = myTask.callBack.get(k);
                    ImageLoadCallBack imageLoadCallBack = softCallback.get();
                    if(imageLoadCallBack == null || imageLoadCallBack.getCaller().equals(caller)){
                        myTask.callBack.remove(softCallback);
                    }
                }
                if(myTask.callBack.size() == 0){
                    mAllTasks.remove(i);
                }
            }
        }
    }
    
    private class TaskThread extends Thread{
        private boolean postMainFinish = false;
        private final Object postMainFinishMux = new Object();
        private int taskType;
        boolean needDecode;
        private Handler mainHandler = new Handler(Looper.getMainLooper());
        public TaskThread(int type,String name){
            super(name);
            taskType = type;
        }
        @Override
        public void run() {
            MyTask task = null;
            while(true){
                synchronized (mAllTasks) {//
                    task = getTask(taskType);
                    if(task == null){
                        try {
                            mAllTasks.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } 
                        continue;
                    }
                }
                
                boolean success = task.downLoadImage();
                if(!success){
                    final MyTask mtk = task;
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            for(SoftReference<ImageLoadCallBack> softCallback:mtk.callBack){
                                ImageLoadCallBack imageLoadCallBack = softCallback.get();
                                if(imageLoadCallBack != null){
                                    imageLoadCallBack.onImageLoadFailed(mtk.url, "downLoadImage failed!");
                                }
                            }
                        }
                    });
                   continue;
                }
                
                /*for(SoftReference<ImageLoadCallBack> softCallback:task.callBack){
                    needDecode = softCallback.get().isNeedToDecode(task.url);
                    if(needDecode){
                        break;
                    }
                }*/
                final MyTask finalTask = task;
                postMainFinish = false;
                needDecode = false;
                mainHandler.postAtFrontOfQueue(new Runnable() {
                    @Override
                    public void run() {
                        for(SoftReference<ImageLoadCallBack> softCallback:finalTask.callBack){
                            ImageLoadCallBack imageLoadCallBack = softCallback.get();
                            if(imageLoadCallBack == null){
                                continue;
                            }
                            needDecode = imageLoadCallBack.isNeedToDecode(finalTask.url);
                            if(needDecode){
                                break;
                            }
                        }
                        synchronized (postMainFinishMux) {
                            postMainFinish = true;
                            postMainFinishMux.notify();
                        }
                    }
                });
                
                synchronized (postMainFinishMux) {
                    while(postMainFinish == false) {
                        try {
                            postMainFinishMux.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                
                if(needDecode){
                    task.decodeBitmap();
                }  
            }
            
        }
        
        private MyTask getTask(int taskType){
            synchronized (mAllTasks) {
                int length = mAllTasks.size(); 
                for(int i = length - 1;i >= 0;i--){
                    MyTask task = mAllTasks.get(i);
                    if(task.type == taskType){
                        mAllTasks.remove(i);
                        return task;
                    }
                } 
                return null;
            }
        }
    }
    
    private class MyTask{
        public String url;
        private int type;
        //注意,当前callBack仅在主线城中被使用,如果在多线程中使用,一定要做同步//
        private ArrayList<SoftReference<ImageLoadCallBack>> callBack = new ArrayList<SoftReference<ImageLoadCallBack>>();
        private File zipRes;
        private File cacheFile;
        private int scaleWidth;
        private int scaleHeight;
        public MyTask(String taskUrl,int taskType,ImageLoadCallBack callbackTask,
                File zipFile ,File cFile ,int width,int height){
            url = taskUrl;
            type = taskType;
            SoftReference<ImageLoadCallBack> softCallback 
                = new SoftReference<AsynTaskManager.ImageLoadCallBack>(callbackTask);
            callBack.add(softCallback);
            zipRes = zipFile;
            scaleWidth = width;
            scaleHeight = height;
            cacheFile = cFile;
        }
        
        public boolean downLoadImage(){
            if(type == TYPE_LOACAL){
                if(zipRes != null){
                    if(!zipRes.exists()){
                        return false;
                    }
                } 
                if(cacheFile != null){
                    if(!cacheFile.exists() ){
                        if(url.startsWith("http")) {
                            return getBitmapWithUrlAndSaveToFile(url); 
                        } else {
                            return false;
                        }
                    }
                }
                return true;
            } else {
                return getBitmapWithUrlAndSaveToFile(url);
            }
        }
        
        public void decodeBitmap(){
            if(type == TYPE_LOACAL){
                if(zipRes != null){
                    loadBitmapFromZipFile(url, zipRes, scaleWidth, scaleHeight);  
                } else {
                    loadImageFromFile(url, cacheFile, scaleWidth, scaleHeight);
                }
            } else {
                cacheFile = mFileCache.getCachedImageFile(url);
                loadImageFromFile(url, cacheFile, scaleWidth, scaleHeight);
            }
            
        }
        
        /**
         * 从缓存文件中获取bitmap
         */
        private  void loadImageFromFile(final String path,  File srcfile, int scaleWidth, int scaleHeight) {
            if (path == null || path.isEmpty()) {
                return;
            }
            Bitmap bmp = null;
            try {
                String filepath = null;
                if (srcfile != null && srcfile.exists()) {
                    filepath = srcfile.getAbsolutePath();
                } else {
                    filepath = path;
                }

                bmp = getScaleBitmapFromFile(filepath, scaleWidth, scaleWidth);
            } catch (final Throwable e) {
                e.printStackTrace();
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for(int i = 0;i<callBack.size();i++){
                            ImageLoadCallBack imageLoadCallBack = callBack.get(i).get();
                            if(imageLoadCallBack != null) {
                                imageLoadCallBack.onImageLoadFailed(url,e.toString());
                            }
                        }
                    }
                });
            }
            final Bitmap result =  bmp;  
            if (result != null) {
                String memoryCacheKey = url + "w=" +scaleWidth+"h=" + scaleHeight;
                mMemoryCache.addBitmapToCache(memoryCacheKey, result);
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for(int i = 0;i<callBack.size();i++){
                            ImageLoadCallBack imageLoadCallBack = callBack.get(i).get();
                            if(imageLoadCallBack != null) {
                                imageLoadCallBack.onImageLoadSuccess(url,result);
                            }
                        }
                    }
                });
            } else {
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for(int i = 0;i<callBack.size();i++){
                            ImageLoadCallBack imageLoadCallBack = callBack.get(i).get();
                            if(imageLoadCallBack != null) {
                                imageLoadCallBack.onImageLoadFailed(url,"load bitmap from filecache failed");
                            }
                        }
                    }
                });
            }
        }
        
        private void loadBitmapFromZipFile(final String url,File zipRes,int scaleWidth,int scaleHeight){
            Bitmap bmp = null;
            int len = url.length();
            int index = url.lastIndexOf('$');
            if (index > 0 && index < len - 1) {
                String filePathInZip = url.substring(index + 1);

                if (filePathInZip.startsWith("/")) {
                    filePathInZip = filePathInZip.substring(1);
                }
                bmp = getBitmapFromZip(filePathInZip, zipRes, scaleWidth, scaleHeight);
            }

            final Bitmap result =  bmp;  
            if (result != null) {
                String memoryCacheKey = url + "w=" +scaleWidth+"h=" + scaleHeight;
                mMemoryCache.addBitmapToCache(memoryCacheKey, result);
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for(int i = 0;i < callBack.size();i++){
                            ImageLoadCallBack imageLoadCallBack = callBack.get(i).get();
                            if(imageLoadCallBack != null) {
                                imageLoadCallBack.onImageLoadSuccess(url,result);
                            }
                        }
                    }
                });
            } else {
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for(int i = 0;i<callBack.size();i++){
                            ImageLoadCallBack imageLoadCallBack = callBack.get(i).get();
                            if(imageLoadCallBack != null) {
                                imageLoadCallBack.onImageLoadFailed(url,"load bitmap from ZipFile failed");
                            }
                        }
                    }
                });
            }
        }
        
        private Bitmap getBitmapFromZip(String filepath, File zipRes, int scaleW, int scaleH) {
            LogEx.d(TAG, "getBitmapFromZip(), path=" + filepath + ",zipfile=" + zipRes.getName() + ",scaleW=" + scaleW + ",scaleH=" + scaleH);
            ZipFile zipFile = null;
            InputStream bis = null;
            ZipInputStream zis = null;
            InputStream ip = null;
            Bitmap bmp = null;
            try {
                zipFile = new ZipFile(zipRes);
                bis = new BufferedInputStream(new FileInputStream(zipRes));
                zis = new ZipInputStream(bis);
                ZipEntry ze = zipFile.getEntry(filepath);
                
                if (ze != null) {
                    ip = zipFile.getInputStream(ze);
                    
                    if (scaleW > 0 && scaleH > 0) {
                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inSampleSize = 1;
                        opts.inJustDecodeBounds = true;
                        
                        BitmapFactory.decodeStream(ip, null, opts);
                        ip.close(); //zip包解析出来的流只能用一次，不能reset()，因此，真的解压Bitmap时再get一次。
                        ip = null;
                        
                        final int minSideLength = Math.min(scaleW, scaleH);
                        opts.inSampleSize = computeSampleSize(opts, minSideLength, scaleW * scaleH);
                        opts.inJustDecodeBounds = false;
                        opts.inInputShareable = true;
                        opts.inPurgeable = true;

                        ip = zipFile.getInputStream(ze);
                        bmp = BitmapFactory.decodeStream(ip, null, opts);
                    } else {
                        bmp = BitmapFactory.decodeStream(ip, null, null);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    bis.close();
                } catch (Exception e) {
                }
                try {
                    ip.close();
                } catch (Exception e) {
                }
                try {
                    zis.closeEntry();
                } catch (Exception e) {
                }
                try {
                    zis.close();
                } catch (Exception e) {
                }
                try {
                    zipFile.close();
                } catch (Exception e) {
                }
            }
            return bmp;
        }
        
        private Bitmap getScaleBitmapFromFile(String filepath, int scaleW, int scaleH) {
            LogEx.d(TAG, "getScaleBitmapFromFile(), path=" + filepath + ",scaleW=" + scaleW + ",scaleH=" + scaleH);
            Bitmap bmp = null;
            if (scaleW > 0 && scaleH > 0) {
                Options opts = new Options();
                opts.inJustDecodeBounds = true;

                BitmapFactory.decodeFile(filepath, opts);

                final int minSideLength = Math.min(scaleW, scaleH);
                opts.inSampleSize = computeSampleSize(opts, minSideLength, scaleW * scaleH);
                opts.inJustDecodeBounds = false;
                opts.inInputShareable = true;
                opts.inPurgeable = true;

                bmp = BitmapFactory.decodeFile(filepath, opts);
            } else {
                bmp = BitmapFactory.decodeFile(filepath);
            }
            return bmp;
        }
        
        
        private  int computeSampleSize(BitmapFactory.Options options,
                int minSideLength, int maxNumOfPixels) {
            int initialSize = computeInitialSampleSize(options, minSideLength,
                    maxNumOfPixels);

            int roundedSize;
            if (initialSize <= 8) {
                roundedSize = 1;
                while (roundedSize < initialSize) {
                    roundedSize <<= 1;
                }
            } else {
                roundedSize = (initialSize + 7) / 8 * 8;
            }

            return roundedSize;
        }
        
        private int computeInitialSampleSize(BitmapFactory.Options options,
                int minSideLength, int maxNumOfPixels) {
            double w = options.outWidth;
            double h = options.outHeight;

            int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
                    .sqrt(w * h / maxNumOfPixels));
            int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math
                    .floor(w / minSideLength), Math.floor(h / minSideLength));

            if (upperBound < lowerBound) {
                // return the larger one when there is no overlapping zone.
                return lowerBound;
            }

            if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
                return 1;
            } else if (minSideLength == -1) {
                return lowerBound;
            } else {
                return upperBound;
            }
        }
        
        private boolean getBitmapWithUrlAndSaveToFile(String url) {
            LogEx.d(TAG, "getBitmapWithUrlAndSaveToFile(), url=" + url);
            File tmpfile = null;
            FileOutputStream fos = null;
            String savepath = mFileCache.getCachedImageFilePath(url);
            try {
                String tmppath = savepath + ".tmp";
                tmpfile = new File(tmppath);
                if (!tmpfile.exists()) {
                    tmpfile.createNewFile();
                } else {
                    tmpfile.delete();
                }
                
                fos = new FileOutputStream(tmppath);
            } catch (Exception e) {
                if (tmpfile != null) {
                    tmpfile.delete();
                }
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                return false;
            } 
            
            HttpURLConnection conn = null;
            InputStream is = null;
            try {
                URL u = new URL(url);
                conn = (HttpURLConnection) u.openConnection();
                is = conn.getInputStream();
                int length = -1;
                length = conn.getContentLength();
                
                int savecnt = 0; 
                final int defaultBufsize = 1024 * 4;
                byte[] buffer = new byte[defaultBufsize];
                while(true) {
                    int count = is.read(buffer);
                    if (count == -1) {
                        break;
                    }
                    if (count == 0) {
                        Thread.sleep(1);
                        continue;
                    }
                    
                    fos.write(buffer, 0, count);
                    savecnt += count;
                }
                
                if (savecnt != length) {
                    fos.close();
                    fos = null;
                    tmpfile.delete();
                    tmpfile = null;
                    if (mSaveBmpFileRepeatTimes < 3) {
                        getBitmapWithUrlAndSaveToFile(url);
                        mSaveBmpFileRepeatTimes++;
                    } else {
                        mSaveBmpFileRepeatTimes = 0;
                        return false;
                    }
                }
                fos.flush();
                fos.close();

                if (tmpfile.length() != length) {
                    tmpfile.delete();
                    tmpfile = null;
                    if (mSaveBmpFileRepeatTimes < 3) {
                        getBitmapWithUrlAndSaveToFile(url);
                        mSaveBmpFileRepeatTimes++;
                    } else {
                        mSaveBmpFileRepeatTimes = 0;
                        return false;
                    }
                }
                
                File savefile = new File(savepath);
                if (savefile.exists()) {
                    savefile.delete();
                }

                if (tmpfile != null && tmpfile.exists()) {
                    tmpfile.renameTo(savefile);
                }

                fos = null;
                tmpfile = null;

                mSaveBmpFileRepeatTimes = 0;
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                if (tmpfile != null) {
                    tmpfile.delete();
                }
                
                if (conn != null) {
                    conn.disconnect();
                    conn = null;
                }
            }
        }
    }
    /**
     * 文件缓存任务
     * @param url
     * @param callback
     */
    public  void pushTask(String url,File cacheFile,ImageLoadCallBack callback) {
        pushTask(url,TYPE_LOACAL,callback,null,cacheFile,-1,-1);
    }
    /**
     * 文件缓存任务,图片大小固定
     * @param url
     * @param callback
     * @param width
     * @param height
     */
    public  void pushTask(String url,File cacheFile,ImageLoadCallBack callback,int width,int height) {
        pushTask(url,TYPE_LOACAL,callback,null,cacheFile,width,height);
    }
    
    /**
     * 网络任务
     * @param url
     * @param callback
     */
    public  void pushTask(String url,ImageLoadCallBack callback) {
        pushTask(url,TYPE_NETWORK,callback,null,null,-1,-1);
    }
    /**
     * 网络任务,图片大小固定
     * @param url
     * @param callback
     * @param width
     * @param height
     */
    public  void pushTask(String url,ImageLoadCallBack callback,int width,int height) {
        pushTask(url,TYPE_NETWORK,callback,null,null,width,height);
    }
    
    /**
     * 这个用来push从zip包中获取bitmap的任务
     * @param url
     * @param callback
     * @param zipRes
     */
    public  void pushTask(String url,ImageLoadCallBack callback,File zipRes){
        pushTask(url,TYPE_LOACAL,callback,zipRes,null,-1,-1);
    }
    /**
     * 图片大小固定的 zip包任务
     * @param url
     * @param callback
     * @param zipRes
     * @param width
     * @param height
     */
    public  void pushTask(String url,ImageLoadCallBack callback,File zipRes,int width,int height){
        pushTask(url,TYPE_LOACAL,callback,zipRes,null,width,height);
    }
    
    private  void pushTask(String url,int taskType,ImageLoadCallBack callback,File zipRes,File cacheFile,int width,int height){
        synchronized(mAllTasks){
            MyTask task = null;
            for(MyTask tk:mAllTasks){
                if(tk.url.equals(url)){
                    task = tk;
                }
            }
            if(task == null){
                task = new MyTask(url,taskType,callback,zipRes ,cacheFile ,width,height);
                mAllTasks.add(task);
            } else {
                boolean isFind = false;
                for(SoftReference<ImageLoadCallBack> softCallback:task.callBack){
                    ImageLoadCallBack imageLoadCallBack = softCallback.get();
                    if(imageLoadCallBack == null) {
                        continue;
                    }
                    if(imageLoadCallBack.getCaller().equals(callback.getCaller())){
                        isFind = true;
                        break;
                    }
                }
                if(!isFind){
                    task.callBack.add(new SoftReference<AsynTaskManager.ImageLoadCallBack>(callback));
                }
                mAllTasks.remove(task);
                mAllTasks.add(task);
            }
            mAllTasks.notifyAll();//要唤醒所有等待线程//
        }
    }
    
    public interface ImageLoadCallBack{
        public abstract boolean isNeedToDecode(String imageUrl);
        public abstract void onImageLoadSuccess(String imageUrl,Bitmap bitmap);
        public abstract void onImageLoadFailed(String imageUrl, String reason);
        public abstract String getCaller();
    }
    
}
