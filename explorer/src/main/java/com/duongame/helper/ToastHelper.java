package com.duongame.helper;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

/**
 * Created by 정수 on 2015-11-15.
 */
public class ToastHelper {
    public static void showToast(Context context, int resId) {
        if (context == null)
            return;

        final String message = context.getString(resId);
        showToast(context, message);
    }

    public static void showToast(Context context, String message) {
        if (context == null)
            return;

        final Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 40);
        toast.show();
    }

    public static void info(Context context, int resId) {
        if (context != null)
            Toasty.info(context, context.getString(resId), Toast.LENGTH_SHORT, true).show();
    }

    public static void error(Context context, int resId) {
        if (context != null)
            Toasty.error(context, context.getString(resId), Toast.LENGTH_SHORT, true).show();
    }

    public static void success(Context context, int resId) {
        if (context != null)
            Toasty.success(context, context.getString(resId), Toast.LENGTH_SHORT, true).show();
    }

    public static void warning(Context context, int resId) {
        if (context != null)
            Toasty.warning(context, context.getString(resId), Toast.LENGTH_SHORT, true).show();
    }

}
