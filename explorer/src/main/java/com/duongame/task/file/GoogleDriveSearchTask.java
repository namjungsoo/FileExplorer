package com.duongame.task.file;

import android.os.AsyncTask;
import android.util.Log;

import com.duongame.adapter.ExplorerItem;
import com.duongame.cloud.googledrive.GoogleDriveManager;
import com.duongame.file.FileExplorer;
import com.duongame.fragment.ExplorerFragment;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class GoogleDriveSearchTask extends AsyncTask<String, Void, FileExplorer.Result> {
    private WeakReference<ExplorerFragment> fragmentWeakReference;
    private String path;

    public GoogleDriveSearchTask(ExplorerFragment fragment) {
        fragmentWeakReference = new WeakReference<>(fragment);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();// AsyncTask는 아무것도 안함

        ExplorerFragment fragment = fragmentWeakReference.get();
        if (fragment == null)
            return;

        fragment.setCanClick(false);// 이제부터 클릭할수 없음
    }

    @Override
    protected FileExplorer.Result doInBackground(String... strings) {
        path = strings[0];
        if (path == null)
            path = "/";

        // GoogleDrive에서 찾기 시작
        Drive driveService = GoogleDriveManager.getClient();
        if (driveService == null)
            return null;

        String pageToken = null;
        ArrayList<ExplorerItem> fileList = new ArrayList<>();

        do {
            FileList result = null;

            try {
                result = driveService.files().list()
                        .setQ("'root' in parents")
//                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name, createdTime)")
                        .setPageToken(pageToken)
                        .execute();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            for (File file : result.getFiles()) {
                int type = ExplorerItem.FILETYPE_FILE;
//                ExplorerItem item = new ExplorerItem(file.getName(), file.getName(), file.getModifiedTime().toString(), file.getSize(), type);
//                fileList.add(item);

                Log.e("Jungsoo", file.getName() + " " + file.getCreatedTime());
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);


        FileExplorer.Result result = new FileExplorer.Result();
        result.fileList = fileList;
        return result;
    }

    @Override
    protected void onPostExecute(FileExplorer.Result result) {
        super.onPostExecute(result);// AsyncTask는 아무것도 안함

        if (isCancelled())
            return;

        if (result == null)
            return;

        ExplorerFragment fragment = fragmentWeakReference.get();
        if (fragment == null)
            return;

        //FIX: Index Out of Bound
        // 쓰레드에서 메인쓰레드로 옮김
        fragment.setFileList(result.fileList);
        fragment.getApplication().setImageList(result.imageList);
        fragment.getAdapter().setFileList(fragment.getFileList());

        fragment.getAdapter().notifyDataSetChanged();

        fragment.getApplication().setLastPath(path);
        fragment.getTextPath().setText(path);
        fragment.getTextPath().requestLayout();

        fragment.setCanClick(true);
    }

}
