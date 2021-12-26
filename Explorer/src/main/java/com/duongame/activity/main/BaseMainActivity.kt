package com.duongame.activity.main

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.MediaPlayer
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.users.FullAccount
import com.duongame.App.Companion.instance
import com.duongame.BuildConfig
import com.duongame.R
import com.duongame.activity.BaseActivity
import com.duongame.activity.SettingsActivity.Companion.getLocalIntent
import com.duongame.bitmap.BitmapCacheManager
import com.duongame.cloud.dropbox.DropboxClientFactory
import com.duongame.cloud.dropbox.GetCurrentAccountTask
import com.duongame.cloud.googledrive.GoogleDriveManager
import com.duongame.cloud.googledrive.GoogleDriveManager.credential
import com.duongame.cloud.googledrive.GoogleDriveManager.login
import com.duongame.db.BookDB.Companion.clearBooks
import com.duongame.db.BookLoader.openLastBookDirect
import com.duongame.fragment.BaseFragment
import com.duongame.fragment.ExplorerFragment
import com.duongame.helper.AlertHelper.showAlert
import com.duongame.helper.AlertHelper.showAlertWithAd
import com.duongame.helper.AppHelper.appName
import com.duongame.helper.AppHelper.isComicz
import com.duongame.helper.AppHelper.isPro
import com.duongame.helper.PreferenceHelper.accountDropbox
import com.duongame.helper.PreferenceHelper.accountGoogleDrive
import com.duongame.helper.PreferenceHelper.viewType
import com.duongame.helper.ToastHelper.error
import com.duongame.helper.ToastHelper.info
import com.duongame.helper.ToastHelper.showToast
import com.duongame.helper.UnitHelper.dpToPx
import com.duongame.manager.AdBannerManager.adPopupView
import com.duongame.manager.AdBannerManager.initBannerAdExt
import com.duongame.manager.AdBannerManager.initExt
import com.duongame.manager.AdBannerManager.initPopupAd
import com.duongame.manager.AdBannerManager.requestAd
import com.duongame.manager.AdInterstitialManager
import com.duongame.manager.AdInterstitialManager.maxCount
import com.duongame.manager.AdInterstitialManager.request
import com.duongame.manager.AdRewardManager
import com.duongame.manager.AdRewardManager.request
import com.duongame.manager.PermissionManager
import com.duongame.manager.PermissionManager.checkContactsPermission
import com.duongame.manager.PermissionManager.onRequestContactsPermissionsResult
import com.duongame.manager.PermissionManager.onRequestStoragePermissionsResult
import com.duongame.manager.ReviewManager.checkReview
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.navigation.NavigationView
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import timber.log.Timber

/**
 * Created by Jungsoo on 2017-10-05.
 */
