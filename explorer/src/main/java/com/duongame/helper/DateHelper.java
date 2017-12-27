package com.duongame.helper;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * Created by namjungsoo on 2017. 1. 7..
 */

public class DateHelper {
    //FIX:
    // Thread-safe 문제를 해결함
    private static final DateTimeFormatter explorerFormat = DateTimeFormat.forPattern("yy-MM-dd(E) hh:mm:ss a");
    private static final DateTimeFormatter dbFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public static String getExplorerDateString(long dateLong) {
        return explorerFormat.print(dateLong);
    }

    public static String getExplorerDateString(Date date) {
        return explorerFormat.print(date.getTime());
    }

    public static String getExplorerDateString(String dbDate) {
        final long dateLong = dbFormat.parseDateTime(dbDate).getMillis();
        return getExplorerDateString(dateLong);
    }

    public static Date getDateFromExplorerDate(String explorerDate) {
        final long dateLong = explorerFormat.parseDateTime(explorerDate).getMillis();
        Date date = new Date(dateLong);
        return date;
    }
}
