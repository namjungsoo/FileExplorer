package com.duongame.viewer.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.duongame.comicz.db.BookDB;
import com.duongame.explorer.R;
import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.bitmap.BitmapCache;
import com.duongame.explorer.bitmap.ZipLoader;
import com.duongame.explorer.helper.AlertHelper;
import com.duongame.viewer.adapter.PhotoPagerAdapter;
import com.duongame.viewer.adapter.ViewerPagerAdapter;

import net.lingala.zip4j.exception.ZipException;

import java.util.ArrayList;

import static com.duongame.explorer.adapter.ExplorerItem.Side.LEFT;
import static com.duongame.explorer.adapter.ExplorerItem.Side.RIGHT;
import static com.duongame.explorer.adapter.ExplorerItem.Side.SIDE_ALL;

/**
 * Created by namjungsoo on 2016-11-19.
 */

public class ZipActivity extends PagerActivity {
    private final static String TAG = "ZipActivity";

    private final ZipLoader zipLoader = new ZipLoader();

    private long size;// zip 파일의 용량
    private ExplorerItem.Side side = LEFT;
    private ExplorerItem.Side lastSide = LEFT;

    private int totalFileCount = 0;
    private int extractFileCount = 0;// 압축 풀린 파일의 갯수
    private boolean zipExtractCompleted = false;

    private void changeSide(ExplorerItem.Side side) {
        lastSide = this.side;
        this.side = side;
    }

    @Override
    protected void updateScrollInfo(int position) {
//        Log.d(TAG, "updateScrollInfo="+position);
        if (totalFileCount == 0) {
            textPage.setText((position + 1) + "/" + pagerAdapter.getCount());
        } else {
            int loadingPercent = (extractFileCount + 1) * 100 / totalFileCount;
            if (loadingPercent == 100) {
                textPage.setText((position + 1) + "/" + pagerAdapter.getCount());
            } else {
                textPage.setText((position + 1) + "/" + pagerAdapter.getCount() + String.format(" (%02d%%)", loadingPercent));
            }
        }

        seekPage.setMax(pagerAdapter.getCount());
        seekPage.setProgress(position + 1);
    }

    private ZipLoader.ZipLoaderListener listener = new ZipLoader.ZipLoaderListener() {
        @Override
        public void onSuccess(int i, ArrayList<ExplorerItem> zipImageList, int totalFileCount) {
//            Log.i(TAG, "onSuccess=" + i + " totalFileCount=" + totalFileCount);
            ZipActivity.this.totalFileCount = totalFileCount;
            extractFileCount = i;

            final ArrayList<ExplorerItem> imageList = (ArrayList<ExplorerItem>) zipImageList.clone();

            pagerAdapter.setImageList(imageList);
            pagerAdapter.notifyDataSetChanged();

            updateScrollInfo(pager.getCurrentItem());
        }

        @Override
        public void onFail(int i, String name) {

        }

        @Override
        public void onFinish(ArrayList<ExplorerItem> zipImageList, int totalFileCount) {
            // 체크해놓고 나중에 파일을 지우지 말자
            Log.i(TAG, "onFinish totalFileCount=" + totalFileCount);
            zipExtractCompleted = true;
            ZipActivity.this.totalFileCount = totalFileCount;
            extractFileCount = totalFileCount;
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
    protected void onPause() {
        Log.i(TAG, "onPause");
        zipLoader.cancelTask();

        final BookDB.Book book = new BookDB.Book();

        // 고정적인 내용 5개
        book.path = path;
        book.name = name;
        book.type = ExplorerItem.FileType.ZIP;
        book.size = size;
        book.total_file = totalFileCount;// 파일의 갯수이다.

        // 동적인 내용 6개
        final int page = pager.getCurrentItem();
        book.current_page = page;
        final ArrayList<ExplorerItem> zipImageList = pagerAdapter.getImageList();
        book.total_page = zipImageList.size();
        book.current_file = zipImageList.get(page).orgIndex;

        //TODO: 숫자가 맞는지 검증할것
        if (zipExtractCompleted) {
            // 전부 압축이 다 풀렸으므로 전체 파일 갯수를 입력해준다.
            book.extract_file = extractFileCount;
        } else {
            // 앞으로 읽어야할 위치를 기억하기 위해 +1을 함
            book.extract_file = extractFileCount + 1;
        }
        book.side = side;

        BookDB.setLastBook(this, book);

        super.onPause();
    }

    protected ArrayList<ExplorerItem> getImageList() {
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
            final int page = extras.getInt("current_page");
            path = extras.getString("path");
            name = extras.getString("name");
            size = extras.getLong("size");
            extractFileCount = extras.getInt("extract_file");
            side.setValue(extras.getInt("side"));
            lastSide = side;

            Log.i(TAG, "current_page=" + page + " name=" + name + " size=" + size + " extract_file=" + extractFileCount + " side=" + side.getValue());

            // initPagerAdapter의 기능이다.
            pager.setAdapter(pagerAdapter);// setAdapter이후에 imageList를 변경하면 항상 notify해주어야 한다.

            // zip 파일을 로딩한다.
            final ArrayList<ExplorerItem> imageList = zipLoader.load(this, path, listener, extractFileCount, side, false);
            if (imageList.size() <= 0) {
                AlertHelper.showAlert(this, "알림", "압축(ZIP) 파일에 이미지가 없습니다.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }, null, true);
                return;
            }
            pagerAdapter.setImageList(imageList);
            pagerAdapter.notifyDataSetChanged();

            pager.setCurrentItem(page);

            seekPage.setMax(imageList.size());
            seekPage.setProgress(1);
        }
    }

    private void updateTopSidePanelColor() {
        ImageView iv;
        TextView tv;
        switch (side) {
            case LEFT:
                iv = (ImageView) findViewById(R.id.img_left);
                tv = (TextView) findViewById(R.id.text_left);
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_orange_light));
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light));

