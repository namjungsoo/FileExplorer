package com.duongame.explorer.task.thumbnail;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.duongame.GlideApp;
import com.duongame.R;
import com.duongame.explorer.bitmap.BitmapCacheManager;
import com.duongame.explorer.bitmap.BitmapLoader;

import java.io.File;

/**
 * Created by namjungsoo on 2016-12-16.
 */

public class LoadZipThumbnailTask extends AsyncTask<String, Void, String> {
    private final String TAG = LoadZipThumbnailTask.class.getSimpleName();
    private final ImageView imageView;
    private final Activity context;
    private String path;

    public LoadZipThumbnailTask(Activity context, ImageView imageView) {
        this.imageView = imageView;
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {
        path = params[0];
        final String image = BitmapLoader.getZipThumbnailFileName(context, params[0]);
        return image;
    }

    @Override
    protected void onPostExecute(String param) {
        super.onPostExecute(param);
        if (param != null) {
            GlideApp.with(context)
                    .load(new File(param))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.zip)
                    .centerCrop()
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            BitmapCacheManager.setDrawable(path, resource);
                            return true;
//                            return false;
                        }
                    })
                    .into(imageView);
        }
    }
}
