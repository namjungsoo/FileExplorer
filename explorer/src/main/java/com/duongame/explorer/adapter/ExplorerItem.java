package com.duongame.explorer.adapter;

import static com.duongame.explorer.adapter.ExplorerItem.Side.SIDE_ALL;

/**
 * Created by namjungsoo on 2016-11-06.
 */

public class ExplorerItem implements Cloneable {
    public enum FileType {
        DIRECTORY(0),
        IMAGE(1),
        VIDEO(2),
        AUDIO(3),
        ZIP(4),
        RAR(5),
        PDF(6),
        TEXT(7),
        FILE(8),
        APK(9),
        FILETYPE_ALL(10);

        private int value;
        FileType(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
        public void setValue(int value) {
            this.value = value;
        }
    }

    public enum Side {
        SIDE_ALL(0),
        LEFT(1),
        RIGHT(2),
        OTHER(3);

        private int value;
        Side(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
        public void setValue(int value) {
            this.value = value;
        }
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

    public ExplorerItem(String path, String name, String date, long size, FileType type) {
        this.path = path;
        this.name = name;
        this.date = date;
        this.size = size;
        this.type = type;
    }

    public String toString() {
        return "path=" + path + " name=" + name + " date=" + date + " size=" + size + " type=" + type + " side=" + side + " index=" + index + " width=" + width + " height=" + height;
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