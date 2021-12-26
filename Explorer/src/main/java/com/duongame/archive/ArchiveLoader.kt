package com.duongame.archive

import android.content.Context
import android.os.AsyncTask
import com.duongame.adapter.ExplorerItem
import com.duongame.bitmap.BitmapLoader
import com.duongame.file.FileHelper.NameAscComparator
import com.duongame.file.FileHelper.getCompressType
import com.duongame.file.FileHelper.getFileNameCharset
import com.duongame.file.FileHelper.getFullPath
import com.duongame.file.FileHelper.getZipCacheFile
import com.duongame.file.FileHelper.getZipCachePath
import com.duongame.file.FileHelper.isImage
import com.duongame.task.zip.LoadBookTask
import net.lingala.zip4j.exception.ZipException
import java.io.File
import java.util.*

/**
 * Created by namjungsoo on 2018-01-23.
 */
class ArchiveLoader {
    private var task: LoadBookTask? = null
    private var extractPath: String? = null
    private var side = ExplorerItem.SIDE_LEFT
    private var extract = 0
    private var listener: ArchiveLoaderListener? = null
    private var zipHeaders: List<ArchiveHeader>? = null
    private var zipFile: ArchiveFile? = null

    interface ArchiveLoaderListener {
        fun onSuccess(i: Int, zipImageList: ArrayList<ExplorerItem>?, totalFileCount: Int)
        fun onFail(i: Int, name: String?)
        fun onFinish(zipImageList: ArrayList<ExplorerItem>?, totalFileCount: Int)
    }

    fun cancelTask() {
        task?.cancel(true)
    }

    fun setZipImageList(zipImageList: ArrayList<ExplorerItem>?) {
        if (task?.isCancelled == false) {
            task?.setZipImageList(zipImageList)
        }
    }

    fun setSide(side: Int) {
        if (task?.isCancelled == false) {
            task?.setSide(side)
        }
    }

    fun pause() {
        if (task?.isCancelled == false) {
            task?.setPauseWork(true)
        }
    }

    fun resume() {
        if (task?.isCancelled == false) {
            task?.setPauseWork(false)
        }
    }

    private fun filterImageList(imageList: ArrayList<ExplorerItem>?) {
        if (zipHeaders == null) return
        if (imageList == null) return
        for ((name, size) in zipHeaders!!) {
            if (isImage(name)) {
                imageList.add(
                    ExplorerItem(
                        getFullPath(extractPath!!, name),
                        name,
                        null,
                        size,
                        ExplorerItem.FILETYPE_IMAGE
                    )
                )
            }
        }
        Collections.sort(imageList, NameAscComparator())
    }

    private fun loadFirstImageOnly(imageList: ArrayList<ExplorerItem>): ArrayList<ExplorerItem>? {
        if (imageList.size > 0) {
            // 이미지 로딩후 확인해보고 좌우를 나눠야 되면 나누어 주자
            // 파일명으로 실제 폴더 안에 파일이 있는지 검사
            val file = File(imageList[0].name)
            if (!file.exists()) {
                if (!zipFile!!.extractFile(imageList[0].name, extractPath!!)) return null
            } else {
                // 이미 있으면, 사이즈가 같은지 확인하고 이 파일을 로딩하자
                if (file.length() == imageList[0].size) {
                    return imageList
                }
            }
            val options = BitmapLoader.decodeBounds(imageList[0].path)

            // 일본식(RIGHT)를 기준으로 잡자
            // 현재는 LEFT가 기본인데 이것을 설정에서 설정하도록 하자
            if (options.outWidth > options.outHeight) {
                imageList[0].side = side
            }
            return imageList
        }
        return null
    }

