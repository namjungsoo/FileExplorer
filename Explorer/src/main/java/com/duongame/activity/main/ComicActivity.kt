package com.duongame.activity.main

import android.os.Bundle
import android.view.Menu
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import com.duongame.R
import com.duongame.adapter.ComicPagerAdapter
import com.duongame.databinding.ActivityMainComicBinding
import com.duongame.fragment.BaseFragment
import com.duongame.fragment.ExplorerFragment
import com.google.android.material.navigation.NavigationView

class ComicActivity : BaseComicActivity() {
    lateinit var binding: ActivityMainComicBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_comic)
        super.onCreate(savedInstanceState)
        initTabs(binding.comic.pager, binding.comic.tab)
        showPermissionAlert()
    }

    override var progressBarLoading: ProgressBar = binding.comic.progressLoading
    override var navigationMenu: Menu = binding.navView.menu
    override var bottom: LinearLayout = binding.comic.bottom.bottom
    override var miniPlayer: LinearLayout = binding.comic.miniplayer.miniplayer

    override val explorerFragment = (binding.comic.pager.adapter as ComicPagerAdapter?)?.getItem(0) as ExplorerFragment
    override val currentFragment = (binding.comic.pager.adapter as ComicPagerAdapter?)?.getItem(0) as BaseFragment

    override var btnArchive: ImageButton = binding.comic.bottom.btnArchive
    override var btnCopy: ImageButton = binding.comic.bottom.btnCopy
    override var btnCut: ImageButton = binding.comic.bottom.btnCut
    override var btnDelete: ImageButton = binding.comic.bottom.btnDelete
    override var btnPaste: ImageButton = binding.comic.bottom.btnPaste

    override var btnClose: ImageButton
        get() = TODO("Not yet implemented")
        set(value) {}
    override var btnForward: ImageButton
        get() = TODO("Not yet implemented")
        set(value) {}
    override var btnRewind: ImageButton
        get() = TODO("Not yet implemented")
        set(value) {}
    override var btnPlay: ImageButton
        get() = TODO("Not yet implemented")
        set(value) {}
    override var textTitle: TextView
        get() = TODO("Not yet implemented")
        set(value) {}

    override var drawer: DrawerLayout
        get() = TODO("Not yet implemented")
        set(value) {}
    override var navigationView: NavigationView
        get() = TODO("Not yet implemented")
        set(value) {}
}