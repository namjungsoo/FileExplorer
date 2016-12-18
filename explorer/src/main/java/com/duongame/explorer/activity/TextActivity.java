package com.duongame.explorer.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.duongame.explorer.R;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
/**
 * Created by namjungsoo on 2016-11-18.
 */

// text는 pager가 아니라 상하 스크롤되는 액티비티
public class TextActivity extends ViewerActivity {
    ScrollView scrollText;
    TextView textContent;
    String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_text);

        scrollText = (ScrollView) findViewById(R.id.scroll_text);
        textContent = (TextView) findViewById(R.id.text_content);

        scrollText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        processIntent();
    }

    protected void processIntent() {
        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();
        if (extras != null) {
            path = extras.getString("path");

            try {
                File file = new File(path);
                FileInputStream is = new FileInputStream(file);

                UniversalDetector detector = new UniversalDetector(null);
                byte[] buffer = new byte[is.available()];
                is.read(buffer);
                is.close();
                detector.handleData(buffer, 0, buffer.length);
                detector.dataEnd();

                String text = new String(buffer, detector.getDetectedCharset());
                textContent.setText(text);
                textContent.setTextSize(20);
                textContent.setLineSpacing(0, 1.5f);
                textContent.setTextColor(Color.BLACK);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
