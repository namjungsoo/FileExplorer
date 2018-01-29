package com.duongame.helper;

import android.content.Context;

import com.duongame.adapter.ExplorerItem;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by namjungsoo on 2016-11-19.
 */

public class FileHelper {
    private static final DecimalFormat formatter = new DecimalFormat("#,###.##");
    private static final long MEGA = 1024 * 1024;
    private static final long KILO = 1024;
    private static final long GIGA = 1024 * 1024 * 1024;
    public static final int BLOCK_SIZE = 8 * (int) KILO;

    public static class Progress {
        public int percent;
        public int index;
        public String fileName;
        public int count;
    }

    public static String getNameWithoutTar(String name) {
        if (name.endsWith(".tar")) {
            return name.substring(0, name.length() - 4);
            //return tar.replace(".tar", "");
        } else {
            return name;
        }
    }

    public static String getCommaSize(long size) {
        if (size < 0)
            return "";
        return formatter.format(size);
    }

    public static String getFileName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    // Zip 압축을 풀때 기존에 폴더가 있으면 새로운 폴더명으로 풀어준다.
    // 폴더를 생성할때는 새로운 폴더명이 있으면 있다고 확인을 한다.
    public static String getNewFileName(String path) {
        File file = new File(path);
        if (!file.exists())
            return path;

        String base, ext = null;
        if (file.isDirectory()) {
            base = path;
        } else {
            base = path.substring(0, path.lastIndexOf("."));
            ext = path.substring(path.lastIndexOf("."));
        }

        int index = 1;

        while (true) {
            String candidate = makeCandidateFileName(base, ext, index);
            File newFile = new File(candidate);
            if (!newFile.exists())
                return candidate;
            index++;
        }
    }

    private static String makeCandidateFileName(String base, String ext, int index) {
        StringBuilder builder = new StringBuilder();
        builder.append(base);
        builder.append(" (")
                .append(index)
                .append(")");
        if (ext != null) {
            builder.append(ext);
        }
        return builder.toString();
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

    public static int getCompressType(String path) {
        if (path.endsWith(".zip") || path.endsWith(".cbz"))
            return ExplorerItem.COMPRESSTYPE_ZIP;
        //TODO: 현재 지원 안함
        if (path.endsWith(".rar") || path.endsWith(".cbr"))
            return ExplorerItem.COMPRESSTYPE_RAR;
        if (path.endsWith(".7z") || path.endsWith(".cb7"))
            return ExplorerItem.COMPRESSTYPE_SEVENZIP;
        if (path.endsWith(".tar") || path.endsWith(".cbt"))
            return ExplorerItem.COMPRESSTYPE_TAR;
        if (path.endsWith(".gz") || path.endsWith(".tgz"))
            return ExplorerItem.COMPRESSTYPE_GZIP;
        //TODO: 테스트 더해보고 안되면 막아야 함
//        if (path.endsWith(".bz2") || path.endsWith(".tbz2"))
//            return ExplorerItem.CompressType.BZIP2;
        return ExplorerItem.COMPRESSTYPE_OTHER;
    }

    //region Extension
    public static int getFileType(File eachFile) {
        int type = eachFile.isDirectory() ? ExplorerItem.FILETYPE_FOLDER : ExplorerItem.FILETYPE_FILE;
        if (FileHelper.isImage(eachFile.getName())) {
            type = ExplorerItem.FILETYPE_IMAGE;
        } else if (getCompressType(eachFile.getName()) != ExplorerItem.COMPRESSTYPE_OTHER)
            type = ExplorerItem.FILETYPE_ZIP;
        else if (eachFile.getName().endsWith(".pdf"))
            type = ExplorerItem.FILETYPE_PDF;
        else if (eachFile.getName().endsWith(".mp4") || eachFile.getName().endsWith(".avi") || eachFile.getName().endsWith(".3gp") || eachFile.getName().endsWith(".mkv") || eachFile.getName().endsWith(".mov"))
            type = ExplorerItem.FILETYPE_VIDEO;
        else if (eachFile.getName().endsWith(".mp3"))
            type = ExplorerItem.FILETYPE_AUDIO;
        else if (isText(eachFile.getName()))
            type = ExplorerItem.FILETYPE_TEXT;
        else if (eachFile.getName().endsWith(".apk"))
            type = ExplorerItem.FILETYPE_APK;

        return type;
    }

    public static boolean isText(String filename) {
        if (filename.endsWith(".txt") || filename.endsWith(".log"))
            return true;
        return false;
    }

    public static boolean isImage(String filename) {
        if (filename.endsWith(".jpg")
                || filename.endsWith(".jpeg")
                || filename.endsWith(".gif")
                || filename.endsWith(".png")
                ) {
            return true;
        }
        return false;
    }

    public static boolean isGifImage(String filename) {
        if (filename.endsWith(".gif")) {
            return true;
        }
        return false;
    }

    public static boolean isPngImage(String filename) {
        if (filename.endsWith(".png")) {
            return true;
        }
        return false;
    }

    public static boolean isJpegImage(String filename) {
        if (filename.endsWith(".jpg")
                || filename.endsWith(".jpeg")
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

    public static class DateAscComparator implements Comparator<ExplorerItem> {
        @Override
        public int compare(ExplorerItem lhs, ExplorerItem rhs) {
            Date lhsDate = DateHelper.getDateFromExplorerDate(lhs.date);
            Date rhsDate = DateHelper.getDateFromExplorerDate(rhs.date);
            return lhsDate.compareTo(rhsDate);
        }
    }

    public static class DateDescComparator implements Comparator<ExplorerItem> {
        @Override
        public int compare(ExplorerItem lhs, ExplorerItem rhs) {
            Date lhsDate = DateHelper.getDateFromExplorerDate(lhs.date);
            Date rhsDate = DateHelper.getDateFromExplorerDate(rhs.date);
            return rhsDate.compareTo(lhsDate);
        }
    }

    public static class ExtAscComparator implements Comparator<ExplorerItem> {
        @Override
        public int compare(ExplorerItem lhs, ExplorerItem rhs) {
            String lhsExt = lhs.getExt();
            String rhsExt = rhs.getExt();

            int ret = lhsExt.compareToIgnoreCase(rhsExt);
            if (ret == 0) {
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
            if (ret == 0) {
                return rhs.name.compareToIgnoreCase(lhs.name);
            } else {
                return ret;
            }
        }
    }

    public static class SizeAscComparator implements Comparator<ExplorerItem> {
        @Override
        public int compare(ExplorerItem lhs, ExplorerItem rhs) {
            long ret = lhs.size - rhs.size;
            if (ret == 0) {
                return lhs.name.compareToIgnoreCase(rhs.name);
            } else {
                if (ret < 0)
                    return -1;
                else
                    return 1;
            }
        }
    }

    public static class SizeDescComparator implements Comparator<ExplorerItem> {
        @Override
        public int compare(ExplorerItem lhs, ExplorerItem rhs) {
            long ret = rhs.size - lhs.size;
            if (ret == 0) {
                return rhs.name.compareToIgnoreCase(lhs.name);
            } else {
                if (ret < 0)
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
