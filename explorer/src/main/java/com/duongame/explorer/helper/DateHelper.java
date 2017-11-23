package com.duongame.explorer.helper;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * Created by namjungsoo on 2017. 1. 7..
 */

public class DateHelper {
    //FIX:
    // Thread-safe 문제를 해결함
    private static final DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yy-MM-dd(E) hh:mm:ss a");
    private static final DateTimeFormatter dbFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public static String getExplorerDate(long dateLong) {
        return dateFormat.print(dateLong);
    }

    public static String getExplorerDate(Date date) {
        return dateFormat.print(date.getTime());
    }

    public static String getDBDate(String dbDate) {
        final long dateLong = dbFormat.parseDateTime(dbDate).getMillis();
        return getExplorerDate(dateLong);
    }
}
