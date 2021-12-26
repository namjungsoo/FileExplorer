package com.duongame.db

import com.duongame.adapter.ExplorerItem
import com.duongame.db.TextBook

/**
 * Created by namjungsoo on 2017-11-05.
 */
object TextBook {
    @JvmField
    var LINES_PER_PAGE = 1000

    // 현재페이지 까지의 누적 라인 갯수/전체 라인의 갯수
    @JvmStatic
    fun getPageText(book: Book): String {
        val totalLineCount = book.total_page
        return "" + totalLineCount * book.percent / 100 + "/" + totalLineCount
    }

    // setLastBook에 저장될 Book을 만듬
    @JvmStatic
    fun buildTextBook(
        path: String,
        name: String,
        size: Long,
        percent: Int,
        page: Int,
        lineCount: Int
    ): Book {
        // 현재 라인을 book에 저장하자.
        val book = Book()

        // 고정적인 내용 5개
        book.path = path
        book.name = name
        book.type = ExplorerItem.FILETYPE_TEXT
        book.size = size
        book.total_file = 0 // 파일의 갯수이다.

        // 동적인 내용 6개
        book.current_page = page * LINES_PER_PAGE + percent
        book.total_page = lineCount

        // zip아 아니면 사용하지 않는 부분
        book.current_file = 0
        book.extract_file = 0
        book.side = ExplorerItem.SIDE_ALL
        return book
    }

    @JvmStatic
    fun buildTextBook2(
        path: String,
        name: String,
        size: Long,
        percent: Int,
        page: Int,
        lineCount: Int
    ): Book {
        // 현재 라인을 book에 저장하자.
        val book = Book()

        // 고정적인 내용 5개
        book.path = path
        book.name = name
        book.type = ExplorerItem.FILETYPE_TEXT
        book.size = size
        book.total_file = 0 // 파일의 갯수이다.

        // 동적인 내용 6개
        // 하위호환성 확보를 위해서 1000단위로 넣음
        book.current_page = page * LINES_PER_PAGE + percent / 10
        book.total_page = lineCount

        // zip아 아니면 사용하지 않는 부분
        book.current_file = percent
        book.extract_file = 0
        book.side = ExplorerItem.SIDE_ALL
        return book
    }
}