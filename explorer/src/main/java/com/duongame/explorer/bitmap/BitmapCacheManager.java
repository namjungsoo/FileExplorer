package com.duongame.explorer.bitmap;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by namjungsoo on 2016-11-16.
 */

public class BitmapCacheManager {
    private final static String TAG="BitmapCacheManager";
    static HashMap<String, Bitmap> thumbnailCache = new HashMap<String, Bitmap>();
    static HashMap<String, ImageView> thumbnailImageCache = new HashMap<>();

    static HashMap<String, Bitmap> bitmapCache = new HashMap<>();
    static HashMap<Integer, Bitmap> resourceCache = new HashMap<>();

    static HashMap<String, Drawable> drawableCache = new HashMap<>();

    // resource bitmap
    public static Bitmap getResourceBitmap(Resources res, int resId) {
        synchronized (resourceCache) {
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
    }

    // drawable
    public static void setDrawable(String path, Drawable drawable) {
        synchronized(drawableCache) {
            if (drawableCache.get(path) == null) {
                drawableCache.put(path, drawable);
            }
        }
    }

    public static Drawable getDrawable(String path) {
        synchronized(drawableCache) {
            return drawableCache.get(path);
        }
    }

    // image bitmap
    public static void setBitmap(String path, Bitmap bitmap) {
        synchronized(bitmapCache) {
            if (bitmapCache.get(path) == null) {
                bitmapCache.put(path, bitmap);
            }
        }
    }

    public static Bitmap getBitmap(String path) {
        synchronized(bitmapCache) {
            return bitmapCache.get(path);
        }
    }

    public static void removeBitmap(String path) {
        synchronized(bitmapCache) {
            if (bitmapCache.get(path) != null) {
                bitmapCache.get(path).recycle();
                bitmapCache.remove(path);
            }
        }
    }

    public static void recycleBitmap() {
        synchronized (bitmapCache) {
            for (String key : bitmapCache.keySet()) {
                if (bitmapCache.get(key) != null)
                    bitmapCache.get(key).recycle();
            }
            bitmapCache.clear();
        }
    }

    // thumbnail
    public static void setThumbnail(String path, Bitmap bitmap, ImageView imageView) {
        synchronized (thumbnailCache) {
            if (thumbnailCache.get(path) == null) {
                thumbnailCache.put(path, bitmap);
                synchronized (thumbnailImageCache) {
                    thumbnailImageCache.put(path, imageView);
                }
            }
        }
    }

    public static Bitmap getThumbnail(String path) {
        synchronized (thumbnailCache) {
            return thumbnailCache.get(path);
        }
    }

    public static int getThumbnailCount() {
        synchronized (thumbnailCache) {
            return thumbnailCache.size();
        }
    }

    public static void recycleThumbnail() {
        // 이미지를 먼저 null로 하고
        synchronized (thumbnailImageCache) {
            for (String key : thumbnailImageCache.keySet()) {
                ImageView imageView = thumbnailImageCache.get(key);
                if (imageView != null) {
                    imageView.setImageBitmap(null);
                }
            }
            thumbnailImageCache.clear();
        }

        synchronized (thumbnailCache) {
            ArrayList<String> recycleList = new ArrayList<>();
            for (String key : thumbnailCache.keySet()) {
                if (thumbnailCache.get(key) != null) {
                    // 리소스(아이콘)용 썸네일이 아니면 삭제
                    synchronized (resourceCache) {
                        if (!resourceCache.containsValue(thumbnailCache.get(key))) {
                            thumbnailCache.get(key).recycle();
                            recycleList.add(key);
                        }
                    }
                }
            }

            for (String key : recycleList) {
                thumbnailCache.remove(key);
            }
        }
    }
}
