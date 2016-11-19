package com.duongame.fileexplorer.helper;

import android.os.Environment;

import com.duongame.fileexplorer.adapter.ExplorerFileItem;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Created by namjungsoo on 2016-11-06.
 */

public class ExplorerSearcher {
    private static String lastPath;
    private static final String initialPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd(E) hh:mm:ss a");
    private static ArrayList<ExplorerFileItem> imageList = new ArrayList<>();

    public static String getLastPath() {
        return lastPath;
    }

    public static boolean isInitialPath() {
        return lastPath.equals(initialPath);
    }

    public static ArrayList<ExplorerFileItem> search(String path) {
        if (path == null) {
            path = initialPath;
        }
        lastPath = path;

        ArrayList<ExplorerFileItem> fileList = new ArrayList<ExplorerFileItem>();
        ArrayList<ExplorerFileItem> directoryList = new ArrayList<ExplorerFileItem>();
        ArrayList<ExplorerFileItem> normalList = new ArrayList<ExplorerFileItem>();

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
            if (eachFile.getName().equals(".") ||
                    eachFile.getName().equals("..")) {// .으로 시작되면 패스 함
                continue;
            }

            String name = eachFile.getName();
            Date dateSource = new Date(eachFile.lastModified());

            String date = dateFormat.format(dateSource);
            long size = eachFile.length();

            ExplorerFileItem.FileType type = getFileType(eachFile);

            ExplorerFileItem item = new ExplorerFileItem(path, name, date, size, type);
            if (type == ExplorerFileItem.FileType.DIRECTORY) {
                item.size = -1;
                directoryList.add(item);
            } else {
                normalList.add(item);
            }
//            Log.d("TAG", item.toString());
        }

        // 디렉토리 우선 정렬 및 가나다 정렬
        Collections.sort(directoryList, new FileHelper.FileNameCompare());
        Collections.sort(normalList, new FileHelper.FileNameCompare());

        fileList.addAll(directoryList);
        fileList.addAll(normalList);

        imageList.clear();
        for (int i = 0; i < normalList.size(); i++) {
            if (normalList.get(i).type == ExplorerFileItem.FileType.IMAGE) {
                imageList.add(normalList.get(i));
            }
        }
        return fileList;
    }

    public static ArrayList<ExplorerFileItem> getImageList() {
        return imageList;
    }

    public static ExplorerFileItem.FileType getFileType(File eachFile) {
        ExplorerFileItem.FileType type = eachFile.isDirectory() ? ExplorerFileItem.FileType.DIRECTORY : ExplorerFileItem.FileType.FILE;
        final String lower = eachFile.getName().toLowerCase();

        if (FileHelper.isImage(eachFile.getName())) {
            type = ExplorerFileItem.FileType.IMAGE;
        }

        if (lower.endsWith(".zip"))
            type = ExplorerFileItem.FileType.ZIP;
        if (lower.endsWith(".rar"))
            type = ExplorerFileItem.FileType.RAR;
        if (lower.endsWith(".pdf"))
            type = ExplorerFileItem.FileType.PDF;
        if (lower.endsWith(".mp3"))
            type = ExplorerFileItem.FileType.AUDIO;
        if (lower.endsWith(".txt"))
            type = ExplorerFileItem.FileType.TEXT;

        return type;
    }
}
