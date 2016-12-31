package com.duongame.explorer.adapter;

import static com.duongame.explorer.adapter.ExplorerFileItem.Side.SIDE_ALL;

/**
 * Created by namjungsoo on 2016-11-06.
 */

public class ExplorerFileItem implements Cloneable {
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
        APK,
        FILETYPE_ALL
    }

    public enum Side {
        SIDE_ALL,
        LEFT,
        RIGHT,
        OTHER
    }

    public String name;
    public String date;
    public long size;
    public String path;
    public FileType type;

    public Side side = SIDE_ALL;
    public int index;

    // ZIP 추가 데이터
    public int width;
    public int height;

    public ExplorerFileItem(String path, String name, String date, long size, FileType type) {
        this.path = path;
        this.name = name;
        this.date = date;
        this.size = size;
        this.type = type;
    }

    public String toString() {
        return "path=" + path + " name=" + name + " date=" + date + " size=" + size + " type=" + type + " side=" + side + " index=" + index;
    }

    @Override
    public Object clone() {
        Object item;
        try {
            item = super.clone();
            return item;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
