package com.duongame.explorer.bitmap;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.duongame.explorer.adapter.ExplorerItem;

/**
 * Created by namjungsoo on 2017-04-06.
 */

public class BitmapMessage {
    public int position;
    public ExplorerItem.FileType type;
    public ImageView imageView;
    public Bitmap bitmap;
    public Drawable drawable;
    public String path;
}
