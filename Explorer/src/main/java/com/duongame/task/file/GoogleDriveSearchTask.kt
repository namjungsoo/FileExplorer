package com.duongame.task.file

import android.os.AsyncTask
import com.duongame.App.Companion.instance
import com.duongame.R
import com.duongame.activity.main.BaseMainActivity
import com.duongame.adapter.ExplorerItem
import com.duongame.cloud.googledrive.GoogleDriveManager.REQUEST_AUTHORIZATION
import com.duongame.cloud.googledrive.GoogleDriveManager.client
import com.duongame.file.FileExplorer
import com.duongame.file.FileHelper.NameAscComparator
import com.duongame.file.FileHelper.getFileType
import com.duongame.fragment.ExplorerFragment
import com.duongame.helper.ToastHelper.showToast
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.drive.model.FileList
import timber.log.Timber
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

class GoogleDriveSearchTask(fragment: ExplorerFragment) :
    AsyncTask<String?, Void?, FileExplorer.Result?>() {
    private val fragmentWeakReference: WeakReference<ExplorerFragment>
    private var path: String? = null
    override fun onPreExecute() {
        super.onPreExecute() // AsyncTask는 아무것도 안함
        val fragment = fragmentWeakReference.get() ?: return
        fragment.setCanClick(false) // 이제부터 클릭할수 없음
    }

    fun getFolderFileId(parent: String?, name: String): String? {
        val driveService = client ?: return null
        var fileId: String? = null
        var pageToken: String? = null
        do {
            var result: FileList? = null
            result = try {
                driveService.files().list()
                    .setQ("'$parent' in parents and name='$name'")
                    .setFields("nextPageToken, files(id)")
                    .setPageToken(pageToken)
                    .execute()
            } catch (e: UserRecoverableAuthIOException) {
                e.printStackTrace()
                val fragment = fragmentWeakReference.get() ?: return null
                fragment.startActivityForResult(e.intent, REQUEST_AUTHORIZATION)
                return null
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            } ?: return fileId

            for (file in result.files) {
                fileId = file.id
                return fileId
            }
            pageToken = result.nextPageToken
        } while (pageToken != null)
        return fileId
    }

    // /개인적인/남채은
    protected override fun doInBackground(vararg strings: String?): FileExplorer.Result? {
        path = strings[0]
        if (path == null) path = "/"
        val fragment = fragmentWeakReference.get() ?: return null

        /*
        [0] = ""
        [1] = "개인적인"
        [2] = "남채은"
         */
        var fileId: String? = null
        val folders = path!!.split("/").toTypedArray()
        if (folders.size < 2) {
            fileId = "root"
        } else {
            for (i in 1 until folders.size) {
                fileId = if (i == 1) {
                    getFolderFileId("root", folders[i])
                } else {
                    getFolderFileId(fileId, folders[i])
                }
                if (fileId == null) return null
            }
        }

        // GoogleDrive에서 찾기 시작
        val driveService = client ?: return null
        var pageToken: String? = null
        val fileList = ArrayList<ExplorerItem>()
        val folderList = ArrayList<ExplorerItem>()
        val normalList = ArrayList<ExplorerItem>()
        do {
            var result: FileList? = null
            result = try {
                driveService.files().list()
                    .setQ("'$fileId' in parents")
                    .setFields("nextPageToken, files(*)")
                    .setPageToken(pageToken)
                    .execute()
            } catch (e: UserRecoverableAuthIOException) {
                e.printStackTrace()
                fragment.startActivityForResult(e.intent, REQUEST_AUTHORIZATION)
                return null
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            } ?: return null
            for (file in result.getFiles()) {
                var type = ExplorerItem.FILETYPE_FILE
                if (file.mimeType == "application/vnd.google-apps.folder") type =
                    ExplorerItem.FILETYPE_FOLDER
                val size = if (file.getSize() != null) file.getSize().toLong() else 0
                val item =
                    ExplorerItem(file.name, file.name, file.createdTime.toString(), size, type)
                item.metadata = file.id
                if (type == ExplorerItem.FILETYPE_FOLDER) {
                    folderList.add(item)
                } else {
                    type = getFileType(item.name)
                    when (type) {
                        ExplorerItem.FILETYPE_ZIP, ExplorerItem.FILETYPE_TEXT, ExplorerItem.FILETYPE_PDF -> {}
                        else -> type = ExplorerItem.FILETYPE_FILE
                    }
                    normalList.add(item)
                }
                //fileList.add(item);
                Timber.e("name=" + file.name + " createdTime=" + file.createdTime + " fileId=" + file.id + " mime=" + file.mimeType)
            }
            pageToken = result.getNextPageToken()
        } while (pageToken != null)
        val comparator = NameAscComparator()
        Collections.sort(folderList, comparator)
        Collections.sort(normalList, comparator)
        fileList.addAll(folderList)
        fileList.addAll(normalList)
        val result = FileExplorer.Result()
        result.fileList = fileList
        return result
    }

    override fun onPostExecute(result: FileExplorer.Result?) {
        super.onPostExecute(result) // AsyncTask는 아무것도 안함
        if (result == null) {
            onExit()
            return
        }
        val fragment = fragmentWeakReference.get() ?: return
        try {
            //FIX: Index Out of Bound
            // 쓰레드에서 메인쓰레드로 옮김
            fragment.fileList = result.fileList
            instance.fileList = result.fileList
            instance.imageList = result.imageList
            fragment.adapter!!.fileList = fragment.fileList!!
            fragment.adapter!!.notifyDataSetChanged()
            instance.lastPath = path
            fragment.textPath!!.text = path
            fragment.textPath!!.requestLayout()
            fragment.setCanClick(true)
        } catch (e: NullPointerException) {
        }
    }

    private fun onExit() {
        val fragment = fragmentWeakReference.get() ?: return

        // UI 업데이트
        fragment.updateGoogleDriveUI(false)
        fragment.setCanClick(true)

        // 에러 메세지
        showToast(fragment.context, R.string.toast_error)
        val activity = fragment.activity as BaseMainActivity? ?: return

        // 로그아웃 처리
        val item = activity.googleDriveMenuItem
        activity.logoutGoogleDrive(item)
    }

    init {
        fragmentWeakReference = WeakReference(fragment)
    }
}