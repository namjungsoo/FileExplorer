package com.duongame.task.bitmap

import android.graphics.Bitmap
import android.os.AsyncTask
import android.widget.ImageView
import com.duongame.activity.viewer.PagerActivity
import com.duongame.adapter.ExplorerItem
import com.duongame.bitmap.BitmapCacheManager.changePathToPage
import com.duongame.bitmap.BitmapCacheManager.getBitmap
import com.duongame.bitmap.BitmapCacheManager.getPage
import com.duongame.bitmap.BitmapCacheManager.setBitmap
import com.duongame.bitmap.BitmapCacheManager.setPage
import com.duongame.bitmap.BitmapLoader.SplittedBitmap
import com.duongame.bitmap.BitmapLoader.decodeBounds
import com.duongame.bitmap.BitmapLoader.decodeSampleBitmapFromFile
import com.duongame.bitmap.BitmapLoader.splitBitmapSide
import com.duongame.file.FileHelper.isJpegImage
import com.duongame.file.FileHelper.isPngImage
import com.duongame.task.bitmap.LoadBitmapTask
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Created by namjungsoo on 2016-12-16.
 */
class LoadBitmapTask(
    context: PagerActivity,
    imageView: ImageView,
    private val screenWidth: Int,
    private var screenHeight: Int,
    private val exifRotation: Boolean,
    position: Int,
    loadingByOther: Boolean
) : AsyncTask<ExplorerItem?, Void?, SplittedBitmap?>() {
    private val contextRef: WeakReference<PagerActivity>
    private val imageViewRef: WeakReference<ImageView>
    private var item: ExplorerItem? = null
    private val position: Int
    private var count = 0
    private val loadingByOther: Boolean
    protected override fun doInBackground(vararg params: ExplorerItem?): SplittedBitmap? {
        if (isCancelled) return null
        item = params[0]
        //        setCurrentLoadingBitmap(item.path);
        Timber.e("setCurrentLoadingBitmap " + item!!.path)
        return loadBitmap(item)
    }

    override fun onPostExecute(sb: SplittedBitmap?) {
        super.onPostExecute(sb)

        // imageView 셋팅은 UI 쓰레드에서 해야 한다.
        if (sb == null) return
        val imageView = imageViewRef.get() ?: return
        if (isCancelled) return
        if (item!!.side == ExplorerItem.SIDE_ALL) {
            if (sb.bitmap == null) return
            imageView.setImageBitmap(sb.bitmap)
            imageView.tag = sb.path
            setBitmap(sb.path, sb.bitmap!!, imageView)
        } else {
            imageView.setImageBitmap(sb.page)
            imageView.tag = sb.key
            setPage(sb.key, sb.page, imageView)
            if (sb.pageOther != null) {
                // other 페이지는 없을때만 등록하자
                if (getPage(sb.keyOther!!) == null) {
                    setPage(sb.keyOther, sb.pageOther, null)
                }
            }
        }

        // 셀프 로딩인 경우에만 제거해준다.
        if (!loadingByOther) removeCurrentLoadingBitmap(item!!.path)
        val context = contextRef.get() ?: return
        if (!context.isFinishing) {
            context.updateInfo(position)
        }
    }

    private fun loadBitmap(item: ExplorerItem?): SplittedBitmap? {
        // 캐시에 있는지 확인해 보고
        // split일 경우에는 무조건 없다
        var bitmap: Bitmap? = null
        var sb: SplittedBitmap? = SplittedBitmap()
        if (item!!.side != ExplorerItem.SIDE_ALL) { // split인 이미지 이면서 캐쉬가 되어 있으면 바로 리턴한다.
            val page = changePathToPage(item)
            Timber.e("loadBitmap changePathToPage " + page + " hash=" + this.hashCode())
            count = 0
            //            boolean loadingByOther = isCurrentLoadingBitmap(item.path);
            Timber.e("isCurrentLoadingBitmap " + item.path + " " + loadingByOther)
            while (true) {
                bitmap = getPage(page)
                if (bitmap != null) {
                    Timber.e("loadBitmap found $page")
                    sb!!.key = page
                    sb.page = bitmap
                    return sb
                } else {
                    // 옆의 페이지가 내것을 로딩하고 있지 않으면 내가 직접 로딩해야 한다.
                    if (!loadingByOther) {
                        Timber.e("Not loading. Self load begin $page")
                        break
                    } else {
                        if (isTimedOutForImageExtracting) {
                            break
                        }
                        Timber.e("Loaded by next page $page")
                    }
                }
            }
        }
        bitmap = getBitmap(item.path)
        if (bitmap == null) {
            val options = decodeBounds(item.path)

            // 자르는 경우에는 실제 예상보다 screenWidth/2를 하자
            if (item.side != ExplorerItem.SIDE_ALL) {
                options.outWidth = options.outWidth shr 1
            }
            item.width = options.outWidth
            item.height = options.outHeight
            val bitmapRatio = options.outHeight.toFloat() / options.outWidth.toFloat()
            val screenRatio = screenHeight.toFloat() / screenWidth.toFloat()
            screenHeight = if (screenRatio > bitmapRatio) {
                (screenWidth * bitmapRatio).toInt()
            } else {
                (screenWidth * screenRatio).toInt()
            }

            // 파일에서 읽어서 있으면 캐시에 넣는다
            count = 0
            while (true) {
                //NEW-2
                if (item.side == ExplorerItem.SIDE_ALL) {
                    bitmap = decodeSampleBitmapFromFile(
                        item.path,
                        screenWidth,
                        screenHeight,
                        exifRotation
                    )
                    if (bitmap == null) {
                        // 다른 비트맵이 기다려지길 기다렸다가 다시 시도하자.
                        // 왜냐면 압축을 푸는 중인 파일도 있기 때문이다.
                        if (isTimedOutForImageExtracting) {
                            sb!!.path = item.path
                            sb.bitmap = null
                            break
                        }
                    } else {
                        sb!!.path = item.path
                        sb.bitmap = bitmap
                        break
                    }
                } else {
                    // RegionDecoder가 지원되는 경우는 PNG, JPG
                    if (isJpegImage(item.path) || isPngImage(item.path)) {
                        sb = splitBitmapSide(item, screenWidth, screenHeight, exifRotation)
                        if (sb == null) {
                            // 다른 비트맵이 기다려지길 기다렸다가 다시 시도하자.
                            // 왜냐면 압축을 푸는 중인 파일도 있기 때문이다.
                            if (isTimedOutForImageExtracting) break
                        } else {
                            break
                        }
                    } else {
                        // GIF는 RegionDecoder가 지원이 되지 않는다.
                        bitmap = decodeSampleBitmapFromFile(
                            item.path,
                            screenWidth,
                            screenHeight,
                            exifRotation
                        )
                        if (bitmap == null) {
                            // 다른 비트맵이 기다려지길 기다렸다가 다시 시도하자.
                            // 왜냐면 압축을 푸는 중인 파일도 있기 때문이다.
                            if (isTimedOutForImageExtracting) break
                        } else {
                            sb = splitBitmapSide(bitmap, item)
                            break
                        }
                    }
                }
            }
        } else {
            sb!!.bitmap = bitmap
            sb.path = item.path
        }

        // SIDE_ALL일때 파일이 없으면
        // sb.path = item.path
        // sb.bitmap = null

        // SIDE_LEFT or SIDE_RIGHT는
        // sb = null
        return sb
        //        return bitmap;
    }// 최대 시간을 기다렸다면 멈추고 종료 한다.

    // 다른 쓰레드에 의해서 이미지가 압축 풀리길 기다렸다가
    // 타임아웃이 되면 true를 리턴한다.
    // false를 리턴하는 것은 sleep에 예외가 발생했을 때이다.
    private val isTimedOutForImageExtracting: Boolean
        private get() {
            try {
                // 최대 시간을 기다렸다면 멈추고 종료 한다.
                count += RETRY_INTERVAL_MS
                if (count == RETRY_INTERVAL_MS * RETRY_COUNT) return true
                Thread.sleep(RETRY_INTERVAL_MS.toLong())
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            return false
        }

    companion object {
        private const val RETRY_INTERVAL_MS = 100
        private const val RETRY_COUNT = 5

        // 동기화를 위해서 사용함 CopyOnWriteArraySet
        // 현재 읽고 있는 bitmap을 중복적으로 읽는 것을 방지하기 위함
        private val currentLoadingBitmapList = CopyOnWriteArraySet<String>()
        fun isCurrentLoadingBitmap(path: String): Boolean {
            return currentLoadingBitmapList.contains(path)
        }

        fun setCurrentLoadingBitmap(path: String) {
            currentLoadingBitmapList.add(path)
        }

        fun removeCurrentLoadingBitmap(path: String) {
            currentLoadingBitmapList.remove(path)
        }
    }

    init {
        contextRef = WeakReference(context)
        imageViewRef = WeakReference(imageView)
        this.position = position
        this.loadingByOther = loadingByOther
    }
}