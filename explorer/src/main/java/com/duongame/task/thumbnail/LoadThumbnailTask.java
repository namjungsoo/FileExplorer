package com.duongame.task.thumbnail;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.duongame.adapter.ExplorerItem;
import com.duongame.bitmap.BitmapCacheManager;
import com.duongame.bitmap.BitmapLoader;

import java.lang.ref.WeakReference;

import static com.duongame.bitmap.BitmapLoader.loadThumbnail;

/**
 * Created by namjungsoo on 2017-02-19.
 */

public class LoadThumbnailTask extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<Context> contextRef;
    private final WeakReference<ImageView> iconRef, iconSmallRef;
    private String path;
    private int fileType;

    public LoadThumbnailTask(Context context, ImageView icon, ImageView iconSmall, int fileType) {
        this.contextRef = new WeakReference<>(context);
        this.iconRef = new WeakReference<>(icon);
        this.iconSmallRef = new WeakReference<>(iconSmall);
        this.fileType = fileType;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        path = params[0];

        if (contextRef.get() == null)
            return null;

        if (contextRef.get() instanceof Activity) {
            if (((Activity) contextRef.get()).isFinishing())
                return null;
        }

        return loadThumbnail(contextRef.get(), fileType, path);
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
