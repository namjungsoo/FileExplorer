package com.duongame.explorer.activity;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.duongame.explorer.R;
import com.duongame.explorer.bitmap.BitmapCacheManager;

/**
 * Created by namjungsoo on 2016-11-16.
 */

// 전체 화면을 지원한다.
public class ViewerActivity extends AppCompatActivity {
    private static final String TAG = "ComicViewerActivity";

    protected boolean isFullscreen = true;
    protected ActionBar actionBar;

    // toolbox
    protected TextView textName;
    //    protected TextView textPath;
    protected LinearLayout toolBox;
    protected TextView textPage;
    protected SeekBar seekPage;

    // touch
    protected boolean isPagerIdle = true;
    protected boolean isBeingDragged = false;
    protected PointF lastMotionPt = new PointF();
    protected PointF initialMotionPt = new PointF();

    // configuration
    protected VelocityTracker velocityTracker = null;
    protected int touchSlop = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initActionBar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BitmapCacheManager.recycleBitmap();
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
//        textPath = (TextView)findViewById(R.id.text_path);
        textName = (TextView)findViewById(R.id.text_name);
        textName.setY(getStatusBarHeight());

        toolBox = (LinearLayout) findViewById(R.id.tool_box);
        textPage = (TextView) findViewById(R.id.text_page);
        seekPage = (SeekBar) findViewById(R.id.seek_page);

        int height = getNavigationBarHeight();
        toolBox.setY(toolBox.getY() - height);
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

        // 툴박스 보이기
        //TODO: 알파 애니메이션은 나중에 하자
        if (!fullscreen) {
            toolBox.setVisibility(View.VISIBLE);
            textName.setVisibility(View.VISIBLE);
//            textPath.setVisibility(View.VISIBLE);
        } else {
            toolBox.setVisibility(View.INVISIBLE);
            textName.setVisibility(View.INVISIBLE);
//            textPath.setVisibility(View.INVISIBLE);
        }
    }

    protected void setName(int i) {
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

    protected void startDragXIfNeeded(MotionEvent ev) {
        final float x = ev.getX(0);
        final float xSignedDiff = x - initialMotionPt.x;
        final float xDiff = Math.abs(xSignedDiff);
        if (xDiff < touchSlop) {
            isBeingDragged = false;
            return;
        }
        isBeingDragged = true;
    }

    protected void startDragYIfNeeded(MotionEvent ev) {
        final float y = ev.getY(0);
        final float ySignedDiff = y - initialMotionPt.y;
        final float yDiff = Math.abs(ySignedDiff);
        if (yDiff < touchSlop) {
            isBeingDragged = false;
            return;
        }
        isBeingDragged = true;
    }

}