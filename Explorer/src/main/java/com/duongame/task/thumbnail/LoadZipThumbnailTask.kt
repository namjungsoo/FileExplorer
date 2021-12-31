package com.duongame.task.thumbnail

import android.content.Context
import com.duongame.bitmap.BitmapLoader.getZipThumbnailFileName
import com.duongame.bitmap.BitmapLoader.drawableToBitmap
import com.duongame.bitmap.BitmapCacheManager.setThumbnail
import android.os.AsyncTask
import com.duongame.bitmap.BitmapLoader
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.duongame.R
import com.bumptech.glide.request.target.ImageViewTarget
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.duongame.bitmap.BitmapCacheManager
import java.io.File
import java.lang.Exception
import java.lang.ref.WeakReference

/**
 * Created by namjungsoo on 2016-12-16.
 */
class LoadZipThumbnailTask(context: Context?, icon: ImageView?, iconSmall: ImageView?) :
    AsyncTask<String?, Void?, String?>() {
    private val contextRef: WeakReference<Context?> = WeakReference(context)
    private val iconRef: WeakReference<ImageView?> = WeakReference(icon)
    private val iconSmallRef: WeakReference<ImageView?> = WeakReference(iconSmall)
    private var path: String? = null
    protected override fun doInBackground(vararg params: String?): String? {
        path = params[0]
        return if (contextRef.get() == null) null else getZipThumbnailFileName(
            contextRef.get(),
            path
        )
    }

    override fun onPostExecute(param: String?) {
        super.onPostExecute(param)
        if (path == null) return
        if (param == null) return
        val context = contextRef.get() ?: return
        if (iconRef.get() == null) return
        if (iconSmallRef.get() == null) return

//        if (context.isFinishing())
//            return;
        try {
            Glide.with(context.applicationContext)
                .load(File(param))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_file_zip)
                .centerCrop()
                .into(object : ImageViewTarget<Drawable?>(iconRef.get()) {
                    override fun setResource(resource: Drawable?) {
                        //FIX: destroyed activity error
                        if (resource == null) return
                        if (path == iconSmallRef.get()!!.tag) {
                            val bitmap = drawableToBitmap(resource)
                            getView().setImageBitmap(bitmap)
                            setThumbnail(path, bitmap, iconRef.get())
                        }
                    }
                })
        } catch (e: Exception) { // FIX: IllegalArgumentException
        }
    }

}