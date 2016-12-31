package com.duongame.explorer.task;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.duongame.explorer.adapter.ExplorerFileItem;
import com.duongame.explorer.bitmap.BitmapLoader;
import com.duongame.explorer.bitmap.ZipLoader;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.util.ArrayList;

import static com.duongame.explorer.adapter.ExplorerFileItem.Side.LEFT;
import static com.duongame.explorer.adapter.ExplorerFileItem.Side.RIGHT;

/**
 * Created by namjungsoo on 2016-12-16.
 */

public class ZipExtractTask extends AsyncTask<String, Integer, Void> {
    private ArrayList<ExplorerFileItem> imageList;
    private ArrayList<ExplorerFileItem> zipImageList;
    private ZipFile zipFile;
    private ZipLoader.ZipLoaderListener listener;
    private ExplorerFileItem.Side firstSide = LEFT;

    private final Object mPauseWorkLock = new Object();
    protected boolean mPauseWork = false;

    public ZipExtractTask(ZipFile zipFile, ArrayList<ExplorerFileItem> imageList, ZipLoader.ZipLoaderListener listener) {
        this.zipFile = zipFile;
        this.imageList = imageList;
        this.listener = listener;
        zipImageList = new ArrayList<ExplorerFileItem>();
    }

    public void setPauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
    }

    public void setFirstSide(ExplorerFileItem.Side side) {
//        synchronized (firstSide) {
            this.firstSide = side;
//        }
    }

    @Override
    protected Void doInBackground(String... params) {
//        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        final String path = params[0];
        int i = 0;

        try {
            for (i = 0; i < imageList.size(); i++) {
                final ExplorerFileItem item = imageList.get(i);

                if(isCancelled())
                    break;

                synchronized (mPauseWorkLock) {
                    while (mPauseWork && !isCancelled()) {
                        try {
                            mPauseWorkLock.wait();
                        } catch (InterruptedException e) {}
                    }
                }

                zipFile.extractFile(item.name, path);

                BitmapFactory.Options options = BitmapLoader.decodeBounds(item.path);

                // 나중에 페이지 전환을 위해서 넣어둔다.
                item.width = options.outWidth;
                item.height = options.outHeight;

                if (options.outWidth > options.outHeight) {// 잘라야 한다. 가로 파일이다.
//                    synchronized (firstSide) {
                        if(firstSide == LEFT) {
                            // 한국식은 right를 먼저 넣는다.
                            ExplorerFileItem left = (ExplorerFileItem) item.clone();
                            left.side = LEFT;
                            zipImageList.add(left);

                            ExplorerFileItem right = (ExplorerFileItem) item.clone();
                            right.side = RIGHT;
                            zipImageList.add(right);

                        } else if(firstSide == RIGHT) {
                            // 일본식은 right를 먼저 넣는다.
                            ExplorerFileItem right = (ExplorerFileItem) item.clone();
                            right.side = RIGHT;
                            zipImageList.add(right);

                            ExplorerFileItem left = (ExplorerFileItem) item.clone();
                            left.side = LEFT;
                            zipImageList.add(left);
                        } else {// 전체보기
                            zipImageList.add(item);
                        }
//                    }
                } else {
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
