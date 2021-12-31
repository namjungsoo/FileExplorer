package com.duongame.helper

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.duongame.App
import com.duongame.R

/**
 * Created by namjungsoo on 2017-12-02.
 */
object AppHelper {
    private const val PRO = ".pro"
    private const val COMICZ = ".comicz"

    val isPro = App.instance.applicationContext.packageName.contains(PRO)
    val isComicz = App.instance.applicationContext.packageName.contains(COMICZ)

    val appName = if (isComicz) {
        App.instance.applicationContext.resources.getString(R.string.comicz_name_free)
    } else {
        App.instance.applicationContext.resources.getString(R.string.file_name_free)
    }
    val iconResId = if (isComicz) {
        R.mipmap.comicz
    } else {
        R.mipmap.explorer
    }

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