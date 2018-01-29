package com.duongame.task.bitmap;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.duongame.adapter.ExplorerItem;
import com.duongame.bitmap.BitmapCacheManager;

/**
 * Created by namjungsoo on 2016-12-17.
 */

public class RemoveBitmapTask extends AsyncTask<ExplorerItem, Void, Bitmap> {
    @Override
    protected Bitmap doInBackground(ExplorerItem... params) {
        for (int i = 0; i < params.length; i++) {
            final ExplorerItem item = params[i];

            if (item.side == ExplorerItem.SIDE_ALL)
                BitmapCacheManager.removeBitmap(item.path);
            else {
                String path = BitmapCacheManager.changePathToPage(item);
                BitmapCacheManager.removePage(path);
            }
        }
        return null;
    }
}
