package com.duongame;

import android.app.Activity;
import android.os.Environment;

import androidx.multidex.MultiDexApplication;

import com.duongame.activity.BaseActivity;
import com.duongame.adapter.ExplorerItem;
import com.duongame.helper.PreferenceHelper;

import java.util.ArrayList;

import timber.log.Timber;

// 여기서 말하는 Analytics란 Google Analytics(이하 GA)를 말한다.
// GA 앱의 구분은 GA_TRACKING_ID로 하며, free/pro의 구분은 없다.
// FA 앱의 구분은 자동으로 이루어 진다. package name에 의존적이다.
public class MainApplication extends MultiDexApplication {
    // path
    private ArrayList<ExplorerItem> fileList;

    private ArrayList<ExplorerItem> imageList;
    private ArrayList<ExplorerItem> videoList;
    private ArrayList<ExplorerItem> audioList;

    private final String initialPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private String lastPath;

    // setting
    private boolean thumbnailDisabled;
    private boolean nightMode;
    private boolean japaneseDirection;
    private boolean pagingAnimationDisabled;

    public static MainApplication getInstance(Activity activity) {
        if (activity == null)
            return null;

        return (MainApplication) activity.getApplication();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());
        Timber.e("MainApplication.onCreate begin");

        nightMode = PreferenceHelper.getNightMode(this);
        thumbnailDisabled = PreferenceHelper.getThumbnailDisabled(this);
        japaneseDirection = PreferenceHelper.getJapaneseDirection(this);
        pagingAnimationDisabled = PreferenceHelper.getPagingAnimationDisabled(this);

        Timber.e("MainApplication.onCreate end");
    }

    //region Path
    public void setImageList(ArrayList<ExplorerItem> imageList) {
        this.imageList = imageList;
    }

    public ArrayList<ExplorerItem> getImageList() {
        return imageList;
    }

    public void setVideoList(ArrayList<ExplorerItem> videoList) {
        this.videoList = videoList;
    }

    public ArrayList<ExplorerItem> getVideoList() {
        return videoList;
    }

    public void setAudioList(ArrayList<ExplorerItem> audioList) {
        this.audioList = audioList;
    }

    public ArrayList<ExplorerItem> getAudioList() {
        return audioList;
    }

    public void setFileList(ArrayList<ExplorerItem> fileList) {
        this.fileList = fileList;
    }

    public ArrayList<ExplorerItem> getFileList() {
        return fileList;
    }

    public String getInitialPath() {
        return initialPath;
    }

    public String getLastPath() {
        return lastPath;
    }

    public void setLastPath(String path) {
        lastPath = path;
    }

    public boolean isInitialPath(String path) {
        return path.equals(initialPath);
    }
    //endregion

    //region setting
    public boolean isJapaneseDirection() {
        return japaneseDirection;
    }

    public boolean isNightMode() {
        return nightMode;
    }

    public boolean isThumbnailDisabled() {
        return thumbnailDisabled;
    }

    public boolean isPagingAnimationDisabled() {
        return pagingAnimationDisabled;
    }

    public void setPagingAnimationDisabled(boolean pagingAnimationDisabled) {
        this.pagingAnimationDisabled = pagingAnimationDisabled;
        PreferenceHelper.setPagingAnimationDisabled(this, pagingAnimationDisabled);
    }

    public void setJapaneseDirection(boolean b) {
        japaneseDirection = b;
        PreferenceHelper.setJapaneseDirection(this, b);
    }

    public void setThumbnailDisabled(boolean b) {
        thumbnailDisabled = b;
        PreferenceHelper.setThumbnailDisabled(this, b);
    }

    public void setNightMode(boolean b) {
        nightMode = b;
        PreferenceHelper.setNightMode(this, b);
    }
    //endregion

    public void exit(final BaseActivity activity) {
        if (activity == null)
            return;

        activity.showInterstitialAd(new Runnable() {
            @Override
            public void run() {
                activity.moveTaskToBack(true);
                if (android.os.Build.VERSION.SDK_INT >= 21) {
                    activity.finishAndRemoveTask();
                } else {
                    activity.finish();
                }
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
    }
}
