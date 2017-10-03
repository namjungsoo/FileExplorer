package com.duongame.explorer.task.thumbnail;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.duongame.explorer.bitmap.BitmapLoader;

import static com.duongame.explorer.adapter.ExplorerItem.FileType.APK;
import static com.duongame.explorer.bitmap.BitmapLoader.loadThumbnail;

/**
 * Created by js296 on 2017-08-13.
 */

public class LoadApkThumbnailTask extends AsyncTask<String, Void, Drawable> {
    private final Context context;

    private ImageView icon, iconSmall;
    private String path;

    public LoadApkThumbnailTask(Context context, ImageView icon, ImageView iconSmall) {
        this.context = context;
        this.icon = icon;
        this.iconSmall = iconSmall;
    }

    @Override
    protected Drawable doInBackground(String... params) {
        path = params[0];
        BitmapLoader.BitmapOrDrawable bod = loadThumbnail(context, APK, params[0]);
        return bod.drawable;
    }

    @Override
    protected void onPostExecute(Drawable drawable) {
        super.onPostExecute(drawable);
        if (drawable == null)
            return;
        if (icon == null)
            return;
        if (path == null)
            return;
        if (iconSmall.getTag() == null)
            return;
        if (path.equals(iconSmall.getTag()))
            icon.setImageDrawable(drawable);
    }

}
