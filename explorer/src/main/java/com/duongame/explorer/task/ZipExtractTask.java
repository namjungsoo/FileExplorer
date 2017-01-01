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

    private final Object mPauseWorkLock = new Object();
    protected boolean mPauseWork = false;

    public ZipExtractTask(ZipFile zipFile, ArrayList<ExplorerItem> imageList, ZipLoader.ZipLoaderListener listener) {
        this.zipFile = zipFile;
        this.imageList = imageList;
        this.listener = listener;
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

    public void setZipImageList(ArrayList<ExplorerItem> zipImageList) {
        this.zipImageList = zipImageList;
    }

    public void setSide(ExplorerItem.Side side) {
//        synchronized (side) {
        this.side = side;
//        }
    }

    @Override
    protected Void doInBackground(String... params) {
//        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        final String path = params[0];
        int i = 0;

        try {
            for (i = 0; i < imageList.size(); i++) {
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

                final BitmapFactory.Options options = BitmapLoader.decodeBounds(item.path);

                // 나중에 페이지 전환을 위해서 넣어둔다.
                item.width = options.outWidth;
                item.height = options.outHeight;

                final int size = zipImageList.size();
                if (options.outWidth > options.outHeight) {// 잘라야 한다. 가로 파일이다.
//                    synchronized (side) {
                    if (side == LEFT) {
                        // 한국식은 right를 먼저 넣는다.
                        final ExplorerItem left = (ExplorerItem) item.clone();
                        left.side = LEFT;
                        left.index = size;
                        zipImageList.add(left);

                        final ExplorerItem right = (ExplorerItem) item.clone();
                        right.side = RIGHT;
                        right.index = size + 1;
                        zipImageList.add(right);

                    } else if (side == RIGHT) {
                        // 일본식은 right를 먼저 넣는다.
                        final ExplorerItem right = (ExplorerItem) item.clone();
                        right.side = RIGHT;
                        right.index = size;
                        zipImageList.add(right);

                        final ExplorerItem left = (ExplorerItem) item.clone();
                        left.side = LEFT;
                        left.index = size + 1;
                        zipImageList.add(left);
                    } else {// 전체보기
                        final ExplorerItem newItem = (ExplorerItem) item.clone();
                        newItem.index = size;
                        zipImageList.add(newItem);
                    }
//                    }
                } else {
                    final ExplorerItem newItem = (ExplorerItem) item.clone();
                    newItem.index = size;
                    zipImageList.add(item);
                }

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
            listener.onSuccess(i, zipImageList);
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
