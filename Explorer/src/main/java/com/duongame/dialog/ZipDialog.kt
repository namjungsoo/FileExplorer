package com.duongame.dialog

import android.app.AlertDialog
import com.duongame.dialog.MultiDialog
import com.duongame.R
import android.content.DialogInterface

/**
 * Created by namjungsoo on 2018-01-01.
 */
class ZipDialog : MultiDialog() {
    override fun onCustomizeButtons(builder: AlertDialog.Builder) {
        builder.setPositiveButton(R.string.cancel) { dialog, which -> // 압축풀던 파일을 취소한다.
            onPositiveClickListener?.onClick(dialog, which)
        }
    }

    init {
        messageResId = R.string.msg_file_zip
    }
}