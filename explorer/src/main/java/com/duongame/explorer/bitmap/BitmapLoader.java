package com.duongame.explorer.bitmap;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.helper.FileHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static android.graphics.Bitmap.createBitmap;
import static com.duongame.explorer.adapter.ExplorerItem.Side.LEFT;
import static com.duongame.explorer.adapter.ExplorerItem.Side.RIGHT;

/**
 * Created by namjungsoo on 2016. 11. 17..
 */

public class BitmapLoader {
    public static final String TAG = "BitmapLoader";

    public static void writeDebugBitmap(String path, Bitmap bitmap) {
        File file = new File(path);
        String name = file.getName();
        name = name.replace(".zip", ".png");
        String extPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + name;
        try {
            OutputStream os = new FileOutputStream(extPath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap getThumbnail(Activity context, String path, boolean exifRotation) {
//        Log.d("BitmapLoader", "getThumbnail path="+path);
        final Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.MediaColumns._ID},
                MediaStore.MediaColumns.DATA + "=?",
                new String[]{path}, null);
        if (cursor != null && cursor.moveToFirst()) {
            final int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));

            cursor.close();
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
            Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), id, MediaStore.Images.Thumbnails.MICRO_KIND, null);
            if (exifRotation) {
                bitmap = rotateBitmapOnExif(bitmap, null, path);
            }
            return bitmap;
        }
        cursor.close();
        return null;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        // 실제 크기가 더 클경우에
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public static Bitmap rotateBitmapOnExif(Bitmap bitmap, BitmapFactory.Options options, String path) {
        if (!FileHelper.isJpegImage(path))
            return bitmap;

        try {
            int degree = 0;
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }

            if (bitmap != null && degree != 0) {
                Matrix m = new Matrix();
                m.setRotate(degree, (float) bitmap.getWidth() * 0.5f, (float) bitmap.getHeight() * 0.5f);
                Bitmap rotated = createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, false);
                if (rotated != null) {
                    bitmap.recycle();
                    bitmap = rotated;
                }
            }
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return bitmap;
        }
    }

    public static BitmapFactory.Options decodeBounds(String path) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        return options;
    }

    public static Bitmap decodeSampleBitmapFromFile(String path, int reqWidth, int reqHeight, boolean exifRotation) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
//        Log.d(TAG, "inSampleSize=" + options.inSampleSize);

        // 로드하기 위해서는 위에서 true 로 설정했던 inJustDecodeBounds 의 값을 false 로 설정합니다.
        options.inJustDecodeBounds = false;
        //options.inDither = true;
        //options.inPreferQualityOverSped = true;

        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        if (exifRotation) {
            bitmap = rotateBitmapOnExif(bitmap, options, path);
        }
        return bitmap;
    }

    private static Bitmap cropBitmap(String path, float ratio, BitmapFactory.Options options) {
        Bitmap decoder = BitmapFactory.decodeFile(path, options);
        Bitmap bitmap;
        if (ratio > 1) {
            int top = (decoder.getHeight() - decoder.getWidth()) >> 1;
            bitmap = Bitmap.createBitmap(decoder, 0, top, decoder.getWidth(), decoder.getWidth());
            decoder.recycle();
        } else {
            int left = (decoder.getWidth() - decoder.getHeight()) >> 1;
            bitmap = Bitmap.createBitmap(decoder, left, 0, decoder.getHeight(), decoder.getHeight());
            decoder.recycle();
        }
        return bitmap;
    }

    private static Bitmap cropBitmapUsingDecoder(String path, float ratio, BitmapFactory.Options options) throws IOException {
        Bitmap bitmap;
        final BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(path, false);
//            Log.d("tag", "decoder path=" + path + " width="+width + " height="+height);
        if (ratio > 1) {// 어떤 크기가 들어오더라도 가운데 크롭을 하기 위해서이다.
            int top = (decoder.getHeight() - decoder.getWidth()) >> 1;
            bitmap = decoder.decodeRegion(new Rect(0, top, decoder.getWidth(), top + decoder.getWidth()), options);
            decoder.recycle();
        } else {
            int left = (decoder.getWidth() - decoder.getHeight()) >> 1;
            bitmap = decoder.decodeRegion(new Rect(left, 0, left + decoder.getHeight(), decoder.getHeight()), options);
            decoder.recycle();
        }
        return bitmap;
    }

    public static Bitmap decodeSquareThumbnailFromFile(String path, int size, boolean exifRotation) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        int width = size;
        int height = size;

        // 종횡비 계산해야 함
        float ratio = (float) options.outHeight / (float) options.outWidth;
        if (ratio > 1) {
            height = (int) (size * ratio);
        } else {
            width = (int) (size / ratio);
        }

        options.inSampleSize = calculateInSampleSize(options, width, height);
