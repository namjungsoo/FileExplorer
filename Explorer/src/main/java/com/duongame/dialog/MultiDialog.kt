package com.duongame.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import com.duongame.R
import com.duongame.helper.AppHelper.appName
import com.duongame.helper.AppHelper.iconResId

/**
 * Created by namjungsoo on 2017-12-29.
 */
abstract class MultiDialog : DialogFragment() {
    var eachProgress: ProgressBar? = null
    var totalProgress: ProgressBar? = null
    var fileName: TextView? = null
    var eachText: TextView? = null
    var totalText: TextView? = null

    var onDismissListener: DialogInterface.OnDismissListener? = null
    var onPositiveClickListener: DialogInterface.OnClickListener? = null
    var onNegativeClickListener: DialogInterface.OnClickListener? = null
    var onNeutralClickListener: DialogInterface.OnClickListener? = null
    protected var messageResId = 0

    override fun onCreateDialog(bundle: Bundle): Dialog? {
        val activity = activity ?: return null

        //FIX: String resource ID #0x0
        if (messageResId == 0) return null
        val builder = AlertDialog.Builder(activity)
        val view = activity.layoutInflater.inflate(R.layout.dialog_multi, null, false)
        fileName = view.findViewById(R.id.file_name)
        eachText = view.findViewById(R.id.each_text)
        totalText = view.findViewById(R.id.total_text)
        eachProgress = view.findViewById(R.id.each_progress)
        totalProgress = view.findViewById(R.id.total_progress)
        eachProgress?.setMax(100)
        totalProgress?.setMax(100)
        builder.setTitle(appName)
            .setIcon(iconResId)
            .setMessage(messageResId)
            .setView(view)
        onCustomizeButtons(builder)

        // dialog가 아니라 fragment에 속성을 주어야 한다.
        isCancelable = false
        return builder.create()
    }

    protected abstract fun onCustomizeButtons(builder: AlertDialog.Builder)
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (onDismissListener != null) {
            onDismissListener!!.onDismiss(dialog)
        }
    }
}