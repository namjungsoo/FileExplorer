package com.duongame.fileexplorer.bitmap;

import android.os.AsyncTask;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by namjungsoo on 2016-11-19.
 */

public class ZipLoader {
    private static class ZipExtractTask extends AsyncTask<String, Integer, Void> {

        @Override
        protected Void doInBackground(String... params) {
            return null;
        }
    }

    public static void load(String filename) throws ZipException, UnsupportedEncodingException {
        ZipFile zipFile = new ZipFile(filename);
        zipFile.setFileNameCharset("EUC-KR");
        List<FileHeader> headers = zipFile.getFileHeaders();
        for(FileHeader header : headers) {
            String name = header.getFileName();

//            UniversalDetector detector = new UniversalDetector(null);
//            detector.handleData(name.getBytes(), 0, name.length());
//            detector.dataEnd();
//            String encoding = detector.getDetectedCharset();
//
//            Log.d("ZipLoader", "filename="+name +" encoding="+encoding);
        }

        // 이미 풀어놓은게 없으면 AsyncTask로 로딩함
        // 첫번째 이미지 파일이 로딩이 끝나면 바로 띄운다
    }

    public static void checkCachedPath(String filename) {

    }
}
