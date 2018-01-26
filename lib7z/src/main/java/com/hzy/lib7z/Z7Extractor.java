package com.hzy.lib7z;

import java.util.ArrayList;

/**
 * Created by huzongyao on 17-11-24.
 */

public class Z7Extractor {
    public static final long DEFAULT_IN_BUF_SIZE = 0x200000;

    private int id;

    public Z7Extractor(String z7Path) {
        id = init(z7Path);
    }

    public static String getLzmaVersion() {
        return nGetLzmaVersion();
    }

    public ArrayList<Z7Header> getHeaders() {
        return getHeaders(id, DEFAULT_IN_BUF_SIZE);
    }

    public boolean extractFile(String targetName, String outPath, ExtractCallback callback) {
        return extractFile(id, targetName, outPath, callback, DEFAULT_IN_BUF_SIZE);
    }

    public boolean extractAll(String outPath, ExtractCallback callback) {
        return extractAll(id, outPath, callback, DEFAULT_IN_BUF_SIZE);
    }

    /*
    public static boolean extractFile(String filePath, String targetName, String outPath,
                                      ExtractCallback callback) {
        callback = callback == null ? ExtractCallback.EMPTY_CALLBACK : callback;
        File inputFile = new File(filePath);
        if (TextUtils.isEmpty(filePath) || !inputFile.exists() ||
                TextUtils.isEmpty(outPath) || !prepareOutPath(outPath)) {
            callback.onError(ErrorCode.ERROR_CODE_PATH_ERROR, "File Path Error!");
            return false;
        }
        return nExtractFile(filePath, targetName, outPath, callback, DEFAULT_IN_BUF_SIZE);
    }

    public static boolean extractAll(String filePath, String outPath, ExtractCallback callback) {
        callback = callback == null ? ExtractCallback.EMPTY_CALLBACK : callback;
        File inputFile = new File(filePath);
        if (TextUtils.isEmpty(filePath) || !inputFile.exists() ||
                TextUtils.isEmpty(outPath) || !prepareOutPath(outPath)) {
            callback.onError(ErrorCode.ERROR_CODE_PATH_ERROR, "File Path Error!");
            return false;
        }
        return nExtractAll(filePath, outPath, callback, DEFAULT_IN_BUF_SIZE);
    }

    public static boolean extractAsset(AssetManager assetManager, String fileName,
                                       String outPath, ExtractCallback callback) {
        callback = callback == null ? ExtractCallback.EMPTY_CALLBACK : callback;
        if (TextUtils.isEmpty(fileName) || TextUtils.isEmpty(outPath) || !prepareOutPath(outPath)) {
            callback.onError(ErrorCode.ERROR_CODE_PATH_ERROR, "File Path Error!");
            return false;
        }
        return nExtractAsset(assetManager, fileName, outPath, callback, DEFAULT_IN_BUF_SIZE);
    }

    public static ArrayList<Z7Header> getHeaders(String filePath) {
        return getHeaders(filePath, DEFAULT_IN_BUF_SIZE);
    }

    private static boolean prepareOutPath(String outPath) {
        File outDir = new File(outPath);
        if (!outDir.exists()) {
            if (outDir.mkdirs())
                return true;
        }
        return outDir.exists() && outDir.isDirectory();
    }

    private static native ArrayList<Z7Header> getHeaders(String filePath, long inBufSize);

    private static native boolean nExtractAll(String filePath, String outPath,
                                              ExtractCallback callback, long inBufSize);

    private static native boolean nExtractFile(String filePath, String targetName, String outPath,
                                               ExtractCallback callback, long inBufSize);

    private static native boolean nExtractAsset(AssetManager assetManager,
                                                String fileName, String outPath,
                                                ExtractCallback callback, long inBufSize);

    */


    static {
        System.loadLibrary("7z");
    }

    //region native
    private native int init(String rarPath);
    private native ArrayList<Z7Header> getHeaders(int id, long inBufSize);
    private native boolean extractAll(int id, String outPath, ExtractCallback callback, long inBufSize);
    private native boolean extractFile(int id, String targetName, String outPath, ExtractCallback callback, long inBufSize);
    private static native String nGetLzmaVersion();
    //endregion native
}