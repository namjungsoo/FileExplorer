package com.duongame.helper

import android.content.Context
import android.view.Gravity
import android.widget.Toast
import es.dmoral.toasty.Toasty

/**
 * Created by 정수 on 2015-11-15.
 */
object ToastHelper {
    fun showToast(context: Context?, resId: Int) {
        val context = context ?: return
        val message = context.getString(resId)
        showToast(context, message)
    }

    @JvmStatic
    fun showToast(context: Context?, message: String?) {
        val context = context ?: return
        val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.BOTTOM, 0, 40)
        toast.show()
    }

    @JvmStatic
    fun info(context: Context?, resId: Int) {
        val context = context ?: return
        Toasty.info(
            context,
            context.getString(resId),
            Toast.LENGTH_SHORT,
            true
        ).show()
    }

    @JvmStatic
    fun error(context: Context?, resId: Int) {
        val context = context ?: return
        Toasty.error(
            context,
            context.getString(resId),
            Toast.LENGTH_SHORT,
            true
        ).show()
    }

    @JvmStatic
    fun success(context: Context?, resId: Int) {
        val context = context ?: return
        Toasty.success(
            context,
            context.getString(resId),
            Toast.LENGTH_SHORT,
            true
        ).show()
    }

    @JvmStatic
    fun warning(context: Context?, resId: Int) {
        val context = context ?: return
        Toasty.warning(
            context,
            context.getString(resId),
            Toast.LENGTH_SHORT,
            true
        ).show()
    }
}