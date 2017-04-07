package com.duongame.explorer.bitmap;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.duongame.explorer.adapter.ExplorerItem;

import java.lang.ref.WeakReference;

/**
 * Created by namjungsoo on 2017-04-06.
 */

public class BitmapMsg {
    public ExplorerItem.FileType type;
    public WeakReference<ImageView> imageView;
    public Bitmap bitmap;
    public Drawable drawable;
    public String path;
}
