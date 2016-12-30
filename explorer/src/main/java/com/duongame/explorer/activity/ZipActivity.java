package com.duongame.explorer.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.duongame.explorer.adapter.ExplorerFileItem;
import com.duongame.explorer.adapter.ViewerPagerAdapter;
import com.duongame.explorer.adapter.PhotoPagerAdapter;
import com.duongame.explorer.bitmap.ZipLoader;
import com.duongame.explorer.helper.AlertManager;

import net.lingala.zip4j.exception.ZipException;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-19.
 */

public class ZipActivity extends PagerActivity {
    private final static String TAG = "ZipActivity";

    private boolean zipExtractCompleted = false;
    private final ZipLoader zipLoader = new ZipLoader();

    private ZipLoader.ZipLoaderListener listener = new ZipLoader.ZipLoaderListener() {
        @Override
        public void onSuccess(int i, ArrayList<ExplorerFileItem> zipImageList) {
//            Log.d(TAG, "onSuccess="+i);
            final ArrayList<ExplorerFileItem> imageList = (ArrayList<ExplorerFileItem> )zipImageList.clone();

            pagerAdapter.setImageList(imageList);
            pagerAdapter.notifyDataSetChanged();

            updateScrollInfo(pager.getCurrentItem());
        }

        @Override
        public void onFail(int i, String name) {

        }

        @Override
        public void onFinish(ArrayList<ExplorerFileItem> zipImageList) {
            // 체크해놓고 나중에 파일을 지우지 말자
            zipExtractCompleted = true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pagerAdapter.setExifRotation(false);

        try {
            processIntent();
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        zipLoader.cancelTask();

        super.onDestroy();
    }

    protected ArrayList<ExplorerFileItem> getImageList() {
        return null;
    }

    @Override
    protected ViewerPagerAdapter createPagerAdapter() {
        return new PhotoPagerAdapter(this);
    }

    @Override
    protected void updateName(int i) {
        textName.setText(name);
    }

    protected void processIntent() throws ZipException {
        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();
        if (extras != null) {
            final int page = extras.getInt("page");
            path = extras.getString("path");
            name = extras.getString("name");

            // initPagerAdapter의 기능이다.
            pager.setAdapter(pagerAdapter);// setAdapter이후에 imageList를 변경하면 항상 notify해주어야 한다.

            // zip 파일을 로딩한다.
            final ArrayList<ExplorerFileItem> imageList = zipLoader.load(this, path, listener, false);
            if(imageList.size() <= 0) {
                AlertManager.showAlert(this, "알림", "압축(ZIP) 파일에 이미지가 없습니다.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }, null, true);
                return;
            }

            pagerAdapter.setImageList(imageList);
            pagerAdapter.notifyDataSetChanged();

            seekPage.setMax(imageList.size());
            seekPage.setProgress(1);
        }
    }
}
