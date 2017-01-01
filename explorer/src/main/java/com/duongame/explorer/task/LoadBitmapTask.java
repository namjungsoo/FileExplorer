package com.duongame.explorer.task;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
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
//        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        final ExplorerFileItem item = params[0];
        Bitmap bitmap = null;
        if (!isCancelled()) {
            Log.d(this.getClass().getSimpleName(), "loadBitmap");
            bitmap = loadBitmap(item);
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        // imageView 셋팅은 UI 쓰레드에서 해야 한다.
        if (imageView != null && bitmap != null) {
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);

                // 종횡비 체크
                final int bmWidth = bitmap.getWidth();
                final int bmHeight = bitmap.getHeight();
                final float bmRatio = (float) bmHeight / (float) bmWidth;

                // 화면비 체크
                final int width = imageView.getWidth();
                final int height = imageView.getHeight();
                final float imageRatio = (float) height / (float) width;

                int newWidth;
                int newHeight;
                if (bmRatio > imageRatio) {
                    newWidth = (int) (height / bmRatio);
                    newHeight = height;
                } else {
                    newWidth = width;
                    newHeight = (int) (width * bmRatio);
                }

//                Log.d(TAG, "width=" + width + " height=" + height);

                final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageView.getLayoutParams();
                params.width = newWidth;
                params.height = newHeight;
                params.gravity = Gravity.CENTER;

                imageView.setLayoutParams(params);
                imageView.requestLayout();
            }
        }
    }
}
