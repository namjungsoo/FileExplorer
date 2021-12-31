package com.duongame.listener

import com.duongame.activity.viewer.TextActivity
import com.duongame.listener.BaseOnTouchListener

/**
 * Created by namjungsoo on 2017-01-22.
 */
class TextOnTouchListener(private val activity: TextActivity) : BaseOnTouchListener(
    activity
) {
    override fun handleActionUp(): Boolean {
        if (!isBeingDragged) {
            activity.isFullscreen = !activity.isFullscreen
            return true
        } else {
            isBeingDragged = false
        }
        return false
    }

    init {
        touchAxis = Axis.AXIS_Y
    }
}