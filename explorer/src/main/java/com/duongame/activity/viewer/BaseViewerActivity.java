package com.duongame.activity.viewer;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;

import com.duongame.BuildConfig;
import com.duongame.App;
import com.duongame.R;
import com.duongame.activity.BaseActivity;
import com.duongame.activity.SettingsActivity;
import com.duongame.bitmap.BitmapCacheManager;
import com.duongame.manager.AdBannerManager;
import com.google.android.gms.ads.AdView;

import timber.log.Timber;

/**
 * Created by namjungsoo on 2016-11-16.
 */

// 전체 화면을 지원한다.
public class BaseViewerActivity extends BaseActivity {
    private static final String TAG = "ComicViewerActivity";

    protected boolean isFullscreen = true;
    protected ActionBar actionBar;

    // bottom panel
    protected TextView textName;

    protected LinearLayout bottomPanel;
    protected LinearLayout topPanel;

    protected TextView textPage;
    protected TextView textInfo;
    protected TextView textSize;

    protected SeekBar seekPage;
    protected int contentViewResId;

    //private View mainView;
    private AdView adView;

    protected LinearLayout nightMode;
    protected LinearLayout pagingAnim;

    protected Button leftPage;
    protected Button rightPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initContentView();
        initActionBar();
    }

    protected void initContentView() {
        setContentView(contentViewResId);
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            ViewGroup vg = (ViewGroup) adView.getParent();
            if (vg != null) {
                vg.removeView(adView);
            }
            adView.removeAllViews();
            adView.destroy();
        }

        BitmapCacheManager.removeAllBitmaps();
        Timber.e("onDestroy removeAllBitmaps");
        BitmapCacheManager.removeAllPages();
        Timber.e("onDestroy removeAllPages");

        // 전면 광고 노출
        showInterstitialAd(null);

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (adView != null) {
            adView.pause();
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (adView != null) {
            adView.resume();
            // 광고 리워드 제거 시간 중인가?
            if(isAdRemoveReward()) {
                adView.setVisibility(View.GONE);
            } else {
                adView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void initActionBar() {
        actionBar = getSupportActionBar();
        if (actionBar == null)
            return;

        // 투명한 칼라는 액션바의 테마에 적용이 안되서 이렇게 나중에 바꾸어 준다
        actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryTransparent)));
        actionBar.hide();

        // 로고 버튼
        actionBar.setDisplayShowHomeEnabled(true);

        // Up 버튼
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }
    }

    // 현재 값을 읽어서 UI의 색상을 변경시킨다.
    protected void updateNightMode() {
        ImageView iv;
        TextView tv;

        iv = findViewById(R.id.img_night);
        tv = findViewById(R.id.text_night);
        if (App.getInstance(BaseViewerActivity.this).isNightMode()) {
            iv.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_orange_light));
            tv.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light));
        } else {
            iv.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
            tv.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        }
    }

    void updatePagingAnim() {
        ImageView iv;
        TextView tv;

        iv = findViewById(R.id.img_anim);
        tv = findViewById(R.id.text_anim);
        if (!App.getInstance(BaseViewerActivity.this).isPagingAnimationDisabled()) {
            iv.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_orange_light));
            tv.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light));
        } else {
            iv.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
            tv.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        }
    }

    protected void initToolBox() {
        textName = findViewById(R.id.text_name);

        textInfo = findViewById(R.id.text_info);
        textSize = findViewById(R.id.text_size);

        topPanel = findViewById(R.id.panel_top);
        topPanel.setY(getStatusBarHeight());

        bottomPanel = findViewById(R.id.panel_bottom);
        if (hasSoftKeys()) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) bottomPanel.getLayoutParams();
            lp.bottomMargin = getNavigationBarHeight();
            bottomPanel.requestLayout();
        }

        textPage = findViewById(R.id.text_page);
        seekPage = findViewById(R.id.seek_page);

        nightMode = findViewById(R.id.layout_night);
        pagingAnim = findViewById(R.id.layout_anim);

        nightMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 값을 반전시킨다.
                try {
                    App.getInstance(BaseViewerActivity.this).setNightMode(!App.getInstance(BaseViewerActivity.this).isNightMode());
                } catch (NullPointerException e) {

                }
                updateNightMode();
            }
        });
        updateNightMode();

        pagingAnim.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 값을 반전시킨다.
                try {
                    App.getInstance(BaseViewerActivity.this).setPagingAnimationDisabled(!App.getInstance(BaseViewerActivity.this).isPagingAnimationDisabled());
                } catch (NullPointerException e) {

                }
                updatePagingAnim();
            }
        });
        updatePagingAnim();

        //ADVIEW
        if (BuildConfig.SHOW_AD) {
            AdBannerManager.initBannerAd(this, 1);
            adView = AdBannerManager.getAdBannerView(1);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            adView.setLayoutParams(params);
            AdBannerManager.requestAd(1);
            bottomPanel.addView(adView);
        }

        seekPage.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ImageView settings = findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = SettingsActivity.getLocalIntent(BaseViewerActivity.this);
                startActivity(intent);
            }
        });

        leftPage = findViewById(R.id.left_page);
        rightPage = findViewById(R.id.right_page);
    }

    public boolean getFullscreen() {
        return isFullscreen;
    }

    /**
     * Detects and toggles immersive mode (also known as "hidey bar" mode).
     */
    //TODO: 4.3, 4.4에서 테스트 해볼것
    public void setFullscreen(boolean fullscreen) {
        if (fullscreen) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
            bottomPanel.setVisibility(View.INVISIBLE);
            topPanel.setVisibility(View.INVISIBLE);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            // 툴박스 보이기
            //TODO: 알파 애니메이션은 나중에 하자
            bottomPanel.setVisibility(View.VISIBLE);
            topPanel.setVisibility(View.VISIBLE);
        }

        isFullscreen = fullscreen;
        updateFullscreen(fullscreen);
    }

    protected void updateName(int i) {
    }

    protected void updateFullscreen(boolean isFullscreen) {
    }

    public boolean hasSoftKeys() {
        boolean hasSoftwareKeys = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            final Display d = getWindowManager().getDefaultDisplay();

            final DisplayMetrics realDisplayMetrics = new DisplayMetrics();
            d.getRealMetrics(realDisplayMetrics);

            final int realHeight = realDisplayMetrics.heightPixels;
            final int realWidth = realDisplayMetrics.widthPixels;

            final DisplayMetrics displayMetrics = new DisplayMetrics();
            d.getMetrics(displayMetrics);

            final int displayHeight = displayMetrics.heightPixels;
            final int displayWidth = displayMetrics.widthPixels;

            hasSoftwareKeys = (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
        } else {
            boolean hasMenuKey = ViewConfiguration.get(this).hasPermanentMenuKey();
            boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            hasSoftwareKeys = !hasMenuKey && !hasBackKey;
        }
        return hasSoftwareKeys;
    }

    protected int getNavigationBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    protected int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

}
