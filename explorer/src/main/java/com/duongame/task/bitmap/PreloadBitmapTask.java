package com.duongame.task.bitmap;

import android.graphics.Bitmap;

import com.duongame.adapter.ExplorerItem;
import com.duongame.helper.FileHelper;

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
            if (isCancelled())
                return null;

            ExplorerItem item = params[i];
            if (FileHelper.isGifImage(item.path))
                continue;

            // preload는 bitmap만 읽어서 캐쉬에 넣어놓는 용도이다.
            loadBitmap(item);
        }
        return null;
    }
}
