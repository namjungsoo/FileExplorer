package com.duongame.task.thumbnail

import com.duongame.bitmap.BitmapLoader.loadThumbnail
import com.duongame.bitmap.BitmapCacheManager.setThumbnail
import android.os.AsyncTask
import android.graphics.Bitmap
import android.app.Activity
import android.content.Context
import android.widget.ImageView
import com.duongame.bitmap.BitmapCacheManager
import java.lang.ref.WeakReference

/**
 * Created by namjungsoo on 2017-02-19.
 */
class LoadThumbnailTask(context: Context?, icon: ImageView, iconSmall: ImageView, fileType: Int) :
    AsyncTask<String?, Void?, Bitmap?>() {
    private val contextRef: WeakReference<Context?> = WeakReference(context)
    private val iconRef: WeakReference<ImageView> = WeakReference(icon)
    private val iconSmallRef: WeakReference<ImageView> = WeakReference(iconSmall)
    private var path: String? = null
    private val fileType: Int = fileType
    protected override fun doInBackground(vararg params: String?): Bitmap? {
        path = params[0]
        if (contextRef.get() == null) return null
        if (contextRef.get() is Activity) {
            if ((contextRef.get() as Activity?)!!.isFinishing) return null
        }
        return loadThumbnail(contextRef.get()!!, fileType, path!!)
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