package com.topwise.topos.appstore.utils;

import android.os.Build;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;

import dalvik.system.DexClassLoader;

/**
 * Created by jixin.jia on 15/10/31.
 */
public class DexUtils {

    public static DexClassLoader injectDexAtFirst(String dexPath, String defaultDexOptPath, String soPath) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        DexClassLoader dexClassLoader = new DexClassLoader(dexPath, defaultDexOptPath, soPath, getPathClassLoader());
        Object baseDexElements = getDexElements(getPathList(getPathClassLoader()));
        Object newDexElements = getDexElements(getPathList(dexClassLoader));
        Object allDexElements = combineArray(newDexElements, baseDexElements);
        Object pathList = getPathList(getPathClassLoader());
        setField(pathList, pathList.getClass(), "dexElements", allDexElements);

        if (soPath != null) {
        	if (Build.VERSION.SDK_INT >= 23) {
                Field nativeLibraryDirectories = (Field)pathList.getClass().getDeclaredField("nativeLibraryDirectories");
                nativeLibraryDirectories.setAccessible(true);
                ArrayList<File> files1 = (ArrayList<File>)((Field) nativeLibraryDirectories).get(pathList);
                files1.add(0, new File(soPath));
            } else {
                Field nativeLibraryDirectories = (Field)pathList.getClass().getDeclaredField("nativeLibraryDirectories");
                nativeLibraryDirectories.setAccessible(true);
                File[] files1 = (File[])((Field) nativeLibraryDirectories).get(pathList);
                Object filesss = Array.newInstance(File.class, files1.length + 1);
                Array.set(filesss, 0, new File(soPath));
                for(int i = 1;i<files1.length+1;i++){
                    Array.set(filesss,i,files1[i-1]);
                }
                nativeLibraryDirectories.set(pathList, filesss);
            }
        }

        return dexClassLoader;
    }

    private static ClassLoader getPathClassLoader() {
        ClassLoader pathClassLoader = DexUtils.class.getClassLoader();
        return pathClassLoader;
    }

    private static Object getDexElements(Object paramObject)
            throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        return getField(paramObject, paramObject.getClass(), "dexElements");
    }

    private static Object getPathList(Object baseDexClassLoader)
            throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        return getField(baseDexClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    private static Object combineArray(Object firstArray, Object secondArray) {
        Class<?> localClass = firstArray.getClass().getComponentType();
        int firstArrayLength = Array.getLength(firstArray);
        int allLength = firstArrayLength + Array.getLength(secondArray);
        Object result = Array.newInstance(localClass, allLength);
        for (int k = 0; k < allLength; ++k) {
            if (k < firstArrayLength) {
                Array.set(result, k, Array.get(firstArray, k));
            } else {
                Array.set(result, k, Array.get(secondArray, k - firstArrayLength));
            }
        }
        return result;
    }

    private static Object getField(Object obj, Class<?> cl, String field)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(obj);
    }

    private static void setField(Object obj, Class<?> cl, String field, Object value)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        localField.set(obj, value);
    }
}
