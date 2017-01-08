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
import com.duongame.explorer.task.RemoveAndPreloadTask;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-12-25.
 */

public class PhotoPagerAdapter extends ViewerPagerAdapter {
    private static final String TAG = "PhotoPagerAdapter";

    private ArrayList<AsyncTask> taskList = new ArrayList<>();
    private int lastPosition = -1;

//    public boolean getExifRotation() {
//        return exifRotation;
//    }
//
//    public void setMaxIndex(int index) {
//        maxIndex = index;
//    }
//
//    public int getMaxIndex() {
//        return maxIndex;
//    }

    public PhotoPagerAdapter(Activity context) {
        super(context);
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        Log.w(TAG, "instantiateItem position=" + position);

        final ViewGroup rootView = (ViewGroup) context.getLayoutInflater().inflate(R.layout.viewer_page, container, false);
        final ImageView imageView = (ImageView) rootView.findViewById(R.id.image_viewer);

//        final TextView textPath = (TextView) rootView.findViewById(R.id.text_path);
//        textPath.setText(item.path);

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
//                    final LoadBitmapTask task = new LoadBitmapTask(imageView, width, height, exifRotation);
//                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item);
//                    taskList.add(task);

                    container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });

        } else {
            loadCurrentBitmap(position, imageView, width, height);
//            final LoadBitmapTask task = new LoadBitmapTask(imageView, width, height, exifRotation);
//            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item);
//            taskList.add(task);
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

    private boolean checkBitmapOrPage(ExplorerItem item) {
        if (item.side == ExplorerItem.Side.SIDE_ALL) {
            return BitmapCache.getBitmap(item.path) == null ? false : true;
        } else {
            return BitmapCache.getPage(BitmapCache.changePathToPage(item)) == null ? false : true;
        }
    }

    @Override
    public void setPrimaryItem(final ViewGroup container, final int position, Object object) {
//        Log.d(TAG, "setPrimaryItem position=" + position);
        final int width = container.getWidth();
        final int height = container.getHeight();
//        Log.d(TAG, "setPrimaryItem width=" + width + " height=" + height);

        if(position != lastPosition) {
            lastPosition = position;

            // preload bitmap task
            if (width == 0 || height == 0) {
                container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        final int width = container.getWidth();
                        final int height = container.getHeight();
//                    Log.d(TAG, "onGlobalLayout width=" + width + " height=" + height);

                        //runPreloadTask(position, width, height);
                        preloadAndRemoveNearBitmap(position, width, height);
//                    final ExplorerItem[] preloadArray = getPreloadArray(position, width, height);
//                    final ExplorerItem[] removeArray = getRemoveArray(position);
//                    final RemoveAndPreloadTask task = new RemoveAndPreloadTask(width, height, exifRotation);
//                    task.setRemoveArray(removeArray);
//                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, preloadArray);

                        container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                });

            } else {
                //runPreloadTask(position, width, height);
                preloadAndRemoveNearBitmap(position, width, height);
//            final ExplorerItem[] preloadArray = getPreloadArray(position, width, height);
//            final ExplorerItem[] removeArray = getRemoveArray(position);
//            final RemoveAndPreloadTask task = new RemoveAndPreloadTask(width, height, exifRotation);
//            task.setRemoveArray(removeArray);
//            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, preloadArray);
            }
        }

        // remove bitmap task
