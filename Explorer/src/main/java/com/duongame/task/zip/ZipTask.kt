package com.duongame.task.zip

import android.app.Activity
import android.content.DialogInterface
import android.os.AsyncTask
import android.view.View
import com.duongame.R
import com.duongame.adapter.ExplorerItem
import com.duongame.dialog.ZipDialog
import com.duongame.file.FileExplorer
import com.duongame.file.FileHelper.BLOCK_SIZE
import com.duongame.file.FileHelper.Progress
import com.duongame.file.FileHelper.getCompressType
import com.duongame.file.FileHelper.getParentPath
import com.duongame.file.LocalExplorer
import com.duongame.helper.ToastHelper.error
import com.duongame.helper.ToastHelper.success
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import java.io.*
import java.lang.ref.WeakReference
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Created by namjungsoo on 2017-12-31.
 */
class ZipTask(activity: Activity) : AsyncTask<Void?, Progress?, Boolean>() {
    private var fileList: ArrayList<ExplorerItem>? = null
    private var zipList: ArrayList<ExplorerItem>? = null
    private var dialogWeakReference: WeakReference<ZipDialog>? = null
    private val activityWeakReference: WeakReference<Activity> = WeakReference(activity)
    private var onDismissListener: DialogInterface.OnDismissListener? = null
    private var path: String? = null
    private var type = 0
    fun setOnDismissListener(listener: DialogInterface.OnDismissListener?) {
        onDismissListener = listener
    }

    // 압축할 파일의 목록
    fun setFileList(fileList: ArrayList<ExplorerItem>?) {
        this.fileList = fileList
    }

    // 압축 대상 파일의 패스
    fun setPath(path: String?) {
        this.path = path
        type = getCompressType(path!!)
    }

