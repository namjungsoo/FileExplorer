package com.duongame.explorer.helper;

import android.content.Context;

import com.duongame.explorer.adapter.ExplorerFileItem;

import java.io.File;
import java.util.Comparator;

/**
 * Created by namjungsoo on 2016-11-19.
 */

public class FileHelper {
    public static boolean isImage(String filename) {
        final String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".gif")
                || lower.endsWith(".png")
                ) {
            return true;
        }
        return false;
    }

    public static boolean isJpegImage(String filename) {
        final String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                ) {
            return true;
        }
        return false;
    }

    public static File getZipCacheFile(Context context, String filename) {
        final String filesDir = context.getFilesDir().getAbsolutePath();
        final String cachePath = filesDir + "/" + filename;
        return new File(cachePath);
    }

    public static String getZipCachePath(Context context, String filename) {
        final String filesDir = context.getFilesDir().getAbsolutePath();
        final String cachePath = filesDir + "/" + filename;
        return cachePath;
    }

    public static class FileNameCompare implements Comparator<ExplorerFileItem> {
        @Override
        public int compare(ExplorerFileItem lhs, ExplorerFileItem rhs) {
            return lhs.name.compareToIgnoreCase(rhs.name);
        }
    }
}
