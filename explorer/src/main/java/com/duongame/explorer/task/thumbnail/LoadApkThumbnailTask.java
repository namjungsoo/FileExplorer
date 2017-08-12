package com.duongame.explorer.task.thumbnail;

import android.app.Activity;
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
    private final Activity context;
    private final ImageView imageView;
    private String path;

    public LoadApkThumbnailTask(Activity context, ImageView imageView) {
        this.context = context;
        this.imageView = imageView;
    }

    @Override
    protected Drawable doInBackground(String... params) {
        BitmapLoader.BitmapOrDrawable bod = loadThumbnail(context, APK, params[0]);
        return bod.drawable;
    }

    @Override
    protected void onPostExecute(Drawable drawable) {
        super.onPostExecute(drawable);
        if (imageView != null && drawable != null) {
            if (imageView != null) {
                imageView.setImageDrawable(drawable);
            }
        }
    }

}
