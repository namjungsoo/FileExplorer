package com.duongame.file

import com.duongame.adapter.ExplorerItem
import java.io.File
import java.util.*

abstract class FileExplorer {
    class Result {
        var fileList: ArrayList<ExplorerItem> = arrayListOf()
        var imageList: ArrayList<ExplorerItem> = arrayListOf()
        var videoList: ArrayList<ExplorerItem> = arrayListOf()
        var audioList: ArrayList<ExplorerItem> = arrayListOf()
    }

    var extensions: ArrayList<String>? = null
    var keyword: String? = null
    var comparator: Comparator<ExplorerItem>? = null
    var isExcludeDirectory = false
    var isRecursiveDirectory = false
    var isHiddenFile = false
    var isImageListEnable = false
    var isVideoListEnable = false
    var isAudioListEnable = false

    abstract fun search(path: String?): Result

    class DirectoryPreferComparator : Comparator<File> {
        override fun compare(lhs: File, rhs: File): Int {
            if (lhs.isDirectory) return -1
            return if (rhs.isDirectory) 1 else 0
        }
    }
}