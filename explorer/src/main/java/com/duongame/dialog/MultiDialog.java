package com.duongame.dialog;

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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_multi, null, false);

        fileName = (TextView) view.findViewById(R.id.file_name);
        eachText = (TextView) view.findViewById(R.id.each_text);
        totalText = (TextView) view.findViewById(R.id.total_text);

        eachProgress = (ProgressBar) view.findViewById(R.id.each_progress);
        totalProgress = (ProgressBar) view.findViewById(R.id.total_progress);

        eachProgress.setMax(100);
        totalProgress.setMax(100);

        builder.setTitle(AppHelper.getAppName(getActivity()))
                .setIcon(AppHelper.getIconResId(getActivity()))
                .setMessage(R.string.msg_file_delete)
                .setView(view);

        onCustomizeButtons(builder);

        return builder.create();
    }

    protected abstract void onCustomizeButtons(AlertDialog.Builder builder);
}
