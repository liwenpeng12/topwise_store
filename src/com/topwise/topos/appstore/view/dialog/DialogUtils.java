package com.topwise.topos.appstore.view.dialog;

import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.database.SharedPreferencesCenter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.widget.CompoundButton;

public class DialogUtils {

    private static boolean PromptFrist = true;

    public static AlertDialog setNormalDialog(final Context context,String negativeButtonText,String positiveButtonText,String title,String content,final Runnable task){
        if(title == null){
            title ="";
        }
        if(content == null){
            content = "";
        }
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                    case DialogInterface.BUTTON_POSITIVE:
                        task.run();
                        break;
                    default:
                        break;
                }
            }
        };

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(content)
                .setNegativeButton(negativeButtonText, listener)
                .setPositiveButton(positiveButtonText, listener)
                .create();
        dialog.setCancelable(true);
        dialog.show();
        return dialog;
    }

    public static AlertDialog setOkCancelDialog(Context context,String title,String content,DialogInterface.OnClickListener listener) {
        if(title == null){
            title = "";
        }
        if(content == null){
            content = "";
        }
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(content)
                .setNegativeButton(R.string.as_cancel, listener)
                .setPositiveButton(R.string.as_ok, listener)
                .create();
        dialog.setCancelable(true);
        dialog.show();
        return dialog;
    }

    public static void setCheckBoxDialog(final Context context, String negativeButtonText, String positiveButtonText, String title, String content, String checkBoxTitle, final Runnable checkBoxCheckedRunnable, final Runnable positiveButtonClickedRunnable) {
        if (title == null) {
            title = "";
        }
        if (content == null) {
            content = "";
        }

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_NEGATIVE: {
                        SharedPreferences sp = SharedPreferencesCenter.getInstance().getSharedPreferences();
                        sp.edit().putBoolean("prompt", false).commit();
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(0);
                        break;
                    }
                    case DialogInterface.BUTTON_POSITIVE: {
                        if(PromptFrist){
                            SharedPreferences sp = SharedPreferencesCenter.getInstance().getSharedPreferences();
                            sp.edit().putBoolean("prompt", true).commit();
                            if (checkBoxCheckedRunnable != null) {
                                checkBoxCheckedRunnable.run();;
                            }
                        }
                        if (positiveButtonClickedRunnable != null) {
                            positiveButtonClickedRunnable.run();
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
        };

        CompoundButton.OnCheckedChangeListener mlistener = new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PromptFrist = isChecked;
                SharedPreferences sp = SharedPreferencesCenter.getInstance().getSharedPreferences();
                sp.edit().putBoolean("prompt", isChecked).commit();
                if (isChecked && checkBoxCheckedRunnable != null) {
                    checkBoxCheckedRunnable.run();
                }
            }
        };

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(content)
                .setNegativeButton(negativeButtonText, listener)
                .setPositiveButton(positiveButtonText, listener)
                .setCancelable(false)
                .setOnKeyListener(new DialogInterface.OnKeyListener() {

                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                            return true;
                        } else {
                            return false;
                        }
                    }

                })
                .create();
        dialog.setCheckBoxDisplay(true, checkBoxTitle, mlistener);
        PromptFrist = true;
        if (checkBoxCheckedRunnable != null) {
            checkBoxCheckedRunnable.run();
        }
        dialog.show();
    }
    
}
