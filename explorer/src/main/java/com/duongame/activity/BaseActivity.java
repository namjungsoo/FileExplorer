package com.duongame.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.duongame.MainApplication;
import com.duongame.BuildConfig;
import com.duongame.helper.JLog;
import com.duongame.helper.PreferenceHelper;
import com.duongame.manager.AdInterstitialManager;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

import java.io.File;

import io.fabric.sdk.android.Fabric;

// main, viewer의 통합 base activity
//
public class BaseActivity extends AppCompatActivity {
    protected FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
//            }
//        }).start();
//        JLog.e("Jungsoo", "BaseActivity.onCreate end");
    }

    private void setupFabric() {
        CrashlyticsCore crashlyticsCore = new CrashlyticsCore.Builder()
//                .disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(this, new Crashlytics.Builder().core(crashlyticsCore).build());
    }

    private void setupFirebase() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        // 디버그 개발중에는 크래쉬 오류 보고 하지 않음
//        if (BuildConfig.DEBUG)
//            FirebaseCrash.setCrashCollectionEnabled(false);
//        else
//            FirebaseCrash.setCrashCollectionEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void showInterstitialAd(Runnable runnable) {
        if(BuildConfig.SHOW_AD) {
            final int count = PreferenceHelper.getExitAdCount(this);

            // 2번중에 1번을 띄워준다.
            if (count % AdInterstitialManager.getMaxCount() == 1) {// 전면 팝업후 종료 팝업
                if (!AdInterstitialManager.showAd(runnable)) {
                    // 보여지지 않았다면 insterstitial후 카운트 증가하지 않음
                    if(runnable != null)
                        runnable.run();
                } else {
                    // 보여졌다면, 여기서 카운트 증가하고 광고가 끝난후 내부에서 run을 함
                    PreferenceHelper.setExitAdCount(this, count + 1);
                }
            } else {
                PreferenceHelper.setExitAdCount(this, count + 1);
                if(runnable != null)
                    runnable.run();
            }
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
