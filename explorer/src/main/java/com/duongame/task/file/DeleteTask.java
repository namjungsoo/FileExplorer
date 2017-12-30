package com.duongame.task.file;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.duongame.R;
import com.duongame.adapter.ExplorerItem;
import com.duongame.dialog.DeleteDialog;
import com.duongame.helper.FileHelper;
import com.duongame.helper.FileSearcher;
import com.duongame.helper.JLog;
import com.duongame.helper.ToastHelper;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by namjungsoo on 2017-12-29.
 */

public class DeleteTask extends AsyncTask<Void, Integer, Void> {
    private ArrayList<ExplorerItem> fileList;
    private ArrayList<ExplorerItem> deleteList;
    private WeakReference<DeleteDialog> dialogWeakReference;
    private WeakReference<Activity> activityWeakReference;

    // 생성자에서 사용
    private DialogInterface.OnDismissListener onDismissListener;

    public DeleteTask(Activity activity) {
        activityWeakReference = new WeakReference<Activity>(activity);
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        onDismissListener = listener;
    }

    // 삭제할 파일과 폴더를 입력해야 한다.
    // 폴더 삭제도 가능한지 확인해봐야 한다.
    public void setFileList(ArrayList<ExplorerItem> fileList) {
        this.fileList = fileList;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        dialogWeakReference = new WeakReference<DeleteDialog>(new DeleteDialog());
        DeleteDialog dialog = dialogWeakReference.get();
        if (dialog != null) {
            // 확인 버튼이 눌려쥐면 task를 종료한다.
            dialog.setOnPositiveClickListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DeleteTask.this.cancel(true);
                }
            });
            dialog.setOnDismissListener(onDismissListener);

            Activity activity = activityWeakReference.get();
            if (activity != null) {
                dialog.show(activity.getFragmentManager(), "delete");
            }
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        deleteList = new ArrayList<>();
        for (int i = 0; i < fileList.size(); i++) {
            ExplorerItem item = fileList.get(i);
            if (!item.selected)
                continue;

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
                    deleteList.addAll(result.fileList);
                }

                deleteList.add(item);
            } else {
                deleteList.add(item);
            }
        }

        for (int i = 0; i < deleteList.size(); i++) {
            // 파일을 하나하나 지운다.
            try {
                if (isCancelled()) {
                    break;
                } else {
                    work(deleteList.get(i).path);
                    publishProgress(i);
                }
            } catch (SecurityException e) {
                // 지울수 없는 파일
                Activity activity = activityWeakReference.get();
                if (activity != null) {
                    ToastHelper.showToast(activity, R.string.toast_error);
                }
                return null;
            }
        }
        return null;
    }

    void work(String path) {
        new File(path).delete();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];

        String name = deleteList.get(progress).name;
        int size = deleteList.size();
        float total = ((float) progress + 1) / size;
        int percent = (int) (total * 100);

        DeleteDialog dialog = dialogWeakReference.get();
        if (dialog != null) {
            dialog.getFileName().setText(name);

            dialog.getEachProgress().setProgress(100);
            dialog.getTotalProgress().setProgress(percent);

            Activity activity = activityWeakReference.get();
            if (activity != null) {
                dialog.getEachText().setText(String.format(activity.getString(R.string.each_text), 100));
                dialog.getTotalText().setText(String.format(activity.getString(R.string.total_text), progress + 1, size, percent));
            }
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        JLog.e("TAG", "onPostExecute");
        Activity activity = activityWeakReference.get();
        if (activity != null) {
            ToastHelper.showToast(activity, R.string.toast_file_delete);
        }

        DeleteDialog dialog = dialogWeakReference.get();
        if(dialog != null) {
            JLog.e("TAG", "dialog.dismiss");
            dialog.dismiss();
        }
    }
}
