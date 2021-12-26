package com.duongame.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.widget.SwitchCompat
import com.duongame.BuildConfig
import com.duongame.R
import com.duongame.bitmap.BitmapCacheManager
import com.duongame.db.BookDB
import com.duongame.helper.AlertHelper.showAlert
import com.duongame.helper.AlertHelper.showAlertWithAd
import com.duongame.helper.AppHelper.appName
import com.duongame.helper.AppHelper.isComicz
import com.duongame.helper.AppHelper.isPro
import com.duongame.helper.AppHelper.launchMarket
import com.duongame.helper.PreferenceHelper
import com.duongame.helper.ToastHelper.showToast
import com.duongame.manager.AdBannerManager
import com.google.android.gms.ads.AdView
import timber.log.Timber

// 셋팅을 전부 관장하는 액티비티이다.
//
// 앞으로 해야할일
// 1. 타이틀바 텍스트
// 2. 하단 광고
// 필요한 설정 내용
// 1. 좌우 기본값
// 2. 텍스트 폰트 변경
// 3. 캐쉬 초기화
// 4. 캐쉬 위치(SD카드)
// 5. 패스 워드
// 6. ZIP 파일 인코딩
// 7. 이미지 프로세싱
class SettingsActivity : BaseActivity() {
    private var adView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initContentView()
        initToolbar()
        initUI()
    }

    override fun onDestroy() {
        adView?.let {
            val vg = it.parent as ViewGroup?
            vg?.removeView(it)
            it.removeAllViews()
            it.destroy()
        }
        super.onDestroy()
    }

    override fun onPause() {
        adView?.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        adView?.let {
            it.resume()
            it.visibility = if (isAdRemoveReward) View.GONE else View.VISIBLE
        }
    }

    private fun clearHistory() {
        BookDB.clearBooks(this@SettingsActivity)
        showToast(this, resources.getString(R.string.msg_clear_history))
    }

    private fun clearCache() {
        BitmapCacheManager.removeAllThumbnails()
        BitmapCacheManager.removeAllPages()
        BitmapCacheManager.removeAllBitmaps()
        val file = filesDir
        deleteRecursive(file)
        showToast(this, resources.getString(R.string.msg_clear_cache))
    }

    private fun showLicense() {
        if (BuildConfig.SHOW_AD && !isAdRemoveReward) {
            showAlertWithAd(this,
                appName,
                "Icon license: designed by Smashicons from Flaticon",
                { dialogInterface, i -> dialogInterface.dismiss() }, null, true
            )
            AdBannerManager.initPopupAd(this) // 항상 초기화 해주어야 함
        } else {
            showAlert(this,
                appName,
                "Icon license: designed by Smashicons from Flaticon",
                null,
                { dialogInterface, i -> dialogInterface.dismiss() }, null, true
            )
        }
    }

    private fun initUI() {
        if (!isComicz) {
            findViewById<View>(R.id.layout_japanese_direction).visibility = View.GONE
            findViewById<View>(R.id.layout_action_clear_history).visibility = View.GONE
        }

//        Button adRemove = findViewById(R.id.action_ad_remove);
//        adRemove.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AdRewardManager.show(SettingsActivity.this);
//            }
//        });
        val donate = findViewById<Button>(R.id.action_ad_donate)
        donate.setOnClickListener {
            val intent = Intent(this@SettingsActivity, DonateActivity::class.java)
            startActivity(intent)
        }
        val nightMode = findViewById<SwitchCompat>(R.id.night_mode)
        val thumbnailDisabled = findViewById<SwitchCompat>(R.id.thumbnail_disabled)
        val japaneseDirection = findViewById<SwitchCompat>(R.id.japanese_direction)
        val pagingAnimationDisabled = findViewById<SwitchCompat>(R.id.paging_animation_disabled)

        // 자동 페이징 시간 설정
        val autoPagingTime = findViewById<SeekBar>(R.id.seek_time)
        val autoPagingTimeValue = findViewById<TextView>(R.id.auto_paging_time_value)
        val time = PreferenceHelper.autoPagingTime
        autoPagingTime.max = 10
        autoPagingTime.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                Timber.e("change track " + seekBar.progress)
                autoPagingTimeValue.text = seekBar.progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                Timber.e("start track " + seekBar.progress)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Timber.e("stop track " + seekBar.progress)
                PreferenceHelper.autoPagingTime = seekBar.progress
            }
        })
        autoPagingTimeValue.text = time.toString()
        autoPagingTime.progress = time
        try {
            //MainApplication application = MainApplication.getInstance(this);
            nightMode.isChecked = PreferenceHelper.nightMode
            thumbnailDisabled.isChecked = PreferenceHelper.thumbnailDisabled
            japaneseDirection.isChecked = PreferenceHelper.japaneseDirection
            pagingAnimationDisabled.isChecked = PreferenceHelper.pagingAnimationDisabled
        } catch (e: NullPointerException) {
        }

        // viewer
        nightMode.setOnCheckedChangeListener { _, isChecked ->
            try {
                PreferenceHelper.nightMode = isChecked
            } catch (e: NullPointerException) {
            }
        }
        thumbnailDisabled.setOnCheckedChangeListener { _, isChecked ->
            try {
                PreferenceHelper.thumbnailDisabled = isChecked
            } catch (e: NullPointerException) {
            }
        }
        japaneseDirection.setOnCheckedChangeListener { _, isChecked ->
            try {
                PreferenceHelper.japaneseDirection = isChecked
            } catch (e: NullPointerException) {
            }
        }
        pagingAnimationDisabled.setOnCheckedChangeListener { _, isChecked ->
            try {
                PreferenceHelper.pagingAnimationDisabled = isChecked
            } catch (e: NullPointerException) {
            }
        }

        // history
        val cache = findViewById<Button>(R.id.action_clear_cache)
        cache.setOnClickListener { clearCache() }
        val history = findViewById<Button>(R.id.action_clear_history)
        history.setOnClickListener { clearHistory() }

        // system
        val license = findViewById<Button>(R.id.action_license)
        license.setOnClickListener { // 라이센스 팝업 띄우기
            showLicense()
        }
        val version = findViewById<TextView>(R.id.version)
        version.text = "v" + BuildConfig.VERSION_NAME + "/" + BuildConfig.VERSION_CODE
        val view = findViewById<View>(R.id.pro_purchase)
        if (!isPro) {
            view.visibility = View.VISIBLE
            val packageName = applicationContext.packageName
            val proPackageName = packageName.replace(".free", ".pro")

            // pro 구매하기
            // pro version일때는 숨겨야 함
            val purchase = findViewById<Button>(R.id.purchase)
            purchase.setOnClickListener { launchMarket(this@SettingsActivity, proPackageName) }
        }
    }

    private fun initToolbar() {
        val actionBar = supportActionBar ?: return

        // 로고 버튼
        actionBar.setDisplayShowHomeEnabled(true)

        // Up 버튼
        actionBar.setDisplayHomeAsUpEnabled(true)
    }

    private fun initContentView() {
        if (BuildConfig.SHOW_AD) {
            AdBannerManager.initBannerAd(this, 2)
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val activityView = inflater.inflate(R.layout.activity_settings, null, true)
            val layout = RelativeLayout(this)
            layout.layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
            adView = AdBannerManager.getAdBannerView(2)

            // adview layout params
            var params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            adView?.layoutParams = params
            AdBannerManager.requestAd(2)

            // mainview layout params
            params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
            adView?.id?.let { params.addRule(RelativeLayout.ABOVE, it) }
            activityView.layoutParams = params
            layout.addView(adView)
            layout.addView(activityView)
            setContentView(layout)
        } else {
            setContentView(R.layout.activity_settings)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        @JvmStatic
        fun getLocalIntent(context: Context?): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }
}