package com.duongame.activity.main

import com.duongame.App
import com.duongame.R
import com.duongame.adapter.ComicPagerAdapter
import com.duongame.helper.AlertHelper
import com.duongame.helper.AppHelper
import com.duongame.helper.PreferenceHelper
import com.duongame.manager.PermissionManager
import com.duongame.view.ViewPagerEx
import com.google.android.material.tabs.TabLayout

abstract class BaseComicActivity : BaseMainActivity() {
    protected fun initTabs(pager: ViewPagerEx, tab: TabLayout) {
        val adapter = ComicPagerAdapter(supportFragmentManager, this)
        pager.adapter = adapter
        pager.offscreenPageLimit = 3
        tab.setupWithViewPager(pager)
    }

    protected fun showPermissionAlert() {
        if (!PreferenceHelper.permissionAgreed) {
            AlertHelper.showAlert(this,
                AppHelper.appName,
                getString(R.string.required_permission),
                null,
                { _, _ ->
                    PreferenceHelper.permissionAgreed = true
                    PermissionManager.checkStoragePermissions(this@BaseComicActivity)
                },
                { _, _ ->
                    App.instance.exit(this@BaseComicActivity)
                }
            ) { _, _, _ -> false }
        }
    }

    override val menuResId = R.menu.menu_comicz
}