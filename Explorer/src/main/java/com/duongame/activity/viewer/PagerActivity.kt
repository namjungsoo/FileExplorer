package com.duongame.activity.viewer

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.duongame.BuildConfig
import com.duongame.R
import com.duongame.adapter.ViewerPagerAdapter
import com.duongame.bitmap.BitmapCacheManager
import com.duongame.db.BookLoader.openNextBook
import com.duongame.file.FileHelper.getFileName
import com.duongame.helper.AlertHelper.showAlert
import com.duongame.helper.AlertHelper.showAlertWithAd
import com.duongame.helper.AppHelper.appName
import com.duongame.helper.PreferenceHelper.autoPagingTime
import com.duongame.helper.PreferenceHelper.pagingAnimationDisabled
import com.duongame.listener.PagerOnTouchListener
import com.duongame.view.ViewPagerEx
import com.felipecsl.gifimageview.library.GifImageView
import timber.log.Timber
import java.util.*

/**
 * Created by namjungsoo on 2016-11-19.
 */
// 지원 목록: Photo, Pdf, Zip
// +전체화면
//
// +좌우 view pager
// +하단 toolbox
open class PagerActivity : BaseViewerActivity() {
    // 파일의 정보
    protected lateinit var path: String
    protected lateinit var name: String
    protected var size // zip 파일의 용량
            : Long = 0
    var pager: ViewPagerEx? = null
    var pagerAdapter: ViewerPagerAdapter? = null
    var pagerIdle = true

    //if(this.gifImageView != null)
    //    this.gifImageView.stopAnimation();
    // startAnimation은 외부에서 수행함
    var gifImageView: GifImageView? = null
    private var autoTime = 0
    private var lastAutoTime = 0
    private var textAutoTime: TextView? = null
    private var btnPlusTime: Button? = null
    private var btnMinusTime: Button? = null
    private var timer: Timer? = null
    protected var isGoingNextBook = false
    protected lateinit var nextBook: String

    internal class PagingInfo {
        var page = 0
        var smoothScroll = false
        var pager: ViewPagerEx? = null
        var activity: PagerActivity? = null
    }

    internal class TimerHandler : Handler() {
        override fun handleMessage(msg: Message) {
            val info = msg.obj as PagingInfo
            if (msg.what == AUTO_PAGING) {
                info.pager!!.setCurrentItem(info.page, info.smoothScroll)
            } else if (msg.what == GOTO_NEXTBOOK) {
                info.activity!!.openNextBook()
            }
        }
    }

    var handler: Handler = TimerHandler()
    protected fun openNextBookWithPopup() {
        isGoingNextBook = true

        // 팝업을 띄운다.
        // 확인시 현재 위치에서 Activity를 재시작 한다.
        val fileName = getFileName(nextBook!!)
        @SuppressLint("StringFormatInvalid", "LocalSuppress") val message =
            String.format(getString(R.string.msg_next_book), fileName)
        if (BuildConfig.SHOW_AD && !isAdRemoveReward) {
            showAlertWithAd(this,
                appName,
                message,
                { dialog, which -> openNextBook(this@PagerActivity, nextBook) }, { dialog, which ->
                    // 취소일 경우 액티비티 닫음
                    finish()
                }, null
            )
        } else {
            showAlert(this,
                appName,
                message,
                null,
                { dialog, which -> openNextBook(this@PagerActivity, nextBook) }, { dialog, which ->
                    // 취소일 경우 액티비티 닫음
                    finish()
                }, null
            )
        }

        // 옵션을 어떻게 넣을지 확인한다.
        isGoingNextBook = false
    }

