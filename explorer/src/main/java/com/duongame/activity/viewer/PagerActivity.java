package com.duongame.activity.viewer;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.duongame.R;
import com.duongame.adapter.ViewerPagerAdapter;
import com.duongame.bitmap.BitmapCacheManager;
import com.duongame.helper.JLog;
import com.duongame.listener.PagerOnTouchListener;
import com.felipecsl.gifimageview.library.GifImageView;

/**
 * Created by namjungsoo on 2016-11-19.
 */

// 지원 목록: Photo, Pdf, Zip
// +전체화면
//
// +뷰페이저
// +하단 툴박스
public class PagerActivity extends BaseViewerActivity {
    // 파일의 정보
    protected String path;
    protected String name;
    protected long size;// zip 파일의 용량

    protected ViewPager pager;
    protected ViewerPagerAdapter pagerAdapter;
    protected boolean isPagerIdle = true;

    private GifImageView gifImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewResId = R.layout.activity_pager;
        super.onCreate(savedInstanceState);

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
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                final int page = seekBar.getProgress();

                int current = pager.getCurrentItem();
                if (Math.abs(current - page) > 2) {
                    // 모든 로딩 중인 태스크를 정리하고 비트맵을 리사이클을 한다.
                    pagerAdapter.stopAllTasks();

                    // ImageView bitmap을 전부 null로 셋팅한다.
                    int count = pager.getChildCount();
                    for (int i = 0; i < count; i++) {
                        View view = pager.getChildAt(i);
                        if (view == null)
                            continue;
                        ImageView imageView = view.findViewById(R.id.image_viewer);
                        if (imageView == null)
                            continue;

                        imageView.setImageBitmap(null);
                    }

                    // 모든 캐쉬 비트맵을 정리한다.
                    BitmapCacheManager.removeAllPages();
                    BitmapCacheManager.removeAllBitmaps();
                }

                JLog.e("PagerActivity", "setCurrentItem");
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
        pager = findViewById(R.id.pager);
        pagerAdapter = createPagerAdapter();
//        pager.setOffscreenPageLimit(OFFLINE_PAGE_LIMIT);
    }

    protected void updateScrollInfo(int position) {
        final int count = pagerAdapter.getCount();
        textPage.setText((position + 1) + "/" + count);

        seekPage.setMax(count - 1);

        // 이미지가 1개일 경우 처리
        if (position == 0 && count == 1) {
            seekPage.setProgress(count);
            seekPage.setEnabled(false);
        } else {
            seekPage.setProgress(position);
            seekPage.setEnabled(true);
        }
    }

    public void updateInfo(int position) {

    }

    protected void initPagerListeners() {
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                textName.setText(pagerAdapter.getImageList().get(position).name);
            }

            @Override
            public void onPageSelected(int position) {
                updateScrollInfo(position);
                updateName(position);
                updateInfo(position);
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

    // startAnimation은 외부에서 수행함
    public void setGifImageView(GifImageView gifImageView) {
//        if(this.gifImageView != null)
//            this.gifImageView.stopAnimation();
        this.gifImageView = gifImageView;
    }

    public GifImageView getGifImageView() {
        return gifImageView;
    }

    public void stopGifAnimation() {
        if (gifImageView != null) {
            gifImageView.stopAnimation();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

//        // 종료시에 현재 GIF가 있으면 stop 해줌
//        stopGifAnimation();
    }
}
