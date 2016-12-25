package com.duongame.explorer.bitmap;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import com.duongame.explorer.adapter.ExplorerFileItem;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by namjungsoo on 2016-11-16.
 */

public class BitmapCacheManager {
    private final static String TAG = "BitmapCacheManager";

    // 썸네일 관련
    static HashMap<String, Bitmap> thumbnailCache = new HashMap<String, Bitmap>();
    static HashMap<String, ImageView> thumbnailImageCache = new HashMap<>();

    // 일반 이미지 관련
    static HashMap<String, Bitmap> bitmapCache = new HashMap<>();

    // 리소스나 아이콘 관련
    static HashMap<Integer, Bitmap> resourceCache = new HashMap<>();
    static HashMap<String, Drawable> drawableCache = new HashMap<>();

    // zip파일 잘린 이미지 관련
    static HashMap<String, Bitmap> pageCache = new HashMap<>();

    public static String changePath(ExplorerFileItem item) {
        String path;
        if (item.side == ExplorerFileItem.Side.LEFT) {
            path = item.path + ".left";
        } else if (item.side == ExplorerFileItem.Side.RIGHT) {
            path = item.path + ".right";
        } else {
            path = item.path;
        }
        return path;
    }

    // page
    public static void setPage(String key, Bitmap bitmap) {
//        synchronized (pageCache)
        {
            pageCache.put(key, bitmap);
        }
    }

    public static Bitmap getPage(String key) {
//        synchronized (pageCache)
        {
            return pageCache.get(key);
        }
    }

    public static void removePage(String key) {
//        synchronized (pageCache)
        {
            final Bitmap bitmap = pageCache.get(key);
            //if (bitmap != null && !bitmap.isRecycled()) {
            if (bitmap != null) {
                bitmap.recycle();
                pageCache.remove(key);
            }
        }
    }

    public static void recyclePage() {
//        synchronized (pageCache)
        {
            for (String key : pageCache.keySet()) {
                final Bitmap bitmap = pageCache.get(key);
                //if (bitmap != null && !bitmap.isRecycled()) {
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }
            pageCache.clear();
        }
        Log.d(TAG, "recyclePage");
    }

    // resource bitmap
    public static Bitmap getResourceBitmap(Resources res, int resId) {
//        synchronized (resourceCache)
        {
            Bitmap bitmap = resourceCache.get(resId);
            if (bitmap == null) {
                final BitmapFactory.Options options = new BitmapFactory.Options();
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
//        synchronized (drawableCache)
        {
            if (drawableCache.get(path) == null) {
                drawableCache.put(path, drawable);
            }
        }
    }

    public static Drawable getDrawable(String path) {
//        synchronized (drawableCache)
        {
            return drawableCache.get(path);
        }
    }

    // image bitmap
    public static void setBitmap(String path, Bitmap bitmap) {
//        synchronized (bitmapCache)
        {
            if (bitmapCache.get(path) == null) {
                bitmapCache.put(path, bitmap);
                Log.d(TAG, "setBitmap bitmapCache size=" + bitmapCache.size());
            }
        }
    }

    public static Bitmap getBitmap(String path) {
//        synchronized (bitmapCache)
        {
            return bitmapCache.get(path);
        }
    }

    public static void removeBitmap(String path) {
//        synchronized (bitmapCache)
        {
            final Bitmap bitmap = bitmapCache.get(path);
            //if (bitmap != null && !bitmap.isRecycled()) {
            if (bitmap != null) {
                bitmap.recycle();
                bitmapCache.remove(path);
                Log.d(TAG, "removeBitmap removeBitmap=" + path + " size=" + bitmapCache.size());
            }
        }
    }

    public static void recycleBitmap() {
//        synchronized (bitmapCache)
        {
            for (String key : bitmapCache.keySet()) {
                final Bitmap bitmap = bitmapCache.get(key);
                //if (bitmap != null && !bitmap.isRecycled())
                if (bitmap != null)
                    bitmap.recycle();
            }
            bitmapCache.clear();
        }
        Log.d(TAG, "recycleBitmap");
    }

    // thumbnail
    public static void setThumbnail(String path, Bitmap bitmap, ImageView imageView) {
//        synchronized (thumbnailCache)
        {
            if (thumbnailCache.get(path) == null) {
                thumbnailCache.put(path, bitmap);
//                synchronized (thumbnailImageCache)
                {
                    thumbnailImageCache.put(path, imageView);
                }
            }
        }
    }

    public static Bitmap getThumbnail(String path) {
//        synchronized (thumbnailCache)
        {
            return thumbnailCache.get(path);
        }
    }

    public static int getThumbnailCount() {
//        synchronized (thumbnailCache)
        {
            return thumbnailCache.size();
        }
    }

    public static void recycleThumbnail() {
//        synchronized (thumbnailImageCache)
        {
            // 이미지를 먼저 null로 하고 => try/catch로 처리함
            for (String key : thumbnailImageCache.keySet()) {
                final ImageView imageView = thumbnailImageCache.get(key);
                if (imageView != null) {
                    imageView.setImageResource(android.R.color.transparent);
                }
            }
            thumbnailImageCache.clear();
        }

//        synchronized (thumbnailCache)
        {
            final ArrayList<String> recycleList = new ArrayList<>();
            for (String key : thumbnailCache.keySet()) {
                final Bitmap bitmap = thumbnailCache.get(key);
                //if (bitmap != null && !bitmap.isRecycled()) {
                if (bitmap != null) {
                    // 리소스(아이콘)용 썸네일이 아니면 삭제
//                    synchronized (resourceCache)
                    {
                        if (!resourceCache.containsValue(bitmap)) {
                            bitmap.recycle();
                            recycleList.add(key);
                        }
                    }
                }
            }

            for (String key : recycleList) {
                thumbnailCache.remove(key);
            }
            thumbnailCache.clear();
        }
        Log.d(TAG, "recycleThumbnail");
    }
}
