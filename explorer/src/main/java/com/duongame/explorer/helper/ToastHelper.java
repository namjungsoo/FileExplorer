package com.duongame.explorer.helper;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by 정수 on 2015-11-15.
 */
public class ToastHelper {
    public static void showToast(Context context, int resId) {
        if(context == null)
            return;

        final String message = context.getString(resId);
        showToast(context, message);
    }

    public static void showToast(Context context, String message) {
        final Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 40);
        toast.show();
    }
}
