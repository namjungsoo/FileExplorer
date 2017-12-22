package com.duongame.manager;

import android.os.Environment;

import com.duongame.adapter.ExplorerItem;
import com.duongame.helper.DateHelper;
import com.duongame.helper.FileHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by namjungsoo on 2016-11-06.
 */

public class ExplorerManager {
    private static final String initialPath = Environment.getExternalStorageDirectory().getAbsolutePath();
//    private static String lastPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    private ArrayList<ExplorerItem> imageList = new ArrayList<>();
    private String lastPath;
    private String keyword;
    private String ext;
    private boolean excludeDirectory;
    private boolean recursiveDirectory;


    public static String getInitialPath() {
        return initialPath;
    }

    public String getLastPath() {
        return lastPath;
    }

    public boolean isInitialPath(String path) {
        return path.equals(initialPath);
    }

    public void setExtension(String ext) {

    }

    public void setKeyword(String keyword) {

    }

    public void setExcludeDirectory(boolean b) {

    }

    public void setRecursiveDirectory(boolean b) {

    }

    public void setComparator(Comparator<ExplorerItem> comparator) {

    }

    public ArrayList<ExplorerItem> search(String path, String keyword, String ext, boolean excludeDirectory, boolean recursiveDirectory) {
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
            if (eachFile.getName().startsWith(".")) {// .으로 시작되면 패스 함 (숨김파일임)
                continue;
            }

            String name = eachFile.getName();
            Date dateSource = new Date(eachFile.lastModified());

            //String date = dateFormat.format(dateSource);
            String date = DateHelper.getExplorerDate(dateSource);
            long size = eachFile.length();

            ExplorerItem.FileType type = getFileType(eachFile);

            String fullPath = FileHelper.getFullPath(path, name);
            ExplorerItem item = new ExplorerItem(fullPath, name, date, size, type);

            if (type == ExplorerItem.FileType.FOLDER) {
                if (excludeDirectory == false) {
                    item.size = -1;
                    directoryList.add(item);
                }

                if (recursiveDirectory) {
                    ArrayList<ExplorerItem> subFileList = search(fullPath, keyword, ext, excludeDirectory, recursiveDirectory);

                    for (ExplorerItem subItem : subFileList) {
                        if (subItem.type == ExplorerItem.FileType.FOLDER) {
                            directoryList.add(subItem);
                        } else {
                            normalList.add(subItem);
                        }
                    }
                }
            } else {
                boolean willAdd = true;
                if (ext != null) {
                    willAdd = item.name.toLowerCase().endsWith(ext);
                }

                if (keyword != null && willAdd) {
                    willAdd = item.name.toLowerCase().contains(keyword);
                }

                if (willAdd)
                    normalList.add(item);
            }
        }

        // 디렉토리 우선 정렬 및 가나다 정렬
        Collections.sort(directoryList, new FileHelper.NameAscComparator());
        Collections.sort(normalList, new FileHelper.NameAscComparator());

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

    public ArrayList<ExplorerItem> search(String path) {
        return search(path, null, null, false, false);
    }

    public ArrayList<ExplorerItem> getImageList() {
        return imageList;
    }

    public static ExplorerItem.FileType getFileType(File eachFile) {
        ExplorerItem.FileType type = eachFile.isDirectory() ? ExplorerItem.FileType.FOLDER : ExplorerItem.FileType.FILE;
        final String lower = eachFile.getName().toLowerCase();

        if (FileHelper.isImage(eachFile.getName())) {
            type = ExplorerItem.FileType.IMAGE;
        } else if (lower.endsWith(".zip"))
            type = ExplorerItem.FileType.ZIP;
        else if (lower.endsWith(".pdf"))
            type = ExplorerItem.FileType.PDF;
        else if (lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".3gp") || lower.endsWith(".mkv") || lower.endsWith(".mov"))
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
