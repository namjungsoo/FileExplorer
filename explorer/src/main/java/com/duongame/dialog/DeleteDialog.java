package com.duongame.dialog;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.duongame.R;

/**
 * Created by namjungsoo on 2017-12-07.
 */

public class DeleteDialog extends MultiDialog {
    public DeleteDialog() {
        messageResId = R.string.msg_file_delete;
    }

    @Override
    protected void onCustomizeButtons(AlertDialog.Builder builder) {
        builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 지우던 파일을 취소한다.
                if (onPositiveClickListener != null)
                    onPositiveClickListener.onClick(dialog, which);
            }
        });
    }
}
