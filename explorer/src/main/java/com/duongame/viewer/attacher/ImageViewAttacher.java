package com.duongame.viewer.attacher;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.duongame.viewer.activity.PagerActivity;
import com.duongame.viewer.listener.PagerOnTouchListener;
import com.github.chrisbanes.photoview.PhotoViewAttacher;

/**
 * Created by js296 on 2017-07-05.
 */

public class ImageViewAttacher extends PhotoViewAttacher {
    private final static String TAG = ImageViewAttacher.class.getSimpleName();
    PagerOnTouchListener mPagerOnTouchListener;
    ImageView imageView;
    boolean isLayoutInitialized = false;

    public ImageViewAttacher(ImageView imageView) {
        super(imageView);
        this.imageView = imageView;
    }

    public void setActivity(PagerActivity activity) {
        mPagerOnTouchListener = new PagerOnTouchListener(activity);
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        boolean ret = super.onTouch(v, ev);
        if (mPagerOnTouchListener != null)
            mPagerOnTouchListener.handleTouch(v, ev);
        return ret;
    }

    // 확대된 상태에서 menu를 호출하게 되면 다시 원본으로 축소된다.
    // 이유는 같은 크기인데도 layout change로 matrix가 초기화 되기 때문이다.
    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (imageView == null || imageView.getDrawable() == null)
            return;

        if (!isLayoutInitialized) {
            super.onLayoutChange(v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom);
            isLayoutInitialized = true;
        }
    }
}