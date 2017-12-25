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
import com.duongame.helper.AppHelper;
import com.duongame.helper.FileHelper;
import com.duongame.helper.FileSearcher;
import com.duongame.helper.ToastHelper;

import java.io.File;
import java.util.ArrayList;

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
    DialogInterface.OnDismissListener onDismissListener;

    public DeleteDialog() {

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

            builder.setTitle(AppHelper.getAppName(getActivity()))
                    .setIcon(AppHelper.getIconResId(getActivity()))
                    .setMessage(R.string.msg_file_delete)
                    .setView(view)
                    .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 지우던 파일을 취소한다.
                            if (task != null) {
                                task.cancel(true);
                            }
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
        ArrayList<ExplorerItem> deleteList;

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
                        new File(deleteList.get(i).path).delete();
                        publishProgress(i);
                    }
                } catch (SecurityException e) {
                    // 지울수 없는 파일
                    ToastHelper.showToast(getActivity(), R.string.toast_error);
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int progress = values[0].intValue();

            String name = deleteList.get(progress).name;
            fileName.setText(name);

            eachProgress.setProgress(100);
            eachText.setText(String.format(getResources().getString(R.string.each_text), 100));

            int size = deleteList.size();
            float total = ((float) progress + 1) / size;
            int percent = (int) (total * 100);
            totalProgress.setProgress(percent);

            totalText.setText(String.format(getActivity().getResources().getString(R.string.total_text), progress + 1, size, percent));
        }

        @Override
        protected void onPostExecute(Void result) {
            ToastHelper.showToast(getActivity(), R.string.toast_file_delete);
            DeleteDialog.this.dismiss();

            if(onDismissListener != null) {
                onDismissListener.onDismiss(getDialog());
            }
        }
    }
}
