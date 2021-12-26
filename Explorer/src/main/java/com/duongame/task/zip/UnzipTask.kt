package com.duongame.task.zip

import android.app.Activity
import android.content.DialogInterface
import android.os.AsyncTask
import android.view.View
import com.duongame.R
import com.duongame.adapter.ExplorerItem
import com.duongame.archive.RarFile
import com.duongame.archive.Zip4jFile
import com.duongame.dialog.UnzipDialog
import com.duongame.file.FileHelper.BLOCK_SIZE
import com.duongame.file.FileHelper.Progress
import com.duongame.file.FileHelper.getCompressType
import com.duongame.file.FileHelper.getFileNameCharset
import com.duongame.helper.ToastHelper.error
import com.duongame.helper.ToastHelper.success
import com.hzy.lib7z.ExtractCallback
import com.hzy.lib7z.Z7Extractor
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by namjungsoo on 2017-12-31.
 */
/*
6가지 파일을 테스트 해야 한다.
--
zip
tar/tar.gz/tar.bz2
gz
bz2
7z
rar
 */
class UnzipTask(activity: Activity) : AsyncTask<Void?, Progress?, Boolean>() {
    private var fileList: ArrayList<ExplorerItem>? = null
    private var dialogWeakReference: WeakReference<UnzipDialog>? = null
    private val activityWeakReference: WeakReference<Activity>

    // 생성자에서 사용
    private var onDismissListener: DialogInterface.OnDismissListener? = null
    private var path: String? = null
    private var z7success = false
    private var count = 0
    fun setOnDismissListener(listener: DialogInterface.OnDismissListener?) {
        onDismissListener = listener
    }

    fun setFileList(fileList: ArrayList<ExplorerItem>?) {
        this.fileList = fileList
    }

    fun setPath(path: String?) {
        this.path = path
    }

    override fun onPreExecute() {
        super.onPreExecute()

        // 폴더를 생성
        if (path != null) {
            val file = File(path)
            if (!file.exists()) file.mkdirs()
        }

        // Dialog의 초기화를 진행한다.
        dialogWeakReference = WeakReference(UnzipDialog())
        val dialog = dialogWeakReference!!.get()
        if (dialog != null) {
            dialog.onPositiveClickListener = DialogInterface.OnClickListener { dialog, which ->
                cancel(
                    true
                )
            }
            dialog.onDismissListener = onDismissListener
            val activity = activityWeakReference.get()
            if (activity != null) {
                dialog.show(activity.fragmentManager, "unzip")
            }
        }
    }

    //TODO: ZIP 현재는 1개 파일만 선택해서 압축을 풀으므로 i는 무시된다.
    fun updateProgress(j: Int, count: Int, name: String?, percent: Int) {
        val progress = Progress()
        progress.index = j
        progress.percent = percent
        progress.fileName = name
        progress.count = count
        publishProgress(progress)
    }

    @Throws(IOException::class)
    fun unarchiveRar(item: ExplorerItem): Boolean {
        // 현재 동작안되고 다운됨
        val rar = RarFile(item.path)
        val headers = rar.headers ?: return false
        val size = headers.size
        for (j in 0 until size) {
            if (isCancelled) return false
            updateProgress(j, size, headers[j].name, 100)

            // 하위 폴더가 없을때 만들어줌
            val target = path + "/" + headers[j].name
            File(target).parentFile.mkdirs()
            rar.extractFile(headers[j].name, path!!)
        }
        return true
    }

    @Throws(IOException::class)
    fun unarchiveGzip(item: ExplorerItem): Boolean {
        val buf = ByteArray(BLOCK_SIZE)
        val tar: String = if (item.path.endsWith(".tgz")) {
            item.path.replace(".tgz", ".tar")
        } else {
            item.path.replace(".gz", "")
        }
        val src = File(item.path)
        val stream = GzipCompressorInputStream(FileInputStream(src))
        val outputStream = FileOutputStream(tar)
        val srcLength = src.length()
        var totalRead: Long = 0
        var nRead = 0
        while (stream.read(buf).also { nRead = it } > 0) {
            outputStream.write(buf, 0, nRead)
            totalRead += nRead.toLong()
            val percent = totalRead * 100 / srcLength
            updateProgress(0, count + 1, tar, percent.toInt())
        }
        if (tar.endsWith(".tar")) {
            if (!unarchiveTar(tar, 1)) return false

            // tar 파일 삭제
            File(tar).delete()
        }
        return true
    }

