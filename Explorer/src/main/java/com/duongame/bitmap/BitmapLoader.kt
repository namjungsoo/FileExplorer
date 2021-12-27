package com.duongame.bitmap

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfRenderer
import android.media.ExifInterface
import android.media.ThumbnailUtils
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import com.duongame.adapter.ExplorerItem
import com.duongame.archive.ArchiveLoader
import com.duongame.file.FileHelper.isJpegImage
import net.lingala.zip4j.exception.ZipException
import timber.log.Timber
import java.io.*

/**
 * Created by namjungsoo on 2016. 11. 17..
 */
object BitmapLoader {
    const val TAG = "BitmapLoader"
    const val MICRO_KIND_SIZE = 96

    fun loadThumbnail(context: Context, type: Int, path: String): Bitmap? {
        var bitmap: Bitmap? = null
        when (type) {
            ExplorerItem.FILETYPE_APK -> bitmap = loadApkThumbnailDrawable(context, path)
            ExplorerItem.FILETYPE_PDF -> bitmap = loadPdfThumbnailBitmap(context, path)
            ExplorerItem.FILETYPE_IMAGE -> bitmap = loadImageThumbnailBitmap(context, path)
            ExplorerItem.FILETYPE_VIDEO -> bitmap = loadVideoThumbnailBitmap(context, path)
            ExplorerItem.FILETYPE_ZIP -> bitmap = loadZipThumbnailBitmap(context, path)
        }
        return bitmap
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        var bitmap: Bitmap
        if (drawable is BitmapDrawable) {
            val bitmapDrawable = drawable
            if (bitmapDrawable.bitmap != null) {
                return bitmapDrawable.bitmap
            }
        }
        bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(
                1,
                1,
                Bitmap.Config.ARGB_8888
            ) // Single color bitmap will be created of 1x1 pixel
        } else {
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun loadApkThumbnailDrawable(context: Context, path: String): Bitmap? {
        var bitmap = BitmapCacheManager.getThumbnail(path)
        if (bitmap != null) return bitmap
        val pm = context.packageManager
        val pi = pm.getPackageArchiveInfo(path!!, 0)
        if (pi != null) {
            pi.applicationInfo.sourceDir = path
            pi.applicationInfo.publicSourceDir = path
            val drawable = pi.applicationInfo.loadIcon(pm)
            if (drawable != null) {
                bitmap = drawableToBitmap(drawable)
                return bitmap
            }
        }
        return null
    }

    fun loadImageThumbnailBitmap(context: Context, path: String): Bitmap? {
        var bitmap = BitmapCacheManager.getThumbnail(path)
        if (bitmap != null) return bitmap

        // 시스템에서 찾은거
        bitmap = getThumbnail(context, path, true)
        if (bitmap != null) {
            BitmapCacheManager.setThumbnail(path, bitmap, null)
            return bitmap
        }

        // 직접 생성
        bitmap = decodeSquareThumbnailFromFile(path, MICRO_KIND_SIZE, true)
        if (bitmap != null) {
            BitmapCacheManager.setThumbnail(path, bitmap, null)
            return bitmap
        }
        return null
    }

    fun loadVideoThumbnailBitmap(context: Context?, path: String): Bitmap? {
        var bitmap = BitmapCacheManager.getThumbnail(path)
        if (bitmap != null) return bitmap

        // 시스템에서 찾은거
        bitmap =
            ThumbnailUtils.createVideoThumbnail(path!!, MediaStore.Images.Thumbnails.MICRO_KIND)
        if (bitmap != null) {
            BitmapCacheManager.setThumbnail(path, bitmap, null)
            return bitmap
        }

        //TODO: 직접 만드는 것도 넣어야 한다.
        return null
    }

    fun loadPdfThumbnailBitmap(context: Context?, path: String): Bitmap? {
        var bitmap = BitmapCacheManager.getThumbnail(path)
        if (bitmap != null) return bitmap

        // 직접 생성
        bitmap = decodeSquareThumbnailFromPdfFile(path, MICRO_KIND_SIZE)
        if (bitmap != null) {
            BitmapCacheManager.setThumbnail(path, bitmap, null)
            return bitmap
        }
        return null
    }

    fun getZipThumbnailFileName(context: Context?, path: String?): String? {
        // ZIP파일 안에 있는 이미지 파일을 찾자.
        var image: String? = null
        try {
            val loader = ArchiveLoader()
            val imageList = loader.load(context, path, null, 0, ExplorerItem.SIDE_LEFT, true)
            if (imageList != null && imageList.size > 0) {
                image = imageList[0].path
            }
        } catch (e: ZipException) {
            return null
        }
        return image
    }

    fun loadZipThumbnailBitmap(context: Context?, path: String): Bitmap? {
        var bitmap = BitmapCacheManager.getThumbnail(path)
        if (bitmap != null) return bitmap
        val image = getZipThumbnailFileName(context, path)

        // 못찾았을 경우에는 기본 ZIP 아이콘이 뜨게 한다. 이거는 외부에서 설정되어 있다.
        if (image == null) {
            return null
        } else {
            bitmap = decodeSquareThumbnailFromFile(image, MICRO_KIND_SIZE, false)
            if (bitmap != null) {
                BitmapCacheManager.setThumbnail(path, bitmap, null)
            }
        }
        return bitmap
    }

    fun writeDebugBitmap(path: String?, bitmap: Bitmap) {
        val file = File(path)
        var name = file.name
        name = name.replace(".zip", ".png")
        val extPath = Environment.getExternalStorageDirectory().absolutePath + "/" + name
        try {
            val os: OutputStream = FileOutputStream(extPath)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
            os.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getThumbnail(context: Context, path: String?, exifRotation: Boolean): Bitmap? {
        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.MediaColumns._ID),
            MediaStore.MediaColumns.DATA + "=?", arrayOf(path), null
        )
        if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
            cursor.close()
            //            cursor = context.getContentResolver().query(
//                    MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
//                    new String[]{MediaStore.Images.Thumbnails.DATA},
//                    MediaStore.Images.Thumbnails.IMAGE_ID + "=?",
//                    new String[]{String.valueOf(id)}, null);
//            if (cursor != null && cursor.moveToFirst()) {
//                String fullPath = cursor.getString(0);
//                cursor.close();
//                return BitmapFactory.decodeFile(fullPath);
//            }
            var bitmap = MediaStore.Images.Thumbnails.getThumbnail(
                context.contentResolver,
                id.toLong(),
                MediaStore.Images.Thumbnails.MICRO_KIND,
                null
            )
            if (exifRotation) {
                bitmap = rotateBitmapOnExif(bitmap, null, path)
            }
            return bitmap
        }
        cursor!!.close()
        return null
    }

