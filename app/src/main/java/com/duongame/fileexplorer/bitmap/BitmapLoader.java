package com.duongame.fileexplorer.bitmap;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;

import java.io.IOException;

/**
 * Created by namjungsoo on 2016. 11. 17..
 */

public class BitmapLoader {
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

    public static BitmapFactory.Options decodeBounds(String path) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        return options;
    }

    public static Bitmap decodeSampleBitmapFromFile(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // 로드하기 위해서는 위에서 true 로 설정했던 inJustDecodeBounds 의 값을 false 로 설정합니다.
        options.inJustDecodeBounds = false;
        //options.inDither = true;
        //options.inPreferQualityOverSpeed = true;
        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap decodeSquareThumbnailFromFile(String path, int size) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        int width = size;
        int height = size;

        // 종횡비 계산해야 함
        float ratio = (float)options.outHeight / (float)options.outWidth;
        if (ratio > 1) {
            height = (int)(size * ratio);
        } else {
            width = (int)(size * ratio);
        }
        options.inSampleSize = calculateInSampleSize(options, width, height);

        // 로드하기 위해서는 위에서 true 로 설정했던 inJustDecodeBounds 의 값을 false 로 설정합니다.
        options.inJustDecodeBounds = false;
        try {
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(path, false);
            if(ratio > 1) {
                int top = (decoder.getHeight() - decoder.getWidth()) >> 1;
                Bitmap bitmap = decoder.decodeRegion(new Rect(0, top, decoder.getWidth(), top + decoder.getWidth()), options);
                decoder.recycle();
                return bitmap;
            } else {
                int left = (decoder.getWidth() - decoder.getHeight()) >> 1;
                Bitmap bitmap = decoder.decodeRegion(new Rect(left, 0, left + decoder.getHeight(), decoder.getHeight()), options);
                decoder.recycle();
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap decodeSampleBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // 로드하기 위해서는 위에서 true 로 설정했던 inJustDecodeBounds 의 값을 false 로 설정합니다.
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }
}
