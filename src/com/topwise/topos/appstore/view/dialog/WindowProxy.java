package com.topwise.topos.appstore.view.dialog;

import java.lang.reflect.Method;

import android.content.Context;
import android.view.MotionEvent;
import android.view.Window;

public class WindowProxy {
    
    private Window mWindow;
    
    public WindowProxy(Window window){
        mWindow = window;
    }
    
    public void alwaysReadCloseOnTouchAttr() {
        try {
            Method method = Window.class
                    .getDeclaredMethod("alwaysReadCloseOnTouchAttr");
            method.invoke(mWindow);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void setCloseOnTouchOutside(boolean close) {
        try {
            Method method = Window.class.getDeclaredMethod(
                    "setCloseOnTouchOutside", boolean.class);
            method.invoke(mWindow, close);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void setCloseOnTouchOutsideIfNotSet(boolean close) {
        try {
            Method method = Window.class.getDeclaredMethod(
                    "setCloseOnTouchOutsideIfNotSet", boolean.class);
            method.invoke(mWindow, close);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public boolean shouldCloseOnTouch(Context context, MotionEvent event) {
        try {
            Method method = Window.class.getDeclaredMethod(
                    "shouldCloseOnTouch", Context.class, MotionEvent.class);
            return (Boolean) method.invoke(mWindow, context, event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
