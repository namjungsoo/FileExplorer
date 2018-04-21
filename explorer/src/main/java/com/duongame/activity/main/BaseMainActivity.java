package com.duongame.activity.main;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.crashlytics.android.Crashlytics;
import com.duongame.AnalyticsApplication;
import com.duongame.BuildConfig;
import com.duongame.R;
import com.duongame.bitmap.BitmapCacheManager;
import com.duongame.db.BookDB;
import com.duongame.db.BookLoader;
import com.duongame.fragment.BaseFragment;
import com.duongame.fragment.ExplorerFragment;
import com.duongame.helper.AlertHelper;
import com.duongame.helper.AppHelper;
import com.duongame.helper.PreferenceHelper;
import com.duongame.helper.ToastHelper;
import com.duongame.helper.UnitHelper;
import com.duongame.manager.AdBannerManager;
import com.duongame.manager.AdInterstitialManager;
import com.duongame.manager.PermissionManager;
import com.duongame.manager.ReviewManager;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.io.File;

import io.fabric.sdk.android.Fabric;

import static com.duongame.fragment.ExplorerFragment.SWITCH_GRID;
import static com.duongame.fragment.ExplorerFragment.SWITCH_LIST;

/**
 * Created by Jungsoo on 2017-10-05.
 */

public abstract class BaseMainActivity extends AppCompatActivity {
    protected FirebaseAnalytics mFirebaseAnalytics;
    protected Tracker mTracker;
    protected FirebaseRemoteConfig mFirebaseRemoteConfig;

    // admob
    protected View activityView;
    protected AdView adView;
    protected View mainView;

    protected Menu menu;
    protected LinearLayout bottom;

    ImageButton btnCopy;
    ImageButton btnCut;
    ImageButton btnPaste;
    ImageButton btnArchive;
    ImageButton btnDelete;

    protected boolean showReview;

    public boolean getShowReview() {
        return showReview;
    }

    protected abstract int getLayoutResId();

    protected abstract int getMenuResId();

    protected abstract ExplorerFragment getExplorerFragment();

    protected abstract BaseFragment getCurrentFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
//        final Fabric fabric = new Fabric.Builder(BaseMainActivity.this)
//                .kits(new Crashlytics())
//                .debuggable(true)           // Enables Crashlytics debugger
//                .build();
//        Fabric.with(fabric);


        AdBannerManager.init(this);
        AdInterstitialManager.init(this);

        initContentView();
        initToolbar();

