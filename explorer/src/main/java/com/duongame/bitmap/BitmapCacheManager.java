package com.duongame.bitmap;

import android.graphics.Bitmap;
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

    private static ConcurrentHashMap<String, ArrayList<BitmapCache>> thumbnailCache = new ConcurrentHashMap<>();// 썸네일
    private static ConcurrentHashMap<String, BitmapCache> bitmapCache = new ConcurrentHashMap<>();// 일반 이미지
    private static ConcurrentHashMap<String, BitmapCache> pageCache = new ConcurrentHashMap<>();// zip파일 잘린 이미지

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
                JLog.e(TAG, "setPage removeBitmapCache " + key + " " + bitmap + " " + imageView);
                removeBitmapCache(cache);
            }
            // bitmap은 정리가 끝났으므로 바로 대입
            cache.bitmap = bitmap;

            // imageview는 무조건 대입
            if (imageView != null) {
                cache.imageViewRef = new WeakReference<>(imageView);
            } else {
                // imageView가 null일때 삭제할 것이냐? 일때 update = true이다.
                if (update) {
                    cache.imageViewRef = null;
                }
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

    private static void removeBitmapCache(BitmapCache cache) {
        if (cache.bitmap != null) {
            if (!cache.bitmap.isRecycled()) {
                if (cache.imageViewRef != null) {
                    ImageView imageView = cache.imageViewRef.get();
                    if (imageView != null) {
                        imageView.setImageBitmap(null);
                        JLog.e(TAG, "removeBitmapCache imageView set null");
                    }
                }
                cache.bitmap.recycle();
                JLog.e(TAG, "removeBitmapCache recycle");
            }
            cache.bitmap = null;
        }
    }

    public static void removePage(String key) {
        BitmapCache cache = pageCache.get(key);
        if (cache == null) {
            return;
        }
        JLog.e(TAG, "removePage removeBitmapCache");
        removeBitmapCache(cache);
        pageCache.remove(key);
    }

    public static void removeAllPages() {
        Log.e(TAG, "removeAllPages");
        for (String key : pageCache.keySet()) {
            BitmapCache cache = pageCache.get(key);
            if (cache == null) {
                continue;
            }
            JLog.e(TAG, "removeAllPages removeBitmapCache");
            removeBitmapCache(cache);
        }
        pageCache.clear();
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
                JLog.e(TAG, "setBitmap removeBitmapCache");
                removeBitmapCache(cache);
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
        JLog.e(TAG, "removeBitmap removeBitmapCache");
        removeBitmapCache(cache);
        bitmapCache.remove(path);
    }

    public static void removeAllBitmaps() {
        Log.e(TAG, "removeAllBitmaps");

        for (String key : bitmapCache.keySet()) {
            BitmapCache cache = bitmapCache.get(key);
            if (cache == null) {
                continue;
            }
            JLog.e(TAG, "removeAllBitmaps removeBitmapCache");
            removeBitmapCache(cache);
        }
        bitmapCache.clear();
    }

    //TODO: 하나의 썸네일이 여러곳에서 사용될수 있으니 해당 부분에 대해서 조치를 해야함
    // thumbnail
    public static void setThumbnail(String path, Bitmap bitmap, ImageView imageView) {
        if (path == null)
            return;
        ArrayList<BitmapCache> cacheList = thumbnailCache.get(path);
        if (cacheList == null) {
            cacheList = new ArrayList<>();

            // imageview가 null이라도 무조건 대입
            BitmapCache cache = new BitmapCache();
            cache.bitmap = bitmap;
            cache.imageViewRef = new WeakReference<>(imageView);

            cacheList.add(cache);
            thumbnailCache.put(path, cacheList);
        } else {
            for(BitmapCache cache : cacheList) {
                // 이미 있으면 리턴
                if(cache.bitmap == bitmap && cache.imageViewRef.get() == imageView)
                    return;

                // 캐쉬와 다르면 거절
                if(cache.bitmap != bitmap)
                    return;
            }

            BitmapCache newCache = new BitmapCache();
            newCache.bitmap = cacheList.get(0).bitmap;
            newCache.imageViewRef = new WeakReference<>(imageView);
        }
    }

    public static Bitmap getThumbnail(String path) {
        ArrayList<BitmapCache> cacheList = thumbnailCache.get(path);
        if (cacheList == null) {
            return null;
        }
        if (cacheList.size() == 0) {
            return null;
        }
        return cacheList.get(0).bitmap;
    }

    public static int getThumbnailCount() {
        return thumbnailCache.size();
    }

    public static void removeAllThumbnails() {
        for (String key : thumbnailCache.keySet()) {
            ArrayList<BitmapCache> cacheList = thumbnailCache.get(key);
            if (cacheList == null) {
                continue;
            }

            JLog.e(TAG, "removeAllThumbnails removeBitmapCache");

            // 모든걸 다 지운다.
            for (BitmapCache cache : cacheList) {
                removeBitmapCache(cache);
            }
        }
        thumbnailCache.clear();
    }
}
