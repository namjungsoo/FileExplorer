package com.duongame.fileexplorer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.duongame.fileexplorer.ExplorerFileItem;
import com.duongame.fileexplorer.ExplorerSearcher;
import com.duongame.fileexplorer.R;
import com.duongame.fileexplorer.adapter.ExplorerPagerAdapter;
import com.duongame.fileexplorer.bitmap.BitmapCacheManager;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-16.
 */

public class ViewerActivity extends AppCompatActivity {
    private ViewPager pager;
    private ExplorerPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new ExplorerPagerAdapter(this);

        ArrayList<ExplorerFileItem> imageList = ExplorerSearcher.getImageList();
        pagerAdapter.setImageList(imageList);
        pager.setAdapter(pagerAdapter);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String name = extras.getString("name");

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
