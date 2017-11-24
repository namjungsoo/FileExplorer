package com.duongame.explorer.helper;

import android.util.Log;

import com.duongame.BuildConfig;

/**
 * Created by namjungsoo on 2017-11-24.
 */

public class JLog {
    public static void v(String tag, String msg) {
        if(BuildConfig.DEBUG)
            Log.v(tag, msg);
    }

    public static void d(String tag, String msg) {
        if(BuildConfig.DEBUG)
            Log.d(tag, msg);
    }

    public static void i(String tag, String msg) {
        if(BuildConfig.DEBUG)
            Log.i(tag, msg);
    }

    public static void w(String tag, String msg) {
        if(BuildConfig.DEBUG)
            Log.w(tag, msg);
    }

    public static void e(String tag, String msg) {
        if(BuildConfig.DEBUG)
            Log.e(tag, msg);
    }

}
