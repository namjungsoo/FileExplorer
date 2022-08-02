package com.duongame.helper

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.duongame.R

/**
 * Created by namjungsoo on 2017-12-02.
 */
object AppHelper {
    @JvmStatic
    fun isPro(context: Context?): Boolean {
        if (context == null) return false
        return context.applicationContext.packageName.contains(".pro")
    }

    @JvmStatic
    fun isComicz(context: Context?): Boolean {
        if (context == null) return false
        return context.applicationContext.packageName.contains(".comicz")
    }

    @JvmStatic
    fun getAppName(context: Context?): String {
        if (context == null) return ""
        return if (isComicz(context)) {
            context.resources.getString(R.string.comicz_name_free)
        } else {
            context.resources.getString(R.string.file_name_free)
        }
    }

    @JvmStatic
    fun getIconResId(context: Context?): Int {
        return if (isComicz(context)) {
            R.mipmap.comicz
        } else {
            R.mipmap.explorer
        }
    }

    @JvmStatic
    fun launchMarket(context: Context, packageName: String) {
        try {
            val marketLaunch = Intent(Intent.ACTION_VIEW)
            marketLaunch.data = Uri.parse("market://details?id=$packageName")
            context.startActivity(marketLaunch)
        } catch (e: ActivityNotFoundException) { // FIX: ActivityNotFoundException
            val marketLaunch = Intent(Intent.ACTION_VIEW)
            marketLaunch.data =
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            context.startActivity(marketLaunch)
        }
    }
}