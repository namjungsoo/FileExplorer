package com.duongame.explorer.task.bitmap;

import android.graphics.Bitmap;

import com.duongame.explorer.adapter.ExplorerItem;

/**
 * Created by namjungsoo on 2016-12-16.
 */

public class PreloadBitmapTask extends BitmapTask {
    public PreloadBitmapTask(int width, int height, boolean exif) {
        super(width, height, exif);
    }

    @Override
    protected Bitmap doInBackground(ExplorerItem... params) {
        // preload
        for (int i = 0; i < params.length; i++) {
            ExplorerItem item = params[i];

            if(!isCancelled())
                loadBitmap(item);
        }
        return null;
    }
}
