package com.duongame.explorer.task.thumbnail;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.duongame.explorer.bitmap.BitmapCache;

/**
 * Created by namjungsoo on 2017-04-05.
 */

public class LoadVideoThumbnailTask extends AsyncTask<String, Void, Bitmap> {
    private final Activity context;
    private final ImageView imageView;
    private String path;

    public LoadVideoThumbnailTask(Activity context, ImageView imageView) {
        this.context = context;
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        path = params[0];
        if (isCancelled())
            return null;

        Bitmap bitmap = BitmapCache.getThumbnail(path);
        if (isCancelled())
            return bitmap;

        // 이제 PDF에서 파일을 읽어서 렌더링 하자.
        if (bitmap == null) {
            //bitmap = BitmapLoader.decodeSquareThumbnailFromPdfFile(path, 96);
            bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MICRO_KIND);
        }

        // 찾았으면 캐시에 추가
        if (bitmap != null) {
            BitmapCache.setThumbnail(path, bitmap, imageView);
        }

        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (imageView != null && bitmap != null) {
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

}
