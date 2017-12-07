package com.duongame.task;

import android.os.AsyncTask;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by namjungsoo on 2017. 1. 28..
 */

// AsyncTask:
//  parameter
//  progress
//  result
public class LoadGifTask extends AsyncTask<String, Integer, Void> {
    public interface LoadGifListener {
        public void onSuccess(byte[] data);

        public void onFail();
    }

    private byte[] data;
    final private LoadGifListener listener;

    public LoadGifTask(LoadGifListener listener) {
        this.listener = listener;
    }

    public static byte[] loadGif(String path) throws IOException {
        final File file = new File(path);
        byte[] data = new byte[(int) file.length()];
        final FileInputStream fis = new FileInputStream(file);
        final DataInputStream dis = new DataInputStream(fis);
        dis.readFully(data);
        dis.close();
        fis.close();
        return data;
    }

    @Override
    protected Void doInBackground(String... params) {
        final String path = params[0];

        try {
            final File file = new File(path);
            data = new byte[(int) file.length()];
            final FileInputStream fis = new FileInputStream(file);
            final DataInputStream dis = new DataInputStream(fis);
            dis.readFully(data);
            dis.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onFail();
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onFail();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (listener != null) {
            listener.onSuccess(data);
        }
    }
}
