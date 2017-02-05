package com.duongame.explorer.helper;

import android.content.Context;

import com.duongame.explorer.adapter.ExplorerItem;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Comparator;

/**
 * Created by namjungsoo on 2016-11-19.
 */

public class FileHelper {
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
            return formatter.format(newsize) + " GB";
        } else if (size > MEGA) {
            double newsize = (double) size / MEGA;
            return formatter.format(newsize) + " MB";

        } else if (size > KILO) {
            double newsize = (double) size / KILO;
            return formatter.format(newsize) + " KB";
        } else {
            return formatter.format(size);
        }
    }

    public static boolean isImage(String filename) {
        final String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".gif")
                || lower.endsWith(".png")
                ) {
            return true;
        }
        return false;
    }

    public static boolean isJpegImage(String filename) {
        final String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                ) {
            return true;
        }
        return false;
    }

    public static File getZipCacheFile(Context context, String filename) {
        final String filesDir = context.getFilesDir().getAbsolutePath();
        final String cachePath = filesDir + "/" + filename;
        return new File(cachePath);
    }

    public static String getZipCachePath(Context context, String filename) {
        final String filesDir = context.getFilesDir().getAbsolutePath();
        final String cachePath = filesDir + "/" + filename;
        return cachePath;
    }

    public static class FileNameAscendingComparator implements Comparator<ExplorerItem> {
        @Override
        public int compare(ExplorerItem lhs, ExplorerItem rhs) {
            return lhs.name.compareToIgnoreCase(rhs.name);
        }
    }

    public static class FilePriorityComparator implements Comparator<ExplorerItem> {
        @Override
        public int compare(ExplorerItem lhs, ExplorerItem rhs) {
            if(lhs.priority < rhs.priority)
                return -1;
            else if(lhs.priority > rhs.priority)
                return 1;
            return 0;
        }
    }

    public static String setPdfFileNameFromPage(String pdf, int page) {
        final String ret = pdf + "." + String.valueOf(page);

        return ret;
    }

    public static int getPdfPageFromFileName(String pdfWithPage) {
        String page = pdfWithPage.substring(pdfWithPage.lastIndexOf(".") + 1);
        return Integer.parseInt(page);
    }

    public static String getFullPath(String path, String name) {
        return path + "/" + name;
    }
}
