package com.duongame.activity.viewer

import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import com.duongame.adapter.ExplorerItem
import com.duongame.adapter.PdfPagerAdapter
import com.duongame.adapter.ViewerPagerAdapter
import com.duongame.db.Book
import com.duongame.db.BookDB.Companion.setLastBook
import com.duongame.file.FileHelper.getMinimizedSize
import com.duongame.file.FileHelper.setPdfFileNameFromPage
import com.duongame.helper.AppHelper.isComicz
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Created by namjungsoo on 2016-11-18.
 */
class PdfActivity : PagerActivity() {
    private var renderer: PdfRenderer? = null
    private var adapter: PdfPagerAdapter? = null

    override fun openNextBook() {
        super.openNextBook()
        if (isGoingNextBook) return
        if (isComicz) openNextBookWithPopup()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = pagerAdapter as PdfPagerAdapter?
        processIntent()
        pager?.offscreenPageLimit = 1
    }

    override fun createPagerAdapter(): ViewerPagerAdapter {
        return PdfPagerAdapter(this)
    }

    override fun onPause() {
        if (isComicz) {
            val book = Book()

            // 고정적인 내용 5개
            book.path = path
            book.name = name
            book.type = ExplorerItem.FILETYPE_ZIP
            book.size = size
            book.total_file = 0 // 파일의 갯수이다.

            // 동적인 내용 6개
            val page = pager!!.currentItem
            book.current_page = page
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //FIX:
                // renderer가 null일 수 있음
                if (renderer != null) {
                    book.total_page = renderer!!.pageCount
                }
            }
            book.current_file = 0
            book.extract_file = 0
            book.side = ExplorerItem.SIDE_ALL
            setLastBook(this, book)
        }
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (renderer != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                renderer!!.close()
            }
        }
    }

    override fun updateName(i: Int) {
        textName!!.text = name
    }

    protected fun processIntent() {
        val imageList: ArrayList<ExplorerItem>
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            val page = extras.getInt("current_page")
            path = extras.getString("path").toString()
            val name = extras.getString("name") ?: ""
            this.name = name
            size = extras.getLong("size")
            nextBook = extras.getString("next_book") ?: ""
            textSize!!.text = getMinimizedSize(size)
            pager!!.adapter = pagerAdapter

            // pdf 파일의 페이지를 체크함
            try {
                val parcel =
                    ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    renderer = PdfRenderer(parcel)
                    imageList = ArrayList()
                    for (i in 0 until renderer!!.pageCount) {
                        // path를 페이지 번호로 사용하자
                        imageList.add(
                            ExplorerItem(
                                setPdfFileNameFromPage(path!!, i),
                                name,
                                null,
                                0,
                                ExplorerItem.FILETYPE_PDF
                            )
                        )
                    }
                    adapter!!.setRenderer(renderer)
                    pagerAdapter!!.imageList = imageList
                    pagerAdapter!!.notifyDataSetChanged()
                    pager!!.currentItem = page
                    textInfo!!.text = "" + renderer!!.pageCount + " pages"
                    updateName(page)
                    updateScrollInfo(page)
                } else {
                    finish()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}