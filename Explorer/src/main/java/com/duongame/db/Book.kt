package com.duongame.db

import com.duongame.file.FileHelper.isText
import com.duongame.adapter.ExplorerItem
import com.duongame.file.FileHelper
import com.duongame.db.TextBook

/**
 * Created by namjungsoo on 2017-11-05.
 */
class Book {
    // 변하지 않음
    lateinit var path: String// 패스
    var name: String? = null// 파일명
    var type = ExplorerItem.FILETYPE_ZIP
    var size: Long = 0// zip파일 사이즈
    var total_file = 0// 최대 파일 수

    // 동적으로 변함
    var current_page = 0// 현재 페이지 인덱스. 텍스트의 경우 현재 페이지*1000 + 0~99.9%
    var total_page = 0// 최대 페이지 수. 전체를 다 로딩하지 않으면 알수 없음. 텍스트의 경우 전체 라인수
    var current_file = 0// 현재 파일 인덱스
    var extract_file = 0// 압축 풀린 파일수. zip에만 해당함. total_file == extract_file이면 로딩이 완료된것
    var side = ExplorerItem.SIDE_ALL // 책넘김 방법
    var date: String? = null
    var last_file: String? = null// 마지막 이미지 파일의 파일명

    // DB에 저장되지 않음
    var percent = 0// 0~100을 가진다.

    override fun toString(): String {
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
                " currentPercent=" + percent
    }

    fun updatePercent() {
        if (isText(name!!)) {

            // 전체 페이지 갯수를 계산
            // 저장된 total_page는 lineCount이다.
            var textTotalPages = total_page / TextBook.LINES_PER_PAGE
            if (total_page % TextBook.LINES_PER_PAGE > 0) {
                textTotalPages++
            }

            // 현재 페이지의 번호를 계산
            val textCurrentPage = current_page / TextBook.LINES_PER_PAGE

            // 페이지당 퍼센트를 계산
            val perPagePercent = 100.0f / textTotalPages

            // 현재 페이지의 시작점 퍼센트를 계산
            val beginPagePercent = perPagePercent * textCurrentPage

            // 남은 페이지 라인에 해당하는 퍼센트를 계산해서 더함
            // 마지막 페이지와 중간 페이지의 차이가 있음
            val proceedingPagePercent: Float
            proceedingPagePercent = if (current_file == 0) {
                val current = current_page % TextBook.LINES_PER_PAGE
                if (current == 999) {
                    //percent = 100;
                    perPagePercent
                } else {
                    //percent = current / 10;
                    perPagePercent * (current / 1000.0f)
                }
            } else {
                val current = current_file % 10000
                if (current == 9999) {
                    //percent = 100;
                    perPagePercent
                } else {
                    //percent = current / 100;
                    perPagePercent * (current / 10000.0f)
                }
            }
            percent = (beginPagePercent + proceedingPagePercent).toInt()
        } else {
            if (total_page > 0) {
                percent = (current_page + 1) * 100 / total_page
            } else if (total_file > 0) {
                percent = (current_file + 1) * 100 / total_file
            }
        }
    }
}