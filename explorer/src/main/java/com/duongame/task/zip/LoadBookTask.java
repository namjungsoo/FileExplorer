package com.duongame.task.zip;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.duongame.adapter.ExplorerItem;
import com.duongame.archive.ArchiveLoader;
import com.duongame.archive.IArchiveFile;
import com.duongame.bitmap.BitmapLoader;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2018-01-23.
 */

public class LoadBookTask extends AsyncTask<String, Integer, Void> {
    private ArrayList<ExplorerItem> imageList;// zip안의 이미지 파일의 갯수
    private ArrayList<ExplorerItem> zipImageList;// 잘려진 zip 파일의 이미지 갯수. 파일갯수와 다름

    private IArchiveFile zipFile;
    private ArchiveLoader.ArchiveLoaderListener listener;

    private int side = ExplorerItem.SIDE_LEFT;
    private int extract;// 압축 풀린 이미지 파일의 갯수

    private final Object mPauseWorkLock = new Object();
    private boolean mPauseWork = false;

    public LoadBookTask(IArchiveFile zipFile, ArrayList<ExplorerItem> imageList, ArchiveLoader.ArchiveLoaderListener listener, int extract, ArrayList<ExplorerItem> zipImageList) {
        this.zipFile = zipFile;
        this.imageList = imageList;
        this.listener = listener;
        this.extract = extract;

        if (zipImageList != null) {
            this.zipImageList = zipImageList;
        } else {
            this.zipImageList = new ArrayList<>();
        }
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

    public void setSide(int side) {
        this.side = side;
    }

    public static void processItem(int orgIndex, ExplorerItem item, int side, ArrayList<ExplorerItem> imageList) {
        final BitmapFactory.Options options = BitmapLoader.decodeBounds(item.path);

        // 나중에 페이지 전환을 위해서 넣어둔다.
        item.width = options.outWidth;
        item.height = options.outHeight;

        final int size = imageList.size();
        if (options.outWidth > options.outHeight) {// 잘라야 한다. 가로 파일이다.
            if (side == ExplorerItem.SIDE_LEFT) {
                // 한국식은 right를 먼저 넣는다.
                final ExplorerItem left = (ExplorerItem) item.clone();
                left.side = ExplorerItem.SIDE_LEFT;
                left.index = size;
                left.orgIndex = orgIndex;
                imageList.add(left);

                final ExplorerItem right = (ExplorerItem) item.clone();
                right.side = ExplorerItem.SIDE_RIGHT;
                right.index = size + 1;
                right.orgIndex = orgIndex;
                imageList.add(right);
            } else if (side == ExplorerItem.SIDE_RIGHT) {
                // 일본식은 right를 먼저 넣는다.
                final ExplorerItem right = (ExplorerItem) item.clone();
                right.side = ExplorerItem.SIDE_RIGHT;
                right.index = size;
                right.orgIndex = orgIndex;
                imageList.add(right);

                final ExplorerItem left = (ExplorerItem) item.clone();
                left.side = ExplorerItem.SIDE_LEFT;
                left.index = size + 1;
                left.orgIndex = orgIndex;
                imageList.add(left);
            } else {// 전체보기
                final ExplorerItem newItem = (ExplorerItem) item.clone();
                newItem.index = size;
                newItem.orgIndex = orgIndex;
                imageList.add(newItem);
            }
        } else {
            final ExplorerItem newItem = (ExplorerItem) item.clone();
            newItem.index = size;
            newItem.orgIndex = orgIndex;
            imageList.add(newItem);
        }
    }

    @Override
    protected Void doInBackground(String... params) {
        final String path = params[0];
        int i = 0;

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

            if (zipFile.extractFile(item.name, path)) {
                processItem(i, item, side, zipImageList);

                publishProgress(i);
            } else {
                if (listener != null) {
                    listener.onFail(i, imageList.get(i).name);
                }
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
        super.onPostExecute(result);
        if (listener != null) {
            listener.onFinish(zipImageList, imageList.size());
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
