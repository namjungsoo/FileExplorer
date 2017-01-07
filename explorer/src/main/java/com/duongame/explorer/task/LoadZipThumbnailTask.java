package com.duongame.explorer.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.duongame.explorer.R;
import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.bitmap.BitmapCacheManager;
import com.duongame.explorer.bitmap.BitmapLoader;
import com.duongame.explorer.bitmap.ZipLoader;

import net.lingala.zip4j.exception.ZipException;

import java.util.ArrayList;

import static com.duongame.explorer.adapter.ExplorerItem.Side.LEFT;
import static com.duongame.explorer.bitmap.BitmapCacheManager.getThumbnail;

/**
 * Created by namjungsoo on 2016-12-16.
 */

public class LoadZipThumbnailTask extends AsyncTask<String, Void, Bitmap> {
    private final ImageView imageView;
    private final Context context;

    public LoadZipThumbnailTask(Context context, ImageView imageView) {
        this.imageView = imageView;
        this.context = context;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
//        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        final String path = params[0];

        if(isCancelled())
            return null;

        Bitmap bitmap = getThumbnail(path);
        if(isCancelled())
            return bitmap;

        if (bitmap == null) {
            String image = null;
            try {
                //image = ZipLoader.getFirstImage(context, path);
                final ZipLoader loader = new ZipLoader();
                final ArrayList<ExplorerItem> imageList = loader.load(context, path, null, 0, LEFT, true);

                if(imageList != null && imageList.size() > 0) {
                    image = imageList.get(0).path;
                }

            } catch (ZipException e) {
                e.printStackTrace();
            }

            if(isCancelled())
                return bitmap;

            if (image == null) {
                bitmap = BitmapCacheManager.getResourceBitmap(context.getResources(), R.drawable.zip);
                if(bitmap != null)
                    BitmapCacheManager.setThumbnail(path, bitmap, imageView);
            }
            else {
                //bitmap = BitmapLoader.decodeSampleBitmapFromFile(image, 96, 96, false);
                bitmap = BitmapLoader.decodeSquareThumbnailFromFile(image, 96, false);
                if(bitmap != null)
                    BitmapCacheManager.setThumbnail(path, bitmap, imageView);
            }
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
