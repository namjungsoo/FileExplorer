package com.duongame.helper

import android.content.Context
import com.duongame.helper.ToastHelper
import android.widget.Toast
import android.view.Gravity
import es.dmoral.toasty.Toasty

/**
 * Created by 정수 on 2015-11-15.
 */
object ToastHelper {
    fun showToast(context: Context?, resId: Int) {
        if (context == null) return
        val message = context.getString(resId)
        showToast(context, message)
    }

    fun showToast(context: Context?, message: String?) {
        if (context == null) return
        val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.BOTTOM, 0, 40)
        toast.show()
    }

    fun info(context: Context?, resId: Int) {
        if (context != null) Toasty.info(
            context,
            context.getString(resId),
            Toast.LENGTH_SHORT,
            true
        ).show()
    }

    fun error(context: Context?, resId: Int) {
        if (context != null) Toasty.error(
            context,
            context.getString(resId),
            Toast.LENGTH_SHORT,
            true
        ).show()
    }

    fun success(context: Context?, resId: Int) {
        if (context != null) Toasty.success(
            context,
            context.getString(resId),
            Toast.LENGTH_SHORT,
            true
        ).show()
    }

    fun warning(context: Context?, resId: Int) {
        if (context != null) Toasty.warning(
            context,
            context.getString(resId),
            Toast.LENGTH_SHORT,
            true
        ).show()
    }
}