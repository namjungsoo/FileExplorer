package com.duongame.explorer.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.duongame.explorer.bitmap.BitmapCacheManager;
import com.duongame.explorer.bitmap.BitmapLoader;

/**
 * Created by namjungsoo on 2016-12-16.
 */

public class PreloadBitmapTask extends AsyncTask<String, Void, Bitmap> {
    private static final int RETRY_INTERVAL_MS = 500;
    private static final int RETRY_COUNT = 5;

    private int width, height;

    public PreloadBitmapTask(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        for (int i = 0; i < params.length; i++) {
            final String path = params[i];

            // 캐시에 없으면
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
                        Log.w("PreloadBitmapTask", "decode retry=" + count);
                    } else {
                        BitmapCacheManager.setBitmap(path, bitmap);
                        break;
                    }
                }
            }
        }
        return null;
    }
}
