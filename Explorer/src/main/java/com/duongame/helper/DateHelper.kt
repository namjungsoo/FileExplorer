package com.duongame.helper

import org.apache.commons.lang3.time.FastDateFormat
import java.text.ParseException
import java.util.*

/**
 * Created by namjungsoo on 2017. 1. 7..
 */
object DateHelper {
    //FIX:
    // Thread-safe 문제를 해결함
    // YodaTime을 사용하면 해결됨
    // FastDateFormat으로 변경
    private const val dbPattern = "yyyy-MM-dd HH:mm:ss"
    private const val explorerPattern = "yy-MM-dd(E) hh:mm:ss a"
    private const val simplePattern = "yy-MM-dd(E)"
    private fun getExplorerDateString(dateLong: Long): String {
        return FastDateFormat.getInstance(explorerPattern).format(dateLong)
    }

    @JvmStatic
    fun getExplorerDateString(date: Date?): String {
        return FastDateFormat.getInstance(explorerPattern).format(date)
    }

    private fun getSimpleDateString(dateLong: Long): String {
        return FastDateFormat.getInstance(simplePattern).format(dateLong)
    }

    private fun getLongFromDbDateString(dbDate: String?): Long {
        return try {
            FastDateFormat.getInstance(dbPattern).parse(dbDate).time
        } catch (e: ParseException) {
            e.printStackTrace()
            0L
        }
    }

    private fun getLongFromExplorerDateString(explorerDate: String?): Long {
        return try {
            FastDateFormat.getInstance(explorerPattern).parse(explorerDate).time
        } catch (e: ParseException) {
            e.printStackTrace()
            0L
        }
    }

    @JvmStatic
    fun getExplorerDateStringFromDbDateString(dbDate: String?): String {
        return getExplorerDateString(getLongFromDbDateString(dbDate))
    }

    @JvmStatic
    fun getDateFromExplorerDateString(explorerDate: String?): Date {
        return Date(getLongFromExplorerDateString(explorerDate))
    }

    @JvmStatic
    fun getSimpleDateStringFromExplorerDateString(explorerDate: String?): String {
        return getSimpleDateString(getLongFromExplorerDateString(explorerDate))
    }
}