package com.duongame.dialog

import android.app.AlertDialog
import com.duongame.R

class DownloadDialog : MultiDialog() {
    override fun onCustomizeButtons(builder: AlertDialog.Builder) {
        builder.setPositiveButton(R.string.cancel) { dialog, which -> // 다운로드하던 파일을 취소한다.
            onPositiveClickListener?.onClick(dialog, which)
        }
    }

    init {
        messageResId = R.string.msg_cloud_download
    }
}