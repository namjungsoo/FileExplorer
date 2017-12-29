package com.duongame.dialog;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.duongame.R;

/**
 * Created by namjungsoo on 2017-12-26.
 */

// 메세지를 설정
// Toast로 붙여넣을지 결정
// FileChannel로 퍼센트를 표시 (copy일 경우)
public class PasteDialog extends MultiDialog {
    public PasteDialog() {
    }

    public void setIsCut(boolean isCut) {
        if(isCut) {
            messageResId = R.string.msg_file_paste_cut;
        } else {
            messageResId = R.string.msg_file_paste_copy;
        }
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
