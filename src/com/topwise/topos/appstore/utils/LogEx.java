package com.topwise.topos.appstore.utils;

public class LogEx {
    
	private static final String TAG = "AppStore";
	
	private static final boolean debug = true;
    private static final boolean mPrintDetail = true;
    
    private static String detail(String tag) {

        String print = "";
        boolean readNext = false;
        String localClassName = LogEx.class.getName();

        StackTraceElement stack[] = Thread.currentThread().getStackTrace();
        for (StackTraceElement ste : stack) {
            String className = ste.getClassName();
            int filterSubClass;
            if ((filterSubClass = className.lastIndexOf('$')) > 0) {
                className = className.substring(0, filterSubClass);
            }

            if (localClassName.equals(className)) {
                readNext = true;
            } else if (readNext) {
                readNext = false;
                int classNameOffset = className.lastIndexOf(".");
                if (classNameOffset < 0) {
                    break;
                }
                print = ste.getClassName().substring(classNameOffset + 1); // print class name
                print += "." + ste.getMethodName(); // print method name
                print += "(" + ste.getLineNumber() + ")"; // print line number
            }
        }
        return print;
    }
    
    private static String p(String tag, String msg) {
        return (mPrintDetail ? "[" + detail(tag) + "]$ " : "") + (tag != null ? tag + ":/" : "") + msg;
    }
    
    public static void v(String msg) {
        v(null, msg);
    }

    public static void v(String tag, String msg) {
		if(!debug) {
			return;
		}
        android.util.Log.v(TAG, p(tag, msg));
    }

    public static void d(String msg) {
        d(null, msg);
    }

    public static void d(String tag, String msg) {
		if(!debug) {
			return;
		}
        android.util.Log.d(TAG, p(tag, msg));
    }

    public static void i(String msg) {
        i(null, msg);
    }

    public static void i(String tag, String msg) {
        android.util.Log.i(TAG, p(tag, msg));
    }

    public static void w(String msg) {
        w(null, msg);
    }

    public static void w(String tag, String msg) {
        android.util.Log.w(TAG, p(tag, msg));
    }
    
    public static void e(String msg) {
        e(null, msg);
    }

    public static void e(String tag, String msg) {
        android.util.Log.e(TAG, p(tag, msg));
    }

    public static void printCallStack() {
        if (!debug) {
            return;
        }
        StackTraceElement stack[] = Thread.currentThread().getStackTrace();
        int i = 1;
        for (StackTraceElement ste : stack) {
            android.util.Log.d(TAG, "at(" + (i++) + ") " + ste.getClassName()
                    + "." + ste.getMethodName() + "(" + ste.getLineNumber()
                    + ")");
        }
    }

}
