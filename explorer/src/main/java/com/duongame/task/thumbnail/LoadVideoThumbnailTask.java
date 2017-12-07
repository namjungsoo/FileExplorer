package com.duongame.task.thumbnail;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.duongame.bitmap.BitmapLoader;

import java.lang.ref.WeakReference;

import static com.duongame.adapter.ExplorerItem.FileType.VIDEO;
import static com.duongame.bitmap.BitmapLoader.loadThumbnail;

/**
 * Created by namjungsoo on 2017-04-05.
 */

public class LoadVideoThumbnailTask extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<Context> contextRef;
    private final WeakReference<ImageView> iconRef, iconSmallRef;
    private String path;

    public LoadVideoThumbnailTask(Context context, ImageView icon, ImageView iconSmall) {
        this.contextRef = new WeakReference<Context>(context);
        this.iconRef = new WeakReference<ImageView>(icon);
        this.iconSmallRef = new WeakReference<ImageView>(iconSmall);
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        path = params[0];

        if(contextRef.get() == null)
            return null;

        if (contextRef.get() instanceof Activity) {
            if (((Activity) contextRef.get()).isFinishing())
                return null;
        }

        BitmapLoader.BitmapOrDrawable bod = loadThumbnail(contextRef.get(), VIDEO, path);
        return bod.bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (bitmap == null)
            return;
        if (path == null)
            return;
        if (iconRef.get() == null)
            return;
        if (iconSmallRef.get() == null)
            return;
        if (iconSmallRef.get().getTag() == null)
            return;
        if (path.equals(iconSmallRef.get().getTag()))
            iconRef.get().setImageBitmap(bitmap);
    }
}
