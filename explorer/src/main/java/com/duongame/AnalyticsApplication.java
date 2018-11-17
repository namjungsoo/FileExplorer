package com.duongame;

import android.app.Activity;
import android.app.Application;
import android.os.Environment;
import android.support.multidex.MultiDexApplication;

import com.duongame.adapter.ExplorerItem;
import com.duongame.helper.JLog;
import com.duongame.helper.PreferenceHelper;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;

// 여기서 말하는 Analytics란 Google Analytics(이하 GA)를 말한다.
// GA 앱의 구분은 GA_TRACKING_ID로 하며, free/pro의 구분은 없다.
// FA 앱의 구분은 자동으로 이루어 진다. package name에 의존적이다.
public class AnalyticsApplication extends MultiDexApplication {
    private Tracker mTracker;

    // path
    private ArrayList<ExplorerItem> imageList;
    private final String initialPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private String lastPath;

    // setting
    private boolean thumbnail;
    private boolean nightMode;
    private boolean japaneseDirection;

    @Override
    public void onCreate() {
        super.onCreate();

        nightMode = PreferenceHelper.getNightMode(this);
        thumbnail = PreferenceHelper.getThumbnail(this);
        japaneseDirection = PreferenceHelper.getJapaneseDirection(this);

        JLog.e("Jungsoo", "onCreate end");
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            //mTracker = analytics.newTracker(R.xml.global_tracker);
            mTracker = analytics.newTracker(BuildConfig.GA_TRACKING_ID);

            // 모두 활성화 한다. Android에 권고되는 사항이다.
            mTracker.enableAdvertisingIdCollection(true);
            mTracker.enableAutoActivityTracking(true);
            mTracker.enableExceptionReporting(true);
        }

        if (BuildConfig.DEBUG) {
            // disable FB
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false);
            FirebaseCrash.setCrashCollectionEnabled(false);

            // disable GA
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.setAppOptOut(false);
        }
        return mTracker;
    }

    //region Path
    public void setImageList(ArrayList<ExplorerItem> imageList) {
        this.imageList = imageList;
    }

    public ArrayList<ExplorerItem> getImageList() {
        return imageList;
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

    public boolean isThumbnail() {
        return thumbnail;
    }

    public void setJapaneseDirection(boolean b) {
        japaneseDirection = b;
        PreferenceHelper.setJapaneseDirection(this, b);
    }

    public void setThumbnail(boolean b) {
        thumbnail = b;
        PreferenceHelper.setThumbnail(this, b);
    }

    public void setNightMode(boolean b) {
        nightMode = b;
        PreferenceHelper.setNightMode(this, b);
    }
    //endregion

    public void exit(Activity activity) {
        if (activity == null)
            return;

        activity.moveTaskToBack(true);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            activity.finishAndRemoveTask();
        } else {
            activity.finish();
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
