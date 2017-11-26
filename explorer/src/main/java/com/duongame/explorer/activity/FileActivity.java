package com.duongame.explorer.activity;

import android.Manifest;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.duongame.AnalyticsApplication;
import com.duongame.BuildConfig;
import com.duongame.R;
import com.duongame.comicz.db.BookDB;
import com.duongame.explorer.bitmap.BitmapCacheManager;
import com.duongame.explorer.fragment.BaseFragment;
import com.duongame.explorer.fragment.ExplorerFragment;
import com.duongame.explorer.helper.ToastHelper;
import com.duongame.explorer.manager.AdBannerManager;
import com.duongame.explorer.manager.AdInterstitialManager;
import com.duongame.explorer.manager.ExplorerManager;
import com.duongame.explorer.manager.ReviewManager;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;

public class FileActivity extends BaseActivity {
    private final static String TAG = "ComicActivity";

    private FirebaseAnalytics mFirebaseAnalytics;
    private Tracker mTracker;

    // admob
    private View mainView;
    private AdView adView;

    private boolean showReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_file);

        AdBannerManager.init(this);
        AdInterstitialManager.init(this);

        initContentView();

        initToolbar();

        showReview = ReviewManager.checkReview(this);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        //TEST
//        FirebaseCrash.report(new Exception("My first Android non-fatal error"));

    }

    private void initContentView() {
        if (BuildConfig.SHOW_AD) {
            final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mainView = inflater.inflate(R.layout.activity_file, null, true);

            final RelativeLayout layout = new RelativeLayout(this);
            layout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

            adView = AdBannerManager.getAdBannerView(0);

            // adview layout params
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            adView.setLayoutParams(params);
            AdBannerManager.requestAd(0);

            // mainview layout params
            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.ABOVE, adView.getId());
            mainView.setLayoutParams(params);

            layout.addView(adView);
            layout.addView(mainView);

            setContentView(layout);
        } else {
            setContentView(R.layout.activity_file);
        }
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setTitleTextColor(Color.WHITE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        final String read = Manifest.permission.READ_EXTERNAL_STORAGE;
        final String write = Manifest.permission.WRITE_EXTERNAL_STORAGE;

        boolean readEnable = false;
        boolean writeEnable = false;

        for (int i = 0; i < permissions.length; i++) {
            if (read.equals(permissions[i]) && grantResults[i] == 0)
                readEnable = true;
            if (write.equals(permissions[i]) && grantResults[i] == 0)
                writeEnable = true;
        }

        if (readEnable && writeEnable) {
            // 최초 이므로 무조건 null
        }
    }

    @Override
    public void onBackPressed() {
        if (!ExplorerManager.isInitialPath()) {
            final BaseFragment fragment = (BaseFragment)getSupportFragmentManager().getFragments().get(0);
            fragment.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // 메뉴를 흰색으로 변경
        menu.getItem(0).getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        if (id == R.id.action_view_type) {
            ExplorerFragment fragment = (ExplorerFragment) getSupportFragmentManager().getFragments().get(0);
            if (fragment != null) {
                if (fragment.getViewType() == ExplorerFragment.SWITCH_GRID)
                    fragment.switchToList();
                else if (fragment.getViewType() == ExplorerFragment.SWITCH_LIST)
                    fragment.switchToGrid();
                return true;
            }
            return false;
        }

        if (id == R.id.action_clear_cache) {
            clearHistory();
            clearCache();

            ToastHelper.showToast(this, "캐쉬 파일을 삭제하였습니다.");
        }

        if (id == R.id.action_clear_history) {
            clearHistory();

            ToastHelper.showToast(this, "최근파일 목록을 삭제하였습니다.");
        }

        return super.onOptionsItemSelected(item);
    }

    void clearCache() {
        BitmapCacheManager.removeAllThumbnails();
        BitmapCacheManager.removeAllPages();
        BitmapCacheManager.removeAllBitmaps();
        BitmapCacheManager.removeAllDrawables();

        final File file = getFilesDir();
        deleteRecursive(file);

        final BaseFragment fragment = (BaseFragment)getSupportFragmentManager().getFragments().get(0);
        fragment.onRefresh();
    }

    void clearHistory() {
        BookDB.clearBooks(this);

    }

    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.getAbsolutePath().endsWith("instant-run"))
            return;

        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }
}
