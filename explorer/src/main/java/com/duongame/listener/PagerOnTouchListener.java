package com.duongame.listener;

import android.os.AsyncTask;
import android.view.MotionEvent;
import android.view.View;

import com.duongame.MainApplication;
import com.duongame.activity.viewer.PagerActivity;
import com.duongame.helper.PreferenceHelper;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by namjungsoo on 2017-01-22.
 */

public class PagerOnTouchListener extends BaseOnTouchListener {
    private final static String TAG = PagerOnTouchListener.class.getSimpleName();
    private final static int DOUBLE_TAP_INTERVAL_MS = 150;// 더블탭 타임 안쪽으로 터치가 다시한번 들어오면 더블탭으로 인정

    private WeakReference<PagerActivity> activityWeakReference;
    private long actionUpTime;
    private long actionDownTime;
    private AtomicBoolean isTaskRunning = new AtomicBoolean(false);

    public PagerOnTouchListener(PagerActivity activity) {
        super(activity);
        activityWeakReference = new WeakReference<PagerActivity>(activity);
    }

    private long getActionUpTime() {
        return actionUpTime;
    }

    private long getActionDownTime() {
        return actionDownTime;
    }

    private WeakReference<PagerActivity> getActivityWeakReference() {
        return activityWeakReference;
    }

    private AtomicBoolean getIsTaskRunning() {
        return isTaskRunning;
    }

    static class PagingTask extends AsyncTask<Void, Void, Void> {
        int page;
        PagerOnTouchListener listener;

        PagingTask(int page, PagerOnTouchListener listener) {
            this.page = page;
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(DOUBLE_TAP_INTERVAL_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                if (listener.getActionDownTime() < listener.getActionUpTime()) {
                    PagerActivity activity = listener.getActivityWeakReference().get();
                    boolean smoothScroll = true;
                    if (PreferenceHelper.INSTANCE.getPagingAnimationDisabled()) {
                        smoothScroll = false;
                    }
                    activity.getPager().setCurrentItem(page, smoothScroll);
                }
                listener.getIsTaskRunning().set(false);
            } catch (NullPointerException e) {

            }
        }
    }

    static class FullscreenTask extends AsyncTask<Void, Void, Void> {
        PagerOnTouchListener listener;

        FullscreenTask(PagerOnTouchListener listener) {
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(DOUBLE_TAP_INTERVAL_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (listener.getActionDownTime() < listener.getActionUpTime()) {
                PagerActivity activity = listener.getActivityWeakReference().get();
                if (activity != null) {
                    activity.setFullscreen(!activity.getFullscreen());
                }
            }

            listener.getIsTaskRunning().set(false);
        }
    }

    @Override
    public boolean handleTouch(View v, MotionEvent ev) {
        boolean ret = super.handleTouch(v, ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                actionDownTime = System.currentTimeMillis();
                break;
            }
            case MotionEvent.ACTION_UP: {
                break;
            }
        }
        return ret;
    }

    @Override
    protected boolean handleActionUp() {
        PagerActivity activity = activityWeakReference.get();
        if (activity == null) {
            return false;
        }

        if (!isBeingDragged && activity.getPagerIdle()) {
            // 터치 영역을 확인하여 좌/중/우를 확인하자.
            int width = activity.getPager().getWidth();
            int height = activity.getPager().getHeight();

            int left = width / 4;
            int right = width * 3 / 4;

            if (lastMotionPt.x < left) {
                int page = activity.getPager().getCurrentItem();
                if (page > 0) {
                    if (!isTaskRunning.getAndSet(true)) {
                        actionUpTime = System.currentTimeMillis();
                        PagingTask task = new PagingTask(page - 1, this);
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
            } else if (lastMotionPt.x > right) {
                //int total_file = pager.getChildCount();
                int count = activity.getPagerAdapter().getCount();
                int page = activity.getPager().getCurrentItem();
                if (page < count - 1) {
                    if (!isTaskRunning.getAndSet(true)) {
                        actionUpTime = System.currentTimeMillis();
                        PagingTask task = new PagingTask(page + 1, this);
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                } else {
                    // 마지막 페이지 이면
                    activity.openNextBook();
                }
            } else {
                if (!isTaskRunning.getAndSet(true)) {
                    actionUpTime = System.currentTimeMillis();
                    FullscreenTask task = new FullscreenTask(this);
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
            return true;
        } else {
            isBeingDragged = false;
        }
        return false;
    }
}
