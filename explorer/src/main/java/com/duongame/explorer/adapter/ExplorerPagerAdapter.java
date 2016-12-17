package com.duongame.explorer.adapter;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.duongame.explorer.R;
import com.duongame.explorer.task.LoadBitmapTask;
import com.duongame.explorer.task.PreloadBitmapTask;
import com.duongame.explorer.task.RemoveBitmapTask;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-17.
 */
//TODO: Zip파일 양면 읽기용으로 상속받아야 함. Pdf 파일 버전으로 따로 만들어야 함.
public class ExplorerPagerAdapter extends PagerAdapter {
    private static final String TAG = "ExplorerPagerAdapter";

    private ArrayList<AsyncTask> taskList = new ArrayList<>();
    private ArrayList<ExplorerFileItem> imageList;
    private Activity context;
    private int maxIndex = 0;

    private boolean exifRotation = true;
    private boolean splitBitmap = false;

    public void setExifRotation(boolean rotation) {
        exifRotation = rotation;
    }

    public boolean getExifRotation() {
        return exifRotation;
    }
    
    public void setMaxIndex(int index) {
        maxIndex = index;
    }

    public int getMaxIndex() {
        return maxIndex;
    }

    public void setSplitBitmap(boolean splitBitmap) {
        this.splitBitmap = splitBitmap;
    }

    public boolean getSplitBitmap() {
        return splitBitmap;
    }

    public ExplorerPagerAdapter(Activity context) {
        this.context = context;
    }

    public void setImageList(ArrayList<ExplorerFileItem> imageList) {
        this.imageList = imageList;
    }

    public ArrayList<ExplorerFileItem> getImageList() {
        return imageList;
    }

    public void stopAllTasks() {
        for (AsyncTask task : taskList) {
            task.cancel(true);
        }
        taskList.clear();
    }

    @Override
    public Object instantiateItem(final ViewGroup container, int position) {
        Log.d(TAG, "instantiateItem position=" + position);

        final ViewGroup rootView = (ViewGroup) context.getLayoutInflater().inflate(R.layout.viewer_page, container, false);
        final ImageView imageView = (ImageView) rootView.findViewById(R.id.image_viewer);

        final ExplorerFileItem item = imageList.get(position);

        final TextView textPath = (TextView) rootView.findViewById(R.id.text_path);
        textPath.setText(item.path);

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

                    final LoadBitmapTask task = new LoadBitmapTask(imageView, width, height, exifRotation, splitBitmap);
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item);
//                    Log.d(TAG, "LoadBitmapTask execute");
                    taskList.add(task);

                    container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });

        } else {
            final LoadBitmapTask task = new LoadBitmapTask(imageView, width, height, exifRotation, splitBitmap);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item);
            taskList.add(task);
        }

        return rootView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        Log.d(TAG, "destroyItem position=" + position);
        container.removeView((View) object);

        final ViewGroup rootView = (ViewGroup) object;
        final ImageView imageView = (ImageView) rootView.findViewById(R.id.image_viewer);
        imageView.setImageBitmap(null);
    }

    private void runPreloadTask(int position, int width, int height) {
        if(imageList == null)
            return;

        final ArrayList<ExplorerFileItem> preloadList = new ArrayList<ExplorerFileItem>();

        if (position + 2 < imageList.size()) {
            preloadList.add(imageList.get(position + 2));
        }
        if (position + 3 < imageList.size()) {
            preloadList.add(imageList.get(position + 3));
        }
        if (position - 2 >= 0) {
            preloadList.add(imageList.get(position - 2));
        }

        final ExplorerFileItem[] preloadArray = new ExplorerFileItem[preloadList.size()];
        preloadList.toArray(preloadArray);

        final PreloadBitmapTask task = new PreloadBitmapTask(width, height, exifRotation, splitBitmap);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, preloadArray);
        taskList.add(task);
    }

    private void runRemoveTask(int position) {
        if(imageList == null)
            return;

        // split일 경우에는 현재 reload된 것에 bitmap이 사용되는지 안되는지 확인해라
        final ArrayList<String> removeList = new ArrayList<String>();

        if (position - 3 >= 0) {// -2번이 같은 비트맵인지 확인해라
            ExplorerFileItem item = imageList.get(position - 3);
            ExplorerFileItem item2 = imageList.get(position - 2);
            if(!item.path.equals(item2.path)) {
                removeList.add(item.path);
            }
        }
        if (position + 4 < imageList.size()) {// 3번이 같은 비트맵인지 확인해라
            ExplorerFileItem item = imageList.get(position + 4);
            ExplorerFileItem item3 = imageList.get(position + 3);
            if(!item.path.equals(item3.path)) {
                removeList.add(item.path);
            }
        }

        final String[] removeArray = new String[removeList.size()];
        removeList.toArray(removeArray);

        final RemoveBitmapTask task = new RemoveBitmapTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, removeArray);
        taskList.add(task);
    }

    @Override
    public void setPrimaryItem(final ViewGroup container, final int position, Object object) {
        Log.d(TAG, "setPrimaryItem position=" + position);
        final int width = container.getWidth();
        final int height = container.getHeight();
//        Log.d(TAG, "setPrimaryItem width=" + width + " height=" + height);

        // preload bitmap task
        if (width == 0 || height == 0) {
            container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    final int width = container.getWidth();
                    final int height = container.getHeight();
//                    Log.d(TAG, "onGlobalLayout width=" + width + " height=" + height);

                    runPreloadTask(position, width, height);
                    container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });

        } else {
            runPreloadTask(position, width, height);
        }

        // remove bitmap task
        runRemoveTask(position);
    }

    @Override
    public int getCount() {
        if(imageList == null)
            return 0;

//        Log.d(TAG, "getCount="+imageList.size());
        return imageList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
