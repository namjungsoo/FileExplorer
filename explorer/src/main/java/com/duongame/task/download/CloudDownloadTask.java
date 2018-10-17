package com.duongame.task.download;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;

import com.duongame.R;
import com.duongame.adapter.ExplorerItem;
import com.duongame.dialog.DownloadDialog;
import com.duongame.file.FileHelper;
import com.duongame.helper.ToastHelper;

import java.io.File;
import java.lang.ref.WeakReference;

public abstract class CloudDownloadTask extends AsyncTask<ExplorerItem, FileHelper.Progress, File> {
    public interface Callback {
        void onDownloadComplete(File result);
        void onError(Exception e);
    }

    private final Callback mCallback;
    protected Exception mException;
    protected String name;
    protected boolean cancelled;

    // 생성자에서 사용
    private WeakReference<DownloadDialog> dialogWeakReference;
    protected WeakReference<Activity> activityWeakReference;

    CloudDownloadTask(Activity activity, Callback callback) {
        mCallback = callback;
        activityWeakReference = new WeakReference<>(activity);
    }

    @Override
    abstract protected File doInBackground(ExplorerItem... explorerItems);

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        dialogWeakReference = new WeakReference<>(new DownloadDialog());

        // 다이얼로그를 show 하자
        DownloadDialog dialog = dialogWeakReference.get();
        if (dialog != null) {
            dialog.setOnPositiveClickListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // 버튼이 1개이므로 무조건 취소한다.
                    CloudDownloadTask.this.cancel(true);
                }
            });

            Activity activity = activityWeakReference.get();
            if (activity != null) {
                dialog.show(activity.getFragmentManager(), "download");
            }
        }
    }

    // 파일번호와 현재 파일의 percent로 progress를 publish 한다.
    void updateProgress(int i, int percent) {
        // progress를 기입해야 한다.
        FileHelper.Progress progress = new FileHelper.Progress();
        progress.index = i;
        progress.percent = percent;

        publishProgress(progress);
    }

    @Override
    protected void onProgressUpdate(FileHelper.Progress... values) {
        FileHelper.Progress progress = values[0];

        // 파일명 (name)

        // 전체 파일 갯수
        int size = 1;

        // 전체 파일 리스트 퍼센트
        int totalPercent = progress.index * 100 / size;

        DownloadDialog dialog = dialogWeakReference.get();
        if (dialog != null) {
            dialog.getFileName().setText(name);

            dialog.getEachProgress().setProgress(progress.percent);
            dialog.getTotalProgress().setProgress(totalPercent);

            Activity activity = activityWeakReference.get();
            if (activity != null) {
                dialog.getEachText().setVisibility(View.VISIBLE);
                dialog.getEachText().setText(String.format(activity.getString(R.string.each_text), progress.percent));
                dialog.getTotalText().setVisibility(View.VISIBLE);
                dialog.getTotalText().setText(String.format(activity.getString(R.string.total_text), progress.index + 1, size, totalPercent));
            }
        }
    }

    @Override
    protected void onPostExecute(File result) {
        super.onPostExecute(result);

        // 정리하고 toast를 띄워주고 팝업을 닫는다.
        Activity activity = activityWeakReference.get();
        if (activity != null) {
            if (result != null) {// 성공일 경우
                ToastHelper.info(activity, R.string.toast_cloud_complete);
            } else {
                // 취소된 경우와 에러가 난 경우가 있다.
                if (cancelled)
                    ToastHelper.error(activity, R.string.toast_cancel);
                else
                    ToastHelper.error(activity, R.string.toast_error);
            }
        }

        // 팝업을 닫는다.
        DownloadDialog dialog = dialogWeakReference.get();
        if (dialog != null) {
            dialog.dismiss();
        }

        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDownloadComplete(result);
        }
    }

}
