package com.duongame.task.file

import android.os.AsyncTask
import com.dropbox.core.DbxException
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.FolderMetadata
import com.duongame.App.Companion.instance
import com.duongame.R
import com.duongame.activity.main.BaseMainActivity
import com.duongame.adapter.ExplorerItem
import com.duongame.cloud.dropbox.DropboxClientFactory.client
import com.duongame.file.FileExplorer
import com.duongame.file.FileHelper.getFileType
import com.duongame.fragment.ExplorerFragment
import com.duongame.helper.ToastHelper.showToast
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.*

class DropboxSearchTask(fragment: ExplorerFragment) :
    AsyncTask<String?, Void?, FileExplorer.Result?>() {
    private val fragmentWeakReference: WeakReference<ExplorerFragment>
    private var path: String? = null
    override fun onPreExecute() {
        super.onPreExecute() // AsyncTask는 아무것도 안함
        val fragment = fragmentWeakReference.get() ?: return
        fragment.setCanClick(false) // 이제부터 클릭할수 없음
    }

    fun createFolder(metadata: FolderMetadata): ExplorerItem {
        val item =
            ExplorerItem(metadata.pathDisplay, metadata.name, null, 0, ExplorerItem.FILETYPE_FOLDER)
        item.metadata = metadata
        return item
    }

    fun createFile(metadata: FileMetadata): ExplorerItem {
        // 압축파일일 경우 책(ZIP)으로 셋팅함
        // 추가적으로 TXT, PDF를 지원
        var type = getFileType(metadata.name)
        when (type) {
            ExplorerItem.FILETYPE_ZIP, ExplorerItem.FILETYPE_TEXT, ExplorerItem.FILETYPE_PDF -> {}
            else -> type = ExplorerItem.FILETYPE_FILE
        }
        val item = ExplorerItem(
            metadata.pathDisplay,
            metadata.name,
            metadata.serverModified.toString(),
            metadata.size,
            type
        )
        item.metadata = metadata
        return item
    }

    protected override fun doInBackground(vararg strings: String?): FileExplorer.Result? {
        path = strings[0]
        if (path == null) path = "/"
        //            ExplorerFragment fragment = fragmentWeakReference.get();
//            if (fragment == null)
//                return null;
        if (path == "/") path = path!!.replace("/", "")
        try {
            val client = client
            val requests = client!!.files()
            val listFolderResult = requests.listFolder(path)
            val fileList = ArrayList<ExplorerItem>()
            if (listFolderResult != null) {
                val entries = listFolderResult.entries
                if (entries != null) {
                    for (i in entries.indices) {
                        val metadata = entries[i] ?: continue
                        var item: ExplorerItem = if (metadata is FileMetadata) {
                            createFile(metadata)
                        } else if (metadata is FolderMetadata) {
                            createFolder(metadata)
                        } else continue

                        // 패스를 찾았다. 리스트에 더해주자.
                        fileList.add(item)
                        Timber.e("i=$i $item")
                    }
                }
                val result = FileExplorer.Result()
                result.fileList = fileList
                return result
            }
        } catch (e: DbxException) {
            Timber.e(e.localizedMessage)
        } catch (e: IllegalStateException) {
            Timber.e(e.localizedMessage)
        }
        return null
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

            // 성공했을때 현재 패스를 업데이트
            // 드롭박스에서만 앞에 /를 붙여준다.
            if (path!!.length == 0) path = "/"
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
        fragment.updateDropboxUI(false)
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