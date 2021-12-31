package com.duongame.activity.viewer

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ViewSwitcher
import androidx.core.content.ContextCompat
import com.duongame.BuildConfig
import com.duongame.R
import com.duongame.adapter.ExplorerItem
import com.duongame.adapter.PhotoPagerAdapter
import com.duongame.adapter.ViewerPagerAdapter
import com.duongame.archive.ArchiveLoader
import com.duongame.archive.ArchiveLoader.ArchiveLoaderListener
import com.duongame.bitmap.BitmapCacheManager
import com.duongame.db.Book
import com.duongame.db.BookDB.Companion.setLastBook
import com.duongame.helper.AlertHelper.showAlert
import com.duongame.helper.AlertHelper.showAlertWithAd
import com.duongame.helper.AppHelper.isComicz
import com.duongame.manager.AdBannerManager.initPopupAd
import net.lingala.zip4j.exception.ZipException
import timber.log.Timber
import java.util.*

/**
 * Created by namjungsoo on 2016-11-19.
 */
class ZipActivity : PagerActivity() {
    //RAR
    //private final ZipLoader zipLoader = new ZipLoader();
    private val zipLoader = ArchiveLoader()
    private var side = ExplorerItem.SIDE_LEFT
    private var lastSide = ExplorerItem.SIDE_LEFT
    private var totalFileCount = 0
    private var extractFileCount = 0 // 압축 풀린 파일의 갯수
    private var zipExtractCompleted = false
    private fun changeSide(side: Int) {
        lastSide = this.side
        this.side = side
    }

    override fun openNextBook() {
        super.openNextBook()
        if (isGoingNextBook) return
        if (nextBook == null) return
        if (isComicz) openNextBookWithPopup()
    }

    // 퍼센트를 기록함
    override fun updateScrollInfo(position: Int) {
        if (totalFileCount == 0) {
            textPage!!.text = (position + 1).toString() + "/" + pagerAdapter!!.count
        } else {
            val loadingPercent = (extractFileCount + 1) * 100 / totalFileCount
            if (loadingPercent == 100) {
                textPage!!.text = (position + 1).toString() + "/" + pagerAdapter!!.count
            } else {
                textPage!!.text =
                    (position + 1).toString() + "/" + pagerAdapter!!.count + String.format(
                        " (%02d%%)",
                        loadingPercent
                    )
            }
        }
        seekPage!!.max = pagerAdapter!!.count - 1
        seekPage!!.progress = position
    }

