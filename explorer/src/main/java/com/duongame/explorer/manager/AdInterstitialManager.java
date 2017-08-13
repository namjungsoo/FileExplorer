package com.duongame.explorer.manager;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

/**
 * Created by namjungsoo on 2016-04-30.
 */
public class AdInterstitialManager {
    private final static String TAG = "AdInterstitialManager";

    private static final String INTERSTITIAL_ID = "ca-app-pub-5576037828251153/9933993027";
    private static InterstitialAd interstitialAD = null;

    public static final int MODE_EXIT = 1;
    public static final int MODE_REFRESH = 2;

    private static int mode = MODE_EXIT;

    private static void requestNewInterstitial() {
        final AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
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
                Log.d(TAG, "onAdClosed");
                requestNewInterstitial();

                //TODO: 나중에 구현
//                if (mode == MODE_EXIT) {
//                    AlertHelper.showAlertExit(context);
//                } else if (mode == MODE_REFRESH) {
//                    AlertHelper.showAlertRefresh(context);
//                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                Log.d(TAG, "onAdFailedToLoad");
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                Log.d(TAG, "onAdLeftApplication");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                Log.d(TAG, "onAdOpened");
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.d(TAG, "onAdLoaded");
            }
        });                                // 광고의 리스너를 설정합니다.

    }

    public static boolean showAd(Activity context, int mode) {
        AdInterstitialManager.mode = mode;
        if (interstitialAD.isLoaded()) {
            interstitialAD.show();
            Log.d(TAG, "show");
            return true;
        } else {
            Log.d(TAG, "finish");
            return false;
        }
    }
}
