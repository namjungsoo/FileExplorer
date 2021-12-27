package com.duongame.listener

import android.app.Activity
import android.view.View.OnTouchListener
import android.graphics.PointF
import android.view.VelocityTracker
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration

/**
 * Created by namjungsoo on 2017-01-22.
 */
// Base OnTouchListener
abstract class BaseOnTouchListener internal constructor(activity: Activity?) : OnTouchListener {
    enum class Axis {
        AXIS_X, AXIS_Y, AXIS_BOTH
    }

    var touchAxis = Axis.AXIS_X

    // touch
    var isBeingDragged = false
    var lastMotionPt = PointF()
    private val initialMotionPt = PointF()

    // configuration
    private var velocityTracker: VelocityTracker? = null
    private var touchSlop = 0
    private fun startDragXIfNeeded(ev: MotionEvent) {
        val x = ev.getX(0)
        val xSignedDiff = x - initialMotionPt.x
        val xDiff = Math.abs(xSignedDiff)
        if (xDiff < touchSlop) {
            isBeingDragged = false
            return
        }
        isBeingDragged = true
    }

    private fun startDragYIfNeeded(ev: MotionEvent) {
        val y = ev.getY(0)
        val ySignedDiff = y - initialMotionPt.y
        val yDiff = Math.abs(ySignedDiff)
        if (yDiff < touchSlop) {
            isBeingDragged = false
            return
        }
        isBeingDragged = true
    }

    open fun handleTouch(v: View, ev: MotionEvent): Boolean {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker!!.addMovement(ev)
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                initialMotionPt.x = ev.getX(0)
                lastMotionPt.x = initialMotionPt.x
                initialMotionPt.y = ev.getY(0)
                lastMotionPt.y = initialMotionPt.y
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isBeingDragged) {
                    if (touchAxis == Axis.AXIS_X) {
                        startDragXIfNeeded(ev)
                    } else if (touchAxis == Axis.AXIS_Y) {
                        startDragYIfNeeded(ev)
                    }
                }
                val x = ev.getX(0)
                val y = ev.getY(0)
                lastMotionPt.x = x
                lastMotionPt.y = y
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (velocityTracker != null) {
                    velocityTracker!!.recycle()
                    velocityTracker = null
                }

                // 내가 캡쳐 했으면 true
                if (handleActionUp()) {
                    return true
                } else {
                    v.performClick()
                }
            }
        }

        // 하위 뷰에게 전달하려면 false
        return false
    }

    override fun onTouch(v: View, ev: MotionEvent): Boolean {
        return handleTouch(v, ev)
    }

    protected abstract fun handleActionUp(): Boolean

    init {
        val configuration = ViewConfiguration.get(activity)
        touchSlop = configuration.scaledTouchSlop shr 1
    }
}