    //RAR
    //    private ZipLoader.ZipLoaderListener listener = new ZipLoader.ZipLoaderListener() {
    private val listener: ArchiveLoaderListener = object : ArchiveLoaderListener {
        override fun onSuccess(
            i: Int,
            zipImageList: ArrayList<ExplorerItem>?,
            totalFileCount: Int
        ) {
            this@ZipActivity.totalFileCount = totalFileCount
            extractFileCount = i
            val imageList = zipImageList!!.clone() as ArrayList<ExplorerItem>
            pagerAdapter!!.imageList = imageList
            pagerAdapter!!.notifyDataSetChanged()
            updateScrollInfo(pager!!.currentItem)
            //            Timber.e("onSuccess i=" + i);
        }

        override fun onFail(i: Int, name: String?) {
            Timber.e("onFail i=$i $name")
        }

        override fun onFinish(zipImageList: ArrayList<ExplorerItem>?, totalFileCount: Int) {
            // 체크해놓고 나중에 파일을 지우지 말자
            zipExtractCompleted = true
            this@ZipActivity.totalFileCount = totalFileCount
            extractFileCount = totalFileCount
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pagerAdapter!!.exifRotation = false

        // 정보를 표기하는 대신에 페이지 변경 아이콘을 표시
        val switcher = findViewById<View>(R.id.switcher_info) as ViewSwitcher
        switcher.displayedChild = 1
        try {
            processIntent()
        } catch (e: ZipException) {
            e.printStackTrace()
        }
        updateTopSidePanelColor()
    }

    override fun onPause() {
        if (isComicz) {
            zipLoader.cancelTask()
            val book = Book()

            // 고정적인 내용 5개
            book.path = path
            book.name = name
            book.type = ExplorerItem.FILETYPE_ZIP
            book.size = size
            book.total_file = totalFileCount // 파일의 갯수이다.

            // 동적인 내용 6개
            val page = pager!!.currentItem
            book.current_page = page
            try {
                // 페이지로 잘려져 있다.
                val zipImageList = pagerAdapter!!.imageList
                if (zipImageList != null) {
                    book.total_page = zipImageList.size
                    val item = zipImageList[page]
                    book.current_file = item.orgIndex

                    //TODO: 숫자가 맞는지 검증할것
                    if (zipExtractCompleted) {
                        // 전부 압축이 다 풀렸으므로 전체 파일 갯수를 입력해준다.
                        book.extract_file = extractFileCount
                    } else {
                        // 앞으로 읽어야할 위치를 기억하기 위해 +1을 함
                        book.extract_file = extractFileCount + 1
                    }
                    book.side = side
                    book.last_file = item.path
                    setLastBook(this, book)
                }
            } //TODO: 일단 막아둠
            catch (e: IndexOutOfBoundsException) {
            }
        }
        super.onPause()
    }

    protected val imageList: ArrayList<ExplorerItem>?
        protected get() = null

    override fun createPagerAdapter(): ViewerPagerAdapter? {
        // 이때는 애니메이션을 하지 않는다.
        return PhotoPagerAdapter(this, false)
    }

    override fun updateName(i: Int) {
        textName!!.text = name
    }

    @Throws(ZipException::class)
    protected fun processIntent() {
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            val page = extras.getInt("current_page")
            path = extras.getString("path").toString()
            name = extras.getString("name").toString()
            size = extras.getLong("size")
            extractFileCount = extras.getInt("extract_file")
            side = extras.getInt("side")
            nextBook = extras.getString("next_book") ?: ""
            lastSide = side

            // initPagerAdapter의 기능이다.
            pager!!.adapter = pagerAdapter // setAdapter이후에 imageList를 변경하면 항상 notify해주어야 한다.

            // zip 파일을 로딩한다.
            val imageList = zipLoader.load(this, path, listener, extractFileCount, side, false)
            if (imageList == null || imageList.size <= 0) {
                val title = getString(R.string.comicz_name_free)
                val content = getString(R.string.msg_no_image_in_zip)
                val positiveListener = DialogInterface.OnClickListener { dialog, which -> finish() }
                if (BuildConfig.SHOW_AD && !isAdRemoveReward) {
                    showAlertWithAd(
                        this,
                        title,
                        content,
                        positiveListener,
                        null,
                        true
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
                        true
                    )
                }
                return
            }

            // 실질적으로 아무런 역할을 하지 않는다. ZipLoaderListener에서 담당한다.
            pagerAdapter!!.imageList = imageList
            pagerAdapter!!.notifyDataSetChanged()
            pager!!.currentItem = page
            updateScrollInfo(page)
            updateName(page)
        }
    }

