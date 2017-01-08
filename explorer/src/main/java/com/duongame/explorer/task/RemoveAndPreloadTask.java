package com.duongame.explorer.task;

import android.graphics.Bitmap;

import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.bitmap.BitmapCache;

/**
 * Created by namjungsoo on 2016. 12. 25..
 */

public class RemoveAndPreloadTask extends BitmapTask {

    private ExplorerItem[] removeList;
    public void setRemoveArray(ExplorerItem[] removeList) {
        this.removeList = removeList;
    }

    public RemoveAndPreloadTask(int width, int height, boolean exif) {
        super(width, height, exif);
    }

    @Override
    protected Bitmap doInBackground(ExplorerItem... params) {
        if(removeList != null) {
            // remove
            for (int i = 0; i < removeList.length; i++) {
                final ExplorerItem item = removeList[i];

                if (item.side == ExplorerItem.Side.SIDE_ALL)
                    BitmapCache.removeBitmap(item.path);
                else {
                    String path = BitmapCache.changePathToPage(item);
                    BitmapCache.removePage(path);
                }
            }
        }

        if(params != null) {
            // preload
            for (int i = 0; i < params.length; i++) {
                ExplorerItem item = params[i];

                if(!isCancelled())
                    loadBitmap(item);
            }
        }
        return null;
    }

}
