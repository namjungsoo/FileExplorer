package com.duongame.photoviewattacher;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.PhotoViewAttacher;

public class MainActivity extends AppCompatActivity {

    PhotoViewAttacher mAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageView = (ImageView)findViewById(R.id.tekken);
        mAttacher = new PhotoViewAttacher(imageView);
    }
}
