package com.duongame.task.bitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.duongame.activity.viewer.PagerActivity;
import com.duongame.adapter.ExplorerItem;
import com.duongame.bitmap.BitmapCacheManager;
import com.duongame.bitmap.BitmapLoader;
import com.duongame.file.FileHelper;
import com.duongame.helper.JLog;

import java.lang.ref.WeakReference;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.duongame.adapter.ExplorerItem.SIDE_ALL;

/**
 * Created by namjungsoo on 2016-12-16.
 */

public class LoadBitmapTask extends AsyncTask<ExplorerItem, Void, BitmapLoader.SplittedBitmap> {
    private final static String TAG = LoadBitmapTask.class.getSimpleName();
    private static final int RETRY_INTERVAL_MS = 100;
    private static final int RETRY_COUNT = 5;

    private final WeakReference<PagerActivity> contextRef;
    private final WeakReference<ImageView> imageViewRef;

    private ExplorerItem item;
    private int position;

    private int screenWidth, screenHeight;
    private boolean exifRotation;
    private int count;
    private boolean loadingByOther;

    private static CopyOnWriteArraySet<String> currentLoadingBitmapList = new CopyOnWriteArraySet<>();

    public static boolean isCurrentLoadingBitmap(String path) {
        return currentLoadingBitmapList.contains(path);
    }

    public static void setCurrentLoadingBitmap(String path) {
        currentLoadingBitmapList.add(path);
    }

    public static void removeCurrentLoadingBitmap(String path) {
        currentLoadingBitmapList.remove(path);
    }

    public LoadBitmapTask(PagerActivity context, ImageView imageView, int width, int height, boolean exifRotation, int position, boolean loadingByOther) {
        this.screenWidth = width;
        this.screenHeight = height;
        this.exifRotation = exifRotation;

        this.contextRef = new WeakReference<>(context);
        this.imageViewRef = new WeakReference<>(imageView);

        this.position = position;
        this.loadingByOther = loadingByOther;
    }

    @Override
    protected BitmapLoader.SplittedBitmap doInBackground(ExplorerItem... params) {
        if (isCancelled())
            return null;

        item = params[0];
//        setCurrentLoadingBitmap(item.path);
        JLog.e(TAG, "setCurrentLoadingBitmap " + item.path);
        return loadBitmap(item);
    }

    @Override
    protected void onPostExecute(BitmapLoader.SplittedBitmap sb) {
        super.onPostExecute(sb);

        // imageView 셋팅은 UI 쓰레드에서 해야 한다.
        if (sb == null)
            return;

        ImageView imageView = imageViewRef.get();
        if (imageView == null)
            return;

        if (isCancelled())
            return;

        if (item.side == SIDE_ALL) {
            if (sb.bitmap == null)
                return;

            imageView.setImageBitmap(sb.bitmap);
            imageView.setTag(sb.path);

            BitmapCacheManager.setBitmap(sb.path, sb.bitmap, imageView);
        } else {
            imageView.setImageBitmap(sb.page);
            imageView.setTag(sb.key);

            BitmapCacheManager.setPage(sb.key, sb.page, imageView);
            if (sb.pageOther != null) {
                // other 페이지는 없을때만 등록하자
                if(BitmapCacheManager.getPage(sb.keyOther) == null) {
                    BitmapCacheManager.setPage(sb.keyOther, sb.pageOther, null);
                }
            }
        }

        // 셀프 로딩인 경우에만 제거해준다.
        if (!loadingByOther)
            removeCurrentLoadingBitmap(item.path);

        PagerActivity context = contextRef.get();
        if (context == null)
            return;

        if (!context.isFinishing()) {
            context.updateInfo(position);
        }
    }

    private BitmapLoader.SplittedBitmap loadBitmap(ExplorerItem item) {
        // 캐시에 있는지 확인해 보고
        // split일 경우에는 무조건 없다
        Bitmap bitmap = null;
        BitmapLoader.SplittedBitmap sb = new BitmapLoader.SplittedBitmap();
        if (item.side != ExplorerItem.SIDE_ALL) {// split인 이미지 이면서 캐쉬가 되어 있으면 바로 리턴한다.
            final String page = BitmapCacheManager.changePathToPage(item);
            JLog.e(TAG, "loadBitmap changePathToPage " + page + " hash=" + this.hashCode());

            count = 0;
//            boolean loadingByOther = isCurrentLoadingBitmap(item.path);
            JLog.e(TAG, "isCurrentLoadingBitmap " + item.path + " " + loadingByOther);

            while (true) {
                bitmap = BitmapCacheManager.getPage(page);
                if (bitmap != null) {
                    JLog.e(TAG, "loadBitmap found " + page);
                    sb.key = page;
                    sb.page = bitmap;
                    return sb;
                } else {
                    // 옆의 페이지가 내것을 로딩하고 있지 않으면 내가 직접 로딩해야 한다.
                    if (!loadingByOther) {
                        JLog.e(TAG, "Not loading. Self load begin " + page);
                        break;
                    } else {
                        if (isTimedOutForImageExtracting()) {
                            break;
                        }
                        JLog.e(TAG, "Loaded by next page " + page);
                    }
                }
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
                    bitmap = BitmapLoader.decodeSampleBitmapFromFile(item.path, screenWidth, screenHeight, exifRotation);
                    if (bitmap == null) {
                        // 다른 비트맵이 기다려지길 기다렸다가 다시 시도하자.
                        // 왜냐면 압축을 푸는 중인 파일도 있기 때문이다.
                        if (isTimedOutForImageExtracting()) {
                            sb.path = item.path;
                            sb.bitmap = null;
                            break;
                        }
                    } else {
                        sb.path = item.path;
                        sb.bitmap = bitmap;
                        break;
                    }
                } else {
                    // RegionDecoder가 지원되는 경우는 PNG, JPG
                    if (FileHelper.isJpegImage(item.path) || FileHelper.isPngImage(item.path)) {
                        sb = BitmapLoader.splitBitmapSide(item, screenWidth, screenHeight, exifRotation);
                        if (sb == null) {
                            // 다른 비트맵이 기다려지길 기다렸다가 다시 시도하자.
                            // 왜냐면 압축을 푸는 중인 파일도 있기 때문이다.
                            if (isTimedOutForImageExtracting())
                                break;
                        } else {
                            break;
                        }
                    } else {
                        // GIF는 RegionDecoder가 지원이 되지 않는다.
                        bitmap = BitmapLoader.decodeSampleBitmapFromFile(item.path, screenWidth, screenHeight, exifRotation);
                        if (bitmap == null) {
                            // 다른 비트맵이 기다려지길 기다렸다가 다시 시도하자.
                            // 왜냐면 압축을 푸는 중인 파일도 있기 때문이다.
                            if (isTimedOutForImageExtracting())
                                break;
                        } else {
                            sb = BitmapLoader.splitBitmapSide(bitmap, item);
                            break;
                        }
                    }
                }
            }
        } else {
            sb.bitmap = bitmap;
            sb.path = item.path;
        }

        // SIDE_ALL일때 파일이 없으면
        // sb.path = item.path
        // sb.bitmap = null

        // SIDE_LEFT or SIDE_RIGHT는
        // sb = null
        return sb;
//        return bitmap;
    }

    // 다른 쓰레드에 의해서 이미지가 압축 풀리길 기다렸다가
    // 타임아웃이 되면 true를 리턴한다.
    // false를 리턴하는 것은 sleep에 예외가 발생했을 때이다.
    private boolean isTimedOutForImageExtracting() {
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
