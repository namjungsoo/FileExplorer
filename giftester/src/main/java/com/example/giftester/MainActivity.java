package com.example.giftester;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import java.io.DataInputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView gif = (ImageView)findViewById(R.id.image_viewer);
        GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(gif);

        DataInputStream dis = null;
        try {
            dis = new DataInputStream(getAssets().open("박기량.gif"));
            int length = dis.available();
            byte[] data = new byte[length];
            dis.readFully(data);

            Glide
                    .with(this)
                    .load(data)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(imageViewTarget);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        GifImageView gif = (GifImageView)findViewById(R.id.image_viewer);
//
//        try {
//            DataInputStream dis = new DataInputStream(getAssets().open("박기량.gif"));
//            int length = dis.available();
//            byte[] data = new byte[length];
//            dis.readFully(data);
//            gif.setBytes(data);
//            gif.startAnimation();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


    }
}
