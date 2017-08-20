package com.duongame.viewer.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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
    private int fontIndex = 4;

    private ArrayList<String> lineList = new ArrayList<>();
    static int MAX_FONT_SIZE_INDEX = 16;

    private int[] fontSizeArray = new int[] { 12, 14, 16, 18, 20,
            24, 28, 32, 36, 40,
            44, 48, 54, 60, 66,
            72 };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewResId = R.layout.activity_text;
        super.onCreate(savedInstanceState);

        initToolBox();

        scrollText = (ScrollView) findViewById(R.id.scroll_text);

        textContent = (TextView) findViewById(R.id.text_content);
        textContent.setTypeface(FontManager.getTypeFaceNanumMeyongjo(this));
        textContent.setTextSize(fontSize);
        textContent.setLineSpacing(0, 1.5f);
        textContent.setTextColor(Color.BLACK);

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

    void updateFontSize() {
        fontSize = fontSizeArray[fontIndex];
        textContent.setTextSize(fontSize);
        Log.d(TAG, "fontSize=" + fontSize);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if(fontIndex + 1 < MAX_FONT_SIZE_INDEX) {
                    fontIndex++;
                    updateFontSize();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if(fontIndex - 1 >= 0) {
                    fontIndex--;
                    updateFontSize();
                }
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }
}
