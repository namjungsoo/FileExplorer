package com.duongame.fileexplorer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.duongame.fileexplorer.adapter.ExplorerFileItem;
import com.duongame.fileexplorer.Helper.ExplorerSearcher;
import com.duongame.fileexplorer.R;
import com.duongame.fileexplorer.adapter.ExplorerPagerAdapter;
import com.duongame.fileexplorer.bitmap.BitmapCacheManager;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-18.
 */

public class ImageActivity extends ViewerActivity {
    private final static String TAG = "ImageActivity";
    private ViewPager pager;
    private ExplorerPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new ExplorerPagerAdapter(this);

        pager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d(TAG,"onTouch");
                if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    setFullscreen(!isFullscreen);
                    return true;
                }
                return false;
            }
        });

        initAdapter();
        processIntent();
    }

    private void initAdapter() {
        final ArrayList<ExplorerFileItem> imageList = ExplorerSearcher.getImageList();
        pagerAdapter.setImageList(imageList);
        pager.setAdapter(pagerAdapter);
    }

    private void processIntent() {
        final ArrayList<ExplorerFileItem> imageList = ExplorerSearcher.getImageList();
        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();
        if (extras != null) {
            final String name = extras.getString("name");

            int item = 0;
            for (int i = 0; i < imageList.size(); i++) {
                if (imageList.get(i).name.equals(name)) {
                    item = i;
                }
            }
            pager.setCurrentItem(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BitmapCacheManager.recycleBitmap();
    }
}
