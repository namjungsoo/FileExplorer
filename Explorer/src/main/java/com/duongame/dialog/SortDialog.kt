package com.duongame.dialog

import com.duongame.helper.AppHelper.appName
import com.duongame.helper.AppHelper.iconResId
import com.duongame.dialog.SortDialog.OnSortListener
import android.os.Bundle
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import com.duongame.R
import com.duongame.helper.AppHelper
import android.content.DialogInterface
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import com.duongame.dialog.SortDialog

/**
 * Created by namjungsoo on 2017-12-23.
 */
class SortDialog : DialogFragment() {
    var sortType = 0
    var sortDir = 0

    interface OnSortListener {
        fun onSort(type: Int, dir: Int)
    }

    var onSortListener: OnSortListener? = null

    fun setTypeAndDirection(sortType: Int, sortDir: Int) {
        this.sortType = sortType
        this.sortDir = sortDir
    }

    override fun onCreateDialog(bundle: Bundle): Dialog? {
        val activity = activity ?: return null
        val builder = AlertDialog.Builder(activity)
        val view = activity.layoutInflater.inflate(R.layout.dialog_sort, null, false)
        initUI(view)
        builder.setTitle(appName)
            .setIcon(iconResId)
            .setMessage(R.string.msg_file_sort)
            .setView(view)
            .setPositiveButton(R.string.ok) { dialog, which -> // 확인이므로 pref에 저장하고 소팅을 새로 해줌
//                            Timber.e("ok");
                if (onSortListener != null) {
                    onSortListener!!.onSort(sortType, sortDir)
                }
            }
            .setNegativeButton(R.string.cancel) { dialog, which ->
                //                            Timber.e("cancel");
            }
        return builder.create()
    }

    // pref에서 초기값을 읽어서 셋팅한다.
    // 값이 변했을 경우 적용해준다.
    private fun initUI(view: View) {
        val type = view.findViewById<RadioGroup>(R.id.sort_type)
        val types = arrayOfNulls<RadioButton>(4)
        types[0] = view.findViewById(R.id.sort_name)
        types[1] = view.findViewById(R.id.sort_ext)
        types[2] = view.findViewById(R.id.sort_date)
        types[3] = view.findViewById(R.id.sort_size)
        val direction = view.findViewById<RadioGroup>(R.id.sort_direction)
        val dirs = arrayOfNulls<RadioButton>(2)
        dirs[0] = view.findViewById(R.id.sort_accending)
        dirs[1] = view.findViewById(R.id.sort_descending)
        types[sortType]?.isChecked = true
        dirs[sortDir]?.isChecked = true
        type.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.sort_name -> sortType = 0
                R.id.sort_ext -> sortType = 1
                R.id.sort_date -> sortType = 2
                R.id.sort_size -> sortType = 3
            }
        }
        direction.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.sort_accending -> sortDir = 0
                R.id.sort_descending -> sortDir = 1
            }
        }
    }

    companion object {
        private val TAG = SortDialog::class.java.simpleName
    }
}