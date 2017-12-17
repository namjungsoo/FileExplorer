package com.duongame.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.duongame.R;
import com.duongame.adapter.ExplorerItem;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by namjungsoo on 2017-12-07.
 */

public class DeleteDialog extends DialogFragment {
    ArrayList<ExplorerItem> fileList;

    ProgressBar eachProgress;
    ProgressBar totalProgress;

    TextView fileName;
    TextView eachText;
    TextView totalText;

    DeleteTask task;

    public DeleteDialog() {

    }

    public void setFileList(ArrayList<ExplorerItem> fileList) {
        this.fileList = fileList;
    }

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (builder != null) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_multi, null, false);

            fileName = (TextView) view.findViewById(R.id.file_name);
            eachText = (TextView) view.findViewById(R.id.each_text);
            totalText = (TextView) view.findViewById(R.id.total_text);

            eachProgress = (ProgressBar) view.findViewById(R.id.each_progress);
            totalProgress = (ProgressBar) view.findViewById(R.id.total_progress);

            eachProgress.setMax(100);
            totalProgress.setMax(100);

            //TODO: TEXT "삭제"
            builder.setMessage("삭제")
                    .setView(view)
                    .setPositiveButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 지우던 파일을 취소한다.
                            if (task != null) {
                                task.cancel(true);
                            }
                            dismiss();
                        }
                    });

            // task를 실행한다.
            task = new DeleteTask();
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        // 여기서 task 시작하자
        return builder.create();
    }

    class DeleteTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            for (int i = 0; i < fileList.size(); i++) {
                // 파일을 하나하나 지운다.
                try {
                    if (isCancelled()) {
                        break;
                    } else {
                        new File(fileList.get(i).path).delete();
                        publishProgress(i);
                    }
                } catch (SecurityException e) {
                    // 지울수 없는 파일
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int progress = values[0].intValue();

            String name = fileList.get(progress).name;
            fileName.setText(name);

            eachProgress.setProgress(100);
            eachText.setText(String.format(getResources().getString(R.string.each_text), 100));

            int size = fileList.size();
            float total = ((float) progress + 1) / size;
            int percent = (int) (total * 100);
            totalProgress.setProgress(percent);

            totalText.setText(String.format(getActivity().getResources().getString(R.string.total_text), progress + 1, size, percent));
        }
    }
}
