package com.duongame.comicz.db;

import android.os.Build;

import com.duongame.explorer.adapter.ExplorerItem;

/**
 * Created by namjungsoo on 2017-11-05.
 */

public class TextBook {
    public static int LINES_PER_PAGE = 1000;

    public static String getPageText(Book book) {
        int lineCount;// 전체 라인 갯수. 1000라인별로 페이지가 분할되어 슬라이더 이동됨
        int currentPage;// 1000으로 나눈 몫
        int currentPercent;// 현재 페이지의 퍼센트
        int currentLine;// 전체중에 몇번째 라인인지
        int totalPercent;

        lineCount = book.total_page;

        // 현재 페이지 번호
        currentPage = book.current_page / LINES_PER_PAGE;

        // 현재 페이지 번호를 곱하면 현재 퍼센트가 나옴. 1/1000단위
        currentPercent = book.current_page - currentPage * LINES_PER_PAGE;

        // 마지막 페이지이면, 1000라인 이하일수 있다. 전체가 1000라인 이하인 것도 마찬가지다.
        if (currentPage == (lineCount / LINES_PER_PAGE)) {// 읽기 완료된 파일이다.
            currentLine = lineCount;
            totalPercent = LINES_PER_PAGE;
        } else {
            int currentPageLine = 0;
            int prevTotalPageLineCount = 0;
            int currentTotalPageLineCount = 0;
            prevTotalPageLineCount = currentPage * LINES_PER_PAGE;

            currentPageLine = currentTotalPageLineCount * currentPercent / LINES_PER_PAGE;
            if (currentPage == ((lineCount / LINES_PER_PAGE) - 1)) {// 마지막 페이지이다.
                // 남은 라인을 먼저 계산
                currentTotalPageLineCount = lineCount - prevTotalPageLineCount;
            } else {// 가운데 페이지이다.
                currentTotalPageLineCount = LINES_PER_PAGE;
            }

            currentLine = prevTotalPageLineCount + currentPageLine;
            totalPercent = currentLine * LINES_PER_PAGE / lineCount;
        }

        return "" + (lineCount * totalPercent / LINES_PER_PAGE) + "/" + lineCount;
    }

    public static String getPercentText() {
        return "";
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            book.total_page = lineCount;
        }

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            book.total_page = lineCount;
        }

        // zip아 아니면 사용하지 않는 부분
        book.current_file = percent;
        book.extract_file = 0;
        book.side = ExplorerItem.Side.SIDE_ALL;

        return book;
    }

}
