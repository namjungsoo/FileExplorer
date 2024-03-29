package com.duongame.adapter;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.View;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;

import com.duongame.MainApplication;
import com.duongame.activity.viewer.PagerActivity;

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

    ColorMatrix getColorMatrix() {
        ColorMatrix colorMatrix = new ColorMatrix(new float[]{
                1, 0, 0, 0, 0,
                0, 1, 0, 0, 0,
                0, 0, 1, 0, 0,
                0, 0, 0, 1, 0});

        try {
            if (MainApplication.getInstance(context).isNightMode()) {
                colorMatrix = new ColorMatrix(new float[]{
                        -1, 0, 0, 0, 192,
                        0, -1, 0, 0, 192,
                        0, 0, -1, 0, 192,
                        0, 0, 0, 1, 0});
            }
            return colorMatrix;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public void updateColorFilter(ImageView imageView) {
        if (imageView == null)
            return;

        imageView.setColorFilter(new ColorMatrixColorFilter(getColorMatrix()));
    }
}
