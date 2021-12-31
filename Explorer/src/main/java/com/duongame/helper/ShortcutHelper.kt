package com.duongame.helper

import android.content.Context
import com.duongame.helper.PreferenceHelper.shortcut
import com.duongame.helper.ShortcutHelper
import com.duongame.helper.PreferenceHelper
import android.content.Intent
import android.os.Parcelable
import com.duongame.helper.AppHelper

/**
 * Created by 정수 on 2015-11-15.
 */
object ShortcutHelper {
    fun checkShortcut(context: Context) {
        initShortcut(context)
    }

    private fun initShortcut(context: Context) {
        // 바로가기 중복 생성 방지
        if (!shortcut) {
            Thread { addShortcut(context) }.start()
        }
    }

    private fun addShortcut(context: Context) {
        val shortcutIntent = Intent()
        shortcutIntent.action = Intent.ACTION_MAIN
        shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        shortcutIntent.setClassName(context, context.javaClass.name)
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val iconResource: Parcelable =
            Intent.ShortcutIconResource.fromContext(context, AppHelper.iconResId)
        val intent = Intent()
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
        val resId = 0

        // 현재 사용안함
        // 추후 package name을 사용한 것으로 교체하여야 함
        // context.getApplicationContext().getPackageName();
//        if (BuildConfig.FLAVOR_project.equals("comicz")) {
//            if (BuildConfig.SHOW_AD) {
//                resId = R.string.comicz_name_free;
//            } else {
//                resId = R.string.comicz_name_pro;
//            }
//        } else if (BuildConfig.FLAVOR_project.equals("file")) {
//            if (BuildConfig.SHOW_AD) {
//                resId = R.string.file_name_free;
//            } else {
//                resId = R.string.file_name_pro;
//            }
//        }
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.resources.getString(resId))
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource)
        intent.putExtra("duplicate", false)
        intent.action = "com.android.launcher.action.INSTALL_SHORTCUT"
        context.sendBroadcast(intent)

        // 바로가기 생성했다고 체크함
        shortcut = true
    }
}