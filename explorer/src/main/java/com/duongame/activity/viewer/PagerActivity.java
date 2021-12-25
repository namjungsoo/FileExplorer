package com.duongame.activity.viewer;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.duongame.BuildConfig;
import com.duongame.R;
import com.duongame.adapter.ViewerPagerAdapter;
import com.duongame.bitmap.BitmapCacheManager;
import com.duongame.db.BookLoader;
import com.duongame.file.FileHelper;
import com.duongame.helper.AlertHelper;
import com.duongame.helper.AppHelper;
import com.duongame.helper.PreferenceHelper;
import com.duongame.listener.PagerOnTouchListener;
import com.duongame.view.ViewPagerEx;
import com.felipecsl.gifimageview.library.GifImageView;

import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

/**
 * Created by namjungsoo on 2016-11-19.
 */

// 지원 목록: Photo, Pdf, Zip
// +전체화면
//
// +좌우 view pager
// +하단 toolbox
public class PagerActivity extends BaseViewerActivity {
    private static final String TAG = PagerActivity.class.getSimpleName();
    private final static int AUTO_PAGING = 1;
    private final static int GOTO_NEXTBOOK = 2;

    public final static int SEC_TO_MS = 1000;
    public final static int AUTO_SEC_MAX = 10;

    // 파일의 정보
    protected String path;
    protected String name;
    protected long size;// zip 파일의 용량

    protected ViewPagerEx pager;
    protected ViewerPagerAdapter pagerAdapter;
    protected boolean isPagerIdle = true;

    private GifImageView gifImageView;

    private int autoTime, lastAutoTime;
    private TextView textAutoTime;
    private Button btnPlusTime, btnMinusTime;
    private Timer timer;

    protected boolean isGoingNextBook;
    protected String nextBook;

    static class PagingInfo {
        int page;
        boolean smoothScroll;
        ViewPagerEx pager;
        PagerActivity activity;
    }

    static class TimerHandler extends Handler {
        public void handleMessage(Message msg) {
            PagingInfo info = (PagingInfo) msg.obj;
            if (msg.what == AUTO_PAGING) {
                info.pager.setCurrentItem(info.page, info.smoothScroll);
            } else if (msg.what == GOTO_NEXTBOOK) {
                info.activity.openNextBook();
            }
        }
    }

    Handler handler = new TimerHandler();

