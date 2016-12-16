package com.duongame.explorer.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.duongame.explorer.bitmap.BitmapCacheManager;
import com.duongame.explorer.bitmap.BitmapLoader;

/**
 * Created by namjungsoo on 2016-12-16.
 */

public class LoadBitmapTask extends AsyncTask<String, Void, Bitmap> {
    private static final int RETRY_INTERVAL_MS = 500;
    private static final int RETRY_COUNT = 5;

    private final ImageView imageView;
    private int width, height;

    public LoadBitmapTask(ImageView imageView, int width, int height) {
        this.imageView = imageView;
        this.width = width;
        this.height = height;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        final String path = params[0];

        // 캐시에 있는지 확인해 보고
        Bitmap bitmap = BitmapCacheManager.getBitmap(path);
        if (bitmap == null) {
            BitmapFactory.Options options = BitmapLoader.decodeBounds(path);
            float bitmapRatio = (float) options.outHeight / (float) options.outWidth;
            float screenRatio = (float) height / (float) width;

            if (screenRatio > bitmapRatio) {
                height = (int) (width * bitmapRatio);
            } else {
                height = (int) (width * screenRatio);
            }

            // 파일에서 읽어서 있으면 캐시에 넣는다
            int count = 0;
            while (true) {
                bitmap = BitmapLoader.decodeSampleBitmapFromFile(path, width, height, true);
                if (bitmap == null) {
                    try {
                        count += RETRY_INTERVAL_MS;
                        if (count == RETRY_INTERVAL_MS * RETRY_COUNT)
                            break;
                        Thread.sleep(RETRY_INTERVAL_MS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.w("LoadBitmapTask", "decode retry=" + count);
                } else {
                    BitmapCacheManager.setBitmap(path, bitmap);
                    break;
                }
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
