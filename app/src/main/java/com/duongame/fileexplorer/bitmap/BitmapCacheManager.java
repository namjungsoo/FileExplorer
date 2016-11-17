package com.duongame.fileexplorer.bitmap;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import java.util.HashMap;

/**
 * Created by namjungsoo on 2016-11-16.
 */

public class BitmapCacheManager {
    static HashMap<String, Bitmap> thumbnailCache = new HashMap<String, Bitmap>();
    static HashMap<String, Bitmap> bitmapCache = new HashMap<>();
    static HashMap<Bitmap, ImageView> imageViewCache = new HashMap<>();

    public static void setBitmap(String path, Bitmap bitmap, ImageView imageView) {
        if(bitmapCache.get(path) == null) {
            bitmapCache.put(path, bitmap);
            imageViewCache.put(bitmap, imageView);
        }
    }

    public static Bitmap getBitmap(String path) {
        return bitmapCache.get(path);
    }

    public static void removeBitmap(String path) {
        if(bitmapCache.get(path) != null) {
            imageViewCache.get(bitmapCache.get(path)).setImageBitmap(null);
            bitmapCache.get(path).recycle();

            imageViewCache.remove(bitmapCache.get(path));
            bitmapCache.remove(path);
            Log.d("cache", "removeBitmap path="+path);
        }
    }

    public static void recycleBitmap() {
        for (String key : bitmapCache.keySet()) {
            imageViewCache.get(bitmapCache.get(key)).setImageBitmap(null);
            bitmapCache.get(key).recycle();
        }
        bitmapCache.clear();
        imageViewCache.clear();
    }

    public static void setThumbnail(String path, Bitmap bitmap) {
        if(thumbnailCache.get(path) == null) {
            thumbnailCache.put(path, bitmap);
        }
    }

    public static Bitmap getThumbnail(String path) {
        return thumbnailCache.get(path);
    }

    public static void recycleThumbnail() {
        for (String key : thumbnailCache.keySet()) {
            thumbnailCache.get(key).recycle();
        }
        thumbnailCache.clear();
    }
}
