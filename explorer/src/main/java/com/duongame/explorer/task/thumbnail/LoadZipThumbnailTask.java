package com.duongame.explorer.task.thumbnail;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.duongame.R;
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
        final String image = BitmapLoader.getZipThumbnailFileName(context, params[0]);
        return image;
    }

    @Override
    protected void onPostExecute(String param) {
        super.onPostExecute(param);
        if (param != null) {
            Glide.with(context)
                    .load(new File(param))
                    .placeholder(R.drawable.zip)
                    .centerCrop()
                    .into(imageView);
        }
    }
}
