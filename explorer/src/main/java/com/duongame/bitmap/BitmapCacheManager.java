package com.duongame.bitmap;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import com.duongame.adapter.ExplorerItem;
import com.duongame.helper.JLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by namjungsoo on 2016-11-16.
 */

public class BitmapCacheManager {
    private final static String TAG = BitmapCacheManager.class.getSimpleName();
    @SuppressWarnings("unused")
    private final static boolean DEBUG = false;

    private static class BitmapCache {
        Bitmap bitmap;
        WeakReference<ImageView> imageViewRef;
    }

    private static ConcurrentHashMap<String, BitmapCache> thumbnailCache = new ConcurrentHashMap<>();// 썸네일
    private static ConcurrentHashMap<String, BitmapCache> bitmapCache = new ConcurrentHashMap<>();// 일반 이미지
    private static ConcurrentHashMap<String, BitmapCache> pageCache = new ConcurrentHashMap<>();// zip파일 잘린 이미지

    // 리소스나 아이콘 관련(항상 사용하기 위해서 한번 로딩하면 recycle 하지 않는다.)
    private static ConcurrentHashMap<Integer, Bitmap> resourceCache = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Drawable> drawableCache = new ConcurrentHashMap<>();

    public static String changePathToPage(ExplorerItem item) {
        String path;
        if (item.side == ExplorerItem.SIDE_LEFT) {
            path = item.path + ".left";
        } else if (item.side == ExplorerItem.SIDE_RIGHT) {
            path = item.path + ".right";
        } else {
            path = item.path;
        }
        return path;
    }

    // current_page
    public static void setPage(String key, Bitmap bitmap, ImageView imageView, boolean update) {
        if (key == null)
            return;
        BitmapCache cache = pageCache.get(key);
        if (cache == null) {
            cache = new BitmapCache();
            cache.bitmap = bitmap;
            cache.imageViewRef = new WeakReference<>(imageView);
            pageCache.put(key, cache);
        } else {
            if (cache.bitmap != null && cache.bitmap != bitmap) {
                JLog.e(TAG, "setPage removeBitmapOrPageInternal " + key + " " + bitmap + " " + imageView);
                removeBitmapOrPageInternal(cache);
            }
            // bitmap은 정리가 끝났으므로 바로 대입
            cache.bitmap = bitmap;

            // imageview는 무조건 대입
            if (imageView != null) {
                cache.imageViewRef = new WeakReference<>(imageView);
            } else {
                if(update)
                    cache.imageViewRef = null;
            }
        }
    }

    public static Bitmap getPage(String key) {
        BitmapCache cache = pageCache.get(key);
        if (cache == null) {
            return null;
        }
        return cache.bitmap;
    }

    private static void removeBitmapOrPageInternal(BitmapCache cache) {
        if (cache.bitmap != null) {
            if (!cache.bitmap.isRecycled()) {
                if (cache.imageViewRef != null) {
                    ImageView imageView = cache.imageViewRef.get();
                    if (imageView != null) {
                        imageView.setImageBitmap(null);
                        JLog.e(TAG, "removeBitmapOrPageInternal imageView set null");
                    }
                }
                cache.bitmap.recycle();
                JLog.e(TAG, "removeBitmapOrPageInternal recycle");
            }
            cache.bitmap = null;
        }
    }

    public static void removePage(String key) {
        BitmapCache cache = pageCache.get(key);
        if (cache == null) {
            return;
        }
        JLog.e(TAG, "removePage removeBitmapOrPageInternal");
        removeBitmapOrPageInternal(cache);
        pageCache.remove(key);
    }