                // 나머지 두개를 꺼주어야 한다.
                iv = (ImageView) findViewById(R.id.img_right);
                tv = (TextView) findViewById(R.id.text_right);
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.white));

                iv = (ImageView) findViewById(R.id.img_both);
                tv = (TextView) findViewById(R.id.text_both);
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                break;
            case RIGHT:
                iv = (ImageView) findViewById(R.id.img_right);
                tv = (TextView) findViewById(R.id.text_right);
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_orange_light));
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light));

                // 나머지 두개를 꺼주어야 한다.
                iv = (ImageView) findViewById(R.id.img_left);
                tv = (TextView) findViewById(R.id.text_left);
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.white));

                iv = (ImageView) findViewById(R.id.img_both);
                tv = (TextView) findViewById(R.id.text_both);
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                break;
            case SIDE_ALL:
                iv = (ImageView) findViewById(R.id.img_both);
                tv = (TextView) findViewById(R.id.text_both);
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_orange_light));
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light));

                // 나머지 두개를 꺼주어야 한다.
                iv = (ImageView) findViewById(R.id.img_right);
                tv = (TextView) findViewById(R.id.text_right);
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.white));

                iv = (ImageView) findViewById(R.id.img_left);
                tv = (TextView) findViewById(R.id.text_left);
                iv.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                break;
        }
    }

    private void updatePageSide() {
        Log.d(TAG, "updatePageSide");

        // Task가 실행중이면 pause
        // 그리고 리스트에서 좌우를 변경함
        // 이후 task에서는 변경된 것으로 작업함
        if (!zipExtractCompleted) {
            zipLoader.pause();
        }

        // 전부 초기화 한다.
        // 초기화 하기전에 task를 전부 stop한다.
        pagerAdapter.stopAllTasks();
        pager.removeAllViews();
        BitmapCache.recyclePage();
        BitmapCache.recycleBitmap();

        // 데이터를 업데이트 하자.
        // 좌우를 변경한다.
        //final ArrayList<ExplorerItem> imageList = pagerAdapter.getImageList();
        final ArrayList<ExplorerItem> imageList = (ArrayList<ExplorerItem>) pagerAdapter.getImageList().clone();
        Log.e(TAG, "updatePageSide imageList size=" + imageList.size());
        final ArrayList<ExplorerItem> newImageList = new ArrayList<>();

        for (int i = 0; i < imageList.size(); i++) {
            final ExplorerItem item = imageList.get(i);
//            Log.e(TAG, "updatePageSide i=" + i);

            // 잘려진 데이터는 둘중 하나를 삭제한다.
            if (side == SIDE_ALL) {
                if (item.side != SIDE_ALL) {
                    // 둘중하나는 삭제 해야 함
                    // 앞에꺼가 있는지 확인하고 삭제하자
                    if (i == 0)
                        continue;

                    final ExplorerItem item1 = imageList.get(i - 1);

                    // 같은 파일이면...
                    if (item.path.equals(item1.path)) {
                        // 2번째것을 삭제하고, 1번째것은 값을 변경하자
                        final ExplorerItem newItem = (ExplorerItem) item.clone();
                        newItem.side = SIDE_ALL;
                        newImageList.add(newItem);
                        Log.e(TAG, "updatePageSide equal to SIDE_ALL i=" + i);
                    } else {
                        Log.e(TAG, "item=" + item.path + " item1=" + item1.path);
                    }
                } else {// 이미 SIDE_ALL이면 그냥 더하자
                    final ExplorerItem newItem = (ExplorerItem) item.clone();
                    newImageList.add(newItem);
                }
            } else {// 좌우 변경, 강제 BOTH에서 잘라야 할 것이라면... (side = LEFT or RIGHT)
                // 같은 파일명을 공유하는 애들끼리 LEFT, RIGHT 순서를 체크한 후에 바꿀 필요가 있을 경우에 바꾸자.
                // 현재 포지션은 바뀌지 않는다.
                if (item.side == SIDE_ALL) {
                    // 원래 잘려야할 애들이라면 잘라주어야 한다.
                    if (item.width > item.height) {
                        final ExplorerItem left = (ExplorerItem) item.clone();
                        final ExplorerItem right = (ExplorerItem) item.clone();
                        left.side = LEFT;
                        right.side = RIGHT;

                        if (side == LEFT) {
                            newImageList.add(left);
                            newImageList.add(right);
                        } else if (side == RIGHT) {
                            newImageList.add(right);
                            newImageList.add(left);
                        }
                    } else {
                        // 잘려야 될 애들이 아니면 그냥 넣어준다.
                        final ExplorerItem newItem = (ExplorerItem) item.clone();
                        newImageList.add(newItem);
                    }
                } else {
                    if (i == 0)
                        continue;

                    final ExplorerItem item1 = imageList.get(i - 1);

                    // 같은 파일일 경우 좌우 순서를 바꿈
                    if (item.path.equals(item1.path)) {
                        final ExplorerItem left = (ExplorerItem) item.clone();
                        final ExplorerItem right = (ExplorerItem) item.clone();
                        left.side = LEFT;
                        right.side = RIGHT;

                        if (side == LEFT) {
                            newImageList.add(left);
                            newImageList.add(right);
                        } else if (side == RIGHT) {
                            newImageList.add(right);
                            newImageList.add(left);
                        }
                    }
                }
            }
        }

        Log.d(TAG, "updatePageSide imageList END");

        // 단순 좌우 변경인지, split 변경인지 확인한다.
        final int position = pager.getCurrentItem();
        final String lastPath = imageList.get(position).path;

        // setAdapter를 다시 해줘야 모든 item이 다시 instantiate 된다.
        pagerAdapter.setImageList(newImageList);
        pagerAdapter.notifyDataSetChanged();
        pager.setAdapter(pagerAdapter);

        Log.e(TAG, "updatePageSide newImageList size=" + newImageList.size());
        Log.e(TAG, "updatePageSide newImageList 0=" + newImageList.get(0));
        Log.e(TAG, "updatePageSide newImageList 1=" + newImageList.get(1));
        Log.d(TAG, "updatePageSide notifyDataSetChanged");

        if (lastSide == SIDE_ALL || side == SIDE_ALL) {
            // 페이지 연산을 파일명 단위로 한다.
            int i;
            for (i = 0; i < newImageList.size(); i++) {
                if (newImageList.get(i).path.equals(lastPath)) {
                    break;
                }
            }
            pager.setCurrentItem(i);
        } else {
            pager.setCurrentItem(position);
        }
        Log.d(TAG, "updatePageSide setCurrentItem END");

        if (!zipExtractCompleted) {
            zipLoader.setZipImageList((ArrayList<ExplorerItem>) newImageList.clone());
            zipLoader.setSide(side);
            zipLoader.resume();
        }
    }

    @Override
    public void setFullscreen(boolean fullscreen) {
        super.setFullscreen(fullscreen);

        if (!fullscreen) {
            // top_side_panel을 보이게 하자
            final LinearLayout topOptionPanel = (LinearLayout) findViewById(R.id.panel_top_option);
            topOptionPanel.setVisibility(View.VISIBLE);

            final LinearLayout layoutLeft = (LinearLayout) findViewById(R.id.layout_left);
            layoutLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (side == LEFT)
                        return;
                    changeSide(LEFT);
                    updateTopSidePanelColor();
                    updatePageSide();
                }
            });
            final LinearLayout layoutRight = (LinearLayout) findViewById(R.id.layout_right);
            layoutRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (side == RIGHT)
                        return;
                    changeSide(RIGHT);
                    updateTopSidePanelColor();
                    updatePageSide();
                }
            });

            final LinearLayout layoutBoth = (LinearLayout) findViewById(R.id.layout_both);
            layoutBoth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (side == SIDE_ALL)
                        return;
                    changeSide(SIDE_ALL);
                    updateTopSidePanelColor();
                    updatePageSide();
                }
            });
        }
    }
}
