package com.duongame.explorer.activity;

import android.content.Intent;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.adapter.ViewerPagerAdapter;
import com.duongame.explorer.adapter.PdfPagerAdapter;
import com.duongame.explorer.helper.FileHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-18.
 */
//TODO: 아직 구현되지 않음 작업중
public class PdfActivity extends PagerActivity {
    private final static String TAG = "PhotoActivity";
    private PdfRenderer renderer;
    private PdfPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate pdf");

        this.adapter = (PdfPagerAdapter)pagerAdapter;
        processIntent();
    }

    @Override
    protected ViewerPagerAdapter createPagerAdapter() {
        return new PdfPagerAdapter(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(renderer != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                renderer.close();
            }
        }
    }

    protected void processIntent() {
        Log.d(TAG, "processIntent pdf");
        final ArrayList<ExplorerItem> imageList;
        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();

        if (extras != null) {
            final int page = extras.getInt("page");
            path = extras.getString("path");
            name = extras.getString("name");

            pager.setAdapter(pagerAdapter);

            // pdf 파일의 페이지를 체크함
            try {
                final ParcelFileDescriptor parcel = ParcelFileDescriptor.open(new File(path), ParcelFileDescriptor.MODE_READ_ONLY);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    renderer = new PdfRenderer(parcel);

                    imageList = new ArrayList<>();
                    for(int i=0; i<renderer.getPageCount(); i++) {
                        // path를 페이지 번호로 사용하자
                        imageList.add(new ExplorerItem(FileHelper.setPdfFileNameFromPage(path, i), name, null, 0, ExplorerItem.FileType.PDF));
                    }

                    adapter.setRenderer(renderer);

                    pagerAdapter.setImageList(imageList);
                    pagerAdapter.notifyDataSetChanged();

                    seekPage.setMax(imageList.size());
                    seekPage.setProgress(1);
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