//        Log.d("tag", "original path=" +path + " width="+options.outWidth + " height="+options.outHeight + " sample="+options.inSampleSize);
//        Log.d("tag", "thumb path=" +path + " width="+width + " height="+height);

        // 로드하기 위해서는 위에서 true 로 설정했던 inJustDecodeBounds 의 값을 false 로 설정합니다.
        options.inJustDecodeBounds = false;
        try {
            //Bitmap bitmap = cropBitmapUsingDecoder(path, ratio, options);
            Bitmap bitmap = cropBitmap(path, ratio, options);

            if (exifRotation) {
                bitmap = rotateBitmapOnExif(bitmap, options, path);
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 현재 사용안함
//    public static Bitmap decodeSampleBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
//        final BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeResource(res, resId, options);
//
//        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
//
//        // 로드하기 위해서는 위에서 true 로 설정했던 inJustDecodeBounds 의 값을 false 로 설정합니다.
//        options.inJustDecodeBounds = false;
//        return BitmapFactory.decodeResource(res, resId, options);
//    }

    // 왼쪽 오른쪽을 자른 비트맵을 리턴한다
    public static Bitmap splitBitmapSide(Bitmap bitmap, ExplorerItem item) {
//        Log.d(TAG, "splitBitmapSide=" + item.name);

        // 이미 캐시된 페이지가 있으면
        final String key;
        final String keyOther;
        final ExplorerItem itemOther = (ExplorerItem) item.clone();
        itemOther.side = item.side == LEFT ? RIGHT : LEFT;
        key = BitmapCache.changePathToPage(item);
        keyOther = BitmapCache.changePathToPage(itemOther);

        Bitmap page = BitmapCache.getPage(key);
        if (page != null) {
            Log.d(TAG, "splitBitmapSide getPage=" + item.name);
            return page;
        }

        Bitmap pageOther = null;
        switch (item.side) {
            case LEFT:
                page = createBitmap(bitmap, 0, 0, bitmap.getWidth() >> 1, bitmap.getHeight());
//                Log.d(TAG, "splitBitmapSide LEFT current_page=" + item.name);
                pageOther = createBitmap(bitmap, bitmap.getWidth() >> 1, 0, bitmap.getWidth() >> 1, bitmap.getHeight());
//                Log.d(TAG, "splitBitmapSide LEFT pageOther=" + itemOther.name);
                break;
            case RIGHT:
                page = createBitmap(bitmap, bitmap.getWidth() >> 1, 0, bitmap.getWidth() >> 1, bitmap.getHeight());
//                Log.d(TAG, "splitBitmapSide RIGHT current_page=" + item.name);
                pageOther = createBitmap(bitmap, 0, 0, bitmap.getWidth() >> 1, bitmap.getHeight());
//                Log.d(TAG, "splitBitmapSide RIGHT pageOther=" + itemOther.name);
                break;
        }

        if (page != null && pageOther != null) {
            BitmapCache.setPage(key, page);
            BitmapCache.setPage(keyOther, pageOther);
        } else {
            Log.e(TAG, "splitBitmapSide current_page or pageOther is null");
        }

        // 잘리는 비트맵은 더이상 사용하지 않으므로 삭제한다.
        // 이거 때문에 recycled 에러가 발생한다.
        // remove를 하지 않으면 oom이 발생한다.
        BitmapCache.removeBitmap(item.path);
//        Log.d(TAG, "splitBitmapSide removeBitmap=" + item.name);

        return page;
    }
}
