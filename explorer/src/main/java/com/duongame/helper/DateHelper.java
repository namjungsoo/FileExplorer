package com.duongame.helper;

import org.apache.commons.lang3.time.FastDateFormat;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by namjungsoo on 2017. 1. 7..
 */

public class DateHelper {
    //FIX:
    // Thread-safe 문제를 해결함
    // YodaTime을 사용하면 해결됨
    // FastDateFormat으로 변경
    private static final String dbPattern = "yyyy-MM-dd HH:mm:ss";
    private static final String explorerPattern = "yy-MM-dd(E) hh:mm:ss a";
    private static final String simplePattern = "yy-MM-dd(E)";

    public static String getExplorerDateString(long dateLong) {
        return FastDateFormat.getInstance(explorerPattern).format(dateLong);
    }

    public static String getExplorerDateString(Date date) {
        return FastDateFormat.getInstance(explorerPattern).format(date);
    }

    public static String getSimpleDateString(long dateLong) {
        return FastDateFormat.getInstance(simplePattern).format(dateLong);
    }

    public static long getLongFromDbDateString(String dbDate) {
        try {
            return FastDateFormat.getInstance(dbPattern).parse(dbDate).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0L;
        }
    }

    public static long getLongFromExplorerDateString(String explorerDate) {
        try {
            return FastDateFormat.getInstance(explorerPattern).parse(explorerDate).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0L;
        }
    }

    public static String getExplorerDateStringFromDbDateString(String dbDate) {
        return getExplorerDateString(getLongFromDbDateString(dbDate));
    }

    public static Date getDateFromExplorerDateString(String explorerDate) {
        return new Date(getLongFromExplorerDateString(explorerDate));
    }

    public static String getSimpleDateStringFromExplorerDateString(String explorerDate) {
        return getSimpleDateString(getLongFromExplorerDateString(explorerDate));
    }
}
