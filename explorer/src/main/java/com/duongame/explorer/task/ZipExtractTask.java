package com.duongame.explorer.task;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.bitmap.BitmapLoader;
import com.duongame.explorer.bitmap.ZipLoader;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.util.ArrayList;

import static com.duongame.explorer.adapter.ExplorerItem.Side.LEFT;
import static com.duongame.explorer.adapter.ExplorerItem.Side.RIGHT;

/**
 * Created by namjungsoo on 2016-12-16.
 */

public class ZipExtractTask extends AsyncTask<String, Integer, Void> {
    private ArrayList<ExplorerItem> imageList;
    private ArrayList<ExplorerItem> zipImageList;
    private ZipFile zipFile;
    private ZipLoader.ZipLoaderListener listener;
    private ExplorerItem.Side side = LEFT;
    private int extract;

    private final Object mPauseWorkLock = new Object();
    protected boolean mPauseWork = false;

    public ZipExtractTask(ZipFile zipFile, ArrayList<ExplorerItem> imageList, ZipLoader.ZipLoaderListener listener, int extract) {
        this.zipFile = zipFile;
        this.imageList = imageList;
        this.listener = listener;
        this.extract = extract;
        zipImageList = new ArrayList<ExplorerItem>();
    }

    public void setPauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
    }

    // image side가 변경되었을때 앞에 좌우가 변경된 것을 넘겨주기 위함
    public void setZipImageList(ArrayList<ExplorerItem> zipImageList) {
        this.zipImageList = zipImageList;
    }

    public void setSide(ExplorerItem.Side side) {
//        synchronized (side) {
        this.side = side;
//        }
    }

    public static void processItem(ExplorerItem item, ExplorerItem.Side side, ArrayList<ExplorerItem> imageList) {
        final BitmapFactory.Options options = BitmapLoader.decodeBounds(item.path);

        // 나중에 페이지 전환을 위해서 넣어둔다.
        item.width = options.outWidth;
        item.height = options.outHeight;

        final int size = imageList.size();
        if (options.outWidth > options.outHeight) {// 잘라야 한다. 가로 파일이다.
//                    synchronized (side) {
            if (side == LEFT) {
                // 한국식은 right를 먼저 넣는다.
                final ExplorerItem left = (ExplorerItem) item.clone();
                left.side = LEFT;
                left.index = size;
                imageList.add(left);

                final ExplorerItem right = (ExplorerItem) item.clone();
                right.side = RIGHT;
                right.index = size + 1;
                imageList.add(right);

            } else if (side == RIGHT) {
                // 일본식은 right를 먼저 넣는다.
                final ExplorerItem right = (ExplorerItem) item.clone();
                right.side = RIGHT;
                right.index = size;
                imageList.add(right);

                final ExplorerItem left = (ExplorerItem) item.clone();
                left.side = LEFT;
                left.index = size + 1;
                imageList.add(left);
            } else {// 전체보기
                final ExplorerItem newItem = (ExplorerItem) item.clone();
                newItem.index = size;
                imageList.add(newItem);
            }
//                    }
        } else {
            final ExplorerItem newItem = (ExplorerItem) item.clone();
            newItem.index = size;
            imageList.add(item);
        }

    }

    @Override
    protected Void doInBackground(String... params) {
//        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        final String path = params[0];
        int i = 0;

        try {
            // 압축 풀려있는 파일의 처리
//            for (i = 0; i < extract; i++) {
//                final ExplorerItem item = imageList.get(i);
//                if (isCancelled())
//                    break;
//
//                processItem(item);
//            }
//            publishProgress(i);

            for (i = extract; i < imageList.size(); i++) {
                final ExplorerItem item = imageList.get(i);

                if (isCancelled())
                    break;

                synchronized (mPauseWorkLock) {
                    while (mPauseWork && !isCancelled()) {
                        try {
                            mPauseWorkLock.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }

                zipFile.extractFile(item.name, path);

                processItem(item, side, zipImageList);

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
            listener.onSuccess(i, zipImageList, imageList.size());
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        if (listener != null) {
            listener.onFinish(zipImageList);
        }
    }

    @Override
    protected void onCancelled(Void value) {
        super.onCancelled(value);
        synchronized (mPauseWorkLock) {
            mPauseWorkLock.notifyAll();
        }
    }

}
