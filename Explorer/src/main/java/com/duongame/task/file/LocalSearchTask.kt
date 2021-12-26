package com.duongame.task.file

import android.os.AsyncTask
import android.view.View
import com.duongame.App.Companion.instance
import com.duongame.adapter.ExplorerItem
import com.duongame.db.ExplorerItemDB.Companion.getInstance
import com.duongame.file.FileExplorer
import com.duongame.file.FileHelper.DateAscComparator
import com.duongame.file.FileHelper.DateDescComparator
import com.duongame.file.FileHelper.ExtAscComparator
import com.duongame.file.FileHelper.ExtDescComparator
import com.duongame.file.FileHelper.NameAscComparator
import com.duongame.file.FileHelper.NameDescComparator
import com.duongame.file.FileHelper.SizeAscComparator
import com.duongame.file.FileHelper.SizeDescComparator
import com.duongame.fragment.ExplorerFragment
import com.duongame.manager.PermissionManager.checkStoragePermissions
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.*

class LocalSearchTask(
    fragment: ExplorerFragment?, // 기본값 false
    private val pathChanged: Boolean
) : AsyncTask<String?, Void?, FileExplorer.Result?>() {
    private val fragmentWeakReference: WeakReference<ExplorerFragment?>
    private var disableUpdateCanClick = false
    private var path: String? = null
    private var comparator: Comparator<ExplorerItem>? = null

    constructor(
        fragment: ExplorerFragment?,
        pathChanged: Boolean,
        disableUpdateCanClick: Boolean
    ) : this(fragment, pathChanged) {
        this.disableUpdateCanClick = disableUpdateCanClick
    }

    fun updateComparator() {
        val fragment = fragmentWeakReference.get() ?: return
        if (fragment.sortDirection == 0) { // ascending
            when (fragment.sortType) {
                0 -> comparator = NameAscComparator()
                1 -> comparator = ExtAscComparator()
                2 -> comparator = DateAscComparator()
                3 -> comparator = SizeAscComparator()
            }
        } else {
            when (fragment.sortType) {
                0 -> comparator = NameDescComparator()
                1 -> comparator = ExtDescComparator()
                2 -> comparator = DateDescComparator()
                3 -> comparator = SizeDescComparator()
            }
        }
    }

    protected override fun doInBackground(vararg params: String?): FileExplorer.Result? {
        Timber.e("LocalSearchTask doInBackground begin")
        path = params[0]
        updateComparator()
        val fragment = fragmentWeakReference.get() ?: return null
        val explorer = fragment.fileExplorer ?: return null
        val result = explorer.apply {
            isRecursiveDirectory = false
            isExcludeDirectory = false
            comparator = comparator
            isHiddenFile = false
            isImageListEnable = true
            isVideoListEnable = true
            isAudioListEnable = true
        }.search(path)
        Timber.e("LocalSearchTask doInBackground end")
        return result
    }

    override fun onPreExecute() {
        super.onPreExecute() // AsyncTask는 아무것도 안함
        val fragment = fragmentWeakReference.get() ?: return
        if (!disableUpdateCanClick) {
            fragment.setCanClick(false) // 이제부터 클릭할수 없음. 프로그래스바 표시
        }
    }

    override fun onPostExecute(result: FileExplorer.Result?) {
        Timber.e("LocalSearchTask onPostExecute begin")
        super.onPostExecute(result) // AsyncTask는 아무것도 안함
        if (isCancelled) return
        val fragment = fragmentWeakReference.get() ?: return
        try {
            //FIX: Index Out of Bound
            // 쓰레드에서 메인쓰레드로 옮김
            fragment.fileList = result!!.fileList
            instance.fileList = result.fileList
            instance.imageList = result.imageList
            instance.videoList = result.videoList
            instance.audioList = result.audioList
            fragment.adapter!!.fileList = fragment.fileList!!
            fragment.adapter!!.notifyDataSetChanged()

            // SearchTask가 resume
            if (pathChanged) {
                synchronized(fragment) {
                    if (fragment.fileList != null && fragment.fileList!!.size > 0) {
                        fragment.currentView!!.scrollToPosition(0)
                        fragment.currentView!!.invalidate()
                    }
                }
            }

            // 성공했을때 현재 패스를 업데이트
            instance.lastPath = path
            fragment.textPath!!.text = path
            fragment.textPath!!.requestLayout()
            if (fragment.switcherContents != null) {
                if (fragment.fileList == null || fragment.fileList!!.size <= 0) {
                    fragment.switcherContents!!.displayedChild = 1

                    // 퍼미션이 있으면 퍼미션 버튼을 보이지 않게 함
                    if (checkStoragePermissions(fragment.activity)) {
                        fragment.permissionButton!!.visibility = View.GONE
                        fragment.textNoFiles!!.visibility = View.VISIBLE
                    } else {
                        fragment.permissionButton!!.visibility = View.VISIBLE
                        fragment.textNoFiles!!.visibility = View.GONE
                    }
                } else {
                    fragment.switcherContents!!.displayedChild = 0
                }
            }
            fragment.setCanClick(true)
            Thread {

                // 결과가 왔으므로 DB에 저장해 준다.
                Timber.e("LocalSearchTask doInBackground DB begin")
                val dao = getInstance(fragment.context!!).db.explorerItemDao()
                dao.deleteAll()
                for (item in result.fileList) {
                    Timber.e(item.toString())
                }
                dao.insertItems(result.fileList)
                Timber.e("LocalSearchTask doInBackground DB end")
            }.start()
            Timber.e("LocalSearchTask onPostExecute end")
        } catch (e: NullPointerException) {
        }
    }

    init {
        fragmentWeakReference = WeakReference(fragment)
        Timber.e("LocalSearchTask begin")
    }
}