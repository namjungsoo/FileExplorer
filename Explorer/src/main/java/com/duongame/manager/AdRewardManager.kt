package com.duongame.manager

import android.content.Context
import android.os.Handler
import com.duongame.R
import com.duongame.helper.AppHelper.isComicz
import com.duongame.helper.PreferenceHelper.adRemoveTimeReward
import com.duongame.helper.ToastHelper.error
import com.duongame.helper.ToastHelper.success
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import timber.log.Timber
import java.util.*

object AdRewardManager {
    private var mRewardedVideoAd: RewardedVideoAd? = null
    @JvmStatic
    fun init(context: Context) {
        val handler = Handler()

        // Use an activity context to get the rewarded video instance.
        // getRewardedVideoAdInstance()가 성능이 느리므로 thread에서 처리하고, setRewardedVideoAdListener()는 UI thread에서 처리한다.
        Thread(Runnable {
            Timber.e("AdRewardManager instance begin")
            mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(context)
            if (mRewardedVideoAd == null) return@Runnable
            Timber.e("AdRewardManager instance end")
            handler.post {
                mRewardedVideoAd?.rewardedVideoAdListener = object : RewardedVideoAdListener {
                    override fun onRewardedVideoAdLoaded() {}
                    override fun onRewardedVideoAdOpened() {}
                    override fun onRewardedVideoStarted() {}
                    override fun onRewardedVideoAdClosed() {
                        loadRewardedVideoAd(context)
                    }

                    override fun onRewarded(rewardItem: RewardItem) {
                        // 리워드가 성공적으로 호출되었으면 시간을 늘려준다.
                        // 시간을 늘려주고 결과를 토스트로 뿌려준다.
                        var date = Date()
                        Timber.e("reward now=" + date.toString() + " " + " long=" + date.time)
                        val nowTime = date.time
                        val c = Calendar.getInstance()
                        c.add(Calendar.HOUR, 1) // 1시간을 더한다.
                        date = c.time
                        val nextTime = date.time
                        Timber.e("reward now + 1 hour=" + date.toString() + " " + date.time)
                        val rewardTime = adRemoveTimeReward

                        // 이전에 리워드 받았던 시간(이전의 nextTime)이 현재 시간보다 작아야 가능
                        if (rewardTime < nowTime) {
                            adRemoveTimeReward = nextTime
                            success(context, R.string.ad_remove_success)
                        } else {
                            error(context, R.string.ad_remove_fail)
                        }
                    }

                    override fun onRewardedVideoAdLeftApplication() {}
                    override fun onRewardedVideoAdFailedToLoad(i: Int) {}
                    override fun onRewardedVideoCompleted() {}
                }
            }
        }).start()
    }

    fun request(context: Context) {
        loadRewardedVideoAd(context)
    }

    private fun loadRewardedVideoAd(context: Context) {
        val REWARD_ID: String = if (isComicz) {
            context.getString(R.string.comicz_admob_popup_id)
        } else {
            context.getString(R.string.file_admob_popup_id)
        }
        mRewardedVideoAd?.loadAd(REWARD_ID, AdRequest.Builder().build())
    }

    fun show(context: Context?) {
        if(mRewardedVideoAd?.isLoaded == true) {
            mRewardedVideoAd?.show()
        } else {
            // 광고가 준비되지 않았다는 메세지를 출력
            error(context, R.string.ad_not_loaded)
        }
    }

    fun onResume(context: Context?) {
        mRewardedVideoAd?.resume(context)
    }

    fun onPause(context: Context?) {
        mRewardedVideoAd?.pause(context)
    }

    fun onDestroy(context: Context?) {
        mRewardedVideoAd?.destroy(context)
    }
}