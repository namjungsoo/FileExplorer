package com.duongame.task.download;

import android.app.Activity;
import android.os.Environment;

import com.duongame.adapter.ExplorerItem;
import com.duongame.cloud.googledrive.GoogleDriveManager;
import com.duongame.helper.JLog;
import com.google.api.services.drive.Drive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.duongame.file.FileHelper.BLOCK_SIZE;

public class GoogleDriveDownloadTask extends CloudDownloadTask {

    public GoogleDriveDownloadTask(Activity activity, Callback callback) {
        super(activity, callback);
    }

    @Override
    protected File doInBackground(ExplorerItem... params) {
        ExplorerItem item = params[0];
        name = item.name;
        String fileId = (String) item.metadata;

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

            // GoogleDrive에서 찾기 시작
            Drive driveService = GoogleDriveManager.getClient();
            if (driveService == null)
                return null;

            OutputStream outputStream = new FileOutputStream(file);
            //driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            InputStream inputStream = driveService.files().get(fileId).executeMediaAsInputStream();

            // 이걸 분해해서 100%를 표시할수 있도록 해야 한다.
            byte[] buf = new byte[BLOCK_SIZE];
            int nRead = 0;
            long totalRead = 0;
            long srcLength = item.size;

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

            return file;
        } catch (IOException e) {
            mException = e;
        }

        return null;
    }
}
