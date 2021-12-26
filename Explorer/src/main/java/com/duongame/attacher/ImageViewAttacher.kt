package com.duongame.attacher

import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import com.duongame.activity.viewer.PagerActivity
import com.duongame.listener.PagerOnTouchListener
import com.github.chrisbanes.photoview.PhotoViewAttacher

/**
 * Created by js296 on 2017-07-05.
 */
class ImageViewAttacher(var imageView: ImageView) : PhotoViewAttacher(
    imageView
) {
    var mPagerOnTouchListener: PagerOnTouchListener? = null
    var isLayoutInitialized = false

    fun setActivity(activity: PagerActivity) {
        mPagerOnTouchListener = PagerOnTouchListener(activity)
    }

    override fun onTouch(v: View, ev: MotionEvent): Boolean {
        val ret = super.onTouch(v, ev)
        if (mPagerOnTouchListener != null) mPagerOnTouchListener!!.handleTouch(v, ev)
        return ret
    }

    // 확대된 상태에서 menu를 호출하게 되면 다시 원본으로 축소된다.
    // 이유는 같은 크기인데도 layout change로 matrix가 초기화 되기 때문이다.
    override fun onLayoutChange(
        v: View,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {
        if (!isLayoutInitialized) {
            super.onLayoutChange(v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom)
            isLayoutInitialized = true
        }
    }
}