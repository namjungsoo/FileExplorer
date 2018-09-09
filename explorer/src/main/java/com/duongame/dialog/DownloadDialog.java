package com.duongame.dialog;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.duongame.R;

public class DownloadDialog extends MultiDialog {
    public DownloadDialog() {
        messageResId = R.string.msg_cloud_download;
    }

    @Override
    protected void onCustomizeButtons(AlertDialog.Builder builder) {
        builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 다운로드하던 파일을 취소한다.
                if (onPositiveClickListener != null)
                    onPositiveClickListener.onClick(dialog, which);
            }
        });
    }
}
