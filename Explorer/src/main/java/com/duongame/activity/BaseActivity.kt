package com.duongame.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.duongame.BuildConfig
import com.duongame.helper.PreferenceHelper
import com.duongame.manager.AdInterstitialManager
import com.duongame.manager.AdRewardManager
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber
import java.io.File
import java.util.*

// main, viewer의 통합 base activity
//
open class BaseActivity : AppCompatActivity() {
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.e("BaseActivity.onCreate begin")
        setupFabric()
        Timber.e("setupFabric end")
        setupFirebase()
        Timber.e("setupFirebase end")
    }

    private fun setupFabric() {
//        CrashlyticsCore crashlyticsCore = new CrashlyticsCore.Builder()
//                .disabled(BuildConfig.DEBUG)
//                .build();
//        Crashlytics crashlytics = new Crashlytics.Builder().core(crashlyticsCore).build();
//        Fabric.with(this, crashlytics);
    }

    private fun setupFirebase() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        // 디버그 개발중에는 크래쉬 오류 보고 하지 않음
//        if (BuildConfig.DEBUG)
//            FirebaseCrash.setCrashCollectionEnabled(false);
//        else
//            FirebaseCrash.setCrashCollectionEnabled(true);
    }

    override fun onResume() {
        AdRewardManager.onResume(this)
        super.onResume()
    }

    override fun onPause() {
        AdRewardManager.onPause(this)
        super.onPause()
    }

    override fun onDestroy() {
        AdRewardManager.onDestroy(this)
        super.onDestroy()
    }

    val isAdRemoveReward: Boolean
        get() {
            val rewardTime = PreferenceHelper.adRemoveTimeReward
            val date = Date()
            return rewardTime > date.time
        }

    fun showInterstitialAd(runnable: Runnable?) {
        if (BuildConfig.SHOW_AD && !isAdRemoveReward) {
            val count = PreferenceHelper.exitAdCount

            // 2번중에 1번을 띄워준다.
            if (count % AdInterstitialManager.maxCount == 1) { // 전면 팝업후 종료 팝업
                if (!AdInterstitialManager.showAd(runnable)) {
                    // 보여지지 않았다면 insterstitial후 카운트 증가하지 않음
                    runnable?.run()
                } else {
                    // 보여졌다면, 여기서 카운트 증가하고 광고가 끝난후 내부에서 run을 함
                    PreferenceHelper.exitAdCount = count + 1
                }
            } else {
                PreferenceHelper.exitAdCount = count + 1
                runnable?.run()
            }
        } else { // 광고 제거되었을때는 카운트 증가 없이 그냥 종료 
            runnable?.run()
        }
    }

    protected fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.absolutePath.endsWith("instant-run")) return
        if (fileOrDirectory.isDirectory) for (child in fileOrDirectory.listFiles()) deleteRecursive(
            child
        )
        val ret = fileOrDirectory.delete()
    }
}