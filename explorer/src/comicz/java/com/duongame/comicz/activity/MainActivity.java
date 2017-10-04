package com.duongame.comicz.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.duongame.BuildConfig;
import com.duongame.R;
import com.duongame.AnalyticsApplication;
import com.duongame.comicz.adapter.ComicPagerAdapter;
import com.duongame.comicz.db.BookDB;
import com.duongame.comicz.db.BookLoader;
import com.duongame.explorer.bitmap.BitmapCacheManager;
import com.duongame.explorer.fragment.BaseFragment;
import com.duongame.explorer.fragment.ExplorerFragment;
import com.duongame.explorer.helper.ShortcutHelper;
import com.duongame.explorer.helper.ToastHelper;
import com.duongame.explorer.manager.AdBannerManager;
import com.duongame.explorer.manager.AdInterstitialManager;
import com.duongame.explorer.manager.PermissionManager;
import com.duongame.explorer.manager.ReviewManager;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";

    private FirebaseAnalytics mFirebaseAnalytics;
    private Tracker mTracker;

    private ViewPager pager;
    private ComicPagerAdapter adapter;
    private TabLayout tab;
    private View mainView;

    private boolean showReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AdBannerManager.init(this);
        AdInterstitialManager.init(this);

        initContentView();

        initTabs();
        initToolbar();

        ShortcutHelper.checkShortcut(this);
        showReview = ReviewManager.checkReview(this);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        //TEST
//        FirebaseCrash.report(new Exception("My first Android non-fatal error"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("MainActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public boolean getShowReview() {
        return showReview;
    }

    private void initContentView() {
        if (BuildConfig.SHOW_AD) {
            final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mainView = inflater.inflate(R.layout.activity_main, null, true);

            final RelativeLayout layout = new RelativeLayout(this);
            layout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

            final AdView adView = AdBannerManager.getAdBannerView(0);

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
            setContentView(R.layout.activity_main);
        }
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
    }

    private void initTabs() {
        pager = (ViewPager) findViewById(R.id.pager);
        adapter = new ComicPagerAdapter(getSupportFragmentManager(), this);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(3);
        tab = (TabLayout) findViewById(R.id.tab);
        tab.setupWithViewPager(pager);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.onRequestStoragePermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        final int position = pager.getCurrentItem();
        final BaseFragment fragment = (BaseFragment) adapter.getItem(position);
        if (fragment != null) {
            fragment.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

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

//        if (id == R.id.action_settings) {
//            return true;
//        }

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

    void clearCache() {
        BitmapCacheManager.recycleThumbnail();
        BitmapCacheManager.recyclePage();
        BitmapCacheManager.recycleBitmap();
        BitmapCacheManager.recycleDrawable();

        final File file = getFilesDir();
        deleteRecursive(file);

        final BaseFragment fragment = (BaseFragment) adapter.getItem(0);
        fragment.onRefresh();
    }

    void clearHistory() {
        BookDB.clearBooks(this);

        final BaseFragment fragment = (BaseFragment) adapter.getItem(1);
        fragment.onRefresh();
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