    private fun updateTopSidePanelColor() {
        var iv: ImageView
        var tv: TextView
        when (side) {
            ExplorerItem.SIDE_LEFT -> {
                iv = findViewById(R.id.img_left)
                tv = findViewById(R.id.text_left)
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_orange_light))
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light))

                // 나머지 두개를 꺼주어야 한다.
                iv = findViewById(R.id.img_right)
                tv = findViewById(R.id.text_right)
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.white))
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                iv = findViewById(R.id.img_both)
                tv = findViewById(R.id.text_both)
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.white))
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
            ExplorerItem.SIDE_RIGHT -> {
                iv = findViewById(R.id.img_right)
                tv = findViewById(R.id.text_right)
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_orange_light))
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light))

                // 나머지 두개를 꺼주어야 한다.
                iv = findViewById(R.id.img_left)
                tv = findViewById(R.id.text_left)
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.white))
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                iv = findViewById(R.id.img_both)
                tv = findViewById(R.id.text_both)
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.white))
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
            ExplorerItem.SIDE_ALL -> {
                iv = findViewById(R.id.img_both)
                tv = findViewById(R.id.text_both)
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_orange_light))
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light))

                // 나머지 두개를 꺼주어야 한다.
                iv = findViewById(R.id.img_right)
                tv = findViewById(R.id.text_right)
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.white))
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                iv = findViewById(R.id.img_left)
                tv = findViewById(R.id.text_left)
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.white))
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
        }
    }

    private fun updatePageSide() {
        if (pagerAdapter == null) return
        if (pagerAdapter!!.imageList == null) return

        // Task가 실행중이면 pause
        // 그리고 리스트에서 좌우를 변경함
        // 이후 task에서는 변경된 것으로 작업함
        if (!zipExtractCompleted) {
            zipLoader.pause()
        }

        // 전부 초기화 한다.
        // 초기화 하기전에 task를 전부 stop한다.
        pagerAdapter!!.stopAllTasks()

        // ImageView bitmap을 전부 null로 셋팅한다.
        val count = pager!!.childCount
        for (i in 0 until count) {
            val view = pager!!.getChildAt(i) ?: continue
            val imageView = view.findViewById<ImageView>(R.id.image_viewer) ?: continue
            imageView.setImageBitmap(null)
        }
        pager!!.removeAllViews()
        BitmapCacheManager.removeAllPages()
        BitmapCacheManager.removeAllBitmaps()

        // 데이터를 업데이트 하자.
        // 좌우를 변경한다.
        //final ArrayList<ExplorerItem> imageList = pagerAdapter.getImageList();
        val imageList = pagerAdapter!!.imageList?.clone() as ArrayList<ExplorerItem>
        val newImageList = ArrayList<ExplorerItem>()
        for (i in imageList.indices) {
            val item = imageList[i]

            // 잘려진 데이터는 둘중 하나를 삭제한다.
            if (side == ExplorerItem.SIDE_ALL) {
                if (item.side != ExplorerItem.SIDE_ALL) {
                    // 둘중하나는 삭제 해야 함
                    // 앞에꺼가 있는지 확인하고 삭제하자
                    if (i == 0) continue
                    val item1 = imageList[i - 1]

                    // 같은 파일이면...
                    if (item.path == item1.path) {
                        // 2번째것을 삭제하고, 1번째것은 값을 변경하자
                        val newItem = item.clone() as ExplorerItem
                        newItem.side = ExplorerItem.SIDE_ALL
                        newImageList.add(newItem)
                    } else {
                    }
                } else { // 이미 SIDE_ALL이면 그냥 더하자
                    val newItem = item.clone() as ExplorerItem
                    newImageList.add(newItem)
                }
            } else { // 좌우 변경, 강제 BOTH에서 잘라야 할 것이라면... (side = LEFT or RIGHT)
                // 같은 파일명을 공유하는 애들끼리 LEFT, RIGHT 순서를 체크한 후에 바꿀 필요가 있을 경우에 바꾸자.
                // 현재 포지션은 바뀌지 않는다.
                if (item.side == ExplorerItem.SIDE_ALL) {
                    // 원래 잘려야할 애들이라면 잘라주어야 한다.
                    if (item.width > item.height) {
                        val left = item.clone() as ExplorerItem
                        val right = item.clone() as ExplorerItem
                        left.side = ExplorerItem.SIDE_LEFT
                        right.side = ExplorerItem.SIDE_RIGHT
                        if (side == ExplorerItem.SIDE_LEFT) {
                            newImageList.add(left)
                            newImageList.add(right)
                        } else if (side == ExplorerItem.SIDE_RIGHT) {
                            newImageList.add(right)
                            newImageList.add(left)
                        }
                    } else {
                        // 잘려야 될 애들이 아니면 그냥 넣어준다.
                        val newItem = item.clone() as ExplorerItem
                        newImageList.add(newItem)
                    }
                } else {
                    if (i == 0) continue
                    val item1 = imageList[i - 1]

                    // 같은 파일일 경우 좌우 순서를 바꿈
                    if (item.path == item1.path) {
                        val left = item.clone() as ExplorerItem
                        val right = item.clone() as ExplorerItem
                        left.side = ExplorerItem.SIDE_LEFT
                        right.side = ExplorerItem.SIDE_RIGHT
                        if (side == ExplorerItem.SIDE_LEFT) {
                            newImageList.add(left)
                            newImageList.add(right)
                        } else if (side == ExplorerItem.SIDE_RIGHT) {
                            newImageList.add(right)
                            newImageList.add(left)
                        }
                    }
                }
            }
        }

        // 단순 좌우 변경인지, split 변경인지 확인한다.
        val position = pager!!.currentItem
        val lastPath = imageList[position].path

        // setAdapter를 다시 해줘야 모든 item이 다시 instantiate 된다.
        pagerAdapter!!.imageList = newImageList
        pagerAdapter!!.notifyDataSetChanged()
        pager!!.adapter = pagerAdapter
        if (lastSide == ExplorerItem.SIDE_ALL || side == ExplorerItem.SIDE_ALL) {
            // 페이지 연산을 파일명 단위로 한다.
            var i: Int
            i = 0
            while (i < newImageList.size) {
                if (newImageList[i].path == lastPath) {
                    break
                }
                i++
            }
            pager!!.currentItem = i
        } else {
            pager!!.currentItem = position
        }
        if (!zipExtractCompleted) {
            zipLoader.setZipImageList(newImageList.clone() as ArrayList<ExplorerItem>?)
            zipLoader.setSide(side)
            zipLoader.resume()
        }
    }

    override var isFullscreen: Boolean = false
        set(fullscreen) {
            super.isFullscreen = fullscreen
            if (!fullscreen) {
                // top_side_panel을 보이게 하자
                val topOptionPanel = findViewById<LinearLayout>(R.id.panel_top_option)
                topOptionPanel.visibility = View.VISIBLE
                val layoutLeft = findViewById<LinearLayout>(R.id.layout_left)
                layoutLeft.setOnClickListener(View.OnClickListener {
                    if (side == ExplorerItem.SIDE_LEFT) return@OnClickListener
                    changeSide(ExplorerItem.SIDE_LEFT)
                    updateTopSidePanelColor()
                    updatePageSide()
                })
                val layoutRight = findViewById<LinearLayout>(R.id.layout_right)
                layoutRight.setOnClickListener(View.OnClickListener {
                    if (side == ExplorerItem.SIDE_RIGHT) return@OnClickListener
                    changeSide(ExplorerItem.SIDE_RIGHT)
                    updateTopSidePanelColor()
                    updatePageSide()
                })
                val layoutBoth = findViewById<LinearLayout>(R.id.layout_both)
                layoutBoth.setOnClickListener(View.OnClickListener {
                    if (side == ExplorerItem.SIDE_ALL) return@OnClickListener
                    changeSide(ExplorerItem.SIDE_ALL)
                    updateTopSidePanelColor()
                    updatePageSide()
                })
            }
            field = fullscreen
        }

