package com.duongame.explorer.task;

import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.TextView;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by namjungsoo on 2017. 1. 22..
 */

public class LoadTextTask extends AsyncTask<String, Integer, Void> {
    TextView textContent;
    TextView textInfo;

    int page;
    int fontSize;
    int scroll;

    ArrayList<String> lineList;

    public LoadTextTask(TextView textContent, TextView textInfo, ArrayList<String> lineList, int page, int textSize, int scroll) {
        this.textContent = textContent;
        this.textInfo = textInfo;

        this.lineList = lineList;
        this.page = page;
        this.fontSize = textSize;
        this.scroll = scroll;
    }

    private String checkEncoding(String fileName) {
        byte[] buf = new byte[4096];
        final FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(fileName);

            // (1)
            final UniversalDetector detector = new UniversalDetector(null);

            // (2)
            int nread;
            while ((nread = fileInputStream.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            // (3)
            detector.dataEnd();

            // (4)
            final String encoding = detector.getDetectedCharset();

            // (5)
            detector.reset();
            fileInputStream.close();
            return encoding;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected Void doInBackground(String... params) {
        final String path = params[0];

        final String encoding = checkEncoding(path);

        final File file = new File(path);
        try {
            final FileInputStream fis = new FileInputStream(file);
            final InputStreamReader reader = new InputStreamReader(fis, encoding);
            final BufferedReader bufferedReader = new BufferedReader(reader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lineList.add(line);

                if (lineList.size() == (page + 1) * 1000) {
                    publishProgress(page);
                }
            }

            // 남은 자료가 마지막것보다 작지만 나머지를 보내야 할때는
            final int size = lineList.size();
            if (size > page * 1000 && size < (page + 1) * 1000) {
                publishProgress(page);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        final StringBuilder builder = new StringBuilder();
        final int size = lineList.size();
        if (size < page * 1000)
            return;

        int max = Math.min(size, (page + 1) * 1000);
        for (int i = page * 1000; i < max; i++) {
            builder.append(lineList.get(i));
            builder.append("\n");
        }

        final String text = builder.toString();
        textContent.setText(text);
        textContent.setTextSize(fontSize);
        textContent.setLineSpacing(0, 1.5f);
        textContent.setTextColor(Color.BLACK);
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        textInfo.setText("" + lineList.size() + " lines");
    }
}
