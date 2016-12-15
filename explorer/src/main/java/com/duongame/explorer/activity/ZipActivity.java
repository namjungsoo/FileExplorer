package com.duongame.explorer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.duongame.explorer.adapter.ExplorerFileItem;
import com.duongame.explorer.bitmap.ZipLoader;

import net.lingala.zip4j.exception.ZipException;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-19.
 */

public class ZipActivity extends PagerActivity {
    private final static String TAG = "ZipActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        initPagerAdapter();

        pagerAdapter.setExifRotation(false);
        processIntent();
    }

//    private void initPagerAdapter() {
//        final ArrayList<ExplorerFileItem> imageList = getImageList();
//        pagerAdapter.setImageList(imageList);
//        pager.setAdapter(pagerAdapter);
//    }

    protected ArrayList<ExplorerFileItem> getImageList() {
        return null;
    }

    protected void processIntent() {
        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();
        if (extras != null) {
            final int page = extras.getInt("page");
            path = extras.getString("path");
            textPath.setText(path);

            // zip 파일을 로딩한다.
            final ZipLoader zipLoader = new ZipLoader();
            try {
                final ArrayList<ExplorerFileItem> imageList = zipLoader.load(this, path, new ZipLoader.ZipLoaderListener() {
                    @Override
                    public void onSuccess(int i, String name) {
                        if (i == page) {
                            pager.setCurrentItem(page);
                            Log.d(TAG, "setCurrentItem="+i);
                        }
                        pagerAdapter.setMaxIndex(i);
                    }

                    @Override
                    public void onFail(int i, String name) {
                        Log.e(TAG, "onFail i="+i + " name="+name);
                    }
                }, false);
                pagerAdapter.setImageList(imageList);
                pager.setAdapter(pagerAdapter);

                seekPage.setMax(imageList.size());
                seekPage.setProgress(1);

            } catch (ZipException e) {
                e.printStackTrace();
            }
        }
    }
}
