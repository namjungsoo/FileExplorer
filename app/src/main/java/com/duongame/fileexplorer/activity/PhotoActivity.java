package com.duongame.fileexplorer.activity;

import android.content.Intent;
import android.os.Bundle;

import com.duongame.fileexplorer.adapter.ExplorerFileItem;
import com.duongame.fileexplorer.helper.ExplorerSearcher;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-18.
 */

public class PhotoActivity extends PagerActivity {
    private final static String TAG = "PhotoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initPagerAdapter();

        processIntent();
    }

    protected ArrayList<ExplorerFileItem> getImageList() {
        return ExplorerSearcher.getImageList();
    }

    private void initPagerAdapter() {
        final ArrayList<ExplorerFileItem> imageList = getImageList();
        pagerAdapter.setImageList(imageList);
        pager.setAdapter(pagerAdapter);
    }

    protected void processIntent() {
        final ArrayList<ExplorerFileItem> imageList = getImageList();
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
}