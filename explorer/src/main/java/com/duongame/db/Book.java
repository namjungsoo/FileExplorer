package com.duongame.db;

import com.duongame.adapter.ExplorerItem;

import static com.duongame.db.TextBook.LINES_PER_PAGE;

/**
 * Created by namjungsoo on 2017-11-05.
 */

public class Book {
    private static final String TAG = Book.class.getSimpleName();

    // 변하지 않음
    public String path;// 패스
    public String name;// 파일명
    public ExplorerItem.FileType type = ExplorerItem.FileType.ZIP;
    public long size;// zip파일 사이즈
    public int total_file;// 최대 파일 수

    // 동적으로 변함
    public int current_page;// 현재 페이지 인덱스. 텍스트의 경우 현재 페이지*1000 + 0~99.9%
    public int total_page;// 최대 페이지 수. 전체를 다 로딩하지 않으면 알수 없음. 텍스트의 경우 전체 라인수

    public int current_file;// 현재 파일 인덱스
    public int extract_file;// 압축 풀린 파일수. zip에만 해당함. total_file == extract_file이면 로딩이 완료된것

    public ExplorerItem.Side side = ExplorerItem.Side.SIDE_ALL;// 책넘김 방법
    public String date;

    public String last_file;// 마지막 이미지 파일의 파일명

    // DB에 저장되지 않음
    public int percent;// 0~100을 가진다.

    public Book() {

    }

    @Override
    public String toString() {
        return "path=" + path +
                " name=" + name +
                " type=" + type +
                " size=" + size +
                " total_file=" + total_file +

                " current_page=" + current_page +
                " total_page=" + total_page +
                " current_file=" + current_file +
                " extract_file=" + extract_file +
                " side=" + side +
                " date=" + date +
                " last_file=" + last_file +

                " currentPercent=" + percent;
    }

    public void updatePercent() {
        if (name.toLowerCase().endsWith(".txt")) {

            // 전체 페이지 갯수를 계산
            // 저장된 total_page는 lineCount이다.
            int textTotalPages = total_page / LINES_PER_PAGE;
            if (total_page % LINES_PER_PAGE > 0) {
                textTotalPages++;
            }

            // 현재 페이지의 번호를 계산
            int textCurrentPage = current_page / LINES_PER_PAGE;

            // 페이지당 퍼센트를 계산
            float perPagePercent = 100.0f / textTotalPages;

            // 현재 페이지의 시작점 퍼센트를 계산
            float beginPagePercent = perPagePercent * textCurrentPage;

            // 남은 페이지 라인에 해당하는 퍼센트를 계산해서 더함
            // 마지막 페이지와 중간 페이지의 차이가 있음
            float proceedingPagePercent;

            if (current_file == 0) {
                int current = current_page % LINES_PER_PAGE;
                if (current == 999) {
                    //percent = 100;
                    proceedingPagePercent = perPagePercent;
                } else {
                    //percent = current / 10;
                    proceedingPagePercent = perPagePercent * (current / 1000.0f);
                }

            } else {
                int current = current_file % 10000;
                if (current == 9999) {
                    //percent = 100;
                    proceedingPagePercent = perPagePercent;
                } else {
                    //percent = current / 100;
                    proceedingPagePercent = perPagePercent * (current / 10000.0f);
                }
            }
            percent = (int) (beginPagePercent + proceedingPagePercent);
        } else {
            if (total_page > 0) {
                percent = ((current_page + 1) * 100) / total_page;
            } else if (total_file > 0) {
                percent = ((current_file + 1) * 100) / total_file;
            }
        }
    }
}
