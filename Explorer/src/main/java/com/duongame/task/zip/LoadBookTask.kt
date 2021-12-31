package com.duongame.task.zip

import android.os.AsyncTask
import com.duongame.adapter.ExplorerItem
import com.duongame.archive.ArchiveFile
import com.duongame.archive.ArchiveLoader.ArchiveLoaderListener
import com.duongame.bitmap.BitmapLoader
import java.util.*

/**
 * Created by namjungsoo on 2018-01-23.
 */
// 모든 Zip 타입 책의 통합된 Loader
// 이전의 UnzipBookTask를 대체한다.
class LoadBookTask(
    private val zipFile: ArchiveFile?, // zip안의 이미지 파일의 갯수
    private val imageList: ArrayList<ExplorerItem>,
    private val listener: ArchiveLoaderListener?, // 압축 풀린 이미지 파일의 갯수
    private val extract: Int,
    zipImageList: ArrayList<ExplorerItem>?
) : AsyncTask<String?, Int?, Void?>() {
    private var zipImageList // 잘려진 zip 파일의 이미지 갯수. 파일갯수와 다름
            : ArrayList<ExplorerItem>? = null
    private var side = ExplorerItem.SIDE_LEFT
    private val mPauseWorkLock = Object()
    private var mPauseWork = false
    fun setPauseWork(pauseWork: Boolean) {
        synchronized(mPauseWorkLock) {
            mPauseWork = pauseWork
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll()
            }
        }
    }

    // image side가 변경되었을때 앞에 좌우가 변경된 것을 넘겨주기 위함
    fun setZipImageList(zipImageList: ArrayList<ExplorerItem>?) {
        this.zipImageList = zipImageList
    }

    fun setSide(side: Int) {
        this.side = side
    }

    override fun doInBackground(vararg params: String?): Void? {
        val path = params[0]
        path ?: return null
        var i = 0
        i = extract
        while (i < imageList.size) {
            val item = imageList[i]
            if (isCancelled) break
            synchronized(mPauseWorkLock) {
                while (mPauseWork && !isCancelled) {
                    try {
                        mPauseWorkLock.wait()
                    } catch (e: InterruptedException) {
                    }
                }
            }
            if (zipFile?.extractFile(item.name, path) == true) {
                processItem(i, item, side, zipImageList)
                publishProgress(i)
            } else {
                listener?.onFail(i, imageList[i].name)
            }
            i++
        }
        return null
    }

    override fun onProgressUpdate(vararg progress: Int?) {
        listener ?: return
        val i = progress[0]
        i ?: return
        listener.onSuccess(i, zipImageList, imageList.size)
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        listener?.onFinish(zipImageList, imageList.size)
    }

    override fun onCancelled(value: Void?) {
        super.onCancelled(value)
        synchronized(mPauseWorkLock) { mPauseWorkLock.notifyAll() }
    }

    companion object {
        fun processItem(
            orgIndex: Int,
            item: ExplorerItem,
            side: Int,
            imageList: ArrayList<ExplorerItem>?
        ) {
            imageList ?: return
            val options = BitmapLoader.decodeBounds(item.path)

            // 나중에 페이지 전환을 위해서 넣어둔다.
            item.width = options.outWidth
            item.height = options.outHeight
            val size = imageList.size
            if (options.outWidth > options.outHeight) { // 잘라야 한다. 가로 파일이다.
                if (side == ExplorerItem.SIDE_LEFT) {
                    // 한국식은 right를 먼저 넣는다.
                    val left = item.clone() as ExplorerItem
                    left.side = ExplorerItem.SIDE_LEFT
                    left.index = size
                    left.orgIndex = orgIndex
                    imageList.add(left)
                    val right = item.clone() as ExplorerItem
                    right.side = ExplorerItem.SIDE_RIGHT
                    right.index = size + 1
                    right.orgIndex = orgIndex
                    imageList.add(right)
                } else if (side == ExplorerItem.SIDE_RIGHT) {
                    // 일본식은 right를 먼저 넣는다.
                    val right = item.clone() as ExplorerItem
                    right.side = ExplorerItem.SIDE_RIGHT
                    right.index = size
                    right.orgIndex = orgIndex
                    imageList.add(right)
                    val left = item.clone() as ExplorerItem
                    left.side = ExplorerItem.SIDE_LEFT
                    left.index = size + 1
                    left.orgIndex = orgIndex
                    imageList.add(left)
                } else { // 전체보기
                    val newItem = item.clone() as ExplorerItem
                    newItem.index = size
                    newItem.orgIndex = orgIndex
                    imageList.add(newItem)
                }
            } else {
                val newItem = item.clone() as ExplorerItem
                newItem.index = size
                newItem.orgIndex = orgIndex
                imageList.add(newItem)
            }
        }
    }

    init {
        if (zipImageList != null) {
            this.zipImageList = zipImageList
        } else {
            this.zipImageList = ArrayList()
        }
    }
}