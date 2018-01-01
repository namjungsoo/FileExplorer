package com.duongame.bitmap;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.duongame.adapter.ExplorerItem;
import com.duongame.helper.FileHelper;
import com.duongame.task.zip.UnzipBookTask;

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

    private String zipEncoding = "EUC-KR";
    private UnzipBookTask task;
    private ZipFile zipFile;
    private ZipLoaderListener listener;
    private List<FileHeader> zipHeaders;

    private String extractPath;
    private ExplorerItem.Side side = ExplorerItem.Side.LEFT;
    private int extract;

    public interface ZipLoaderListener {
        void onSuccess(int i, ArrayList<ExplorerItem> zipImageList, int totalFileCount);

        void onFail(int i, String name);

        void onFinish(ArrayList<ExplorerItem> zipImageList, int totalFileCount);
    }

    public void cancelTask() {
        if (task != null) {
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
            task.setPauseWork(true);
        }
    }

    public void resume() {
        if (task != null && !task.isCancelled()) {
            task.setPauseWork(false);
        }
    }

//    public void copy(File src, File dst) throws IOException {
//        FileInputStream inStream = new FileInputStream(src);
//        FileOutputStream outStream = new FileOutputStream(dst);
//        FileChannel inChannel = inStream.getChannel();
//        FileChannel outChannel = outStream.getChannel();
//        inChannel.transferTo(0, inChannel.size(), outChannel);
//        inStream.close();
//        outStream.close();
//    }

    private void filterImageList(ArrayList<ExplorerItem> imageList) {
        for (FileHeader header : zipHeaders) {
            final String name = header.getFileName();
            if (FileHelper.isImage(name)) {
                imageList.add(new ExplorerItem(FileHelper.getFullPath(extractPath, name), name, "", 0, ExplorerItem.FileType.IMAGE));
            }
        }
        Collections.sort(imageList, new FileHelper.NameAscComparator());
    }

    private ArrayList<ExplorerItem> loadFirstImageOnly(ArrayList<ExplorerItem> imageList) {
        if (imageList.size() > 0) {
            // 이미지 로딩후 확인해보고 좌우를 나눠야 되면 나누어 주자
            // 파일명으로 실제 폴더 안에 파일이 있는지 검사
            if (!(new File(imageList.get(0).name).exists())) {
                try {
                    zipFile.extractFile(imageList.get(0).name, extractPath);
                } catch (ZipException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            final BitmapFactory.Options options = BitmapLoader.decodeBounds(imageList.get(0).path);

            // 일본식(RIGHT)를 기준으로 잡자
            if (options.outWidth > options.outHeight) {
                imageList.get(0).side = side;
            }
            return imageList;
        }
        return null;
    }

    private ArrayList<ExplorerItem> loadNew(ArrayList<ExplorerItem> imageList, ArrayList<ExplorerItem> firstList) {
        final ExplorerItem item = (ExplorerItem) imageList.get(0).clone();
        final BitmapFactory.Options options = BitmapLoader.decodeBounds(item.path);

        // 일본식(RIGHT)를 기준으로 잡자
        if (options.outWidth > options.outHeight) {
            item.side = side;
        }

        firstList.add(item);

        task = new UnzipBookTask(zipFile, imageList, listener, extract, null);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, extractPath);

        return firstList;
    }


    private ArrayList<ExplorerItem> loadContinue(ArrayList<ExplorerItem> imageList, ArrayList<ExplorerItem> firstList) {
        // extract 미만의 파일들은 파일이 존재한다고 가정하고 시작한 것이다.
        if(extract > imageList.size()) // 에러인 경우이다.
            extract = imageList.size();

        for (int i = 0; i < extract; i++) {
            // 여기서 파일의 존재여부에 대한 검증을 해야 한다.
            try {
                final ExplorerItem item = (ExplorerItem) imageList.get(i);
                if(isFileExtracted(item.path, zipHeaders)) {
                    UnzipBookTask.processItem(i, item, side, firstList);
                } else {
                    // 에러이므로 여기서 중단한다.
                    extract = i;
                    break;
                }
            } catch (IndexOutOfBoundsException e) {
                return firstList;
            }
        }

        // 읽어보고 나머지 리스트에 대해서 추가해줌
        task = new UnzipBookTask(zipFile, imageList, listener, extract, (ArrayList<ExplorerItem>) firstList.clone());
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, extractPath);
        return firstList;
    }

    private boolean isFileExtracted(String path, List<FileHeader> headers) {
        File file = new File(path);
        if(!file.exists())
            return false;

        for(int i=0; i<headers.size(); i++) {
            if(file.getName().equals(headers.get(i).getFileName())) {
                if(file.length() == headers.get(i).getUncompressedSize())
                    return true;
            }
        }
        return false;
    }

    // 리턴값은 이미지 리스트이다.
    // 압축을 풀지 않으면 정보를 알수가 없다. 좌우 잘라야 되는지 마는지를
    public ArrayList<ExplorerItem> load(Context context, String filename, ZipLoaderListener listener, int extract, ExplorerItem.Side side, boolean firstImageOnly) throws ZipException {
        // 일단 무조건 압축 풀자
        //TODO: 이미 전체 압축이 풀려있는지 검사해야함
        makeCachePath(context, filename);

        this.side = side;
        this.listener = listener;
        this.extract = extract;

        zipFile = new ZipFile(filename);
        zipFile.setFileNameCharset(zipEncoding);// 일단 무조건 한국 사용자를 위해서 이렇게 설정함
        zipHeaders = zipFile.getFileHeaders();
        extractPath = FileHelper.getZipCachePath(context, filename);// 파일이 풀릴 예상 경로

        final ArrayList<ExplorerItem> imageList = new ArrayList<ExplorerItem>();
        filterImageList(imageList);// 이미지 파일만 추출함

        // 처음 이미지만 풀 경우에는 처음 이미지 파일 한개만 풀고 끝낸다.
        if (firstImageOnly) {
            return loadFirstImageOnly(imageList);
        } else {
            if (imageList.size() > 0) {

                // 아무것도 안들어 있는 리스트
                // firstList의 존재이유는 동기적으로 이미지를 읽어서 액티비티에 빠르게 보여주게 하기 위함
                final ArrayList<ExplorerItem> firstList = new ArrayList<>();

                // 이미지 파일이 있으면 첫번째 페이지만 추가해줌
                // 왜 이렇게 했냐면 새로 읽었을때 빠르게 추가해 주기 위해서
                // 안해도 무방하다
                if (extract == 0) {
                    return loadNew(imageList, firstList);
                } else {
                    return loadContinue(imageList, firstList);
                }
            }
        }
        return imageList;
    }

    public static boolean makeCachePath(Context context, String filename) {
        final File cacheFile = FileHelper.getZipCacheFile(context, filename);
        boolean ret = cacheFile.exists();

        // 폴더가 없으면 만들어 준다.
        if (!ret) {
            cacheFile.mkdirs();
        }
        return ret;
    }
}
