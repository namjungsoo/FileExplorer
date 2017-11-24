package com.duongame.explorer.task.bitmap;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.helper.FileHelper;
import com.duongame.viewer.activity.PagerActivity;

/**
 * Created by namjungsoo on 2016-12-16.
 */

public class LoadBitmapTask extends BitmapTask {
    private final ImageView imageView;
    private ExplorerItem item;
    private PagerActivity context;
    private boolean useGifAni;
    int position;

    public LoadBitmapTask(PagerActivity context, ImageView imageView, int width, int height, boolean exif, boolean useGifAni, int position) {
        super(width, height, exif);

        this.context = context;
        this.imageView = imageView;
        this.position = position;
        this.useGifAni = useGifAni;
    }

    @Override
    protected Bitmap doInBackground(ExplorerItem... params) {
//        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        if (isCancelled())
            return null;

        item = params[0];

        // GIF는 여기서 읽지 않는다.
        if (useGifAni && FileHelper.isGifImage(item.path)) {
//            Glide.with(context).load(new File(item.path)).into(imageView);
            return null;
        }

        Bitmap bitmap = null;
        bitmap = loadBitmap(item);
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        // imageView 셋팅은 UI 쓰레드에서 해야 한다.
        if (imageView != null) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                imageView.setTag(item.path);

                context.updateInfo(position);

                // 종횡비 체크
//                final int bmWidth = bitmap.getWidth();
//                final int bmHeight = bitmap.getHeight();
//                final float bmRatio = (float) bmHeight / (float) bmWidth;
//
//                // 화면비 체크
//                final int width = imageView.getWidth();
//                final int height = imageView.getHeight();
//                final float imageRatio = (float) height / (float) width;
//
//                int newWidth;
//                int newHeight;
//                if (bmRatio > imageRatio) {
//                    newWidth = (int) (height / bmRatio);
//                    newHeight = height;
//                } else {
//                    newWidth = width;
//                    newHeight = (int) (width * bmRatio);
//                }

                //TODO: 이게 왜 현재 사용이 안되지?
                //PhotoView 때문인가
//                final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageView.getLayoutParams();
//                params.width = newWidth;
//                params.height = newHeight;
//                params.gravity = Gravity.CENTER;
//
//                imageView.setLayoutParams(params);
//                imageView.requestLayout();
            } else {
                // 사용안함
//                Glide.with(context).load(new File(item.path)).into(imageView);
            }
        }
    }
}
