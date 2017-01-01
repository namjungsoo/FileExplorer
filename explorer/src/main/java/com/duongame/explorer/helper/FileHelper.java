package com.duongame.explorer.helper;

import android.content.Context;

import com.duongame.explorer.adapter.ExplorerItem;

import java.io.File;
import java.util.Comparator;

/**
 * Created by namjungsoo on 2016-11-19.
 */

public class FileHelper {
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

    public static class FileNameAscendingCompare implements Comparator<ExplorerItem> {
        @Override
        public int compare(ExplorerItem lhs, ExplorerItem rhs) {
            return lhs.name.compareToIgnoreCase(rhs.name);
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
