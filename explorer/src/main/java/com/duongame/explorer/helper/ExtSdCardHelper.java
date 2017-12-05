package com.duongame.explorer.helper;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Locale;

/**
 * Created by namjungsoo on 2017-01-22.
 */

public class ExtSdCardHelper {
    public static HashSet<String> getExternalMounts() {
        final HashSet<String> out = new HashSet<String>();
        String reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
        String s = "";
        try {
            final Process process = new ProcessBuilder().command("mount")
                    .redirectErrorStream(true).start();
            process.waitFor();
            final InputStream is = process.getInputStream();
            final byte[] buffer = new byte[1024];
            while (is.read(buffer) != -1) {
                s = s + new String(buffer);
            }
            is.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // parse output
        final String[] lines = s.split("\n");
        for (String line : lines) {
            if (!line.toLowerCase(Locale.US).contains("asec")) {
                if (line.matches(reg)) {
                    String[] parts = line.split(" ");
                    for (String part : parts) {
                        if (part.startsWith("/"))
                            if (!part.toLowerCase(Locale.US).contains("vold"))
                                out.add(part);
                    }
                }
            }
        }
        return out;
    }

    public static String getExternalSdCardPath() {
        // 외장 SD카드 주소
        HashSet<String> mountSet = getExternalMounts();
        if (mountSet.size() == 0)
            return null;

        String extSdCard = mountSet.iterator().next();
        extSdCard = "/storage/" + extSdCard.substring(extSdCard.lastIndexOf("/") + 1);
        return extSdCard;
    }
}
