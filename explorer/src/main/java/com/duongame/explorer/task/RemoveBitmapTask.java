package com.duongame.explorer.task;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.duongame.explorer.bitmap.BitmapCacheManager;

/**
 * Created by namjungsoo on 2016-12-17.
 */

public class RemoveBitmapTask extends AsyncTask<String, Void, Bitmap> {
    @Override
    protected Bitmap doInBackground(String... params) {
        for (int i = 0; i < params.length; i++) {
            final String path = params[i];
            BitmapCacheManager.removeBitmap(path);
        }
        return null;
    }
}

