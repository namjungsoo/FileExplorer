package com.duongame.archive;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2018-01-23.
 */

//TODO: 추후 구현
public class Z7File implements IArchiveFile {
    @Override
    public ArrayList<IArchiveHeader> getHeaders() {
        return null;
    }

    @Override
    public boolean extractFile(String fileName, String destPath) {
        return false;
    }

    @Override
    public boolean extractAll(String destPath) {
        return false;
    }
}
