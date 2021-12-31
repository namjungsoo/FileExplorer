package com.duongame.task.download

import android.app.Activity
import android.os.Environment
import com.dropbox.core.DbxException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata
import com.duongame.adapter.ExplorerItem
import com.duongame.cloud.dropbox.DropboxClientFactory.client
import com.duongame.file.FileHelper.BLOCK_SIZE
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class DropboxDownloadTask(activity: Activity, callback: Callback) : CloudDownloadTask(activity, callback) {
    private val mDbxClient: DbxClientV2? = client

    override fun doInBackground(vararg explorerItems: ExplorerItem?): File? {
        val metadata = explorerItems[0]?.metadata as FileMetadata?
        name = metadata!!.name
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

            // Download the file.
            val outputStream: OutputStream = FileOutputStream(file)

            //original
            //mDbxClient.files().download(metadata.getPathLower(), metadata.getRev()).download(outputStream);
            val inputStream =
                mDbxClient!!.files().download(metadata.pathLower, metadata.rev).inputStream

            // 이걸 분해해서 100%를 표시할수 있도록 해야 한다.
            val buf = ByteArray(BLOCK_SIZE)
            var nRead = 0
            var totalRead: Long = 0

            //long srcLength = src.length();
            //long srcLength = inputStream.available();
            val srcLength = metadata.size
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
        } catch (e: DbxException) {
            mException = e
        } catch (e: IOException) {
            mException = e
        }
        return null
    }
}