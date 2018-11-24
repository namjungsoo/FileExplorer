package com.duongame.task.bitmap;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.duongame.activity.viewer.PagerActivity;
import com.duongame.adapter.ExplorerItem;
import com.duongame.bitmap.BitmapCacheManager;
import com.duongame.bitmap.BitmapLoader;
import com.duongame.file.FileHelper;
import com.duongame.helper.JLog;

import java.lang.ref.WeakReference;

import static com.duongame.adapter.ExplorerItem.SIDE_ALL;

/**
 * Created by namjungsoo on 2016-12-16.
 */

public class LoadBitmapTask extends BitmapTask {
    private final static String TAG = LoadBitmapTask.class.getSimpleName();
    private final WeakReference<PagerActivity> contextRef;
    private final WeakReference<ImageView> imageViewRef;

    private ExplorerItem item;
    private boolean useGifAni;
    private int position;

    public LoadBitmapTask(PagerActivity context, ImageView imageView, int width, int height, boolean exif, boolean useGifAni, int position) {
        super(width, height, exif);

        this.contextRef = new WeakReference<>(context);
        this.imageViewRef = new WeakReference<>(imageView);

        this.position = position;
        this.useGifAni = useGifAni;
    }

    @Override
    protected BitmapLoader.SplittedBitmap doInBackground(ExplorerItem... params) {
        if (isCancelled())
            return null;

        item = params[0];
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
                BitmapCacheManager.setPage(sb.keyOther, sb.pageOther, null);
            }
        }
        item.loading = false;

        PagerActivity context = contextRef.get();
        if (context == null)
            return;

        if (!context.isFinishing()) {
            context.updateInfo(position);
        }
    }
}
