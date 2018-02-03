package com.duongame.archive;

import com.hzy.lib7z.Z7Extractor;
import com.hzy.lib7z.Z7Header;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2018-01-23.
 */

public class Z7File implements IArchiveFile {
    Z7Extractor z7Extractor;

    public Z7File(String z7Path) {
        z7Extractor = new Z7Extractor(z7Path);
    }

    @Override
    public ArrayList<ArchiveHeader> getHeaders() {
        ArrayList<Z7Header> headers = z7Extractor.getHeaders();
        if(headers == null)
            return null;

        ArrayList<ArchiveHeader> newHeaders = new ArrayList<>();
        for (Z7Header header : headers) {
            newHeaders.add(new ArchiveHeader(header.fileName, header.size));
        }
        return newHeaders;
    }

    @Override
    public boolean extractFile(String fileName, String destPath) {
        return z7Extractor.extractFile(fileName, destPath, null);
    }

    @Override
    public boolean extractAll(String destPath) {
        return z7Extractor.extractAll(destPath, null);
    }

    @Override
    public void destroy() {
        z7Extractor.destroy();
    }
}
