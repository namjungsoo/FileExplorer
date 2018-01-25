package com.duongame.archive;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2018-01-23.
 */

public class RarFile implements IArchiveFile {
    Unrar rar;

    public class RarHeader implements IArchiveHeader {
        String name;
        long size;

        public RarHeader(String name, long size) {
            this.name = name;
            this.size = size;
        }
        @Override
        public String getName() {
            return name;
        }

        @Override
        public long getSize() {
            return 0;
        }
    }

    public RarFile(String rarPath) {
        rar = new Unrar(rarPath);
    }

    @Override
    public ArrayList<IArchiveHeader> getHeaders() {
        ArrayList<UnrarHeader> headers = rar.getHeaders();
        ArrayList<IArchiveHeader> newHeaders = new ArrayList<>();

        for(UnrarHeader header : headers) {
            newHeaders.add(new RarHeader(header.fileName, header.size));
        }

        return newHeaders;
    }

    @Override
    public boolean extractFile(String fileName, String destPath) {
        return rar.extractFile(fileName, destPath, null);
    }

    @Override
    public boolean extractAll(String destPath) {
        return rar.extractAll(destPath, null);
    }
}
