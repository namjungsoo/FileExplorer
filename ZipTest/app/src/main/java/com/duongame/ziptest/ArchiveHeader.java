package com.duongame.ziptest;

/**
 * Created by namjungsoo on 2018-01-23.
 */

public class ArchiveHeader {
    String name;
    long size;

    public ArchiveHeader(String name, long size) {
        this.name = name;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }
}
