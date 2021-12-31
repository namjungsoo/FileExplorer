package com.duongame.activity.main

import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import androidx.viewpager.widget.ViewPager
import com.duongame.App.Companion.instance
import com.duongame.R
import com.duongame.activity.main.ComicActivity
import com.duongame.adapter.ComicPagerAdapter
import com.duongame.fragment.BaseFragment
import com.duongame.fragment.ExplorerFragment
import com.duongame.helper.AlertHelper.showAlert
import com.duongame.helper.AppHelper.appName
import com.duongame.helper.AppHelper.isPro
import com.duongame.helper.PreferenceHelper.permissionAgreed
import com.duongame.manager.PermissionManager.checkStoragePermissions
import com.google.android.material.tabs.TabLayout
import timber.log.Timber

class ComicActivity : BaseMainActivity() {
    // viewpager
    private var pager: ViewPager? = null
    private var adapter: ComicPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // 무조건 onCreate 이전에 셋팅 되어야 함
        setTheme(R.style.ExplorerTheme)
        super.onCreate(savedInstanceState)
        Timber.e("initTabs begin")
        initTabs()
        Timber.e("initTabs end")
        if (!permissionAgreed) {
            showAlert(this,
                appName,
                getString(R.string.required_permission),
                null,
                { dialog: DialogInterface?, which: Int ->
                    permissionAgreed = true
                    checkStoragePermissions(this@ComicActivity)
                },
                { dialog: DialogInterface?, which: Int ->
                    try {
                        instance.exit(this@ComicActivity)
                    } catch (e: NullPointerException) {
                    }
                }
            ) { dialog: DialogInterface?, keyCode: Int, event: KeyEvent? -> false }
        }
    }

    override val layoutResId =
        if (isPro) {
            R.layout.activity_main_comic
        } else {
            R.layout.activity_main_comic_ad
        }

    override val menuResId = R.menu.menu_comicz

    override val explorerFragment = adapter?.getItem(0) as ExplorerFragment

    override val currentFragment = adapter?.getItem(pager?.currentItem ?: 0) as BaseFragment

    private fun initTabs() {
        // 이부분은 쓰레드에서 할 것이 아니다. 성능상의 이점도 없음
        pager = findViewById(R.id.pager)
        adapter = ComicPagerAdapter(supportFragmentManager, this@ComicActivity)
        pager?.adapter = adapter
        pager?.offscreenPageLimit = 3
        val tab = findViewById<TabLayout>(R.id.tab)
        tab.setupWithViewPager(pager)
    }
}