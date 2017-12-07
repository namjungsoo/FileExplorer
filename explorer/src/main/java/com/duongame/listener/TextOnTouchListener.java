package com.duongame.listener;

import com.duongame.activity.TextActivity;

/**
 * Created by namjungsoo on 2017-01-22.
 */

public class TextOnTouchListener extends BaseOnTouchListener {
    private TextActivity activity;

    public TextOnTouchListener(TextActivity activity) {
        super(activity);
        this.activity = activity;
        touchAxis = Axis.AXIS_Y;
    }

    @Override
    protected boolean handleActionUp() {
        if (!isBeingDragged) {
            activity.setFullscreen(!activity.getFullscreen());
            return true;
        } else {
            isBeingDragged = false;
        }
        return false;
    }
}
