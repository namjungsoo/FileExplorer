package com.duongame.manager;

import android.content.Context;

import com.duongame.R;
import com.duongame.helper.AppHelper;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import timber.log.Timber;

/**
 * Created by namjungsoo on 2016-04-30.
 */
public class AdBannerManager {
    private final static String TAG = "AdBannerManager";

    private static AdView[] adBannerView = new AdView[3];
    private static AdView adPopupView;

    public static AdView createAd(Context context, String adid, AdSize adtype) {
        final AdView adView = new AdView(context);
        adView.setAdUnitId(adid);

        // 좌우를 꽉채워주는 배너 타입
        adView.setAdSize(adtype);
        return adView;
    }

    public static void initBannerAdExt(Context context, int i, AdView adView) {
        adBannerView[i] = adView;
        initBannerAdCommon(context, i);
    }

    public static void initBannerAd(Context context, int i) {
        final String BANNER_ID;
        if (AppHelper.INSTANCE.isComicz()) {
            BANNER_ID = context.getString(R.string.comicz_admob_banner_id);
        } else {
            BANNER_ID = context.getString(R.string.file_admob_banner_id);
        }

        adBannerView[i] = createAd(context, BANNER_ID, AdSize.SMART_BANNER);
        adBannerView[i].setId(R.id.admob);
        initBannerAdCommon(context, i);
    }

    private static void initBannerAdCommon(Context context, int i) {
        adBannerView[i].setAdListener(new AdListener() {
            private static final String TAG = "adBannerView";

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Timber.d("onAdClosed");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                Timber.d("onAdFailedToLoad=" + errorCode);
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
        });
        //requestAd(i);
    }

    public static void initPopupAd(Context context) {
        final String POPUP_ID;
        if (AppHelper.INSTANCE.isComicz()) {
            POPUP_ID = context.getString(R.string.comicz_admob_popup_id);
        } else {
            POPUP_ID = context.getString(R.string.file_admob_popup_id);
        }

        adPopupView = createAd(context, POPUP_ID, AdSize.MEDIUM_RECTANGLE);
        adPopupView.setAdListener(new AdListener() {
            private static final String TAG = "adPopupView";

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Timber.d("onAdClosed");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                Timber.d("onAdFailedToLoad " + errorCode);
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
        });
    }

    public static void init(Context context) {
        initBannerAd(context, 0);
//        initBannerAd(context, 1);
        initPopupAd(context);
    }

    // adView를 xml에서 만들어서 셋팅할때를 ext라 하며, 이때는 popup만 초기화한다.
    public static void initExt(Context context) {
//        initBannerAdExt(context, 0, adView);
//        initBannerAd(context, 1);
        initPopupAd(context);
    }

    public static AdView getAdBannerView(int i) {
        return adBannerView[i];
    }

    public static AdView getAdPopupView() {
        return adPopupView;
    }

    public static void requestAd(final AdView adView) {
        if (adView != null) {
            // 기본 요청을 시작합니다.
            final AdRequest adRequest = new AdRequest.Builder()
                    // 이제 테스트를 제거하자.
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("E7D6AF2C21297EECB65D16AD42FDF992")
                    .build();

            // 광고 요청으로 adView를 로드합니다.
            adView.loadAd(adRequest);
        }
    }

    public static void requestAd(int i) {
        AdView adView = getAdBannerView(i);
        requestAd(adView);
    }
}
