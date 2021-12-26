package com.duongame.db

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.AsyncTask
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.duongame.App.Companion.instance
import com.duongame.R
import com.duongame.activity.viewer.BaseViewerActivity
import com.duongame.activity.viewer.PdfActivity
import com.duongame.activity.viewer.TextActivity
import com.duongame.activity.viewer.ZipActivity
import com.duongame.adapter.ExplorerItem
import com.duongame.adapter.HistoryRecyclerAdapter.HistoryViewHolder
import com.duongame.bitmap.BitmapCacheManager
import com.duongame.file.FileExplorer
import com.duongame.file.FileHelper.getCompressType
import com.duongame.file.FileHelper.getFileName
import com.duongame.file.FileHelper.getFileSize
import com.duongame.file.FileHelper.getFileType
import com.duongame.file.FileHelper.getMinimizedSize
import com.duongame.file.FileHelper.getParentPath
import com.duongame.file.FileHelper.isText
import com.duongame.file.LocalExplorer
import com.duongame.helper.AppHelper.iconResId
import com.duongame.helper.DateHelper.getExplorerDateStringFromDbDateString
import com.duongame.helper.PreferenceHelper.japaneseDirection
import com.duongame.helper.PreferenceHelper.thumbnailDisabled
import com.duongame.helper.ToastHelper.error
import com.duongame.task.thumbnail.LoadThumbnailTask
import com.duongame.task.thumbnail.LoadZipThumbnailTask
import java.io.File
import java.util.*

/**
 * Created by js296 on 2017-08-21.
 */
object BookLoader {
    private fun findNextBook(context: Activity, path: String?, type: Int): String? {
        // 다음 책을 찾는다.
        // 현재 폴더인지 아닌지 확인한다.
        try {
            val lastPath = instance.lastPath
            val filteredList = ArrayList<ExplorerItem>()
            val fileList: ArrayList<ExplorerItem>?
            val folderPath = getParentPath(path!!)

            // 현재 폴더에서 읽은 것이면
            if (lastPath == folderPath) {
                fileList = instance.fileList
            } else {
                // 검색을 해서 찾는다.
                val explorer: FileExplorer = LocalExplorer()
                val result: FileExplorer.Result? = explorer.apply {
                    isRecursiveDirectory = true
                    isHiddenFile = true
                    isExcludeDirectory = false
                    isImageListEnable = false
                }.search(folderPath)
                fileList = result?.fileList
            }

            // 분류속에서 내가 몇번째인지 찾음. 그리고 다음번 것을 찾음
            // 필터링 함
            for (item in fileList!!) {
                if (item.type == type) {
                    filteredList.add(item)
                }
            }

            // 내것의 위치를 찾아보자
            var found = -1
            for (i in filteredList.indices) {
                if (filteredList[i].path == path) {
                    found = i
                    break
                }
            }
            if (found != -1) {
                if (found != filteredList.size - 1) { // 마지막 파일이 아니면
                    // 다음 파일이 있다.
                    return filteredList[found + 1].path
                } else if (filteredList.size > 1) { // 마지막 파일일 경우 나 외에 다른 것이 있으면
                    return filteredList[0].path // 0번을 돌려준다.
                }
            }
        } catch (e: NullPointerException) {
        }
        return null
    }

    @JvmStatic
    fun openNextBook(context: Activity, path: String) {
        var book = BookDB.getBook(context, path)
        if (book == null) {
            book = makeBook(path)
        }
        loadContinue(context, book)
    }

    // 액티비티 시작할때
    @JvmStatic
    fun openLastBook(context: Activity): Boolean {
        val book = BookDB.getLastBook(context)
        if (book != null && book.percent < 100) {
            // DB에서는 책을 읽어야 하지만, 파일이 삭제된 경우에는 제외한다.
            try {
                val file = File(book.path)
                if (file.exists()) {
                    loadWithAlert(context, book, true)
                    return true
                } else {
                    // DB에서 삭제한다.
                    BookDB.clearBook(context, book.path)
                }
            } catch (e: Exception) {
                // 아무것도 하지 않고 false 리턴한다.
            }
        }
        return false
    }

