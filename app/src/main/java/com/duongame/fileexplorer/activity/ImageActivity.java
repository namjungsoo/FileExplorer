package com.duongame.fileexplorer.activity;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

import com.duongame.fileexplorer.helper.ExplorerSearcher;
import com.duongame.fileexplorer.R;
import com.duongame.fileexplorer.adapter.ExplorerFileItem;
import com.duongame.fileexplorer.adapter.ExplorerPagerAdapter;
import com.duongame.fileexplorer.bitmap.BitmapCacheManager;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-18.
 */

public class ImageActivity extends ViewerActivity {
    private final static String TAG = "ImageActivity";
    private ViewPager pager;
    private ExplorerPagerAdapter pagerAdapter;

    // touch
    private boolean isPagerIdle = true;
    private boolean isBeingDragged = false;
    private PointF lastMotionPt = new PointF();
    private PointF initialMotionPt = new PointF();

    // configuration
    protected VelocityTracker velocityTracker = null;
    protected int touchSlop = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new ExplorerPagerAdapter(this);

        initAdapter();
        initPagerListeners();
        processIntent();
    }

    private void startDragIfNeeded(MotionEvent ev) {
        final float x = ev.getX(0);
        final float xSignedDiff = x - initialMotionPt.x;
        final float xDiff = Math.abs(xSignedDiff);
        if (xDiff < touchSlop) {
            isBeingDragged = false;
            return;
        }
        isBeingDragged = true;
    }

    private void initPagerListeners() {
        final ViewConfiguration configuration = ViewConfiguration.get(this);
        touchSlop = configuration.getScaledTouchSlop() >> 1;

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                switch (state) {
                    case ViewPager.SCROLL_STATE_IDLE:
                        isPagerIdle = true;
                        break;
                    case ViewPager.SCROLL_STATE_DRAGGING:
                    case ViewPager.SCROLL_STATE_SETTLING:
                        isPagerIdle = false;
                        break;
                }
            }
        });

        pager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent ev) {
                Log.d(TAG, "onTouch");

                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                }
                velocityTracker.addMovement(ev);

                switch (ev.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        lastMotionPt.x = initialMotionPt.x = ev.getX(0);
                        lastMotionPt.y = initialMotionPt.y = ev.getY(0);
                        break;
                    }

                    case MotionEvent.ACTION_MOVE: {
                        if (!isBeingDragged) {
                            startDragIfNeeded(ev);
                        }
                        final float x = ev.getX(0);
                        final float y = ev.getY(0);
                        lastMotionPt.x = x;
                        lastMotionPt.y = y;
                        break;
                    }

                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP: {
                        if (velocityTracker != null) {
                            velocityTracker.recycle();
                            velocityTracker = null;
                        }
                        if (!isBeingDragged && isPagerIdle) {
                            setFullscreen(!isFullscreen);
                            return true;
                        } else {
                            isBeingDragged = false;
                        }
                        break;
                    }
                }
                return false;
            }
        });

    }

    private void initAdapter() {
        final ArrayList<ExplorerFileItem> imageList = ExplorerSearcher.getImageList();
        pagerAdapter.setImageList(imageList);
        pager.setAdapter(pagerAdapter);
    }

    private void processIntent() {
        final ArrayList<ExplorerFileItem> imageList = ExplorerSearcher.getImageList();
        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();
        if (extras != null) {
            final String name = extras.getString("name");

            int item = 0;
            for (int i = 0; i < imageList.size(); i++) {
                if (imageList.get(i).name.equals(name)) {
                    item = i;
                }
            }
            pager.setCurrentItem(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BitmapCacheManager.recycleBitmap();
    }
}
