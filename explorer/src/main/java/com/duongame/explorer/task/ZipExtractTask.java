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

    public ZipExtractTask(ZipFile zipFile, ArrayList<ExplorerFileItem> imageList, ZipLoader.ZipLoaderListener listener) {
        this.zipFile = zipFile;
        this.imageList = imageList;
        this.listener = listener;
        zipImageList = new ArrayList<ExplorerFileItem>();
    }

    @Override
    protected Void doInBackground(String... params) {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        final String path = params[0];
        int i = 0;

        try {
            for (i = 0; i < imageList.size(); i++) {
                final ExplorerFileItem item = imageList.get(i);
                zipFile.extractFile(item.name, path);
                publishProgress(i);

                BitmapFactory.Options options = BitmapLoader.decodeBounds(item.path);
                if (options.outWidth > options.outHeight) {// 잘라야 한다. 가로 파일이다.
                    ExplorerFileItem left = (ExplorerFileItem) item.clone();
                    left.side = LEFT;
                    ExplorerFileItem right = (ExplorerFileItem) item.clone();
                    right.side = RIGHT;
                    zipImageList.add(left);
                    zipImageList.add(right);
                } else {
                    zipImageList.add(item);
                }
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

    @Override
    protected void onPostExecute(Void result) {
        if (listener != null) {
            listener.onFinish(zipImageList);
        }
    }
}
