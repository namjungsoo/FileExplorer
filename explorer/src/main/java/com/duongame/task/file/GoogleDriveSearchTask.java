package com.duongame.task.file;

import android.os.AsyncTask;

import com.duongame.file.FileExplorer;
import com.duongame.fragment.ExplorerFragment;

import java.lang.ref.WeakReference;

public class GoogleDriveSearchTask extends AsyncTask<String, Void, FileExplorer.Result> {
    private WeakReference<ExplorerFragment> fragmentWeakReference;
    private String path;

    public GoogleDriveSearchTask(ExplorerFragment fragment) {
        fragmentWeakReference = new WeakReference<>(fragment);
    }


    @Override
    protected FileExplorer.Result doInBackground(String... strings) {
        return null;
    }
}
