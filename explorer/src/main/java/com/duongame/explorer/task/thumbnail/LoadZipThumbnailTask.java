package com.duongame.explorer.task.thumbnail;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.ImageViewTarget;
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

    private final Activity context;

    private ImageView icon, iconSmall;
    private String path;

    public LoadZipThumbnailTask(Activity context, ImageView icon, ImageView iconSmall) {
        this.context = context;
        this.icon = icon;
        this.iconSmall = iconSmall;
    }

    @Override
    protected String doInBackground(String... params) {
        path = params[0];
        final String image = BitmapLoader.getZipThumbnailFileName(context, path);
        return image;
    }

    @Override
    protected void onPostExecute(final String param) {
        super.onPostExecute(param);
        if (path == null)
            return;
        if (param == null)
            return;
        GlideApp.with(context)
                .load(new File(param))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.zip)
                .centerCrop()
                .into(new ImageViewTarget<Drawable>(icon) {
                    @Override
                    protected void setResource(@Nullable Drawable resource) {
                        if (path.equals(iconSmall.getTag())) {
                            if(resource != null) {
                                getView().setImageDrawable(resource);
                                BitmapCacheManager.setDrawable(path, resource);
                            }
                        }
                    }
                });
    }
}
