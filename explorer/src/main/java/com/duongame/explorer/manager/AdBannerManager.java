package com.duongame.explorer.manager;

import android.app.Activity;
import android.util.Log;

import com.duongame.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

/**
 * Created by namjungsoo on 2016-04-30.
 */
public class AdBannerManager {
    private final static String TAG = "AdBannerManager";

    //NEXTDOOR
//    private static final String BANNER_ID = "ca-app-pub-5576037828251153/2291112622";
//    private static final String POPUP_ID = "ca-app-pub-5576037828251153/3639793825";

    //COMICZ
    private static final String BANNER_ID = "ca-app-pub-5576037828251153/8260818625";
    private static final String POPUP_ID = "ca-app-pub-5576037828251153/2214285028";

    private static AdView[] adBannerView = new AdView[2];
    private static AdView adPopupView;

    public static AdView createAd(Activity context, String adid, AdSize adtype) {
        final AdView adView = new AdView(context);
        adView.setAdUnitId(adid);

        // 좌우를 꽉채워주는 배너 타입
        adView.setAdSize(adtype);

        // 기본 요청을 시작합니다.
        final AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        // 광고 요청으로 adView를 로드합니다.
        adView.loadAd(adRequest);
        return adView;
    }


    public static void initBannerAd(Activity context, int i) {
        adBannerView[i] = createAd(context, BANNER_ID, AdSize.SMART_BANNER);
        adBannerView[i].setId(R.id.admob);
        adBannerView[i].setAdListener(new AdListener() {
            private static final String TAG = "adBannerView";

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.d(TAG, "onAdClosed");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                Log.d(TAG, "onAdFailedToLoad="+errorCode);
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
        });
    }

    public static void initPopupAd(Activity context) {
        adPopupView = createAd(context, POPUP_ID, AdSize.MEDIUM_RECTANGLE);
        adPopupView.setAdListener(new AdListener() {
            private static final String TAG = "adPopupView";

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.d(TAG, "onAdClosed");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                Log.d(TAG, "onAdFailedToLoad " + errorCode);
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
        });

    }

    public static void init(Activity context) {
        initBannerAd(context, 0);
        initBannerAd(context, 1);
        initPopupAd(context);
    }

    public static AdView getAdBannerView(int i) {
        return adBannerView[i];
    }

    public static AdView getAdPopupView() {
        return adPopupView;
    }
}
