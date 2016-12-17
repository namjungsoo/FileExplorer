package com.duongame.explorer.activity;

import android.content.Intent;
import android.os.Bundle;

import com.duongame.explorer.adapter.ExplorerFileItem;
import com.duongame.explorer.bitmap.ZipLoader;

import net.lingala.zip4j.exception.ZipException;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-19.
 */

public class ZipActivity extends PagerActivity {
    private final static String TAG = "ZipActivity";

    private boolean zipExtractCompleted = false;
    private ZipLoader.ZipLoaderListener listener = new ZipLoader.ZipLoaderListener() {
        @Override
        public void onSuccess(int i, ArrayList<ExplorerFileItem> zipImageList) {
//            Log.d(TAG, "onSuccess="+i);
            ArrayList<ExplorerFileItem> imageList = (ArrayList<ExplorerFileItem> )zipImageList.clone();

//            pagerAdapter.setImageList(imageList);
//            pagerAdapter.notifyDataSetChanged();
        }

        @Override
        public void onFail(int i, String name) {

        }

        @Override
        public void onFinish(ArrayList<ExplorerFileItem> zipImageList) {
            // 체크해놓고 나중에 파일을 지우지 말자
            zipExtractCompleted = true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pagerAdapter.setExifRotation(false);
        pagerAdapter.setSplitBitmap(true);
        try {
            processIntent();
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }

    protected ArrayList<ExplorerFileItem> getImageList() {
        return null;
    }

    @Override
    protected void setName(int i) {
        textName.setText(name);
    }

    protected void processIntent() throws ZipException {
        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();
        if (extras != null) {
            final int page = extras.getInt("page");
            path = extras.getString("path");
            name = extras.getString("name");

            // initPagerAdapter의 기능이다.
            pager.setAdapter(pagerAdapter);// setAdapter이후에 imageList를 변경하면 항상 notify해주어야 한다.

            // zip 파일을 로딩한다.
            final ZipLoader zipLoader = new ZipLoader();
            final ArrayList<ExplorerFileItem> imageList = zipLoader.load(this, path, listener, false);
            pagerAdapter.setImageList(imageList);
            pagerAdapter.notifyDataSetChanged();

            seekPage.setMax(imageList.size());
            seekPage.setProgress(1);


//            try {
//                final ArrayList<ExplorerFileItem> imageList = zipLoader.load(this, path, new ZipLoader.ZipLoaderListener() {
//                    @Override
//                    public void onSuccess(int i, ArrayList<ExplorerFileItem> zipImageList) {
//                        Log.d(TAG, "onSuccess="+i);
////                        if (i == page) {
////                            pager.setCurrentItem(page);
////                            Log.d(TAG, "setCurrentItem=" + i);
////                        }
////                        pagerAdapter.setMaxIndex(i);
//
//                        pager.setCurrentItem(0);
//
////                        // 파일리스트가 변경되는대로 업데이트 해준다.
////                        pagerAdapter.setImageList(zipImageList);
////                        pagerAdapter.notifyDataSetChanged();
////
////                        // 리스트 갯수가 최대값이다.
////                        seekPage.setMax(zipImageList.size());
//                    }
//
//                    @Override
//                    public void onFail(int i, String name) {
//                        Log.e(TAG, "onFail i=" + i + " name=" + name);
//                    }
//
//                    @Override
//                    public void onFinish(ArrayList<ExplorerFileItem> zipImageList) {
//                        // zip 파일 압축풀이가 끝났으면 imageList를 갱신한다.
////                        Log.d(TAG, "zipImageList.size=" + zipImageList.size());
////
////                        pagerAdapter.setImageList(zipImageList);
////                        pagerAdapter.notifyDataSetChanged();
////
////                        seekPage.setMax(zipImageList.size());
////                        seekPage.setProgress(1);
//
//                        // 체크해놓고 나중에 파일을 지우지 말자
//                        zipExtractCompleted = true;
//                    }
//                }, false);
//
////                Log.d(TAG, "imageList.size=" + imageList.size());
////
//                pagerAdapter.setImageList(imageList);
//
//                seekPage.setMax(imageList.size());
//                seekPage.setProgress(1);
//            } catch (ZipException e) {
//                e.printStackTrace();
//            }
        }
    }
}
