package com.duongame.explorer.manager;

import android.os.Environment;

import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.helper.DateHelper;
import com.duongame.explorer.helper.FileHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Created by namjungsoo on 2016-11-06.
 */

public class ExplorerManager {
    private static String lastPath;
    private static final String initialPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static ArrayList<ExplorerItem> imageList = new ArrayList<>();

    public static String getLastPath() {
        return lastPath;
    }

    public static boolean isInitialPath() {
        return lastPath.equals(initialPath);
    }

    public static ArrayList<ExplorerItem> search(String path) {
        if (path == null) {
            path = initialPath;
        }
        lastPath = path;

        ArrayList<ExplorerItem> fileList = new ArrayList<ExplorerItem>();
        ArrayList<ExplorerItem> directoryList = new ArrayList<ExplorerItem>();
        ArrayList<ExplorerItem> normalList = new ArrayList<ExplorerItem>();

        File file = new File(path);
        if (file == null)
            return fileList;

        // 모든 파일 가져옴
        File[] files = file.listFiles();
        if (files == null)
            return fileList;

        // 파일로 아이템을 만듬
        for (int i = 0; i < files.length; i++) {
            File eachFile = files[i];
            //if (eachFile.getName().equals(".") || eachFile.getName().equals("..")) {// .으로 시작되면 패스 함
            if (eachFile.getName().startsWith(".")) {// .으로 시작되면 패스 함
                continue;
            }

            String name = eachFile.getName();
            Date dateSource = new Date(eachFile.lastModified());

            //String date = dateFormat.format(dateSource);
            String date = DateHelper.getExplorerDate(dateSource);
            long size = eachFile.length();

            ExplorerItem.FileType type = getFileType(eachFile);

            ExplorerItem item = new ExplorerItem(FileHelper.getFullPath(path, name), name, date, size, type);
            if (type == ExplorerItem.FileType.DIRECTORY) {
                item.size = -1;
                directoryList.add(item);
            } else {
                normalList.add(item);
            }
//            Log.d("TAG", item.toString());
        }

        // 디렉토리 우선 정렬 및 가나다 정렬
        Collections.sort(directoryList, new FileHelper.FileNameAscendingComparator());
        Collections.sort(normalList, new FileHelper.FileNameAscendingComparator());

        fileList.addAll(directoryList);
        fileList.addAll(normalList);

        imageList.clear();
        for (int i = 0; i < normalList.size(); i++) {
            if (normalList.get(i).type == ExplorerItem.FileType.IMAGE) {
                imageList.add(normalList.get(i));
            }
        }
        return fileList;
    }

    public static ArrayList<ExplorerItem> getImageList() {
        return imageList;
    }

    public static ExplorerItem.FileType getFileType(File eachFile) {
        ExplorerItem.FileType type = eachFile.isDirectory() ? ExplorerItem.FileType.DIRECTORY : ExplorerItem.FileType.FILE;
        final String lower = eachFile.getName().toLowerCase();

        if (FileHelper.isImage(eachFile.getName())) {
            type = ExplorerItem.FileType.IMAGE;
        }
        else if (lower.endsWith(".zip"))
            type = ExplorerItem.FileType.ZIP;
//        else if (lower.endsWith(".rar"))
//            type = ExplorerItem.FileType.RAR;
        else if (lower.endsWith(".pdf"))
            type = ExplorerItem.FileType.PDF;
        else if (lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".3gp") || lower.endsWith(".mkv"))
            type = ExplorerItem.FileType.VIDEO;
        else if (lower.endsWith(".mp3"))
            type = ExplorerItem.FileType.AUDIO;
        else if (lower.endsWith(".txt") || lower.endsWith(".cap") || lower.endsWith(".log"))
            type = ExplorerItem.FileType.TEXT;
        else if (lower.endsWith(".apk"))
            type = ExplorerItem.FileType.APK;

        return type;
    }
}
