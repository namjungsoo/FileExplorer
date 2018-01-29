package com.duongame.task.bitmap;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.duongame.activity.PagerActivity;
import com.duongame.adapter.ExplorerItem;
import com.duongame.helper.FileHelper;

import java.lang.ref.WeakReference;

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

        this.contextRef = new WeakReference<PagerActivity>(context);
        this.imageViewRef = new WeakReference<ImageView>(imageView);

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
        // useGifAni: 애니메이션이 있을때는 외부에서 쓰레드를 통해서 렌더링 하므로 여기서는 미리 gif를 로딩해 놓지 않는다.
        if (useGifAni && FileHelper.isGifImage(item.path)) {
//            Glide.with(context).load(new File(item.path)).into(imageView);

            // 일단 애니메이션이 있는지를 체크해보고 없으면 내가 로딩하자
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
        if (bitmap == null)
            return;

        if (imageViewRef.get() == null)
            return;

        imageViewRef.get().setImageBitmap(bitmap);
        imageViewRef.get().setTag(item.path);

        if (!contextRef.get().isFinishing()) {
            contextRef.get().updateInfo(position);
        }

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

        //PhotoView 때문인가
//                final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageView.getLayoutParams();
//                params.width = newWidth;
//                params.height = newHeight;
//                params.gravity = Gravity.CENTER;
//
//                imageView.setLayoutParams(params);
//                imageView.requestLayout();
//            } else {
        // 사용안함
//                Glide.with(context).load(new File(item.path)).into(imageView);
//            }
//        }
    }
}
