package com.duongame

import android.app.Activity
import android.os.Build
import android.os.Environment
import android.os.Process
import androidx.multidex.MultiDexApplication
import com.duongame.activity.BaseActivity
import com.duongame.adapter.ExplorerItem
import com.duongame.helper.PreferenceHelper
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.util.ArrayList

class MainApplication : MultiDexApplication() {
    var fileList: ArrayList<ExplorerItem>? = null

    var imageList: ArrayList<ExplorerItem>? = null
    var videoList: ArrayList<ExplorerItem>? = null
    var audioList: ArrayList<ExplorerItem>? = null

    var initialPath = Environment.getExternalStorageDirectory().absolutePath
    var lastPath: String? = null

    override fun onCreate() {
        super.onCreate()

        Timber.plant(DebugTree())
        Timber.e("MainApplication.onCreate begin")

        PreferenceHelper.init(this)
        Timber.e("MainApplication.onCreate end")
    }

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

    companion object {
        @JvmStatic
        fun getInstance(activity: Activity?): MainApplication? {
            return if (activity == null) null else activity.application as MainApplication
        }
    }
}
