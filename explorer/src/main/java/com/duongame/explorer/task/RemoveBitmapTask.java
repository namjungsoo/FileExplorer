package com.duongame.explorer.task;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.duongame.explorer.adapter.ExplorerFileItem;
import com.duongame.explorer.bitmap.BitmapCacheManager;
import com.duongame.explorer.bitmap.PageKey;

/**
 * Created by namjungsoo on 2016-12-17.
 */

public class RemoveBitmapTask extends AsyncTask<ExplorerFileItem, Void, Bitmap> {
    @Override
    protected Bitmap doInBackground(ExplorerFileItem... params) {
        for (int i = 0; i < params.length; i++) {
            final ExplorerFileItem item = params[i];

            if(item.side == ExplorerFileItem.Side.SIDE_ALL)
                BitmapCacheManager.removeBitmap(item.path);
            else
                BitmapCacheManager.removePage(new PageKey(item.path, item.side));
        }
        return null;
    }
}

