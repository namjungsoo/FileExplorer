package com.duongame.activity.main

import android.os.Bundle
import com.duongame.R
import com.duongame.fragment.ExplorerFragment
import com.duongame.helper.AppHelper.isPro

class FileActivity: BaseMainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 무조건 onCreate 이전에 셋팅 되어야 함
        setTheme(R.style.ExplorerTheme)
        super.onCreate(savedInstanceState)
    }

    override val layoutResId =
        if (isPro) {
            R.layout.activity_main_file
        } else {
            R.layout.activity_main_file_ad
        }

    override val menuResId: Int = R.menu.menu_file

    override val explorerFragment = supportFragmentManager.fragments[0] as ExplorerFragment

    override val currentFragment = explorerFragment
}