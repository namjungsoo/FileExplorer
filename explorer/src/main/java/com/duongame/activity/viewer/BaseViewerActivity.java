package com.duongame.activity.viewer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.duongame.BuildConfig;
import com.duongame.R;
import com.duongame.activity.BaseActivity;
import com.duongame.bitmap.BitmapCacheManager;
import com.duongame.helper.JLog;
import com.duongame.manager.AdBannerManager;
import com.google.android.gms.ads.AdView;

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
        JLog.e(TAG, "onDestroy removeAllBitmaps");
        BitmapCacheManager.removeAllPages();
        JLog.e(TAG, "onDestroy removeAllPages");

        // 전면 광고 노출
        showInterstitialAd(null);

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (adView != null)
            adView.pause();

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (adView != null) {
            adView.resume();
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
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }


        isFullscreen = fullscreen;

        // 툴박스 보이기
        //TODO: 알파 애니메이션은 나중에 하자
        if (!fullscreen) {
            //TEST
            bottomPanel.setVisibility(View.VISIBLE);
            topPanel.setVisibility(View.VISIBLE);
        } else {
            bottomPanel.setVisibility(View.INVISIBLE);
            topPanel.setVisibility(View.INVISIBLE);
        }
    }

    protected void updateName(int i) {
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
