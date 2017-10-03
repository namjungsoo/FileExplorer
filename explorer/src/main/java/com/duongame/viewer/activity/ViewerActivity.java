package com.duongame.viewer.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.duongame.BuildConfig;
import com.duongame.R;
import com.duongame.comicz.AnalyticsApplication;
import com.duongame.explorer.bitmap.BitmapCacheManager;
import com.duongame.explorer.helper.PreferenceHelper;
import com.duongame.explorer.manager.AdBannerManager;
import com.duongame.explorer.manager.AdInterstitialManager;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by namjungsoo on 2016-11-16.
 */

// 전체 화면을 지원한다.
public class ViewerActivity extends AppCompatActivity {
    private static final String TAG = "ComicViewerActivity";

    private FirebaseAnalytics mFirebaseAnalytics;
    private Tracker mTracker;

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

    private View mainView;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initContentView();

        initActionBar();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

    }

    protected void initContentView() {
        if (BuildConfig.SHOW_AD) {
            final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mainView = inflater.inflate(contentViewResId, null, true);

            final RelativeLayout layout = new RelativeLayout(this);
            layout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

            adView = AdBannerManager.getAdBannerView(1);

            // adview layout params
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            adView.setLayoutParams(params);

            // mainview layout params
            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.ABOVE, adView.getId());
            mainView.setLayoutParams(params);

            layout.addView(adView);
            layout.addView(mainView);

            setContentView(layout);

        } else {
            setContentView(contentViewResId);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ((ViewGroup) adView.getParent()).removeView(adView);
        BitmapCacheManager.recycleBitmap();
        BitmapCacheManager.recyclePage();

        // 전면 광고 노출
        showInterstitialAd();
    }

    void showInterstitialAd() {
        final int count = PreferenceHelper.getExitAdCount(this);

        // 2번중에 1번을 띄워준다.
        if (count % 2 == 1) {// 전면 팝업후 종료 팝업
            if (!AdInterstitialManager.showAd(this, AdInterstitialManager.MODE_EXIT)) {
                // 보여지지 않았다면 insterstitial후 카운트 증가하지 않음
                return;
            }
        }
        PreferenceHelper.setExitAdCount(this, count + 1);
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
        textName = (TextView) findViewById(R.id.text_name);

        textInfo = (TextView) findViewById(R.id.text_info);
        textSize = (TextView) findViewById(R.id.text_size);

        topPanel = (LinearLayout) findViewById(R.id.panel_top);
        topPanel.setY(getStatusBarHeight());

        bottomPanel = (LinearLayout) findViewById(R.id.panel_bottom);
        textPage = (TextView) findViewById(R.id.text_page);
        seekPage = (SeekBar) findViewById(R.id.seek_page);

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

//        int height = getNavigationBarHeight();
//        bottomPanel.setY(bottomPanel.getY() - height);
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
