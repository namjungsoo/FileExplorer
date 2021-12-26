package com.duongame.activity.viewer

import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import com.duongame.R
import com.duongame.db.BookDB.Companion.setLastBook
import com.duongame.db.TextBook.LINES_PER_PAGE
import com.duongame.db.TextBook.buildTextBook
import com.duongame.db.TextBook.buildTextBook2
import com.duongame.file.FileHelper.BLOCK_SIZE
import com.duongame.file.FileHelper.getMinimizedSize
import com.duongame.helper.PreferenceHelper
import com.duongame.listener.TextOnTouchListener
import com.duongame.manager.FontManager.getTypeFaceNanumMeyongjo
import org.mozilla.universalchardet.UniversalDetector
import java.io.*
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by namjungsoo on 2016-11-18.
 */
// text는 pager가 아니라 상하 스크롤되는 액티비티
open class TextActivity : BaseViewerActivity() {
    private var scrollText: ScrollView? = null
    private var textContent: TextView? = null
    private var progressBar: ProgressBar? = null

    private lateinit var path: String
    private lateinit var name: String
    private var size: Long = 0
    private var page = 0
    private var scroll = 0
    private var fontSize = 20
    private var fontIndex = 4
    private var useScrollV2 = false
    private val lineList = arrayListOf<String>()
    private val fontSizeArray = intArrayOf(
        12, 14, 16, 18, 20,
        24, 28, 32, 36, 40,
        44, 48, 54, 60, 66,
        72
    )
    var onGlobalLayoutListener: OnGlobalLayoutListener = object : OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            val scrollY = scrollY
            scrollText?.scrollY = scrollY
            scrollText?.viewTreeObserver?.removeGlobalOnLayoutListener(this)
        }
    }

    override fun updateNightMode() {
        super.updateNightMode()
        updateNightModeText()
    }

    fun updateNightModeText() {
        try {
            if (PreferenceHelper.nightMode) {
                scrollText?.setBackgroundColor(Color.BLACK)
                textContent?.setTextColor(Color.rgb(192, 192, 192))
            } else {
                scrollText?.setBackgroundColor(Color.WHITE)
                textContent?.setTextColor(Color.BLACK)
            }
        } catch (e: NullPointerException) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        contentViewResId = R.layout.activity_text

        // onCreate안에서 updateNightModeText가 호출됨
        super.onCreate(savedInstanceState)
        initToolBox()
        scrollText = findViewById(R.id.scroll_text)
        textContent = findViewById(R.id.text_content)
        textContent?.setTypeface(getTypeFaceNanumMeyongjo(this))
        textContent?.setTextSize(fontSize.toFloat())
        textContent?.setLineSpacing(0f, 1.5f)
        updateNightModeText()
        scrollText?.setOnTouchListener(TextOnTouchListener(this))
        scrollText?.getViewTreeObserver()?.addOnScrollChangedListener { updateScrollInfo(page) }
        scrollText?.getViewTreeObserver()?.addOnGlobalLayoutListener(onGlobalLayoutListener)
        progressBar = findViewById<View>(R.id.progress_text) as ProgressBar
        processIntent()

        // 전체 화면으로 들어감
        isFullscreen = true
    }

    protected fun processIntent() {
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            path = extras.getString("path").toString()
            name = extras.getString("name").toString()
            size = extras.getLong("size")

            // 이때 DB의 마이그레이션이 필요할수 있음
            val current_page = extras.getInt("current_page")
            val current_file = extras.getInt("current_file")
            page = current_page / LINES_PER_PAGE
            if (current_file == 0) {
                scroll = current_page - LINES_PER_PAGE * page
                useScrollV2 = false
            } else {
                scroll = current_file
                useScrollV2 = true
            }
            textSize?.text = getMinimizedSize(size)
            textName?.text = name
            progressBar?.visibility = View.VISIBLE
            val task = LoadTextTask(this)
            task.execute(path)
        }
    }

    fun updateFontSize() {
        fontSize = fontSizeArray[fontIndex]
        textContent?.textSize = fontSize.toFloat()
    }// 현재 스크롤 위치를 얻어보자

    // 10000으로 곱한다.
    // 1/10000 퍼센트를 지정함
    private val percent2: Int
        get() {
            scrollText?.let {
                // 현재 스크롤 위치를 얻어보자
                val maxScroll = it.getChildAt(0).height - it.height
                if (maxScroll <= 0) {
                    return 10000
                }
                val scrollY = it.scrollY

                // 10000으로 곱한다.
                return scrollY * LINES_PER_PAGE * 10 / maxScroll
            }
            return 0
        }

    // 현재 스크롤 위치를 얻어보자
    val percent: Int
        get() {
            scrollText?.let {
                // 현재 스크롤 위치를 얻어보자
                val maxScroll = it.getChildAt(0).height - it.height
                if (maxScroll <= 0) {
                    return 1000
                }
                val scrollY = it.scrollY
                return scrollY * LINES_PER_PAGE / maxScroll
            }
            return 0
        }

    public override fun onPause() {
        if (USE_10K_PERCENT) {
            var percent = percent2
            if (percent >= LINES_PER_PAGE * 10) { // 9999를 만들어야 페이지를 넘어가지 않는다.
                percent = LINES_PER_PAGE * 10 - 1
            }
            val book = buildTextBook2(path, name, size, percent, page, lineList.size)
            setLastBook(this, book)
        } else {
            var percent = percent
            if (percent >= LINES_PER_PAGE) {
                percent = LINES_PER_PAGE - 1
            }
            val book = buildTextBook(path, name, size, percent, page, lineList.size)
            setLastBook(this, book)
        }
        super.onPause()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (fontIndex + 1 < MAX_FONT_SIZE_INDEX) {
                    fontIndex++
                    updateFontSize()
                }
                true
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (fontIndex - 1 >= 0) {
                    fontIndex--
                    updateFontSize()
                }
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun initToolBox() {
        super.initToolBox()
        pagingAnim?.visibility = View.INVISIBLE
        seekPage?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            var dragging = false
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (dragging) {
                    page = seekBar.progress
                    scroll = 0
                    updateTextView()
                    updateScrollInfo(page)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                dragging = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                dragging = false
            }
        })
        leftPage?.setOnClickListener {
            val progress = seekPage?.progress ?: 0
            if (progress > 0) {
                page = progress - 1
                updateTextView()
                updateScrollInfo(progress - 1)
            }
        }
        rightPage?.setOnClickListener {
            val progress = seekPage?.progress ?: 0
            val max = seekPage?.max ?: 0
            if (progress < max) {
                page = progress + 1
                updateTextView()
                updateScrollInfo(progress + 1)
            }
        }
    }

    // 현재 스크롤 정보를 표시하고 seek를 업데이트함
    // position은 0 베이스이다.
    fun updateScrollInfo(position: Int) {
        //final int count = lineList.size() / LINES_PER_PAGE;

        // 0-999까지가 1개의 페이지이다.
        val count: Int = (lineList.size - 1) / LINES_PER_PAGE
        if (USE_10K_PERCENT) {
            // 현재페이지(1부터시작)/전체페이지 (현재페이지 퍼센트%)
            val text = (position + 1).toString() + "/" + (count + 1) + String.format(
                " (%02d%%)",
                percent2 / 100
            )
            textPage?.text = text
        } else {
            val text = (position + 1).toString() + "/" + (count + 1) + String.format(
                " (%02d%%)",
                percent / 10
            )
            textPage?.text = text
        }
        seekPage?.max = count

        // 이미지가 1개일 경우 처리
        if (position == 0 && count == 0) {
            seekPage?.progress = count
            seekPage?.isEnabled = false
        } else {
            seekPage?.progress = position
            seekPage?.isEnabled = true
        }
    }

    internal class LoadTextTask(activity: TextActivity) : AsyncTask<String?, Int?, Void?>() {
        var activityWeakReference: WeakReference<TextActivity> = WeakReference(activity)
        private fun checkEncoding(fileName: String): String? {
            val buf = ByteArray(BLOCK_SIZE)
            val fileInputStream: FileInputStream
            try {
                fileInputStream = FileInputStream(fileName)

                // (1)
                val detector = UniversalDetector(null)

                // (2)
                val nRead: Int
                nRead = fileInputStream.read(buf)
                //TODO: FIX: 빠른속도를 위해서 8192b만 읽어서 처리
                //while ((nRead = fileInputStream.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nRead)
                //}
                // (3)
                detector.dataEnd()

                // (4)
                val encoding = detector.detectedCharset

                // (5)
                detector.reset()
                fileInputStream.close()
                return encoding
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        protected override fun doInBackground(vararg params: String?): Void? {
            val path = params[0]
            path ?: return null
            // 인코딩을 얻는다.
            val encoding = checkEncoding(path)
            //TODO: 에러일 경우 EUC-KR로 간주. 이것도 국가별로 변경해야함
            //final String encoding = "euc-kr";
            val file = File(path)
            try {
                val fis = FileInputStream(file)

                //FIX:
                // encoding이 null일수 있음
                val reader: InputStreamReader = if (encoding != null) {
                    InputStreamReader(fis, encoding)
                } else {
                    InputStreamReader(fis, "euc-kr")
                }
                val bufferedReader = BufferedReader(reader)

                // 파일 전체의 라인을 얻는다.
                var line: String
                while (bufferedReader.readLine().also { line = it } != null) {
                    val activity = activityWeakReference.get()
                    if (activity != null) {
                        activity.lineList.add(line)

                        // 라인 갯수가 1000개씩 딱딱 맞다면, 해당페이지의 정보를 textview에 업데이트 한다.
                        if (activity.lineList.size == (activity.page + 1) * LINES_PER_PAGE) {
                            publishProgress(activity.page)
                        }
                    }
                }
                val activity = activityWeakReference.get()
                if (activity != null) {
                    // 남은 자료가 마지막것보다 작지만 나머지를 보내야 할때는 publish 한다.
                    val size = activity.lineList.size
                    if (size > activity.page * LINES_PER_PAGE && size < (activity.page + 1) * LINES_PER_PAGE) {
                        publishProgress(activity.page)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        protected override fun onProgressUpdate(vararg values: Int?) {
            val activity = activityWeakReference.get()
            activity?.updateTextView()
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            val activity = activityWeakReference.get()
            if (activity != null) {
                activity.textInfo?.text = "" + activity.lineList.size + " lines"
                activity.scrollText?.viewTreeObserver?.addOnGlobalLayoutListener(activity.onGlobalLayoutListener)
                activity.updateScrollInfo(activity.page)
                activity.progressBar?.visibility = View.GONE
            }
        }

    }

    // 본문 텍스트를 업데이트함
    fun updateTextView() {
        val builder = StringBuilder()
        val size = lineList.size
        if (size < page * LINES_PER_PAGE) return

        // 전체 텍스트에서 페이지에 해당하는 라인을 1000개(LINES_PER_PAGE)만큼 추가해 준다.
        val max = Math.min(size, (page + 1) * LINES_PER_PAGE)
        for (i in page * LINES_PER_PAGE until max) {
            builder.append(lineList[i])
            builder.append("\n")
        }
        val text = builder.toString()
        textContent?.text = text
    }

    // 저장되었던 스크롤 위치를 계산함
    val scrollY: Int
        get() {
            scrollText?.let {
                val maxScroll = it.getChildAt(0).height - it.height
                val scrollY: Int = if (useScrollV2) {
                    maxScroll * scroll / (LINES_PER_PAGE * 10)
                } else {
                    maxScroll * scroll / LINES_PER_PAGE
                }
                return scrollY
            }
            return 0
        }

    companion object {
        private const val USE_10K_PERCENT = true
        private const val MAX_FONT_SIZE_INDEX = 16
    }
}