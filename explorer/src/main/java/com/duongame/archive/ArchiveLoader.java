package com.duongame.archive;

import com.duongame.adapter.ExplorerItem;
import com.duongame.task.zip.LoadBookTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by namjungsoo on 2018-01-23.
 */

// ZipLoader의 일반화
public class ArchiveLoader<T> {
    private LoadBookTask task;
    private String extractPath;
    private ExplorerItem.Side side = ExplorerItem.Side.LEFT;
    private int extract;
    private ArchiveLoaderListener listener;

    private List<IArchiveHeader> zipHeaders;
    private T zilFile;

    public ArchiveLoader(T file) {
        this.zilFile = file;
    }

    public interface ArchiveLoaderListener {
        void onSuccess(int i, ArrayList<ExplorerItem> zipImageList, int totalFileCount);

        void onFail(int i, String name);

        void onFinish(ArrayList<ExplorerItem> zipImageList, int totalFileCount);
    }

//    public void cancelTask() {
//        if (task != null) {
//            task.cancel(true);
//        }
//    }
//
//    public void setZipImageList(ArrayList<ExplorerItem> zipImageList) {
//        if (task != null && !task.isCancelled()) {
//            task.setZipImageList(zipImageList);
//        }
//    }
//
//    public void setSide(ExplorerItem.Side side) {
//        if (task != null && !task.isCancelled()) {
//            task.setSide(side);
//        }
//    }
//
//    public void pause() {
//        if (task != null && !task.isCancelled()) {
//            task.setPauseWork(true);
//        }
//    }
//
//    public void resume() {
//        if (task != null && !task.isCancelled()) {
//            task.setPauseWork(false);
//        }
//    }
//
//    private void filterImageList(ArrayList<ExplorerItem> imageList) {
//        for (FileHeader header : zipHeaders) {
//            final String name = header.getFileName();
//            if (FileHelper.isImage(name)) {
//                imageList.add(new ExplorerItem(FileHelper.getFullPath(extractPath, name), name, "", 0, ExplorerItem.FileType.IMAGE));
//            }
//        }
//        Collections.sort(imageList, new FileHelper.NameAscComparator());
//    }

}
