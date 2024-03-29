package com.duongame.activity.viewer;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.duongame.MainApplication;
import com.duongame.R;
import com.duongame.db.Book;
import com.duongame.db.BookDB;
import com.duongame.db.TextBook;
import com.duongame.file.FileHelper;
import com.duongame.listener.TextOnTouchListener;
import com.duongame.manager.FontManager;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.duongame.db.TextBook.LINES_PER_PAGE;
import static com.duongame.file.FileHelper.BLOCK_SIZE;

/**
 * Created by namjungsoo on 2016-11-18.
 */

// text는 pager가 아니라 상하 스크롤되는 액티비티
public class TextActivity extends BaseViewerActivity {
    private static final String TAG = "TextActivity";

    private ScrollView scrollText;
    private TextView textContent;
    private ProgressBar progressBar;

    private String path;
    private String name;

    private long size = 0;
    private int page = 0;
    private int scroll = 0;

    private int fontSize = 20;
    private int fontIndex = 4;
    private boolean useScrollV2 = false;

    private ArrayList<String> lineList = new ArrayList<>();

    private static boolean USE_10K_PERCENT = true;
    private static int MAX_FONT_SIZE_INDEX = 16;

    private int[] fontSizeArray = new int[]{12, 14, 16, 18, 20,
            24, 28, 32, 36, 40,
            44, 48, 54, 60, 66,
            72};

    ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            int scrollY = getScrollY();
            scrollText.setScrollY(scrollY);
            scrollText.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }
    };

    @Override
    protected void updateNightMode() {
        super.updateNightMode();

        updateNightModeText();
    }

    void updateNightModeText() {
        try {
            if (MainApplication.getInstance(this).isNightMode()) {
                if(scrollText != null)
                    scrollText.setBackgroundColor(Color.BLACK);
                if(textContent != null)
                    textContent.setTextColor(Color.rgb(192, 192, 192));
            } else {
                if(scrollText != null)
                    scrollText.setBackgroundColor(Color.WHITE);
                if(textContent != null)
                    textContent.setTextColor(Color.BLACK);
            }
        } catch (NullPointerException e) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewResId = R.layout.activity_text;

        // onCreate안에서 updateNightModeText가 호출됨
        super.onCreate(savedInstanceState);

        initToolBox();

        scrollText = findViewById(R.id.scroll_text);
        textContent = findViewById(R.id.text_content);
        textContent.setTypeface(FontManager.getTypeFaceNanumMeyongjo(this));
        textContent.setTextSize(fontSize);
        textContent.setLineSpacing(0, 1.5f);

        updateNightModeText();

        scrollText.setOnTouchListener(new TextOnTouchListener(this));
        scrollText.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                updateScrollInfo(page);
            }
        });

        scrollText.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        progressBar = (ProgressBar) findViewById(R.id.progress_text);

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

            // 이때 DB의 마이그레이션이 필요할수 있음
            int current_page = extras.getInt("current_page");
            int current_file = extras.getInt("current_file");
            page = current_page / LINES_PER_PAGE;

            if (current_file == 0) {
                scroll = current_page - LINES_PER_PAGE * page;
                useScrollV2 = false;
            } else {
                scroll = current_file;
                useScrollV2 = true;
            }

            textSize.setText(FileHelper.getMinimizedSize(size));
            textName.setText(name);

            progressBar.setVisibility(View.VISIBLE);
            final LoadTextTask task = new LoadTextTask(this);
            task.execute(path);
        }
    }

    void updateFontSize() {
        fontSize = fontSizeArray[fontIndex];
        textContent.setTextSize(fontSize);
    }

    // 1/10000 퍼센트를 지정함
    int getPercent2() {
        // 현재 스크롤 위치를 얻어보자
        int maxScroll = scrollText.getChildAt(0).getHeight() - scrollText.getHeight();
        if (maxScroll <= 0) {
            return 10000;
        }

        int scrollY = scrollText.getScrollY();

        // 10000으로 곱한다.
        int percent = scrollY * LINES_PER_PAGE * 10 / maxScroll;
        return percent;
    }

    int getPercent() {
        // 현재 스크롤 위치를 얻어보자
        int maxScroll = scrollText.getChildAt(0).getHeight() - scrollText.getHeight();
        if (maxScroll <= 0) {
            return 1000;
        }
        int scrollY = scrollText.getScrollY();

        int percent = scrollY * LINES_PER_PAGE / maxScroll;
        return percent;
    }

    @Override
    public void onPause() {
        if (USE_10K_PERCENT) {
            int percent = getPercent2();
            if (percent >= LINES_PER_PAGE * 10) {// 9999를 만들어야 페이지를 넘어가지 않는다.
                percent = LINES_PER_PAGE * 10 - 1;
            }
            Book book = TextBook.buildTextBook2(path, name, size, percent, page, lineList.size());
            BookDB.setLastBook(this, book);
        } else {
            int percent = getPercent();
            if (percent >= LINES_PER_PAGE) {
                percent = LINES_PER_PAGE - 1;
            }
            Book book = TextBook.buildTextBook(path, name, size, percent, page, lineList.size());
            BookDB.setLastBook(this, book);
        }

        super.onPause();
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

        pagingAnim.setVisibility(View.INVISIBLE);

        seekPage.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean dragging;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (dragging) {
                    page = seekBar.getProgress();
                    scroll = 0;
                    updateTextView();
                    updateScrollInfo(page);
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

        leftPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int progress = seekPage.getProgress();
                if (progress > 0) {
                    page = progress - 1;
                    updateTextView();
                    updateScrollInfo(progress - 1);
                }
            }
        });

        rightPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int progress = seekPage.getProgress();
                int max = seekPage.getMax();
                if (progress < max) {
                    page = progress + 1;
                    updateTextView();
                    updateScrollInfo(progress + 1);
                }
            }
        });
    }

    // 현재 스크롤 정보를 표시하고 seek를 업데이트함
    // position은 0 베이스이다.
    public void updateScrollInfo(int position) {
        //final int count = lineList.size() / LINES_PER_PAGE;

        // 0-999까지가 1개의 페이지이다.
        final int count = (lineList.size() - 1) / LINES_PER_PAGE;

        if (USE_10K_PERCENT) {
            // 현재페이지(1부터시작)/전체페이지 (현재페이지 퍼센트%)
            String text = (position + 1) + "/" + (count + 1) + String.format(" (%02d%%)", getPercent2() / 100);
            textPage.setText(text);
        } else {
            String text = (position + 1) + "/" + (count + 1) + String.format(" (%02d%%)", getPercent() / 10);
            textPage.setText(text);
        }

        seekPage.setMax(count);

        // 이미지가 1개일 경우 처리
        if (position == 0 && count == 0) {
            seekPage.setProgress(count);
            seekPage.setEnabled(false);
        } else {
            seekPage.setProgress(position);
            seekPage.setEnabled(true);
        }
    }

    static class LoadTextTask extends AsyncTask<String, Integer, Void> {
        WeakReference<TextActivity> activityWeakReference;

        LoadTextTask(TextActivity activity) {
            activityWeakReference = new WeakReference<TextActivity>(activity);
        }

        private String checkEncoding(String fileName) {
            byte[] buf = new byte[BLOCK_SIZE];
            final FileInputStream fileInputStream;
            try {
                fileInputStream = new FileInputStream(fileName);

                // (1)
                final UniversalDetector detector = new UniversalDetector(null);

                // (2)
                int nRead;
                nRead = fileInputStream.read(buf);
                //TODO: FIX: 빠른속도를 위해서 8192b만 읽어서 처리
                //while ((nRead = fileInputStream.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nRead);
                //}
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
            //TODO: 에러일 경우 EUC-KR로 간주. 이것도 국가별로 변경해야함
            //final String encoding = "euc-kr";

            final File file = new File(path);
            try {
                final FileInputStream fis = new FileInputStream(file);

                //FIX:
                // encoding이 null일수 있음
                final InputStreamReader reader;
                if (encoding != null) {
                    reader = new InputStreamReader(fis, encoding);
                } else {
                    reader = new InputStreamReader(fis, "euc-kr");
                }

                final BufferedReader bufferedReader = new BufferedReader(reader);

                // 파일 전체의 라인을 얻는다.
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    TextActivity activity = activityWeakReference.get();
                    if (activity != null) {
                        activity.lineList.add(line);

                        // 라인 갯수가 1000개씩 딱딱 맞다면, 해당페이지의 정보를 textview에 업데이트 한다.
                        if (activity.lineList.size() == (activity.page + 1) * LINES_PER_PAGE) {
                            publishProgress(activity.page);
                        }
                    }
                }

                TextActivity activity = activityWeakReference.get();
                if (activity != null) {
                    // 남은 자료가 마지막것보다 작지만 나머지를 보내야 할때는 publish 한다.
                    final int size = activity.lineList.size();
                    if (size > activity.page * LINES_PER_PAGE && size < (activity.page + 1) * LINES_PER_PAGE) {
                        publishProgress(activity.page);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            TextActivity activity = activityWeakReference.get();
            if (activity != null) {
                activity.updateTextView();
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            TextActivity activity = activityWeakReference.get();
            if (activity != null) {
                activity.textInfo.setText("" + activity.lineList.size() + " lines");
                activity.scrollText.getViewTreeObserver().addOnGlobalLayoutListener(activity.onGlobalLayoutListener);
                activity.updateScrollInfo(activity.page);

                activity.progressBar.setVisibility(View.GONE);
            }
        }
    }

    // 본문 텍스트를 업데이트함
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

    // 저장되었던 스크롤 위치를 계산함
    int getScrollY() {
        int maxScroll = scrollText.getChildAt(0).getHeight() - scrollText.getHeight();
        int scrollY;

        if (useScrollV2) {
            scrollY = maxScroll * TextActivity.this.scroll / (LINES_PER_PAGE * 10);
        } else {
            scrollY = maxScroll * TextActivity.this.scroll / LINES_PER_PAGE;
        }
        return scrollY;
    }
}
