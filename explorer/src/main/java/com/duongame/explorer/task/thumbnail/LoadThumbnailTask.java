package com.duongame.explorer.task.thumbnail;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.duongame.explorer.bitmap.BitmapCacheManager;
import com.duongame.explorer.bitmap.BitmapLoader;

import java.lang.ref.WeakReference;

/**
 * Created by namjungsoo on 2016-12-16.
 */

public class LoadThumbnailTask extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<Context> contextRef;
    private final WeakReference<ImageView> imageViewRef;

    private String path;

    public LoadThumbnailTask(Context context, ImageView imageView) {
        this.contextRef = new WeakReference<Context>(context);
        this.imageViewRef = new WeakReference<ImageView>(imageView);
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        path = params[0];

        if(isCancelled())
            return null;

        if(contextRef.get() == null)
            return null;

        if (contextRef.get() instanceof Activity) {
            if (((Activity) contextRef.get()).isFinishing())
                return null;
        }

        Bitmap bitmap = BitmapCacheManager.getThumbnail(path);
        if(isCancelled())
            return bitmap;

        if (bitmap == null) {
            bitmap = BitmapLoader.getThumbnail(contextRef.get(), path, true);
            if(isCancelled())
                return bitmap;

            if (bitmap == null) {
                bitmap = BitmapLoader.decodeSquareThumbnailFromFile(path, 96, true);
            }
            if (bitmap != null) {
                BitmapCacheManager.setThumbnail(path, bitmap);
            }
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (bitmap == null)
            return;
        if (imageViewRef.get() == null)
            return;
        imageViewRef.get().setImageBitmap(bitmap);
    }
}
