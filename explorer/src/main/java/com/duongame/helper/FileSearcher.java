package com.duongame.helper;

import com.duongame.adapter.ExplorerItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import static com.duongame.helper.FileHelper.getFileType;

/**
 * Created by namjungsoo on 2016-11-06.
 */

public class FileSearcher {
    public static class Result {
        public ArrayList<ExplorerItem> fileList;
        public ArrayList<ExplorerItem> imageList;
    }

    private String keyword;
    private String ext;
    private boolean excludeDirectory;
    private boolean recursiveDirectory;
    private Comparator<ExplorerItem> comparator;

    public FileSearcher setExtension(String ext) {
        this.ext = ext;
        return this;
    }

    public FileSearcher setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public FileSearcher setExcludeDirectory(boolean b) {
        this.excludeDirectory = b;
        return this;
    }

    public FileSearcher setRecursiveDirectory(boolean b) {
        this.recursiveDirectory = b;
        return this;
    }

    public FileSearcher setComparator(Comparator<ExplorerItem> comparator) {
        this.comparator = comparator;
        return this;
    }

    public Result search(String path) {
        File file = new File(path);
        if (file == null)
            return null;

        // 모든 파일 가져옴
        File[] files = file.listFiles();
        if (files == null)
            return null;

        ArrayList<ExplorerItem> fileList = new ArrayList<ExplorerItem>();
        ArrayList<ExplorerItem> directoryList = new ArrayList<ExplorerItem>();
        ArrayList<ExplorerItem> normalList = new ArrayList<ExplorerItem>();
        ArrayList<ExplorerItem> imageList = new ArrayList<ExplorerItem>();
        Result result = new Result();

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
                if (!excludeDirectory) {
                    item.size = -1;
                    directoryList.add(item);
                }

                if (recursiveDirectory) {
                    Result subFileList = search(fullPath);

                    if (subFileList != null) {
                        for (ExplorerItem subItem : subFileList.fileList) {
                            if (subItem.type == ExplorerItem.FileType.FOLDER) {
                                directoryList.add(subItem);
                            } else {
                                normalList.add(subItem);
                            }
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

        if(comparator == null) {
            comparator = new FileHelper.NameAscComparator();
        }
        // 디렉토리 우선 정렬 및 가나다 정렬
        Collections.sort(directoryList, comparator);
        Collections.sort(normalList, comparator);

        fileList.addAll(directoryList);
        fileList.addAll(normalList);

        // 이미지는 마지막에 모아서 처리한다.
        imageList.clear();
        for (int i = 0; i < normalList.size(); i++) {
            if (normalList.get(i).type == ExplorerItem.FileType.IMAGE) {
                imageList.add(normalList.get(i));
            }
        }

        result.fileList = fileList;
        result.imageList = imageList;
        return result;
    }

}
