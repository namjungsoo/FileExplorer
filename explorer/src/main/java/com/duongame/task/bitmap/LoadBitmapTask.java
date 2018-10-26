package com.duongame.task.bitmap;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.duongame.activity.viewer.PagerActivity;
import com.duongame.adapter.ExplorerItem;
import com.duongame.bitmap.BitmapCacheManager;
import com.duongame.file.FileHelper;

import java.lang.ref.WeakReference;

import static com.duongame.adapter.ExplorerItem.SIDE_ALL;

/**
 * Created by namjungsoo on 2016-12-16.
 */

public class LoadBitmapTask extends BitmapTask {
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
    protected Bitmap doInBackground(ExplorerItem... params) {
        if (isCancelled())
            return null;

        item = params[0];

        // GIF는 여기서 읽지 않는다.
        // useGifAni: 애니메이션이 있을때는 외부에서 쓰레드를 통해서 렌더링 하므로 여기서는 미리 gif를 로딩해 놓지 않는다.
        if (useGifAni && FileHelper.isGifImage(item.path)) {
//            Glide.with(context).load(new File(item.path)).into(imageView);

            // 일단 애니메이션이 있는지를 체크해보고 없으면 내가 로딩하자
            return null;
        }

        return loadBitmap(item);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        // imageView 셋팅은 UI 쓰레드에서 해야 한다.
        if (bitmap == null)
            return;

        ImageView imageView = imageViewRef.get();
        if (imageView == null)
            return;

        if (isCancelled())
            return;

        imageView.setImageBitmap(bitmap);
        imageView.setTag(item.path);
        if(item.side == SIDE_ALL)
            BitmapCacheManager.setBitmap(item.path, bitmap, imageView);
        else
            BitmapCacheManager.setPage(item.path, bitmap, imageView);

        PagerActivity context = contextRef.get();
        if(context == null)
            return;

        if (!context.isFinishing()) {
            context.updateInfo(position);
        }
    }
}
