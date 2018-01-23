package com.duongame.archive;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2018-01-23.
 */

public class RarFile implements IArchiveFile {
    Unrar rar;

    public class RarHeader implements IArchiveHeader {
        String name;

        public RarHeader(String name) {
            this.name = name;
        }
        @Override
        public String getName() {
            return name;
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
            newHeaders.add(new RarHeader(header.fileName));
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
