package com.topwise.topos.appstore.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.Stack;

public class FileUtil {
    private static final String TAG = "FileUtil";

    /**
     * 删除文件
     * @param fileName 文件路径
     * @return 成功或失败
     */
    public static boolean deleteFile(String fileName) {
        if (fileName == null || fileName.length() == 0) {
            return false;
        }
        File file = new File(fileName);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }
    
    /**
     * 删除目录及所有子目录和文件，包括删除当前目录
     * @param dir 目录名
     * @return 成功或失败
     */
    public static boolean deleteDir(String dir) {
        if (!dir.endsWith(File.separator)) {
            dir = dir + File.separator;
        }
        File dirFile = new File(dir);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag)
                    break;
            } else {
                flag = deleteDir(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag)
            return false;
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * 删除目录的子目录和文件，不删除当前目录
     * @param dir 目录名
     * @return 成功或失败
     */
    public static boolean clearDir(String dir) {
        if (!dir.endsWith(File.separator)) {
            dir = dir + File.separator;
        }
        File dirFile = new File(dir);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag)
                    break;
            } else {
                flag = deleteDir(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag)
            return false;
        return true;
    }
    
    /**
     * 递归创建文件所在的上级所有目录
     * @param fileName 文件路径
     * @return 成功或失败
     */
    public static boolean mkdirIfNotExist(String fileName) {
        try {
            File file = new File(fileName);
            if (file.exists()) {
                return true;
            }
            String parentName = file.getParent();
            File parent = new File(parentName);
            if (parent.exists()) {
                return file.mkdir();
            }
            if (parent.mkdir()) {
                return file.mkdir();
            }
            if (mkdirIfNotExist(parentName)) {
                return file.mkdir();
            }
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * inputStream转File
     * @param ins 输入
     * @param file 输出
     */
    public static void inputStreamToFile(InputStream ins, File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 创建文件，并创建文件所在目录
     * @param file 文件
     * @return 成功或失败
     * @throws IOException
     */
    public static boolean createNewFile(File file) throws IOException {
        boolean result = true;
        File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            mkdirs(parentFile, 0777);
        }
        try {
            result = file.createNewFile();
            // ClassProxy.FileUtils_setPermissions(file.getPath(), 0777, -1, -1);
        } catch (IOException e) {
            throw e;
        }
        return result;
    }
    private static boolean mkdirs(File dirs, int permission) {
        boolean result = true;
        try {
            File parentFile = dirs;
            Stack<File> needCreateParentFileList = new Stack<File>();
            while (true) {
                if (parentFile == null || parentFile.exists()) {
                    break;
                }
                needCreateParentFileList.push(parentFile);
                parentFile = parentFile.getParentFile();
            }
            while (needCreateParentFileList.size() > 0) {
                File file = needCreateParentFileList.pop();
                result = (result && mkdir(file, permission));
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }
    private static boolean mkdir(File dir, int permission) {
        boolean result = dir.mkdir();
        ClassProxy.FileUtils_setPermissions(dir.getPath(), permission, -1, -1);
        return result;
    }
    
    /**
     * 单纯文件拷贝
     * @param srcFile srcFile 必须存在
     * @param destFile destFile 可以不存在,会自动创建
     * @return true success
     */
    public static boolean copyFile(File srcFile, File destFile, int permissionMode) {
        boolean result = true;
        // 确保文件夹及文件存在//
        try {
            File parentFile = destFile;
            Stack<File> needCreateparentFileList = new Stack<File>();
            while (true) {
                parentFile = parentFile.getParentFile();
                if (parentFile == null || parentFile.exists()) {
                    break;
                }
                needCreateparentFileList.push(parentFile);
            }
            while (needCreateparentFileList.size() > 0) {
                File file = needCreateparentFileList.pop();
                file.mkdir();
                setPermissions(file.getPath(),permissionMode);
            }
            if (destFile.exists()) {
                destFile.delete();
            }
            destFile.createNewFile();
            setPermissions(destFile.getPath(),permissionMode);
        } catch (Exception e) {
            LogEx.e(TAG, e.toString());
            result = false;
            return result;
        }

        // 开始拷贝数据//
        FileChannel in = null;
        FileChannel out = null;
        try {
            in = new FileInputStream(srcFile).getChannel();
            out = new FileOutputStream(destFile).getChannel();
            in.transferTo(0, in.size(), out);
        } catch (Exception e) {
            LogEx.e(TAG, e.toString());
        } finally {
            try {
                in.close();
            } catch (Exception e) {
            }
            try {
                out.close();
            } catch (Exception e) {
            }
        }
        return result;
    }
    private static void setPermissions(String filePath,int permissionMode){
        try {
            Class<?> cls = Class.forName("android.os.FileUtils");
            Method method = cls.getMethod("setPermissions", String.class,int.class,int.class,int.class);
            method.invoke(null, filePath,permissionMode,-1,-1);
        } catch (Exception e) {
            LogEx.e(TAG, e.toString());
        }
    }
}
