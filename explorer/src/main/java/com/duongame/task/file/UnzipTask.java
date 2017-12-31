package com.duongame.task.file;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;

import com.duongame.R;
import com.duongame.adapter.ExplorerItem;
import com.duongame.dialog.UnzipDialog;
import com.duongame.helper.FileHelper;
import com.duongame.helper.ToastHelper;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by namjungsoo on 2017-12-31.
 */

public class UnzipTask extends AsyncTask<Void, FileHelper.Progress, Void> {
    private ArrayList<ExplorerItem> fileList;
    private List<FileHeader> zipList;

    private WeakReference<UnzipDialog> dialogWeakReference;
    private WeakReference<Activity> activityWeakReference;

    // 생성자에서 사용
    private DialogInterface.OnDismissListener onDismissListener;

    private String path;

    public UnzipTask(Activity activity) {
        activityWeakReference = new WeakReference<Activity>(activity);
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        onDismissListener = listener;
    }

    public void setFileList(ArrayList<ExplorerItem> fileList) {
        this.fileList = fileList;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // 폴더를 생성
        File file = new File(path);
        if (!file.exists())
            file.mkdirs();

        // Dialog의 초기화를 진행한다.
        dialogWeakReference = new WeakReference<UnzipDialog>(new UnzipDialog());
        UnzipDialog dialog = dialogWeakReference.get();
        if (dialog != null) {
            dialog.setOnPositiveClickListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    UnzipTask.this.cancel(true);
                }
            });
            dialog.setOnDismissListener(onDismissListener);

            Activity activity = activityWeakReference.get();
            if (activity != null) {
                dialog.show(activity.getFragmentManager(), "unzip");
            }
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        // 파일의 갯수만큼 루프를 돌아서 unzip 해준다.
        for (int i = 0; i < fileList.size(); i++) {
            ExplorerItem item = fileList.get(i);

            try {
                ZipFile zipFile = new ZipFile(item.path);
                zipFile.setFileNameCharset("EUC-KR");

                zipList = zipFile.getFileHeaders();
                for (int j = 0; j < zipList.size(); j++) {
                    if (isCancelled())
                        return null;

                    zipFile.extractFile(zipList.get(j), path);
                    FileHelper.Progress progress = new FileHelper.Progress();
                    progress.index = i;
                    progress.percent = (j + 1) * 100 / zipList.size();
                    progress.fileName = zipList.get(j).getFileName();
                    publishProgress(progress);
                }

                // 여기까지 오면 파일이 끝난것임
            } catch (ZipException e) {
                e.printStackTrace();

                // 에러를 표시하고 종료한다.
                return null;
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(FileHelper.Progress... values) {
        FileHelper.Progress progress = values[0];

        UnzipDialog dialog = dialogWeakReference.get();
        if (dialog != null) {
            dialog.getFileName().setText(progress.fileName);

            dialog.getEachProgress().setProgress(100);
            //int totalPercent = progress.index * 100 / fileList.size();
            //
            // 앞에꺼까지 퍼센트 + 현재 퍼센트
            int totalPercent = (int) (progress.index * (100.0f / fileList.size())) + (int) (progress.percent / (float) fileList.size());
            dialog.getTotalProgress().setProgress(totalPercent);

            Activity activity = activityWeakReference.get();
            if (activity != null) {
                dialog.getEachText().setVisibility(View.VISIBLE);
                dialog.getEachText().setText(String.format(activity.getString(R.string.each_text), 100));
                dialog.getTotalText().setVisibility(View.VISIBLE);
                dialog.getTotalText().setText(String.format(activity.getString(R.string.total_text), progress.index, fileList.size(), totalPercent));
            }
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        Activity activity = activityWeakReference.get();
        if (activity != null) {
            ToastHelper.showToast(activity, R.string.toast_file_unzip);
        }

        UnzipDialog dialog = dialogWeakReference.get();
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
