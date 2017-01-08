package com.duongame.viewer.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.SeekBar;

import com.duongame.explorer.R;
import com.duongame.explorer.bitmap.BitmapCache;
import com.duongame.viewer.adapter.ViewerPagerAdapter;

/**
 * Created by namjungsoo on 2016-11-19.
 */

// 지원 목록: Photo, Pdf, Zip
// +전체화면
//
// +뷰페이저
// +하단 툴박스
public class PagerActivity extends ViewerActivity {
    private final static String TAG = "PagerActivity";

    protected String path;
    protected String name;
    protected ViewPager pager;
    protected ViewerPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);

        initToolBox();

        initPager();
        initPagerListeners();

        // 전체 화면으로 들어감
        setFullscreen(true);
    }

    @Override
    protected void initToolBox() {
        super.initToolBox();
        seekPage.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //seekPage.setProgress(progress);
                //pager.setCurrentItem(progress-1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d(TAG,"onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG,"onStopTrackingTouch");
                final int page = seekBar.getProgress() - 1;
                pagerAdapter.stopAllTasks();
                BitmapCache.recyclePage();
                BitmapCache.recycleBitmap();
                pager.setCurrentItem(page, false);
            }
        });
    }

    protected ViewerPagerAdapter createPagerAdapter() {
        return null;
    }

    protected void initPager() {
        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = createPagerAdapter();
    }

    protected void updateScrollInfo(int position) {
//        Log.d(TAG, "updateScrollInfo="+position);
        textPage.setText((position + 1) + "/" + pagerAdapter.getCount());

        seekPage.setMax(pagerAdapter.getCount());
        seekPage.setProgress(position + 1);
    }

    protected void initPagerListeners() {
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                Log.d("PagerActivity", "onPageScrolled position=" + position);
                updateScrollInfo(position);

                updateName(position);
//                textName.setText(pagerAdapter.getImageList().get(position).name);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

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
//                Log.d(TAG, "onTouch");

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
                            startDragXIfNeeded(ev);
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

                            // 터치 영역을 확인하여 좌/중/우를 확인하자.
                            int width = pager.getWidth();
                            int height = pager.getHeight();
//                            Log.d(TAG, "width="+width + " height="+height);

                            int left = width / 4;
                            int right = width * 3 / 4;

                            if (lastMotionPt.x < left) {
                                int page = pager.getCurrentItem();
                                if (page > 0)
                                    pager.setCurrentItem(page - 1, true);
                            } else if (lastMotionPt.x > right) {
                                //int count = pager.getChildCount();
                                int count = pagerAdapter.getCount();
                                int page = pager.getCurrentItem();
                                if (page < count + 1)
                                    pager.setCurrentItem(page + 1, true);
                            } else {
                                setFullscreen(!isFullscreen);
                            }
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

    @Override
    protected void updateName(int i) {
        textName.setText(pagerAdapter.getImageList().get(i).name);
    }

}

