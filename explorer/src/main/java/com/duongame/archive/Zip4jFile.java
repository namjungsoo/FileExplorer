package com.duongame.archive;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by namjungsoo on 2018-01-28.
 */

public class Zip4jFile implements IArchiveFile {
    ZipFile file;

    public Zip4jFile(String zipPath) {
        try {
            file = new ZipFile(zipPath);
        } catch (ZipException e) {
            e.printStackTrace();
            file = null;
        }
    }

    @Override
    public boolean isEncryped() {
        if (file != null) {
            try {
                return file.isEncrypted();
            } catch (Exception e) {

            }
        }
        return false;
    }

    @Override
    public void setPassword(String password) {
        if(file != null) {
            try {
                file.setPassword(password);
            } catch (ZipException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public ArrayList<ArchiveHeader> getHeaders() {
        if (file != null) {
            try {
                List<FileHeader> headers = file.getFileHeaders();
                if (headers == null)
                    return null;

                ArrayList<ArchiveHeader> newHeaders = new ArrayList<>();
                for (FileHeader header : headers) {
                    newHeaders.add(new ArchiveHeader(header.getFileName(), header.getUncompressedSize()));
                }
                return newHeaders;
            } catch (ZipException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    @Override
    public boolean extractFile(String fileName, String destPath) {
        try {
            file.extractFile(fileName, destPath);
            return true;
        } catch (ZipException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean extractAll(String destPath) {
        try {
            file.extractAll(destPath);
            return true;
        } catch (ZipException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void destroy() {

    }
}
