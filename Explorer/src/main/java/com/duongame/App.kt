package com.duongame

import androidx.multidex.MultiDexApplication
import com.duongame.adapter.ExplorerItem
import timber.log.Timber
import timber.log.Timber.DebugTree
import com.duongame.helper.PreferenceHelper
import com.duongame.activity.BaseActivity
import android.app.Activity
import android.os.Build
import android.os.Environment
import android.os.Process
import com.duongame.App
import java.util.ArrayList

// 여기서 말하는 Analytics란 Google Analytics(이하 GA)를 말한다.
// GA 앱의 구분은 GA_TRACKING_ID로 하며, free/pro의 구분은 없다.
// FA 앱의 구분은 자동으로 이루어 진다. package name에 의존적이다.
class App : MultiDexApplication() {
    // path
    var fileList = arrayListOf<ExplorerItem>()

    //region Path
    var imageList = arrayListOf<ExplorerItem>()
    var videoList = arrayListOf<ExplorerItem>()
    var audioList = arrayListOf<ExplorerItem>()
    val initialPath = Environment.getExternalStorageDirectory().absolutePath
    var lastPath: String = initialPath

    // setting
    private var thumbnailDisabled = false
    private var nightMode = false
    private var japaneseDirection = false
    private var pagingAnimationDisabled = false

    override fun onCreate() {
        super.onCreate()
        Timber.plant(DebugTree())
        Timber.e("MainApplication.onCreate begin")
        nightMode = PreferenceHelper.getNightMode(this)
        thumbnailDisabled = PreferenceHelper.getThumbnailDisabled(this)
        japaneseDirection = PreferenceHelper.getJapaneseDirection(this)
        pagingAnimationDisabled = PreferenceHelper.getPagingAnimationDisabled(this)
        Timber.e("MainApplication.onCreate end")
    }

    fun isInitialPath(path: String): Boolean {
        return path == initialPath
    }

    //endregion
    //region setting
    fun isJapaneseDirection(): Boolean {
        return japaneseDirection
    }

    fun isNightMode(): Boolean {
        return nightMode
    }

    fun isThumbnailDisabled(): Boolean {
        return thumbnailDisabled
    }

    fun isPagingAnimationDisabled(): Boolean {
        return pagingAnimationDisabled
    }

    fun setPagingAnimationDisabled(pagingAnimationDisabled: Boolean) {
        this.pagingAnimationDisabled = pagingAnimationDisabled
        PreferenceHelper.setPagingAnimationDisabled(this, pagingAnimationDisabled)
    }

    fun setJapaneseDirection(b: Boolean) {
        japaneseDirection = b
        PreferenceHelper.setJapaneseDirection(this, b)
    }

    fun setThumbnailDisabled(b: Boolean) {
        thumbnailDisabled = b
        PreferenceHelper.setThumbnailDisabled(this, b)
    }

    fun setNightMode(b: Boolean) {
        nightMode = b
        PreferenceHelper.setNightMode(this, b)
    }

    //endregion
    fun exit(activity: BaseActivity?) {
        if (activity == null) return
        activity.showInterstitialAd(Runnable {
            activity.moveTaskToBack(true)
            if (Build.VERSION.SDK_INT >= 21) {
                activity.finishAndRemoveTask()
            } else {
                activity.finish()
            }
            Process.killProcess(Process.myPid())
        })
    }

    init {
        INSTANCE = this
    }

    companion object {
        @JvmStatic
        lateinit var INSTANCE: App
    }
}