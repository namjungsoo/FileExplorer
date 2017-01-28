package com.duongame.viewer.activity;

import android.content.Intent;
import android.os.Bundle;

import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.manager.ExplorerManager;
import com.duongame.viewer.adapter.ViewerPagerAdapter;
import com.duongame.viewer.adapter.PhotoPagerAdapter;
import com.felipecsl.gifimageview.library.GifImageView;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-18.
 */

public class PhotoActivity extends PagerActivity {
    private final static String TAG = "PhotoActivity";
    private GifImageView gifImageView;

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
            textName.setText(name);
//            textPath.setText(path);

            int item = 0;
            for (int i = 0; i < imageList.size(); i++) {
                if (imageList.get(i).name.equals(name)) {
                    item = i;
                }
            }
            pager.setCurrentItem(item);
        }
    }

    public void setGifImageView(GifImageView gifImageView) {
        if(this.gifImageView != null)
            this.gifImageView.stopAnimation();

        this.gifImageView = gifImageView;
        if(this.gifImageView != null) {
            this.gifImageView.startAnimation();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(gifImageView != null)
            gifImageView.stopAnimation();
    }
}