    // 직접 메뉴에서 마지막 책읽기를 선택한 경우
    @JvmStatic
    fun openLastBookDirect(context: Activity): Boolean {
        val book = BookDB.getLastBook(context)
        if (book != null && book.percent < 100) {
            // DB에서는 책을 읽어야 하지만, 파일이 삭제된 경우에는 제외한다.
            try {
                val file = File(book.path)
                if (file.exists()) {
                    loadContinue(context, book)
                    return true
                } else {
                    // DB에서 삭제한다.
                    BookDB.clearBook(context, book.path)
                }
            } catch (e: Exception) {
                // 아무것도 하지 않고 false 리턴한다.
            }
        }
        error(context, R.string.msg_no_lastbook)
        return false
    }

    private fun getViewerClass(book: Book?): Class<out BaseViewerActivity?>? {
        return when (book!!.type) {
            ExplorerItem.FILETYPE_ZIP -> ZipActivity::class.java
            ExplorerItem.FILETYPE_PDF -> PdfActivity::class.java
            ExplorerItem.FILETYPE_TEXT -> TextActivity::class.java
            else -> null
        }
    }

    // 히스토리일 경우는 바로 읽음
    @JvmStatic
    fun loadContinue(context: Activity, book: Book?) {
        val nextBook = findNextBook(context, book!!.path, book.type)
        val intent = Intent(context, getViewerClass(book))
        intent.putExtra("path", book.path)
        intent.putExtra("name", book.name)
        intent.putExtra("current_page", book.current_page)
        intent.putExtra("size", book.size)
        intent.putExtra("extract_file", book.extract_file)
        intent.putExtra("side", book.side)
        intent.putExtra("next_book", nextBook)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
    }

    // 기존에 읽던 책을 처음부터 다시 로딩할 경우
    private fun getIntentNew(context: Activity, book: Book): Intent {
        val nextBook = findNextBook(context, book.path, book.type)
        val intent = Intent(context, getViewerClass(book))
        intent.putExtra("path", book.path)
        intent.putExtra("name", book.name)
        intent.putExtra("current_page", 0)
        intent.putExtra("size", book.size)
        intent.putExtra("current_file", book.current_file)
        intent.putExtra("extract_file", book.extract_file)
        intent.putExtra("side", book.side)
        intent.putExtra("next_book", nextBook)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return intent
    }

    fun makeBook(path: String): Book {
        val item = ExplorerItem(
            path, getFileName(path!!), null, getFileSize(path), getFileType(
                path
            )
        )
        return makeBook(item)
    }

    fun makeBook(item: ExplorerItem): Book {
        val book = Book()
        book.type = item.type
        book.path = item.path
        book.name = item.name
        book.current_page = 0
        book.size = item.size
        book.current_file = 0
        book.extract_file = 0
        book.side = ExplorerItem.SIDE_LEFT
        try {
            if (japaneseDirection) {
                book.side = ExplorerItem.SIDE_RIGHT
            }
        } catch (e: NullPointerException) {
        }
        return book
    }

    private fun loadNew(context: Activity, item: ExplorerItem) {
        val book = makeBook(item)
        loadNew(context, book)
    }

    private fun loadNew(context: Activity, book: Book) {
        val intent = getIntentNew(context, book)
        context.startActivity(intent)
    }

    // 탐색기, 검색일 경우 여기서 읽음
    @JvmStatic
    fun load(context: Activity, item: ExplorerItem, cancelToRead: Boolean) {
        val book = getHistory(context, item)

        // 새로 페이지 0부터 읽음
        if (book == null) {
            loadNew(context, item)
        } else {
            // 팝업을 띄운다음에 읽자
            loadWithAlert(context, book, cancelToRead)
        }
    }

    private fun updateHistoryItem(context: Activity, view: View, book: Book) {
        val holder = HistoryViewHolder(view)
        updateBookHolder(context, holder, book)
        loadBookBitmap(context, holder, book.path)
        holder.more.visibility = View.GONE
    }

