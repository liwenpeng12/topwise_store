package com.topwise.topos.appstore.manager;

import android.app.Activity;

import java.util.ArrayList;

public class ActivityManager {

    private static ArrayList<Activity> mActivities = new ArrayList<>();

    public static void addActivity(Activity activity) {
        synchronized (ActivityManager.class) {
            mActivities.add(activity);
        }
    }

    public static void removeActivity(Activity activity) {
        synchronized (ActivityManager.class) {
            mActivities.remove(activity);
        }
    }

    public static void finishActivity(Class c) {
        synchronized (ActivityManager.class) {
            for (Activity activity : mActivities) {
                if (activity.getClass().equals(c)) {
                    if (!activity.isDestroyed() && !activity.isFinishing()) {
                        activity.finish();
                    }
                }
            }
        }
    }

    public static void finishAll() {
        synchronized (ActivityManager.class) {
            try {
                if (mActivities.size() == 0) {
                    return;
                }
                for (Activity activity : mActivities) {
                    if (activity != null && !activity.isDestroyed() && !activity.isFinishing()) {
                        activity.finish();
                    }
                }
            } catch (Throwable t) {
            }
            System.gc();
        }
    }
}
