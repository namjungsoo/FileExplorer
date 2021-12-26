package com.duongame.task.thumbnail

import com.duongame.bitmap.BitmapCacheManager.getThumbnail
import com.duongame.bitmap.BitmapLoader.getThumbnail
import com.duongame.bitmap.BitmapLoader.decodeSquareThumbnailFromFile
import com.duongame.bitmap.BitmapCacheManager.setThumbnail
import android.os.AsyncTask
import android.graphics.Bitmap
import android.app.Activity
import android.content.Context
import android.widget.ImageView
import com.duongame.bitmap.BitmapCacheManager
import com.duongame.bitmap.BitmapLoader
import java.lang.ref.WeakReference

/**
 * Created by namjungsoo on 2016-12-16.
 */
// 원래는 일반 이미지 썸네일이었으나 현재는 GIF만 사용함
class LoadGifThumbnailTask(context: Context?, icon: ImageView, iconSmall: ImageView) :
    AsyncTask<String?, Void?, Bitmap?>() {
    private val contextRef: WeakReference<Context?> = WeakReference(context)
    private val iconRef: WeakReference<ImageView> = WeakReference(icon)
    private val iconSmallRef: WeakReference<ImageView> = WeakReference(iconSmall)
    private var path: String? = null
    protected override fun doInBackground(vararg params: String?): Bitmap? {
        Thread.currentThread().priority = Thread.MIN_PRIORITY
        path = params[0]
        if (isCancelled) return null
        if (contextRef.get() == null) return null
        if (contextRef.get() is Activity) {
            if ((contextRef.get() as Activity?)!!.isFinishing) return null
        }
        var bitmap = getThumbnail(path!!)
        if (isCancelled) return bitmap
        if (bitmap == null) {
            bitmap = getThumbnail(contextRef.get()!!, path, true)
            if (isCancelled) return bitmap
            if (bitmap == null) {
                bitmap = decodeSquareThumbnailFromFile(path, 96, true)
            }
            if (bitmap != null) {
                setThumbnail(path, bitmap, null)
            }
        }
        return bitmap
    }

    override fun onPostExecute(bitmap: Bitmap?) {
        super.onPostExecute(bitmap)
        if (bitmap == null) return
        if (path == null) return
        val icon = iconRef.get() ?: return
        val iconSmall = iconSmallRef.get() ?: return
        val tag = iconSmall.tag as String ?: return
        if (path == tag) {
            icon.setImageBitmap(bitmap)
            setThumbnail(path, bitmap, icon)
        }
    }

}