package com.duongame.fileexplorer;

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
    public String size;
    public FileType type;

    public String path;

    public ExplorerFileItem(String name, String date, String size, FileType type) {
        this.name = name;
        this.date = date;
        this.size = size;
        this.type = type;
    }

    public String toString() {
        return "name="+name + " date="+date + " size="+size + " type="+type;
    }
}
