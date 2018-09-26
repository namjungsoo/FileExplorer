package com.duongame.task.download;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.duongame.R;
import com.duongame.dialog.DownloadDialog;
import com.duongame.file.FileHelper;
import com.duongame.helper.JLog;
import com.duongame.helper.ToastHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import static com.duongame.file.FileHelper.BLOCK_SIZE;

public class DropboxDownloadTask extends AsyncTask<FileMetadata, FileHelper.Progress, File> {
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;
    private String name;
    private boolean cancelled;

    // 생성자에서 사용
    private WeakReference<DownloadDialog> dialogWeakReference;
    private WeakReference<Activity> activityWeakReference;

    public interface Callback {
        void onDownloadComplete(File result);
        void onError(Exception e);
    }

    public DropboxDownloadTask(Activity activity, DbxClientV2 dbxClient, Callback callback) {
        mDbxClient = dbxClient;
        mCallback = callback;
        activityWeakReference = new WeakReference<>(activity);
    }

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
                    DropboxDownloadTask.this.cancel(true);
                }
            });

            Activity activity = activityWeakReference.get();
            if (activity != null) {
                dialog.show(activity.getFragmentManager(), "download");
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
    protected File doInBackground(FileMetadata... params) {
        FileMetadata metadata = params[0];
        name = metadata.getName();

        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path, name);

            // Make sure the Downloads directory exists.
            if (!path.exists()) {
                if (!path.mkdirs()) {
                    mException = new RuntimeException("Unable to create directory: " + path);
                    return null;
                }
            } else if (!path.isDirectory()) {
                mException = new IllegalStateException("Download path is not a directory: " + path);
                return null;
            }

            // Download the file.
            OutputStream outputStream = new FileOutputStream(file);

            //original
            //mDbxClient.files().download(metadata.getPathLower(), metadata.getRev()).download(outputStream);
            InputStream inputStream = mDbxClient.files().download(metadata.getPathLower(), metadata.getRev()).getInputStream();

            // 이걸 분해해서 100%를 표시할수 있도록 해야 한다.
            byte[] buf = new byte[BLOCK_SIZE];
            int nRead = 0;
            long totalRead = 0;

            //long srcLength = src.length();
            //long srcLength = inputStream.available();
            long srcLength = metadata.getSize();

            while ((nRead = inputStream.read(buf)) > 0) {
                if (isCancelled()) {
                    cancelled = true;
                    return null;
                }
                outputStream.write(buf, 0, nRead);
                totalRead += nRead;

                long percent = totalRead * 100 / srcLength;
                JLog.e("TAG", "percent=" + percent + " totalRead=" + totalRead + " srcLength=" + srcLength);

                updateProgress(0, (int) percent);
            }

            outputStream.close();
            inputStream.close();

            // Tell android about the file
//            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//            intent.setData(Uri.fromFile(file));
//            activityWeakReference.get().sendBroadcast(intent);

            return file;
        } catch (DbxException | IOException e) {
            mException = e;
        }

        return null;
    }
}
