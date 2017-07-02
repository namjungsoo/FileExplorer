package com.example.giftester;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.felipecsl.gifimageview.library.GifImageView;
import com.github.chrisbanes.photoview.PhotoViewAttacher;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    String sdPath = "/storage/emulated/0/Download/박기량.gif";

    void loadGifStream() {
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
                    .into(gif);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void loadGifDirect2() {

        DataInputStream dis = null;
        try {

            dis = new DataInputStream(new FileInputStream(sdPath));
            int length = dis.available();
            byte[] data = new byte[length];
            dis.readFully(data);

            Glide
                    .with(this)
                    .load(data)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(gif);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void loadGifDirect() {
        Glide.with(this).load(new File("/storage/emulated/0/Download/박기량.gif")).into(gif);
    }

    PhotoViewAttacher attacher;
    void loadGifOld() {
        GifImageView gif = (GifImageView) findViewById(R.id.image_viewer);

        try {
            DataInputStream dis = new DataInputStream(getAssets().open("박기량.gif"));
            int length = dis.available();
            byte[] data = new byte[length];
            dis.readFully(data);
            gif.setBytes(data);
            gif.startAnimation();

            attacher = new PhotoViewAttacher(gif);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void onPermissionAcquired() {
        //loadGifStream(gif);
        //loadGifDirect2();
        loadGifOld();
    }

    ImageView gif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gif = (ImageView) findViewById(R.id.image_viewer);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                String[] permissions = new String[2];
                permissions[0] = Manifest.permission.READ_EXTERNAL_STORAGE;
                permissions[1] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                requestPermissions(permissions, 1);
            } else {
                onPermissionAcquired();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            boolean result = true;

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != 0) {
                    result = false;
                    break;
                }
            }

            if (result) {
                onPermissionAcquired();
            }
        }
    }
}
