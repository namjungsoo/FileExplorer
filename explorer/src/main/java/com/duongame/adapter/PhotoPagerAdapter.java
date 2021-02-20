package com.duongame.adapter;

import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.duongame.R;
import com.duongame.activity.viewer.PagerActivity;
import com.duongame.attacher.ImageViewAttacher;
import com.duongame.bitmap.BitmapCacheManager;
import com.duongame.file.FileHelper;
import com.duongame.helper.JLog;
import com.duongame.listener.PagerOnTouchListener;
import com.duongame.task.bitmap.LoadBitmapTask;
import com.duongame.task.bitmap.LoadGifTask;
import com.felipecsl.gifimageview.library.GifImageView;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-12-25.
 */

public class PhotoPagerAdapter extends ViewerPagerAdapter {
    private static final String TAG = "PhotoPagerAdapter";

    private ArrayList<AsyncTask> taskList = new ArrayList<>();
    private int lastPosition = -1;
    private boolean useGifAni = false;// true: BitmapTask에서 읽음

    PagerOnTouchListener mPagerOnTouchListener;

    public PhotoPagerAdapter(PagerActivity context, boolean useGifAni) {
        super(context);
        mPagerOnTouchListener = new PagerOnTouchListener(context);
        this.useGifAni = useGifAni;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        JLog.e(TAG, "instantiateItem " + position);

        final FrameLayout rootView = (FrameLayout) context.getLayoutInflater().inflate(R.layout.page_viewer, container, false);
        final ImageView imageView = rootView.findViewById(R.id.image_viewer);

//        rootView.setBaseOnTouchListener(mPagerOnTouchListener);

        container.addView(rootView);

        final int width = container.getWidth();
        final int height = container.getHeight();

        if (width == 0 || height == 0) {
            container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    final int width = container.getWidth();
                    final int height = container.getHeight();

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

        // GIF는 여기서 읽지 않는다.
        // useGifAni: 애니메이션이 있을때는 외부에서 쓰레드를 통해서 렌더링 하므로 여기서는 미리 gif를 로딩해 놓지 않는다.
        if (useGifAni && FileHelper.isGifImage(item.path)) {
//            Glide.with(context).load(new File(item.path)).into(imageView);
            // 일단 애니메이션이 있는지를 체크해보고 없으면 내가 로딩하자
            return;
        }

        boolean loadingByOther = LoadBitmapTask.isCurrentLoadingBitmap(item.path);
        if(!loadingByOther)
            LoadBitmapTask.setCurrentLoadingBitmap(item.path);
        final LoadBitmapTask task = new LoadBitmapTask(context, imageView, width, height, exifRotation, position, loadingByOther);

        // THREAD_POOL을 사용하는 이유는 압축을 풀면서 동적으로 로딩을 해야 하기 때문이다.
        // 그런데 양쪽 페이지로 되어 있는 만화 같은 경우 하나의 PNG를 읽으면 양쪽 페이지가 나오는데
        // 두개의 쓰레드가 경쟁할때가 있다.
        // 이를 위해서 쓰레드풀을 분리해야할 수 있다.
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item);

        // viewer_page layout에 장착한다.
//        imageView.setOnTouchListener(mPagerOnTouchListener);
        item.attacher = new ImageViewAttacher(imageView);
        item.attacher.setActivity(context);
        taskList.add(task);

        updateColorFilter(imageView);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        JLog.e(TAG, "destroyItem position=" + position);
        // 공통 사항
        container.removeView((View) object);

        // 이미지뷰에서 bitmap을 끊어주자.
        final ViewGroup rootView = (ViewGroup) object;
        if (rootView == null)
            return;

        final ImageView imageView = rootView.findViewById(R.id.image_viewer);
        if (imageView == null)
            return;

        imageView.setImageBitmap(null);

        //FIX: OOM
        // 현재 위치에 해당하는 bitmap을 찾아서 캐쉬 삭제 해주자.
        deleteItemBitmapCache(position);
    }

    private void deleteItemBitmapCache(int position) {
        try {
            ExplorerItem item = getImageList().get(position);
            if (item == null)
                return;

            // bitmap부터 체크
            if (item.side == ExplorerItem.SIDE_ALL) {
                BitmapCacheManager.removeBitmap(item.path);
            } else {
                BitmapCacheManager.removePage(item.path);
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void setPrimaryItem(final ViewGroup container, final int position, Object object) {
//        JLog.e(TAG, "setPrimaryItem " + position);

        final int width = container.getWidth();
        final int height = container.getHeight();

        if (position != lastPosition) {
            lastPosition = position;

            // preload bitmap task
            if (width == 0 || height == 0) {
                container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        final int width = container.getWidth();
                        final int height = container.getHeight();

                        //FIX: OOM
//                        preloadAndRemoveNearBitmap(position, width, height);
                        container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                });

            } else {
                //FIX: OOM
//                preloadAndRemoveNearBitmap(position, width, height);
            }

            updateGifImage(object, position);
        }
    }

    private void updateGifImage(Object object, final int position) {
        // GIF 이미지일 경우
        // 메모리에 사라졌다가 재 로딩일 경우에 애니메이션이 잘 안된다.
        final ViewGroup rootView = (ViewGroup) object;
        if (rootView == null)
            return;

        final PagerActivity pagerActivity = context;
        final GifImageView imageView = rootView.findViewById(R.id.image_viewer);

        if (!useGifAni)
            return;

        if (imageList.get(position).path.endsWith(".gif")) {
            final LoadGifTask task = new LoadGifTask(new LoadGifTask.LoadGifListener() {
                @Override
                public void onSuccess(byte[] data) {
                    // 기존 GIF가 있으면 가져와서 stop해줌
                    pagerActivity.stopGifAnimation();

                    //imageView.stopAnimation();
                    imageView.setBytes(data);
                    imageView.startAnimation();

                    // 성공이면 imageView를 저장해 놓음
                    pagerActivity.setGifImageView(imageView);

                    ExplorerItem item = getImageList().get(position);
                    if (item != null) {
                        item.width = imageView.getGifWidth();
                        item.height = imageView.getGifHeight();
                    }
                    context.updateInfo(position);
                }

                @Override
                public void onFail() {
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

    public void stopAllTasks() {
        for (AsyncTask task : taskList) {
            task.cancel(true);
        }
        taskList.clear();
    }
}
