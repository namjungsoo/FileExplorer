package com.duongame.explorer.helper;

import android.content.Context;

import com.duongame.R;

/**
 * Created by namjungsoo on 2017-12-02.
 */

public class AppHelper {
    private static boolean isComicz(Context context) {
        if(context == null)
            return false;
        if(context.getApplicationContext().getPackageName().indexOf(".comicz") == -1)
            return false;
        return true;
    }

    public static String getAppName(Context context) {
        if(isComicz(context)) {
            return context.getResources().getString(R.string.comicz_name_free);
        } else {
            return context.getResources().getString(R.string.file_name_free);
        }
    }

    public static int getIconResId(Context context) {
        if(isComicz(context)) {
            return R.drawable.comicz;
        } else {
            return R.drawable.explorer;
        }
    }
}
