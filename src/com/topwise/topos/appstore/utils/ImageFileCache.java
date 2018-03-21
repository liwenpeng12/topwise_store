package com.topwise.topos.appstore.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;

public class ImageFileCache {
                                                            
    private static final int MB = 1024*1024;
    private static final int CACHE_SIZE = 50;
    private static final int FREE_SD_SPACE_NEEDED_TO_CACHE = 10;
                                                                
    public ImageFileCache() {
        //清理文件缓存
        removeCache(getDirectory());
    }
                                                                
    /** 从缓存中获取图片 **/
    public Bitmap getImage(final String url) {    
        String filename = String.valueOf(url.hashCode());
        
        String path = getDirectory() + filename;
        File file = new File(path);
        if (file.exists()) {
            Bitmap bmp = BitmapFactory.decodeFile(path);
            if (bmp == null) {
                file.delete();
            } else {
                updateFileTime(path);
                return bmp;
            }
        }
        
        return null;
    }
    
    public File getCachedImageFile(final String url) {
        final String path = getDirectory() + String.valueOf(url.hashCode());
        File file = new File(path);
        if (file.exists()) {
            return file;
        }
        
        return null;
    }
    
    public String getCachedImageFilePath(String url) {
        return getDirectory() + String.valueOf(url.hashCode());
    }
                                                                
    /** 将图片存入文件缓存 **/
    public void saveBitmap(Bitmap bm, String url) {
        if (bm == null || url == null) {
            return;
        }
        //判断sdcard上的空间
        if (FREE_SD_SPACE_NEEDED_TO_CACHE > freeSpaceOnSd()) {
            //SD空间不足
            return;
        }
        String filename = String.valueOf(url.hashCode());
        String dir = getDirectory();
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        File file = new File(dir + filename);
        OutputStream outStream = null;
        try {
            file.createNewFile();
            outStream = new FileOutputStream(file);
            bm.compress(getPictureType(url), 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (FileNotFoundException e) {
            Log.w("ImageFileCache", "FileNotFoundException");
        } catch (IOException e) {
            Log.w("ImageFileCache", "IOException");
        } finally {
            try {
                outStream.close();
            } catch (Exception e) {
            }
            try {
                bm.recycle();
            } catch (Exception e ) {
            }
        }
    } 
    
    public static Bitmap.CompressFormat getPictureType(String url) {
        int index = url.lastIndexOf('.');
        if (index > 0 && index < url.length() - 1) {
            String type = url.substring(index + 1);
            type = type.toLowerCase();
            
            if ("png".equals(type)) {
                return Bitmap.CompressFormat.PNG;
            } else {
                return Bitmap.CompressFormat.JPEG;
            }
        }
        
        return Bitmap.CompressFormat.JPEG;
    }
                                                                
    /**
     * 计算存储目录下的文件大小，
     * 当文件总大小大于规定的CACHE_SIZE或者sdcard剩余空间小于FREE_SD_SPACE_NEEDED_TO_CACHE的规定
     * 那么删除40%最近没有被使用的文件
     */
    private boolean removeCache(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdir();
            return true;
        }
        
        File[] files = dir.listFiles();
        if (files == null) {
            return true;
        }
        if (files.length == 0) {
            return true;
        }

        int dirSize = 0;
        for (int i = 0; i < files.length; i++) {
            dirSize += files[i].length();
        }
                                                            
        if (dirSize > CACHE_SIZE * MB || FREE_SD_SPACE_NEEDED_TO_CACHE > freeSpaceOnSd()) {
            int removeFactor = (int) ((0.4 * files.length) + 1);
            Arrays.sort(files, new FileLastModifSort());
            for (int i = 0; i < removeFactor; i++) {
                files[i].delete();
            }
        }
                                                            
        if (freeSpaceOnSd() <= CACHE_SIZE) {
            return false;
        }
                                                                    
        return true;
    }
    
    public boolean cleanCache() {
        String dirPath = getDirectory();
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null) {
            return true;
        }
        
        try {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    files[i].delete();
                }
            }
        } catch (Exception e) {
            return false;
        }
        
        return true;
    }
                                                                
    /** 修改文件的最后修改时间 **/
    public static void updateFileTime(String path) {
        File file = new File(path);
        long newModifiedTime = System.currentTimeMillis();
        file.setLastModified(newModifiedTime);
    }
                                                                
    /** 计算sdcard上的剩余空间 **/
    public static double freeSpaceOnSd() {
        StatFs stat = new StatFs(Utils.getInternalStoragePath());
        double sdFreeMB = ((double)stat.getAvailableBlocks() * (double) stat.getBlockSize()) / MB;
        return sdFreeMB;
    } 
                                                                
    /** 获得缓存目录 **/
    private String getDirectory() {
        return Properties.CACHE_PATH;
    }
                                                            
    /**
     * 根据文件的最后修改时间进行排序
     */
    private class FileLastModifSort implements Comparator<File> {
        public int compare(File arg0, File arg1) {
            if (arg0.lastModified() > arg1.lastModified()) {
                return 1;
            } else if (arg0.lastModified() == arg1.lastModified()) {
                return 0;
            } else {
                return -1;
            }
        }
    }
                                                            
}
