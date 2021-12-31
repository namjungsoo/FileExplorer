package com.duongame.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.duongame.App
import com.duongame.R
import com.duongame.activity.BaseActivity
import com.duongame.file.FileExplorer
import com.duongame.file.LocalExplorer
import com.duongame.helper.ToastHelper

/**
 * Created by namjungsoo on 2017-01-02.
 */
open class BaseFragment : Fragment() {
    // Search
    open var fileExplorer: FileExplorer? = null
        protected set
    var fileResult: FileExplorer.Result? = null
        protected set
    var cloud = CLOUD_LOCAL
    var lastBackPressed: Long = 0
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fileExplorer = LocalExplorer()
    }

    //FIX:
    override fun onResume() {
        super.onResume()
        if (fileExplorer == null) {
            fileExplorer = LocalExplorer()
        }
    }

    open fun onRefresh() {}
    open fun onBackPressed() {
        val current = System.currentTimeMillis()
        if (lastBackPressed != 0L && current - lastBackPressed < TIME_MS) { // 마지막 누른후 2초 안이면 종료 한다.
            // activity가 null일수 있음
            try {
                val activity = activity as BaseActivity?
                if (!activity!!.isFinishing) {
                    App.instance.exit(activity)
                }
            } catch (e: NullPointerException) {
                e.printStackTrace()
            } catch (e: ClassCastException) {
                e.printStackTrace()
            }
        } else { // 그게 아니면 한번더 입력을 받는다
            lastBackPressed = current

            // 토스트를 띄운다.
            val activity = activity
            if (activity != null) {
                ToastHelper.info(activity, R.string.back_pressed)
            }
        }
    }

    companion object {
        private const val TIME_MS = 2000
        const val CLOUD_LOCAL = 0
        const val CLOUD_DROPBOX = 1
        const val CLOUD_GOOGLEDRIVE = 2
    }
}