//    override fun setFullscreen(fullscreen: Boolean) {
//        super.setFullscreen(fullscreen)
//        if (!fullscreen) {
//            // top_side_panel을 보이게 하자
//            val topOptionPanel = findViewById<LinearLayout>(R.id.panel_top_option)
//            topOptionPanel.visibility = View.VISIBLE
//            val layoutLeft = findViewById<LinearLayout>(R.id.layout_left)
//            layoutLeft.setOnClickListener(View.OnClickListener {
//                if (side == ExplorerItem.SIDE_LEFT) return@OnClickListener
//                changeSide(ExplorerItem.SIDE_LEFT)
//                updateTopSidePanelColor()
//                updatePageSide()
//            })
//            val layoutRight = findViewById<LinearLayout>(R.id.layout_right)
//            layoutRight.setOnClickListener(View.OnClickListener {
//                if (side == ExplorerItem.SIDE_RIGHT) return@OnClickListener
//                changeSide(ExplorerItem.SIDE_RIGHT)
//                updateTopSidePanelColor()
//                updatePageSide()
//            })
//            val layoutBoth = findViewById<LinearLayout>(R.id.layout_both)
//            layoutBoth.setOnClickListener(View.OnClickListener {
//                if (side == ExplorerItem.SIDE_ALL) return@OnClickListener
//                changeSide(ExplorerItem.SIDE_ALL)
//                updateTopSidePanelColor()
//                updatePageSide()
//            })
//        }
//    }
}