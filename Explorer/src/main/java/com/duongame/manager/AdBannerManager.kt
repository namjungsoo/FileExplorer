package com.duongame.manager

import android.content.Context
import com.duongame.R
import com.duongame.helper.AppHelper.isComicz
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import timber.log.Timber

/**
 * Created by namjungsoo on 2016-04-30.
 */
object AdBannerManager {
    private val adBannerView = arrayOfNulls<AdView>(3)
    @JvmStatic
    var adPopupView: AdView? = null
        private set

    private fun createAd(context: Context?, adid: String?, adtype: AdSize?): AdView {
        val adView = AdView(context)
        adView.adUnitId = adid

        // 좌우를 꽉채워주는 배너 타입
        adView.adSize = adtype
        return adView
    }

    @JvmStatic
    fun initBannerAdExt(context: Context, i: Int, adView: AdView?) {
        adBannerView[i] = adView
        initBannerAdCommon(context, i)
    }

    @JvmStatic
    fun initBannerAd(context: Context, i: Int) {
        val BANNER_ID: String = if (isComicz) {
            context.getString(R.string.comicz_admob_banner_id)
        } else {
            context.getString(R.string.file_admob_banner_id)
        }
        adBannerView[i] = createAd(context, BANNER_ID, AdSize.SMART_BANNER)
        adBannerView[i]?.id = R.id.admob
        initBannerAdCommon(context, i)
    }

    private fun initBannerAdCommon(context: Context, i: Int) {
        adBannerView[i]?.adListener = object : AdListener() {
            override fun onAdClosed() {
                super.onAdClosed()
                Timber.d("onAdClosed")
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                super.onAdFailedToLoad(errorCode)
                Timber.d("onAdFailedToLoad=$errorCode")
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
        }
        //requestAd(i);
    }

    @JvmStatic
    fun initPopupAd(context: Context) {
        val POPUP_ID: String
        POPUP_ID = if (isComicz) {
            context.getString(R.string.comicz_admob_popup_id)
        } else {
            context.getString(R.string.file_admob_popup_id)
        }
        adPopupView = createAd(context, POPUP_ID, AdSize.MEDIUM_RECTANGLE)
        adPopupView?.adListener = object : AdListener() {
            override fun onAdClosed() {
                super.onAdClosed()
                Timber.d("onAdClosed")
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                super.onAdFailedToLoad(errorCode)
                Timber.d("onAdFailedToLoad $errorCode")
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
        }
    }

    fun init(context: Context) {
        initBannerAd(context, 0)
        //        initBannerAd(context, 1);
        initPopupAd(context)
    }

    // adView를 xml에서 만들어서 셋팅할때를 ext라 하며, 이때는 popup만 초기화한다.
    @JvmStatic
    fun initExt(context: Context) {
//        initBannerAdExt(context, 0, adView);
//        initBannerAd(context, 1);
        initPopupAd(context)
    }

    @JvmStatic
    fun getAdBannerView(i: Int): AdView? {
        return adBannerView[i]
    }

    fun requestAd(adView: AdView?) {
        if (adView != null) {
            // 기본 요청을 시작합니다.
            val adRequest = AdRequest.Builder() // 이제 테스트를 제거하자.
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("E7D6AF2C21297EECB65D16AD42FDF992")
                .build()

            // 광고 요청으로 adView를 로드합니다.
            adView.loadAd(adRequest)
        }
    }

    @JvmStatic
    fun requestAd(i: Int) {
        val adView = getAdBannerView(i)
        requestAd(adView)
    }
}