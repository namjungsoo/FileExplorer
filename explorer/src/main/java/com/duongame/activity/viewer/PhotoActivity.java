package com.duongame.activity.viewer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.duongame.adapter.ExplorerItem;
import com.duongame.adapter.PhotoPagerAdapter;
import com.duongame.adapter.ViewerPagerAdapter;
import com.duongame.helper.FileHelper;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-18.
 */

public class PhotoActivity extends PagerActivity {
    private final static String TAG = "PhotoActivity";

    public static Intent getLocalIntent(Context context, ExplorerItem item) {
        final Intent intent = new Intent(context, PhotoActivity.class);
        // 풀패스에서 폴더만 떼옴
        intent.putExtra("path", item.path.substring(0, item.path.lastIndexOf('/')));
        intent.putExtra("name", item.name);
        intent.putExtra("size", item.size);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initPagerAdapter();

        processIntent();
    }

    protected ArrayList<ExplorerItem> getImageList() {
        return application.getImageList();
    }

    @Override
    protected ViewerPagerAdapter createPagerAdapter() {
        // 이때는 애니메이션을 한다.
        // 그런데 애니메이션이 없으면 안해야 한다.
        return new PhotoPagerAdapter(this, true);
    }

    private void initPagerAdapter() {
        final ArrayList<ExplorerItem> imageList = getImageList();
        if(imageList == null)
            return;

        pagerAdapter.setImageList(imageList);
        pager.setAdapter(pagerAdapter);

        seekPage.setMax(imageList.size());
    }

    protected void processIntent() {
        final ArrayList<ExplorerItem> imageList = getImageList();
        if(imageList == null)
            return;

        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();
        if (extras != null) {
            name = extras.getString("name");
            path = extras.getString("path");
            size = extras.getLong("size");

            textName.setText(name);

            //FIX:
            // 이미지 리스트가 없거나 사이즈가 0이라면 리턴한다.
            if(imageList == null || imageList.size() == 0) {
                finish();
            }

            // 이미지 파일 리스트에서 현재 위치를 찾자
            int current = 0;
            for (int i = 0; i < imageList.size(); i++) {
                if (imageList.get(i).name.equals(name)) {
                    current = i;
                    break;
                }
            }

            pager.setCurrentItem(current);

            updateName(current);
            updateInfo(current);
            updateScrollInfo(current);
        }
    }

    @Override
    public void updateInfo(int position) {
        if (pager.getCurrentItem() == position) {
            ExplorerItem item = pagerAdapter.getImageList().get(position);
            textInfo.setText("" + item.width + " x " + item.height);
            textSize.setText(FileHelper.getMinimizedSize(item.size));
        }
    }
}
