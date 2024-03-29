package com.duongame.activity.main;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.users.FullAccount;
import com.duongame.BuildConfig;
import com.duongame.MainApplication;
import com.duongame.R;
import com.duongame.activity.BaseActivity;
import com.duongame.activity.SettingsActivity;
import com.duongame.adapter.ExplorerItem;
import com.duongame.bitmap.BitmapCacheManager;
import com.duongame.cloud.dropbox.DropboxClientFactory;
import com.duongame.cloud.dropbox.GetCurrentAccountTask;
import com.duongame.cloud.googledrive.GoogleDriveManager;
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
import com.duongame.manager.AdRewardManager;
import com.duongame.manager.PermissionManager;
import com.duongame.manager.ReviewManager;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.io.File;
import java.util.ArrayList;

import timber.log.Timber;

import static com.duongame.fragment.ExplorerFragment.SWITCH_GRID;
import static com.duongame.fragment.ExplorerFragment.SWITCH_LIST;
import static com.duongame.fragment.ExplorerFragment.SWITCH_NARROW;

/**
 * Created by Jungsoo on 2017-10-05.
 */

public abstract class BaseMainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    protected FirebaseRemoteConfig mFirebaseRemoteConfig;

    // admob
    protected View activityView;
    protected AdView adView;
    protected View mainView;

    protected Menu menu;
    protected LinearLayout bottom;
    protected LinearLayout miniPlayer;

    // bottom
    ImageButton btnCopy;
    ImageButton btnCut;
    ImageButton btnPaste;
    ImageButton btnArchive;
    ImageButton btnDelete;

    // miniPlayer
    ImageButton btnRewind;
    ImageButton btnForward;
    ImageButton btnPlay;
    ImageButton btnClose;
    TextView textTitle;

    MediaPlayer player = new MediaPlayer();
    int position;
    int track;

    protected boolean showReview;
    protected boolean drawerOpened;
    DrawerLayout drawer;
    ColorStateList grayStateList;
    NavigationView navigationView;

    ProgressBar loadingProgressBar;
    boolean isDropboxLoginClicked;
    boolean isGoogleDriveLoginClicked;

    Handler handler;

    public boolean getShowReview() {
        return showReview;
    }

    protected abstract int getLayoutResId();

    protected abstract int getMenuResId();

    protected abstract ExplorerFragment getExplorerFragment();

    protected abstract BaseFragment getCurrentFragment();

    public MediaPlayer getPlayer() {
        return player;
    }

    void gotoAppStorePage(String packageName) {
        try {
            final Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
            marketLaunch.setData(Uri.parse("market://details?id=" + packageName));
            this.startActivity(marketLaunch);
        } catch (ActivityNotFoundException e) {// FIX: ActivityNotFoundException
            final Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
            marketLaunch.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
            this.startActivity(marketLaunch);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();

        Timber.e("onCreate begin");
        if (BuildConfig.SHOW_AD) {
            new Thread(() -> MobileAds.initialize(BaseMainActivity.this,
                    initializationStatus -> {
                        Timber.e("onCreate MobileAds.initialize onInitializationComplete end");
                        handler.post(() -> {// UI thread에서 처리
                            // init에서 제외한 request 수행
                            // banner는 initContentView에서 수행
                            AdRewardManager.request(this);
                            AdBannerManager.requestAd(AdBannerManager.getAdPopupView());
                            AdInterstitialManager.request();
                        });
                    })).start();
            Timber.e("onCreate MobileAds.initialize end");
        }

        // init에서 request는 제외함
        // init은 MobileAds.initialize 완료되기전에 가능
        AdRewardManager.init(BaseMainActivity.this);
        Timber.e("onCreate AdRewardManager.initialize end");
        //AdBannerManager.init(BaseMainActivity.this);
        AdBannerManager.initExt(BaseMainActivity.this);
        Timber.e("onCreate AdBannerManager.initialize end");
        AdInterstitialManager.init(BaseMainActivity.this);
        Timber.e("onCreate AdInterstitialManager.initialize end");

        Timber.e("initContentView begin");
        initContentView();
        Timber.e("initContentView end");
        initToolbar();
        Timber.e("initToolbar end");
        initDrawer();
        Timber.e("initDrawer end");

        showReview = ReviewManager.checkReview(BaseMainActivity.this);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.fetch(0).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mFirebaseRemoteConfig.fetchAndActivate();

                    long adMaxCount = mFirebaseRemoteConfig.getLong("ad_max_count");
                    if (adMaxCount > 0) {
                        AdInterstitialManager.setMaxCount((int) adMaxCount);
                    }

                    long version = mFirebaseRemoteConfig.getLong("latest_version");
                    boolean force = mFirebaseRemoteConfig.getBoolean("force_update");
                    if (BuildConfig.VERSION_CODE < version) {
                        ToastHelper.info(BaseMainActivity.this, R.string.toast_new_version);
                        if (force) {
                            // 강제로 플레이 스토어로 이동함
                            gotoAppStorePage(getApplicationContext().getPackageName());
                        }
                    }

                    // 앱 마이그레이션 관련
                    String from = mFirebaseRemoteConfig.getString("migration_from");
                    String to = mFirebaseRemoteConfig.getString("migration_to");
                    if (from.equals(getApplicationContext().getPackageName())) {
                        gotoAppStorePage(to);
                    }
                }
            }
        });
        Timber.e("onCreate end");
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
            if (isAdRemoveReward()) {
                adView.setVisibility(View.GONE);
            } else {
                adView.setVisibility(View.VISIBLE);
            }
        }

        //TODO: 코믹z만 클라우드 지원. 추후 다른 앱에서 지원하려면 해제해야함
        if (AppHelper.isComicz(this)) {
            Timber.e("onResume begin");

            if (isDropboxLoginClicked) {
                onResumeDropbox();
                isDropboxLoginClicked = false;
            }

            if (isGoogleDriveLoginClicked) {
                onResumeGoogleDrive();
                isGoogleDriveLoginClicked = false;
            }

            Timber.e("onResume end");
        }
    }

    class GoogleDriveLoginTask extends android.os.AsyncTask<Void, Void, Void> {
        String accountName;

        @Override
        public void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        public void onPostExecute(Void result) {
            super.onPostExecute(result);
            Timber.e("GoogleDriveLoginTask.onPostExecute begin");
            loadGoogleDrive(accountName);
            Timber.e("GoogleDriveLoginTask.onPostExecute end");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Timber.e("GoogleDriveLoginTask.doInBackground begin");
            accountName = PreferenceHelper.getAccountGoogleDrive(BaseMainActivity.this);
            GoogleDriveManager.login(BaseMainActivity.this, accountName);
            Timber.e("GoogleDriveLoginTask.doInBackground end");
            return null;
        }
    }

    void onResumeGoogleDrive() {
        Timber.e("onResumeGoogleDrive");
        GoogleDriveLoginTask task = new GoogleDriveLoginTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    class DropboxLoginTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        public void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        public void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Timber.e("DropboxLoginTask.onPostExecute begin");
            if (result.booleanValue()) {
                loadDropbox();
            } else {
                getExplorerFragment().updateDropboxUI(false);
            }
            Timber.e("DropboxLoginTask.onPostExecute end");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Timber.e("DropboxLoginTask.doInBackground begin");
            String accessToken = PreferenceHelper.getAccountDropbox(BaseMainActivity.this);
            if (accessToken == null) {
                accessToken = Auth.getOAuth2Token();
                if (accessToken != null) {
                    PreferenceHelper.setAccountDropbox(BaseMainActivity.this, accessToken);
                    DropboxClientFactory.init(accessToken);
                    return true;
                } else {
                    return false;
                }
            }
            DropboxClientFactory.init(accessToken);
            Timber.e("DropboxLoginTask.doInBackground end");
            return true;
        }
    }

    void onResumeDropbox() {
        Timber.e("onResumeDropbox");
        DropboxLoginTask task = new DropboxLoginTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void initDropbox(String accessToken) {
        DropboxClientFactory.init(accessToken);
        loadDropbox();
    }

    private void loadDropbox() {
        new GetCurrentAccountTask(DropboxClientFactory.getClient(), new GetCurrentAccountTask.Callback() {
            @Override
            public void onComplete(FullAccount result) {
                String email = result.getEmail();
                String name = result.getName().getDisplayName();

                if (navigationView == null)
                    return;

                Menu menu = navigationView.getMenu();
                if (menu == null)
                    return;

                MenuItem dropboxItem = menu.findItem(R.id.nav_dropbox);
                if (dropboxItem != null) {
                    // 로그인이 되었으므로 타이틀을 바꿔준다.
                    dropboxItem.setTitle(email);
                    dropboxItem.setChecked(true);

                    // 이제 목록을 업데이트 하자.
                    //updateDropbox();
                    getExplorerFragment().updateDropboxUI(true);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(getClass().getName(), "Failed to get account details.", e);
                getExplorerFragment().updateDropboxUI(false);
            }
        }).execute();
    }

    @Override
    public void onBackPressed() {
        final BaseFragment fragment = getCurrentFragment();
        final ExplorerFragment explorerFragment = getExplorerFragment();

        // 탐색기일 경우에는 붙이기 모드에서만 상단의 바를 활성화 함
        if (fragment == explorerFragment) {
            // 붙여넣기 모드에서는 상위 폴더로 올라가기 쉽게 함
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
                fragment.changeViewType((fragment.getViewType() + 1) % 3);
                updateViewTypeMenuIcon();
                return true;
            }
            return false;
        }

        if (id == R.id.action_sort) {
            ExplorerFragment fragment = getExplorerFragment();
            if (fragment != null) {
                fragment.sortFileWithDialog();
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
            return true;
        }

        if (id == R.id.action_clear_history) {
            clearHistory();
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
            if (BuildConfig.SHOW_AD && !isAdRemoveReward()) {
                AlertHelper.showAlertWithAd(this,
                        AppHelper.getAppName(this),
                        "Icon license: designed by Smashicons from Flaticon",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }, null, true);
                AdBannerManager.initPopupAd(this);// 항상 초기화 해주어야 함
            } else {
                AlertHelper.showAlert(this,
                        AppHelper.getAppName(this),
                        "Icon license: designed by Smashicons from Flaticon",
                        null,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }, null, true);

            }
            return true;
        }

        //action_setting
        if (id == R.id.action_settings) {
            Intent intent = SettingsActivity.getLocalIntent(this);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_exit) {
            try {
                MainApplication.getInstance(this).exit(this);
                return true;
            } catch (NullPointerException e) {

            }
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
            setContentView(getLayoutResId());
            // getContentView
            mainView = this.findViewById(R.id.activity_main);
            AdView adView = this.findViewById(R.id.adView);
            AdBannerManager.initBannerAdExt(this, 0, adView);
            handler.postDelayed(() -> {
                Timber.e("requestAd begin");
                AdBannerManager.requestAd(0);
                Timber.e("requestAd end");
            }, 1000);
        } else {
            Timber.e("initContentView setContentView begin");
            setContentView(getLayoutResId());

            // getContentView
            mainView = this.findViewById(android.R.id.content);
            Timber.e("initContentView setContentView end");
        }

        Timber.e("initContentView initBottomUI begin");
        initBottomUI();
        Timber.e("initContentView initBottomUI end");

        initPlayerUI();
    }

    private void initBottomUI() {
        // 최초에는 하단으로 숨겨둠
        bottom = findViewById(R.id.bottom);
//        bottom.setTranslationY(UnitHelper.dpToPx(48));

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

        loadingProgressBar = findViewById(R.id.progress_loading);
    }

    private void initPlayerUI() {
        // miniplayer
        miniPlayer = findViewById(R.id.miniplayer);
//        miniPlayer.setTranslationY(UnitHelper.dpToPx(56));

        textTitle = findViewById(R.id.txt_title);

        // 원래 대로 돌아옴
        btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(v ->
            getExplorerFragment().onNormalMode()
        );

        btnForward = findViewById(R.id.btn_forward);
        btnForward.setOnClickListener(v -> {
            forwardAudio();
        });
        btnRewind = findViewById(R.id.btn_rewind);
        btnRewind.setOnClickListener(v -> {
            rewardAudio();
        });
        btnPlay = findViewById(R.id.btn_play_pause);
        btnPlay.setOnClickListener(v -> {
            if(player.isPlaying()) {
                pauseAudio();
            } else {
                playAudio(track);
            }
        });
    }

    public ProgressBar getProgressBarLoading() {
        return loadingProgressBar;
    }

    class MyActionBarDrawerToggle extends ActionBarDrawerToggle {

        public MyActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
            super(activity, drawerLayout, toolbar, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        @Override
        public void onDrawerStateChanged(int newState) {
            super.onDrawerStateChanged(newState);
            Timber.e("onDrawerStateChanged " + newState);
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            Timber.e("onDrawerOpened ");
            drawerOpened = true;

            // drawer가 열리면 노말모드로 변환한다.
            ExplorerFragment explorerFragment = getExplorerFragment();
            if (explorerFragment != null) {
                explorerFragment.onNormalMode();
            }
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            super.onDrawerClosed(drawerView);
            Timber.e("onDrawerClosed ");
            drawerOpened = false;
        }
    }

    void initDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            ActionBarDrawerToggle toggle = new MyActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

            drawer.addDrawerListener(toggle);
            toggle.syncState();
        }

        navigationView = (NavigationView) findViewById(R.id.nav_view);

        // comicz만 존재한다.
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
            navigationView.setItemIconTintList(null);
            grayStateList = navigationView.getItemIconTintList();
        }
    }

    public boolean isDrawerOpened() {
        return drawerOpened;
    }

    public void closeDrawer() {
        if (drawer != null)
            drawer.closeDrawers();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PermissionManager.onRequestStoragePermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionManager.PERMISSION_CONTACTS) {
            final BaseFragment fragment = getCurrentFragment();
            if (fragment != null)
                fragment.onRefresh();
        }

        PermissionManager.onRequestContactsPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionManager.PERMISSION_CONTACTS) {
            // 구글 로그인 중이다.
            GoogleDriveManager.login(this);
            isGoogleDriveLoginClicked = true;
        }
    }

    protected void updateViewTypeMenuIcon() {
        int viewType = PreferenceHelper.getViewType(this);
        int resId = 0;
        switch (viewType) {
            case SWITCH_LIST:
                resId = R.drawable.ic_menu_grid;
                break;
            case SWITCH_GRID:
                resId = R.drawable.ic_menu_narrow;
                break;
            case SWITCH_NARROW:
                resId = R.drawable.ic_menu_list;
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
                }
            }
        }

        BitmapCacheManager.removeAllThumbnails();
        BitmapCacheManager.removeAllPages();
        BitmapCacheManager.removeAllBitmaps();

        final File file = getFilesDir();
        deleteRecursive(file);

        if (explorerFragment != null)
            explorerFragment.onRefresh();

        ToastHelper.showToast(this, getResources().getString(R.string.msg_clear_cache));
    }

    protected void clearHistory() {
        BookDB.clearBooks(this);

        final BaseFragment fragment = getCurrentFragment();
        if (fragment != null)
            fragment.onRefresh();

        ToastHelper.showToast(this, getResources().getString(R.string.msg_clear_history));
    }

    public void showPlayerUI() {
        Timber.e("showPlayerUI");
//        miniPlayer.setTranslationY(UnitHelper.dpToPx(56));
        position = 0;
        showUI(miniPlayer, UnitHelper.dpToPx(56));
    }

    public void hidePlayerUI() {
        Timber.e("hidePlayerUI");
        stopAudio();
        hideUI(miniPlayer);
    }

    public void stopAudio() {
        player.stop();
    }

    public void playAudio(int track) {
        try {
            if (position > 0) {
                player.seekTo(position);
                player.start();
            } else {
                ArrayList<ExplorerItem> audioList = MainApplication.getInstance(this).getAudioList();
                ExplorerItem item = audioList.get(track);
                player.reset();
                player.setDataSource(item.path);
                player.prepareAsync();
                player.setOnCompletionListener(mp -> {
                    forwardAudio();
                });
                player.setOnPreparedListener(mp -> {
                    mp.start();
                });
                this.track = track;
                textTitle.setText(item.name);
            }
            btnPlay.setImageResource(R.drawable.ic_player_pause);
        } catch (Exception e) {
            ToastHelper.error(this, R.string.toast_error);
        }
    }

    public void pauseAudio() {
        player.pause();
        position = player.getCurrentPosition();
        btnPlay.setImageResource(R.drawable.ic_player_play);
    }

    public void forwardAudio() {
        ArrayList<ExplorerItem> audioList = MainApplication.getInstance(this).getAudioList();
        if (this.track < audioList.size() - 1) {
            position = 0;
            playAudio(this.track + 1);
        }
    }

    public void rewardAudio() {
        if (this.track > 0) {
            position = 0;
            playAudio(this.track - 1);
        }
    }

    private void showUI(View bottomView, int initPositionY) {
//        final int defaultHeight = mainView.getHeight();
        bottomView.setVisibility(View.VISIBLE);
        bottomView.setTranslationY(initPositionY);
        bottomView.post(() -> {
            Timber.e("bottomView.height=" + bottomView.getHeight());
            Timber.e("bottomView.translationY=" + bottomView.getTranslationY());
            Timber.e("bottomView.y=" + bottomView.getY());
            // setUpdateListener requires API 19
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                bottomView.animate().translationYBy(-bottomView.getHeight()).setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int offset = (int) ((float) animation.getAnimatedValue() * bottomView.getHeight());
//                Timber.e("" + animation.getAnimatedValue() + " " + offset + " " + mainView.getHeight());
//                    mainView.getLayoutParams().height = defaultHeight - offset;
//                    mainView.requestLayout();
                    }
                }).setListener(null);
            } else {
                ObjectAnimator oa = ObjectAnimator.ofFloat(bottomView, View.TRANSLATION_Y, bottomView.getTranslationY(), bottomView.getTranslationY() - bottomView.getHeight());
                oa.setDuration(300);
                oa.start();
            }
        });
    }

    private void hideUI(View bottomView) {
//        final int defaultHeight = mainView.getHeight();
        Timber.e("hideUI");

        // setUpdateListener requires API 19
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            bottomView.animate().translationYBy(bottomView.getHeight()).setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int offset = (int) ((float) animation.getAnimatedValue() * bottomView.getHeight());
//                Timber.e("" + animation.getAnimatedValue() + " " + offset + " " + mainView.getHeight());
//                    mainView.getLayoutParams().height = defaultHeight + offset;
//                    mainView.requestLayout();
                }
            }).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    bottomView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        } else {
            ObjectAnimator oa = ObjectAnimator.ofFloat(bottomView, View.TRANSLATION_Y, bottomView.getTranslationY(), bottomView.getTranslationY() + bottomView.getHeight());
            oa.setDuration(300);
            oa.start();
        }
    }

    // from onSelectMode()
    public void showBottomUI() {
        Timber.e("showBottomUI");
//        bottom.setTranslationY(UnitHelper.dpToPx(48));
        showUI(bottom, UnitHelper.dpToPx(48));

        // 타이틀을 숫자(선택된 파일 갯수)와 화살표로 변경
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        updateSelectMenuIcon(true, false);
    }

    // from onNormalMode()
    public void hideBottomUI() {
        Timber.e("hideBottomUI");
        hideUI(bottom);

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

    void loginDropbox(final MenuItem item) {
        // 로그인이 안되어 있으면 로그인을 한다.

        int app_key;
        if (AppHelper.isPro(this)) {
            app_key = R.string.app_key_dropbox_pro;
        } else {
            app_key = R.string.app_key_dropbox_free;
        }
        Auth.startOAuth2Authentication(this, getString(app_key));

        // 최종적으로 로그인을 하고 나서는 explorer에서 dropbox로 가야한다. -> 이건 선택으로 남겨놓자.
        isDropboxLoginClicked = true;
    }

    void logoutDropbox(final MenuItem item) {
        // 로그인이 되어 있으면 팝업후에 로그아웃을 하고, account를 null로 만든다.
        final String title = AppHelper.getAppName(this);
        final String content = String.format(getString(R.string.msg_cloud_logout), getString(R.string.dropbox));
        final DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (item != null) {
                    item.setTitle(getString(R.string.dropbox));
                    item.setChecked(false);
                }
                PreferenceHelper.setAccountDropbox(BaseMainActivity.this, null);
                // 로그아웃후에는 explorer에서 toolbar에서 dropbox image button을 삭제해야 한다.
                // 그리고 갈곳이 없으니 home으로 간다.
                getExplorerFragment().updateDropboxUI(false);
            }
        };

        if (BuildConfig.SHOW_AD && !isAdRemoveReward()) {
            AlertHelper.showAlertWithAd(this,
                    title,
                    content,
                    positiveListener,
                    null,
                    false);
            AdBannerManager.initPopupAd(this);// 항상 초기화 해주어야 함
        } else {
            AlertHelper.showAlert(this,
                    title,
                    content,
                    null,
                    positiveListener,
                    null,
                    false);
        }
    }

    // 드롭박스 클릭시
    private void onDropbox(final MenuItem item) {
        final String account = PreferenceHelper.getAccountDropbox(this);
        if (account == null) {
            loginDropbox(item);
        } else {
            logoutDropbox(item);
        }
    }

    void loginGoogleDrive(MenuItem item) {
        if (PermissionManager.checkContactsPermission(this)) {
            // 퍼미션이 있을경우 여기서 로그인을 함
            GoogleDriveManager.login(this);
            isGoogleDriveLoginClicked = true;
        }
    }

    public void logoutGoogleDrive(final MenuItem item) {
        // 로그인이 되어 있으면 팝업후에 로그아웃을 하고, account를 null로 만든다.
        final String title = AppHelper.getAppName(this);
        final String content = String.format(getString(R.string.msg_cloud_logout), getString(R.string.google_drive));
        final DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (item != null) {
                    item.setTitle(getString(R.string.google_drive));
                    item.setChecked(false);
                }
                // 로그인이 되어 있으면 팝업후에 로그아웃을 하고, account를 null로 만든다.
                PreferenceHelper.setAccountGoogleDrive(BaseMainActivity.this, null);

                // 로그아웃후에는 explorer에서 toolbar에서 dropbox image button을 삭제해야 한다.
                // 그리고 갈곳이 없으니 home으로 간다.
                getExplorerFragment().updateGoogleDriveUI(false);
            }
        };

        if (BuildConfig.SHOW_AD && !isAdRemoveReward()) {
            AlertHelper.showAlertWithAd(this,
                    title,
                    content,
                    positiveListener,
                    null,
                    false);
            AdBannerManager.initPopupAd(this);// 항상 초기화 해주어야 함
        } else {
            AlertHelper.showAlert(this,
                    title,
                    content,
                    null,
                    positiveListener,
                    null,
                    false);
        }
    }

    // 구글 드라이브 클릭시
    private void onGoogleDrive(final MenuItem item) {
        final String account = PreferenceHelper.getAccountGoogleDrive(this);
        if (account == null) {
            loginGoogleDrive(item);
        } else {
            logoutGoogleDrive(item);
        }
    }

    public MenuItem getGoogleDriveMenuItem() {
        if (menu == null)
            return null;
        return menu.findItem(R.id.nav_google_drive);
    }

    public MenuItem getDropboxMenuItem() {
        if (menu == null)
            return null;
        return menu.findItem(R.id.nav_dropbox);
    }


    //TODO: 나중에 직접 구현할것
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_dropbox) {
            onDropbox(item);
        } else if (id == R.id.nav_google_drive) {
            onGoogleDrive(item);
        } else if (id == R.id.action_open_lastbook) {
            BookLoader.openLastBookDirect(this);
        } else if (id == R.id.action_settings) {
            Intent intent = SettingsActivity.getLocalIntent(this);
            startActivity(intent);
        } else if (id == R.id.action_exit) {
            try {
                MainApplication.getInstance(this).exit(this);
            } catch (NullPointerException ignored) {
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (GoogleDriveManager.onActivityResult(requestCode, resultCode, data)) {
            // 구글이 정확하게 로그인 되었는지는 selected account name을 보면됨
            // android.permission.GET_ACCOUNTS가 없으면 null이 리턴됨
            String accountName = GoogleDriveManager.getCredential().getSelectedAccountName();
            loadGoogleDrive(accountName);
        }
    }

    void loadGoogleDrive(String accountName) {
        Timber.e("loadGoogleDrive");

        // 로그인이 성공했다고 봄
        if (navigationView == null)
            return;

        Menu menu = navigationView.getMenu();
        if (menu == null)
            return;

        MenuItem googleDriveItem = menu.findItem(R.id.nav_google_drive);
        if (accountName != null && accountName.length() > 0) {
            if (googleDriveItem != null) {
                googleDriveItem.setChecked(true);
                googleDriveItem.setTitle(accountName);
                getExplorerFragment().updateGoogleDriveUI(true);
                PreferenceHelper.setAccountGoogleDrive(BaseMainActivity.this, accountName);
            }
        } else {
            if (googleDriveItem != null) {
                googleDriveItem.setChecked(false);
                googleDriveItem.setTitle(getString(R.string.google_drive));
            }
            getExplorerFragment().updateGoogleDriveUI(false);
        }
    }
}
