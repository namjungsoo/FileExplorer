package com.duongame.fileexplorer;

import android.graphics.Bitmap;

/**
 * Created by namjungsoo on 2016-11-06.
 */

class ExplorerFileItem {
    public enum FileType {
        DIRECTORY,
        IMAGE,
        VIDEO,
        COMPRESSED,
        NORMAL,
    }

    String name;
    String date;
    String size;
    FileType type;
    public Bitmap bitmap;
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
