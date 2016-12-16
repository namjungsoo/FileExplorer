package com.duongame.explorer.bitmap;

import android.content.Context;
import android.os.AsyncTask;

import com.duongame.explorer.adapter.ExplorerFileItem;
import com.duongame.explorer.helper.FileHelper;
import com.duongame.explorer.task.ZipExtractTask;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by namjungsoo on 2016-11-19.
 */

public class ZipLoader {
    private static final String TAG = "ZipLoader";
    private ZipExtractTask task;

    public interface ZipLoaderListener {
        void onSuccess(int i, String name);

        void onFail(int i, String name);

        void onFinish(ArrayList<ExplorerFileItem> zipImageList);
    }

    // 리턴값은 이미지 리스트이다.
    // 압축을 풀지 않으면 정보를 알수가 없다. 좌우 잘라야 되는지 마는지를
    public ArrayList<ExplorerFileItem> load(Context context, String filename, ZipLoaderListener listener, boolean firstImageOnly) throws ZipException {
        // 일단 무조건 압축 풀자
        //TODO: 이미 전체 압축이 풀려있는지 검사해야함
        checkCachedPath(context, filename);

        final ZipFile zipFile = new ZipFile(filename);
        zipFile.setFileNameCharset("EUC-KR");// 일단 무조건 한국 사용자를 위해서 이렇게 설정함

        final ArrayList<ExplorerFileItem> imageList = new ArrayList<ExplorerFileItem>();
        final List<FileHeader> zipHeaders = zipFile.getFileHeaders();

        // 파일이 풀릴 예상 경로
        final String extractPath = FileHelper.getZipCachePath(context, filename);

        for (FileHeader header : zipHeaders) {
            final String name = header.getFileName();
            if (FileHelper.isImage(name)) {
                imageList.add(new ExplorerFileItem(extractPath, name, "", 0, ExplorerFileItem.FileType.IMAGE));
            }
        }

        Collections.sort(imageList, new FileHelper.FileNameCompare());

        if (firstImageOnly) {
            if (imageList.size() > 0)
                zipFile.extractFile(imageList.get(0).name, extractPath);
        } else {
            // 이미 풀어놓은게 없으면 AsyncTask로 로딩함
            // 첫번째 이미지 파일이 로딩이 끝나면 바로 띄운다
            task = new ZipExtractTask(zipFile, imageList, listener);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, extractPath);
        }
        return imageList;
    }

    public static boolean checkCachedPath(Context context, String filename) {
        final File cacheFile = FileHelper.getZipCacheFile(context, filename);
        boolean ret = cacheFile.exists();
        if (!ret) {
            cacheFile.mkdirs();
        }
        return ret;
    }
}