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
import com.duongame.databinding.ActivityMainFileBinding
import com.duongame.fragment.ExplorerFragment
import com.google.android.material.navigation.NavigationView

class FileActivity: BaseMainActivity() {
    lateinit var binding: ActivityMainFileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_file)
        super.onCreate(savedInstanceState)
    }

    override var progressBarLoading: ProgressBar = binding.file.progressLoading
    override var navigationMenu: Menu = binding.navView.menu
    override var bottom: LinearLayout = binding.file.bottom.bottom
    override var miniPlayer: LinearLayout = binding.file.miniplayer.miniplayer

    override val menuResId: Int = R.menu.menu_file
    override val explorerFragment = supportFragmentManager.fragments[0] as ExplorerFragment
    override val currentFragment = explorerFragment

    override var btnArchive: ImageButton = binding.file.bottom.btnArchive
    override var btnCopy: ImageButton = binding.file.bottom.btnCopy
    override var btnCut: ImageButton = binding.file.bottom.btnCut
    override var btnDelete: ImageButton = binding.file.bottom.btnDelete
    override var btnPaste: ImageButton = binding.file.bottom.btnPaste
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