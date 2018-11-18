package com.duongame.helper;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

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

    public static void launchMarket(Context context, String packageName) {
        try {
            final Intent marketLaunch = new Intent(Intent.ACTION_VIEW);

            marketLaunch.setData(Uri.parse("market://details?id=" + packageName));
            context.startActivity(marketLaunch);
        } catch (ActivityNotFoundException e) {// FIX: ActivityNotFoundException
            final Intent marketLaunch = new Intent(Intent.ACTION_VIEW);

            marketLaunch.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
            context.startActivity(marketLaunch);
        }
    }
}
