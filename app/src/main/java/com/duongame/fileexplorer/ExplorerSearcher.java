package com.duongame.fileexplorer;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by namjungsoo on 2016-11-06.
 */

public class ExplorerSearcher {
    private String lastPath;
    private final String initialPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd(E) hh:mm:ss a");

    public String getLastPath() {
        return lastPath;
    }

    public boolean isInitialPath() {
        return lastPath.equals(initialPath);
    }

    public ArrayList<ExplorerFileItem> search(String path) {
        if(path == null) {
            path = initialPath;
        }
        lastPath = path;

        ArrayList<ExplorerFileItem> fileList = new ArrayList<ExplorerFileItem>();
        ArrayList<ExplorerFileItem> directoryList = new ArrayList<ExplorerFileItem>();
        ArrayList<ExplorerFileItem> normalList = new ArrayList<ExplorerFileItem>();

        File file = new File(path);
        if (file == null)
            return null;

        // 모든 파일 가져옴
        File[] files = file.listFiles();
        if (files == null)
            return null;

        // 파일로 아이템을 만듬
        for(int i=0; i<files.length; i++) {
            File eachFile = files[i];
            if(eachFile.getName().startsWith(".")) {// .으로 시작되면 패스 함
                continue;
            }

            String name = eachFile.getName();
            Date dateSource = new Date(eachFile.lastModified());

            String date = dateFormat.format(dateSource);
            String size = String.valueOf(eachFile.length());

            ExplorerFileItem.FileType type = getFileType(eachFile);

            ExplorerFileItem item = new ExplorerFileItem(name, date, size, type);
            if(type == ExplorerFileItem.FileType.DIRECTORY) {
                item.size = "";
                directoryList.add(item);
            } else {
//                item.size = String.valueOf(eachFile.length());
                normalList.add(item);
            }
//            fileList.add(item);
            Log.d("TAG", item.toString());
        }

        // 디렉토리 우선 정렬 및 가나다 정렬
        Collections.sort(directoryList, new FileTypeCompare());
        Collections.sort(normalList, new FileTypeCompare());

        fileList.addAll(directoryList);
        fileList.addAll(normalList);
        return fileList;
    }

    public ExplorerFileItem.FileType getFileType(File eachFile) {
        ExplorerFileItem.FileType type = eachFile.isDirectory() ? ExplorerFileItem.FileType.DIRECTORY : ExplorerFileItem.FileType.FILE;

        if(eachFile.getName().toLowerCase().endsWith(".jpg")
                || eachFile.getName().toLowerCase().endsWith(".jpeg")
                || eachFile.getName().toLowerCase().endsWith(".gif")
                || eachFile.getName().toLowerCase().endsWith(".png")
                ) {
            type = ExplorerFileItem.FileType.IMAGE;
        }
        if(eachFile.getName().toLowerCase().endsWith(".zip"))
            type = ExplorerFileItem.FileType.ZIP;
        if(eachFile.getName().toLowerCase().endsWith(".rar"))
            type = ExplorerFileItem.FileType.RAR;
        if(eachFile.getName().toLowerCase().endsWith(".pdf"))
            type = ExplorerFileItem.FileType.PDF;
        if(eachFile.getName().toLowerCase().endsWith(".mp3"))
            type = ExplorerFileItem.FileType.AUDIO;
        if(eachFile.getName().toLowerCase().endsWith(".txt"))
            type = ExplorerFileItem.FileType.TEXT;

        return type;
    }

    class FileTypeCompare implements Comparator<ExplorerFileItem> {
        @Override
        public int compare(ExplorerFileItem lhs, ExplorerFileItem rhs) {
            return lhs.name.compareToIgnoreCase(rhs.name);
        }
    }
}
