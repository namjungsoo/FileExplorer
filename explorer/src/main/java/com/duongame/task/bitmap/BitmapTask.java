package com.duongame.task.bitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.duongame.adapter.ExplorerItem;
import com.duongame.bitmap.BitmapCacheManager;
import com.duongame.bitmap.BitmapLoader;
import com.duongame.helper.FileHelper;

/**
 * Created by namjungsoo on 2016-12-17.
 */

public class BitmapTask extends AsyncTask<ExplorerItem, Void, Bitmap> {
    private static final String TAG = BitmapTask.class.getSimpleName();
    private static final int RETRY_INTERVAL_MS = 500;
    private static final int RETRY_COUNT = 5;

    private int width, height;
    private boolean exif;
    private int count;

    // width, height는 화면(컨테이너)의 크기이다.
    public BitmapTask(int width, int height, boolean exif) {
        this.width = width;
        this.height = height;
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
        if (item.side != ExplorerItem.Side.SIDE_ALL) {
            final String page = BitmapCacheManager.changePathToPage(item);
            bitmap = BitmapCacheManager.getPage(page);

            if (bitmap != null) {
                return bitmap;
            }
        }

        bitmap = BitmapCacheManager.getBitmap(item.path);
        if (bitmap == null) {
            final BitmapFactory.Options options = BitmapLoader.decodeBounds(item.path);

            // 자르는 경우에는 실제 예상보다 width/2를 하자
            if (item.side != ExplorerItem.Side.SIDE_ALL) {
                options.outWidth >>= 1;
            }
            item.width = options.outWidth;
            item.height = options.outHeight;

            float bitmapRatio = (float) options.outHeight / (float) options.outWidth;
            float screenRatio = (float) height / (float) width;

            if (screenRatio > bitmapRatio) {
                height = (int) (width * bitmapRatio);
            } else {
                height = (int) (width * screenRatio);
            }

            // 파일에서 읽어서 있으면 캐시에 넣는다
            count = 0;
            while (true) {
                //NEW-2
                if (item.side == ExplorerItem.Side.SIDE_ALL) {
                    bitmap = BitmapLoader.decodeSampleBitmapFromFile(item.path, width, height, exif);
                    if (bitmap == null) {
                        // 다른 비트맵이 기다려지길 기다렸다가 다시 시도하자.
                        // 왜냐면 압축을 푸는 중인 파일도 있기 때문이다.
                        if (!waitImageExtracting())
                            break;
                    } else {
                        BitmapCacheManager.setBitmap(item.path, bitmap);
                        break;
                    }
                } else {
                    // RegionDecoder가 지원되는 경우는 PNG, JPG
                    if (FileHelper.isJpegImage(item.path) || FileHelper.isPngImage(item.path)) {
                        bitmap = BitmapLoader.splitBitmapSide(item, width, height, exif);
                        if (bitmap == null) {
                            // 다른 비트맵이 기다려지길 기다렸다가 다시 시도하자.
                            // 왜냐면 압축을 푸는 중인 파일도 있기 때문이다.
                            if (!waitImageExtracting())
                                break;
                        } else {
                            break;
                        }
                    } else {
                        bitmap = BitmapLoader.decodeSampleBitmapFromFile(item.path, width, height, exif);
                        if (bitmap == null) {
                            // 다른 비트맵이 기다려지길 기다렸다가 다시 시도하자.
                            // 왜냐면 압축을 푸는 중인 파일도 있기 때문이다.
                            if (!waitImageExtracting())
                                break;
                        } else {
                            bitmap = BitmapLoader.splitBitmapSide(bitmap, item);
                            break;
                        }
                    }
                }

                //NEW-1
//                if (item.side == ExplorerItem.Side.SIDE_ALL) {
//                    bitmap = BitmapLoader.decodeSampleBitmapFromFile(item.path, width, height, exif);
//                    if (bitmap == null) {
//                        // 다른 비트맵이 기다려지길 기다렸다가 다시 시도하자.
//                        // 왜냐면 압축을 푸는 중인 파일도 있기 때문이다.
//                        if (!waitImageExtracting())
//                            break;
//                    }
//                    else {
//                        BitmapCacheManager.setBitmap(item.path, bitmap);
//                        break;
//                    }
//                } else {
//                    bitmap = BitmapLoader.decodeSampleBitmapFromFile(item.path, width, height, exif);
//                    if (bitmap == null) {
//                        // 다른 비트맵이 기다려지길 기다렸다가 다시 시도하자.
//                        // 왜냐면 압축을 푸는 중인 파일도 있기 때문이다.
//                        if (!waitImageExtracting())
//                            break;
//                    }
//                    else {
//                        bitmap = BitmapLoader.splitBitmapSide(bitmap, item);
//                        break;
//                    }
//                }

                //OLD
//                bitmap = BitmapLoader.decodeSampleBitmapFromFile(item.path, width, height, exif);
//                if (bitmap == null) {
//                    // 다른 비트맵이 기다려지길 기다렸다가 다시 시도하자.
//                    // 왜냐면 압축을 푸는 중인 파일도 있기 때문이다.
//                    if (!waitImageExtracting())
//                        break;
//                } else {
//                    if (item.side == ExplorerItem.Side.SIDE_ALL) {
//                        BitmapCacheManager.setBitmap(item.path, bitmap);
//                    } else {
//                        // 비트맵을 로딩했으면 이제 자르자
//                        // 자르고 현재 page의 bitmap을 리턴한다.
//                        bitmap = BitmapLoader.splitBitmapSide(bitmap, item);
////                        bitmap = BitmapLoader.splitBitmapSide(item, width, height, exif);
//                    }
//                    break;
//                }
            }
        }

        return bitmap;
    }

    private boolean waitImageExtracting() {
        try {
            count += RETRY_INTERVAL_MS;
            if (count == RETRY_INTERVAL_MS * RETRY_COUNT)
                return false;
//            JLog.e(TAG, "waitImageExtracting");
            Thread.sleep(RETRY_INTERVAL_MS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }
}
