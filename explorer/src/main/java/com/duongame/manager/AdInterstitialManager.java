package com.duongame.manager;

import android.content.Context;

import com.duongame.R;
import com.duongame.helper.AppHelper;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import timber.log.Timber;

/**
 * Created by namjungsoo on 2016-04-30.
 */
public class AdInterstitialManager {
    private final static String TAG = "AdInterstitialManager";

    private static InterstitialAd interstitialAD;
    private static Runnable runnable;

    private static int maxCount = 2;// 기본값은 2이고 remote config에 의해서 조정된다.

    public static int getMaxCount() {
        return maxCount;
    }

    public static void setMaxCount(int count) {
        maxCount = count;
    }

    public static void request() {
        requestNewInterstitial();
    }

    private static void requestNewInterstitial() {
        final AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("E7D6AF2C21297EECB65D16AD42FDF992")
                .build();

        interstitialAD.loadAd(adRequest);
    }

    public static void init(final Context context) {
        interstitialAD = new InterstitialAd(context);            // 삽입 광고 생성관련 메소드들.

        final String INTERSTITIAL_ID;
        if (AppHelper.isComicz(context)) {
            INTERSTITIAL_ID = context.getString(R.string.comicz_admob_interstitial_id);
        } else {
            INTERSTITIAL_ID = context.getString(R.string.file_admob_interstitial_id);
        }

        interstitialAD.setAdUnitId(INTERSTITIAL_ID);
        interstitialAD.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Timber.d("onAdClosed");
                requestNewInterstitial();
                if (runnable != null) {
                    runnable.run();
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                Timber.d("onAdFailedToLoad");
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                Timber.d("onAdLeftApplication");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                Timber.d("onAdOpened");
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Timber.d("onAdLoaded");
            }
        });                                // 광고의 리스너를 설정합니다.
    }

    public static boolean showAd(Runnable runnable) {
        AdInterstitialManager.runnable = runnable;
        if (interstitialAD == null)
            return false;

        if (interstitialAD.isLoaded()) {
            interstitialAD.show();
            Timber.d("show");
            return true;
        } else {
            Timber.d("finish");
            return false;
        }
    }
}
