package com.duongame.manager;

import android.content.Context;
import android.util.Log;

import com.duongame.BuildConfig;
import com.duongame.R;
import com.duongame.helper.PreferenceHelper;
import com.duongame.helper.ToastHelper;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import java.util.Calendar;
import java.util.Date;

public class AdRewardManager {
    private static RewardedVideoAd mRewardedVideoAd;
    private static final String REWARD_ID = BuildConfig.REWARD_ID;
    private static Context context;

    public static void init(final Context context) {
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

                Date date = new Date();
                Log.e("reward", "now=" + date.toString() + " " + " long=" + date.getTime());

                long nowTime = date.getTime();
                Calendar c = Calendar.getInstance();
                c.add(Calendar.HOUR, 1);// 1시간을 더한다.
                date = c.getTime();

                long nextTime = date.getTime();
                Log.e("reward", "now + 1 hour=" + date.toString() + " " + date.getTime());

                long rewardTime = PreferenceHelper.getAdRemoveTimeReward(context);

                // 이전에 리워드 받았던 시간(이전의 nextTime)이 현재 시간보다 작아야 가능
                //
                if (rewardTime < nowTime) {
                    PreferenceHelper.setAdRemoveTimeReward(context, nextTime);
                    ToastHelper.success(context, R.string.ad_remove_success);
                } else {
                    ToastHelper.error(context, R.string.ad_remove_fail);
                }

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

    public static void show(Context context) {
        AdRewardManager.context = context;
        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        } else {
            // 광고가 준비되지 않았다는 메세지를 출력
            ToastHelper.error(context, R.string.ad_not_loaded);
        }
    }

    public static void onResume(Context context) {
        AdRewardManager.context = context;
        mRewardedVideoAd.resume(context);
    }

    public static void onPause(Context context) {
        AdRewardManager.context = context;
        mRewardedVideoAd.pause(context);
    }

    public static void onDestroy(Context context) {
        AdRewardManager.context = context;
        mRewardedVideoAd.destroy(context);
    }
}
