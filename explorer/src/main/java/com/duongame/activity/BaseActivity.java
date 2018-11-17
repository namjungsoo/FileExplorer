package com.duongame.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.duongame.AnalyticsApplication;
import com.duongame.BuildConfig;
import com.duongame.helper.JLog;
import com.duongame.helper.PreferenceHelper;
import com.duongame.manager.AdInterstitialManager;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

import java.io.File;

import io.fabric.sdk.android.Fabric;

// main, viewer의 통합 base activity
//
public class BaseActivity extends AppCompatActivity {
    protected FirebaseAnalytics mFirebaseAnalytics;
    protected Tracker mTracker;
    protected AnalyticsApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (AnalyticsApplication) getApplication();

        JLog.e("Jungsoo", "BaseActivity.onCreate begin");

        // 0.1초 단축
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
                JLog.e("Jungsoo", "setupFabric begin");
                setupFabric();
                JLog.e("Jungsoo", "setupFabric end");
                setupFirebase();
                JLog.e("Jungsoo", "setupFirebase end");
                setupGA();
                JLog.e("Jungsoo", "setupGA end");
//            }
//        }).start();
//        JLog.e("Jungsoo", "BaseActivity.onCreate end");
    }

    private void setupFabric() {
        CrashlyticsCore crashlyticsCore = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(this, new Crashlytics.Builder().core(crashlyticsCore).build());
    }

    private void setupFirebase() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        // 디버그 개발중에는 크래쉬 오류 보고 하지 않음
        if (BuildConfig.DEBUG)
            FirebaseCrash.setCrashCollectionEnabled(false);
        else
            FirebaseCrash.setCrashCollectionEnabled(true);
    }

    private void setupGA() {
        JLog.e("Jungsoo", "setupGA inner begin");
        mTracker = application.getDefaultTracker();
        JLog.e("Jungsoo", "setupGA inner end");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mTracker != null) {
            mTracker.setScreenName(this.getClass().getSimpleName());
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    protected void showInterstitialAd() {
        if(BuildConfig.SHOW_AD) {
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
    }

    protected void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.getAbsolutePath().endsWith("instant-run"))
            return;

        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        boolean ret = fileOrDirectory.delete();
    }

}