    private fun loadNew(
        imageList: ArrayList<ExplorerItem>,
        firstList: ArrayList<ExplorerItem>
    ): ArrayList<ExplorerItem> {
        val item = imageList[0].clone() as ExplorerItem
        val options = BitmapLoader.decodeBounds(item.path)

        // 일본식(RIGHT)를 기준으로 잡자
        if (options.outWidth > options.outHeight) {
            item.side = side
        }
        firstList.add(item)
        task = LoadBookTask(zipFile, imageList, listener, extract, null)
        task!!.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, extractPath)
        return firstList
    }

    private fun loadContinue(
        imageList: ArrayList<ExplorerItem>,
        firstList: ArrayList<ExplorerItem>
    ): ArrayList<ExplorerItem> {
        // extract 미만의 파일들은 파일이 존재한다고 가정하고 시작한 것이다.
        if (extract > imageList.size) // 에러인 경우이다.
            extract = imageList.size
        for (i in 0 until extract) {
            // 여기서 파일의 존재여부에 대한 검증을 해야 한다.
            try {
                val item = imageList[i]
                if (isFileExtracted(item.path, zipHeaders)) {
                    LoadBookTask.processItem(i, item, side, firstList)
                } else {
                    // 에러이므로 여기서 중단한다.
                    extract = i
                    break
                }
            } catch (e: IndexOutOfBoundsException) {
                return firstList
            }
        }

        // 읽어보고 나머지 리스트에 대해서 추가해줌
        task = LoadBookTask(
            zipFile,
            imageList,
            listener,
            extract,
            firstList.clone() as ArrayList<ExplorerItem>?
        )
        task!!.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, extractPath)
        return firstList
    }

    private fun isFileExtracted(path: String, headers: List<ArchiveHeader>?): Boolean {
        val file = File(path)
        if (!file.exists()) return false
        for (i in headers!!.indices) {
            if (file.name == headers[i].name) {
                if (file.length() == headers[i].size) return true
            }
        }
        return false
    }

    // 리턴값은 이미지 리스트이다.
    // 압축을 풀지 않으면 정보를 알수가 없다. 좌우 잘라야 되는지 마는지를
    @Throws(ZipException::class)
    fun load(
        context: Context?,
        filename: String?,
        listener: ArchiveLoaderListener?,
        extract: Int,
        side: Int,
        firstImageOnly: Boolean
    ): ArrayList<ExplorerItem>? {
        // 일단 무조건 압축 풀자
        //TODO: 이미 전체 압축이 풀려있는지 검사해야함
        makeCachePath(context, filename)
        this.side = side
        this.listener = listener
        this.extract = extract
        val type = getCompressType(filename!!)
        zipFile = when (type) {
            ExplorerItem.COMPRESSTYPE_ZIP -> Zip4jFile(filename)
            ExplorerItem.COMPRESSTYPE_RAR -> RarFile(filename)
            ExplorerItem.COMPRESSTYPE_SEVENZIP -> SevenFile(filename)
            else -> return null
        }

        //zipFile = new net.lingala.zip4j.core.ZipFile(filename);
        //zipFile.setFileNameCharset(zipEncoding);// 일단 무조건 한국 사용자를 위해서 이렇게 설정함
        val charset = getFileNameCharset(context!!)
        if (charset != null) {
            zipFile!!.setFileNameCharset(charset)
        }
        zipHeaders = zipFile!!.headers
        if (zipHeaders == null) return null
        extractPath = getZipCachePath(context, filename) // 파일이 풀릴 예상 경로
        val imageList = ArrayList<ExplorerItem>()
        filterImageList(imageList) // 이미지 파일만 추출함

        // 처음 이미지만 풀 경우에는 처음 이미지 파일 한개만 풀고 끝낸다.
        if (firstImageOnly) {
            return loadFirstImageOnly(imageList)
        } else {
            if (imageList.size > 0) {

                // 아무것도 안들어 있는 리스트
                // firstList의 존재이유는 동기적으로 이미지를 읽어서 액티비티에 빠르게 보여주게 하기 위함
                val firstList = ArrayList<ExplorerItem>()

                // 이미지 파일이 있으면 첫번째 페이지만 추가해줌
                // 왜 이렇게 했냐면 새로 읽었을때 빠르게 추가해 주기 위해서
                // 안해도 무방하다
                return if (extract == 0) {
                    loadNew(imageList, firstList)
                } else {
                    loadContinue(imageList, firstList)
                }
            }
        }
        return imageList
    }

    companion object {
        fun makeCachePath(context: Context?, filename: String?): Boolean {
            val cacheFile = getZipCacheFile(context!!, filename!!)
            val ret = cacheFile.exists()

            // 폴더가 없으면 만들어 준다.
            if (!ret) {
                cacheFile.mkdirs()
            }
            return ret
        }
    }
}