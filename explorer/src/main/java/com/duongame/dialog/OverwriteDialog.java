package com.duongame.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.duongame.R;
import com.duongame.helper.AppHelper;

/**
 * Created by namjungsoo on 2017-12-29.
 */

public class OverwriteDialog extends DialogFragment {
    private boolean applyAll;
    private boolean skip;// skip or overwrite
    private boolean cancel;

    private String path;
    private Object lock;
    private OnFinishListener onFinishListener;

    public void setPath(String path) {
        this.path = path;
    }

    public interface OnFinishListener {
        void onFinish(boolean applyAll, boolean skip, boolean cancel);
    }

    public void setOnFinishListener(OnFinishListener listener) {
        onFinishListener = listener;
    }

    public void setLock(Object lock) {
        this.lock = lock;
    }

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_paste, null, false);
        CheckBox checkApplyAll = (CheckBox) view.findViewById(R.id.apply_all);
        checkApplyAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    applyAll = true;
                }
            }
        });

        TextView fileName = (TextView) view.findViewById(R.id.file_name);
        fileName.setText(path);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(AppHelper.getAppName(getActivity()))
                .setMessage(R.string.msg_overwrite)
                .setView(view)
                .setIcon(AppHelper.getIconResId(getActivity()))
                // 덮어쓰기
                .setPositiveButton(R.string.overwrite, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        skip = false;
                        finish();
                    }
                })
                // 취소
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancel = true;
                        finish();
                    }
                })
                // 건너뛰기
                .setNeutralButton(R.string.skip, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        skip = true;
                        finish();
                    }
                });
        return builder.create();
    }

    void finish() {
        if(onFinishListener != null) {
            onFinishListener.onFinish(applyAll, skip, cancel);
        }

        if(lock != null) {
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }
}
