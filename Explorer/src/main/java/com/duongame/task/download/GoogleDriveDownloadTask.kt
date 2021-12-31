package com.duongame.task.download

import android.app.Activity
import android.os.Environment
import com.duongame.adapter.ExplorerItem
import com.duongame.cloud.googledrive.GoogleDriveManager.REQUEST_AUTHORIZATION
import com.duongame.cloud.googledrive.GoogleDriveManager.client
import com.duongame.file.FileHelper.BLOCK_SIZE
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class GoogleDriveDownloadTask(activity: Activity, callback: Callback) : CloudDownloadTask(activity, callback) {
    override fun doInBackground(vararg params: ExplorerItem?): File? {
        val item = params[0] ?: return null
        name = item.name
        val fileId = item.metadata as String?
        try {
            val path =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(path, name)

            // Make sure the Downloads directory exists.
            if (!path.exists()) {
                if (!path.mkdirs()) {
                    mException = RuntimeException("Unable to create directory: $path")
                    return null
                }
            } else if (!path.isDirectory) {
                mException = IllegalStateException("Download path is not a directory: $path")
                return null
            }

            // GoogleDrive에서 찾기 시작
            val driveService = client ?: return null
            val outputStream: OutputStream = FileOutputStream(file)
            //driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            val inputStream = driveService.files()[fileId].executeMediaAsInputStream()

            // 이걸 분해해서 100%를 표시할수 있도록 해야 한다.
            val buf = ByteArray(BLOCK_SIZE)
            var nRead = 0
            var totalRead: Long = 0
            val srcLength = item.size
            while (inputStream.read(buf).also { nRead = it } > 0) {
                if (isCancelled) {
                    cancelled = true
                    return null
                }
                outputStream.write(buf, 0, nRead)
                totalRead += nRead.toLong()
                val percent = totalRead * 100 / srcLength
                Timber.e("percent=$percent totalRead=$totalRead srcLength=$srcLength")
                updateProgress(0, percent.toInt())
            }
            outputStream.close()
            inputStream.close()
            return file
        } catch (e: UserRecoverableAuthIOException) {
            e.printStackTrace()
            val activity = activityWeakReference.get() ?: return null
            activity.startActivityForResult(e.intent, REQUEST_AUTHORIZATION)
            return null
        } catch (e: IOException) {
            mException = e
        }
        return null
    }
}