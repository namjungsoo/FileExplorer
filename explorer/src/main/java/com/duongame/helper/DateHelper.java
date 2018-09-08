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

    public static String getExplorerDateString(long dateLong) {
        return FastDateFormat.getInstance(explorerPattern).format(dateLong);
    }

    public static String getExplorerDateString(Date date) {
        return FastDateFormat.getInstance(explorerPattern).format(date);
    }

    public static String getExplorerDateStringFromDbDateString(String dbDate) {
        try {
            final long dateLong = FastDateFormat.getInstance(dbPattern).parse(dbDate).getTime();
            return getExplorerDateString(dateLong);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static Date getDateFromExplorerDateString(String explorerDate) {
        try {
            final long dateLong = FastDateFormat.getInstance(explorerPattern).parse(explorerDate).getTime();
            Date date = new Date(dateLong);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
    }
}