abstract class BaseMainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    protected var mFirebaseRemoteConfig: FirebaseRemoteConfig? = null

    // admob
    protected var activityView: View? = null
    protected var adView: AdView? = null
    protected var mainView: View? = null
    protected var menu: Menu? = null
    protected var bottom: LinearLayout? = null
    protected var miniPlayer: LinearLayout? = null

    // bottom
    var btnCopy: ImageButton? = null
    var btnCut: ImageButton? = null
    var btnPaste: ImageButton? = null
    var btnArchive: ImageButton? = null
    var btnDelete: ImageButton? = null

    // miniPlayer
    var btnRewind: ImageButton? = null
    var btnForward: ImageButton? = null
    var btnPlay: ImageButton? = null
    var btnClose: ImageButton? = null
    var textTitle: TextView? = null
    var player = MediaPlayer()
    var position = 0
    var track = 0
    var showReview = false
        protected set
    var isDrawerOpened = false
        protected set
    var drawer: DrawerLayout? = null
    var grayStateList: ColorStateList? = null
    var navigationView: NavigationView? = null
    var progressBarLoading: ProgressBar? = null
    var isDropboxLoginClicked = false
    var isGoogleDriveLoginClicked = false
    var handler: Handler? = null

    protected abstract val layoutResId: Int
    protected abstract val menuResId: Int
    protected abstract val explorerFragment: ExplorerFragment
    protected abstract val currentFragment: BaseFragment

    fun gotoAppStorePage(packageName: String) {
        try {
            val marketLaunch = Intent(Intent.ACTION_VIEW)
            marketLaunch.data = Uri.parse("market://details?id=$packageName")
            this.startActivity(marketLaunch)
        } catch (e: ActivityNotFoundException) { // FIX: ActivityNotFoundException
            val marketLaunch = Intent(Intent.ACTION_VIEW)
            marketLaunch.data =
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            this.startActivity(marketLaunch)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler()
        Timber.e("onCreate begin")
        if (BuildConfig.SHOW_AD) {
            Thread {
                MobileAds.initialize(this@BaseMainActivity
                ) {
                    Timber.e("onCreate MobileAds.initialize onInitializationComplete end")
                    handler!!.post {
                        // UI thread에서 처리
                        // init에서 제외한 request 수행
                        // banner는 initContentView에서 수행
                        request(this)
                        requestAd(adPopupView)
                        request()
                    }
                }
            }.start()
            Timber.e("onCreate MobileAds.initialize end")
        }

        // init에서 request는 제외함
        // init은 MobileAds.initialize 완료되기전에 가능
        AdRewardManager.init(this@BaseMainActivity)
        Timber.e("onCreate AdRewardManager.initialize end")
        //AdBannerManager.init(BaseMainActivity.this);
        initExt(this@BaseMainActivity)
        Timber.e("onCreate AdBannerManager.initialize end")
        AdInterstitialManager.init(this@BaseMainActivity)
        Timber.e("onCreate AdInterstitialManager.initialize end")
        Timber.e("initContentView begin")
        initContentView()
        Timber.e("initContentView end")
        initToolbar()
        Timber.e("initToolbar end")
        initDrawer()
        Timber.e("initDrawer end")
        showReview = checkReview(this@BaseMainActivity)
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        mFirebaseRemoteConfig!!.fetch(0).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                mFirebaseRemoteConfig!!.fetchAndActivate()
                val adMaxCount = mFirebaseRemoteConfig!!.getLong("ad_max_count")
                if (adMaxCount > 0) {
                    maxCount = adMaxCount.toInt()
                }
                val version = mFirebaseRemoteConfig!!.getLong("latest_version")
                val force = mFirebaseRemoteConfig!!.getBoolean("force_update")
                if (BuildConfig.VERSION_CODE < version) {
                    info(this@BaseMainActivity, R.string.toast_new_version)
                    if (force) {
                        // 강제로 플레이 스토어로 이동함
                        gotoAppStorePage(applicationContext.packageName)
                    }
                }

                // 앱 마이그레이션 관련
                val from = mFirebaseRemoteConfig!!.getString("migration_from")
                val to = mFirebaseRemoteConfig!!.getString("migration_to")
                if (from == applicationContext.packageName) {
                    gotoAppStorePage(to)
                }
            }
        }
        Timber.e("onCreate end")
    }

    override fun onDestroy() {
        if (adView != null) {
            val vg = adView!!.parent as ViewGroup
            vg?.removeView(adView)
            adView!!.removeAllViews()
            adView!!.destroy()
        }

        // 전면 광고 노출
        showInterstitialAd(null)
        super.onDestroy()
    }

    override fun onPause() {
        if (adView != null) {
            adView!!.pause()
        }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (adView != null) {
            adView!!.resume()
            // 광고 리워드 제거 시간 중인가?
            if (isAdRemoveReward) {
                adView!!.visibility = View.GONE
            } else {
                adView!!.visibility = View.VISIBLE
            }
        }

        //TODO: 코믹z만 클라우드 지원. 추후 다른 앱에서 지원하려면 해제해야함
        if (isComicz) {
            Timber.e("onResume begin")
            if (isDropboxLoginClicked) {
                onResumeDropbox()
                isDropboxLoginClicked = false
            }
            if (isGoogleDriveLoginClicked) {
                onResumeGoogleDrive()
                isGoogleDriveLoginClicked = false
            }
            Timber.e("onResume end")
        }
    }

    internal inner class GoogleDriveLoginTask : AsyncTask<Void?, Void?, Void?>() {
        var accountName: String? = null
        public override fun onPreExecute() {
            super.onPreExecute()
        }

        public override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            Timber.e("GoogleDriveLoginTask.onPostExecute begin")
            loadGoogleDrive(accountName)
            Timber.e("GoogleDriveLoginTask.onPostExecute end")
        }

        protected override fun doInBackground(vararg params: Void?): Void? {
            Timber.e("GoogleDriveLoginTask.doInBackground begin")
            accountName = accountGoogleDrive
            login(this@BaseMainActivity, accountName)
            Timber.e("GoogleDriveLoginTask.doInBackground end")
            return null
        }
    }

    fun onResumeGoogleDrive() {
        Timber.e("onResumeGoogleDrive")
        val task = GoogleDriveLoginTask()
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    internal inner class DropboxLoginTask : AsyncTask<Void?, Void?, Boolean>() {
        public override fun onPreExecute() {
            super.onPreExecute()
        }

        public override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)
            Timber.e("DropboxLoginTask.onPostExecute begin")
            if (result) {
                loadDropbox()
            } else {
                explorerFragment.updateDropboxUI(false)
            }
            Timber.e("DropboxLoginTask.onPostExecute end")
        }

        protected override fun doInBackground(vararg params: Void?): Boolean? {
            Timber.e("DropboxLoginTask.doInBackground begin")
            var accessToken = accountDropbox
            if (accessToken == null) {
                accessToken = Auth.getOAuth2Token()
                return if (accessToken != null) {
                    accountDropbox = accessToken
                    DropboxClientFactory.init(accessToken)
                    true
                } else {
                    false
                }
            }
            DropboxClientFactory.init(accessToken)
            Timber.e("DropboxLoginTask.doInBackground end")
            return true
        }
    }

    fun onResumeDropbox() {
        Timber.e("onResumeDropbox")
        val task = DropboxLoginTask()
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun initDropbox(accessToken: String?) {
        DropboxClientFactory.init(accessToken)
        loadDropbox()
    }

    private fun loadDropbox() {
        GetCurrentAccountTask(
            DropboxClientFactory.client,
            object : GetCurrentAccountTask.Callback {
                override fun onComplete(result: FullAccount?) {
                    val email = result?.email
                    val name = result?.name?.displayName
                    if (navigationView == null) return
                    val menu = navigationView!!.menu ?: return
                    val dropboxItem = menu.findItem(R.id.nav_dropbox)
                    if (dropboxItem != null) {
                        // 로그인이 되었으므로 타이틀을 바꿔준다.
                        dropboxItem.title = email
                        dropboxItem.isChecked = true

                        // 이제 목록을 업데이트 하자.
                        //updateDropbox();
                        explorerFragment.updateDropboxUI(true)
                    }
                }

                override fun onError(e: Exception?) {
                    Log.e(javaClass.name, "Failed to get account details.", e)
                    explorerFragment.updateDropboxUI(false)
                }

            }).execute()
    }

    override fun onBackPressed() {
        val fragment = currentFragment
        val explorerFragment = explorerFragment

        // 탐색기일 경우에는 붙이기 모드에서만 상단의 바를 활성화 함
        if (fragment === explorerFragment) {
            // 붙여넣기 모드에서는 상위 폴더로 올라가기 쉽게 함
            if (explorerFragment.isPasteMode) {
                explorerFragment.gotoUpDirectory()
            } else {
                defaultBackPressed()
            }
        } else { // 탐색기가 아닐 경우에는 무조건 종료 처리를 위해서 호출함
            defaultBackPressed()
        }
    }

    fun defaultBackPressed() {
        val fragment = currentFragment
        fragment.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(menuResId, menu)

        // 메뉴를 흰색으로 변경
        this.menu = menu
        var item: MenuItem
        item = menu.findItem(R.id.action_sort)
        item.icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        item = menu.findItem(R.id.action_new_folder)
        item.icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        item = menu.findItem(R.id.action_select_all)
        item.icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        updateSelectMenuIcon(false, false)
        updateViewTypeMenuIcon()
        return true
    }

    fun updateSelectMenuIcon(selectMode: Boolean, pasteMode: Boolean) {
        val itemSelectAll = menu!!.findItem(R.id.action_select_all)
        val itemNewFolder = menu!!.findItem(R.id.action_new_folder)
        if (pasteMode) {
            itemSelectAll.isVisible = false
            itemNewFolder.isVisible = true
        } else {
            itemSelectAll.isVisible = selectMode
            itemNewFolder.isVisible = !selectMode
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (id == R.id.action_view_type) {
            val fragment = explorerFragment
            if (fragment != null) {
                fragment.changeViewType((fragment.viewType + 1) % 3)
                updateViewTypeMenuIcon()
                return true
            }
            return false
        }
        if (id == R.id.action_sort) {
            val fragment = explorerFragment
            if (fragment != null) {
                fragment.sortFileWithDialog()
                return true
            }
            return false
        }
        if (id == R.id.action_open_lastbook) {
            openLastBookDirect(this)
            return true
        }
        if (id == R.id.action_clear_cache) {
            clearCache()
            return true
        }
        if (id == R.id.action_clear_history) {
            clearHistory()
            return true
        }
        if (id == R.id.action_new_folder) {
            explorerFragment.newFolderWithDialog()
            return true
        }
        if (id == R.id.action_select_all) {
            explorerFragment.selectAll()
            return true
        }

        // ActionBar의 backbutton
        if (id == android.R.id.home) {
            defaultBackPressed()
            return true
        }
        if (id == R.id.action_license) {
            if (BuildConfig.SHOW_AD && !isAdRemoveReward) {
                showAlertWithAd(this,
                    appName,
                    "Icon license: designed by Smashicons from Flaticon",
                    { dialogInterface, i -> dialogInterface.dismiss() }, null, true
                )
                initPopupAd(this) // 항상 초기화 해주어야 함
            } else {
                showAlert(this,
                    appName,
                    "Icon license: designed by Smashicons from Flaticon",
                    null,
                    { dialogInterface, i -> dialogInterface.dismiss() }, null, true
                )
            }
            return true
        }

        //action_setting
        if (id == R.id.action_settings) {
            val intent = getLocalIntent(this)
            startActivity(intent)
            return true
        }
        if (id == R.id.action_exit) {
            try {
                instance.exit(this)
                return true
            } catch (e: NullPointerException) {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initToolbar() {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(Color.WHITE)
    }

    private fun initContentView() {
        if (BuildConfig.SHOW_AD) {
            setContentView(layoutResId)
            // getContentView
            mainView = findViewById(R.id.activity_main)
            val adView = findViewById<AdView>(R.id.adView)
            initBannerAdExt(this, 0, adView)
            handler!!.postDelayed({
                Timber.e("requestAd begin")
                requestAd(0)
                Timber.e("requestAd end")
            }, 1000)
        } else {
            Timber.e("initContentView setContentView begin")
            setContentView(layoutResId)

            // getContentView
            mainView = findViewById(android.R.id.content)
            Timber.e("initContentView setContentView end")
        }
        Timber.e("initContentView initBottomUI begin")
        initBottomUI()
        Timber.e("initContentView initBottomUI end")
        initPlayerUI()
    }

    private fun initBottomUI() {
        // 최초에는 하단으로 숨겨둠
        bottom = findViewById(R.id.bottom)
        //        bottom.setTranslationY(UnitHelper.dpToPx(48));
        btnCopy = bottom?.findViewById(R.id.btn_copy)
        btnCopy?.setEnabled(false)
        btnCopy?.setOnClickListener(View.OnClickListener { explorerFragment.captureSelectedFile(false) })
        btnCut = bottom?.findViewById(R.id.btn_cut)
        btnCut?.setEnabled(false)
        btnCut?.setOnClickListener(View.OnClickListener { explorerFragment.captureSelectedFile(true) })
        btnPaste = bottom?.findViewById(R.id.btn_paste)
        btnPaste?.setEnabled(false)
        btnPaste?.setOnClickListener(View.OnClickListener { explorerFragment.pasteFileWithDialog() })
        btnArchive = bottom?.findViewById(R.id.btn_archive)
        btnArchive?.setEnabled(false)
        btnArchive?.setOnClickListener(View.OnClickListener { explorerFragment.zipFileWithDialog() })
        btnDelete = bottom?.findViewById(R.id.btn_delete)
        btnDelete?.setEnabled(false)
        btnDelete?.setOnClickListener(View.OnClickListener { explorerFragment.deleteFileWithDialog() })
        progressBarLoading = findViewById(R.id.progress_loading)
    }

    private fun initPlayerUI() {
        // miniplayer
        miniPlayer = findViewById(R.id.miniplayer)
        //        miniPlayer.setTranslationY(UnitHelper.dpToPx(56));
        textTitle = findViewById(R.id.txt_title)

        // 원래 대로 돌아옴
        btnClose = findViewById(R.id.btn_close)
        btnClose?.setOnClickListener(
            View.OnClickListener { v: View? -> explorerFragment.onNormalMode() }
        )
        btnForward = findViewById(R.id.btn_forward)
        btnForward?.setOnClickListener(View.OnClickListener { v: View? -> forwardAudio() })
        btnRewind = findViewById(R.id.btn_rewind)
        btnRewind?.setOnClickListener(View.OnClickListener { v: View? -> rewardAudio() })
        btnPlay = findViewById(R.id.btn_play_pause)
        btnPlay?.setOnClickListener(View.OnClickListener { v: View? ->
            if (player.isPlaying) {
                pauseAudio()
            } else {
                playAudio(track)
            }
        })
    }

    internal inner class MyActionBarDrawerToggle(
        activity: Activity?,
        drawerLayout: DrawerLayout?,
        toolbar: Toolbar?,
        openDrawerContentDescRes: Int,
        closeDrawerContentDescRes: Int
    ) : ActionBarDrawerToggle(
        activity,
        drawerLayout,
        toolbar,
        openDrawerContentDescRes,
        closeDrawerContentDescRes
    ) {
        override fun onDrawerStateChanged(newState: Int) {
            super.onDrawerStateChanged(newState)
            Timber.e("onDrawerStateChanged $newState")
        }

        override fun onDrawerOpened(drawerView: View) {
            super.onDrawerOpened(drawerView)
            Timber.e("onDrawerOpened ")
            isDrawerOpened = true

            // drawer가 열리면 노말모드로 변환한다.
            val explorerFragment: ExplorerFragment? = explorerFragment
            explorerFragment?.onNormalMode()
        }

        override fun onDrawerClosed(drawerView: View) {
            super.onDrawerClosed(drawerView)
            Timber.e("onDrawerClosed ")
            isDrawerOpened = false
        }
    }

    fun initDrawer() {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer != null) {
            val toggle: ActionBarDrawerToggle = MyActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
            )
            drawer!!.addDrawerListener(toggle)
            toggle.syncState()
        }
        navigationView = findViewById<View>(R.id.nav_view) as NavigationView

        // comicz만 존재한다.
        if (navigationView != null) {
            navigationView!!.setNavigationItemSelectedListener(this)
            navigationView!!.itemIconTintList = null
            grayStateList = navigationView!!.itemIconTintList
        }
    }

    fun closeDrawer() {
        if (drawer != null) drawer!!.closeDrawers()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestStoragePermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionManager.PERMISSION_CONTACTS) {
            val fragment = currentFragment
            if (fragment != null) fragment.onRefresh()
        }
        onRequestContactsPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionManager.PERMISSION_CONTACTS) {
            // 구글 로그인 중이다.
            login(this)
            isGoogleDriveLoginClicked = true
        }
    }

    protected fun updateViewTypeMenuIcon() {
        val viewType = viewType
        var resId = 0
        when (viewType) {
            ExplorerFragment.SWITCH_LIST -> resId = R.drawable.ic_menu_grid
            ExplorerFragment.SWITCH_GRID -> resId = R.drawable.ic_menu_narrow
            ExplorerFragment.SWITCH_NARROW -> resId = R.drawable.ic_menu_list
        }
        if (resId > 0) {
            val item = menu!!.findItem(R.id.action_view_type)
            if (item != null) {
                item.setIcon(resId)
                item.icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
            }
        }
    }

    protected fun clearCache() {
        //FIX:
        // 모든 썸네일에서 imagebitmap을 찾아서 null해준다.
        // 그 중에 drawable도 있다.
        val explorerFragment: ExplorerFragment? = explorerFragment
        if (explorerFragment != null) {
            val recyclerView = explorerFragment.currentView
            if (recyclerView != null && recyclerView.childCount > 0) {
                for (i in 0 until recyclerView.childCount) {
                    val view = recyclerView.getChildAt(i) ?: break
                    val imageView = view.findViewById<ImageView>(R.id.file_icon) ?: break
                    imageView.setImageBitmap(null)
                }
            }
        }
        BitmapCacheManager.removeAllThumbnails()
        BitmapCacheManager.removeAllPages()
        BitmapCacheManager.removeAllBitmaps()
        val file = filesDir
        deleteRecursive(file)
        explorerFragment?.onRefresh()
        showToast(this, resources.getString(R.string.msg_clear_cache))
    }

    protected fun clearHistory() {
        clearBooks(this)
        val fragment = currentFragment
        if (fragment != null) fragment.onRefresh()
        showToast(this, resources.getString(R.string.msg_clear_history))
    }

    fun showPlayerUI() {
        Timber.e("showPlayerUI")
        //        miniPlayer.setTranslationY(UnitHelper.dpToPx(56));
        position = 0
        showUI(miniPlayer, dpToPx(56))
    }

    fun hidePlayerUI() {
        Timber.e("hidePlayerUI")
        stopAudio()
        hideUI(miniPlayer)
    }

    fun stopAudio() {
        player.stop()
    }

    fun playAudio(track: Int) {
        try {
            if (position > 0) {
                player.seekTo(position)
                player.start()
            } else {
                val audioList = instance.audioList
                val item = audioList!![track]
                player.reset()
                player.setDataSource(item.path)
                player.prepareAsync()
                player.setOnCompletionListener { mp: MediaPlayer? -> forwardAudio() }
                player.setOnPreparedListener { mp: MediaPlayer -> mp.start() }
                this.track = track
                textTitle!!.text = item.name
            }
            btnPlay!!.setImageResource(R.drawable.ic_player_pause)
        } catch (e: Exception) {
            error(this, R.string.toast_error)
        }
    }

    fun pauseAudio() {
        player.pause()
        position = player.currentPosition
        btnPlay!!.setImageResource(R.drawable.ic_player_play)
    }

    fun forwardAudio() {
        val audioList = instance.audioList
        if (track < audioList!!.size - 1) {
            position = 0
            playAudio(track + 1)
        }
    }

    fun rewardAudio() {
        if (track > 0) {
            position = 0
            playAudio(track - 1)
        }
    }

    private fun showUI(bottomView: View?, initPositionY: Int) {
//        final int defaultHeight = mainView.getHeight();
        bottomView!!.visibility = View.VISIBLE
        bottomView.translationY = initPositionY.toFloat()
        bottomView.post {
            Timber.e("bottomView.height=" + bottomView.height)
            Timber.e("bottomView.translationY=" + bottomView.translationY)
            Timber.e("bottomView.y=" + bottomView.y)
            // setUpdateListener requires API 19
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                bottomView.animate().translationYBy(-bottomView.height.toFloat()).setUpdateListener(
                    AnimatorUpdateListener { animation ->
                        val offset = (animation.animatedValue as Float * bottomView.height).toInt()
                        //                Timber.e("" + animation.getAnimatedValue() + " " + offset + " " + mainView.getHeight());
//                    mainView.getLayoutParams().height = defaultHeight - offset;
//                    mainView.requestLayout();
                    }).setListener(null)
            } else {
                val oa = ObjectAnimator.ofFloat(
                    bottomView,
                    View.TRANSLATION_Y,
                    bottomView.translationY,
                    bottomView.translationY - bottomView.height
                )
                oa.duration = 300
                oa.start()
            }
        }
    }

    private fun hideUI(bottomView: View?) {
//        final int defaultHeight = mainView.getHeight();
        Timber.e("hideUI")

        // setUpdateListener requires API 19
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            bottomView!!.animate().translationYBy(bottomView.height.toFloat())
                .setUpdateListener { animation ->
                    val offset = (animation.animatedValue as Float * bottomView.height).toInt()
                    //                Timber.e("" + animation.getAnimatedValue() + " " + offset + " " + mainView.getHeight());
//                    mainView.getLayoutParams().height = defaultHeight + offset;
//                    mainView.requestLayout();
                }.setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    bottomView.visibility = View.GONE
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
        } else {
            val oa = ObjectAnimator.ofFloat(
                bottomView,
                View.TRANSLATION_Y,
                bottomView!!.translationY,
                bottomView.translationY + bottomView.height
            )
            oa.duration = 300
            oa.start()
        }
    }

    // from onSelectMode()
    fun showBottomUI() {
        Timber.e("showBottomUI")
        //        bottom.setTranslationY(UnitHelper.dpToPx(48));
        showUI(bottom, dpToPx(48))

        // 타이틀을 숫자(선택된 파일 갯수)와 화살표로 변경
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        updateSelectMenuIcon(true, false)
    }

    // from onNormalMode()
    fun hideBottomUI() {
        Timber.e("hideBottomUI")
        hideUI(bottom)

        // 원래 타이틀로 돌려준다.
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.title = appName
            actionBar.setDisplayHomeAsUpEnabled(false)
        }
        updateSelectMenuIcon(false, false)
    }

    fun updateSelectedFileCount(count: Int) {
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.title = "" + count
        }
        if (count > 0) {
            btnArchive!!.isEnabled = true
            btnCopy!!.isEnabled = true
            btnCut!!.isEnabled = true
            btnDelete!!.isEnabled = true
            btnPaste!!.isEnabled = false
        } else {
            btnArchive!!.isEnabled = false
            btnCopy!!.isEnabled = false
            btnCut!!.isEnabled = false
            btnDelete!!.isEnabled = false
            btnPaste!!.isEnabled = false
        }
    }

    fun updatePasteMode() {
        btnArchive!!.isEnabled = false
        btnCopy!!.isEnabled = false
        btnCut!!.isEnabled = false
        btnDelete!!.isEnabled = false
        btnPaste!!.isEnabled = true
        val actionBar = supportActionBar
        actionBar?.setTitle(R.string.paste)
        updateSelectMenuIcon(false, true)
    }

    fun loginDropbox(item: MenuItem?) {
        // 로그인이 안되어 있으면 로그인을 한다.
        val app_key: Int
        app_key = if (isPro) {
            R.string.app_key_dropbox_pro
        } else {
            R.string.app_key_dropbox_free
        }
        Auth.startOAuth2Authentication(this, getString(app_key))

        // 최종적으로 로그인을 하고 나서는 explorer에서 dropbox로 가야한다. -> 이건 선택으로 남겨놓자.
        isDropboxLoginClicked = true
    }

    fun logoutDropbox(item: MenuItem?) {
        // 로그인이 되어 있으면 팝업후에 로그아웃을 하고, account를 null로 만든다.
        val title = appName
        val content =
            String.format(getString(R.string.msg_cloud_logout), getString(R.string.dropbox))
        val positiveListener = DialogInterface.OnClickListener { dialog, which ->
            if (item != null) {
                item.title = getString(R.string.dropbox)
                item.isChecked = false
            }
            accountDropbox = null
            // 로그아웃후에는 explorer에서 toolbar에서 dropbox image button을 삭제해야 한다.
            // 그리고 갈곳이 없으니 home으로 간다.
            explorerFragment.updateDropboxUI(false)
        }
        if (BuildConfig.SHOW_AD && !isAdRemoveReward) {
            showAlertWithAd(
                this,
                title,
                content,
                positiveListener,
                null,
                false
            )
            initPopupAd(this) // 항상 초기화 해주어야 함
        } else {
            showAlert(
                this,
                title,
                content,
                null,
                positiveListener,
                null,
                false
            )
        }
    }

    // 드롭박스 클릭시
    private fun onDropbox(item: MenuItem) {
        val account = accountDropbox
        if (account == null) {
            loginDropbox(item)
        } else {
            logoutDropbox(item)
        }
    }

    fun loginGoogleDrive(item: MenuItem?) {
        if (checkContactsPermission(this)) {
            // 퍼미션이 있을경우 여기서 로그인을 함
            login(this)
            isGoogleDriveLoginClicked = true
        }
    }

    fun logoutGoogleDrive(item: MenuItem?) {
        // 로그인이 되어 있으면 팝업후에 로그아웃을 하고, account를 null로 만든다.
        val title = appName
        val content =
            String.format(getString(R.string.msg_cloud_logout), getString(R.string.google_drive))
        val positiveListener = DialogInterface.OnClickListener { dialog, which ->
            if (item != null) {
                item.title = getString(R.string.google_drive)
                item.isChecked = false
            }
            // 로그인이 되어 있으면 팝업후에 로그아웃을 하고, account를 null로 만든다.
            accountGoogleDrive = null

            // 로그아웃후에는 explorer에서 toolbar에서 dropbox image button을 삭제해야 한다.
            // 그리고 갈곳이 없으니 home으로 간다.
            explorerFragment.updateGoogleDriveUI(false)
        }
        if (BuildConfig.SHOW_AD && !isAdRemoveReward) {
            showAlertWithAd(
                this,
                title,
                content,
                positiveListener,
                null,
                false
            )
            initPopupAd(this) // 항상 초기화 해주어야 함
        } else {
            showAlert(
                this,
                title,
                content,
                null,
                positiveListener,
                null,
                false
            )
        }
    }

    // 구글 드라이브 클릭시
    private fun onGoogleDrive(item: MenuItem) {
        val account = accountGoogleDrive
        if (account == null) {
            loginGoogleDrive(item)
        } else {
            logoutGoogleDrive(item)
        }
    }

    val googleDriveMenuItem: MenuItem?
        get() = if (menu == null) null else menu!!.findItem(R.id.nav_google_drive)
    val dropboxMenuItem: MenuItem?
        get() = if (menu == null) null else menu!!.findItem(R.id.nav_dropbox)

    //TODO: 나중에 직접 구현할것
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId
        if (id == R.id.nav_dropbox) {
            onDropbox(item)
        } else if (id == R.id.nav_google_drive) {
            onGoogleDrive(item)
        } else if (id == R.id.action_open_lastbook) {
            openLastBookDirect(this)
        } else if (id == R.id.action_settings) {
            val intent = getLocalIntent(this)
            startActivity(intent)
        } else if (id == R.id.action_exit) {
            try {
                instance.exit(this)
            } catch (ignored: NullPointerException) {
            }
        }
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (GoogleDriveManager.onActivityResult(requestCode, resultCode, data)) {
            // 구글이 정확하게 로그인 되었는지는 selected account name을 보면됨
            // android.permission.GET_ACCOUNTS가 없으면 null이 리턴됨
            val accountName = credential!!.selectedAccountName
            loadGoogleDrive(accountName)
        }
    }

    fun loadGoogleDrive(accountName: String?) {
        Timber.e("loadGoogleDrive")

        // 로그인이 성공했다고 봄
        if (navigationView == null) return
        val menu = navigationView!!.menu ?: return
        val googleDriveItem = menu.findItem(R.id.nav_google_drive)
        if (accountName != null && accountName.length > 0) {
            if (googleDriveItem != null) {
                googleDriveItem.isChecked = true
                googleDriveItem.title = accountName
                explorerFragment.updateGoogleDriveUI(true)
                accountGoogleDrive = accountName
            }
        } else {
            if (googleDriveItem != null) {
                googleDriveItem.isChecked = false
                googleDriveItem.title = getString(R.string.google_drive)
            }
            explorerFragment.updateGoogleDriveUI(false)
        }
    }
}