package com.duongame.viewer.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.duongame.comicz.db.BookDB;
import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.helper.FileHelper;
import com.duongame.viewer.adapter.PdfPagerAdapter;
import com.duongame.viewer.adapter.ViewerPagerAdapter;

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

    public static Intent getLocalIntent(Context context, ExplorerItem item) {
        final Intent intent = new Intent(context, PdfActivity.class);
        intent.putExtra("path", item.path);
        intent.putExtra("name", item.name);
        intent.putExtra("size", item.size);
        intent.putExtra("current_page", 0);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate pdf");

        this.adapter = (PdfPagerAdapter) pagerAdapter;
        processIntent();
    }

    @Override
    protected ViewerPagerAdapter createPagerAdapter() {
        return new PdfPagerAdapter(this);
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");

        final BookDB.Book book = new BookDB.Book();

        // 고정적인 내용 5개
        book.path = path;
        book.name = name;
        book.type = ExplorerItem.FileType.ZIP;
        book.size = size;
        book.total_file = 0;// 파일의 갯수이다.

        // 동적인 내용 6개
        final int page = pager.getCurrentItem();
        book.current_page = page;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            book.total_page = renderer.getPageCount();
        }
        book.current_file = 0;
        book.extract_file = 0;
        book.side = ExplorerItem.Side.SIDE_ALL;

        BookDB.setLastBook(this, book);

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
        Log.d(TAG, "processIntent pdf");
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
                        imageList.add(new ExplorerItem(FileHelper.setPdfFileNameFromPage(path, i), name, null, 0, ExplorerItem.FileType.PDF));
                    }

                    adapter.setRenderer(renderer);

                    pagerAdapter.setImageList(imageList);
                    pagerAdapter.notifyDataSetChanged();

                    pager.setCurrentItem(page);

                    textInfo.setText(""+renderer.getPageCount() + " pages");
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
