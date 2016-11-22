package com.duongame.fileexplorer.bitmap;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import java.util.HashMap;

/**
 * Created by namjungsoo on 2016-11-16.
 */

public class BitmapCacheManager {
    static HashMap<String, Bitmap> thumbnailCache = new HashMap<String, Bitmap>();
    static HashMap<String, ImageView> thumbnailImageCache = new HashMap<>();

    static HashMap<String, Bitmap> bitmapCache = new HashMap<>();
    static HashMap<Integer, Bitmap> resourceCache = new HashMap<>();

    static HashMap<String, Drawable> drawableCache = new HashMap<>();

    // resource bitmap
    public static Bitmap getResourceBitmap(Resources res, int resId) {
        Bitmap bitmap = resourceCache.get(resId);
        if(bitmap == null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeResource(res, resId, options);
            if (bitmap != null) {
                resourceCache.put(resId, bitmap);
            }
        }
        return bitmap;
    }

    // drawable 
    public static void setDrawable(String path, Drawable drawable) {
        if (drawableCache.get(path) == null) {
            drawableCache.put(path, drawable);
        }
    }

    public static Drawable getDrawable(String path) {
        return drawableCache.get(path);
    }

    // image bitmap
    public static void setBitmap(String path, Bitmap bitmap) {
        if (bitmapCache.get(path) == null) {
            bitmapCache.put(path, bitmap);
        }
    }

    public static Bitmap getBitmap(String path) {
        return bitmapCache.get(path);
    }

    public static void removeBitmap(String path) {
        if (bitmapCache.get(path) != null) {
            bitmapCache.get(path).recycle();
            bitmapCache.remove(path);
        }
    }

    public static void recycleBitmap() {
        for (String key : bitmapCache.keySet()) {
            if (bitmapCache.get(key) != null)
                bitmapCache.get(key).recycle();
        }
        bitmapCache.clear();
    }

    // thumbnail
    public static void setThumbnail(String path, Bitmap bitmap, ImageView imageView) {
        if (thumbnailCache.get(path) == null) {
            thumbnailCache.put(path, bitmap);
            thumbnailImageCache.put(path, imageView);
        }
    }

    public static Bitmap getThumbnail(String path) {
        return thumbnailCache.get(path);
    }

    public static int getThumbnailCount() {
        return thumbnailCache.size();
    }

    public static void recycleThumbnail() {
        for (String key : thumbnailCache.keySet()) {
            if (thumbnailImageCache.get(key) != null)
                thumbnailImageCache.get(key).setImageBitmap(null);

            if (thumbnailCache.get(key) != null) {
                if(!resourceCache.containsValue(thumbnailCache.get(key)))
                    thumbnailCache.get(key).recycle();
            }
        }
        thumbnailCache.clear();
        thumbnailImageCache.clear();
    }
}
