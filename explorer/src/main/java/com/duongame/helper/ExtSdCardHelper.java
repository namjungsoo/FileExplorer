package com.duongame.helper;

import android.os.Environment;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Locale;

import static android.os.Environment.getExternalStorageDirectory;
import static com.duongame.file.FileHelper.BLOCK_SIZE;

/**
 * Created by namjungsoo on 2017-01-22.
 */

public class ExtSdCardHelper {
    public static String getExternalSdCardPath() {
        File root = Environment.getExternalStorageDirectory();
        if (root == null)
            return null;

        File parent = root.getParentFile();
        if (parent == null)
            return null;

        File storage = parent.getParentFile();
        if (storage == null)
            return null;

        File[] files = storage.listFiles();
        if(files == null)
            return null;

        for (File file : files) {
            String path = file.getName();
            if (path.equals("emulated"))
                continue;
            if (path.equals("self"))
                continue;
            return file.getAbsolutePath();
        }
        return null;
    }
}
