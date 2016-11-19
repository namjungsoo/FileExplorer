package com.duongame.fileexplorer.activity;

import android.content.Intent;
import android.os.Bundle;

import com.duongame.fileexplorer.adapter.ExplorerFileItem;
import com.duongame.fileexplorer.bitmap.ZipLoader;

import net.lingala.zip4j.exception.ZipException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-19.
 */

public class ZipActivity extends PagerActivity {
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        initPagerAdapter();

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

            // zip 파일을 로딩한다.
            try {
                final ArrayList<ExplorerFileItem> imageList = ZipLoader.load(this, path, new ZipLoader.ZipLoaderListener() {
                    @Override
                    public void onSuccess(int i) {
                        if(i == page) {
                            pager.setCurrentItem(page);
//                            Log.d("ZipActivity", "setCurrentItem "+i);
                        }
                    }

                    @Override
                    public void onFail() {

                    }
                });
                pagerAdapter.setImageList(imageList);
                pager.setAdapter(pagerAdapter);
//                Log.d("ZipActivity", "pager init");

            } catch (ZipException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
    }
}
