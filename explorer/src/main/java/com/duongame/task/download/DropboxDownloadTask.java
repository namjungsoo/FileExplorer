package com.duongame.task.download;

import android.app.Activity;
import android.os.Environment;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.duongame.adapter.ExplorerItem;
import com.duongame.cloud.dropbox.DropboxClientFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import timber.log.Timber;

import static com.duongame.file.FileHelper.BLOCK_SIZE;

public class DropboxDownloadTask extends CloudDownloadTask {
    private final DbxClientV2 mDbxClient;

    public DropboxDownloadTask(Activity activity, Callback callback) {
        super(activity, callback);
        mDbxClient = DropboxClientFactory.getClient();
    }

    @Override
    protected File doInBackground(ExplorerItem... explorerItems) {
        FileMetadata metadata = (FileMetadata)explorerItems[0].metadata;
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
                Timber.e("percent=" + percent + " totalRead=" + totalRead + " srcLength=" + srcLength);

                updateProgress(0, (int) percent);
            }

            outputStream.close();
            inputStream.close();

            return file;
        } catch (DbxException | IOException e) {
            mException = e;
        }

        return null;
    }
}
