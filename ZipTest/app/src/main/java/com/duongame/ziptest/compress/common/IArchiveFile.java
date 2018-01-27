package com.duongame.ziptest.compress.common;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2018-01-23.
 */

public interface IArchiveFile {
    ArrayList<ArchiveHeader> getHeaders();
    boolean extractFile(String fileName, String destPath);
    boolean extractAll(String destPath);
    void destroy();
}
