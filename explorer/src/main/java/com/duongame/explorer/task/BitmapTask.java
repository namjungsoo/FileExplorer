package com.duongame.explorer.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.bitmap.BitmapCache;
import com.duongame.explorer.bitmap.BitmapLoader;

import static android.content.ContentValues.TAG;

/**
 * Created by namjungsoo on 2016-12-17.
 */

public class BitmapTask extends AsyncTask<ExplorerItem, Void, Bitmap> {
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
    protected Bitmap doInBackground(ExplorerItem... params) {
        return null;
    }

    protected Bitmap loadBitmap(ExplorerItem item) {
        // 캐시에 있는지 확인해 보고
        // split일 경우에는 무조건 없다
        Bitmap bitmap = null;
        if (item.side != ExplorerItem.Side.SIDE_ALL) {
            final String page = BitmapCache.changePathToPage(item);
            bitmap = BitmapCache.getPage(page);

            if (bitmap != null) {
                Log.d(this.getClass().getSimpleName(), "loadBitmap found cached page="+item.path);
                return bitmap;
            }
        }

        bitmap = BitmapCache.getBitmap(item.path);
        if (bitmap == null) {
            // 렌더러를 어떻게 전달하지..
            // PDF는 동적으로 읽을수 없다.
//            if(item.type == ExplorerItem.FileType.PDF) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    final int pageNum = FileHelper.getPdfPageFromFileName(item.path);
//                    final PdfRenderer.Page page = PdfActivity.renderer.openPage(pageNum);
//                    Log.d(TAG,"pdf pageNum="+pageNum);
//
//                    //TODO: 화면크기와 PDF 이미지 크기를 체크할것
//                    int width = page.getWidth();
//                    int height = page.getHeight();
//                    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//
//                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
//                    BitmapCache.setBitmap(item.path, bitmap);
//                    return bitmap;
//                }
//            }

            final BitmapFactory.Options options = BitmapLoader.decodeBounds(item.path);

            // 자르는 경우에는 실제 예상보다 width/2를 하자
            if (item.side != ExplorerItem.Side.SIDE_ALL) {
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
                    if (item.side == ExplorerItem.Side.SIDE_ALL) {
                        Log.d(this.getClass().getSimpleName(), "loadBitmap setBitmap="+item.path);
                        BitmapCache.setBitmap(item.path, bitmap);
                    }
                    else {
                        // 비트맵을 로딩했으면 이제 자르자
                        Log.d(this.getClass().getSimpleName(), "loadBitmap splitBitmapSide="+item.path);
                        bitmap = BitmapLoader.splitBitmapSide(bitmap, item);
                    }
                    break;
                }
            }
        } else {
            Log.d(TAG, "loadBitmap found cached bitmap=" + item.path);
        }
        return bitmap;
    }
}