    fun calculateInSampleSize(
        options: BitmapFactory.Options,
        screenWidth: Int,
        screenHeight: Int
    ): Int {
        // 실제 이미지의 사이즈
        var screenWidth = screenWidth
        var screenHeight = screenHeight
        val bmpWidth = options.outWidth
        val bmpHeight = options.outHeight
        var inSampleSize = 1

        //TODO: 추후 landscape 모드를 고려하자.
        // 원하는 이미지의 사이즈
        // QHD 이상일 경우 메모리 문제로 인해서 FHD로 다운해야 한다.
//        if(screenWidth > 1080) {
//            inSampleSize = 2;
//        }

//        Timber.e("calculateInSampleSize BEGIN screen=" + screenWidth + " " + screenHeight);
        var screenScaleFHD = 0.0f
        if (screenWidth > 1080) {
            screenScaleFHD = 1080.0f / screenWidth
            screenWidth = 1080
            screenHeight *= screenScaleFHD.toInt()
        }

//        Timber.e("calculateInSampleSize END screen=" + screenWidth + " " + screenHeight);

//        if(screenHeight > 1920) {
//            inSampleSize = 2;
//        }

        // 비트맵이 화면 크기보다 더 클경우에
        if (bmpHeight > screenHeight || bmpWidth > screenWidth) {
            val widthRatio = Math.round(bmpWidth.toFloat() / screenWidth.toFloat())
            val heightRatio = Math.round(bmpHeight.toFloat() / screenHeight.toFloat())

            // 둘다 1보다 크기 때문에 작은것으로 해야지 안깨진다.
            val ratioScale = if (heightRatio < widthRatio) heightRatio else widthRatio
            //            Timber.e("calculateInSampleSize widthRatio=" + screenWidth + " heightRatio=" + screenHeight + " ratioScale=" + ratioScale);

            // 답이 소숫점이 있을수 있다.
            // 1보다 큰것이 sampling해야 되는 것이다.
            // 큰값으로 샘플링하면 메모리가 많이 줄어든다.
            inSampleSize *= ratioScale
        }
        //        Timber.e(String.format("calculateInSampleSize bmpWidth=%d bmpHeight=%d screenWidth=%d screenHeight=%d inSampleSize=%d", bmpWidth, bmpHeight, screenWidth, screenHeight, inSampleSize));
        return inSampleSize
    }

