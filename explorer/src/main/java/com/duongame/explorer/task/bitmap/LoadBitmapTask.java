package com.duongame.explorer.task.bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.helper.FileHelper;

import java.io.File;

/**
 * Created by namjungsoo on 2016-12-16.
 */

public class LoadBitmapTask extends BitmapTask {
    private final ImageView imageView;
    private ExplorerItem item;
    private Context context;

    public LoadBitmapTask(Context context, ImageView imageView, int width, int height, boolean exif) {
        super(width, height, exif);

        this.context = context;
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(ExplorerItem... params) {
//        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        if (isCancelled())
            return null;

        item = params[0];
        if (FileHelper.isGifImage(item.path)) {
//            Glide.with(context).load(new File(item.path)).into(imageView);
            return null;
        }

        Bitmap bitmap = null;
        Log.d(this.getClass().getSimpleName(), "loadBitmap");

        bitmap = loadBitmap(item);
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        // imageView 셋팅은 UI 쓰레드에서 해야 한다.
        if (imageView != null) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                imageView.setTag(item.path);

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

//                final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageView.getLayoutParams();
//                params.width = newWidth;
//                params.height = newHeight;
//                params.gravity = Gravity.CENTER;
//
//                imageView.setLayoutParams(params);
//                imageView.requestLayout();
            } else {
                Glide.with(context).load(new File(item.path)).into(imageView);
            }
        }
    }
}