    override fun updateNightMode() {
        super.updateNightMode()
        if (pager == null) return
        val count = pager!!.childCount
        for (i in 0 until count) {
            val view = pager!!.getChildAt(i) ?: continue
            val imageView = view.findViewById<ImageView>(R.id.image_viewer) ?: continue
            pagerAdapter!!.updateColorFilter(imageView)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        contentViewResId = R.layout.activity_pager
        super.onCreate(savedInstanceState)
        initToolBox()
        initPager()
        initPagerListeners()

        // 전체 화면으로 들어감
        isFullscreen = true
    }

    // smooth 연산
    val smoothScroll: Boolean
        get() {
            // smooth 연산
            var smoothScroll = true
            try {
                if (pagingAnimationDisabled) {
                    smoothScroll = false
                }
            } catch (e: NullPointerException) {
            }
            return smoothScroll
        }

    fun resumeTimer() {
        if (isFullscreen && autoTime > 0 && lastAutoTime != autoTime) { // 이제 타이머를 시작
            Timber.e("resumeTimer autoTime=$autoTime")
            timer = Timer()
            timer!!.schedule(object : TimerTask() {
                override fun run() {
                    val current = pager!!.currentItem

                    //TODO: 마지막 페이지
                    val info = PagingInfo()
                    val msg = Message()
                    if (current == pagerAdapter!!.count - 1) {
                        info.activity = this@PagerActivity
                        msg.obj = info
                        msg.what = GOTO_NEXTBOOK
                    } else {
                        info.page = current + 1
                        info.smoothScroll = smoothScroll
                        info.pager = pager
                        msg.obj = info
                        msg.what = AUTO_PAGING
                    }
                    handler.sendMessage(msg)
                }
            }, (autoTime * SEC_TO_MS).toLong(), (autoTime * SEC_TO_MS).toLong())
            lastAutoTime = autoTime
        }
    }

    fun pauseTimer() {
        if (timer != null) timer!!.cancel()
        lastAutoTime = 0 // 초기화 시켜준다.
        //        timer = new Timer();
    }

    override fun onResume() {
        super.onResume()
        Timber.e("onResume")
        resumeTimer()
    }

    override fun onPause() {
        super.onPause()
        pauseTimer()
    }

    override fun updateFullscreen(isFullscreen: Boolean) {
        if (isFullscreen) { // 전체화면으로 돌아가면 timer 발동
            resumeTimer()
        } else {
            pauseTimer()
        }
    }

    fun updateAutoTime(updatePreference: Boolean) {
        textAutoTime!!.text = autoTime.toString()
        if (updatePreference) {
            autoPagingTime = autoTime
        }
    }

    // 페이징 UI 초기화
    // Fullscreen이 false되는 시점에 timer를 on
    fun initAutoPagingUI() {
        textAutoTime = findViewById(R.id.auto_time)
        textAutoTime?.setVisibility(View.VISIBLE)
        lastAutoTime = autoTime
        autoTime = autoPagingTime
        updateAutoTime(false)
        btnPlusTime = findViewById(R.id.plus_time)
        btnPlusTime?.setVisibility(View.VISIBLE)
        btnPlusTime?.setOnClickListener(View.OnClickListener {
            if (autoTime < AUTO_SEC_MAX) {
                lastAutoTime = autoTime
                autoTime++
                updateAutoTime(true)
            }
        })
        btnMinusTime = findViewById(R.id.minus_time)
        btnMinusTime?.setVisibility(View.VISIBLE)
        btnMinusTime?.setOnClickListener(View.OnClickListener {
            if (autoTime > 0) {
                lastAutoTime = autoTime
                autoTime--
                updateAutoTime(true)
            }
        })
    }

    override fun initToolBox() {
        super.initToolBox()
        initAutoPagingUI()
        seekPage!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                //seekPage.setProgress(progress);
                //pager.setCurrentItem(progress-1);
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val page = seekBar.progress
                val current = pager!!.currentItem
                if (Math.abs(current - page) > 2) {
                    // 모든 로딩 중인 태스크를 정리하고 비트맵을 리사이클을 한다.
                    pagerAdapter!!.stopAllTasks()

                    // ImageView bitmap을 전부 null로 셋팅한다.
                    val count = pager!!.childCount
                    for (i in 0 until count) {
                        val view = pager!!.getChildAt(i) ?: continue
                        val imageView = view.findViewById<ImageView>(R.id.image_viewer) ?: continue
                        imageView.setImageBitmap(null)
                    }

                    // 모든 캐쉬 비트맵을 정리한다.
                    BitmapCacheManager.removeAllPages()
                    BitmapCacheManager.removeAllBitmaps()
                }
                Timber.e("setCurrentItem")
                pager!!.setCurrentItem(page, false)
            }
        })
        leftPage!!.setOnClickListener {
            val current = pager!!.currentItem
            if (current > 0) {
                val smooth = smoothScroll
                pager!!.setCurrentItem(current - 1, smooth)
            }
        }
        rightPage!!.setOnClickListener {
            val current = pager!!.currentItem
            if (current == pagerAdapter!!.count - 1) {
                openNextBook()
            } else {
                val smooth = smoothScroll
                pager!!.setCurrentItem(current + 1, smooth)
            }
        }
    }

    fun getPager(): ViewPager? {
        return pager
    }

    protected open fun createPagerAdapter(): ViewerPagerAdapter? {
        return null
    }

    protected fun initPager() {
        pager = findViewById(R.id.pager)
        pagerAdapter = createPagerAdapter()
        //        pager.setOffscreenPageLimit(OFFLINE_PAGE_LIMIT);
    }

    protected open fun updateScrollInfo(position: Int) {
        val count = pagerAdapter!!.count
        textPage!!.text = (position + 1).toString() + "/" + count
        seekPage!!.max = count - 1

        // 이미지가 1개일 경우 처리
        if (position == 0 && count == 1) {
            seekPage!!.progress = count
            seekPage!!.isEnabled = false
        } else {
            seekPage!!.progress = position
            seekPage!!.isEnabled = true
        }
    }

    open fun updateInfo(position: Int) {}
    open fun openNextBook() {
        // 마지막 페이지를 드래깅 하고 잇는 것임
        Timber.w("onPageScrolled last page dragging")

        //TODO: PDF와 ZIP에 대해서만 다음 책을 읽을수 있다.
    }

    protected fun initPagerListeners() {
        pager!!.addOnPageChangeListener(object : OnPageChangeListener {
            var lastState = 0
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                Timber.w("onPageScrolled position=$position positionOffset=$positionOffset positionOffsetPixels=$positionOffsetPixels")
                if (pagerAdapter == null) {
                    return
                }
                if (pagerAdapter!!.count == position + 1) {
                    if (lastState == ViewPager.SCROLL_STATE_DRAGGING) {
                        openNextBook()
                    }
                }
            }

            override fun onPageSelected(position: Int) {
                Timber.w("onPageSelected position=$position")
                updateScrollInfo(position)
                updateName(position)
                updateInfo(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                Timber.w("onPageScrollStateChanged state=$state")
                when (state) {
                    ViewPager.SCROLL_STATE_IDLE -> pagerIdle = true
                    ViewPager.SCROLL_STATE_DRAGGING, ViewPager.SCROLL_STATE_SETTLING -> pagerIdle =
                        false
                }
                lastState = state
            }
        })
        pager!!.setOnTouchListener(PagerOnTouchListener(this))
    }

    override fun updateName(i: Int) {
        textName!!.text = pagerAdapter?.imageList?.get(i)?.name
    }

    fun stopGifAnimation() {
        if (gifImageView != null) {
            gifImageView!!.stopAnimation()
        }
    }

    public override fun onStop() {
        super.onStop()

//        // 종료시에 현재 GIF가 있으면 stop 해줌
//        stopGifAnimation();
    }

    companion object {
        private const val AUTO_PAGING = 1
        private const val GOTO_NEXTBOOK = 2
        const val SEC_TO_MS = 1000
        const val AUTO_SEC_MAX = 10
    }
}