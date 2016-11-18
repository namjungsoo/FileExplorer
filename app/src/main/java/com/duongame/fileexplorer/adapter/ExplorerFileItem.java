package com.duongame.fileexplorer.adapter;

/**
 * Created by namjungsoo on 2016-11-06.
 */

public class ExplorerFileItem {
    public enum FileType {
        DIRECTORY,
        IMAGE,
        VIDEO,
        AUDIO,
        ZIP,
        RAR,
        PDF,
        TEXT,
        FILE,
        ALL
    }

    public String name;
    public String date;
    public long size;
    public String path;
    public FileType type;

    public ExplorerFileItem(String path, String name, String date, long size, FileType type) {
        this.path = path + "/" + name;
        this.name = name;
        this.date = date;
        this.size = size;
        this.type = type;
    }

    public String toString() {
        return "path=" + path + " name=" + name + " date=" + date + " size=" + size + " type=" + type;
    }
}
