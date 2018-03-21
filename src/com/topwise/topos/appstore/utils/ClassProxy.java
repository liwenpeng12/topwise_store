package com.topwise.topos.appstore.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import android.content.Context;
import android.os.IBinder;

public class ClassProxy {
	static HashMap<String,Class<?>> mClassCache = new HashMap<String,Class<?>>();
	static HashMap<String,Method> mMethodCache = new HashMap<String,Method>();
	
	
	
	public static int FileUtils_setPermissions(String file, int mode, int uid, int gid) {
		try{
			Class<?> fileUtilsClass = Class.forName("android.os.FileUtils");
			Method method = fileUtilsClass.getDeclaredMethod("setPermissions", String.class,int.class,int.class,int.class);
			method.setAccessible(true);
			return (Integer)method.invoke(null, file,mode,uid,gid);
		}catch(Exception e) {
			return -1;
		}
	}
	
	public static IBinder ServiceManager_getService(String serviceName) {
		try{
			Class<?> classObject = Class.forName("android.os.ServiceManager");
			Method method = classObject.getDeclaredMethod("getService", String.class);
            return (IBinder)method.invoke(null, serviceName);
		}catch(Exception e) {
		}
		return null;
	}
	
	public static String SystemProperties_get(String key,String defValue) {
		try{
			String className = "android.os.SystemProperties";
			String methodName = "get";
			String methodKey = "android.os.SystemProperties.get(String,String)";
			Class<?> classObject = mClassCache.get(className);
			if(classObject == null) {
				classObject = Class.forName(className);
				mClassCache.put(className, classObject);
			}
			Method method = mMethodCache.get(methodKey);
			if(method == null) {
				method = classObject.getDeclaredMethod(methodName, String.class,String.class);
				mMethodCache.put(methodKey, method);
			}
            return (String)method.invoke(null, key,defValue);
		}catch(Exception e) {
		}
		return null;
	}
	
	public static String SystemProperties_get(String key) {
		try{
			String className = "android.os.SystemProperties";
			String methodName = "get";
			String methodKey = "android.os.SystemProperties.get(String)";
			Class<?> classObject = mClassCache.get(className);
			if(classObject == null) {
				classObject = Class.forName(className);
				mClassCache.put(className, classObject);
			}
			Method method = mMethodCache.get(methodKey);
			if(method == null) {
				method = classObject.getDeclaredMethod(methodName, String.class);
				mMethodCache.put(methodKey, method);
			}
            return (String)method.invoke(null, key);
		}catch(Exception e) {
		}
		return null;
	}
	
	public static Object SystemInterfaceFactory_getSysteminterface() {
		try{
        	Class<?> classObject = Class.forName("com.yulong.android.server.systeminterface.SystemInterfaceFactory");
            Method method = classObject.getDeclaredMethod("getSysteminterface");
            return method.invoke(null);
		}catch(Exception e) {
			
		}
		return null;
	}
	
	public static String ISystemInterface_getAppsSelected(Object object) {
		try{
        	Class<?> classObject = Class.forName("com.yulong.android.server.systeminterface.ISystemInterface");
            Method method = classObject.getDeclaredMethod("getAppsSelected");
            return (String)method.invoke(object);
		}catch(Exception e) {
			
		}
		return null;
	}
	
	public static Object SystemManager_getInstance(Context context) {
		try{
        	Class<?> classObject = Class.forName("com.yulong.android.server.systeminterface.SystemManager");
        	Constructor<?> constructor = classObject.getDeclaredConstructor(Context.class);
        	Object instance = constructor.newInstance(context);
        	return instance;
		}catch(Exception e) {
			return null;
		}
	}
	
	public static Field getField(Object object,String name) {
		Class<?> curClass = object.getClass();
		return getField(curClass,name);
	}
	public static Field getField(Class<?> classL,String name) {
		Field field = null;
		while(true) {
			try {
				field = classL.getDeclaredField(name);
				field.setAccessible(true);
				return field;
			} catch (NoSuchFieldException e) {
				classL = classL.getSuperclass();
				if(classL == null) {
					return null;
				}
			}
		}
	}
	public static Method getMethod(Object object,String name,Class<?>... parameterTypes) {
		Class<?> curClass = object.getClass();
		return getMethod(curClass,name,parameterTypes);
	}
	public static Method getMethod(Class<?> classL,String name,Class<?>... parameterTypes) {
		Method method = null;
		Class<?> curClass = classL;
		while(true) {
			try {
				method = curClass.getDeclaredMethod(name,parameterTypes);
				method.setAccessible(true);
				return method;
			} catch (NoSuchMethodException e) {
				curClass = curClass.getSuperclass();
				if(curClass == null) {
					return null;
				}
			}
		}
	}
}
