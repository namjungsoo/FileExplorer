package com.duongame.bitmap

import android.graphics.Bitmap
import android.widget.ImageView
import com.duongame.adapter.ExplorerItem
import com.duongame.file.FileHelper.getFileFolderIconResId
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by namjungsoo on 2016-11-16.
 */
object BitmapCacheManager {
    private val thumbnailCache: MutableMap<String, ArrayList<BitmapCache>> =
        ConcurrentHashMap() // 썸네일
    private val bitmapCache: MutableMap<String, BitmapCache> = ConcurrentHashMap() // 일반 이미지
    private val pageCache: MutableMap<String, BitmapCache> = ConcurrentHashMap() // zip파일 잘린 이미지

    // path와 side정보를 string으로 변환
    fun changePathToPage(item: ExplorerItem): String {
        val path: String = if (item.side == ExplorerItem.SIDE_LEFT) {
            item.path + ".left"
        } else if (item.side == ExplorerItem.SIDE_RIGHT) {
            item.path + ".right"
        } else {
            item.path
        }
        return path
    }

    // current_page
    fun setPage(key: String?, bitmap: Bitmap?, imageView: ImageView?) {
        if (key == null) return
        var cache = pageCache[key]
        val imageViewHash = imageView?.hashCode() ?: 0
        val bitmapHash = bitmap?.hashCode() ?: 0
        if (cache == null) {
            Timber.e("setPage cache == null $key $bitmapHash $imageViewHash")
            cache = BitmapCache()
            cache.bitmap = bitmap
            cache.imageViewRef = WeakReference(imageView)
            pageCache[key] = cache
        } else {
            Timber.e("setPage cache != null $key $bitmapHash $imageViewHash")
            if (cache.bitmap != null && cache.bitmap != bitmap) {
                Timber.e("setPage removeBitmapCache cache.bitmap changed $key $bitmapHash $imageViewHash")
                removeBitmapCache(cache, 0)
            }
            // bitmap은 정리가 끝났으므로 바로 대입
            cache.bitmap = bitmap

            // imageview는 무조건 대입
            if (imageView != null) {
                cache.imageViewRef = WeakReference(imageView)
            } else {
                cache.imageViewRef = null
            }
        }
    }

    fun getPage(key: String): Bitmap? {
        val cache = pageCache[key] ?: return null
        return cache.bitmap
    }

    private fun removeBitmapCache(cache: BitmapCache, resId: Int) {
        if (cache.bitmap == null) {
            Timber.e("removeBitmapCache bitmap is null")
            return
        }
        if (cache.bitmap!!.isRecycled) {
            Timber.e("removeBitmapCache is already recycled")
            return
        }
        if (cache.imageViewRef == null) {
            Timber.e("removeBitmapCache imageViewRef is null")
            return
        }
        val imageView = cache.imageViewRef!!.get()
        if (imageView == null) {
            Timber.e("removeBitmapCache imageView is null")
            return
        }
        if (resId > 0) {
            imageView.setImageResource(resId)
        } else {
            imageView.setImageBitmap(null)
        }
        Timber.e("removeBitmapCache imageView set bitmap null")
    }

    fun removePage(key: String) {
        val cache = pageCache[key] ?: return
        Timber.e("removePage removeBitmapCache")
        removeBitmapCache(cache, 0)
        pageCache.remove(key)
    }

    fun removeAllPages() {
        Timber.e("removeAllPages")
        for (key in pageCache.keys) {
            val cache = pageCache[key] ?: continue
            Timber.e("removeAllPages removeBitmapCache")
            removeBitmapCache(cache, 0)
        }
        pageCache.clear()
    }

