package com.duongame.manager;

import android.content.Context;

import com.duongame.BuildConfig;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

public class AdRewardManager {
    private static RewardedVideoAd mRewardedVideoAd;
    private static final String REWARD_ID = BuildConfig.REWARD_ID;

    public static void init(Context context) {
        // Use an activity context to get the rewarded video instance.
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(context);
        mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {

            @Override
            public void onRewardedVideoAdLoaded() {

            }

            @Override
            public void onRewardedVideoAdOpened() {

            }

            @Override
            public void onRewardedVideoStarted() {

            }

            @Override
            public void onRewardedVideoAdClosed() {
                loadRewardedVideoAd();
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                // 리워드가 성공적으로 호출되었으면 시간을 늘려준다.
                // 시간을 늘려주고 결과를 토스트로 뿌려준다.
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {

            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {

            }

            @Override
            public void onRewardedVideoCompleted() {

            }
        });
        loadRewardedVideoAd();
    }

    private static void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd(REWARD_ID,
                new AdRequest.Builder().build());
    }

    public static void show() {
        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        } else {
            // 광고가 준비되지 않았다는 메세지를 출력
        }
    }

    public static void onResume(Context context) {
        mRewardedVideoAd.resume(context);
    }

    public static void onPause(Context context) {
        mRewardedVideoAd.pause(context);
    }

    public static void onDestroy(Context context) {
        mRewardedVideoAd.destroy(context);
    }
}
