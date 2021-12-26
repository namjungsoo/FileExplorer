package com.duongame.file

import com.duongame.adapter.ExplorerItem
import com.duongame.file.FileHelper.filterAudioFileList
import com.duongame.file.FileHelper.getFileFolderType
import com.duongame.file.FileHelper.getFullPath
import com.duongame.file.FileHelper.filterImageFileList
import com.duongame.file.FileHelper.filterVideoFileList
import com.duongame.helper.DateHelper.getExplorerDateString
import timber.log.Timber
import java.io.File
import java.util.*

/**
 * Created by namjungsoo on 2016-11-06.
 */
class LocalExplorer : FileExplorer() {
    override fun search(path: String?): Result {
        Timber.e("LocalExplorer search begin")
        val file = File(path)

        // 모든 파일 가져옴
        val files = file.listFiles() ?: return Result()
        Timber.e("LocalExplorer listFiles end %s", files.size)

        // 폴더를 우선하도록 정렬 해야 함
        // 안드로이드는 폴더와 파일을 섞어서 리턴을 해준다.
        Collections.sort(listOf(*files), DirectoryPreferComparator())
        Timber.e("LocalExplorer listFiles sort end")
        val fileList = ArrayList<ExplorerItem>()
        val directoryList = ArrayList<ExplorerItem>()
        val normalList = ArrayList<ExplorerItem>()
        val result = Result()

        // 파일로 아이템을 만듬
        for (eachFile in files) {
            //if (eachFile.getName().equals(".") || eachFile.getName().equals("..")) {// .으로 시작되면 패스 함
            if (!isHiddenFile && eachFile.name.startsWith(".")) { // .으로 시작되면 패스 함 (숨김파일임)
                continue
            }
            val name = eachFile.name
            val dateSource = Date(eachFile.lastModified())

            //String date = dateFormat.format(dateSource);
            //String date = dateSource.toString();
            val date = getExplorerDateString(dateSource)
            val size = eachFile.length()
            val type = getFileFolderType(eachFile)
            val fullPath = getFullPath(path!!, name)
            val item = ExplorerItem(fullPath, name, date, size, type)
            if (type == ExplorerItem.FILETYPE_FOLDER) {
                if (!isExcludeDirectory) {
                    item.size = -1
                    directoryList.add(item)
                }
                if (isRecursiveDirectory) {
                    val subFileList = search(fullPath)
                    if (subFileList != null) {
                        for (subItem in subFileList.fileList!!) {
                            if (subItem.type == ExplorerItem.FILETYPE_FOLDER) {
                                directoryList.add(subItem)
                            } else {
                                normalList.add(subItem)
                            }
                        }
                    }
                }
            } else {
                // 파일이므로 더할지 말지 결정을 해야 함
                var willAdd = true
                if (extensions != null) { // 확장자가 맞으므로 더함
                    willAdd = false
                    for (ext in extensions!!) {
                        willAdd = willAdd or item.name.endsWith(ext)
                    }
                }
                if (keyword != null && willAdd) { // 키워드를 포함하므로 더함
                    willAdd = item.name.contains(keyword!!)
                }
                if (willAdd) normalList.add(item)
            }
        }
        Timber.e("LocalExplorer file item end")
        //        if (comparator == null) {
//            comparator = new FileHelper.NameAscComparator();
//        }
        if (comparator != null) {
            // 디렉토리 우선 정렬 및 가나다 정렬
            Collections.sort(directoryList, comparator)
            Collections.sort(normalList, comparator)
            fileList.addAll(directoryList)
            fileList.addAll(normalList)
        } else {
            // 삭제 리스트용이므로 파일먼저, 폴더 나중
            fileList.addAll(normalList)
            fileList.addAll(directoryList)
        }
        Timber.e("LocalExplorer file item sort end")

        // 이미지 리스트를 따로 모을 것인지?
        if (isImageListEnable) {
//            ArrayList<ExplorerItem> imageList = new ArrayList<>();
//
//            // 이미지는 마지막에 모아서 처리한다.
//            for (int i = 0; i < normalList.size(); i++) {
//                if (normalList.get(i).type == ExplorerItem.FILETYPE_IMAGE) {
//                    imageList.add(normalList.get(i));
//                }
//            }
//
//            result.imageList = imageList;
            result.imageList = filterImageFileList(normalList)
        }
        Timber.e("LocalExplorer image list end")
        if (isVideoListEnable) {
            result.videoList = filterVideoFileList(normalList)
        }
        if (isAudioListEnable) {
            result.audioList = filterAudioFileList(normalList)
        }
        result.fileList = fileList
        Timber.e("LocalExplorer search end")
        return result
    }
}