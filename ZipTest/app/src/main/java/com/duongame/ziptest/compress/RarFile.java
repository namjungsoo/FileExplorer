package com.duongame.ziptest.compress;

import com.duongame.archive.Unrar;
import com.duongame.archive.UnrarHeader;
import com.duongame.ziptest.compress.common.ArchiveHeader;
import com.duongame.ziptest.compress.common.IArchiveFile;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2018-01-23.
 */

public class RarFile implements IArchiveFile {
    Unrar rar;

    public RarFile(String rarPath) {
        rar = new Unrar(rarPath);
    }

    @Override
    public ArrayList<ArchiveHeader> getHeaders() {
        ArrayList<UnrarHeader> headers = rar.getHeaders();
        ArrayList<ArchiveHeader> newHeaders = new ArrayList<>();

        for (UnrarHeader header : headers) {
            newHeaders.add(new ArchiveHeader(header.fileName, header.size));
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

    @Override
    public void destroy() {

    }
}
