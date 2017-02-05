package com.duongame.viewer.adapter;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.duongame.explorer.R;
import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.bitmap.BitmapCache;
import com.duongame.explorer.task.LoadBitmapTask;
import com.duongame.explorer.task.LoadGifTask;
import com.duongame.explorer.task.RemoveAndPreloadTask;
import com.duongame.viewer.activity.PagerActivity;
import com.felipecsl.gifimageview.library.GifImageView;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-12-25.
 */

public class PhotoPagerAdapter extends ViewerPagerAdapter {
    private static final String TAG = "PhotoPagerAdapter";

    private ArrayList<AsyncTask> taskList = new ArrayList<>();
    private int lastPosition = -1;

    public PhotoPagerAdapter(Activity context) {
        super(context);
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        Log.w(TAG, "instantiateItem position=" + position);

        final ViewGroup rootView = (ViewGroup) context.getLayoutInflater().inflate(R.layout.viewer_page, container, false);
        final ImageView imageView = (ImageView) rootView.findViewById(R.id.image_viewer);

        container.addView(rootView);

        final int width = container.getWidth();
        final int height = container.getHeight();
//        Log.d(TAG, "instantiateItem width=" + width + " height=" + height);

        if (width == 0 || height == 0) {
            container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    final int width = container.getWidth();
                    final int height = container.getHeight();
//                    Log.d(TAG, "onGlobalLayout width=" + width + " height=" + height);
//                    Log.d(TAG, "LoadBitmapTask execute");

                    loadCurrentBitmap(position, imageView, width, height);

                    container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });

        } else {
            loadCurrentBitmap(position, imageView, width, height);
        }

        return rootView;
    }

    private void loadCurrentBitmap(int position, ImageView imageView, int width, int height) {
        final ExplorerItem item = imageList.get(position);
        final LoadBitmapTask task = new LoadBitmapTask(imageView, width, height, exifRotation);

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item);
        taskList.add(task);
    }

    private void preloadAndRemoveNearBitmap(int position, int width, int height) {
        final ExplorerItem[] preloadArray = getPreloadArray(position, width, height);
        final ExplorerItem[] removeArray = getRemoveArray(position);
        final RemoveAndPreloadTask task = new RemoveAndPreloadTask(width, height, exifRotation);

        task.setRemoveArray(removeArray);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, preloadArray);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        Log.w(TAG, "destroyItem position=" + position);
        container.removeView((View) object);

        final ViewGroup rootView = (ViewGroup) object;
        final ImageView imageView = (ImageView) rootView.findViewById(R.id.image_viewer);
        imageView.setImageBitmap(null);
    }

    // 캐쉬에 비트맵이나 페이지가 있는지를 리턴
    private boolean checkCacheExistBitmapOrPage(ExplorerItem item) {
        if (item.side == ExplorerItem.Side.SIDE_ALL) {
            return BitmapCache.getBitmap(item.path) == null ? false : true;
        } else {
            return BitmapCache.getPage(BitmapCache.changePathToPage(item)) == null ? false : true;
        }
    }

    @Override
    public void setPrimaryItem(final ViewGroup container, final int position, Object object) {
        Log.w(TAG, "setPrimaryItem position=" + position);
        final int width = container.getWidth();
        final int height = container.getHeight();
//        Log.d(TAG, "setPrimaryItem width=" + width + " height=" + height);

        if (position != lastPosition) {
            lastPosition = position;
            Log.w(TAG, "setPrimaryItem position changed");

            // preload bitmap task
            if (width == 0 || height == 0) {
                container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        final int width = container.getWidth();
                        final int height = container.getHeight();
//                    Log.d(TAG, "onGlobalLayout width=" + width + " height=" + height);

                        preloadAndRemoveNearBitmap(position, width, height);

                        container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                });

            } else {
                preloadAndRemoveNearBitmap(position, width, height);
            }

            // GIF 이미지일 경우
            // 메모리에 사라졌다가 재 로딩일 경우에 애니메이션이 잘 안된다.
            final ViewGroup rootView = (ViewGroup)object;

            final PagerActivity pagerActivity = (PagerActivity) context;
            final GifImageView imageView = (GifImageView) rootView.findViewById(R.id.image_viewer);
            Log.w(TAG, "imageView tag=" + imageView.getTag());

            if (imageList.get(position).path.toLowerCase().endsWith(".gif")) {
                final LoadGifTask task = new LoadGifTask(new LoadGifTask.LoadGifListener() {
                    @Override
                    public void onSuccess(byte[] data) {
                        Log.w(TAG, "onSuccess path=" + imageList.get(position).path);

                        // 기존 GIF가 있으면 가져와서 stop해줌
                        pagerActivity.stopGifAnimation();

//                        imageView.stopAnimation();
                        imageView.setBytes(data);
                        imageView.startAnimation();

                        // 성공이면 imageView를 저장해 놓음
                        pagerActivity.setGifImageView(imageView);
                    }

                    @Override
                    public void onFail() {
                        Log.e(TAG, "onFail " + imageList.get(position).path);

                        // 기존 GIF가 있으면 가져와서 stop해줌
                        pagerActivity.stopGifAnimation();
                        pagerActivity.setGifImageView(null);
                    }
                });
                task.execute(imageList.get(position).path);
            } else {// GIF가 아니면
                // 기존 GIF가 있으면 가져와서 stop해줌
                pagerActivity.stopGifAnimation();
                pagerActivity.setGifImageView(null);
            }
        }
    }

    private void addPreloadListWithPriority(final ArrayList<ExplorerItem> preloadList, final ExplorerItem item, int i, int BEGIN) {
        if (i == BEGIN) {// 시작인 것만 0(최우선 순위), 나머지는 1
            item.priority = 0;
        } else {
            item.priority = 1;
        }
        preloadList.add(item);
    }

    private ExplorerItem[] getPreloadArray(int position, int width, int height) {
        if (imageList == null)
            return null;

        final ArrayList<ExplorerItem> preloadList = new ArrayList<ExplorerItem>();

        final int NEXT_BEGIN = 2;
        final int NEXT_END = 3;

        // 앞쪽(오른쪽)의 이미지를 읽는다.
        for (int i = NEXT_BEGIN; i <= NEXT_END; i++) {
            final int index = position + i;
            if (index < imageList.size()) {
                final ExplorerItem item = imageList.get(index);

                // 전체 모드가 아니면 바로 전 이미지를 체크 한다.
                if (item.side == ExplorerItem.Side.SIDE_ALL) {
                    if (!checkCacheExistBitmapOrPage(item)) {
                        addPreloadListWithPriority(preloadList, item, i, NEXT_BEGIN);
                    }
                } else {
                    // 무조건 바로전 파일이 있다.
                    // 왜냐면 i(BEGIN)가 최소 1이기 때문이다.
                    ExplorerItem item1 = imageList.get(index - 1);
                    if (!item.path.equals(item1.path)) {
                        if (!checkCacheExistBitmapOrPage(item)) {
                            addPreloadListWithPriority(preloadList, item, i, NEXT_BEGIN);
                        }
                    }
                }
            }
        }

        final int PREV_BEGIN = 2;
        final int PREV_END = 2;

        // 뒤쪽(왼쪽)의 이미지를 읽는다.
        for (int i = PREV_BEGIN; i <= PREV_END; i++) {
            final int index = position - i;
            if (index >= 0) {
                final ExplorerItem item = imageList.get(index);

                // 전체 모드가 아니면 바로 전 이미지를 체크 한다.
                if (item.side == ExplorerItem.Side.SIDE_ALL) {
                    if (!checkCacheExistBitmapOrPage(item)) {
                        addPreloadListWithPriority(preloadList, item, i, PREV_BEGIN);
                    }
                } else {
                    if (index - 1 >= 0) {// 바로 전 파일이 있으면
                        ExplorerItem item1 = imageList.get(index - 1);
                        if (!item.path.equals(item1.path)) {
                            if (!checkCacheExistBitmapOrPage(item)) {
                                addPreloadListWithPriority(preloadList, item, i, PREV_BEGIN);
                            }
                        }
                    } else {// 없으면 내 파일을 읽는다
                        if (!checkCacheExistBitmapOrPage(item)) {
                            addPreloadListWithPriority(preloadList, item, i, PREV_BEGIN);
                        }
                    }
                }
            }
        }

        if (preloadList.size() <= 0)
            return null;

//        Collections.sort(preloadList, new FileHelper.FilePriorityComparator());

        final ExplorerItem[] preloadArray = new ExplorerItem[preloadList.size()];
        preloadList.toArray(preloadArray);

        return preloadArray;
    }

    private ExplorerItem[] getRemoveArray(int position) {
        if (imageList == null)
            return null;

        // split일 경우에는 현재 reload된 것에 bitmap이 사용되는지 안되는지 확인해야 한다.
        final ArrayList<ExplorerItem> removeList = new ArrayList<ExplorerItem>();

        // 지우는 것은 순서가 필요 없다.
        final int NEXT_BEGIN = 3;
        final int NEXT_END = 4;

        for (int i = NEXT_BEGIN; i <= NEXT_END; i++) {
            final int index = position + i;
            if (index >= 0 && imageList.size() > index) {
                final ExplorerItem item = imageList.get(index);
                removeList.add(item);
            }
        }

        final int PREV_BEGIN = 3;
        final int PREV_END = 3;

        for (int i = PREV_BEGIN; i <= PREV_END; i++) {
            final int index = position - i;
            if (index >= 0 && imageList.size() > index) {
                final ExplorerItem item = imageList.get(index);
                removeList.add(item);
            }
        }

        if (removeList.size() <= 0)
            return null;

        final ExplorerItem[] removeArray = new ExplorerItem[removeList.size()];
        removeList.toArray(removeArray);

        return removeArray;
    }

    public void stopAllTasks() {
        for (AsyncTask task : taskList) {
            task.cancel(true);
        }
        taskList.clear();
    }
}
