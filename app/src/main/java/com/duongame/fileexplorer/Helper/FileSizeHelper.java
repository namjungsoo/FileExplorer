package com.duongame.fileexplorer.Helper;

import java.text.DecimalFormat;

/**
 * Created by namjungsoo on 2016-11-18.
 */

public class FileSizeHelper {
    private static final DecimalFormat formatter = new DecimalFormat("#,###.##");
    private static final long MEGA = 1024 * 1024;
    private static final long KILO = 1024;
    private static final long GIGA = 1024 * 1024 * 1024;

    public static String getCommaSize(long size) {
        if (size < 0)
            return "";
        return formatter.format(size);
    }

    public static String getMinimizedSize(long size) {
        if (size < 0)
            return "";

        if (size > GIGA) {
            double newsize = (double) size / GIGA;
            return formatter.format(newsize) + "GB";
        } else if (size > MEGA) {
            double newsize = (double) size / MEGA;
            return formatter.format(newsize) + "MB";

        } else if (size > KILO) {
            double newsize = (double) size / KILO;
            return formatter.format(newsize) + "KB";
        } else {
            return formatter.format(size);
        }
    }
}