//        runRemoveTask(position);
    }

    private ExplorerItem[] getPreloadArray(int position, int width, int height) {
        if (imageList == null)
            return null;

        final ArrayList<ExplorerItem> preloadList = new ArrayList<ExplorerItem>();

        if (position + 2 < imageList.size()) {
            ExplorerItem item = imageList.get(position + 2);

            // 전체 모드가 아니면 바로 전 이미지를 체크 한다.
            if (item.side == ExplorerItem.Side.SIDE_ALL) {
                if (!checkBitmapOrPage(item)) {
                    preloadList.add(item);
//                    Log.w(TAG, "getPreloadArray position=" + (position + 2));
                }
            } else {
                ExplorerItem item1 = imageList.get(position + 1);
                if (!item.path.equals(item1.path)) {
                    if (!checkBitmapOrPage(item)) {
                        preloadList.add(item);
//                        Log.w(TAG, "getPreloadArray position=" + (position + 1));
                    }
                }
            }
        }
        if (position + 3 < imageList.size()) {
            ExplorerItem item = imageList.get(position + 3);

            // 전체 모드가 아니면 바로 전 이미지를 체크 한다.
            if (item.side == ExplorerItem.Side.SIDE_ALL) {
                if(!checkBitmapOrPage(item)) {
                    preloadList.add(item);
//                    Log.w(TAG, "getPreloadArray position=" + (position + 3));
                }
            } else {
                ExplorerItem item1 = imageList.get(position + 2);
                if (!item.path.equals(item1.path)) {
                    if(!checkBitmapOrPage(item)) {
                        preloadList.add(item);
//                        Log.w(TAG, "getPreloadArray position=" + (position + 2));
                    }
                }
            }
        }
        if (position - 2 >= 0) {
            ExplorerItem item = imageList.get(position - 2);

            // 전체 모드가 아니면 바로 전 이미지를 체크 한다.
            if (item.side == ExplorerItem.Side.SIDE_ALL) {
                if (!checkBitmapOrPage(item)) {
                    preloadList.add(item);
//                    Log.w(TAG, "getPreloadArray position=" + (position - 2));
                }
            } else {
                if (position - 3 >= 0) {// 바로 전 파일이 있으면
                    ExplorerItem item1 = imageList.get(position - 3);
                    if (!item.path.equals(item1.path)) {
                        if (!checkBitmapOrPage(item)) {
                            preloadList.add(item);
//                            Log.w(TAG, "getPreloadArray position=" + (position - 3));
                        }
                    }
                } else {// 없으면 내 파일을 읽는다
                    if (!checkBitmapOrPage(item)) {
                        preloadList.add(item);
//                        Log.w(TAG, "getPreloadArray position=" + (position - 2));
                    }
                }
            }
        }

        if (preloadList.size() <= 0)
            return null;

        final ExplorerItem[] preloadArray = new ExplorerItem[preloadList.size()];
        preloadList.toArray(preloadArray);

        return preloadArray;
    }

//    private void runPreloadTask(int position, int width, int height) {
//        final ExplorerItem[] preloadArray = getPreloadArray(position, width, height);
//        if (preloadArray == null)
//            return;
//
//        final PreloadBitmapTask task = new PreloadBitmapTask(width, height, exifRotation);
//        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, preloadArray);
//        taskList.add(task);
//    }

    private ExplorerItem[] getRemoveArray(int position) {
        if (imageList == null)
            return null;

        // split일 경우에는 현재 reload된 것에 bitmap이 사용되는지 안되는지 확인해라
        final ArrayList<ExplorerItem> removeList = new ArrayList<ExplorerItem>();

        if (position - 3 >= 0) {
            ExplorerItem item = imageList.get(position - 3);
            removeList.add(item);
//            Log.w(TAG, "getRemoveArray position=" + (position - 3));
        }
        if (position + 3 < imageList.size()) {
            ExplorerItem item = imageList.get(position + 3);
            removeList.add(item);
//            Log.w(TAG, "getRemoveArray position=" + (position + 3));
        }
        if (position + 4 < imageList.size()) {
            ExplorerItem item = imageList.get(position + 4);
            removeList.add(item);
//            Log.w(TAG, "getRemoveArray position=" + (position + 4));
        }

        if (removeList.size() <= 0)
            return null;

        final ExplorerItem[] removeArray = new ExplorerItem[removeList.size()];
        removeList.toArray(removeArray);

        return removeArray;
    }

//    private void runRemoveTask(int position) {
//        final ExplorerItem[] removeArray = getRemoveArray(position);
//        if (removeArray == null)
//            return;
//
//        final RemoveBitmapTask task = new RemoveBitmapTask();
//        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, removeArray);
//        taskList.add(task);
//    }

    public void stopAllTasks() {
        for (AsyncTask task : taskList) {
            task.cancel(true);
        }
        taskList.clear();
    }
}
