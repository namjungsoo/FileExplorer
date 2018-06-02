package com.duongame.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.duongame.R;
import com.duongame.helper.AppHelper;

/**
 * Created by namjungsoo on 2017-12-29.
 */

public abstract class MultiDialog extends DialogFragment {
    ProgressBar eachProgress;
    ProgressBar totalProgress;

    TextView fileName;
    TextView eachText;
    TextView totalText;

    DialogInterface.OnDismissListener onDismissListener;
    DialogInterface.OnClickListener onPositiveClickListener;
    DialogInterface.OnClickListener onNegativeClickListener;
    DialogInterface.OnClickListener onNeutralClickListener;

    protected int messageResId;

    public MultiDialog() {

    }

    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        onDismissListener = listener;
    }

    public void setOnPositiveClickListener(DialogInterface.OnClickListener listener) {
        onPositiveClickListener = listener;
    }

    public void setOnNegativeClickListener(DialogInterface.OnClickListener listener) {
        onNegativeClickListener = listener;
    }

    public void setOnNeutralClickListener(DialogInterface.OnClickListener listener) {
        onNeutralClickListener = listener;
    }

    public TextView getFileName() {
        return fileName;
    }

    public TextView getEachText() {
        return eachText;
    }

    public TextView getTotalText() {
        return totalText;
    }

    public ProgressBar getEachProgress() {
        return eachProgress;
    }

    public ProgressBar getTotalProgress() {
        return totalProgress;
    }

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
//        JLog.w("TAG", "onCreateDialog");
        Activity activity = getActivity();
        if(activity == null)
            return null;

        //FIX: String resource ID #0x0
        if(messageResId == 0)
            return null;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_multi, null, false);

        fileName = view.findViewById(R.id.file_name);
        eachText = view.findViewById(R.id.each_text);
        totalText = view.findViewById(R.id.total_text);

        eachProgress = view.findViewById(R.id.each_progress);
        totalProgress = view.findViewById(R.id.total_progress);

        eachProgress.setMax(100);
        totalProgress.setMax(100);

        builder.setTitle(AppHelper.getAppName(activity))
                .setIcon(AppHelper.getIconResId(activity))
                .setMessage(messageResId)
                .setView(view);

        onCustomizeButtons(builder);

        // dialog가 아니라 fragment에 속성을 주어야 한다.
        setCancelable(false);
        return builder.create();
    }

    protected abstract void onCustomizeButtons(AlertDialog.Builder builder);

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if(onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }
}
