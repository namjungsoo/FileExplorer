package com.duongame.fileexplorer.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.VelocityTracker;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.duongame.fileexplorer.R;

/**
 * Created by namjungsoo on 2016-11-16.
 */

public class ViewerActivity extends AppCompatActivity {
    private static final String TAG = "ComicViewerActivity";

    protected boolean isFullscreen = true;
    private ActionBar actionBar;

    // configuration
    protected VelocityTracker mVelocityTracker = null;
    protected int mMaxVelocity = 0;
    protected int mSnapVelocity = 0;
    protected int mTouchSlop = 0;
    protected static final int VELOCITY_UNIT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initActionBar();
        // 전체 화면으로 들어감
        setFullscreen(true);
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

    /**
     * Detects and toggles immersive mode (also known as "hidey bar" mode).
     */
    //TODO: 4.3, 4.4에서 테스트 해볼것
    protected void setFullscreen(boolean fullscreen) {
        if(fullscreen)
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
        else
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);


        isFullscreen = fullscreen;
    }
}
