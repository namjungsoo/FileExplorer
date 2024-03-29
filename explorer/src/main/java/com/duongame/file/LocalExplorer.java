package com.duongame.file;

import com.duongame.adapter.ExplorerItem;
import com.duongame.helper.DateHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import timber.log.Timber;

/**
 * Created by namjungsoo on 2016-11-06.
 */

public class LocalExplorer extends FileExplorer {
    @Override
    public FileExplorer.Result search(String path) {
        Timber.e("LocalExplorer search begin");
        File file = new File(path);
        if (file == null)
            return null;

        // 모든 파일 가져옴
        File[] files = file.listFiles();
        if (files == null)
            return null;

        Timber.e("LocalExplorer listFiles end " + files.length);

        // 폴더를 우선하도록 정렬 해야 함
        // 안드로이드는 폴더와 파일을 섞어서 리턴을 해준다.
        Collections.sort(Arrays.asList(files), new FileExplorer.DirectoryPreferComparator());
        Timber.e("LocalExplorer listFiles sort end");

        ArrayList<ExplorerItem> fileList = new ArrayList<>();
        ArrayList<ExplorerItem> directoryList = new ArrayList<>();
        ArrayList<ExplorerItem> normalList = new ArrayList<>();
        FileExplorer.Result result = new FileExplorer.Result();

        // 파일로 아이템을 만듬
        for (File eachFile : files) {
            //if (eachFile.getName().equals(".") || eachFile.getName().equals("..")) {// .으로 시작되면 패스 함
            if (!isHiddenFile() && eachFile.getName().startsWith(".")) {// .으로 시작되면 패스 함 (숨김파일임)
                continue;
            }

            String name = eachFile.getName();
            Date dateSource = new Date(eachFile.lastModified());

            //String date = dateFormat.format(dateSource);
            //String date = dateSource.toString();
            String date = DateHelper.getExplorerDateString(dateSource);
            long size = eachFile.length();

            int type = FileHelper.getFileFolderType(eachFile);

            String fullPath = FileHelper.getFullPath(path, name);
            ExplorerItem item = new ExplorerItem(fullPath, name, date, size, type);

            if (type == ExplorerItem.FILETYPE_FOLDER) {
                if (!isExcludeDirectory()) {
                    item.size = -1;
                    directoryList.add(item);
                }

                if (isRecursiveDirectory()) {
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
                if (getExtensions() != null) {// 확장자가 맞으므로 더함
                    willAdd = false;
                    for (String ext : getExtensions()) {
                        willAdd |= item.name.endsWith(ext);
                    }
                }

                if (getKeyword() != null && willAdd) {// 키워드를 포함하므로 더함
                    willAdd = item.name.contains(getKeyword());
                }

                if (willAdd)
                    normalList.add(item);
            }
        }

        Timber.e("LocalExplorer file item end");
//        if (comparator == null) {
//            comparator = new FileHelper.NameAscComparator();
//        }

        if (getComparator() != null) {
            // 디렉토리 우선 정렬 및 가나다 정렬
            Collections.sort(directoryList, getComparator());
            Collections.sort(normalList, getComparator());
            fileList.addAll(directoryList);
            fileList.addAll(normalList);
        } else {
            // 삭제 리스트용이므로 파일먼저, 폴더 나중
            fileList.addAll(normalList);
            fileList.addAll(directoryList);
        }
        Timber.e("LocalExplorer file item sort end");

        // 이미지 리스트를 따로 모을 것인지?
        if (isImageListEnable()) {
//            ArrayList<ExplorerItem> imageList = new ArrayList<>();
//
//            // 이미지는 마지막에 모아서 처리한다.
//            for (int i = 0; i < normalList.size(); i++) {
//                if (normalList.get(i).type == ExplorerItem.FILETYPE_IMAGE) {
//                    imageList.add(normalList.get(i));
//                }
//            }
//
//            result.imageList = imageList;
            result.imageList = FileHelper.getImageFileList(normalList);
        }
        Timber.e("LocalExplorer image list end");

        if (isVideoListEnable()) {
            result.videoList = FileHelper.getVideoFileList(normalList);
        }

        if (isAudioListEnable()) {
            result.audioList = FileHelper.getAudioFileList(normalList);
        }

        result.fileList = fileList;
        Timber.e("LocalExplorer search end");
        return result;
    }
}
