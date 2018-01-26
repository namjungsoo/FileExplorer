package com.duongame.ziptest;

import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;

/**
 * Created by namjungsoo on 2018-01-23.
 */

public class ZipApacheFile implements IArchiveFile {
    private String zipPath;
    final int BLOCK_SIZE = 8*1024;

    public ZipApacheFile(String zipPath) {
        this.zipPath = zipPath;
    }

    @Override
    public ArrayList<ArchiveHeader> getHeaders() {
        ZipArchiveInputStream stream;
        ZipEntry entry;
        ArrayList<ArchiveHeader> newHeaders = new ArrayList<>();

        try {
            stream = new ZipArchiveInputStream(new FileInputStream(zipPath));
            entry = (ZipEntry) stream.getNextEntry();
            while (entry != null) {
                newHeaders.add(new ArchiveHeader(entry.getName(), entry.getSize()));

                entry = (ZipEntry) stream.getNextEntry();
            }
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return newHeaders;
    }

    @Override
    public boolean extractFile(String fileName, String destPath) {
        ZipArchiveInputStream stream;
        ZipEntry entry;

        try {
            stream = new ZipArchiveInputStream(new FileInputStream(zipPath));
            entry = (ZipEntry) stream.getNextEntry();

            while (entry != null) {

                if(fileName.equals(entry.getName())) {
                    byte[] buf = new byte[BLOCK_SIZE];

                    String target = destPath + "/" + entry.getName();
                    new File(target).getParentFile().mkdirs();

                    // 파일 복사 부분
                    FileOutputStream outputStream = new FileOutputStream(target);

                    int nRead = 0;
                    while ((nRead = stream.read(buf)) > 0) {
                        outputStream.write(buf, 0, nRead);
                    }

                    outputStream.close();
                    return true;
                }

                entry = (ZipEntry) stream.getNextEntry();
            }
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean extractAll(String destPath) {
        ZipArchiveInputStream stream;
        ZipEntry entry;

        try {
            stream = new ZipArchiveInputStream(new FileInputStream(zipPath));
            entry = (ZipEntry) stream.getNextEntry();

            byte[] buf = new byte[BLOCK_SIZE];
            while (entry != null) {
                String target = destPath + "/" + entry.getName();
                new File(target).getParentFile().mkdirs();

                // 파일 복사 부분
                FileOutputStream outputStream = new FileOutputStream(target);

                int nRead = 0;
                while ((nRead = stream.read(buf)) > 0) {
                    outputStream.write(buf, 0, nRead);
                }

                outputStream.close();

                entry = (ZipEntry) stream.getNextEntry();
            }
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
