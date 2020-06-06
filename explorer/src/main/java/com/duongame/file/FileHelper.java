package com.duongame.file;

import android.content.Context;
import android.os.Build;

import com.duongame.R;
import com.duongame.adapter.ExplorerItem;
import com.duongame.helper.DateHelper;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    public static String getFileNameCharset(Context context) {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = context.getResources().getConfiguration().locale;
        }

        String charset = null;
        switch (locale.getLanguage()) {
            case "ko":
                charset = "cp949";
                break;
            case "ja":
                charset = "cp932";
                break;
            case "zh":
                charset = "cp936";
                break;
        }
        return charset;
    }

    public static String getNameWithoutTar(String name) {
        if (name.endsWith(".tar") || name.endsWith(".TAR")) {
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

    public static long getFileSize(String path) {
        try {
            File file = new File(path);
            return file.length();
        } catch (Exception e) {
            return 0;
        }
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
            // 여기서 확장자가 없을수도 있음
            // base, ext 둘다 에러 발생 가능함
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
        if (path.endsWith(".zip") || path.endsWith(".cbz") ||
                path.endsWith(".ZIP") || path.endsWith(".CBZ"))
            return ExplorerItem.COMPRESSTYPE_ZIP;
        if (path.endsWith(".rar") || path.endsWith(".cbr") ||
                path.endsWith(".RAR") || path.endsWith(".CBR"))
            return ExplorerItem.COMPRESSTYPE_RAR;
        if (path.endsWith(".7z") || path.endsWith(".cb7") ||
                path.endsWith(".7Z") || path.endsWith(".CB7"))
            return ExplorerItem.COMPRESSTYPE_SEVENZIP;
        if (path.endsWith(".tar") || path.endsWith(".cbt") ||
                path.endsWith(".TAR") || path.endsWith(".CBT"))
            return ExplorerItem.COMPRESSTYPE_TAR;
        if (path.endsWith(".gz") || path.endsWith(".tgz") ||
                path.endsWith(".GZ") || path.endsWith(".TGZ"))
            return ExplorerItem.COMPRESSTYPE_GZIP;
        //TODO: 테스트 더해보고 안되면 막아야 함
//        if (path.endsWith(".bz2") || path.endsWith(".tbz2"))
//            return ExplorerItem.CompressType.BZIP2;
        return ExplorerItem.COMPRESSTYPE_OTHER;
    }

    //region Extension
    public static int getExtType(File eachFile, int fileType) {
        return 0;
    }

    // 파일 타입과 아이콘 타입은 종류가 다르므로 직접 입력하도록 함
    public static int getFileFolderIconResId(String fileName) {
        int type = getFileFolderType(fileName);
        int resId = R.drawable.ic_file_normal;

        // 이미지일 경우 구분
        switch (type) {
            case ExplorerItem.FILETYPE_FOLDER:
                resId = R.drawable.ic_file_folder;
                break;
            case ExplorerItem.FILETYPE_IMAGE: {
                String lowerFileName = fileName.toLowerCase();
                if (lowerFileName.endsWith(".gif"))
                    resId = R.drawable.ic_file_jpg;// gif가 없음
                else if (lowerFileName.endsWith(".png"))
                    resId = R.drawable.ic_file_png;
                else
                    resId = R.drawable.ic_file_jpg;
            }
            break;
            case ExplorerItem.FILETYPE_ZIP:
                resId = R.drawable.ic_file_zip;
                break;
            case ExplorerItem.FILETYPE_PDF:
                resId = R.drawable.ic_file_pdf;
                break;
            case ExplorerItem.FILETYPE_VIDEO: {
                String lowerFileName = fileName.toLowerCase();
                if (lowerFileName.endsWith(".mp4"))
                    resId = R.drawable.ic_file_mp4;
                else if (lowerFileName.endsWith(".fla"))
                    resId = R.drawable.ic_file_fla;
                else
                    resId = R.drawable.ic_file_avi;
            }
            break;
            case ExplorerItem.FILETYPE_AUDIO:
                resId = R.drawable.ic_file_mp3;
                break;
            case ExplorerItem.FILETYPE_TEXT:
                resId = R.drawable.ic_file_txt;
                break;
            case ExplorerItem.FILETYPE_APK:
                resId = R.drawable.ic_file_apk;
                break;
        }
        return resId;
    }

    // 폴더는 제외한다
    public static int getFileType(String fileName) {
        int type = ExplorerItem.FILETYPE_FILE;

        // 이미지
        if (FileHelper.isImage(fileName))
            type = ExplorerItem.FILETYPE_IMAGE;
        else if (getCompressType(fileName) != ExplorerItem.COMPRESSTYPE_OTHER)
            type = ExplorerItem.FILETYPE_ZIP;
        else if (fileName.endsWith(".pdf") || fileName.endsWith(".PDF"))
            type = ExplorerItem.FILETYPE_PDF;
        else if (fileName.endsWith(".mp4") || fileName.endsWith(".avi") || fileName.endsWith(".3gp") || fileName.endsWith(".mkv") || fileName.endsWith(".mov"))
            type = ExplorerItem.FILETYPE_VIDEO;
        else if (fileName.endsWith(".mp3") || fileName.endsWith(".MP3"))
            type = ExplorerItem.FILETYPE_AUDIO;
        else if (isText(fileName))
            type = ExplorerItem.FILETYPE_TEXT;
        else if (fileName.endsWith(".apk") || fileName.endsWith(".APK"))
            type = ExplorerItem.FILETYPE_APK;

        return type;
    }

    public static int getFileFolderType(String fileName) {
        final File file = new File(fileName);
        return getFileFolderType(file);
    }

    public static int getFileFolderType(File eachFile) {
        int type = eachFile.isDirectory() ? ExplorerItem.FILETYPE_FOLDER : ExplorerItem.FILETYPE_FILE;

        // 폴더면 바로 리턴
        if (type == ExplorerItem.FILETYPE_FOLDER)
            return type;

        return getFileType(eachFile.getName());
    }

    public static boolean isVideo(String filename) {
        return false;
    }

    public static boolean isText(String filename) {
        if (filename.endsWith(".txt") || filename.endsWith(".log") || filename.endsWith(".json") ||
                filename.endsWith(".TXT") || filename.endsWith(".LOG") || filename.endsWith(".JSON"))
            return true;
        return false;
    }

    public static boolean isImage(String filename) {
        if (isJpegImage(filename) || isPngImage(filename) || isGifImage(filename)) {
            return true;
        }
        return false;
    }

    public static boolean isGifImage(String filename) {
        if (filename.endsWith(".gif") ||
                filename.endsWith(".GIF")) {
            return true;
        }
        return false;
    }

    public static boolean isPngImage(String filename) {
        if (filename.endsWith(".png") ||
                filename.endsWith(".PNG")) {
            return true;
        }
        return false;
    }

    public static boolean isJpegImage(String filename) {
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") ||
                filename.endsWith(".JPG") || filename.endsWith(".JPEG")) {
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
            Date lhsDate = DateHelper.getDateFromExplorerDateString(lhs.date);
            Date rhsDate = DateHelper.getDateFromExplorerDateString(rhs.date);
            return lhsDate.compareTo(rhsDate);
        }
    }

    public static class DateDescComparator implements Comparator<ExplorerItem> {
        @Override
        public int compare(ExplorerItem lhs, ExplorerItem rhs) {
            Date lhsDate = DateHelper.getDateFromExplorerDateString(lhs.date);
            Date rhsDate = DateHelper.getDateFromExplorerDateString(rhs.date);
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

    public static ArrayList<ExplorerItem> getImageFileList(List<ExplorerItem> fileList) {
        ArrayList<ExplorerItem> imageList = new ArrayList<>();

        for(ExplorerItem item : fileList) {
            if (item.type == ExplorerItem.FILETYPE_IMAGE) {
                imageList.add(item);
            }
        }
        return imageList;
    }

    public static ArrayList<ExplorerItem> getVideoFileList(List<ExplorerItem> fileList) {
        ArrayList<ExplorerItem> videoList = new ArrayList<>();

        for(ExplorerItem item : fileList) {
            if (item.type == ExplorerItem.FILETYPE_VIDEO) {
                videoList.add(item);
            }
        }
        return videoList;
    }

    public static ArrayList<ExplorerItem> getAudioFileList(List<ExplorerItem> fileList) {
        ArrayList<ExplorerItem> audioList = new ArrayList<>();

        for(ExplorerItem item : fileList) {
            if (item.type == ExplorerItem.FILETYPE_AUDIO) {
                audioList.add(item);
            }
        }
        return audioList;
    }
}
