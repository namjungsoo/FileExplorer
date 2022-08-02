package com.duongame.helper

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup
import com.duongame.R
import com.duongame.helper.AppHelper.getIconResId
import com.duongame.manager.AdBannerManager

/**
 * Created by namjungsoo on 2016-04-30.
 */
object AlertHelper {
    private const val TAG = "AlertHelper"
    fun showAlert(
        context: Activity,
        title: String?,
        message: String?,
        view: View?,
        posListener: DialogInterface.OnClickListener?,
        keyListener: DialogInterface.OnKeyListener?,
        okOnly: Boolean
    ) {
        val builder = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setView(view)
            .setIcon(getIconResId(context))
            .setOnKeyListener(keyListener)
            .setPositiveButton(context.getString(R.string.ok), posListener)
        if (!okOnly) {
            builder.setNegativeButton(context.getString(R.string.cancel)) { dialogInterface, i -> }
        }
        builder.show()
    }

    @JvmStatic
    fun showAlert(
        context: Activity,
        title: String?,
        message: String?,
        view: View?,
        posListener: DialogInterface.OnClickListener?,
        negListener: DialogInterface.OnClickListener?,
        keyListener: DialogInterface.OnKeyListener?
    ) {
        val builder = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setView(view)
            .setIcon(getIconResId(context))
            .setOnKeyListener(keyListener)
            .setPositiveButton(context.getString(R.string.ok), posListener)
            .setNegativeButton(context.getString(R.string.cancel), negListener)
        builder.show()
    }

    fun showAlertWithAd(
        context: Activity,
        title: String?,
        message: String?,
        posListener: DialogInterface.OnClickListener?,
        negListener: DialogInterface.OnClickListener?,
        keyListener: DialogInterface.OnKeyListener?
    ) {
        // adPopupView가 이전의 팝업에 붙어 있을경우 처리해줌
        val adView = AdBannerManager.getAdPopupView() ?: return
        val vg = adView.parent as ViewGroup
        vg?.removeView(adView)
        showAlert(context, title, message, adView, posListener, negListener, keyListener)
    }

    @JvmStatic
    fun showAlertWithAd(
        context: Activity,
        title: String?,
        message: String?,
        posListener: DialogInterface.OnClickListener?,
        keyListener: DialogInterface.OnKeyListener?,
        okOnly: Boolean
    ) {
        val adView = AdBannerManager.getAdPopupView() ?: return
        val vg = adView.parent as ViewGroup
        vg?.removeView(adView)
        val builder = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setView(adView)
            .setIcon(getIconResId(context))
            .setOnKeyListener(keyListener)
            .setPositiveButton(context.getString(R.string.ok), posListener)
        if (!okOnly) {
            builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, which ->
                // 아무것도 안함
            }
        }
        builder.show()
    }
}