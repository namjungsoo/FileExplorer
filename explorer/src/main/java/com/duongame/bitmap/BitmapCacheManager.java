package com.duongame.bitmap;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import com.duongame.adapter.ExplorerItem;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by namjungsoo on 2016-11-16.
 */

public class BitmapCacheManager {
    private final static String TAG = BitmapCacheManager.class.getSimpleName();
    private final static boolean DEBUG = false;

    // 썸네일 관련
    static ConcurrentHashMap<String, Bitmap> thumbnailCache = new ConcurrentHashMap<String, Bitmap>();
//    static ConcurrentHashMap<String, ImageView> thumbnailImageCache = new ConcurrentHashMap<>();

    // 일반 이미지 관련
    static ConcurrentHashMap<String, Bitmap> bitmapCache = new ConcurrentHashMap<>();

    // 리소스나 아이콘 관련
    static ConcurrentHashMap<Integer, Bitmap> resourceCache = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, Drawable> drawableCache = new ConcurrentHashMap<>();

    // zip파일 잘린 이미지 관련
    static ConcurrentHashMap<String, Bitmap> pageCache = new ConcurrentHashMap<>();

    public static String changePathToPage(ExplorerItem item) {
        String path;
        if (item.side == ExplorerItem.Side.LEFT) {
            path = item.path + ".left";
        } else if (item.side == ExplorerItem.Side.RIGHT) {
            path = item.path + ".right";
        } else {
            path = item.path;
        }
        return path;
    }

    // current_page
    public static void setPage(String key, Bitmap bitmap) {
        pageCache.putIfAbsent(key, bitmap);
    }

    public static Bitmap getPage(String key) {
        return pageCache.get(key);
    }

    public static void removePage(String key) {
        Bitmap bitmap = pageCache.get(key);
        if (bitmap != null) {
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            bitmap = null;
            pageCache.remove(key);
        }
    }

    public static void removeAllPages() {
        for (String key : pageCache.keySet()) {
            Bitmap bitmap = pageCache.get(key);
            if (bitmap != null) {
                if (!bitmap.isRecycled())
                    bitmap.recycle();
                bitmap = null;
            }
        }
        pageCache.clear();
    }

    // resource bitmap
    public static Bitmap getResourceBitmap(Resources res, int resId) {
        Bitmap bitmap = resourceCache.get(resId);
        if (bitmap == null) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeResource(res, resId, options);
            if (bitmap != null) {
                resourceCache.putIfAbsent(resId, bitmap);
            }
        }
        return bitmap;
    }

    // drawable
    public static void setDrawable(String path, Drawable drawable) {
        if (path == null || drawable == null)
            return;

        drawableCache.putIfAbsent(path, drawable);
    }

    public static Drawable getDrawable(String path) {
        return drawableCache.get(path);
    }


    // image bitmap
    public static void setBitmap(String path, Bitmap bitmap) {
        bitmapCache.putIfAbsent(path, bitmap);
    }

    public static Bitmap getBitmap(String path) {
        return bitmapCache.get(path);
    }

    public static void removeBitmap(String path) {
        Bitmap bitmap = bitmapCache.get(path);
        if (bitmap != null) {
            if (!bitmap.isRecycled())
                bitmap.recycle();
            bitmap = null;
            bitmapCache.remove(path);
        }
    }

    public static void removeAllDrawables() {
        drawableCache.clear();
    }

    public static void removeAllBitmaps() {
        for (String key : bitmapCache.keySet()) {
            Bitmap bitmap = bitmapCache.get(key);
            if (bitmap != null) {
                if (!bitmap.isRecycled())
                    bitmap.recycle();
                bitmap = null;
            }
        }
        bitmapCache.clear();
    }

    // thumbnail
    public static void setThumbnail(String path, Bitmap bitmap) {
        thumbnailCache.putIfAbsent(path, bitmap);

        // 사용안함
//        if(imageView != null)
//            thumbnailImageCache.putIfAbsent(path, imageView);
    }

    public static Bitmap getThumbnail(String path) {
        return thumbnailCache.get(path);
    }

    public static int getThumbnailCount() {
        return thumbnailCache.size();
    }

    public static void removeAllThumbnails() {
        // 이미지를 먼저 null로 하고 => try/catch로 처리함
        // adapter 자체적으로 file, directory 이미지로 처리하게 변경됨
        // 사용안함
//        for (String key : thumbnailImageCache.keySet()) {
//            final ImageView imageView = thumbnailImageCache.get(key);
//            if (imageView != null) {
//                imageView.setImageResource(android.R.color.transparent);
//            }
//        }
//        thumbnailImageCache.clear();

        final ArrayList<String> recycleList = new ArrayList<>();
        for (String key : thumbnailCache.keySet()) {
            Bitmap bitmap = thumbnailCache.get(key);
            //if (bitmap != null && !bitmap.isRecycled()) {
            if (bitmap != null) {
                // 리소스(아이콘)용 썸네일이 아니면 삭제
                if (!resourceCache.containsValue(bitmap)) {
                    bitmap.recycle();
                    recycleList.add(key);
                }
                bitmap = null;
            }
        }

        for (String key : recycleList) {
            thumbnailCache.remove(key);
        }
        thumbnailCache.clear();
    }
}