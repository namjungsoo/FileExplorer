package com.duongame.explorer.bitmap;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.helper.FileHelper;
import com.duongame.explorer.task.ZipExtractTask;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
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
        void onSuccess(int i, ArrayList<ExplorerItem> zipImageList, int totalFileCount);

        void onFail(int i, String name);

        void onFinish(ArrayList<ExplorerItem> zipImageList, int totalFileCount);
    }

    public void cancelTask() {
        Log.d(TAG, "cancelTask");
        if (task != null) {
            Log.d(TAG, "cancelTask OK");
            task.cancel(true);
        }
    }

    public void setZipImageList(ArrayList<ExplorerItem> zipImageList) {
        if (task != null && !task.isCancelled()) {
            task.setZipImageList(zipImageList);
        }
    }

    public void setSide(ExplorerItem.Side side) {
        if (task != null && !task.isCancelled()) {
            task.setSide(side);
        }
    }

    public void pause() {
        if (task != null && !task.isCancelled()) {
            Log.d(TAG, "pause");
            task.setPauseWork(true);
            Log.d(TAG, "pause OK");
        }
    }

    public void resume() {
        Log.d(TAG, "resume");
        if (task != null && !task.isCancelled()) {
            task.setPauseWork(false);
            Log.d(TAG, "resume OK");
        }
    }

    public void copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

    // 리턴값은 이미지 리스트이다.
    // 압축을 풀지 않으면 정보를 알수가 없다. 좌우 잘라야 되는지 마는지를
    public ArrayList<ExplorerItem> load(Context context, String filename, ZipLoaderListener listener, int extract, ExplorerItem.Side side, boolean firstImageOnly) throws ZipException {
        // 일단 무조건 압축 풀자
        //TODO: 이미 전체 압축이 풀려있는지 검사해야함
        checkCachedPath(context, filename);

        final ZipFile zipFile = new ZipFile(filename);
        zipFile.setFileNameCharset("EUC-KR");// 일단 무조건 한국 사용자를 위해서 이렇게 설정함

        final ArrayList<ExplorerItem> imageList = new ArrayList<ExplorerItem>();
        final List<FileHeader> zipHeaders = zipFile.getFileHeaders();

        // 파일이 풀릴 예상 경로
        final String extractPath = FileHelper.getZipCachePath(context, filename);

        for (FileHeader header : zipHeaders) {
            final String name = header.getFileName();
            if (FileHelper.isImage(name)) {
                imageList.add(new ExplorerItem(FileHelper.getFullPath(extractPath, name), name, "", 0, ExplorerItem.FileType.IMAGE));
            }
        }

        Collections.sort(imageList, new FileHelper.FileNameAscendingCompare());

        if (firstImageOnly) {
            if (imageList.size() > 0) {
                // 이미지 로딩후 확인해보고 좌우를 나눠야 되면 나누어 주자
                zipFile.extractFile(imageList.get(0).name, extractPath);
                final BitmapFactory.Options options = BitmapLoader.decodeBounds(imageList.get(0).path);

                //DEBUG
//                File file = new File(imageList.get(0).path);
//                File dstFile = new File(Environment.getExternalStorageDirectory() + "/" + imageList.get(0).name);
//                try {
//                    copy(file, dstFile);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

                // 일본식(RIGHT)를 기준으로 잡자
                if (options.outWidth > options.outHeight) {
                    imageList.get(0).side = ExplorerItem.Side.LEFT;
                }
                return imageList;
            }
        } else {
            if (imageList.size() > 0) {
                final ArrayList<ExplorerItem> firstList = new ArrayList<>();

                // 이미지 파일이 있으면 첫번째 페이지만 추가해줌
                if (extract == 0) {
                    final ExplorerItem item = (ExplorerItem) imageList.get(0).clone();
                    final BitmapFactory.Options options = BitmapLoader.decodeBounds(item.path);

                    // 일본식(RIGHT)를 기준으로 잡자
                    if (options.outWidth > options.outHeight) {
                        item.side = ExplorerItem.Side.LEFT;
                    }

                    firstList.add(item);

                    task = new ZipExtractTask(zipFile, imageList, listener, extract, null);
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, extractPath);

                    return firstList;
                } else {
                    Log.i(TAG, "extract_file=" + extract);
                    // 동기적으로 이미 압축 풀린 놈들을 가져오자
                    for (int i = 0; i < extract; i++) {
                        final ExplorerItem item = (ExplorerItem) imageList.get(i);
                        ZipExtractTask.processItem(i, item, side, firstList);
                    }

                    Log.i(TAG, "firstList.size=" + firstList.size());
                    for(int i=0; i<firstList.size(); i++) {
                        Log.i(TAG, i+ ": " + firstList.get(i).path + " " + firstList.get(i).side);
                    }

                    if (extract == imageList.size()) {
                        Log.i(TAG, "extract_file == imageList.size()");
                    }

                    task = new ZipExtractTask(zipFile, imageList, listener, extract, (ArrayList<ExplorerItem>)firstList.clone());
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, extractPath);
                    return firstList;
                }
            }
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
