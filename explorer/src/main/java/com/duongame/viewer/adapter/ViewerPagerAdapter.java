package com.duongame.viewer.adapter;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.view.View;

import com.duongame.explorer.adapter.ExplorerItem;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-17.
 */
//TODO: Zip파일 양면 읽기용으로 상속받아야 함. Pdf 파일 버전으로 따로 만들어야 함.
public abstract class ViewerPagerAdapter extends PagerAdapter {
    private static final String TAG = "ViewerPagerAdapter";

    protected ArrayList<ExplorerItem> imageList;
    protected Activity context;
    protected boolean exifRotation = true;

    public ViewerPagerAdapter(Activity context) {
        this.context = context;
    }

    public void setImageList(ArrayList<ExplorerItem> imageList) {
        this.imageList = imageList;
    }

    public ArrayList<ExplorerItem> getImageList() {
        return imageList;
    }

    public abstract void stopAllTasks();

    public void setExifRotation(boolean rotation) {
        exifRotation = rotation;
    }

    @Override
    public int getCount() {
        if (imageList == null)
            return 0;

//        Log.d(TAG, "getCount="+imageList.size());
        return imageList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}