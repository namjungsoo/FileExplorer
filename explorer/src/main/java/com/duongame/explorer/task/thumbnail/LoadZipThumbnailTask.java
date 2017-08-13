package com.duongame.explorer.task.thumbnail;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.duongame.explorer.bitmap.BitmapCacheManager;
import com.duongame.explorer.bitmap.BitmapLoader;

import java.io.File;

/**
 * Created by namjungsoo on 2016-12-16.
 */

public class LoadZipThumbnailTask extends AsyncTask<String, Void, String> {
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
            Glide.with(context)
                    .load(new File(param))
                    .listener(new RequestListener<File, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, File model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, File model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            BitmapCacheManager.setDrawable(path, resource);
                            return false;
                        }
                    })
//                    .placeholder(R.drawable.zip)
                    .centerCrop()
                    .into(imageView);
        }
    }
}
