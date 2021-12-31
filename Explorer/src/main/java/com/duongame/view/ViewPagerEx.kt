package com.duongame.view

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager.widget.ViewPager
import android.view.MotionEvent
import java.lang.IllegalArgumentException

/**
 * Created by Jungsoo on 2017-10-10.
 */
//java.lang.IllegalArgumentException: pointerIndex out of range
// 위의 문제 때문에 항상 ViewPagerEx를 사용해야 함. ViewPager 대신에
class ViewPagerEx : ViewPager {
    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        try {
            return super.onTouchEvent(ev)
        } catch (ex: IllegalArgumentException) {
            ex.printStackTrace()
        }
        return false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        try {
            return super.onInterceptTouchEvent(ev)
        } catch (ex: IllegalArgumentException) {
            ex.printStackTrace()
        }
        return false
    }

    // Custom view… overrides onTouchEvent but not performClick
    override fun performClick(): Boolean {
        return super.performClick()
    }
}