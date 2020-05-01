package com.duongame.task.thumbnail;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import androidx.annotation.Nullable;
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

    private final WeakReference<Activity> contextRef;
    private final WeakReference<ImageView> iconRef, iconSmallRef;

    private String path;

    public LoadZipThumbnailTask(Activity context, ImageView icon, ImageView iconSmall) {
        this.contextRef = new WeakReference<>(context);
        this.iconRef = new WeakReference<>(icon);
        this.iconSmallRef = new WeakReference<>(iconSmall);
    }

    @Override
    protected String doInBackground(String... params) {
        path = params[0];
        if (contextRef.get() == null)
            return null;

        return BitmapLoader.getZipThumbnailFileName(contextRef.get(), path);
    }

    @Override
    protected void onPostExecute(final String param) {
        super.onPostExecute(param);
        if (path == null)
            return;
        if (param == null)
            return;

        Activity context = contextRef.get();
        if (context == null)
            return;
        if (iconRef.get() == null)
            return;
        if (iconSmallRef.get() == null)
            return;

        if (context.isFinishing())
            return;

        try {
            GlideApp.with(context.getApplicationContext())
                    .load(new File(param))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_file_zip)
                    .centerCrop()
                    .into(new ImageViewTarget<Drawable>(iconRef.get()) {
                        @Override
                        protected void setResource(@Nullable Drawable resource) {
                            //FIX: destroyed activity error
                            if (resource == null)
                                return;

                            if (path.equals(iconSmallRef.get().getTag())) {
                                Bitmap bitmap = BitmapLoader.drawableToBitmap(resource);
                                getView().setImageBitmap(bitmap);
                                BitmapCacheManager.setThumbnail(path, bitmap, iconRef.get());
                            }
                        }
                    });
        } catch (Exception e) {// FIX: IllegalArgumentException

        }
    }
}
