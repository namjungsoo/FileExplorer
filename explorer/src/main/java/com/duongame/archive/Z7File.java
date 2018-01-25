package com.duongame.archive;

import com.hzy.lib7z.Z7Extractor;
import com.hzy.lib7z.Z7Header;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2018-01-23.
 */

//TODO: 추후 구현
public class Z7File implements IArchiveFile {
    String z7Path;

    public Z7File(String z7Path) {
        this.z7Path = z7Path;
    }

    @Override
    public ArrayList<ArchiveHeader> getHeaders() {
        ArrayList<Z7Header> headers = Z7Extractor.getHeaders(z7Path);
        ArrayList<ArchiveHeader> newHeaders = new ArrayList<>();

        for (Z7Header header : headers) {
            newHeaders.add(new ArchiveHeader(header.fileName, header.size));
        }
        return null;
    }

    @Override
    public boolean extractFile(String fileName, String destPath) {
        return Z7Extractor.extractFile(z7Path, fileName, destPath, null);
    }

    @Override
    public boolean extractAll(String destPath) {
        return Z7Extractor.extractAll(z7Path, destPath, null);
    }
}
