package com.duongame.explorer.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.duongame.explorer.R;
import com.duongame.explorer.bitmap.BitmapCacheManager;
import com.duongame.explorer.bitmap.BitmapLoader;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-17.
 */
//TODO: Zip파일 양면 읽기용으로 상속받아야 함. Pdf 파일 버전으로 따로 만들어야 함.
public class ExplorerPagerAdapter extends PagerAdapter {
    private static final String TAG = "ExplorerPagerAdapter";

    private ArrayList<ExplorerFileItem> imageList;
    private Activity context;

    public static class PreloadBitmapTask extends AsyncTask<String, Void, Bitmap> {
        private int width, height;

        public PreloadBitmapTask(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            for (int i = 0; i < params.length; i++) {
                final String path = params[i];
//                Log.d(TAG, "preload path=" + path);
                Bitmap bitmap = BitmapCacheManager.getBitmap(path);
                if (bitmap == null) {
                    BitmapFactory.Options options = BitmapLoader.decodeBounds(path);
                    float bitmapRatio = (float) options.outHeight / (float) options.outWidth;
                    float screenRatio = (float) height / (float) width;

                    if (screenRatio > bitmapRatio) {
                        height = (int) (width * bitmapRatio);
                    } else {
                        height = (int) (width * screenRatio);
                    }

                    bitmap = BitmapLoader.decodeSampleBitmapFromFile(path, width, height);
                    BitmapCacheManager.setBitmap(path, bitmap);
//                    Log.d(TAG, "preload cache path=" + path);
                }
            }
            return null;
        }
    }

    public static class RemoveBitmapTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            for (int i = 0; i < params.length; i++) {
                final String path = params[i];
                BitmapCacheManager.removeBitmap(path);
            }
            return null;
        }
    }

    public static class LoadBitmapTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private int width, height;

        public LoadBitmapTask(ImageView imageView, int width, int height) {
//            Log.d(TAG, "LoadBitmapTask ctor");
            imageViewReference = new WeakReference<ImageView>(imageView);
            this.width = width;
            this.height = height;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
//            Log.d(TAG, "LoadBitmapTask doInBackground");
            final String path = params[0];

            Bitmap bitmap = BitmapCacheManager.getBitmap(path);
            if (bitmap == null) {
                BitmapFactory.Options options = BitmapLoader.decodeBounds(path);
                float bitmapRatio = (float) options.outHeight / (float) options.outWidth;
                float screenRatio = (float) height / (float) width;

                if (screenRatio > bitmapRatio) {
                    height = (int) (width * bitmapRatio);
                } else {
                    height = (int) (width * screenRatio);
                }

                bitmap = BitmapLoader.decodeSampleBitmapFromFile(path, width, height);
                BitmapCacheManager.setBitmap(path, bitmap);
//                Log.d(TAG, "LoadBitmapTask path=" + path);
            } else {
//                Log.d(TAG, "LoadBitmapTask cache path=" + path);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    public ExplorerPagerAdapter(Activity context) {
        this.context = context;
    }

    public void setImageList(ArrayList<ExplorerFileItem> imageList) {
        this.imageList = imageList;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, int position) {
//        Log.d(TAG, "instantiateItem position=" + position);

        final ViewGroup rootView = (ViewGroup) context.getLayoutInflater().inflate(R.layout.viewer_page, container, false);
        final ImageView imageView = (ImageView) rootView.findViewById(R.id.image_viewer);

        final String path = imageList.get(position).path;

        final TextView textPath = (TextView) rootView.findViewById(R.id.text_path);
        textPath.setText(path);

        container.addView(rootView);

        final int width = container.getWidth();
        final int height = container.getHeight();
//        Log.d(TAG, "instantiateItem width=" + width + " height=" + height);

        if (width == 0 || height == 0) {
            container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int width = container.getWidth();
                    int height = container.getHeight();
//                    Log.d(TAG, "onGlobalLayout width=" + width + " height=" + height);

                    LoadBitmapTask task = new LoadBitmapTask(imageView, width, height);
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);
//                    Log.d(TAG, "LoadBitmapTask execute");

                    container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });

        } else {
            LoadBitmapTask task = new LoadBitmapTask(imageView, width, height);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);
        }

        return rootView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        Log.d(TAG, "destroyItem position=" + position);
        container.removeView((View) object);

        ViewGroup rootView = (ViewGroup) object;
        ImageView imageView = (ImageView) rootView.findViewById(R.id.image_viewer);
        imageView.setImageBitmap(null);
    }

    private void runPreloadTask(int position, int width, int height) {
        ArrayList<String> preloadList = new ArrayList<String>();
        if (position + 2 < imageList.size()) {
            preloadList.add(imageList.get(position + 2).path);
        }
        if (position + 3 < imageList.size()) {
            preloadList.add(imageList.get(position + 3).path);
        }
        if (position - 2 >= 0) {
            preloadList.add(imageList.get(position - 2).path);
        }
        String[] preloadArray = new String[preloadList.size()];
        preloadList.toArray(preloadArray);

        PreloadBitmapTask task = new PreloadBitmapTask(width, height);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, preloadArray);
    }

    private void runRemoveTask(int position) {
        ArrayList<String> removeList = new ArrayList<String>();
        if (position - 3 >= 0) {
            removeList.add(imageList.get(position - 3).path);
        }
        if (position + 4 < imageList.size()) {
            removeList.add(imageList.get(position + 4).path);
        }
        String[] removeArray = new String[removeList.size()];
        removeList.toArray(removeArray);

        RemoveBitmapTask task = new RemoveBitmapTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, removeArray);
    }

    @Override
    public void setPrimaryItem(final ViewGroup container, final int position, Object object) {
//        Log.d(TAG, "setPrimaryItem position=" + position);
        final int width = container.getWidth();
        final int height = container.getHeight();
//        Log.d(TAG, "setPrimaryItem width=" + width + " height=" + height);

        // preload bitmap task
        if (width == 0 || height == 0) {
            container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int width = container.getWidth();
                    int height = container.getHeight();
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
        return imageList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
