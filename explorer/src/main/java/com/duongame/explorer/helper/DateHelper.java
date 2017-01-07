package com.duongame.explorer.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by namjungsoo on 2017. 1. 7..
 */

public class DateHelper {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd(E) hh:mm:ss a");
    private static final SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String getExplorerDate(Date date) {
        return dateFormat.format(date);
    }

    public static String getDBDate(String dbDate)  {
        final Date date;
        try {
            date = dbFormat.parse(dbDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return getExplorerDate(date);
    }
}
