package com.duongame.task.thumbnail;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.duongame.bitmap.BitmapCacheManager;
import com.duongame.bitmap.BitmapLoader;

import java.lang.ref.WeakReference;

/**
 * Created by namjungsoo on 2016-12-16.
 */

// 원래는 일반 이미지 썸네일이었으나 현재는 GIF만 사용함
public class LoadThumbnailTask extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<Context> contextRef;
    private final WeakReference<ImageView> iconRef, iconSmallRef;
    private String path;

    public LoadThumbnailTask(Context context, ImageView icon, ImageView iconSmall) {
        this.contextRef = new WeakReference<>(context);
        this.iconRef = new WeakReference<>(icon);
        this.iconSmallRef = new WeakReference<>(iconSmall);
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        path = params[0];

        if (isCancelled())
            return null;

        if (contextRef.get() == null)
            return null;

        if (contextRef.get() instanceof Activity) {
            if (((Activity) contextRef.get()).isFinishing())
                return null;
        }

        Bitmap bitmap = BitmapCacheManager.getThumbnail(path);
        if (isCancelled())
            return bitmap;

        if (bitmap == null) {
            bitmap = BitmapLoader.getThumbnail(contextRef.get(), path, true);
            if (isCancelled())
                return bitmap;

            if (bitmap == null) {
                bitmap = BitmapLoader.decodeSquareThumbnailFromFile(path, 96, true);
            }
            if (bitmap != null) {
                BitmapCacheManager.setThumbnail(path, bitmap, null);
            }
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (bitmap == null)
            return;

        if (path == null)
            return;

        ImageView icon = iconRef.get();
        if (icon == null)
            return;

        ImageView iconSmall = iconSmallRef.get();
        if (iconSmall == null)
            return;

        String tag = (String) iconSmall.getTag();
        if (tag == null)
            return;

        if (path.equals(tag)) {
            icon.setImageBitmap(bitmap);
            BitmapCacheManager.setThumbnail(path, bitmap, icon);
        }
    }
}
