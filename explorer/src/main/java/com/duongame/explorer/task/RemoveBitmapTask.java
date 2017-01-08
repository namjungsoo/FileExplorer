package com.duongame.explorer.task;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.bitmap.BitmapCache;

/**
 * Created by namjungsoo on 2016-12-17.
 */

public class RemoveBitmapTask extends AsyncTask<ExplorerItem, Void, Bitmap> {
    @Override
    protected Bitmap doInBackground(ExplorerItem... params) {
        for (int i = 0; i < params.length; i++) {
            final ExplorerItem item = params[i];

            if (item.side == ExplorerItem.Side.SIDE_ALL)
                BitmapCache.removeBitmap(item.path);
            else {
                String path = BitmapCache.changePathToPage(item);
                BitmapCache.removePage(path);
            }
        }
        return null;
    }
}