    // 비트맵은 SIDE_ALL인 경우 1:1 맵핑이 됨
    // image bitmap
    fun setBitmap(path: String?, bitmap: Bitmap, imageView: ImageView?) {
        if (path == null) return
        var cache = bitmapCache[path]
        if (cache == null) {
            cache = BitmapCache()
            cache.bitmap = bitmap
            cache.imageViewRef = WeakReference(imageView)
            bitmapCache[path] = cache
        } else {
            if (cache.bitmap != null && cache.bitmap != bitmap) {
                Timber.e("setBitmap removeBitmapCache")
                removeBitmapCache(cache, 0)
            }
            // bitmap은 정리가 끝났으므로 바로 대입
            cache.bitmap = bitmap

            // imageview는 무조건 대입
            if (imageView != null) {
                cache.imageViewRef = WeakReference(imageView)
            } else {
                cache.imageViewRef = null
            }
        }
    }

    fun getBitmap(path: String): Bitmap? {
        val cache = bitmapCache[path] ?: return null
        return cache.bitmap
    }

    fun removeBitmap(path: String) {
        val cache = bitmapCache[path] ?: return
        Timber.e("removeBitmap removeBitmapCache")
        removeBitmapCache(cache, 0)
        bitmapCache.remove(path)
    }

    fun removeAllBitmaps() {
        Timber.e("removeAllBitmaps")
        for (key in bitmapCache.keys) {
            val cache = bitmapCache[key] ?: continue
            Timber.e("removeAllBitmaps removeBitmapCache")
            removeBitmapCache(cache, 0)
        }
        bitmapCache.clear()
    }

    //TODO: 하나의 썸네일이 여러곳에서 사용될수 있으니 해당 부분에 대해서 조치를 해야함
    fun setThumbnail(path: String?, bitmap: Bitmap, imageView: ImageView?) {
        if (path == null) {
            Timber.e("setThumbnail path is null")
            return
        }
        Timber.e("setThumbnail $path")
        var cacheList = thumbnailCache[path]
        if (cacheList == null) {
            cacheList = ArrayList()

            // imageview가 null이라도 무조건 대입
            val cache = BitmapCache()
            cache.bitmap = bitmap
            cache.imageViewRef = WeakReference(imageView)
            cacheList.add(cache)
            thumbnailCache[path] = cacheList
            Timber.e("setThumbnail $path new")
        } else {
            for (cache in cacheList) {
                // 이미 있으면 리턴
                if (cache.bitmap == bitmap && cache.imageViewRef!!.get() === imageView) {
                    Timber.e("setThumbnail $path reject")
                    return
                }

                // 캐쉬와 다르면 거절
                if (cache.bitmap != bitmap) {
                    Timber.e("setThumbnail $path reject")
                    return
                }
            }

            // 같은 bitmap, 다른 imageView를 사용하는 것이다.
            // 한번에 모두 초기화 해주어야 한다.
            val newCache = BitmapCache()
            newCache.bitmap = cacheList[0].bitmap
            newCache.imageViewRef = WeakReference(imageView)
            cacheList.add(newCache)
            Timber.e("setThumbnail $path add")
        }
    }

    fun getThumbnail(path: String): Bitmap? {
        Timber.e("getThumbnail $path")
        val cacheList = thumbnailCache[path] ?: return null
        if (cacheList.size == 0) {
            return null
        }
        Timber.e("getThumbnail $path OK")
        return cacheList[0].bitmap
    }

    fun getThumbnailCount(): Int {
        return thumbnailCache.size
    }

    fun removeAllThumbnails() {
        for (key in thumbnailCache.keys) {
            val cacheList = thumbnailCache[key] ?: continue
            Timber.e("removeAllThumbnails key=" + key + " size=" + cacheList.size)

            // 모든걸 다 지운다.
            for (cache in cacheList) {
                Timber.e("removeAllThumbnails key=$key remove")
                val resId = getFileFolderIconResId(key)
                removeBitmapCache(cache, resId)
            }
        }
        thumbnailCache.clear()
    }

    private class BitmapCache {
        var bitmap: Bitmap? = null
        var imageViewRef: WeakReference<ImageView?>? = null
    }
}