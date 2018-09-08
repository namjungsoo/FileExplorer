package com.duongame.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.duongame.AnalyticsApplication;
import com.duongame.BuildConfig;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

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

        Log.e("Jungsoo", "setupFabric begin");
        setupFabric();
        Log.e("Jungsoo", "setupFabric end");
        setupFirebase();
        Log.e("Jungsoo", "setupFirebase end");
        setupGA();
        Log.e("Jungsoo", "setupGA end");
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
        application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mTracker != null) {
            mTracker.setScreenName(this.getClass().getSimpleName());
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }
}
