package com.duongame.task.zip;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;

import com.duongame.R;
import com.duongame.adapter.ExplorerItem;
import com.duongame.dialog.ZipDialog;
import com.duongame.helper.FileHelper;
import com.duongame.helper.FileSearcher;
import com.duongame.helper.ToastHelper;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by namjungsoo on 2017-12-31.
 */

public class ZipTask extends AsyncTask<Void, FileHelper.Progress, Boolean> {
    private ArrayList<ExplorerItem> fileList;
    private ArrayList<ExplorerItem> zipList;

    private WeakReference<ZipDialog> dialogWeakReference;
    private WeakReference<Activity> activityWeakReference;

    private DialogInterface.OnDismissListener onDismissListener;
    private String path;
    private String currentPath;

    public ZipTask(Activity activity) {
        activityWeakReference = new WeakReference<Activity>(activity);
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        onDismissListener = listener;
    }

    // 압축할 파일의 목록
    public void setFileList(ArrayList<ExplorerItem> fileList) {
        this.fileList = fileList;
    }

    // 압축 대상 파일의 패스
    public void setZipPath(String path) {
        this.path = path;
    }

    public void setPath(String path) {
        this.currentPath = path;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // Dialog의 초기화를 진행한다.
        dialogWeakReference = new WeakReference<>(new ZipDialog());
        ZipDialog dialog = dialogWeakReference.get();
        if (dialog != null) {
            dialog.setOnPositiveClickListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ZipTask.this.cancel(true);
                }
            });
            dialog.setOnDismissListener(onDismissListener);

            Activity activity = activityWeakReference.get();
            if (activity != null) {
                dialog.show(activity.getFragmentManager(), "zip");
            }
        }
    }

    boolean archiveZip() {
        try {
            ZipArchiveOutputStream stream = new ZipArchiveOutputStream(new File(path));
            for(int i=0; i<fileList.size(); i++) {
                String entryName = fileList.get(i).path.replace(currentPath, "");

                ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
                FileInputStream inputStream = new FileInputStream(fileList.get(i).path);

                stream.putArchiveEntry(entry);
                IOUtils.copy(inputStream, stream);
                stream.closeArchiveEntry();
            }
            stream.finish();
            stream.close();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    void makeZipList() {
        zipList = new ArrayList<>();

        // fileList에는 선택된 파일만 들어옴
        for (int i = 0; i < fileList.size(); i++) {
            ExplorerItem item = fileList.get(i);

            if (item.type == ExplorerItem.FileType.FOLDER) {
                // 폴더의 경우 하위 모든 아이템을 찾은뒤에 더한다.
                FileSearcher searcher = new FileSearcher();
                FileSearcher.Result result = searcher.setRecursiveDirectory(true)
                        .setHiddenFile(true)
                        .setExcludeDirectory(false)
                        .setImageListEnable(false)
                        .search(item.path);

                // 폴더 하위 파일의 경우에는 폴더 이름과 파일명을 적어줌
                if (result != null && result.fileList != null) {
                    for (int j = 0; j < result.fileList.size(); j++) {

                        // 선택된 폴더의 최상위 폴더의 폴더명을 적어줌
                        ExplorerItem subItem = result.fileList.get(j);
                        if (subItem.path.startsWith(item.path)) {
                            subItem.name = subItem.path.replace(FileHelper.getParentPath(item.path) + "/", "");
                        }
                    }
                    zipList.addAll(result.fileList);
                }

                // 폴더 자기 자신도 더함
                // zip은 더하지 않음
//                zipList.add(item);
            } else {
                zipList.add(item);
            }
        }
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        // 선택된 파일이 폴더인 경우에 전체 폴더 확장을 해야한다.
        makeZipList();
        return archiveZip();
    }

    @Override
    protected void onProgressUpdate(FileHelper.Progress... values) {
        FileHelper.Progress progress = values[0];

        ZipDialog dialog = dialogWeakReference.get();
        if (dialog != null) {
            dialog.getFileName().setText(progress.fileName);
            dialog.getEachProgress().setProgress(100);

            // 들어온 퍼센트를 바로 토탈로 표시한다.
            int totalPercent = progress.percent;
            dialog.getTotalProgress().setProgress(totalPercent);

            Activity activity = activityWeakReference.get();
            if (activity != null) {
                dialog.getEachText().setVisibility(View.VISIBLE);
                dialog.getEachText().setText(String.format(activity.getString(R.string.each_text), 100));
                dialog.getTotalText().setVisibility(View.VISIBLE);
                dialog.getTotalText().setText(String.format(activity.getString(R.string.total_text), progress.index, zipList.size(), totalPercent));
            }
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        Activity activity = activityWeakReference.get();
        if (activity != null) {
            if (result)
                ToastHelper.showToast(activity, R.string.toast_file_zip);
            else
                ToastHelper.showToast(activity, R.string.toast_cancel);
        }

        ZipDialog dialog = dialogWeakReference.get();
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
