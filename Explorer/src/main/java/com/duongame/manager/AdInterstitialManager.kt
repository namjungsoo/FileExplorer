package com.duongame.manager

import android.content.Context
import com.duongame.R
import com.duongame.helper.AppHelper.isComicz
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import timber.log.Timber

/**
 * Created by namjungsoo on 2016-04-30.
 */
object AdInterstitialManager {
    private var interstitialAD: InterstitialAd? = null
    private var runnable: Runnable? = null

    var maxCount = 2 // 기본값은 2이고 remote config에 의해서 조정된다.

    fun request() {
        requestNewInterstitial()
    }

    private fun requestNewInterstitial() {
        val adRequest = AdRequest.Builder()
            .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
            .addTestDevice("E7D6AF2C21297EECB65D16AD42FDF992")
            .build()
        interstitialAD!!.loadAd(adRequest)
    }

    fun init(context: Context) {
        interstitialAD = InterstitialAd(context) // 삽입 광고 생성관련 메소드들.
        val INTERSTITIAL_ID: String
        INTERSTITIAL_ID = if (isComicz) {
            context.getString(R.string.comicz_admob_interstitial_id)
        } else {
            context.getString(R.string.file_admob_interstitial_id)
        }
        interstitialAD!!.adUnitId = INTERSTITIAL_ID
        interstitialAD!!.adListener = object : AdListener() {
            override fun onAdClosed() {
                super.onAdClosed()
                Timber.d("onAdClosed")
                requestNewInterstitial()
                if (runnable != null) {
                    runnable!!.run()
                }
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                super.onAdFailedToLoad(errorCode)
                Timber.d("onAdFailedToLoad")
            }

            override fun onAdLeftApplication() {
                super.onAdLeftApplication()
                Timber.d("onAdLeftApplication")
            }

            override fun onAdOpened() {
                super.onAdOpened()
                Timber.d("onAdOpened")
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                Timber.d("onAdLoaded")
            }
        } // 광고의 리스너를 설정합니다.
    }

    fun showAd(runnable: Runnable?): Boolean {
        AdInterstitialManager.runnable = runnable
        if (interstitialAD == null) return false
        return if (interstitialAD!!.isLoaded) {
            interstitialAD!!.show()
            Timber.d("show")
            true
        } else {
            Timber.d("finish")
            false
        }
    }
}