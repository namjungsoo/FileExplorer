package com.duongame.viewer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

import com.duongame.R;
import com.duongame.explorer.task.LoadTextTask;
import com.duongame.viewer.listener.TextOnTouchListener;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-18.
 */

// text는 pager가 아니라 상하 스크롤되는 액티비티
public class TextActivity extends ViewerActivity {
    private static final String TAG = "TextActivity";

    private ScrollView scrollText;
    private TextView textContent;
    private String path;
    private int page;
    private int scroll;
    private int textSize = 20;
    private ArrayList<String> lineList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);

        initToolBox();

        scrollText = (ScrollView) findViewById(R.id.scroll_text);
        textContent = (TextView) findViewById(R.id.text_content);

        scrollText.setOnTouchListener(new TextOnTouchListener(this));
        processIntent();

        // 전체 화면으로 들어감
        setFullscreen(true);
    }

    protected void processIntent() {
        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();

        if (extras != null) {
            path = extras.getString("path");
            page = extras.getInt("page");
            scroll = extras.getInt("scroll");

            textName.setText(path);

            final LoadTextTask task = new LoadTextTask(textContent, lineList, page, textSize, scroll);
            task.execute(path);
        }
    }

}
