package com.duongame.explorer.task.thumbnail;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.duongame.explorer.bitmap.BitmapCache;
import com.duongame.explorer.bitmap.BitmapLoader;

/**
 * Created by namjungsoo on 2016-12-16.
 */

public class LoadThumbnailTask extends AsyncTask<String, Void, Bitmap> {
    private final Activity context;
    private final ImageView imageView;
    private String path;

    public LoadThumbnailTask(Activity context, ImageView imageView) {
        this.context = context;
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        path = params[0];

        if(isCancelled())
            return null;

        Bitmap bitmap = BitmapCache.getThumbnail(path);
        if(isCancelled())
            return bitmap;

        if (bitmap == null) {
            bitmap = BitmapLoader.getThumbnail(context, path, true);
            if(isCancelled())
                return bitmap;

            if (bitmap == null) {
                bitmap = BitmapLoader.decodeSquareThumbnailFromFile(path, 96, true);
            }
            if (bitmap != null) {
                BitmapCache.setThumbnail(path, bitmap, imageView);
            }
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (imageView != null && bitmap != null) {
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}
