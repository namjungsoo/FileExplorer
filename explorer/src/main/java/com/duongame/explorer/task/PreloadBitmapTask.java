package com.duongame.explorer.task;

import android.graphics.Bitmap;

import com.duongame.explorer.adapter.ExplorerFileItem;

/**
 * Created by namjungsoo on 2016-12-16.
 */

public class PreloadBitmapTask extends BitmapTask {
    public PreloadBitmapTask(int width, int height, boolean exif) {
        super(width, height, exif);
    }

    @Override
    protected Bitmap doInBackground(ExplorerFileItem... params) {
        for (int i = 0; i < params.length; i++) {
            ExplorerFileItem item = params[i];

            if(!isCancelled())
                loadBitmap(item);
        }
        return null;
    }
}
