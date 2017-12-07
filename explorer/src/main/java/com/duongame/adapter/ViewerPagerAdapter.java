package com.duongame.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;

import com.duongame.activity.PagerActivity;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-17.
 */
//TODO: Zip파일 양면 읽기용으로 상속받아야 함. Pdf 파일 버전으로 따로 만들어야 함.
//TODO: 나중에 FragmentStatePagerAdapter로 변경해야 함
public abstract class ViewerPagerAdapter extends PagerAdapter {
    private static final String TAG = "ViewerPagerAdapter";

    protected ArrayList<ExplorerItem> imageList;
    protected PagerActivity context;
    protected boolean exifRotation = true;

    public ViewerPagerAdapter(PagerActivity context) {
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

        return imageList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
