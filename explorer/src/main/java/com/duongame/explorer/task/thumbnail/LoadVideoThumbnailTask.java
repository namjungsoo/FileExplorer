package com.duongame.explorer.task.thumbnail;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.duongame.explorer.bitmap.BitmapLoader;

import static com.duongame.explorer.adapter.ExplorerItem.FileType.VIDEO;
import static com.duongame.explorer.bitmap.BitmapLoader.loadThumbnail;

/**
 * Created by namjungsoo on 2017-04-05.
 */

public class LoadVideoThumbnailTask extends AsyncTask<String, Void, Bitmap> {
    private final Activity context;

    private ImageView icon, iconSmall;
    private String path;

    public LoadVideoThumbnailTask(Activity context, ImageView icon, ImageView iconSmall) {
        this.context = context;
        this.icon = icon;
        this.iconSmall = iconSmall;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        path = params[0];
        BitmapLoader.BitmapOrDrawable bod = loadThumbnail(context, VIDEO, path);
        return bod.bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (bitmap == null)
            return;
        if (icon == null)
            return;
        if (path == null)
            return;
        if (iconSmall.getTag() == null)
            return;
        if (path.equals(iconSmall.getTag()))
            icon.setImageBitmap(bitmap);
    }
}
