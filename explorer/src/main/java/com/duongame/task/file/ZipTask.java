package com.duongame.task.file;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.duongame.adapter.ExplorerItem;
import com.duongame.helper.FileHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by namjungsoo on 2017-12-31.
 */

public class ZipTask extends AsyncTask<Void, FileHelper.Progress, Void> {
    private ArrayList<ExplorerItem> fileList;
    private WeakReference<ZipTask> dialogWeakReference;
    private WeakReference<Activity> activityWeakReference;
    private DialogInterface.OnDismissListener onDismissListener;
    private String path;

    public ZipTask(Activity activity) {
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
    }

    @Override
    protected Void doInBackground(Void... voids) {
        return null;
    }

    @Override
    protected void onProgressUpdate(FileHelper.Progress... values) {
        FileHelper.Progress progress = values[0];
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
    }
}
