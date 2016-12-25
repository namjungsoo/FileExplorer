package com.duongame.explorer.task;

import android.graphics.Bitmap;

import com.duongame.explorer.adapter.ExplorerFileItem;
import com.duongame.explorer.bitmap.BitmapCacheManager;

/**
 * Created by namjungsoo on 2016. 12. 25..
 */

public class RemoveAndPreloadTask extends BitmapTask {

    private ExplorerFileItem[] removeList;
    public void setRemoveArray(ExplorerFileItem[] removeList) {
        this.removeList = removeList;
    }

    public RemoveAndPreloadTask(int width, int height, boolean exif) {
        super(width, height, exif);
    }

    @Override
    protected Bitmap doInBackground(ExplorerFileItem... params) {
        if(removeList != null) {
            // remove
            for (int i = 0; i < removeList.length; i++) {
                final ExplorerFileItem item = removeList[i];

                if (item.side == ExplorerFileItem.Side.SIDE_ALL)
                    BitmapCacheManager.removeBitmap(item.path);
                else {
                    String path = BitmapCacheManager.changePath(item);
                    BitmapCacheManager.removePage(path);
                }
            }
        }

        if(params != null) {
            // preload
            for (int i = 0; i < params.length; i++) {
                ExplorerFileItem item = params[i];

                if(!isCancelled())
                    loadBitmap(item);
            }
        }
        return null;
    }

}
