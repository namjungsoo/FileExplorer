package com.duongame.db;

import com.duongame.adapter.ExplorerItem;

/**
 * Created by namjungsoo on 2017-11-05.
 */

public class TextBook {
    public static int LINES_PER_PAGE = 1000;

    // 현재페이지 까지의 누적 라인 갯수/전체 라인의 갯수
    public static String getPageText(Book book) {
        int totalLineCount = book.total_page;
        return "" + (totalLineCount * book.percent / 100) + "/" + totalLineCount;
    }

    // setLastBook에 저장될 Book을 만듬
    public static Book buildTextBook(String path, String name, long size, int percent, int page, int lineCount) {
        // 현재 라인을 book에 저장하자.
        final Book book = new Book();

        // 고정적인 내용 5개
        book.path = path;
        book.name = name;
        book.type = ExplorerItem.FileType.TEXT;
        book.size = size;
        book.total_file = 0;// 파일의 갯수이다.

        // 동적인 내용 6개
        book.current_page = page * LINES_PER_PAGE + percent;
        book.total_page = lineCount;

        // zip아 아니면 사용하지 않는 부분
        book.current_file = 0;
        book.extract_file = 0;
        book.side = ExplorerItem.Side.SIDE_ALL;

        return book;
    }

    public static Book buildTextBook2(String path, String name, long size, int percent, int page, int lineCount) {
        // 현재 라인을 book에 저장하자.
        final Book book = new Book();

        // 고정적인 내용 5개
        book.path = path;
        book.name = name;
        book.type = ExplorerItem.FileType.TEXT;
        book.size = size;
        book.total_file = 0;// 파일의 갯수이다.

        // 동적인 내용 6개
        // 하위호환성 확보를 위해서 1000단위로 넣음
        book.current_page = page * LINES_PER_PAGE + percent / 10;
        book.total_page = lineCount;

        // zip아 아니면 사용하지 않는 부분
        book.current_file = percent;
        book.extract_file = 0;
        book.side = ExplorerItem.Side.SIDE_ALL;

        return book;
    }

}
