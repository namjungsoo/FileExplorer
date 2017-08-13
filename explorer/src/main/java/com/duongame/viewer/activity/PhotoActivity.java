package com.duongame.viewer.activity;

import android.content.Intent;
import android.os.Bundle;

import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.helper.FileHelper;
import com.duongame.explorer.manager.ExplorerManager;
import com.duongame.viewer.adapter.PhotoPagerAdapter;
import com.duongame.viewer.adapter.ViewerPagerAdapter;

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

    protected ArrayList<ExplorerItem> getImageList() {
        return ExplorerManager.getImageList();
    }

    @Override
    protected ViewerPagerAdapter createPagerAdapter() {
        return new PhotoPagerAdapter(this);
    }

    private void initPagerAdapter() {
        final ArrayList<ExplorerItem> imageList = getImageList();
        pagerAdapter.setImageList(imageList);
        pager.setAdapter(pagerAdapter);

        seekPage.setMax(imageList.size());
    }

    protected void processIntent() {
        final ArrayList<ExplorerItem> imageList = getImageList();
        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();
        if (extras != null) {
            name = extras.getString("name");
            path = extras.getString("path");
            size = extras.getLong("size");

            textName.setText(name);

            // 이미지 파일 리스트에서 현재 위치를 찾자
            int current = 0;
            for (int i = 0; i < imageList.size(); i++) {
                if (imageList.get(i).name.equals(name)) {
                    current = i;
                    break;
                }
            }

            pager.setCurrentItem(current);

            updateName(current);
            updateInfo(current);
            updateScrollInfo(current);
        }
    }

    @Override
    public void updateInfo(int position) {
        if(pager.getCurrentItem() == position) {
            ExplorerItem item = pagerAdapter.getImageList().get(position);
            textInfo.setText("" + item.width + " x " + item.height);
            textSize.setText(FileHelper.getMinimizedSize(item.size));
        }
    }
}
