package com.duongame.file

import android.content.Context
import com.duongame.helper.DateHelper.getDateFromExplorerDateString
import com.duongame.file.FileHelper
import android.os.Build
import com.duongame.adapter.ExplorerItem
import com.duongame.R
import com.duongame.helper.DateHelper
import java.io.File
import java.lang.Exception
import java.lang.StringBuilder
import java.text.DecimalFormat
import java.util.*

/**
 * Created by namjungsoo on 2016-11-19.
 */
object FileHelper {
    private val formatter = DecimalFormat("#,###.##")
    private const val MEGA = (1024 * 1024).toLong()
    private const val KILO: Long = 1024
    private const val GIGA = (1024 * 1024 * 1024).toLong()
    const val BLOCK_SIZE = 8 * KILO.toInt()

    fun getFileNameCharset(context: Context): String? {
        val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            context.resources.configuration.locale
        }
        var charset: String? = null
        when (locale.language) {
            "ko" -> charset = "cp949"
            "ja" -> charset = "cp932"
            "zh" -> charset = "cp936"
        }
        return charset
    }

    fun getNameWithoutTar(name: String): String {
        return if (name.endsWith(".tar") || name.endsWith(".TAR")) {
            name.substring(0, name.length - 4)
            //return tar.replace(".tar", "");
        } else {
            name
        }
    }

    fun getCommaSize(size: Long): String {
        return if (size < 0) "" else formatter.format(size)
    }

    fun getFileSize(path: String?): Long {
        return try {
            val file = File(path)
            file.length()
        } catch (e: Exception) {
            0
        }
    }

    fun getFileName(path: String): String {
        return path.substring(path.lastIndexOf("/") + 1)
    }

    // Zip 압축을 풀때 기존에 폴더가 있으면 새로운 폴더명으로 풀어준다.
    // 폴더를 생성할때는 새로운 폴더명이 있으면 있다고 확인을 한다.
    fun getNewFileName(path: String): String {
        val file = File(path)
        if (!file.exists()) return path
        val base: String
        var ext: String? = null
        if (file.isDirectory) {
            base = path
        } else {
            // 여기서 확장자가 없을수도 있음
            // base, ext 둘다 에러 발생 가능함
            base = path.substring(0, path.lastIndexOf("."))
            ext = path.substring(path.lastIndexOf("."))
        }
        var index = 1
        while (true) {
            val candidate = makeCandidateFileName(base, ext, index)
            val newFile = File(candidate)
            if (!newFile.exists()) return candidate
            index++
        }
    }

    private fun makeCandidateFileName(base: String, ext: String?, index: Int): String {
        val builder = StringBuilder()
        builder.append(base)
        builder.append(" (")
            .append(index)
            .append(")")
        if (ext != null) {
            builder.append(ext)
        }
        return builder.toString()
    }

    fun getMinimizedSize(size: Long): String {
        if (size < 0) return ""
        return when {
            size > GIGA -> {
                val newsize = size.toDouble() / GIGA
                formatter.format(newsize) + " GB"
            }
            size > MEGA -> {
                val newsize = size.toDouble() / MEGA
                formatter.format(newsize) + " MB"
            }
            size > KILO -> {
                val newsize = size.toDouble() / KILO
                formatter.format(newsize) + " KB"
            }
            else -> {
                formatter.format(size) + " B"
            }
        }
    }

    fun getCompressType(path: String): Int {
        if (path.endsWith(".zip") || path.endsWith(".cbz") ||
            path.endsWith(".ZIP") || path.endsWith(".CBZ")
        ) return ExplorerItem.COMPRESSTYPE_ZIP
        if (path.endsWith(".rar") || path.endsWith(".cbr") ||
            path.endsWith(".RAR") || path.endsWith(".CBR")
        ) return ExplorerItem.COMPRESSTYPE_RAR
        if (path.endsWith(".7z") || path.endsWith(".cb7") ||
            path.endsWith(".7Z") || path.endsWith(".CB7")
        ) return ExplorerItem.COMPRESSTYPE_SEVENZIP
        if (path.endsWith(".tar") || path.endsWith(".cbt") ||
            path.endsWith(".TAR") || path.endsWith(".CBT")
        ) return ExplorerItem.COMPRESSTYPE_TAR
        return if (path.endsWith(".gz") || path.endsWith(".tgz") ||
            path.endsWith(".GZ") || path.endsWith(".TGZ")
        ) ExplorerItem.COMPRESSTYPE_GZIP else ExplorerItem.COMPRESSTYPE_OTHER
        //TODO: 테스트 더해보고 안되면 막아야 함
//        if (path.endsWith(".bz2") || path.endsWith(".tbz2"))
//            return ExplorerItem.CompressType.BZIP2;
    }

    //region Extension
    fun getExtType(eachFile: File?, fileType: Int): Int {
        return 0
    }

    // 파일 타입과 아이콘 타입은 종류가 다르므로 직접 입력하도록 함
    fun getFileFolderIconResId(fileName: String): Int {
        val type = getFileFolderType(fileName)
        var resId = R.drawable.ic_file_normal
        when (type) {
            ExplorerItem.FILETYPE_FOLDER -> resId = R.drawable.ic_file_folder
            ExplorerItem.FILETYPE_IMAGE -> {
                val lowerFileName = fileName.toLowerCase()
                resId = if (lowerFileName.endsWith(".gif")) R.drawable.ic_file_jpg // gif가 없음
                else if (lowerFileName.endsWith(".png")) R.drawable.ic_file_png else R.drawable.ic_file_jpg
            }
            ExplorerItem.FILETYPE_ZIP -> resId = R.drawable.ic_file_zip
            ExplorerItem.FILETYPE_PDF -> resId = R.drawable.ic_file_pdf
            ExplorerItem.FILETYPE_VIDEO -> {
                val lowerFileName = fileName.toLowerCase()
                resId =
                    if (lowerFileName.endsWith(".mp4")) R.drawable.ic_file_mp4 else if (lowerFileName.endsWith(
                            ".fla"
                        )
                    ) R.drawable.ic_file_fla else R.drawable.ic_file_avi
            }
            ExplorerItem.FILETYPE_AUDIO -> resId = R.drawable.ic_file_mp3
            ExplorerItem.FILETYPE_TEXT -> resId = R.drawable.ic_file_txt
            ExplorerItem.FILETYPE_APK -> resId = R.drawable.ic_file_apk
        }
        return resId
    }

    // 폴더는 제외한다
    fun getFileType(fileName: String): Int {
        var type = ExplorerItem.FILETYPE_FILE

        // 이미지
        if (isImage(fileName)) type =
            ExplorerItem.FILETYPE_IMAGE else if (getCompressType(fileName) != ExplorerItem.COMPRESSTYPE_OTHER) type =
            ExplorerItem.FILETYPE_ZIP else if (fileName.endsWith(".pdf") || fileName.endsWith(".PDF")) type =
            ExplorerItem.FILETYPE_PDF else if (fileName.endsWith(".mp4") || fileName.endsWith(".avi") || fileName.endsWith(
                ".3gp"
            ) || fileName.endsWith(".mkv") || fileName.endsWith(".mov")
        ) type =
            ExplorerItem.FILETYPE_VIDEO else if (fileName.endsWith(".mp3") || fileName.endsWith(".MP3")) type =
            ExplorerItem.FILETYPE_AUDIO else if (isText(fileName)) type =
            ExplorerItem.FILETYPE_TEXT else if (fileName.endsWith(".apk") || fileName.endsWith(".APK")) type =
            ExplorerItem.FILETYPE_APK
        return type
    }

    fun getFileFolderType(fileName: String?): Int {
        val file = File(fileName)
        return getFileFolderType(file)
    }

    fun getFileFolderType(eachFile: File): Int {
        val type =
            if (eachFile.isDirectory) ExplorerItem.FILETYPE_FOLDER else ExplorerItem.FILETYPE_FILE

        // 폴더면 바로 리턴
        return if (type == ExplorerItem.FILETYPE_FOLDER) type else getFileType(eachFile.name)
    }

    fun isVideo(filename: String?): Boolean {
        return false
    }

    fun isText(filename: String): Boolean {
        return filename.endsWith(".txt") || filename.endsWith(".log") || filename.endsWith(".json") ||
            filename.endsWith(".TXT") || filename.endsWith(".LOG") || filename.endsWith(".JSON")
    }

    fun isImage(filename: String): Boolean {
        return isJpegImage(filename) || isPngImage(filename) || isGifImage(
                filename
            )
    }

    fun isGifImage(filename: String): Boolean {
        return filename.endsWith(".gif") ||
            filename.endsWith(".GIF")
    }

    fun isPngImage(filename: String): Boolean {
        return filename.endsWith(".png") ||
            filename.endsWith(".PNG")
    }

    fun isJpegImage(filename: String): Boolean {
        return filename.endsWith(".jpg") || filename.endsWith(".jpeg") ||
            filename.endsWith(".JPG") || filename.endsWith(".JPEG")
    }
    //endregion

    fun getZipCacheFile(context: Context, filename: String): File {
        val filesDir = context.filesDir.absolutePath
        val cachePath = "$filesDir/$filename"
        return File(cachePath)
    }

    fun getZipCachePath(context: Context, filename: String): String {
        val filesDir = context.filesDir.absolutePath
        return "$filesDir/$filename"
    }

    //    public static class PriorityAscComparator implements Comparator<ExplorerItem> {
    //        @Override
    //        public int compare(ExplorerItem lhs, ExplorerItem rhs) {
    //            if (lhs.priority < rhs.priority)
    //                return -1;
    //            else if (lhs.priority > rhs.priority)
    //                return 1;
    //            return 0;
    //        }
    //    }
    //endregion

    fun setPdfFileNameFromPage(pdf: String, page: Int): String {
        return "$pdf.$page"
    }

    fun getPdfPageFromFileName(pdfWithPage: String): Int {
        val page = pdfWithPage.substring(pdfWithPage.lastIndexOf(".") + 1)
        return page.toInt()
    }

    fun getFullPath(path: String, name: String): String {
        return "$path/$name"
    }

    // 마지막에 /를 포함하지 않는다.
    fun getParentPath(path: String): String {
        return path.substring(0, path.lastIndexOf('/'))
    }

    fun filterImageFileList(fileList: List<ExplorerItem>): ArrayList<ExplorerItem> {
        val imageList = ArrayList<ExplorerItem>()
        for (item in fileList) {
            if (item.type == ExplorerItem.FILETYPE_IMAGE) {
                imageList.add(item)
            }
        }
        return imageList
    }

    fun filterVideoFileList(fileList: List<ExplorerItem>): ArrayList<ExplorerItem> {
        val videoList = ArrayList<ExplorerItem>()
        for (item in fileList) {
            if (item.type == ExplorerItem.FILETYPE_VIDEO) {
                videoList.add(item)
            }
        }
        return videoList
    }

    fun filterAudioFileList(fileList: List<ExplorerItem>): ArrayList<ExplorerItem> {
        val audioList = ArrayList<ExplorerItem>()
        for (item in fileList) {
            if (item.type == ExplorerItem.FILETYPE_AUDIO) {
                audioList.add(item)
            }
        }
        return audioList
    }

    class Progress {
        var percent = 0
        var index = 0
        var fileName: String? = null
        var count = 0
    }

    //region Comparator
    // 파일 이름은 이름만 정렬하면 되는데
    // 크기나 확장자는 정렬후에 이름으로 한번더 정렬 하여야 한다
    class NameAscComparator : Comparator<ExplorerItem> {
        override fun compare(lhs: ExplorerItem, rhs: ExplorerItem): Int {
            return lhs.name.compareTo(rhs.name, ignoreCase = true)
        }
    }

    class NameDescComparator : Comparator<ExplorerItem> {
        override fun compare(lhs: ExplorerItem, rhs: ExplorerItem): Int {
            return rhs.name.compareTo(lhs.name, ignoreCase = true)
        }
    }

    class DateAscComparator : Comparator<ExplorerItem> {
        override fun compare(lhs: ExplorerItem, rhs: ExplorerItem): Int {
            val lhsDate = getDateFromExplorerDateString(lhs.date)
            val rhsDate = getDateFromExplorerDateString(rhs.date)
            return lhsDate.compareTo(rhsDate)
        }
    }

    class DateDescComparator : Comparator<ExplorerItem> {
        override fun compare(lhs: ExplorerItem, rhs: ExplorerItem): Int {
            val lhsDate = getDateFromExplorerDateString(lhs.date)
            val rhsDate = getDateFromExplorerDateString(rhs.date)
            return rhsDate.compareTo(lhsDate)
        }
    }

    class ExtAscComparator : Comparator<ExplorerItem> {
        override fun compare(lhs: ExplorerItem, rhs: ExplorerItem): Int {
            val lhsExt = lhs.ext
            val rhsExt = rhs.ext
            val ret = lhsExt.compareTo(rhsExt, ignoreCase = true)
            return if (ret == 0) {
                lhs.name.compareTo(rhs.name, ignoreCase = true)
            } else {
                ret
            }
        }
    }

    class ExtDescComparator : Comparator<ExplorerItem> {
        override fun compare(lhs: ExplorerItem, rhs: ExplorerItem): Int {
            val lhsExt = lhs.ext
            val rhsExt = rhs.ext
            val ret = rhsExt.compareTo(lhsExt, ignoreCase = true)
            return if (ret == 0) {
                rhs.name.compareTo(lhs.name, ignoreCase = true)
            } else {
                ret
            }
        }
    }

    class SizeAscComparator : Comparator<ExplorerItem> {
        override fun compare(lhs: ExplorerItem, rhs: ExplorerItem): Int {
            val ret = lhs.size - rhs.size
            return if (ret == 0L) {
                lhs.name.compareTo(rhs.name, ignoreCase = true)
            } else {
                if (ret < 0) -1 else 1
            }
        }
    }

    class SizeDescComparator : Comparator<ExplorerItem> {
        override fun compare(lhs: ExplorerItem, rhs: ExplorerItem): Int {
            val ret = rhs.size - lhs.size
            return if (ret == 0L) {
                rhs.name.compareTo(lhs.name, ignoreCase = true)
            } else {
                if (ret < 0) -1 else 1
            }
        }
    }
}