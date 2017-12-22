package com.duongame.helper;

import android.content.Context;

import com.duongame.adapter.ExplorerItem;

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
            return formatter.format(size) + " B";
        }
    }

    //region Extension
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

    public static boolean isGifImage(String filename) {
        final String lower = filename.toLowerCase();
        if (lower.endsWith(".gif")) {
            return true;
        }
        return false;
    }

    public static boolean isPngImage(String filename) {
        final String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) {
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
    //endregion

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

    //region Comparator
    // 파일 이름은 이름만 정렬하면 되는데
    // 크기나 확장자는 정렬후에 이름으로 한번더 정렬 하여야 한다
    public static class NameAscComparator implements Comparator<ExplorerItem> {
        @Override
        public int compare(ExplorerItem lhs, ExplorerItem rhs) {
            return lhs.name.compareToIgnoreCase(rhs.name);
        }
    }

    public static class NameDescComparator implements Comparator<ExplorerItem> {
        @Override
        public int compare(ExplorerItem lhs, ExplorerItem rhs) {
            return rhs.name.compareToIgnoreCase(lhs.name);
        }
    }

    public static class ExtAscComparator implements Comparator<ExplorerItem> {
        @Override
        public int compare(ExplorerItem lhs, ExplorerItem rhs) {
            String lhsExt = lhs.getExt();
            String rhsExt = rhs.getExt();

            int ret = lhsExt.compareToIgnoreCase(rhsExt);
            if(ret == 0) {
                return lhs.name.compareToIgnoreCase(rhs.name);
            } else {
                return ret;
            }
        }
    }

    public static class ExtDescComparator implements Comparator<ExplorerItem> {
        @Override
        public int compare(ExplorerItem lhs, ExplorerItem rhs) {
            String lhsExt = lhs.getExt();
            String rhsExt = rhs.getExt();

            int ret = rhsExt.compareToIgnoreCase(lhsExt);
            if(ret == 0) {
                return rhs.name.compareToIgnoreCase(lhs.name);
            } else {
                return ret;
            }
        }
    }

    public static class SizeAscComparator implements Comparator<ExplorerItem> {
        @Override
        public int compare(ExplorerItem lhs, ExplorerItem rhs) {
            long ret = rhs.size - lhs.size;
            if(ret == 0) {
                return lhs.name.compareToIgnoreCase(rhs.name);
            } else {
                if(ret < 0)
                    return -1;
                else
                    return 1;
            }
        }
    }

    public static class SizeDescComparator implements Comparator<ExplorerItem> {
        @Override
        public int compare(ExplorerItem lhs, ExplorerItem rhs) {
            long ret = lhs.size - rhs.size;
            if(ret == 0) {
                return rhs.name.compareToIgnoreCase(lhs.name);
            } else {
                if(ret < 0)
                    return -1;
                else
                    return 1;
            }
        }
    }

//    public static class PriorityAscComparator implements Comparator<ExplorerItem> {
//        @Override
//        public int compare(ExplorerItem lhs, ExplorerItem rhs) {
//            if (lhs.priority < rhs.priority)
//                return -1;
//            else if (lhs.priority > rhs.priority)
//                return 1;
//            return 0;
//        }
//    }
    //endregion

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

    // 마지막에 /를 포함하지 않는다.
    public static String getParentPath(String path) {
        return path.substring(0, path.lastIndexOf('/'));
    }
}
