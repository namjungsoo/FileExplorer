package com.duongame.explorer.activity;

import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.duongame.explorer.R;
import com.duongame.explorer.adapter.ExplorerPagerAdapter;
import com.duongame.explorer.bitmap.BitmapCacheManager;

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
    protected ExplorerPagerAdapter pagerAdapter;

    protected TextView textName;
    protected TextView textPath;

    protected LinearLayout toolBox;
    protected TextView textPage;
    protected SeekBar seekPage;

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

        initToolBox();

        initPager();
        initPagerListeners();

        // 전체 화면으로 들어감
        setFullscreen(true);
    }

    protected int getNavigationBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
            return result;
        }
        return 0;
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    protected void setFullscreen(boolean fullscreen) {
        super.setFullscreen(fullscreen);

        // 툴박스 보이기
        //TODO: 알파 애니메이션은 나중에 하자
        if (!fullscreen) {
            toolBox.setVisibility(View.VISIBLE);
            textName.setVisibility(View.VISIBLE);
            textPath.setVisibility(View.VISIBLE);
        } else {
            toolBox.setVisibility(View.INVISIBLE);
            textName.setVisibility(View.INVISIBLE);
            textPath.setVisibility(View.INVISIBLE);
        }
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

    protected void initToolBox() {
        textPath = (TextView)findViewById(R.id.text_path);
        textName = (TextView)findViewById(R.id.text_name);
        textName.setY(getStatusBarHeight());

        toolBox = (LinearLayout) findViewById(R.id.tool_box);
        textPage = (TextView) findViewById(R.id.text_page);
        seekPage = (SeekBar) findViewById(R.id.seek_page);

        int height = getNavigationBarHeight();
        toolBox.setY(toolBox.getY() - height);

        seekPage.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //seekPage.setProgress(progress);
                //pager.setCurrentItem(progress-1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                final int page = seekBar.getProgress() - 1;
                pager.setCurrentItem(page, false);
            }
        });
    }

    protected void initPager() {
        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new ExplorerPagerAdapter(this);
    }

    protected void initPagerListeners() {
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                Log.d("PagerActivity", "onPageScrolled position=" + position);
                textPage.setText((position + 1) + "/" + pagerAdapter.getCount());
                seekPage.setProgress(position + 1);
                textName.setText(pagerAdapter.getImageList().get(position).name);
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
    protected void onDestroy() {
        super.onDestroy();
        BitmapCacheManager.recycleBitmap();
    }
}

