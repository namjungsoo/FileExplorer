package com.duongame.task.download;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import com.duongame.cloud.googledrive.GoogleDriveManager;
import com.google.api.services.drive.Drive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

public class GoogleDriveDownloadTask extends AsyncTask<String, Void, File> {
    private final WeakReference<Context> mContextRef;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onDownloadComplete(File result);
        void onError(Exception e);
    }

    public GoogleDriveDownloadTask(Context context, Callback callback) {
        mContextRef = new WeakReference<>(context);
        mCallback = callback;
    }

    @Override
    protected File doInBackground(String... params) {
        String name = params[0];
        String fileId = params[1];

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(path, name);

        // Make sure the Downloads directory exists.
        if (!path.exists()) {
            if (!path.mkdirs()) {
                mException = new RuntimeException("Unable to create directory: " + path);
            }
        } else if (!path.isDirectory()) {
            mException = new IllegalStateException("Download path is not a directory: " + path);
            return null;
        }

        // GoogleDrive에서 찾기 시작
        Drive driveService = GoogleDriveManager.getClient();
        if (driveService == null)
            return null;

        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        try {
            driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // Tell android about the file
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        mContextRef.get().sendBroadcast(intent);

        return file;
    }

    @Override
    protected void onPostExecute(File result) {
        super.onPostExecute(result);

        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDownloadComplete(result);
        }
    }

}
