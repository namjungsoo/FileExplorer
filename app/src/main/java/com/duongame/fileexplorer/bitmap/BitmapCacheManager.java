package com.duongame.fileexplorer.bitmap;

import android.graphics.Bitmap;
import android.widget.ImageView;

import java.util.HashMap;

/**
 * Created by namjungsoo on 2016-11-16.
 */

public class BitmapCacheManager {
    static HashMap<String, Bitmap> thumbnailCache = new HashMap<String, Bitmap>();
    static HashMap<String, Bitmap> bitmapCache = new HashMap<>();

    static HashMap<String, ImageView> thumbnailImageCache = new HashMap<>();
    static HashMap<String, ImageView> bitmapImageCache = new HashMap<>();

    public static void setBitmap(String path, Bitmap bitmap, ImageView imageView) {
        if(bitmapCache.get(path) == null) {
            bitmapCache.put(path, bitmap);
            bitmapImageCache.put(path, imageView);
        }
    }

    public static Bitmap getBitmap(String path) {
        return bitmapCache.get(path);
    }

    public static void removeBitmapImage(String path) {
        if(bitmapImageCache.get(path) != null) {
            bitmapImageCache.get(path).setImageBitmap(null);
            bitmapImageCache.remove(path);
        }
    }

    public static void removeBitmap(String path) {
        if(bitmapCache.get(path) != null) {
            bitmapCache.get(path).recycle();
            bitmapCache.remove(path);
        }
    }

    public static void recycleBitmap() {
        for (String key : bitmapCache.keySet()) {
            bitmapImageCache.get(key).setImageBitmap(null);
            bitmapCache.get(key).recycle();
        }
        bitmapCache.clear();
        bitmapImageCache.clear();
    }

    public static void setThumbnail(String path, Bitmap bitmap, ImageView imageView) {
        if(thumbnailCache.get(path) == null) {
            thumbnailCache.put(path, bitmap);
            thumbnailImageCache.put(path, imageView);
        }
    }

    public static Bitmap getThumbnail(String path) {
        return thumbnailCache.get(path);
    }

    public static void recycleThumbnail() {
        for (String key : thumbnailCache.keySet()) {
            thumbnailImageCache.get(key).setImageBitmap(null);
            thumbnailCache.get(key).recycle();
        }
        thumbnailCache.clear();
        thumbnailImageCache.clear();
    }
}