    @Throws(IOException::class)
    fun unarchiveBzip2(item: ExplorerItem): Boolean {
        val buf = ByteArray(BLOCK_SIZE)
        val tar: String
        tar = if (item.path.endsWith(".tbz2")) {
            item.path.replace(".tbz2", ".tar")
        } else {
            item.path.replace(".bz2", "")
        }
        val src = File(item.path)
        val stream = BZip2CompressorInputStream(FileInputStream(src))
        val outputStream = FileOutputStream(tar)
        val srcLength = src.length()
        var totalRead: Long = 0
        var nRead = 0
        while (stream.read(buf).also { nRead = it } > 0) {
            outputStream.write(buf, 0, nRead)
            totalRead += nRead.toLong()
            val percent = totalRead * 100 / srcLength
            updateProgress(0, count + 1, tar, percent.toInt())
        }
        if (tar.endsWith(".tar")) {
            if (!unarchiveTar(tar, 1)) return false

            // tar 파일 삭제
            File(tar).delete()
        }
        return true
    }

    @Throws(IOException::class)
    fun unarchiveTar(tar: String?, add: Int): Boolean {
        var stream: TarArchiveInputStream
        var entry: TarArchiveEntry
        val buf = ByteArray(BLOCK_SIZE)

        // 파일 갯수를 위해서 두번 오픈한다.
        stream = TarArchiveInputStream(FileInputStream(tar))
        entry = stream.nextEntry as TarArchiveEntry
        count = 0

        // 파일 갯수를 세기
        while (entry != null) {
            if (isCancelled) return false
            count++
            entry = stream.nextEntry as TarArchiveEntry
        }
        stream.close()
        stream = TarArchiveInputStream(FileInputStream(tar))
        entry = stream.nextEntry as TarArchiveEntry
        var j = 0
        while (entry != null) {
            if (isCancelled) return false


            // 하위 폴더가 없을때 만들어줌
            val target = path + "/" + entry.name
            File(target).parentFile.mkdirs()

            // 파일 복사 부분
            val outputStream = FileOutputStream(target)
            val srcLength = entry.size
            var totalRead: Long = 0
            var nRead = 0
            while (stream.read(buf).also { nRead = it } > 0) {
                outputStream.write(buf, 0, nRead)
                totalRead += nRead.toLong()
                val percent = totalRead * 100 / srcLength
                updateProgress(j, count + add, entry.name, percent.toInt())
            }
            outputStream.close()
            entry = stream.nextEntry as TarArchiveEntry
            j++
        }
        stream.close()
        return true
    }

    @Throws(IOException::class)
    fun unarchive7z(item: ExplorerItem): Boolean {
        // un7z를 사용함
        val extractor = Z7Extractor(item.path)
        val headers = extractor.headers
        extractor.extractAll(path, object : ExtractCallback() {
            var count = 0
            var j = 0
            override fun onStart() {}
            override fun onGetFileNum(fileNum: Int) {
                count = fileNum
                Timber.e("7z fileNum=$count")
            }

            override fun onProgress(name: String, size: Long) {
                Timber.e("7z onProgress name=$name count=$size j=$j")
                updateProgress(j, count, name, 100)
                j++
            }

            override fun onError(errorCode: Int, message: String) {
                z7success = false
            }

            override fun onSucceed() {
                z7success = true
            }
        })
        return z7success
    }

