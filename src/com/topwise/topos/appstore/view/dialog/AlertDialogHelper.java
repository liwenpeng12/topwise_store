package com.topwise.topos.appstore.view.dialog;

class AlertDialogHelper {

    public static int getThemeResource(String name) {
        return getInternalResource("style", name);
    }

    public static int getInternalResource(String type, String name) {
        try {
            Class<?> localClass = Class.forName("com.android.internal.R$"
                    + type);
            int resId = Integer.parseInt(localClass.getField(name)
                    .get(localClass).toString());
            return resId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public static int[] getInternalAttrs(String name) {
        try {
            Class<?> localClass = Class
                    .forName("com.android.internal.R$styleable");
            int[] attrs = (int[]) localClass.getField(name).get(localClass);
            return attrs;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new int[0];
    }
}