    public static void removeAllPages() {
        Log.e(TAG, "removeAllPages");
        for (String key : pageCache.keySet()) {
            BitmapCache cache = pageCache.get(key);
            if (cache == null) {
                continue;
            }
            JLog.e(TAG, "removeAllPages removeBitmapOrPageInternal");
            removeBitmapOrPageInternal(cache);
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

    public static void removeAllDrawables() {
        drawableCache.clear();
    }

    // 비트맵은 SIDE_ALL인 경우 1:1 맵핑이 됨
    // image bitmap
    public static void setBitmap(String path, Bitmap bitmap, ImageView imageView) {
        if (path == null)
            return;
        BitmapCache cache = bitmapCache.get(path);
        if (cache == null) {
            cache = new BitmapCache();
            cache.bitmap = bitmap;
            cache.imageViewRef = new WeakReference<>(imageView);
            bitmapCache.put(path, cache);
        } else {
            if (cache.bitmap != null && cache.bitmap != bitmap) {
                JLog.e(TAG, "setBitmap removeBitmapOrPageInternal");
                removeBitmapOrPageInternal(cache);
            }
            // bitmap은 정리가 끝났으므로 바로 대입
            cache.bitmap = bitmap;

            // imageview는 무조건 대입
            if (imageView != null) {
                cache.imageViewRef = new WeakReference<>(imageView);
            } else {
                cache.imageViewRef = null;
            }
        }
    }

    public static Bitmap getBitmap(String path) {
        BitmapCache cache = bitmapCache.get(path);
        if (cache == null) {
            return null;
        }
        return cache.bitmap;
    }

    public static void removeBitmap(String path) {
        BitmapCache cache = bitmapCache.get(path);
        if (cache == null) {
            return;
        }
        JLog.e(TAG, "removeBitmap removeBitmapOrPageInternal");
        removeBitmapOrPageInternal(cache);
        bitmapCache.remove(path);
    }

    public static void removeAllBitmaps() {
        Log.e(TAG, "removeAllBitmaps");

        for (String key : bitmapCache.keySet()) {
            BitmapCache cache = bitmapCache.get(key);
            if (cache == null) {
                continue;
            }
            JLog.e(TAG, "removeAllBitmaps removeBitmapOrPageInternal");
            removeBitmapOrPageInternal(cache);
        }
        bitmapCache.clear();
    }

    //TODO: 하나의 썸네일이 여러곳에서 사용될수 있으니 해당 부분에 대해서 조치를 해야함
    // thumbnail
    public static void setThumbnail(String path, Bitmap bitmap, ImageView imageView) {
        if (path == null)
            return;
        BitmapCache cache = thumbnailCache.get(path);
        if (cache == null) {
            cache = new BitmapCache();
            cache.bitmap = bitmap;
            cache.imageViewRef = new WeakReference<>(imageView);
            thumbnailCache.put(path, cache);
        } else {
            if (cache.bitmap != null && cache.bitmap != bitmap) {
                JLog.e(TAG, "setThumbnail removeBitmapOrPageInternal");
                removeBitmapOrPageInternal(cache);
            }
            // bitmap은 정리가 끝났으므로 바로 대입
            cache.bitmap = bitmap;

            // imageview는 무조건 대입
            if (imageView != null) {
                cache.imageViewRef = new WeakReference<>(imageView);
            } else {
                cache.imageViewRef = null;
            }
        }
    }

    public static Bitmap getThumbnail(String path) {
        BitmapCache cache = thumbnailCache.get(path);
        if (cache == null) {
            return null;
        }
        return cache.bitmap;
    }

    public static int getThumbnailCount() {
        return thumbnailCache.size();
    }

    public static void removeAllThumbnails() {
        final ArrayList<String> recycleList = new ArrayList<>();

        for (String key : thumbnailCache.keySet()) {
            BitmapCache cache = thumbnailCache.get(key);
            if (cache == null) {
                continue;
            }

            // 리소스(아이콘)용 썸네일이 아니면 삭제
            if (!resourceCache.containsValue(cache.bitmap)) {
                JLog.e(TAG, "removeAllThumbnails removeBitmapOrPageInternal");
                removeBitmapOrPageInternal(cache);
                recycleList.add(key);
            }
        }

        for (String key : recycleList) {
            thumbnailCache.remove(key);
        }
    }
}
