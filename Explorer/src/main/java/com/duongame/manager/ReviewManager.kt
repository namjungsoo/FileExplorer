package com.duongame.manager

import android.app.Activity
import android.content.DialogInterface
import com.duongame.BuildConfig
import com.duongame.R
import com.duongame.activity.BaseActivity
import com.duongame.db.BookLoader.openLastBook
import com.duongame.helper.AlertHelper.showAlert
import com.duongame.helper.AlertHelper.showAlertWithAd
import com.duongame.helper.AppHelper.appName
import com.duongame.helper.AppHelper.isComicz
import com.duongame.helper.AppHelper.launchMarket
import com.duongame.helper.PreferenceHelper
import com.duongame.helper.PreferenceHelper.reviewCount
import com.duongame.helper.PreferenceHelper.reviewed
import com.duongame.manager.AdBannerManager.initPopupAd

/**
 * Created by namjungsoo on 2016-05-03.
 */
object ReviewManager {
    private val reviewIndex = intArrayOf(2, 5, 9)

    fun checkReview(context: Activity): Boolean {
        // 리뷰 카운트를 체크하여 리뷰가 안되어 있으면 리뷰를 해줌
        var ret = false
        if (!reviewed) {
            val reviewCount = reviewCount + 1
            for (i in reviewIndex.indices) {
                if (reviewCount == reviewIndex[i]) { // 리뷰할 횟수와 동일하면
                    val appName = appName
                    val title = String.format(
                        context.resources.getString(R.string.dialog_review_title),
                        appName
                    )
                    val content = String.format(
                        context.resources.getString(R.string.dialog_review_content),
                        appName
                    )
                    val positiveListener = DialogInterface.OnClickListener { dialog, which ->
                        val packageName = context.applicationContext.packageName
                        launchMarket(context, packageName)
                        reviewed = true
                    }
                    val negativeListener =
                        DialogInterface.OnClickListener { dialog, which -> // 리뷰하기에서 취소를 누르면 마지막 파일이 있는지 없는지를 확인한다.
                            // 왜냐면 앱 시작하기에서 호출되므로
                            if (isComicz) {
                                openLastBook(context)
                            }
                        }
                    val baseActivity = context as BaseActivity
                    if (BuildConfig.SHOW_AD && !baseActivity.isAdRemoveReward) {
                        showAlertWithAd(
                            context,
                            title,
                            content,
                            positiveListener,
                            negativeListener,
                            null
                        )
                        initPopupAd(context) // 항상 초기화 해주어야 함
                    } else {
                        showAlert(
                            context,
                            title,
                            content,
                            null,
                            positiveListener,
                            negativeListener,
                            null
                        )
                    }
                    ret = true
                    break
                }
            }
            PreferenceHelper.reviewCount = reviewCount
        }
        return ret
    }
}