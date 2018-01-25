package com.duongame.archive;

import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;

import static com.duongame.helper.FileHelper.BLOCK_SIZE;

/**
 * Created by namjungsoo on 2018-01-23.
 */

public class ZipFile implements IArchiveFile {
    public class ZipHeader implements IArchiveHeader {
        String name;
        long size;
        public ZipHeader(String name, long size) {
            this.name = name;
            this.size = size;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public long getSize() {
            return size;
        }
    }

    String zipPath;

    public ZipFile(String zipPath) {
        this.zipPath = zipPath;
    }

    @Override
    public ArrayList<IArchiveHeader> getHeaders() {
        ZipArchiveInputStream stream;
        ZipEntry entry;
        ArrayList<IArchiveHeader> newHeaders = new ArrayList<>();

        try {
            stream = new ZipArchiveInputStream(new FileInputStream(zipPath));
            entry = (ZipEntry) stream.getNextEntry();
            while (entry != null) {
                newHeaders.add(new ZipHeader(entry.getName(), entry.getSize()));

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
