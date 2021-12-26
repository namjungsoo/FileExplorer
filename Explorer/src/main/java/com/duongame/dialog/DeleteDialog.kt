package com.duongame.dialog

import android.app.AlertDialog
import com.duongame.R

/**
 * Created by namjungsoo on 2017-12-07.
 */
class DeleteDialog : MultiDialog() {
    override fun onCustomizeButtons(builder: AlertDialog.Builder) {
        builder.setPositiveButton(R.string.cancel) { dialog, which -> // 지우던 파일을 취소한다.
            onPositiveClickListener?.onClick(dialog, which)
        }
    }

    init {
        messageResId = R.string.msg_file_delete
    }
}