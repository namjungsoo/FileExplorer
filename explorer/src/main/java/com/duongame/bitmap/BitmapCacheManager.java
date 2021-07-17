package com.duongame.bitmap;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.duongame.adapter.ExplorerItem;
import com.duongame.file.FileHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import timber.log.Timber;

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

    private static Map<String, ArrayList<BitmapCache>> thumbnailCache = new ConcurrentHashMap<>();// 썸네일
    private static Map<String, BitmapCache> bitmapCache = new ConcurrentHashMap<>();// 일반 이미지
    private static Map<String, BitmapCache> pageCache = new ConcurrentHashMap<>();// zip파일 잘린 이미지

    // path와 side정보를 string으로 변환
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
    public static void setPage(String key, Bitmap bitmap, ImageView imageView) {
        if (key == null)
            return;

        BitmapCache cache = pageCache.get(key);
        int imageViewHash = imageView != null ? imageView.hashCode() : 0;
        int bitmapHash = bitmap != null ? bitmap.hashCode() : 0;

        if (cache == null) {
            Timber.e("setPage cache == null " + key + " " + bitmapHash + " " + imageViewHash);
            cache = new BitmapCache();
            cache.bitmap = bitmap;
            cache.imageViewRef = new WeakReference<>(imageView);
            pageCache.put(key, cache);
        } else {
            Timber.e("setPage cache != null " + key + " " + bitmapHash + " " + imageViewHash);
            if (cache.bitmap != null && cache.bitmap != bitmap) {
                Timber.e("setPage removeBitmapCache cache.bitmap changed " + key + " " + bitmapHash + " " + imageViewHash);
                removeBitmapCache(cache, 0);
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

    public static Bitmap getPage(String key) {
        BitmapCache cache = pageCache.get(key);
        if (cache == null) {
            return null;
        }
        return cache.bitmap;
    }

    private static void removeBitmapCache(BitmapCache cache, int resId) {
        if (cache.bitmap == null) {
            Timber.e("removeBitmapCache bitmap is null");
            return;
        }
        if (cache.bitmap.isRecycled()) {
            Timber.e("removeBitmapCache is already recycled");
            return;
        }
        if (cache.imageViewRef == null) {
            Timber.e("removeBitmapCache imageViewRef is null");
            return;
        }
        ImageView imageView = cache.imageViewRef.get();
        if (imageView == null) {
            Timber.e("removeBitmapCache imageView is null");
            return;
        }
        if(resId > 0) {
            imageView.setImageResource(resId);
        } else {
            imageView.setImageBitmap(null);
        }
        Timber.e("removeBitmapCache imageView set bitmap null");
    }

    public static void removePage(String key) {
        BitmapCache cache = pageCache.get(key);
        if (cache == null) {
            return;
        }
        Timber.e("removePage removeBitmapCache");
        removeBitmapCache(cache, 0);
        pageCache.remove(key);
    }

    public static void removeAllPages() {
        Timber.e("removeAllPages");
        for (String key : pageCache.keySet()) {
            BitmapCache cache = pageCache.get(key);
            if (cache == null) {
                continue;
            }
            Timber.e("removeAllPages removeBitmapCache");
            removeBitmapCache(cache, 0);
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
                Timber.e("setBitmap removeBitmapCache");
                removeBitmapCache(cache, 0);
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
        Timber.e("removeBitmap removeBitmapCache");
        removeBitmapCache(cache, 0);
        bitmapCache.remove(path);
    }

    public static void removeAllBitmaps() {
        Timber.e("removeAllBitmaps");

        for (String key : bitmapCache.keySet()) {
            BitmapCache cache = bitmapCache.get(key);
            if (cache == null) {
                continue;
            }
            Timber.e("removeAllBitmaps removeBitmapCache");
            removeBitmapCache(cache, 0);
        }
        bitmapCache.clear();
    }

    //TODO: 하나의 썸네일이 여러곳에서 사용될수 있으니 해당 부분에 대해서 조치를 해야함
    public static void setThumbnail(String path, Bitmap bitmap, ImageView imageView) {
        if (path == null) {
            Timber.e("setThumbnail path is null");
            return;
        }

        Timber.e("setThumbnail " + path);
        ArrayList<BitmapCache> cacheList = thumbnailCache.get(path);
        if (cacheList == null) {
            cacheList = new ArrayList<>();

            // imageview가 null이라도 무조건 대입
            BitmapCache cache = new BitmapCache();
            cache.bitmap = bitmap;
            cache.imageViewRef = new WeakReference<>(imageView);

            cacheList.add(cache);
            thumbnailCache.put(path, cacheList);
            Timber.e("setThumbnail " + path + " new");
        } else {
            for (BitmapCache cache : cacheList) {
                // 이미 있으면 리턴
                if (cache.bitmap == bitmap && cache.imageViewRef.get() == imageView) {
                    Timber.e("setThumbnail " + path + " reject");
                    return;
                }

                // 캐쉬와 다르면 거절
                if (cache.bitmap != bitmap) {
                    Timber.e("setThumbnail " + path + " reject");
                    return;
                }
            }

            // 같은 bitmap, 다른 imageView를 사용하는 것이다.
            // 한번에 모두 초기화 해주어야 한다.
            BitmapCache newCache = new BitmapCache();
            newCache.bitmap = cacheList.get(0).bitmap;
            newCache.imageViewRef = new WeakReference<>(imageView);
            cacheList.add(newCache);
            Timber.e("setThumbnail " + path + " add");
        }
    }

    public static Bitmap getThumbnail(String path) {
        Timber.e("getThumbnail " + path);
        ArrayList<BitmapCache> cacheList = thumbnailCache.get(path);
        if (cacheList == null) {
            return null;
        }
        if (cacheList.size() == 0) {
            return null;
        }
        Timber.e("getThumbnail " + path + " OK");
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

            Timber.e("removeAllThumbnails key=" + key + " size=" + cacheList.size());

            // 모든걸 다 지운다.
            for (BitmapCache cache : cacheList) {
                Timber.e("removeAllThumbnails key=" + key + " remove");

                int resId = FileHelper.getFileFolderIconResId(key);
                removeBitmapCache(cache, resId);
            }
        }
        thumbnailCache.clear();
    }
}
