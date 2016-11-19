package com.duongame.fileexplorer.activity;

import android.content.Intent;
import android.os.Bundle;

import com.duongame.fileexplorer.adapter.ExplorerFileItem;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-19.
 */

public class ZipActivity extends PagerActivity {
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initPagerAdapter();

        processIntent();
    }

    private void initPagerAdapter() {
        final ArrayList<ExplorerFileItem> imageList = getImageList();
        pagerAdapter.setImageList(imageList);
        pager.setAdapter(pagerAdapter);
    }

    protected ArrayList<ExplorerFileItem> getImageList() {
        return null;
    }

    protected void processIntent() {
        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();
        if (extras != null) {
            final int page = extras.getInt("page");
            path = extras.getString("path");

            // zip 파일을 로딩한다.

            pager.setCurrentItem(page);
        }
    }
}