    override fun onPreExecute() {
        super.onPreExecute()

        // Dialog의 초기화를 진행한다.
        dialogWeakReference = WeakReference(ZipDialog())
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
                dialog.show(activity.fragmentManager, "zip")
            }
        }
    }

    fun updateProgress(i: Int, name: String?, percent: Int) {
        val progress = Progress()
        progress.index = i
        progress.fileName = name
        //progress.percent = i * 100 / zipList.count();
        progress.percent = percent
        publishProgress(progress)
    }

    fun archiveTar(): Boolean {
        val tar = path!!.replace(".gz", "")
        var stream: TarArchiveOutputStream? = null
        try {
            stream = TarArchiveOutputStream(FileOutputStream(tar))
            val buf = ByteArray(BLOCK_SIZE)
            for (i in zipList!!.indices) {
                val entry = TarArchiveEntry(zipList!![i].name)
                val src = File(zipList!![i].path)
                val inputStream = FileInputStream(zipList!![i].path)
                val srcLength = src.length()
                var totalRead: Long = 0
                var nRead = 0
                entry.size = srcLength
                stream.putArchiveEntry(entry)
                while (inputStream.read(buf).also { nRead = it } > 0) {
                    stream.write(buf, 0, nRead)
                    totalRead += nRead.toLong()
                    val percent = totalRead * 100 / srcLength
                    updateProgress(i, zipList!![i].name, percent.toInt())
                }
                stream.closeArchiveEntry()
            }
            stream.finish()
            stream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return false
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    //region Tar를 먼저 수행하는 압축
    fun archiveGzip(): Boolean {
        if (!archiveTar()) return false
        val tar = path!!.replace(".gz", "")
        try {
            // gzip 압축
            val src = File(tar)
            val inputStream = FileInputStream(src)
            val buf = ByteArray(BLOCK_SIZE)
            val outputStream = BufferedOutputStream(FileOutputStream(path))
            val stream = GzipCompressorOutputStream(outputStream)
            val srcLength = src.length()
            var totalRead: Long = 0
            var nRead = 0
            while (inputStream.read(buf).also { nRead = it } > 0) {
                stream.write(buf, 0, nRead)
                totalRead += nRead.toLong()
                val percent = totalRead * 100 / srcLength
                updateProgress(zipList!!.size, path, percent.toInt())
            }
            stream.close()
            inputStream.close()

            // tar 삭제
            src.delete()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return false
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun archiveBzip2(): Boolean {
        if (!archiveTar()) return false
        val tar = path!!.replace(".bz2", "")
        try {
            // gzip 압축
            val src = File(tar)
            val inputStream = FileInputStream(src)
            val buf = ByteArray(BLOCK_SIZE)
            val outputStream = BufferedOutputStream(FileOutputStream(path))
            val stream = BZip2CompressorOutputStream(outputStream)
            val srcLength = src.length()
            var totalRead: Long = 0
            var nRead = 0
            while (inputStream.read(buf).also { nRead = it } > 0) {
                stream.write(buf, 0, nRead)
                totalRead += nRead.toLong()
                val percent = totalRead * 100 / srcLength
                updateProgress(zipList!!.size, path, percent.toInt())
            }
            stream.close()
            inputStream.close()

            // tar 삭제
            src.delete()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return false
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    //endregion
    // 현재 지원 안됨
    fun archive7z(): Boolean {
        return true
    }

    fun archiveZip(): Boolean {
        try {
            val src = File(path)
            val stream = ZipOutputStream(FileOutputStream(src))
            //            ZipArchiveOutputStream stream = new ZipArchiveOutputStream(src);
            val buf = ByteArray(BLOCK_SIZE)
            for (i in zipList!!.indices) {
                val entry = ZipEntry(zipList!![i].name)
                //                ZipArchiveEntry entry = new ZipArchiveEntry(zipList.get(i).name);
                val inputStream = FileInputStream(zipList!![i].path)
                stream.putNextEntry(entry)
                //                stream.putArchiveEntry(entry);
                val srcLength = src.length()
                var totalRead: Long = 0
                var nRead = 0
                while (inputStream.read(buf).also { nRead = it } > 0) {
                    stream.write(buf, 0, nRead)
                    totalRead += nRead.toLong()
                    val percent = totalRead * 100 / srcLength
                    updateProgress(i, zipList!![i].name, percent.toInt())
                }
                stream.closeEntry()
                //                stream.closeArchiveEntry();
            }
            stream.finish()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun makeZipList() {
        zipList = ArrayList()

        // fileList에는 선택된 파일만 들어옴
        for (i in fileList!!.indices) {
            val item = fileList!![i]
            if (item.type == ExplorerItem.FILETYPE_FOLDER) {
                // 폴더의 경우 하위 모든 아이템을 찾은뒤에 더한다.
                val explorer: FileExplorer = LocalExplorer()
                val result = explorer.apply {
                    isRecursiveDirectory = true
                    isHiddenFile = true
                    isExcludeDirectory = false
                    isImageListEnable = false
                }.search(item.path)

                // 폴더 하위 파일의 경우에는 폴더 이름과 파일명을 적어줌
                if (result != null) {
                    for (j in result.fileList.indices) {

                        // 선택된 폴더의 최상위 폴더의 폴더명을 제외한 나머지가 name임
                        val subItem = result.fileList[j]
                        if (subItem.path.startsWith(item.path)) {
                            subItem.name = subItem.path.replace(getParentPath(item.path) + "/", "")
                        }
                    }
                    zipList!!.addAll(result.fileList)
                }

                // 폴더 자기 자신도 더함
                // zip은 더하지 않음
//                zipList.add(item);
            } else {
                zipList!!.add(item)
            }
        }
    }

    protected override fun doInBackground(vararg voids: Void?): Boolean {
        // 선택된 파일이 폴더인 경우에 전체 폴더 확장을 해야한다.
        makeZipList()
        when (type) {
            ExplorerItem.COMPRESSTYPE_ZIP -> return archiveZip()
            ExplorerItem.COMPRESSTYPE_GZIP -> return archiveGzip()
            ExplorerItem.COMPRESSTYPE_BZIP2 -> return archiveBzip2()
            ExplorerItem.COMPRESSTYPE_TAR -> return archiveTar()
        }
        return false
    }

    protected override fun onProgressUpdate(vararg values: Progress?) {
        val progress = values[0] ?: return
        val dialog = dialogWeakReference!!.get()
        if (dialog != null) {
            dialog.fileName!!.text = progress.fileName
            dialog.eachProgress!!.progress = progress.percent

            // 들어온 퍼센트를 바로 토탈로 표시한다.
            //int totalPercent = progress.percent;
            val size: Int
            size =
                if (type == ExplorerItem.COMPRESSTYPE_GZIP || type == ExplorerItem.COMPRESSTYPE_BZIP2) {
                    zipList!!.size + 1
                } else {
                    zipList!!.size
                }
            val totalPercent = progress.index * 100 / size
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
                    size,
                    totalPercent
                )
            }
        }
    }

    override fun onPostExecute(result: Boolean) {
        super.onPostExecute(result)
        val activity = activityWeakReference.get()
        if (activity != null) {
            if (result) success(activity, R.string.toast_file_zip) else error(
                activity,
                R.string.toast_cancel
            )
        }
        val dialog = dialogWeakReference!!.get()
        dialog?.dismiss()
    }

}