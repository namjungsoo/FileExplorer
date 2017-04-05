package com.duongame.explorer.task.thumbnail;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.duongame.explorer.bitmap.BitmapCache;
import com.duongame.explorer.bitmap.BitmapLoader;

/**
 * Created by namjungsoo on 2017-02-19.
 */

public class LoadPdfThumbnailTask extends AsyncTask<String, Void, Bitmap> {
    private final Activity context;
    private final ImageView imageView;
    private String path;

    public LoadPdfThumbnailTask(Activity context, ImageView imageView) {
        this.context = context;
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        path = params[0];
        if (isCancelled())
            return null;

        Bitmap bitmap = BitmapCache.getThumbnail(path);
        if (isCancelled())
            return bitmap;

        // 이제 PDF에서 파일을 읽어서 렌더링 하자.
        if (bitmap == null) {
            bitmap = BitmapLoader.decodeSquareThumbnailFromPdfFile(path, 96);
        }
        if (bitmap != null) {
            BitmapCache.setThumbnail(path, bitmap, imageView);
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
