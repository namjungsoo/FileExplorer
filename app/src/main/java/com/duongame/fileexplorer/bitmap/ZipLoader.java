package com.duongame.fileexplorer.bitmap;

import android.util.Log;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.util.List;

/**
 * Created by namjungsoo on 2016-11-19.
 */

public class ZipLoader {
    public static void load(String filename) throws ZipException {
        ZipFile zipFile = new ZipFile(filename);
        List<FileHeader> headers = zipFile.getFileHeaders();
        for(FileHeader header : headers) {
            Log.d("ZipLoader", "filename="+header.getFileName());
        }

        // 이미 풀어놓은게 없으면 AsyncTask로 로딩함
        // 첫번째 이미지 파일이 로딩이 끝나면 바로 띄운다
    }
}
