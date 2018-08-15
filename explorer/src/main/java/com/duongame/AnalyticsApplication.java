package com.duongame;

import android.app.Application;
import android.os.Environment;
import android.support.multidex.MultiDexApplication;

import com.duongame.adapter.ExplorerItem;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;

public class AnalyticsApplication extends MultiDexApplication {
    private Tracker mTracker;

    private ArrayList<ExplorerItem> imageList;
    private final String initialPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private String lastPath;

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
}
