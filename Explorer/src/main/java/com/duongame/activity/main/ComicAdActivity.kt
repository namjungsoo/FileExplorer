package com.duongame.activity.main

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import com.duongame.R
import com.duongame.adapter.ComicPagerAdapter
import com.duongame.databinding.ActivityMainComicAdBinding
import com.duongame.fragment.BaseFragment
import com.duongame.fragment.ExplorerFragment
import com.duongame.manager.AdBannerManager
import com.google.android.material.navigation.NavigationView

class ComicAdActivity : BaseComicActivity() {
    lateinit var binding: ActivityMainComicAdBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_comic_ad)
        AdBannerManager.initBannerAdExt(this, 0, binding.adView)

        super.onCreate(savedInstanceState)
        initTabs(binding.comic.pager, binding.comic.tab)
        showPermissionAlert()
    }

    override fun onResume() {
        binding.adView.resume()

        if (isAdRemoveReward) {
            binding.adView.visibility = View.GONE
        } else {
            binding.adView.visibility = View.VISIBLE
        }
        super.onResume()
    }

    override fun onPause() {
        binding.adView.pause()
        super.onPause()
    }

    override fun onDestroy() {
        val vg = binding.adView.parent as ViewGroup
        vg.removeView(binding.adView)

        binding.adView.removeAllViews()
        binding.adView.destroy()

        showInterstitialAd(null)
        super.onDestroy()
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
    override var drawer: DrawerLayout
        get() = TODO("Not yet implemented")
        set(value) {}
    override var navigationView: NavigationView
        get() = TODO("Not yet implemented")
        set(value) {}
    override var textTitle: TextView
        get() = TODO("Not yet implemented")
        set(value) {}
}