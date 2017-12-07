package com.duongame.task.thumbnail;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.duongame.GlideApp;
import com.duongame.R;
import com.duongame.bitmap.BitmapCacheManager;
import com.duongame.bitmap.BitmapLoader;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by namjungsoo on 2016-12-16.
 */

public class LoadZipThumbnailTask extends AsyncTask<String, Void, String> {
    private final String TAG = LoadZipThumbnailTask.class.getSimpleName();

    private final WeakReference<Context> contextRef;
    private final WeakReference<ImageView> iconRef, iconSmallRef;

    private String path;

    public LoadZipThumbnailTask(Context context, ImageView icon, ImageView iconSmall) {
        this.contextRef = new WeakReference<Context>(context);
        this.iconRef = new WeakReference<ImageView>(icon);
        this.iconSmallRef = new WeakReference<ImageView>(iconSmall);
    }

    @Override
    protected String doInBackground(String... params) {
        path = params[0];
        if (contextRef.get() == null)
            return null;

        final String image = BitmapLoader.getZipThumbnailFileName(contextRef.get(), path);
        return image;
    }

    @Override
    protected void onPostExecute(final String param) {
        super.onPostExecute(param);
        if (path == null)
            return;
        if (param == null)
            return;

        if (contextRef.get() == null)
            return;
        if (iconRef.get() == null)
            return;
        if (iconSmallRef.get() == null)
            return;

        if (contextRef.get() instanceof Activity) {
            if (((Activity) contextRef.get()).isFinishing())
                return;
        }

        GlideApp.with(contextRef.get())
                .load(new File(param))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.zip)
                .centerCrop()
                .into(new ImageViewTarget<Drawable>(iconRef.get()) {
                    @Override
                    protected void setResource(@Nullable Drawable resource) {
                        //FIX: destroyed activity error
                        if (contextRef.get() instanceof Activity) {
                            if (((Activity) contextRef.get()).isFinishing())
                                return;
                        }

                        if (path.equals(iconSmallRef.get().getTag())) {
                            if (resource != null) {
                                getView().setImageDrawable(resource);
                                BitmapCacheManager.setDrawable(path, resource);
                            }
                        }
                    }
                });
    }
}