    protected void openNextBookWithPopup() {
        isGoingNextBook = true;

        // 팝업을 띄운다.
        // 확인시 현재 위치에서 Activity를 재시작 한다.
        String fileName = FileHelper.getFileName(nextBook);
        @SuppressLint({"StringFormatInvalid", "LocalSuppress"}) String message = String.format(getString(R.string.msg_next_book), fileName);

        if (BuildConfig.SHOW_AD && !isAdRemoveReward()) {
            AlertHelper.INSTANCE.showAlertWithAd(this,
                    AppHelper.INSTANCE.getAppName(),
                    message,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            BookLoader.openNextBook(PagerActivity.this, nextBook);
                        }
                    }, new DialogInterface.OnClickListener() {// 취소일 경우 액티비티 닫음
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PagerActivity.this.finish();
                        }
                    }, null
            );
        } else {
            AlertHelper.INSTANCE.showAlert(this,
                    AppHelper.INSTANCE.getAppName(),
                    message,
                    null,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            BookLoader.openNextBook(PagerActivity.this, nextBook);
                        }
                    }, new DialogInterface.OnClickListener() {// 취소일 경우 액티비티 닫음
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PagerActivity.this.finish();
                        }
                    }, null
            );
        }

        // 옵션을 어떻게 넣을지 확인한다.
        isGoingNextBook = false;
    }

    @Override
    protected void updateNightMode() {
        super.updateNightMode();

        if (pager == null)
            return;

        int count = pager.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = pager.getChildAt(i);
            if (view == null)
                continue;
            ImageView imageView = view.findViewById(R.id.image_viewer);
            if (imageView == null)
                continue;

            pagerAdapter.updateColorFilter(imageView);
        }
    }

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

    boolean getSmoothScroll() {
        // smooth 연산
        boolean smoothScroll = true;
        try {
            if (PreferenceHelper.INSTANCE.getPagingAnimationDisabled()) {
                smoothScroll = false;
            }
        } catch (NullPointerException e) {

        }
        return smoothScroll;
    }

    void resumeTimer() {
        if (getFullscreen() && autoTime > 0 && lastAutoTime != autoTime) {// 이제 타이머를 시작
            Timber.e("resumeTimer autoTime=" + autoTime);

            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    int current = pager.getCurrentItem();

                    //TODO: 마지막 페이지
                    PagingInfo info = new PagingInfo();
                    Message msg = new Message();
                    if (current == pagerAdapter.getCount() - 1) {
                        info.activity = PagerActivity.this;
                        msg.obj = info;
                        msg.what = GOTO_NEXTBOOK;
                    } else {
                        info.page = current + 1;
                        info.smoothScroll = getSmoothScroll();
                        info.pager = pager;
                        msg.obj = info;
                        msg.what = AUTO_PAGING;
                    }
                    handler.sendMessage(msg);
                }
            }, autoTime * SEC_TO_MS, autoTime * SEC_TO_MS);

            lastAutoTime = autoTime;
        }
    }

    void pauseTimer() {
        if (timer != null)
            timer.cancel();

        lastAutoTime = 0;// 초기화 시켜준다.
//        timer = new Timer();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Timber.e("onResume");
        resumeTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();

        pauseTimer();
    }

    @Override
    protected void updateFullscreen(boolean isFullscreen) {
        if (isFullscreen) {// 전체화면으로 돌아가면 timer 발동
            resumeTimer();
        } else {
            pauseTimer();
        }
    }

    void updateAutoTime(boolean updatePreference) {
        textAutoTime.setText(String.valueOf(autoTime));

        if (updatePreference) {
            PreferenceHelper.INSTANCE.setAutoPagingTime(autoTime);
        }
    }

    // 페이징 UI 초기화
    // Fullscreen이 false되는 시점에 timer를 on
    void initAutoPagingUI() {
        textAutoTime = findViewById(R.id.auto_time);
        textAutoTime.setVisibility(View.VISIBLE);

        lastAutoTime = autoTime;
        autoTime = PreferenceHelper.INSTANCE.getAutoPagingTime();
        updateAutoTime(false);

        btnPlusTime = findViewById(R.id.plus_time);
        btnPlusTime.setVisibility(View.VISIBLE);
        btnPlusTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (autoTime < AUTO_SEC_MAX) {
                    lastAutoTime = autoTime;
                    autoTime++;
                    updateAutoTime(true);
                }
            }
        });

        btnMinusTime = findViewById(R.id.minus_time);
        btnMinusTime.setVisibility(View.VISIBLE);
        btnMinusTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (autoTime > 0) {
                    lastAutoTime = autoTime;
                    autoTime--;
                    updateAutoTime(true);
                }
            }
        });
    }

    @Override
    protected void initToolBox() {
        super.initToolBox();

        initAutoPagingUI();

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

                Timber.e("setCurrentItem");
                pager.setCurrentItem(page, false);
            }
        });

        leftPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = pager.getCurrentItem();
                if (current > 0) {
                    boolean smooth = getSmoothScroll();
                    pager.setCurrentItem(current - 1, smooth);
                }
            }
        });

        rightPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = pager.getCurrentItem();
                if (current == pagerAdapter.getCount() - 1) {
                    openNextBook();
                } else {
                    boolean smooth = getSmoothScroll();
                    pager.setCurrentItem(current + 1, smooth);
                }
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

    public void openNextBook() {
        // 마지막 페이지를 드래깅 하고 잇는 것임
        Timber.w("onPageScrolled last page dragging");

        //TODO: PDF와 ZIP에 대해서만 다음 책을 읽을수 있다.
    }

    protected void initPagerListeners() {
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            int lastState;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Timber.w("onPageScrolled position=" + position + " positionOffset=" + positionOffset + " positionOffsetPixels=" + positionOffsetPixels);

                if (pagerAdapter == null) {
                    return;
                }

                if (pagerAdapter.getCount() == position + 1) {
                    if (lastState == ViewPager.SCROLL_STATE_DRAGGING) {
                        openNextBook();
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
                Timber.w("onPageSelected position=" + position);
                updateScrollInfo(position);
                updateName(position);
                updateInfo(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Timber.w("onPageScrollStateChanged state=" + state);
                switch (state) {
                    case ViewPager.SCROLL_STATE_IDLE:
                        isPagerIdle = true;
                        break;
                    case ViewPager.SCROLL_STATE_DRAGGING:
                    case ViewPager.SCROLL_STATE_SETTLING:
                        isPagerIdle = false;
                        break;
                }
                lastState = state;
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