    private fun loadWithAlert(context: Activity, book: Book, cancelToRead: Boolean) {
        val view = context.layoutInflater.inflate(R.layout.item_history, null, false)
        updateHistoryItem(context, view, book)

        // 이부분은 물어보고 셋팅하자.
        val builder = AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.comicz_name_free))
            .setView(view)
            .setMessage(
                String.format(
                    context.getString(R.string.msg_last_page),
                    book.current_page + 1
                )
            )
            .setIcon(iconResId) // 연속해서 읽을경우
            .setPositiveButton(context.getString(R.string.ok)) { dialog, which ->
                loadContinue(
                    context,
                    book
                )
            } // 연속해서 읽지 않거나, 취소함
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, which ->
                if (!cancelToRead) { // 처음부터 읽음
                    loadNew(context, book)
                }
            }
        builder.show()
    }

    private fun getHistory(context: Activity, item: ExplorerItem): Book? {
        return BookDB.getBook(context, item.path)
    }

    private fun loadDefaultThumbnail(context: Context, holder: HistoryViewHolder, path: String?) {
        if (getCompressType(path!!) != ExplorerItem.COMPRESSTYPE_OTHER) {
            // 압축파일
            holder.thumb.setImageResource(R.drawable.ic_file_zip)
        } else {
            // PDF
            holder.thumb.setImageResource(R.drawable.ic_file_pdf)
        }
    }

    @JvmStatic
    fun loadBookBitmap(context: Context, holder: HistoryViewHolder, path: String?) {
        // zip 파일의 썸네일을 읽자
        if (isText(path!!)) {
            holder.thumb.setImageResource(R.drawable.ic_file_txt)
            return
        }
        loadDefaultThumbnail(context, holder, path)
        try {
            if (!thumbnailDisabled) { // 썸네일 비활성화가 아니라면
                val bitmap = BitmapCacheManager.getThumbnail(path)
                if (bitmap == null) {
                    when (getCompressType(path)) {
                        ExplorerItem.COMPRESSTYPE_ZIP, ExplorerItem.COMPRESSTYPE_SEVENZIP, ExplorerItem.COMPRESSTYPE_RAR -> {
                            val task = LoadZipThumbnailTask(context, holder.thumb, holder.more)
                            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path)
                        }
                        else -> if (path.endsWith(".pdf")) {
                            val task = LoadThumbnailTask(
                                context,
                                holder.thumb,
                                holder.more,
                                ExplorerItem.FILETYPE_PDF
                            )
                            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path)
                        }
                    }
                } else {
                    holder.thumb.setImageBitmap(bitmap)
                    BitmapCacheManager.setThumbnail(path, bitmap, holder.thumb)
                }
            }
        } catch (e: NullPointerException) {
        }
    }

    private fun getPageText(book: Book): String {
        // 압축이 다 풀렸으면, 페이지를 기준으로 한다.
        // 압축파일이 아니라면 둘다 0이다.
        return if (book.extract_file == book.total_file) {
            // 텍스트가 아닐경우에는 PDF나 ZIP이다.
            if (book.type != ExplorerItem.FILETYPE_TEXT) (book.current_page + 1).toString() + "/" + book.total_page else {
                TextBook.getPageText(book)
            }
        } else {
            (book.current_file + 1).toString() + "/" + book.total_file
        }
    }

    private fun getPercentText(book: Book): String {
        return "" + book.percent + "%"
    }

    fun updateBookHolder(context: Context, holder: HistoryViewHolder, book: Book) {
        holder.name.text = book.name
        holder.size.text = getMinimizedSize(book.size)
        holder.date.text = getExplorerDateStringFromDbDateString(book.date)
        holder.page.text = getPageText(book)
        holder.percent.text = getPercentText(book)
        holder.progressBar.max = 100
        holder.progressBar.progress = book.percent
        if (book.percent == 100) {
            holder.progressBar.progressDrawable.setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN)
        } else {
            holder.progressBar.progressDrawable.setColorFilter(
                ContextCompat.getColor(context, R.color.colorAccent),
                PorterDuff.Mode.SRC_IN
            )
        }
        holder.more.tag = book.path
    }
}