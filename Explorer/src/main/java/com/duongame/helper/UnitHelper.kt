package com.duongame.helper

import android.content.res.Resources

/**
 * Created by namjungsoo on 2016-11-19.
 */
object UnitHelper {
    @JvmStatic
    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    fun pxToDp(px: Int): Int {
        return (px / Resources.getSystem().displayMetrics.density).toInt()
    }
}