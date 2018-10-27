package com.duongame.helper;

import android.content.Context;

import com.duongame.R;

/**
 * Created by namjungsoo on 2017-12-02.
 */

public class AppHelper {
    public static boolean isPro(Context context) {
        if (context == null)
            return false;
        if (context.getApplicationContext().getPackageName().contains(".pro"))
            return true;
        return false;
    }

    public static boolean isComicz(Context context) {
        if (context == null)
            return false;
        if (context.getApplicationContext().getPackageName().contains(".comicz"))
            return true;
        return false;
    }

    public static String getAppName(Context context) {
        if (context == null)
            return "";
        if (isComicz(context)) {
            return context.getResources().getString(R.string.comicz_name_free);
        } else {
            return context.getResources().getString(R.string.file_name_free);
        }
    }

    public static int getIconResId(Context context) {
        if (isComicz(context)) {
            return R.mipmap.comicz;
        } else {
            return R.mipmap.explorer;
        }
    }
}
