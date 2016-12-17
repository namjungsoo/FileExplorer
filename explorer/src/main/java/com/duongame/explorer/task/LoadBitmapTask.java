package com.duongame.explorer.task;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.duongame.explorer.adapter.ExplorerFileItem;

/**
 * Created by namjungsoo on 2016-12-16.
 */

public class LoadBitmapTask extends BitmapTask {
    private final ImageView imageView;

    public LoadBitmapTask(ImageView imageView, int width, int height, boolean exif) {
        super(width, height, exif);
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(ExplorerFileItem... params) {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        final ExplorerFileItem item = params[0];

        Bitmap bitmap = null;
        if(!isCancelled())
            bitmap = loadBitmap(item);
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        // imageView 셋팅은 UI 쓰레드에서 해야 한다.
        if (imageView != null && bitmap != null) {
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}
