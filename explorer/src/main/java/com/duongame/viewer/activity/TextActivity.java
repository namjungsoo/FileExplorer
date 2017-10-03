package com.duongame.viewer.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.duongame.R;
import com.duongame.comicz.db.BookDB;
import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.helper.FileHelper;
import com.duongame.explorer.manager.FontManager;
import com.duongame.viewer.listener.TextOnTouchListener;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import static com.duongame.comicz.db.BookDB.TextBook.LINES_PER_PAGE;

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

    private long size = 0;
    private int page = 0;
    private int scroll = 0;

    private int fontSize = 20;
    private int fontIndex = 4;

    private ArrayList<String> lineList = new ArrayList<>();
    static int MAX_FONT_SIZE_INDEX = 16;

    private int[] fontSizeArray = new int[]{12, 14, 16, 18, 20,
            24, 28, 32, 36, 40,
            44, 48, 54, 60, 66,
            72};

    public static Intent getLocalIntent(Context context, ExplorerItem item) {
        final Intent intent = new Intent(context, TextActivity.class);
        intent.putExtra("path", item.path);
        intent.putExtra("name", item.name);
        intent.putExtra("size", item.size);
        intent.putExtra("current_page", 0);
        return intent;
    }

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
        scrollText.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                updateScrollInfo(page);
            }
        });
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

            int current_page = extras.getInt("current_page");
            page = current_page / LINES_PER_PAGE;
            scroll = current_page - LINES_PER_PAGE * page;

            textSize.setText(FileHelper.getMinimizedSize(size));
            textName.setText(name);

            final LoadTextTask task = new LoadTextTask();
            task.execute(path);
        }
    }

    void updateFontSize() {
        fontSize = fontSizeArray[fontIndex];
        textContent.setTextSize(fontSize);
        Log.d(TAG, "fontSize=" + fontSize);
    }

    int getPercent() {
        // 현재 스크롤 위치를 얻어보자
        int maxScroll = scrollText.getChildAt(0).getHeight() - scrollText.getHeight();
        int scrollY = scrollText.getScrollY();

//        Log.d(TAG, "maxScroll=" + maxScroll + " scrollY=" + scrollY);

        int percent = scrollY * LINES_PER_PAGE / maxScroll;
        return percent;
    }

    @Override
    public void onPause() {
        super.onPause();

        int percent = getPercent();
        if (percent >= LINES_PER_PAGE) {
            percent = LINES_PER_PAGE - 1;
        }

        BookDB.Book book = BookDB.buildTextBook(path, name, size, percent, page, lineList.size());
        BookDB.setLastBook(this, book);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (fontIndex + 1 < MAX_FONT_SIZE_INDEX) {
                    fontIndex++;
                    updateFontSize();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (fontIndex - 1 >= 0) {
                    fontIndex--;
                    updateFontSize();
                }
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void initToolBox() {
        super.initToolBox();
        seekPage.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean dragging;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(dragging) {
                    page = seekBar.getProgress();
                    scroll = 0;
                    seekChanged();
                    updateTextView();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                dragging = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                dragging = false;
            }
        });
    }

    public void updateScrollInfo(int position) {
        Log.d(TAG, "updateScrollInfo=" + position);
        final int count = lineList.size() / LINES_PER_PAGE;
        textPage.setText((position + 1) + "/" + count + String.format(" (%02d%%)", getPercent() / 10));
        seekPage.setMax(count - 1);

        // 이미지가 1개일 경우 처리
        if (position == 0 && count == 1) {
            seekPage.setProgress(count);
            seekPage.setEnabled(false);
        } else {
            seekPage.setProgress(position);
            seekPage.setEnabled(true);
        }
    }

    public class LoadTextTask extends AsyncTask<String, Integer, Void> {
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

            // 인코딩을 얻는다.
            final String encoding = checkEncoding(path);

            final File file = new File(path);
            try {
                final FileInputStream fis = new FileInputStream(file);
                final InputStreamReader reader = new InputStreamReader(fis, encoding);
                final BufferedReader bufferedReader = new BufferedReader(reader);

                // 파일 전체의 라인을 얻는다.
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    lineList.add(line);

                    // 라인 갯수가 1000개씩 딱딱 맞다면, 해당페이지의 정보를 textview에 업데이트 한다.
                    if (lineList.size() == (page + 1) * LINES_PER_PAGE) {
                        publishProgress(page);
                    }
                }

                // 남은 자료가 마지막것보다 작지만 나머지를 보내야 할때는 publish 한다.
                final int size = lineList.size();
                if (size > page * LINES_PER_PAGE && size < (page + 1) * LINES_PER_PAGE) {
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
            updateTextView();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            textInfo.setText("" + lineList.size() + " lines");
            seekChanged();
        }
    }

    void updateTextView() {
        final StringBuilder builder = new StringBuilder();
        final int size = lineList.size();
        if (size < page * LINES_PER_PAGE)
            return;

        // 전체 텍스트에서 페이지에 해당하는 라인을 1000개(LINES_PER_PAGE)만큼 추가해 준다.
        int max = Math.min(size, (page + 1) * LINES_PER_PAGE);
        for (int i = page * LINES_PER_PAGE; i < max; i++) {
            builder.append(lineList.get(i));
            builder.append("\n");
        }

        final String text = builder.toString();
        textContent.setText(text);
    }

    void seekChanged() {
        updateScrollInfo(page);

        scrollText.post(new Runnable() {
            @Override
            public void run() {
                // 스크롤에 따라서 움직여 줘야 함
                int maxScroll = scrollText.getChildAt(0).getHeight() - scrollText.getHeight();
                int scrollY = maxScroll * TextActivity.this.scroll / LINES_PER_PAGE;

                //TODO: 어떤게 좋은지 체크해봐야 함
//                scrollText.setScrollY(scrollY);
                scrollText.scrollTo(0,0);
                scrollText.invalidate();
            }
        });

    }
}
