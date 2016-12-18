package com.duongame.explorer.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

import com.duongame.explorer.R;

import java.io.BufferedInputStream;
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

        scrollText = (ScrollView)findViewById(R.id.scroll_text);
        textContent = (TextView)findViewById(R.id.text_content);

        processIntent();
    }

    protected void processIntent() {
        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();
        if (extras != null) {
            path = extras.getString("path");

            FileInputStream is = null;
            try {
                is = new FileInputStream(path);
                BufferedInputStream bis = new BufferedInputStream(is);
                byte[] data = new byte[bis.available()];
                bis.read(data);

                String text = new String(data);
                textContent.setText(text);
                textContent.setTextSize(20);
                textContent.setLineSpacing(0,1.5f);
                textContent.setTextColor(Color.BLACK);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
