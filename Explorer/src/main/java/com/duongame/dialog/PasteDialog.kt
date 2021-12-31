package com.duongame.dialog

import android.app.AlertDialog
import com.duongame.dialog.MultiDialog
import com.duongame.R
import android.content.DialogInterface

/**
 * Created by namjungsoo on 2017-12-26.
 */
// 메세지를 설정
// Toast로 붙여넣을지 결정
// FileChannel로 퍼센트를 표시 (copy일 경우)
class PasteDialog : MultiDialog() {
    fun setIsCut(isCut: Boolean) {
        messageResId = if (isCut) {
            R.string.msg_file_paste_cut
        } else {
            R.string.msg_file_paste_copy
        }
    }

    override fun onCustomizeButtons(builder: AlertDialog.Builder) {
        builder.setPositiveButton(R.string.cancel) { dialog, which -> // 지우던 파일을 취소한다.
            onPositiveClickListener?.onClick(dialog, which)
        }
    }
}