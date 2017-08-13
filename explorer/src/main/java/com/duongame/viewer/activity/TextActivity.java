package com.duongame.viewer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

import com.duongame.R;
import com.duongame.explorer.helper.FileHelper;
import com.duongame.explorer.manager.FontManager;
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
    private String name;
    private long size;

    private int page;
    private int scroll;
    private int fontSize = 20;
    private ArrayList<String> lineList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);

        initToolBox();

        scrollText = (ScrollView) findViewById(R.id.scroll_text);
        textContent = (TextView) findViewById(R.id.text_content);
        textContent.setTypeface(FontManager.getTypeFaceNanumMeyongjo(this));

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
            name = extras.getString("name");
            size = extras.getLong("size");

            page = extras.getInt("page");
            scroll = extras.getInt("scroll");

            textSize.setText(FileHelper.getMinimizedSize(size));
            textName.setText(name);

            final LoadTextTask task = new LoadTextTask(textContent, textInfo, lineList, page, fontSize, scroll);
            task.execute(path);
        }
    }

}