    @Throws(IOException::class)
    fun unarchiveZip(item: ExplorerItem): Boolean {
//        ZipArchiveInputStream stream;
//        ZipEntry entry;
//
//        stream = new ZipArchiveInputStream(new FileInputStream(item.path));
//        entry = (ZipEntry) stream.getNextEntry();
//        count = 0;
//
//        // 파일 갯수를 세기
//        while (entry != null) {
//            if (isCancelled())
//                return false;
//
//            count++;
//            entry = (ZipEntry) stream.getNextEntry();
//        }
//        stream.close();
//
//        stream = new ZipArchiveInputStream(new FileInputStream(item.path));
//        entry = (ZipEntry) stream.getNextEntry();
//
//        int j = 0;
//        byte[] buf = new byte[BLOCK_SIZE];
//        while (entry != null) {
//            if (isCancelled())
//                return false;
//
//
//            // 하위 폴더가 없을때 만들어줌
//            String target = path + "/" + entry.getName();
//            new File(target).getParentFile().mkdirs();
//
//
//            // 파일 복사 부분
//            FileOutputStream outputStream = new FileOutputStream(target);
//
//            long srcLength = entry.getSize();
//            long totalRead = 0;
//            int nRead = 0;
//            while ((nRead = stream.read(buf)) > 0) {
//                outputStream.write(buf, 0, nRead);
//
//                totalRead += nRead;
//                long percent = totalRead * 100 / srcLength;
//
//                updateProgress(j, count, entry.getName(), (int) percent);
//            }
//
//            outputStream.close();
//
//
//            entry = (ZipEntry) stream.getNextEntry();
//            j++;
//        }
//
//        stream.close();
        val zip4j = Zip4jFile(item.path)
        val activity = activityWeakReference.get()
        if (activity != null) {
            val charset = getFileNameCharset(activity)
            if (charset != null) {
                zip4j.setFileNameCharset(charset)
            }
        }
        val headers = zip4j.headers ?: return false
        val size = headers.size
        for (j in 0 until size) {
            if (isCancelled) return false
            updateProgress(j, size, headers[j].name, 100)

            // 하위 폴더가 없을때 만들어줌
            val target = path + "/" + headers[j].name
            File(target).parentFile.mkdirs()
            zip4j.extractFile(headers[j].name, path!!)
        }
        return true
    }

    protected override fun doInBackground(vararg voids: Void?): Boolean {
        // 파일의 갯수만큼 루프를 돌아서 unzip 해준다.
//        for (int i = 0; i < fileList.size(); i++) {
        val item = fileList!![0]
        try {
            val type = getCompressType(item.path)
            when (type) {
                ExplorerItem.COMPRESSTYPE_ZIP -> if (!unarchiveZip(item)) return false
                ExplorerItem.COMPRESSTYPE_SEVENZIP -> if (!unarchive7z(item)) return false
                ExplorerItem.COMPRESSTYPE_GZIP -> if (!unarchiveGzip(item)) return false
                ExplorerItem.COMPRESSTYPE_BZIP2 -> if (!unarchiveBzip2(item)) return false
                ExplorerItem.COMPRESSTYPE_RAR -> if (!unarchiveRar(item)) return false
                ExplorerItem.COMPRESSTYPE_TAR -> if (!unarchiveTar(item.path, 0)) return false
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        //        }
        return true
    }

    //TODO: ZIP 현재는 파일 한개씩 밖에 풀수가 없다.
    protected override fun onProgressUpdate(vararg values: Progress?) {
        val progress = values[0] ?: return
        val dialog = dialogWeakReference!!.get()
        if (dialog != null) {
            dialog.fileName!!.text = progress.fileName
            dialog.eachProgress!!.progress = progress.percent
            val totalPercent = progress.index * 100 / progress.count
            dialog.totalProgress!!.progress = totalPercent
            val activity = activityWeakReference.get()
            if (activity != null) {
                dialog.eachText!!.visibility = View.VISIBLE
                dialog.eachText!!.text =
                    String.format(activity.getString(R.string.each_text), progress.percent)
                dialog.totalText!!.visibility = View.VISIBLE
                dialog.totalText!!.text = String.format(
                    activity.getString(R.string.total_text),
                    progress.index + 1,
                    progress.count,
                    totalPercent
                )
            }
        }
    }

    override fun onPostExecute(result: Boolean) {
        super.onPostExecute(result)
        val activity = activityWeakReference.get()
        if (activity != null) {
            if (result) success(activity, R.string.toast_file_unzip) else error(
                activity,
                R.string.toast_cancel
            )
        }
        val dialog = dialogWeakReference!!.get()
        dialog?.dismiss()
    }

    init {
        activityWeakReference = WeakReference(activity)
    }
}