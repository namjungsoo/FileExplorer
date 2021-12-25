package com.duongame.manager;

import android.content.Context;
import android.os.Handler;

import com.duongame.R;
import com.duongame.helper.AppHelper;
import com.duongame.helper.PreferenceHelper;
import com.duongame.helper.ToastHelper;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import java.util.Calendar;
import java.util.Date;

import timber.log.Timber;

public class AdRewardManager {
    private static RewardedVideoAd mRewardedVideoAd;

    public static void init(final Context context) {
        Handler handler = new Handler();

        // Use an activity context to get the rewarded video instance.
        // getRewardedVideoAdInstance()가 성능이 느리므로 thread에서 처리하고, setRewardedVideoAdListener()는 UI thread에서 처리한다.
        new Thread(() -> {
            Timber.e("AdRewardManager instance begin");
            mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(context);
            if (mRewardedVideoAd == null)
                return;
            Timber.e("AdRewardManager instance end");

            handler.post(() -> {
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
                        loadRewardedVideoAd(context);
                    }

                    @Override
                    public void onRewarded(RewardItem rewardItem) {
                        // 리워드가 성공적으로 호출되었으면 시간을 늘려준다.
                        // 시간을 늘려주고 결과를 토스트로 뿌려준다.
                        Date date = new Date();
                        Timber.e("reward now=" + date.toString() + " " + " long=" + date.getTime());

                        long nowTime = date.getTime();
                        Calendar c = Calendar.getInstance();
                        c.add(Calendar.HOUR, 1);// 1시간을 더한다.
                        date = c.getTime();

                        long nextTime = date.getTime();
                        Timber.e("reward now + 1 hour=" + date.toString() + " " + date.getTime());

                        long rewardTime = PreferenceHelper.INSTANCE.getAdRemoveTimeReward();

                        // 이전에 리워드 받았던 시간(이전의 nextTime)이 현재 시간보다 작아야 가능
                        if (rewardTime < nowTime) {
                            PreferenceHelper.INSTANCE.setAdRemoveTimeReward(nextTime);
                            ToastHelper.INSTANCE.success(context, R.string.ad_remove_success);
                        } else {
                            ToastHelper.INSTANCE.error(context, R.string.ad_remove_fail);
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
            });
        }).start();
    }

    public static void request(Context context) {
        loadRewardedVideoAd(context);
    }

    private static void loadRewardedVideoAd(Context context) {
        if (mRewardedVideoAd == null)
            return;

        final String REWARD_ID;
        if(AppHelper.INSTANCE.isComicz()) {
            REWARD_ID = context.getString(R.string.comicz_admob_popup_id);
        } else {
            REWARD_ID = context.getString(R.string.file_admob_popup_id);
        }

        mRewardedVideoAd.loadAd(REWARD_ID, new AdRequest.Builder().build());
    }

    public static void show(Context context) {
        if (mRewardedVideoAd == null)
            return;

        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        } else {
            // 광고가 준비되지 않았다는 메세지를 출력
            ToastHelper.INSTANCE.error(context, R.string.ad_not_loaded);
        }
    }

    public static void onResume(Context context) {
        if (mRewardedVideoAd == null)
            return;

        mRewardedVideoAd.resume(context);
    }

    public static void onPause(Context context) {
        if (mRewardedVideoAd == null)
            return;

        mRewardedVideoAd.pause(context);
    }

    public static void onDestroy(Context context) {
        if (mRewardedVideoAd == null)
            return;

        mRewardedVideoAd.destroy(context);
    }
}
