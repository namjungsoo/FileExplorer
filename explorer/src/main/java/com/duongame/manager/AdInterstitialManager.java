package com.duongame.manager;

import android.app.Activity;

import com.duongame.BuildConfig;
import com.duongame.helper.JLog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

/**
 * Created by namjungsoo on 2016-04-30.
 */
public class AdInterstitialManager {
    private final static String TAG = "AdInterstitialManager";

    // 코믹뷰어 전면광고
//    private static final String INTERSTITIAL_ID = "ca-app-pub-5576037828251153/9737551820";
    private static final String INTERSTITIAL_ID = BuildConfig.INTERSTITIAL_ID;
    private static InterstitialAd interstitialAD;
    private static Runnable runnable;

    private static int maxCount = 2;// 기본값은 2이고 remote config에 의해서 조정된다.

    public static int getMaxCount() {
        return maxCount;
    }

    public static  void setMaxCount(int count) {
        maxCount = count;
    }

    private static void requestNewInterstitial() {
        final AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("E7D6AF2C21297EECB65D16AD42FDF992")
                .build();

        interstitialAD.loadAd(adRequest);
    }

    public static void init(final Activity context) {
        interstitialAD = new InterstitialAd(context);            // 삽입 광고 생성관련 메소드들.
        interstitialAD.setAdUnitId(INTERSTITIAL_ID);
        requestNewInterstitial();

        interstitialAD.setAdListener(new AdListener() {

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                JLog.d(TAG, "onAdClosed");
                requestNewInterstitial();
                if(runnable != null) {
                    runnable.run();
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                JLog.d(TAG, "onAdFailedToLoad");
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                JLog.d(TAG, "onAdLeftApplication");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                JLog.d(TAG, "onAdOpened");
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                JLog.d(TAG, "onAdLoaded");
            }
        });                                // 광고의 리스너를 설정합니다.

    }

    public static boolean showAd(Runnable runnable) {
        AdInterstitialManager.runnable = runnable;
        if (interstitialAD == null)
            return false;

        if (interstitialAD.isLoaded()) {
            interstitialAD.show();
            JLog.d(TAG, "show");
            return true;
        } else {
            JLog.d(TAG, "finish");
            return false;
        }
    }
}
