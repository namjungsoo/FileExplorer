package com.duongame.explorer.task.bitmap;

import android.graphics.Bitmap;
import android.util.Log;

import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.bitmap.BitmapCacheManager;
import com.duongame.explorer.helper.FileHelper;

import static android.content.ContentValues.TAG;

/**
 * Created by namjungsoo on 2016. 12. 25..
 */

public class RemoveAndPreloadBitmapTask extends BitmapTask {

    private ExplorerItem[] removeList;

    public void setRemoveArray(ExplorerItem[] removeList) {
        this.removeList = removeList;
    }

    public RemoveAndPreloadBitmapTask(int width, int height, boolean exif) {
        super(width, height, exif);
    }

    @Override
    protected Bitmap doInBackground(ExplorerItem... params) {
        // 메모리 때문에 지우는 것을 우선함
        // 지우는 것이 속도가 더 빠름
        if (removeList != null) {
            // remove
            for (int i = 0; i < removeList.length; i++) {
                final ExplorerItem item = removeList[i];

                if (item.side == ExplorerItem.Side.SIDE_ALL) {
                    Log.w(TAG, "RemoveAndPreloadBitmapTask REMOVE " + item.path);
                    BitmapCacheManager.removeBitmap(item.path);
                } else {
                    String path = BitmapCacheManager.changePathToPage(item);
                    BitmapCacheManager.removePage(path);
                }
            }
        }

        // 우선순위별로 정렬 되어 있음
        // 0번부터 로딩하게 된다.
        if (params != null) {
            // preload
            for (int i = 0; i < params.length; i++) {
                if(isCancelled())
                    return null;

                ExplorerItem item = params[i];
                if(FileHelper.isGifImage(item.path))
                    continue;

                // preload는 bitmap만 읽어서 캐쉬에 넣어놓는 용도이다.
                Log.w(TAG, "RemoveAndPreloadBitmapTask PRELOAD " + item.path);
                loadBitmap(item);
            }
        }
        return null;
    }

}
