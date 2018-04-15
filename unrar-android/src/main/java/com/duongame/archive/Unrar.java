package com.duongame.archive;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2018-01-20.
 */

public class Unrar {
    static {
        System.loadLibrary("unrar");
    }

    public interface UnrarCallback {
        void process(String filename, int percent);
    }

    private int id;

    public Unrar(String rarPath) {
        id = init(rarPath);
    }

    public int getCount() {
        return getCount(id);
    }

    public ArrayList<UnrarHeader> getHeaders() {
        return getHeaders(id);
    }

    public boolean extractFile(String fileName, String destPath, UnrarCallback callback) {
        return extractFile(id, fileName, destPath, callback);
    }

    public boolean extractAll(String destPath, UnrarCallback callback) {
        return extractAll(id, destPath, callback);
    }

    //region native
    // id를 획득하는 부분
    private native int init(String rarPath);

    private native int getCount(int id);

    // 이하 id를 통해서 작업하는 부분

    // 이거 두개는 일단 나중에
    // 수동으로 열었다 닫았다 하자
//    public native boolean open(int id);
//    public native void close(int id);

    // 전체 파일 리스트를 받기
    private native ArrayList<UnrarHeader> getHeaders(int id);

    // 아래 2개는 callback을 등록해야 한다.
    // 개별 파일 압축 풀기
    private native boolean extractFile(int id, String fileName, String destPath, UnrarCallback callback);

    // 전체 파일 압축 풀기
    private native boolean extractAll(int id, String destPath, UnrarCallback callback);
    //endregion native
}
