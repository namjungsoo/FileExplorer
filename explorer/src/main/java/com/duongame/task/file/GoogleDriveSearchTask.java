package com.duongame.task.file;

import android.os.AsyncTask;
import android.view.MenuItem;

import com.duongame.App;
import com.duongame.R;
import com.duongame.activity.main.BaseMainActivity;
import com.duongame.adapter.ExplorerItem;
import com.duongame.cloud.googledrive.GoogleDriveManager;
import com.duongame.file.FileExplorer;
import com.duongame.file.FileHelper;
import com.duongame.fragment.ExplorerFragment;
import com.duongame.helper.ToastHelper;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;

import timber.log.Timber;

import static com.duongame.adapter.ExplorerItem.FILETYPE_PDF;
import static com.duongame.adapter.ExplorerItem.FILETYPE_TEXT;
import static com.duongame.adapter.ExplorerItem.FILETYPE_ZIP;
import static com.duongame.cloud.googledrive.GoogleDriveManager.REQUEST_AUTHORIZATION;

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

    String getFolderFileId(String parent, String name) {
        Drive driveService = GoogleDriveManager.getClient();
        if (driveService == null)
            return null;

        String fileId = null;
        String pageToken = null;
        do {
            FileList result = null;
            try {
                result = driveService.files().list()
                        .setQ("'" + parent + "' in parents and name='" + name + "'")
                        .setFields("nextPageToken, files(id)")
                        .setPageToken(pageToken)
                        .execute();
            } catch (UserRecoverableAuthIOException e) {
                e.printStackTrace();

                ExplorerFragment fragment = fragmentWeakReference.get();
                if (fragment == null)
                    return null;
                fragment.startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            for (File file : result.getFiles()) {
                fileId = file.getId();
                return fileId;
            }

            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        return fileId;
    }

    // /개인적인/남채은
    @Override
    protected FileExplorer.Result doInBackground(String... strings) {
        path = strings[0];
        if (path == null)
            path = "/";

        ExplorerFragment fragment = fragmentWeakReference.get();
        if (fragment == null)
            return null;

        /*
        [0] = ""
        [1] = "개인적인"
        [2] = "남채은"
         */
        String fileId = null;
        String[] folders = path.split("/");
        if (folders.length < 2) {
            fileId = "root";
        } else {
            for (int i = 1; i < folders.length; i++) {
                if (i == 1) {
                    fileId = getFolderFileId("root", folders[i]);
                } else {
                    fileId = getFolderFileId(fileId, folders[i]);
                }
                if (fileId == null)
                    return null;
            }
        }

        // GoogleDrive에서 찾기 시작
        Drive driveService = GoogleDriveManager.getClient();
        if (driveService == null)
            return null;

        String pageToken = null;
        ArrayList<ExplorerItem> fileList = new ArrayList<>();

        ArrayList<ExplorerItem> folderList = new ArrayList<>();
        ArrayList<ExplorerItem> normalList = new ArrayList<>();

        do {
            FileList result = null;

            try {
                result = driveService.files().list()
                        .setQ("'" + fileId + "' in parents")
                        .setFields("nextPageToken, files(*)")
                        .setPageToken(pageToken)
                        .execute();
            } catch (UserRecoverableAuthIOException e) {
                e.printStackTrace();
                fragment.startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            for (File file : result.getFiles()) {
                int type = ExplorerItem.FILETYPE_FILE;
                if (file.getMimeType().equals("application/vnd.google-apps.folder"))
                    type = ExplorerItem.FILETYPE_FOLDER;

                long size = file.getSize() != null ? file.getSize().longValue() : 0;
                ExplorerItem item = new ExplorerItem(file.getName(), file.getName(), file.getCreatedTime().toString(), size, type);
                item.metadata = file.getId();

                if (type == ExplorerItem.FILETYPE_FOLDER) {
                    folderList.add(item);
                } else {
                    type = FileHelper.getFileType(item.name);

                    switch (type) {
                        case FILETYPE_ZIP:
                        case FILETYPE_TEXT:
                        case FILETYPE_PDF:
                            break;
                        default:
                            type = ExplorerItem.FILETYPE_FILE;
                            break;
                    }

                    normalList.add(item);
                }
                //fileList.add(item);

                Timber.e("name=" + file.getName() + " createdTime=" + file.getCreatedTime() + " fileId=" + file.getId() + " mime=" + file.getMimeType());
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        FileHelper.NameAscComparator comparator = new FileHelper.NameAscComparator();
        Collections.sort(folderList, comparator);
        Collections.sort(normalList, comparator);
        fileList.addAll(folderList);
        fileList.addAll(normalList);

        FileExplorer.Result result = new FileExplorer.Result();
        result.fileList = fileList;
        return result;
    }

    @Override
    protected void onPostExecute(FileExplorer.Result result) {
        super.onPostExecute(result);// AsyncTask는 아무것도 안함

        if (result == null) {
            onExit();
            return;
        }

        ExplorerFragment fragment = fragmentWeakReference.get();
        if (fragment == null)
            return;

        try {
            //FIX: Index Out of Bound
            // 쓰레드에서 메인쓰레드로 옮김
            fragment.setFileList(result.fileList);
            App.Companion.getInstance().setFileList(result.fileList);
            App.Companion.getInstance().setImageList(result.imageList);
            fragment.getAdapter().setFileList(fragment.getFileList());

            fragment.getAdapter().notifyDataSetChanged();

            App.Companion.getInstance().setLastPath(path);
            fragment.getTextPath().setText(path);
            fragment.getTextPath().requestLayout();

            fragment.setCanClick(true);
        } catch (NullPointerException e) {

        }
    }

    private void onExit() {
        ExplorerFragment fragment = fragmentWeakReference.get();
        if (fragment == null)
            return;

        // UI 업데이트
        fragment.updateGoogleDriveUI(false);
        fragment.setCanClick(true);

        // 에러 메세지
        ToastHelper.INSTANCE.showToast(fragment.getContext(), R.string.toast_error);

        BaseMainActivity activity = (BaseMainActivity) fragment.getActivity();
        if (activity == null)
            return;

        // 로그아웃 처리
        MenuItem item = activity.getGoogleDriveMenuItem();
        activity.logoutGoogleDrive(item);
    }

}
