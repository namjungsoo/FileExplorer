package com.duongame.explorer.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.duongame.explorer.R;
import com.duongame.explorer.adapter.ExplorerFileItem;
import com.duongame.explorer.adapter.PhotoPagerAdapter;
import com.duongame.explorer.adapter.ViewerPagerAdapter;
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
    private ExplorerFileItem.Side side = ExplorerFileItem.Side.LEFT;

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

        updateTopSidePanelColor();
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

    private void updateTopSidePanelColor() {
        ImageView iv;
        TextView tv;
        switch(side) {
            case LEFT:
                iv = (ImageView)findViewById(R.id.img_left);
                tv = (TextView)findViewById(R.id.text_left);
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_orange_light));
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light));

                // 나머지 두개를 꺼주어야 한다.
                iv = (ImageView)findViewById(R.id.img_right);
                tv = (TextView)findViewById(R.id.text_right);
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.white));

                iv = (ImageView)findViewById(R.id.img_both);
                tv = (TextView)findViewById(R.id.text_both);
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                break;
            case RIGHT:
                iv = (ImageView)findViewById(R.id.img_right);
                tv = (TextView)findViewById(R.id.text_right);
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_orange_light));
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light));

                // 나머지 두개를 꺼주어야 한다.
                iv = (ImageView)findViewById(R.id.img_left);
                tv = (TextView)findViewById(R.id.text_left);
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.white));

                iv = (ImageView)findViewById(R.id.img_both);
                tv = (TextView)findViewById(R.id.text_both);
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                break;
            case SIDE_ALL:
                iv = (ImageView)findViewById(R.id.img_both);
                tv = (TextView)findViewById(R.id.text_both);
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_orange_light));
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light));

                // 나머지 두개를 꺼주어야 한다.
                iv = (ImageView)findViewById(R.id.img_right);
                tv = (TextView)findViewById(R.id.text_right);
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.white));

                iv = (ImageView)findViewById(R.id.img_left);
                tv = (TextView)findViewById(R.id.text_left);
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                break;
        }
    }

    private void updatePageSide() {
        // Task가 실행중이면 pause
        // 그리고 리스트에서 좌우를 변경함
        // 이후 task에서는 변경된 것으로 작업함
    }

    @Override
    protected void setFullscreen(boolean fullscreen) {
        super.setFullscreen(fullscreen);

        if(!fullscreen) {
            // top_side_panel을 보이게 하자
            final LinearLayout topSidePanel = (LinearLayout)findViewById(R.id.top_side_panel);
            topSidePanel.setVisibility(View.VISIBLE);

            final LinearLayout layoutLeft = (LinearLayout)findViewById(R.id.layout_left);
            layoutLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(side == ExplorerFileItem.Side.LEFT)
                        return;
                    side = ExplorerFileItem.Side.LEFT;
                    updateTopSidePanelColor();
                    updatePageSide();
                }
            });
            final LinearLayout layoutRight = (LinearLayout)findViewById(R.id.layout_right);
            layoutRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(side == ExplorerFileItem.Side.RIGHT)
                        return;
                    side = ExplorerFileItem.Side.RIGHT;
                    updateTopSidePanelColor();
                    updatePageSide();
                }
            });

            final LinearLayout layoutBoth = (LinearLayout)findViewById(R.id.layout_both);
            layoutBoth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(side == ExplorerFileItem.Side.SIDE_ALL)
                        return;
                    side = ExplorerFileItem.Side.SIDE_ALL;
                    updateTopSidePanelColor();
                    updatePageSide();
                }
            });
        }
    }
}
