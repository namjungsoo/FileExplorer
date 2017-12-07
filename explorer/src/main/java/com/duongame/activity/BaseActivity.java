package com.duongame.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.duongame.AnalyticsApplication;
import com.duongame.BuildConfig;
import com.duongame.R;
import com.duongame.bitmap.BitmapCacheManager;
import com.duongame.db.BookDB;
import com.duongame.db.BookLoader;
import com.duongame.fragment.BaseFragment;
import com.duongame.fragment.ExplorerFragment;
import com.duongame.helper.PreferenceHelper;
import com.duongame.helper.ToastHelper;
import com.duongame.manager.AdBannerManager;
import com.duongame.manager.AdInterstitialManager;
import com.duongame.manager.PermissionManager;
import com.duongame.manager.ReviewManager;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;

import static com.duongame.fragment.ExplorerFragment.SWITCH_GRID;
import static com.duongame.fragment.ExplorerFragment.SWITCH_LIST;

/**
 * Created by Jungsoo on 2017-10-05.
 */

public abstract class BaseActivity extends AppCompatActivity {
    protected FirebaseAnalytics mFirebaseAnalytics;
    protected Tracker mTracker;

    // admob
    protected View mainView;
    protected AdView adView;

    protected Menu menu;

    protected boolean showReview;

    public boolean getShowReview() {
        return showReview;
    }

    protected abstract int getLayoutResId();

    protected abstract int getMenuResId();

    protected abstract BaseFragment getExplorerFragment();

    protected abstract BaseFragment getCurrentFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AdBannerManager.init(this);
        AdInterstitialManager.init(this);

        initContentView();
        initToolbar();

        showReview = ReviewManager.checkReview(this);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            ViewGroup vg = (ViewGroup) adView.getParent();
            if (vg != null) {
                vg.removeView(adView);
            }
            adView.removeAllViews();
            adView.destroy();
        }

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

        mTracker.setScreenName(this.getClass().getSimpleName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onBackPressed() {
        final BaseFragment fragment = getCurrentFragment();
        if (fragment != null)
            fragment.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(getMenuResId(), menu);

        // 메뉴를 흰색으로 변경
        this.menu = menu;

        updateViewTypeMenuIcon();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_view_type) {
            ExplorerFragment fragment = (ExplorerFragment) getExplorerFragment();
            if (fragment != null) {
                if (fragment.getViewType() == ExplorerFragment.SWITCH_GRID) {
                    fragment.changeViewType(ExplorerFragment.SWITCH_LIST);
                }
                else if (fragment.getViewType() == ExplorerFragment.SWITCH_LIST) {
                    fragment.changeViewType(ExplorerFragment.SWITCH_GRID);
                }
                updateViewTypeMenuIcon();
                return true;
            }
            return false;
        }

        if (id == R.id.action_open_lastbook) {
            BookLoader.openLastBookDirect(this);
            return true;
        }

        if (id == R.id.action_clear_cache) {
            clearCache();

            ToastHelper.showToast(this, getResources().getString(R.string.msg_clear_cache));
        }

        if (id == R.id.action_clear_history) {
            clearHistory();

            ToastHelper.showToast(this, getResources().getString(R.string.msg_clear_history));
        }

        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
    }

    private void initContentView() {
        if (BuildConfig.SHOW_AD) {
            final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mainView = inflater.inflate(getLayoutResId(), null, true);

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
            setContentView(getLayoutResId());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.onRequestStoragePermissionsResult(requestCode, permissions, grantResults);
    }

    protected void updateViewTypeMenuIcon() {
        int viewType = PreferenceHelper.getViewType(this);
        int resId = 0;
        switch (viewType) {
            case SWITCH_LIST:
                resId = R.drawable.grid;
                break;
            case SWITCH_GRID:
                resId = R.drawable.list;
                break;
        }

        if (resId > 0) {
            menu.getItem(0).setIcon(resId);
            menu.getItem(0).getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }
    }

    protected void clearCache() {
        BitmapCacheManager.removeAllThumbnails();
        BitmapCacheManager.removeAllPages();
        BitmapCacheManager.removeAllBitmaps();
        BitmapCacheManager.removeAllDrawables();

        final File file = getFilesDir();
        deleteRecursive(file);

        final BaseFragment fragment = getExplorerFragment();
        if (fragment != null)
            fragment.onRefresh();
    }

    protected void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.getAbsolutePath().endsWith("instant-run"))
            return;

        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    protected void clearHistory() {
        BookDB.clearBooks(this);

        final BaseFragment fragment = getCurrentFragment();
        if (fragment != null)
            fragment.onRefresh();
    }

}