        showReview = ReviewManager.checkReview(this);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.fetch(0).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mFirebaseRemoteConfig.activateFetched();
                    long version = mFirebaseRemoteConfig.getLong("latest_version");
                    if (BuildConfig.VERSION_CODE < version) {
                        ToastHelper.info(BaseMainActivity.this, R.string.toast_new_version);
                    }
                }
            }
        });

        //TEST Fabric
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Crashlytics.getInstance().crash(); // Force a crash
//            }
//        });
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

        // 전면 광고 노출
        showInterstitialAd();

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
        final ExplorerFragment explorerFragment = getExplorerFragment();

        // 탐색기일 경우에는 붙이기 모드에서만 상단의 바를 활성화 함
        if (fragment == explorerFragment) {
            if (explorerFragment.isPasteMode()) {
                explorerFragment.gotoUpDirectory();
            } else {
                defaultBackPressed();
            }
        } else {// 탐색기가 아닐 경우에는 무조건 종료 처리를 위해서 호출함
            defaultBackPressed();
        }
    }

    void defaultBackPressed() {
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

        MenuItem item;
        item = menu.findItem(R.id.action_sort);
        item.getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        item = menu.findItem(R.id.action_new_folder);
        item.getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        item = menu.findItem(R.id.action_select_all);
        item.getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        updateSelectMenuIcon(false, false);
        updateViewTypeMenuIcon();
        return true;
    }

    void updateSelectMenuIcon(boolean selectMode, boolean pasteMode) {
        MenuItem itemSelectAll = menu.findItem(R.id.action_select_all);
        MenuItem itemNewFolder = menu.findItem(R.id.action_new_folder);

        if (pasteMode) {
            itemSelectAll.setVisible(false);
            itemNewFolder.setVisible(true);
        } else {
            itemSelectAll.setVisible(selectMode);
            itemNewFolder.setVisible(!selectMode);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_view_type) {
            ExplorerFragment fragment = getExplorerFragment();
            if (fragment != null) {
                if (fragment.getViewType() == ExplorerFragment.SWITCH_GRID) {
                    fragment.changeViewType(ExplorerFragment.SWITCH_LIST);
                } else if (fragment.getViewType() == ExplorerFragment.SWITCH_LIST) {
                    fragment.changeViewType(ExplorerFragment.SWITCH_GRID);
                }
                updateViewTypeMenuIcon();
                return true;
            }
            return false;
        }

        if (id == R.id.action_sort) {
            ExplorerFragment fragment = getExplorerFragment();
            if (fragment != null) {
                fragment.sortFileWithDialog();
            }
        }

        if (id == R.id.action_open_lastbook) {
            BookLoader.openLastBookDirect(this);
            return true;
        }

        if (id == R.id.action_clear_cache) {
            clearCache();

            ToastHelper.showToast(this, getResources().getString(R.string.msg_clear_cache));
            return true;
        }

        if (id == R.id.action_clear_history) {
            clearHistory();

            ToastHelper.showToast(this, getResources().getString(R.string.msg_clear_history));
            return true;
        }

        if (id == R.id.action_new_folder) {
            getExplorerFragment().newFolderWithDialog();
            return true;
        }

        if (id == R.id.action_select_all) {
            getExplorerFragment().selectAll();
            return true;
        }

        // ActionBar의 backbutton
        if (id == android.R.id.home) {
            defaultBackPressed();
            return true;
        }

        if (id == R.id.action_license) {
            AlertHelper.showAlertWithAd(this,
                    AppHelper.getAppName(this),
                    "Icon license: designed by Smashicons from Flaticon",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }, null, true);
        }

        return super.onOptionsItemSelected(item);
    }

    void onPasteModeBackPressed() {
        if (getExplorerFragment().isPasteMode()) {
            onBackPressed();
        } else {
            // 붙이기 모드는 상단 홈버튼을 통해서만 취소가 가능하고, 백버튼은 폴더를 이동해야 한다.
            getExplorerFragment().gotoUpDirectory();
        }
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
    }

    private void initContentView() {
        if (BuildConfig.SHOW_AD) {
            final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            activityView = inflater.inflate(getLayoutResId(), null, true);

            mainView = activityView.findViewById(R.id.activity_main);

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
            activityView.setLayoutParams(params);

            layout.addView(adView);
            layout.addView(activityView);

            setContentView(layout);
        } else {
            setContentView(getLayoutResId());
        }

        initBottomUI();
    }

    void initBottomUI() {
        // 최초에는 하단으로 숨겨둠
        bottom = findViewById(R.id.bottom);
        bottom.setTranslationY(UnitHelper.dpToPx(48));

        btnCopy = bottom.findViewById(R.id.btn_copy);
        btnCopy.setEnabled(false);
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getExplorerFragment().captureSelectedFile(false);
            }
        });

        btnCut = bottom.findViewById(R.id.btn_cut);
        btnCut.setEnabled(false);
        btnCut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getExplorerFragment().captureSelectedFile(true);
            }
        });

        btnPaste = bottom.findViewById(R.id.btn_paste);
        btnPaste.setEnabled(false);
        btnPaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getExplorerFragment().pasteFileWithDialog();
            }
        });


        btnArchive = bottom.findViewById(R.id.btn_archive);
        btnArchive.setEnabled(false);
        btnArchive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getExplorerFragment().zipFileWithDialog();
            }
        });


        btnDelete = bottom.findViewById(R.id.btn_delete);
        btnDelete.setEnabled(false);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getExplorerFragment().deleteFileWithDialog();
            }
        });
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
            MenuItem item = menu.findItem(R.id.action_view_type);
            if (item != null) {
                item.setIcon(resId);
                item.getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    protected void clearCache() {
        //FIX:
        // 모든 썸네일에서 imagebitmap을 찾아서 null해준다.
        // 그 중에 drawable도 있다.
        ExplorerFragment explorerFragment = getExplorerFragment();
        if (explorerFragment != null) {
            RecyclerView recyclerView = explorerFragment.getCurrentView();
            if (recyclerView != null && recyclerView.getChildCount() > 0) {
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    View view = recyclerView.getChildAt(i);
                    if (view == null)
                        break;
                    ImageView imageView = view.findViewById(R.id.file_icon);
                    if (imageView == null)
                        break;

                    imageView.setImageBitmap(null);
                    imageView.setImageDrawable(null);
                }
            }
        }

        BitmapCacheManager.removeAllThumbnails();
        BitmapCacheManager.removeAllPages();
        BitmapCacheManager.removeAllBitmaps();
        BitmapCacheManager.removeAllDrawables();
        BookDB.clearBooks(this);

        final File file = getFilesDir();
        deleteRecursive(file);

        if (explorerFragment != null)
            explorerFragment.onRefresh();
    }

    protected void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.getAbsolutePath().endsWith("instant-run"))
            return;

        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        boolean ret = fileOrDirectory.delete();
    }

    protected void clearHistory() {
        BookDB.clearBooks(this);

        final BaseFragment fragment = getCurrentFragment();
        if (fragment != null)
            fragment.onRefresh();
    }

    void showInterstitialAd() {
        final int count = PreferenceHelper.getExitAdCount(this);

        // 2번중에 1번을 띄워준다.
        if (count % 4 == 1) {// 전면 팝업후 종료 팝업
            if (!AdInterstitialManager.showAd(this, AdInterstitialManager.MODE_EXIT)) {
                // 보여지지 않았다면 insterstitial후 카운트 증가하지 않음
                return;
            }
        }
        PreferenceHelper.setExitAdCount(this, count + 1);
    }

    public LinearLayout getBottomUI() {
        return bottom;
    }

    public void showBottomUI() {
        final int defaultHeight = mainView.getHeight();

        // setUpdateListener requires API 19
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            bottom.animate().translationYBy(-bottom.getHeight()).setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int offset = (int) ((float) animation.getAnimatedValue() * bottom.getHeight());
//                JLog.e("TAG", "" + animation.getAnimatedValue() + " " + offset + " " + mainView.getHeight());
                    mainView.getLayoutParams().height = defaultHeight - offset;
                    mainView.requestLayout();
                }
            });
        } else {
            ObjectAnimator oa = ObjectAnimator.ofFloat(bottom, View.TRANSLATION_Y, bottom.getTranslationY(), bottom.getTranslationY() - bottom.getHeight());
            oa.setDuration(300);
            oa.start();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        updateSelectMenuIcon(true, false);
    }

    public void hideBottomUI() {
        final int defaultHeight = mainView.getHeight();

        // setUpdateListener requires API 19
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            bottom.animate().translationYBy(bottom.getHeight()).setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int offset = (int) ((float) animation.getAnimatedValue() * bottom.getHeight());
//                JLog.e("TAG", "" + animation.getAnimatedValue() + " " + offset + " " + mainView.getHeight());
                    mainView.getLayoutParams().height = defaultHeight + offset;
                    mainView.requestLayout();
                }
            });
        } else {
            ObjectAnimator oa = ObjectAnimator.ofFloat(bottom, View.TRANSLATION_Y, bottom.getTranslationY(), bottom.getTranslationY() + bottom.getHeight());
            oa.setDuration(300);
            oa.start();
        }

        // 원래 타이틀로 돌려준다.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(AppHelper.getAppName(this));
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        updateSelectMenuIcon(false, false);
    }

    public void updateSelectedFileCount(int count) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("" + count);
        }

        if (count > 0) {
            btnArchive.setEnabled(true);
            btnCopy.setEnabled(true);
            btnCut.setEnabled(true);
            btnDelete.setEnabled(true);
            btnPaste.setEnabled(false);
        } else {
            btnArchive.setEnabled(false);
            btnCopy.setEnabled(false);
            btnCut.setEnabled(false);
            btnDelete.setEnabled(false);
            btnPaste.setEnabled(false);
        }
    }

    public void updatePasteMode() {
        btnArchive.setEnabled(false);
        btnCopy.setEnabled(false);
        btnCut.setEnabled(false);
        btnDelete.setEnabled(false);
        btnPaste.setEnabled(true);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.paste);
        }

        updateSelectMenuIcon(false, true);
    }
}
