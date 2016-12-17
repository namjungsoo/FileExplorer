package com.duongame.explorer.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.duongame.explorer.adapter.ExplorerFileItem;
import com.duongame.explorer.bitmap.BitmapCacheManager;
import com.duongame.explorer.bitmap.BitmapLoader;
import com.duongame.explorer.bitmap.PageKey;

/**
 * Created by namjungsoo on 2016-12-17.
 */

public class BitmapTask extends AsyncTask<ExplorerFileItem, Void, Bitmap> {
    private static final int RETRY_INTERVAL_MS = 500;
    private static final int RETRY_COUNT = 5;

    private int width, height;
    private boolean exif;

    // width, height는 화면(컨테이너)의 크기이다.
    public BitmapTask(int width, int height, boolean exif) {
        this.width = width;
        this.height = height;
        this.exif = exif;
    }

    @Override
    protected Bitmap doInBackground(ExplorerFileItem... params) {
        return null;
    }

    protected Bitmap loadBitmap(ExplorerFileItem item) {
        // 캐시에 있는지 확인해 보고
        // split일 경우에는 무조건 없다
        Bitmap bitmap = null;
        if(item.side != ExplorerFileItem.Side.SIDE_ALL) {
            bitmap = BitmapCacheManager.getPage(new PageKey(item.path, item.side));
            if(bitmap != null)
                return bitmap;
        }

        bitmap = BitmapCacheManager.getBitmap(item.path);
        if (bitmap == null) {
            BitmapFactory.Options options = BitmapLoader.decodeBounds(item.path);

            // 자르는 경우에는 실제 예상보다 width/2를 하자
            if(item.side != ExplorerFileItem.Side.SIDE_ALL) {
                options.outWidth >>= 1;
            }
            float bitmapRatio = (float) options.outHeight / (float) options.outWidth;
            float screenRatio = (float) height / (float) width;

            if (screenRatio > bitmapRatio) {
                height = (int) (width * bitmapRatio);
            } else {
                height = (int) (width * screenRatio);
            }

            // 파일에서 읽어서 있으면 캐시에 넣는다
            int count = 0;
            while (true) {
                bitmap = BitmapLoader.decodeSampleBitmapFromFile(item.path, width, height, exif);
                if (bitmap == null) {
                    try {
                        count += RETRY_INTERVAL_MS;
                        if (count == RETRY_INTERVAL_MS * RETRY_COUNT)
                            break;
                        Thread.sleep(RETRY_INTERVAL_MS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.w("BitmapTask", "decode retry=" + count);
                } else {
                    BitmapCacheManager.setBitmap(item.path, bitmap);
                    break;
                }
            }
        }

        // 비트맵을 로딩했으면 이제 자르자
        bitmap = BitmapLoader.splitBitmapSide(bitmap, item);
        return bitmap;
    }
}
