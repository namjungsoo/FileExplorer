package com.duongame.task.bitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.duongame.adapter.ExplorerItem;
import com.duongame.bitmap.BitmapCacheManager;
import com.duongame.bitmap.BitmapLoader;
import com.duongame.file.FileHelper;

/**
 * Created by namjungsoo on 2016-12-17.
 */

public class BitmapTask extends AsyncTask<ExplorerItem, Void, Bitmap> {
    private static final String TAG = BitmapTask.class.getSimpleName();
    private static final int RETRY_INTERVAL_MS = 500;
    private static final int RETRY_COUNT = 5;

    private int screenWidth, screenHeight;
    private boolean exif;
    private int count;

    // screenWidth, height는 화면(컨테이너)의 크기이다.
    public BitmapTask(int width, int screenHeight, boolean exif) {
        this.screenWidth = width;
        this.screenHeight = screenHeight;
        this.exif = exif;
    }

    @Override
    protected Bitmap doInBackground(ExplorerItem... params) {
        return null;
    }

    protected Bitmap loadBitmap(ExplorerItem item) {
        // 캐시에 있는지 확인해 보고
        // split일 경우에는 무조건 없다
        Bitmap bitmap = null;
        if (item.side != ExplorerItem.SIDE_ALL) {
            final String page = BitmapCacheManager.changePathToPage(item);
            bitmap = BitmapCacheManager.getPage(page);

            if (bitmap != null) {
                return bitmap;
            }
        }

        bitmap = BitmapCacheManager.getBitmap(item.path);
        if (bitmap == null) {
            final BitmapFactory.Options options = BitmapLoader.decodeBounds(item.path);

            // 자르는 경우에는 실제 예상보다 screenWidth/2를 하자
            if (item.side != ExplorerItem.SIDE_ALL) {
                options.outWidth >>= 1;
            }
            item.width = options.outWidth;
            item.height = options.outHeight;

            float bitmapRatio = (float) options.outHeight / (float) options.outWidth;
            float screenRatio = (float) screenHeight / (float) screenWidth;

            if (screenRatio > bitmapRatio) {
                screenHeight = (int) (screenWidth * bitmapRatio);
            } else {
                screenHeight = (int) (screenWidth * screenRatio);
            }

            // 파일에서 읽어서 있으면 캐시에 넣는다
            count = 0;
            while (true) {
                //NEW-2
                if (item.side == ExplorerItem.SIDE_ALL) {
                    bitmap = BitmapLoader.decodeSampleBitmapFromFile(item.path, screenWidth, screenHeight, exif);
                    if (bitmap == null) {
                        // 다른 비트맵이 기다려지길 기다렸다가 다시 시도하자.
                        // 왜냐면 압축을 푸는 중인 파일도 있기 때문이다.
                        if (isFinishedWaitingImageExtracting())
                            break;
                    } else {
                        BitmapCacheManager.setBitmap(item.path, bitmap, null);
                        break;
                    }
                } else {
                    // RegionDecoder가 지원되는 경우는 PNG, JPG
                    if (FileHelper.isJpegImage(item.path) || FileHelper.isPngImage(item.path)) {
                        bitmap = BitmapLoader.splitBitmapSide(item, screenWidth, screenHeight, exif);
                        if (bitmap == null) {
                            // 다른 비트맵이 기다려지길 기다렸다가 다시 시도하자.
                            // 왜냐면 압축을 푸는 중인 파일도 있기 때문이다.
                            if (isFinishedWaitingImageExtracting())
                                break;
                        } else {
                            break;
                        }
                    } else {
                        // GIF는 RegionDecoder가 지원이 되지 않는다.
                        bitmap = BitmapLoader.decodeSampleBitmapFromFile(item.path, screenWidth, screenHeight, exif);
                        if (bitmap == null) {
                            // 다른 비트맵이 기다려지길 기다렸다가 다시 시도하자.
                            // 왜냐면 압축을 푸는 중인 파일도 있기 때문이다.
                            if (isFinishedWaitingImageExtracting())
                                break;
                        } else {
                            bitmap = BitmapLoader.splitBitmapSide(bitmap, item);
                            break;
                        }
                    }
                }
            }
        }

        return bitmap;
    }

    private boolean isFinishedWaitingImageExtracting() {
        try {
            // 최대 시간을 기다렸다면 멈추고 종료 한다.
            count += RETRY_INTERVAL_MS;
            if (count == RETRY_INTERVAL_MS * RETRY_COUNT)
                return true;
            Thread.sleep(RETRY_INTERVAL_MS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
}
