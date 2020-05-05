package com.duongame.file;

import com.duongame.adapter.ExplorerItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

public abstract class FileExplorer {
    public static class Result {
        public ArrayList<ExplorerItem> fileList;
        public ArrayList<ExplorerItem> imageList;

        public ArrayList<ExplorerItem> videoList;
        public ArrayList<ExplorerItem> audioList;
    }

    private ArrayList<String> extensions;
    private String keyword;
    private Comparator<ExplorerItem> comparator;

    private boolean excludeDirectory;
    private boolean recursiveDirectory;
    private boolean hiddenFile;
    private boolean imageListEnable;

    public abstract Result search(String path);

    public FileExplorer setExtensions(ArrayList<String> extensions) {
        this.extensions = extensions;
        return this;
    }

    public FileExplorer setExtension(String ext) {
        //this.extension = ext;
        if(extensions == null) {
            extensions = new ArrayList<>();
        }
        extensions.clear();
        extensions.add(ext);
        return this;
    }

    public FileExplorer setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public FileExplorer setExcludeDirectory(boolean b) {
        this.excludeDirectory = b;
        return this;
    }

    public FileExplorer setRecursiveDirectory(boolean b) {
        this.recursiveDirectory = b;
        return this;
    }

    public FileExplorer setComparator(Comparator<ExplorerItem> comparator) {
        this.comparator = comparator;
        return this;
    }

    public FileExplorer setHiddenFile(boolean b) {
        this.hiddenFile = b;
        return this;
    }

    public FileExplorer setImageListEnable(boolean b) {
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

    public ArrayList<String> getExtensions() {
        return extensions;
    }

    public String getKeyword() {
        return keyword;
    }

    public Comparator<ExplorerItem> getComparator() {
        return comparator;
    }

    public boolean isExcludeDirectory() {
        return excludeDirectory;
    }

    public boolean isRecursiveDirectory() {
        return recursiveDirectory;
    }

    public boolean isHiddenFile() {
        return hiddenFile;
    }

    public boolean isImageListEnable() {
        return imageListEnable;
    }
}
