package com.duongame

import android.os.Build
import android.os.Environment
import android.os.Process
import androidx.multidex.MultiDexApplication
import com.duongame.activity.BaseActivity
import com.duongame.adapter.ExplorerItem
import com.duongame.helper.PreferenceHelper
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.util.*

class App : MultiDexApplication() {
    var fileList: ArrayList<ExplorerItem> = arrayListOf()

    var imageList: ArrayList<ExplorerItem> = arrayListOf()
    var videoList: ArrayList<ExplorerItem> = arrayListOf()
    var audioList: ArrayList<ExplorerItem> = arrayListOf()

    val initialPath = Environment.getExternalStorageDirectory().absolutePath
    var lastPath: String = ""

    init {
        instance = this
    }

    fun isInitialPath(lastPath: String): Boolean = initialPath == lastPath

    override fun onCreate() {
        super.onCreate()

        Timber.plant(DebugTree())
        Timber.e("MainApplication.onCreate begin")

        PreferenceHelper.init(this)
        Timber.e("MainApplication.onCreate end")
    }

    fun exit(activity: BaseActivity) {
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
        lateinit var instance: App
            private set
    }
}