    //TODO: 비트맵 메모리 복사가 일어나고 있음
    fun rotateBitmapOnExif(
        bitmap: Bitmap?,
        options: BitmapFactory.Options?,
        path: String?
    ): Bitmap? {
        var bitmap = bitmap
        return if (!isJpegImage(path!!)) bitmap else try {
            var degree = 0
            val exif = ExifInterface(path)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
            if (bitmap != null && degree != 0) {
                val m = Matrix()
                m.setRotate(
                    degree.toFloat(), bitmap.width.toFloat() * 0.5f, bitmap.height
                        .toFloat() * 0.5f
                )
                val rotated =
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, false)
                if (rotated != null) {
                    bitmap.recycle()
                    Timber.e("rotateBitmapOnExif recycle")
                    bitmap = rotated
                }
            }
            bitmap
        } catch (e: IOException) {
            e.printStackTrace()
            bitmap
        }
    }

    fun decodeBounds(path: String?): BitmapFactory.Options {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)
        return options
    }

    fun sampleDecodeBounds(path: String?, reqWidth: Int, reqHeight: Int): BitmapFactory.Options {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)
        Timber.e(String.format("sampleDecodeBounds reqWidth=%d reqHeight=%d", reqWidth, reqHeight))
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        //FIX: 565로 메모리를 아낌
        options.inPreferredConfig = Bitmap.Config.RGB_565

        // 로드하기 위해서는 위에서 true 로 설정했던 inJustDecodeBounds 의 값을 false 로 설정합니다.
        options.inJustDecodeBounds = false
        return options
    }

    fun decodeSampleBitmapFromFile(
        path: String?,
        reqWidth: Int,
        reqHeight: Int,
        exifRotation: Boolean
    ): Bitmap? {
        val options = sampleDecodeBounds(path, reqWidth, reqHeight)
        var tryCount = 0
        var bitmap: Bitmap? = null
        while (true) {
            try {
                bitmap = BitmapFactory.decodeFile(path, options)
                if (exifRotation) {
                    bitmap = rotateBitmapOnExif(bitmap, options, path)
                }
                return bitmap
            } catch (e: OutOfMemoryError) {
                options.inSampleSize++
                tryCount++
                if (tryCount == 3) return null
            }
        }
    }

    private fun cropSquareBitmap(decoder: Bitmap?, ratio: Float): Bitmap {
        val bitmap: Bitmap
        if (ratio > 1) {
            val top = decoder!!.height - decoder.width shr 1
            bitmap = Bitmap.createBitmap(decoder, 0, top, decoder.width, decoder.width)
            decoder.recycle()
            Timber.e("cropSquareBitmap recycle")
        } else {
            val left = decoder!!.width - decoder.height shr 1
            bitmap = Bitmap.createBitmap(decoder, left, 0, decoder.height, decoder.height)
            decoder.recycle()
            Timber.e("cropSquareBitmap recycle")
        }
        return bitmap
    }

    private fun cropSquareBitmap(
        path: String?,
        ratio: Float,
        options: BitmapFactory.Options
    ): Bitmap {
        val decoder = BitmapFactory.decodeFile(path, options)
        return cropSquareBitmap(decoder, ratio)
    }

    @Throws(IOException::class)
    private fun cropBitmapUsingDecoder(
        path: String,
        ratio: Float,
        options: BitmapFactory.Options
    ): Bitmap {
        val bitmap: Bitmap
        val decoder = BitmapRegionDecoder.newInstance(path, false)
        if (ratio > 1) { // 어떤 크기가 들어오더라도 가운데 크롭을 하기 위해서이다.
            val top = decoder.height - decoder.width shr 1
            bitmap = decoder.decodeRegion(Rect(0, top, decoder.width, top + decoder.width), options)
            decoder.recycle()
            Timber.e("cropBitmapUsingDecoder recycle")
        } else {
            val left = decoder.width - decoder.height shr 1
            bitmap =
                decoder.decodeRegion(Rect(left, 0, left + decoder.height, decoder.height), options)
            decoder.recycle()
            Timber.e("cropBitmapUsingDecoder recycle")
        }
        return bitmap
    }

    fun decodeSquareThumbnailFromPdfFile(path: String?, size: Int): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val parcel = ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val renderer = PdfRenderer(parcel)
                val page = renderer.openPage(0)

                // 종횡비를 계산하자.
                val ratio = page.height.toFloat() / page.width.toFloat()
                val rectClip = Rect()
                bitmap = if (ratio > 1) { // 세로
                    val newSize = (size * ratio).toInt()
                    Bitmap.createBitmap(size, newSize, Bitmap.Config.ARGB_8888)
                } else { // 가로
                    val newSize = (size / ratio).toInt()
                    Bitmap.createBitmap(newSize, size, Bitmap.Config.ARGB_8888)
                }

                // 이미지를 렌더링후 close한다.
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                renderer.close()
                bitmap = cropSquareBitmap(bitmap, ratio)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

    fun decodeSquareThumbnailFromFile(path: String?, size: Int, exifRotation: Boolean): Bitmap? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

        // 이미지의 크기만 읽는다.
        BitmapFactory.decodeFile(path, options)

        // size는 일반적으로 96이다.
        var width = size
        var height = size

        // 종횡비 계산해야 해서 sample size를 결정한다.
        val ratio = options.outHeight.toFloat() / options.outWidth.toFloat()
        if (ratio > 1) {
            height = (size * ratio).toInt()
        } else {
            width = (size / ratio).toInt()
        }
        options.inSampleSize = calculateInSampleSize(options, width, height)

        // 로드하기 위해서는 위에서 true 로 설정했던 inJustDecodeBounds 의 값을 false 로 설정합니다.
        options.inJustDecodeBounds = false
        return try {
            //Bitmap bitmap = cropBitmapUsingDecoder(path, ratio, options);

            // 이미지를 로딩하고서 크롭한다.
            var bitmap: Bitmap? = cropSquareBitmap(path, ratio, options)
            if (exifRotation) {
                bitmap = rotateBitmapOnExif(bitmap, options, path)
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun splitBitmapSide(
        item: ExplorerItem,
        screenWidth: Int,
        screenHeight: Int,
        exif: Boolean
    ): SplittedBitmap? {
        // 이미 캐시된 페이지가 있으면
        val key: String
        val keyOther: String
        val itemOther = item.clone() as ExplorerItem
        itemOther.side =
            if (item.side == ExplorerItem.SIDE_LEFT) ExplorerItem.SIDE_RIGHT else ExplorerItem.SIDE_LEFT
        key = BitmapCacheManager.changePathToPage(item)
        keyOther = BitmapCacheManager.changePathToPage(itemOther)
        var page = BitmapCacheManager.getPage(key)
        if (page != null) {
            val sb = SplittedBitmap()
            sb.page = page
            sb.key = key
            sb.keyOther = keyOther
            sb.pageOther = null
            return sb
        }
        var pageOther: Bitmap? = null
        var decoder: BitmapRegionDecoder? = null
        val options = sampleDecodeBounds(item.path, screenWidth, screenHeight)
        try {
            var tryCount = 0
            when (item.side) {
                ExplorerItem.SIDE_LEFT -> while (true) {
                    try {
                        decoder = BitmapRegionDecoder.newInstance(item.path, false)
                        if (decoder != null) {
                            val rect = Rect(0, 0, decoder.width shr 1, decoder.height)
                            page = decoder.decodeRegion(rect, options)
                            val rectOther =
                                Rect(decoder.width shr 1, 0, decoder.width, decoder.height)
                            pageOther = decoder.decodeRegion(rectOther, options)
                            decoder.recycle()
                            Timber.e("splitBitmapSide decoder.recycle")
                            break
                        }
                    } catch (e: OutOfMemoryError) {
                        if (decoder != null) {
                            decoder.recycle()
                            Timber.e("splitBitmapSide OutOfMemoryError recycle")
                        }
                        options.inSampleSize++
                        tryCount++
                        if (tryCount == 3) return null
                    }
                }
                ExplorerItem.SIDE_RIGHT -> while (true) {
                    try {
                        decoder = BitmapRegionDecoder.newInstance(item.path, false)
                        if (decoder != null) {
                            val rect = Rect(decoder.width shr 1, 0, decoder.width, decoder.height)
                            page = decoder.decodeRegion(rect, options)
                            val rectOther = Rect(0, 0, decoder.width shr 1, decoder.height)
                            pageOther = decoder.decodeRegion(rectOther, options)
                            decoder.recycle()
                            Timber.e("splitBitmapSide decoder.recycle")
                            break
                        }
                    } catch (e: OutOfMemoryError) {
                        if (decoder != null) {
                            decoder.recycle()
                            Timber.e("splitBitmapSide OutOfMemoryError recycle")
                        }
                        options.inSampleSize++
                        tryCount++
                        if (tryCount == 3) return null
                    }
                }
                else -> {}
            }
        } catch (e: Exception) {
            if (decoder != null) {
                decoder.recycle()
                Timber.e("splitBitmapSide Exception recycle")
            }
            return null
        }
        if (page != null && pageOther != null) {
            val sb = SplittedBitmap()
            sb.key = key
            sb.keyOther = keyOther
            sb.page = page
            sb.pageOther = pageOther
            return sb
        }
        return null
    }

    //HACK: 이것을 사용하지 말것 -> GIF 일때는 이것을 사용하여야 한다.
    // 왼쪽 오른쪽을 자른 비트맵을 리턴한다
    fun splitBitmapSide(bitmap: Bitmap, item: ExplorerItem): SplittedBitmap? {
        // 이미 캐시된 페이지가 있으면
        val key: String
        val keyOther: String
        val itemOther = item.clone() as ExplorerItem
        itemOther.side =
            if (item.side == ExplorerItem.SIDE_LEFT) ExplorerItem.SIDE_RIGHT else ExplorerItem.SIDE_LEFT
        key = BitmapCacheManager.changePathToPage(item)
        keyOther = BitmapCacheManager.changePathToPage(itemOther)
        var page = BitmapCacheManager.getPage(key)
        if (page != null) {
            val sb = SplittedBitmap()
            sb.page = page
            sb.key = key
            sb.keyOther = keyOther
            sb.pageOther = null
            return sb
        }
        var pageOther: Bitmap? = null
        when (item.side) {
            ExplorerItem.SIDE_LEFT -> {
                page = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width shr 1, bitmap.height)
                pageOther = Bitmap.createBitmap(
                    bitmap,
                    bitmap.width shr 1,
                    0,
                    bitmap.width shr 1,
                    bitmap.height
                )
            }
            ExplorerItem.SIDE_RIGHT -> {
                page = Bitmap.createBitmap(
                    bitmap,
                    bitmap.width shr 1,
                    0,
                    bitmap.width shr 1,
                    bitmap.height
                )
                pageOther = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width shr 1, bitmap.height)
            }
        }
        if (page != null && pageOther != null) {
            val sb = SplittedBitmap()
            sb.key = key
            sb.keyOther = keyOther
            sb.page = page
            sb.pageOther = pageOther
            return sb
        }
        return null

        // 잘리는 비트맵은 더이상 사용하지 않으므로 삭제한다.
        // 이거 때문에 recycled 에러가 발생한다.
        // remove를 하지 않으면 oom이 발생한다.

        // 캐쉬에 포함되지 않는 이미지이다.
    }

    class SplittedBitmap {
        var key: String? = null
        var keyOther: String? = null
        var page: Bitmap? = null
        var pageOther: Bitmap? = null// if cached, null

        // non split
        // nullable
        var path: String? = null
        var bitmap: Bitmap? = null
    }
}