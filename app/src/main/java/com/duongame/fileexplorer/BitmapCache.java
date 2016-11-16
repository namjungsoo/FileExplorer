package com.duongame.fileexplorer;

import android.graphics.Bitmap;

import java.util.HashMap;

/**
 * Created by namjungsoo on 2016-11-16.
 */

public class BitmapCache {
    static HashMap<String, Bitmap> thumbnailCache = new HashMap<String, Bitmap>();

    public static void setBitmap(String path, Bitmap bitmap) {
        if(thumbnailCache.get(path) == null) {
            thumbnailCache.put(path, bitmap);
        }
    }

    public static Bitmap getBitmap(String path) {
        return thumbnailCache.get(path);
    }

    public static void recycleAll() {
        for (String key : thumbnailCache.keySet()) {
            thumbnailCache.get(key).recycle();
        }
        thumbnailCache.clear();
    }
}
