package com.duongame.explorer.bitmap;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

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
    private static final String TAG="ZipLoader";
    public interface ZipLoaderListener {
        void onSuccess(int i);
        void onFail();
    }

    private static class ZipExtractTask extends AsyncTask<String, Integer, Void> {
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

            try {
                for (int i = 0; i < imageList.size(); i++) {
                    zipFile.extractFile(imageList.get(i).name, path);
                    publishProgress(i);
                }
            } catch (ZipException e) {
                e.printStackTrace();
                if(listener != null) {
                    listener.onFail();
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            if(listener != null) {
                listener.onSuccess(progress[0]);
            }
        }
    }

    public static String getFirstImage(Context context, String filename) throws ZipException {
        checkCachedPath(context, filename);

        final ZipFile zipFile = new ZipFile(filename);
        zipFile.setFileNameCharset("EUC-KR");// 일단 무조건 한국 사용자를 위해서 이렇게 설정함
        zipFile.setRunInThread(true);

        final ArrayList<ExplorerFileItem> imageList = new ArrayList<ExplorerFileItem>();
        final List<FileHeader> headers = zipFile.getFileHeaders();

        final String path = FileHelper.getZipCachePath(context, filename);

        for (FileHeader header : headers) {
            String name = header.getFileName();
            if (FileHelper.isImage(name)) {
                imageList.add(new ExplorerFileItem(path, name, "", 0, ExplorerFileItem.FileType.IMAGE));
            }
        }

        Collections.sort(imageList, new FileHelper.FileNameCompare());

        if(imageList.size() > 0) {
            // 파일을 풀어놓고 리턴한다
            final File file = new File(imageList.get(0).path);
            if(file != null) {
                if(file.exists())
                    return imageList.get(0).path;
            }
            Log.d(TAG, "name=" +imageList.get(0).name + " path="+path);
            zipFile.extractFile(imageList.get(0).name, path);
            return imageList.get(0).path;
        }
        return null;
    }

    public static ArrayList<ExplorerFileItem> load(Context context, String filename, ZipLoaderListener listener) throws ZipException {
        // 일단 무조건 압축 풀자
        checkCachedPath(context, filename);

        ZipFile zipFile = new ZipFile(filename);
        zipFile.setFileNameCharset("EUC-KR");// 일단 무조건 한국 사용자를 위해서 이렇게 설정함
//        zipFile.setRunInThread(true);

        ArrayList<ExplorerFileItem> imageList = new ArrayList<ExplorerFileItem>();
        List<FileHeader> headers = zipFile.getFileHeaders();

        String path = FileHelper.getZipCachePath(context, filename);

        for (FileHeader header : headers) {
            String name = header.getFileName();
            if (FileHelper.isImage(name)) {
                imageList.add(new ExplorerFileItem(path, name, "", 0, ExplorerFileItem.FileType.IMAGE));
            }
        }

        Collections.sort(imageList, new FileHelper.FileNameCompare());

        // 이미 풀어놓은게 없으면 AsyncTask로 로딩함
        // 첫번째 이미지 파일이 로딩이 끝나면 바로 띄운다
        ZipExtractTask task = new ZipExtractTask(zipFile, imageList, listener);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);

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
