package com.duongame.explorer.bitmap;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.duongame.explorer.adapter.ExplorerFileItem;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by namjungsoo on 2016-11-16.
 */

public class BitmapCacheManager {
    private final static String TAG = "BitmapCacheManager";

    static HashMap<String, Bitmap> thumbnailCache = new HashMap<String, Bitmap>();
    static HashMap<String, ImageView> thumbnailImageCache = new HashMap<>();

    static HashMap<String, Bitmap> bitmapCache = new HashMap<>();
    static HashMap<Integer, Bitmap> resourceCache = new HashMap<>();

    static HashMap<String, Drawable> drawableCache = new HashMap<>();

    static HashMap<PageKey, Bitmap> pageCache = new HashMap<>();

    public static void setPage(PageKey key, Bitmap bitmap) {
        pageCache.put(key, bitmap);
    }

    public static Bitmap getPage(PageKey key) {
        return pageCache.get(key);
    }

    public static void removePage(PageKey key) {
        if (pageCache.get(key) != null) {
            pageCache.get(key).recycle();
            pageCache.remove(key);
        }
    }

    public static void recyclePage() {
        for(PageKey key : pageCache.keySet()) {
            if(key.side != ExplorerFileItem.Side.ALL) {// ALL일 경우는 일반 bitmapCache와 공유한다.
                final Bitmap bitmap = pageCache.get(key);
                if(bitmap != null) {
                    bitmap.recycle();
                }
            }
        }
    }

    // resource bitmap
    public static Bitmap getResourceBitmap(Resources res, int resId) {
        Bitmap bitmap = resourceCache.get(resId);
        if (bitmap == null) {
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
//            Log.w(TAG, "setBitmap bitmapCache.size="+bitmapCache.size());
        }
    }

    public static Bitmap getBitmap(String path) {
        return bitmapCache.get(path);
    }

    public static void removeBitmap(String path) {
        if (bitmapCache.get(path) != null) {
            bitmapCache.get(path).recycle();
            bitmapCache.remove(path);
//            Log.w(TAG, "removeBitmap bitmapCache.size="+bitmapCache.size());
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
        // 이미지를 먼저 null로 하고 => try/catch로 처리함
        for (String key : thumbnailImageCache.keySet()) {
            ImageView imageView = thumbnailImageCache.get(key);
            if (imageView != null) {
                imageView.setImageResource(android.R.color.transparent);
            }
        }
        thumbnailImageCache.clear();

        ArrayList<String> recycleList = new ArrayList<>();
        for (String key : thumbnailCache.keySet()) {
            if (thumbnailCache.get(key) != null) {
                // 리소스(아이콘)용 썸네일이 아니면 삭제
                if (!resourceCache.containsValue(thumbnailCache.get(key))) {
                    thumbnailCache.get(key).recycle();
                    recycleList.add(key);
                }
            }
        }

        for (String key : recycleList) {
            thumbnailCache.remove(key);
        }
    }
}
