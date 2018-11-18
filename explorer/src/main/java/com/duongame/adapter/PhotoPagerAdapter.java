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

        final FrameLayout rootView = (FrameLayout) context.getLayoutInflater().inflate(R.layout.viewer_page, container, false);
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
        final LoadBitmapTask task = new LoadBitmapTask(context, imageView, width, height, exifRotation, useGifAni, position);

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item);

        // viewer_page layout에 장착한다.
//        imageView.setOnTouchListener(mPagerOnTouchListener);
        item.attacher = new ImageViewAttacher(imageView);
        item.attacher.setActivity(context);
        taskList.add(task);
    }

    // 사용안함
//    private void preloadAndRemoveNearBitmap(int position, int width, int height) {
//        final ExplorerItem[] preloadArray = getPreloadArray(position, width, height);
//        final ExplorerItem[] removeArray = getRemoveArray(position);
//        final RemoveAndPreloadBitmapTask task = new RemoveAndPreloadBitmapTask(width, height, exifRotation);
//
//        task.setRemoveArray(removeArray);
//        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, preloadArray);
//    }

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

    // 사용안함
    // 캐쉬에 비트맵이나 페이지가 있는지를 리턴
//    private boolean checkCacheExistBitmapOrPage(ExplorerItem item) {
//        if (item.side == ExplorerItem.SIDE_ALL) {
//            return BitmapCacheManager.getBitmap(item.path) == null ? false : true;
//        } else {
//            return BitmapCacheManager.getPage(BitmapCacheManager.changePathToPage(item)) == null ? false : true;
//        }
//    }

    @Override
    public void setPrimaryItem(final ViewGroup container, final int position, Object object) {
        JLog.e(TAG, "setPrimaryItem " + position);

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

    // 사용안함
//    private void addPreloadListWithPriority(final ArrayList<ExplorerItem> preloadList, final ExplorerItem item, int i, int BEGIN) {
//        if (i == BEGIN) {// 시작인 것만 0(최우선 순위), 나머지는 1
//            item.priority = 0;
//        } else {
//            item.priority = 1;
//        }
//        preloadList.add(item);
//    }


    // 사용안함
    // 다음 인덱스에 해당하는 비트맵을 미리 로딩한다.
    // +1, -1은 setPrimaryItem에서 로딩한다. (offscreen limit이 1이라도 최대 3개를 읽게 되어 있다.)
    // +2~+3
    // -2
//    private ExplorerItem[] getPreloadArray(int position, int width, int height) {
//        if (imageList == null)
//            return null;
//
//        final ArrayList<ExplorerItem> preloadList = new ArrayList<ExplorerItem>();
//
//        final int NEXT_BEGIN = 2;
//        final int NEXT_END = 3;
//
//        // 앞쪽(오른쪽)의 이미지를 읽는다.
//        for (int i = NEXT_BEGIN; i <= NEXT_END; i++) {
//            final int index = position + i;
//            if (index < imageList.size()) {
//                final ExplorerItem item = imageList.get(index);
//
//                // 전체 모드가 아니면 바로 전 이미지를 체크 한다.
//                if (item.side == ExplorerItem.SIDE_ALL) {
//                    if (!checkCacheExistBitmapOrPage(item)) {
//                        addPreloadListWithPriority(preloadList, item, i, NEXT_BEGIN);
//                    }
//                } else {
//                    // 무조건 바로전 파일이 있다.
//                    // 왜냐면 i(BEGIN)가 최소 1이기 때문이다.
//                    ExplorerItem item1 = imageList.get(index - 1);
//                    if (!item.path.equals(item1.path)) {
//                        if (!checkCacheExistBitmapOrPage(item)) {
//                            addPreloadListWithPriority(preloadList, item, i, NEXT_BEGIN);
//                        }
//                    }
//                }
//            }
//        }
//
//        final int PREV_BEGIN = 2;
//        final int PREV_END = 2;
//
//        // 뒤쪽(왼쪽)의 이미지를 읽는다.
//        for (int i = PREV_BEGIN; i <= PREV_END; i++) {
//            final int index = position - i;
//            if (index >= 0) {
//                final ExplorerItem item = imageList.get(index);
//
//                // 전체 모드가 아니면 바로 전 이미지를 체크 한다.
//                if (item.side == ExplorerItem.SIDE_ALL) {
//                    if (!checkCacheExistBitmapOrPage(item)) {
//                        addPreloadListWithPriority(preloadList, item, i, PREV_BEGIN);
//                    }
//                } else {
//                    if (index - 1 >= 0) {// 바로 전 파일이 있으면
//                        ExplorerItem item1 = imageList.get(index - 1);
//                        if (!item.path.equals(item1.path)) {
//                            if (!checkCacheExistBitmapOrPage(item)) {
//                                addPreloadListWithPriority(preloadList, item, i, PREV_BEGIN);
//                            }
//                        }
//                    } else {// 없으면 내 파일을 읽는다
//                        if (!checkCacheExistBitmapOrPage(item)) {
//                            addPreloadListWithPriority(preloadList, item, i, PREV_BEGIN);
//                        }
//                    }
//                }
//            }
//        }
//
//        if (preloadList.size() <= 0)
//            return null;
//
////        Collections.sort(preloadList, new FileHelper.PriorityAscComparator());
//
//        final ExplorerItem[] preloadArray = new ExplorerItem[preloadList.size()];
//        preloadList.toArray(preloadArray);
//
//        return preloadArray;
//    }

    // 사용안함
    // 페이지 이동 방향에 따라 미리 읽어놓은 것중 쓰지 않은 것을 지운다.
//    private ExplorerItem[] getRemoveArray(int position) {
//        if (imageList == null)
//            return null;
//
//        // split일 경우에는 현재 reload된 것에 bitmap이 사용되는지 안되는지 확인해야 한다.
//        final ArrayList<ExplorerItem> removeList = new ArrayList<ExplorerItem>();
//
//        // 지우는 것은 순서가 필요 없다.
//        final int NEXT_BEGIN = 3;
//        final int NEXT_END = 4;
//
//        for (int i = NEXT_BEGIN; i <= NEXT_END; i++) {
//            final int index = position + i;
//            if (index >= 0 && imageList.size() > index) {
//                final ExplorerItem item = imageList.get(index);
//                removeList.add(item);
//            }
//        }
//
//        final int PREV_BEGIN = 3;
//        final int PREV_END = 3;
//
//        for (int i = PREV_BEGIN; i <= PREV_END; i++) {
//            final int index = position - i;
//            if (index >= 0 && imageList.size() > index) {
//                final ExplorerItem item = imageList.get(index);
//                removeList.add(item);
//            }
//        }
//
//        if (removeList.size() <= 0)
//            return null;
//
//        final ExplorerItem[] removeArray = new ExplorerItem[removeList.size()];
//        removeList.toArray(removeArray);
//
//        return removeArray;
//    }

    public void stopAllTasks() {
        for (AsyncTask task : taskList) {
            task.cancel(true);
        }
        taskList.clear();
    }
}
