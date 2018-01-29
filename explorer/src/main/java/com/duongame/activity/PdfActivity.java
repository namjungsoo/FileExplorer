package com.duongame.activity;

import android.content.Intent;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;

import com.duongame.adapter.ExplorerItem;
import com.duongame.db.Book;
import com.duongame.db.BookDB;
import com.duongame.helper.AppHelper;
import com.duongame.helper.FileHelper;
import com.duongame.adapter.PdfPagerAdapter;
import com.duongame.adapter.ViewerPagerAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-18.
 */
public class PdfActivity extends PagerActivity {
    private final static String TAG = "PhotoActivity";

    private PdfRenderer renderer;
    private PdfPagerAdapter adapter;
    private long size;// zip 파일의 용량

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.adapter = (PdfPagerAdapter) pagerAdapter;
        processIntent();
    }

    @Override
    protected ViewerPagerAdapter createPagerAdapter() {
        return new PdfPagerAdapter(this);
    }

    @Override
    protected void onPause() {
        if(AppHelper.isComicz(this)) {
            final Book book = new Book();

            // 고정적인 내용 5개
            book.path = path;
            book.name = name;
            book.type = ExplorerItem.FILETYPE_ZIP;
            book.size = size;
            book.total_file = 0;// 파일의 갯수이다.

            // 동적인 내용 6개
            final int page = pager.getCurrentItem();
            book.current_page = page;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //FIX:
                // renderer가 null일 수 있음
                if (renderer != null) {
                    book.total_page = renderer.getPageCount();
                }
            }
            book.current_file = 0;
            book.extract_file = 0;
            book.side = ExplorerItem.SIDE_ALL;

            BookDB.setLastBook(this, book);
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (renderer != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                renderer.close();
            }
        }
    }

    @Override
    protected void updateName(int i) {
        textName.setText(name);
    }

    protected void processIntent() {
        final ArrayList<ExplorerItem> imageList;
        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();

        if (extras != null) {
            final int page = extras.getInt("current_page");
            path = extras.getString("path");
            name = extras.getString("name");
            size = extras.getLong("size");

            textSize.setText(FileHelper.getMinimizedSize(size));


            pager.setAdapter(pagerAdapter);

            // pdf 파일의 페이지를 체크함
            try {
                final ParcelFileDescriptor parcel = ParcelFileDescriptor.open(new File(path), ParcelFileDescriptor.MODE_READ_ONLY);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    renderer = new PdfRenderer(parcel);

                    imageList = new ArrayList<>();
                    for (int i = 0; i < renderer.getPageCount(); i++) {
                        // path를 페이지 번호로 사용하자
                        imageList.add(new ExplorerItem(FileHelper.setPdfFileNameFromPage(path, i), name, null, 0, ExplorerItem.FILETYPE_PDF));
                    }

                    adapter.setRenderer(renderer);

                    pagerAdapter.setImageList(imageList);
                    pagerAdapter.notifyDataSetChanged();

                    pager.setCurrentItem(page);

                    textInfo.setText("" + renderer.getPageCount() + " pages");
                    updateName(page);
                    updateScrollInfo(page);
                } else {
                    finish();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
