package com.duongame.task.zip;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;

import com.duongame.R;
import com.duongame.adapter.ExplorerItem;
import com.duongame.dialog.UnzipDialog;
import com.duongame.helper.FileHelper;
import com.duongame.helper.ToastHelper;

import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.zip.ZipEntry;

/**
 * Created by namjungsoo on 2017-12-31.
 */

/*
7가지 파일을 테스트 해야 한다.
--
zip
jar
tar
tar.gz
tar.bz2
gz
bz2
7z
rar
 */
public class UnzipTask extends AsyncTask<Void, FileHelper.Progress, Boolean> {
    private ArrayList<ExplorerItem> fileList;

    private WeakReference<UnzipDialog> dialogWeakReference;
    private WeakReference<Activity> activityWeakReference;

    // 생성자에서 사용
    private DialogInterface.OnDismissListener onDismissListener;

    private String path;

    public UnzipTask(Activity activity) {
        activityWeakReference = new WeakReference<>(activity);
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
        dialogWeakReference = new WeakReference<>(new UnzipDialog());
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

    void updateProgress(int i, int j, int size, String name) {
        FileHelper.Progress progress = new FileHelper.Progress();
        progress.index = i;
        progress.percent = (j + 1) * 100 / size;
        progress.fileName = name;
        publishProgress(progress);
    }

    boolean unarchiveZip(ExplorerItem item, int i) throws IOException {
        // common compress
        ZipArchiveInputStream stream;
        ZipEntry entry;

        stream = new ZipArchiveInputStream(new FileInputStream(item.path));
        entry = (ZipEntry) stream.getNextEntry();
        int size = 0;

        while (entry != null) {
            if(isCancelled())
                return false;

            size++;
            entry = (ZipEntry) stream.getNextEntry();
        }
        stream.close();

        stream = new ZipArchiveInputStream(new FileInputStream(item.path));
        entry = (ZipEntry) stream.getNextEntry();

        int j = 0;
        while (entry != null) {
            if(isCancelled())
                return false;

            String target = path + "/" + entry.getName();
            FileOutputStream outputStream = new FileOutputStream(target);
            IOUtils.copy(stream, outputStream);

            outputStream.close();
            updateProgress(i, i, size, entry.getName());
            entry = (ZipEntry) stream.getNextEntry();
            j++;
        }

        stream.close();
        return true;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        // 파일의 갯수만큼 루프를 돌아서 unzip 해준다.
        for (int i = 0; i < fileList.size(); i++) {
            ExplorerItem item = fileList.get(i);

            try {
                if(!unarchiveZip(item, i))
                    return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
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
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        Activity activity = activityWeakReference.get();
        if (activity != null) {
            if (result)
                ToastHelper.showToast(activity, R.string.toast_file_unzip);
            else
                ToastHelper.showToast(activity, R.string.toast_cancel);
        }

        UnzipDialog dialog = dialogWeakReference.get();
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
