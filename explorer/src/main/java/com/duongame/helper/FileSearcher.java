package com.duongame.helper;

import com.duongame.adapter.ExplorerItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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

    private ArrayList<String> extensions;
    private String keyword;
    private Comparator<ExplorerItem> comparator;

    private boolean excludeDirectory;
    private boolean recursiveDirectory;
    private boolean hiddenFile;
    private boolean imageListEnable;

    public FileSearcher setExtensions(ArrayList<String> extensions) {
        this.extensions = extensions;
        return this;
    }

    public FileSearcher setExtension(String ext) {
        //this.extension = ext;
        if(extensions == null) {
            extensions = new ArrayList<>();
        }
        extensions.clear();
        extensions.add(ext);
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

    public FileSearcher setHiddenFile(boolean b) {
        this.hiddenFile = b;
        return this;
    }

    public FileSearcher setImageListEnable(boolean b) {
        this.imageListEnable = b;
        return this;
    }

    public static class DirectoryPreferComparator implements Comparator<File> {
        @Override
        public int compare(File lhs, File rhs) {
            if(lhs.isDirectory())
                return -1;
            if(rhs.isDirectory())
                return 1;
            return 0;
        }
    }


    public Result search(String path) {
        File file = new File(path);
        if (file == null)
            return null;

        // 모든 파일 가져옴
        File[] files = file.listFiles();
        if (files == null)
            return null;

        // 폴더를 우선하도록 정렬 해야 함
        Collections.sort(Arrays.asList(files), new DirectoryPreferComparator());

        ArrayList<ExplorerItem> fileList = new ArrayList<ExplorerItem>();
        ArrayList<ExplorerItem> directoryList = new ArrayList<ExplorerItem>();
        ArrayList<ExplorerItem> normalList = new ArrayList<ExplorerItem>();
        Result result = new Result();

        // 파일로 아이템을 만듬
        for (int i = 0; i < files.length; i++) {
            File eachFile = files[i];
            //if (eachFile.getName().equals(".") || eachFile.getName().equals("..")) {// .으로 시작되면 패스 함
            if (!hiddenFile && eachFile.getName().startsWith(".")) {// .으로 시작되면 패스 함 (숨김파일임)
                continue;
            }

            String name = eachFile.getName();
            Date dateSource = new Date(eachFile.lastModified());

            //String date = dateFormat.format(dateSource);
            String date = DateHelper.getExplorerDateString(dateSource);
            long size = eachFile.length();

            int type = getFileType(eachFile);

            String fullPath = FileHelper.getFullPath(path, name);
            ExplorerItem item = new ExplorerItem(fullPath, name, date, size, type);

            if (type == ExplorerItem.FILETYPE_FOLDER) {
                if (!excludeDirectory) {
                    item.size = -1;
                    directoryList.add(item);
                }

                if (recursiveDirectory) {
                    Result subFileList = search(fullPath);

                    if (subFileList != null) {
                        for (ExplorerItem subItem : subFileList.fileList) {
                            if (subItem.type == ExplorerItem.FILETYPE_FOLDER) {
                                directoryList.add(subItem);
                            } else {
                                normalList.add(subItem);
                            }
                        }
                    }
                }
            } else {
                // 파일이므로 더할지 말지 결정을 해야 함
                boolean willAdd = true;
                if (extensions != null) {// 확장자가 맞으므로 더함
                    willAdd = false;
                    for(String ext : extensions) {
                        willAdd |= item.name.endsWith(ext);
                    }
                }

                if (keyword != null && willAdd) {// 키워드를 포함하므로 더함
                    willAdd = item.name.contains(keyword);
                }

                if (willAdd)
                    normalList.add(item);
            }
        }

//        if (comparator == null) {
//            comparator = new FileHelper.NameAscComparator();
//        }

        if(comparator != null) {
            // 디렉토리 우선 정렬 및 가나다 정렬
            Collections.sort(directoryList, comparator);
            Collections.sort(normalList, comparator);
            fileList.addAll(directoryList);
            fileList.addAll(normalList);
        } else {
            // 삭제 리스트용이므로 파일먼저, 폴더 나중
            fileList.addAll(normalList);
            fileList.addAll(directoryList);
        }

        if(imageListEnable) {
            ArrayList<ExplorerItem> imageList = new ArrayList<ExplorerItem>();

            // 이미지는 마지막에 모아서 처리한다.
            for (int i = 0; i < normalList.size(); i++) {
                if (normalList.get(i).type == ExplorerItem.FILETYPE_IMAGE) {
                    imageList.add(normalList.get(i));
                }
            }

            result.imageList = imageList;
        }

        result.fileList = fileList;
        return result;
    }
}
