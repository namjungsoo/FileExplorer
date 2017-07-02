package com.duongame.viewer.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.SeekBar;

import com.duongame.R;
import com.duongame.explorer.bitmap.BitmapCacheManager;
import com.duongame.viewer.adapter.ViewerPagerAdapter;
import com.duongame.viewer.listener.PagerOnTouchListener;

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
    private final static int OFFLINE_PAGE_LIMIT = 1;

    // 파일의 정보
    protected String path;
    protected String name;

    protected ViewPager pager;
    protected ViewerPagerAdapter pagerAdapter;
    protected boolean isPagerIdle = true;

//    private GifImageView gifImageView;

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
                Log.d(TAG, "onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStopTrackingTouch");
                final int page = seekBar.getProgress();

                int current = pager.getCurrentItem();
                int diff = Math.abs(current - page);
                Log.d(TAG, "onStopTrackingTouch current=" + current + " diff=" + diff);


                if (Math.abs(current - page) > 2) {
                    // 모든 로딩 중인 태스크를 정리하고 비트맵을 리사이클을 한다.
                    pagerAdapter.stopAllTasks();
                    BitmapCacheManager.recyclePage();
                    BitmapCacheManager.recycleBitmap();
                    Log.d(TAG, "onStopTrackingTouch recycle");
                }

                pager.setCurrentItem(page, false);
            }
        });
    }

    public ViewPager getPager() {
        return pager;
    }

    public boolean getPagerIdle() {
        return isPagerIdle;
    }

    public ViewerPagerAdapter getPagerAdapter() {
        return pagerAdapter;
    }

    protected ViewerPagerAdapter createPagerAdapter() {
        return null;
    }

    protected void initPager() {
        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = createPagerAdapter();
//        pager.setOffscreenPageLimit(OFFLINE_PAGE_LIMIT);
    }

    protected void updateScrollInfo(int position) {
        Log.d(TAG, "updateScrollInfo=" + position);
        textPage.setText((position + 1) + "/" + pagerAdapter.getCount());

        seekPage.setMax(pagerAdapter.getCount() - 1);
        seekPage.setProgress(position);
    }

    protected void initPagerListeners() {
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                Log.e(TAG, "onPageScrolled position=" + position);
//                textName.setText(pagerAdapter.getImageList().get(position).name);
            }

            @Override
            public void onPageSelected(int position) {
                Log.w(TAG, "onPageSelected position=" + position);

                updateScrollInfo(position);
                updateName(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

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

        pager.setOnTouchListener(new PagerOnTouchListener(this));
    }

    @Override
    protected void updateName(int i) {
        textName.setText(pagerAdapter.getImageList().get(i).name);
    }

//    // startAnimation은 외부에서 수행함
//    public void setGifImageView(GifImageView gifImageView) {
////        if(this.gifImageView != null)
////            this.gifImageView.stopAnimation();
//        this.gifImageView = gifImageView;
//    }
//
//    public GifImageView getGifImageView() {
//        return gifImageView;
//    }
//
//    public void stopGifAnimation() {
//        if (gifImageView != null) {
//            Log.w(TAG, "stopGifAnimation");
//            gifImageView.stopAnimation();
//        }
//    }

    @Override
    public void onStop() {
        super.onStop();

//        // 종료시에 현재 GIF가 있으면 stop 해줌
//        stopGifAnimation();
    }
}
