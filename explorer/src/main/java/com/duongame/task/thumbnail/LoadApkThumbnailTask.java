package com.duongame.task.thumbnail;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.duongame.adapter.ExplorerItem;
import com.duongame.bitmap.BitmapLoader;

import java.lang.ref.WeakReference;

import static com.duongame.bitmap.BitmapLoader.loadThumbnail;

/**
 * Created by js296 on 2017-08-13.
 */

public class LoadApkThumbnailTask extends AsyncTask<String, Void, Drawable> {
    private final WeakReference<Context> contextRef;
    private final WeakReference<ImageView> iconRef, iconSmallRef;
    private String path;

    public LoadApkThumbnailTask(Context context, ImageView icon, ImageView iconSmall) {
        this.contextRef = new WeakReference<>(context);
        this.iconRef = new WeakReference<>(icon);
        this.iconSmallRef = new WeakReference<>(iconSmall);
    }

    @Override
    protected Drawable doInBackground(String... params) {
        path = params[0];

        if(contextRef.get() == null)
            return null;

        if (contextRef.get() instanceof Activity) {
            if (((Activity) contextRef.get()).isFinishing())
                return null;
        }

        BitmapLoader.BitmapOrDrawable bod = loadThumbnail(contextRef.get(), ExplorerItem.FILETYPE_APK, path);
        return bod.drawable;
    }

    @Override
    protected void onPostExecute(Drawable drawable) {
        super.onPostExecute(drawable);
        if (drawable == null)
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
            iconRef.get().setImageDrawable(drawable);
    }
}
