package com.duongame.explorer.bitmap;

import android.content.Context;
import android.os.AsyncTask;

import com.duongame.explorer.adapter.ExplorerFileItem;
import com.duongame.explorer.helper.FileHelper;

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
    }

    private class ZipExtractTask extends AsyncTask<String, Integer, Void> {
        private ArrayList<ExplorerFileItem> imageList;
        private ZipFile zipFile;
        private ZipLoaderListener listener;

        public ZipExtractTask(ZipFile zipFile, ArrayList<ExplorerFileItem> imageList, ZipLoaderListener listener) {
            this.zipFile = zipFile;
            this.imageList = imageList;
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(String... params) {
            String path = params[0];
            int i = 0;

            try {
                for (i = 0; i < imageList.size(); i++) {
                    zipFile.extractFile(imageList.get(i).name, path);
                    publishProgress(i);
                }
            } catch (ZipException e) {
                e.printStackTrace();
                if (listener != null) {
                    listener.onFail(i, imageList.get(i).name);
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            if (listener != null) {
                int i = progress[0];
                listener.onSuccess(i, imageList.get(i).name);
            }
        }
    }

    public ArrayList<ExplorerFileItem> load(Context context, String filename, ZipLoaderListener listener, boolean firstImageOnly) throws ZipException {
        // 일단 무조건 압축 풀자
        //TODO: 이미 전체 압축이 풀려있는지 검사해야함
        checkCachedPath(context, filename);

        final ZipFile zipFile = new ZipFile(filename);
        zipFile.setFileNameCharset("EUC-KR");// 일단 무조건 한국 사용자를 위해서 이렇게 설정함

        final ArrayList<ExplorerFileItem> imageList = new ArrayList<ExplorerFileItem>();
        final List<FileHeader> zipHeaders = zipFile.getFileHeaders();
        final String extractPath = FileHelper.getZipCachePath(context, filename);

        for (FileHeader header : zipHeaders) {
            final String name = header.getFileName();
            if (FileHelper.isImage(name)) {
                imageList.add(new ExplorerFileItem(extractPath, name, "", 0, ExplorerFileItem.FileType.IMAGE));
            }
        }

        Collections.sort(imageList, new FileHelper.FileNameCompare());

        if(firstImageOnly) {
            if(imageList.size() > 0)
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
