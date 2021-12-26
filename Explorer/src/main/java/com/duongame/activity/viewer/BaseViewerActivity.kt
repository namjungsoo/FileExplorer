package com.duongame.activity.viewer

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.ActionBar
import androidx.core.content.ContextCompat
import com.duongame.BuildConfig
import com.duongame.R
import com.duongame.activity.BaseActivity
import com.duongame.activity.SettingsActivity.Companion.getLocalIntent
import com.duongame.bitmap.BitmapCacheManager
import com.duongame.helper.PreferenceHelper
import com.duongame.helper.PreferenceHelper.pagingAnimationDisabled
import com.duongame.manager.AdBannerManager.getAdBannerView
import com.duongame.manager.AdBannerManager.initBannerAd
import com.duongame.manager.AdBannerManager.requestAd
import com.google.android.gms.ads.AdView
import timber.log.Timber

/**
 * Created by namjungsoo on 2016-11-16.
 */
// 전체 화면을 지원한다.
open class BaseViewerActivity : BaseActivity() {
    open var isFullscreen = true
        set(fullscreen) {
            if (fullscreen) {
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        or View.SYSTEM_UI_FLAG_IMMERSIVE)
                bottomPanel!!.visibility = View.INVISIBLE
                topPanel!!.visibility = View.INVISIBLE
            } else {
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
                // 툴박스 보이기
                //TODO: 알파 애니메이션은 나중에 하자
                bottomPanel!!.visibility = View.VISIBLE
                topPanel!!.visibility = View.VISIBLE
            }
            field = fullscreen
            updateFullscreen(fullscreen)
        }

    protected var actionBar: ActionBar? = null

    // bottom panel
    @JvmField
    protected var textName: TextView? = null
    protected var bottomPanel: LinearLayout? = null
    protected var topPanel: LinearLayout? = null
    @JvmField
    protected var textPage: TextView? = null
    @JvmField
    protected var textInfo: TextView? = null
    @JvmField
    protected var textSize: TextView? = null
    @JvmField
    protected var seekPage: SeekBar? = null
    @JvmField
    protected var contentViewResId = 0

    //private View mainView;
    private var adView: AdView? = null
    protected var nightMode: LinearLayout? = null
    @JvmField
    protected var pagingAnim: LinearLayout? = null
    @JvmField
    protected var leftPage: Button? = null
    @JvmField
    protected var rightPage: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initContentView()
        initActionBar()
    }

    protected fun initContentView() {
        setContentView(contentViewResId)
    }

    override fun onDestroy() {
        if (adView != null) {
            val vg = adView!!.parent as ViewGroup
            vg?.removeView(adView)
            adView!!.removeAllViews()
            adView!!.destroy()
        }
        BitmapCacheManager.removeAllBitmaps()
        Timber.e("onDestroy removeAllBitmaps")
        BitmapCacheManager.removeAllPages()
        Timber.e("onDestroy removeAllPages")

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
    }

    private fun initActionBar() {
        actionBar = supportActionBar
        if (actionBar == null) return

        // 투명한 칼라는 액션바의 테마에 적용이 안되서 이렇게 나중에 바꾸어 준다
        actionBar!!.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    this,
                    R.color.colorPrimaryTransparent
                )
            )
        )
        actionBar!!.hide()

        // 로고 버튼
        actionBar!!.setDisplayShowHomeEnabled(true)

        // Up 버튼
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.BLACK
        }
    }

    // 현재 값을 읽어서 UI의 색상을 변경시킨다.
    protected open fun updateNightMode() {
        val iv: ImageView
        val tv: TextView
        iv = findViewById(R.id.img_night)
        tv = findViewById(R.id.text_night)
        if (PreferenceHelper.nightMode) {
            iv.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_orange_light))
            tv.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light))
        } else {
            iv.setColorFilter(ContextCompat.getColor(this, android.R.color.white))
            tv.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        }
    }

    fun updatePagingAnim() {
        val iv: ImageView
        val tv: TextView
        iv = findViewById(R.id.img_anim)
        tv = findViewById(R.id.text_anim)
        if (!pagingAnimationDisabled) {
            iv.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_orange_light))
            tv.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light))
        } else {
            iv.setColorFilter(ContextCompat.getColor(this, android.R.color.white))
            tv.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        }
    }

    protected open fun initToolBox() {
        textName = findViewById(R.id.text_name)
        textInfo = findViewById(R.id.text_info)
        textSize = findViewById(R.id.text_size)
        topPanel = findViewById(R.id.panel_top)
        topPanel?.y = statusBarHeight.toFloat()
        bottomPanel = findViewById(R.id.panel_bottom)
        if (hasSoftKeys()) {
            val lp = bottomPanel?.layoutParams as FrameLayout.LayoutParams
            lp.bottomMargin = navigationBarHeight
            bottomPanel?.requestLayout()
        }
        textPage = findViewById(R.id.text_page)
        seekPage = findViewById(R.id.seek_page)
        nightMode = findViewById(R.id.layout_night)
        pagingAnim = findViewById(R.id.layout_anim)
        nightMode?.setOnClickListener { // 값을 반전시킨다.
            try {
                PreferenceHelper.nightMode = !PreferenceHelper.nightMode
            } catch (e: NullPointerException) {
            }
            updateNightMode()
        }
        updateNightMode()
        pagingAnim?.setOnClickListener { // 값을 반전시킨다.
            try {
                pagingAnimationDisabled = !pagingAnimationDisabled
            } catch (e: NullPointerException) {
            }
            updatePagingAnim()
        }
        updatePagingAnim()

        //ADVIEW
        if (BuildConfig.SHOW_AD) {
            initBannerAd(this, 1)
            adView = getAdBannerView(1)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            adView?.layoutParams = params
            requestAd(1)
            bottomPanel?.addView(adView)
        }
        seekPage?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        val settings = findViewById<ImageView>(R.id.settings)
        settings.setOnClickListener {
            val intent = getLocalIntent(this@BaseViewerActivity)
            startActivity(intent)
        }
        leftPage = findViewById(R.id.left_page)
        rightPage = findViewById(R.id.right_page)
    }

    /**
     * Detects and toggles immersive mode (also known as "hidey bar" mode).
     */
    //TODO: 4.3, 4.4에서 테스트 해볼것
//    open fun setFullscreen(fullscreen: Boolean) {
//    }

    protected open fun updateName(i: Int) {}
    protected open fun updateFullscreen(isFullscreen: Boolean) {}
    fun hasSoftKeys(): Boolean {
        var hasSoftwareKeys = true
        hasSoftwareKeys = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val d = windowManager.defaultDisplay
            val realDisplayMetrics = DisplayMetrics()
            d.getRealMetrics(realDisplayMetrics)
            val realHeight = realDisplayMetrics.heightPixels
            val realWidth = realDisplayMetrics.widthPixels
            val displayMetrics = DisplayMetrics()
            d.getMetrics(displayMetrics)
            val displayHeight = displayMetrics.heightPixels
            val displayWidth = displayMetrics.widthPixels
            realWidth - displayWidth > 0 || realHeight - displayHeight > 0
        } else {
            val hasMenuKey = ViewConfiguration.get(this).hasPermanentMenuKey()
            val hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
            !hasMenuKey && !hasBackKey
        }
        return hasSoftwareKeys
    }

    protected val navigationBarHeight: Int
        protected get() {
            var result = 0
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            return result
        }
    protected val statusBarHeight: Int
        protected get() {
            var result = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            return result
        }
}