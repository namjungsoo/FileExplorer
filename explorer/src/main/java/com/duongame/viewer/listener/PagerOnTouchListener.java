package com.duongame.viewer.listener;

import android.os.AsyncTask;
import android.view.MotionEvent;
import android.view.View;

import com.duongame.viewer.activity.PagerActivity;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by namjungsoo on 2017-01-22.
 */

public class PagerOnTouchListener extends BaseOnTouchListener {
    private final static String TAG = PagerOnTouchListener.class.getSimpleName();
    private final static int DOUBLE_TAP_INTERVAL_MS = 150;

    private PagerActivity activity;
    private long actionUpTime;
    private long actionDownTime;
    private AtomicBoolean isTaskRunning = new AtomicBoolean(false);

    public PagerOnTouchListener(PagerActivity activity) {
        super(activity);
        this.activity = activity;
    }

    class PagingTask extends AsyncTask<Void, Void, Void> {
        int page;

        public PagingTask(int page) {
            this.page = page;
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
            if (actionDownTime < actionUpTime) {
                activity.getPager().setCurrentItem(page, true);
            }

            isTaskRunning.set(false);
        }
    }

    class FullscreenTask extends AsyncTask<Void, Void, Void> {

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
            if (actionDownTime < actionUpTime) {
                activity.setFullscreen(!activity.getFullscreen());
            }

            isTaskRunning.set(false);
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
                        PagingTask task = new PagingTask(page - 1);
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
            } else if (lastMotionPt.x > right) {
                //int total_file = pager.getChildCount();
                int count = activity.getPagerAdapter().getCount();
                int page = activity.getPager().getCurrentItem();
                if (page < count + 1) {
                    if (!isTaskRunning.getAndSet(true)) {
                        actionUpTime = System.currentTimeMillis();
                        PagingTask task = new PagingTask(page + 1);
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
            } else {
                if (!isTaskRunning.getAndSet(true)) {
                    actionUpTime = System.currentTimeMillis();
                    FullscreenTask task = new FullscreenTask();
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
