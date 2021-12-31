package com.duongame.dialog

import com.duongame.helper.AppHelper.appName
import com.duongame.helper.AppHelper.iconResId
import android.os.Bundle
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import com.duongame.R
import android.widget.TextView
import android.widget.CheckBox

/**
 * Created by namjungsoo on 2017-12-29.
 */
class OverwriteDialog : DialogFragment() {
    private var applyAll = false
    private var skip // skip or overwrite
            = false
    private var cancel = false
    private var path: String? = null
    private var lock: Object? = null
    var onFinishListener: OnFinishListener? = null

    fun setPath(path: String?) {
        this.path = path
    }

    interface OnFinishListener {
        fun onFinish(applyAll: Boolean, skip: Boolean, cancel: Boolean)
    }

    fun setLock(lock: Object?) {
        this.lock = lock
    }

    override fun onCreateDialog(bundle: Bundle): Dialog? {
        val activity = activity ?: return null
        val view = activity.layoutInflater.inflate(R.layout.dialog_paste, null, false)
        val checkApplyAll = view.findViewById<CheckBox>(R.id.apply_all)
        checkApplyAll.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                applyAll = true
            }
        }
        val fileName = view.findViewById<TextView>(R.id.file_name)
        fileName.text = path
        val builder = AlertDialog.Builder(activity)
            .setTitle(appName)
            .setMessage(R.string.msg_overwrite)
            .setView(view)
            .setIcon(iconResId) // 덮어쓰기
            .setPositiveButton(R.string.overwrite) { dialog, which ->
                skip = false
                finish()
            } // 취소
            .setNegativeButton(R.string.cancel) { dialog, which ->
                cancel = true
                finish()
            } // 건너뛰기
            .setNeutralButton(R.string.skip) { dialog, which ->
                skip = true
                finish()
            }
        isCancelable = false
        return builder.create()
    }

    fun finish() {
        if (onFinishListener != null) {
            onFinishListener!!.onFinish(applyAll, skip, cancel)
        }
        if (lock != null) {
            synchronized(lock!!) { lock?.notifyAll() }
        }
    }
}