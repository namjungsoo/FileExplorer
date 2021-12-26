package com.duongame.listener

import android.os.AsyncTask
import android.view.MotionEvent
import android.view.View
import com.duongame.activity.viewer.PagerActivity
import com.duongame.helper.PreferenceHelper.pagingAnimationDisabled
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by namjungsoo on 2017-01-22.
 */
class PagerOnTouchListener(activity: PagerActivity) : BaseOnTouchListener(activity) {
    private val activityWeakReference: WeakReference<PagerActivity> = WeakReference(activity)
    private var actionUpTime: Long = 0
    private var actionDownTime: Long = 0
    private val isTaskRunning = AtomicBoolean(false)

    internal class PagingTask(var page: Int, var listener: PagerOnTouchListener) :
        AsyncTask<Void?, Void?, Void?>() {
        override fun doInBackground(vararg params: Void?): Void? {
            try {
                Thread.sleep(DOUBLE_TAP_INTERVAL_MS.toLong())
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            try {
                if (listener.actionDownTime < listener.actionUpTime) {
                    val activity = listener.activityWeakReference.get()
                    var smoothScroll = true
                    if (pagingAnimationDisabled) {
                        smoothScroll = false
                    }
                    activity?.pager?.setCurrentItem(page, smoothScroll)
                }
                listener.isTaskRunning.set(false)
            } catch (e: NullPointerException) {
            }
        }
    }

    internal class FullscreenTask(var listener: PagerOnTouchListener) :
        AsyncTask<Void?, Void?, Void?>() {
        override fun doInBackground(vararg params: Void?): Void? {
            try {
                Thread.sleep(DOUBLE_TAP_INTERVAL_MS.toLong())
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            if (listener.actionDownTime < listener.actionUpTime) {
                val activity = listener.activityWeakReference.get()
                if (activity != null) {
                    activity.isFullscreen = !activity.isFullscreen
                }
            }
            listener.isTaskRunning.set(false)
        }
    }

    override fun handleTouch(v: View, ev: MotionEvent): Boolean {
        val ret = super.handleTouch(v, ev)
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                actionDownTime = System.currentTimeMillis()
            }
            MotionEvent.ACTION_UP -> {}
        }
        return ret
    }

    override fun handleActionUp(): Boolean {
        val activity = activityWeakReference.get() ?: return false
        if (!isBeingDragged && activity.pagerIdle) {
            // 터치 영역을 확인하여 좌/중/우를 확인하자.
            val width = activity.pager?.width ?: 0
            val height = activity.pager?.height ?: 0
            val left = width / 4
            val right = width * 3 / 4
            if (lastMotionPt.x < left) {
                val page = activity.pager?.currentItem ?: 0
                if (page > 0) {
                    if (!isTaskRunning.getAndSet(true)) {
                        actionUpTime = System.currentTimeMillis()
                        val task = PagingTask(page - 1, this)
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                    }
                }
            } else if (lastMotionPt.x > right) {
                //int total_file = pager.getChildCount();
                val count = activity.pagerAdapter?.count ?: 0
                val page = activity.pager?.currentItem ?: 0
                if (page < count - 1) {
                    if (!isTaskRunning.getAndSet(true)) {
                        actionUpTime = System.currentTimeMillis()
                        val task = PagingTask(page + 1, this)
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                    }
                } else {
                    // 마지막 페이지 이면
                    activity.openNextBook()
                }
            } else {
                if (!isTaskRunning.getAndSet(true)) {
                    actionUpTime = System.currentTimeMillis()
                    val task = FullscreenTask(this)
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                }
            }
            return true
        } else {
            isBeingDragged = false
        }
        return false
    }

    companion object {
        private const val DOUBLE_TAP_INTERVAL_MS = 150 // 더블탭 타임 안쪽으로 터치가 다시한번 들어오면 더블탭으로 인정
    }

}