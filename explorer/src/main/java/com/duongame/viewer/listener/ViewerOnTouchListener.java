package com.duongame.viewer.listener;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by namjungsoo on 2017-01-22.
 */


// Base OnTouchListener
public class ViewerOnTouchListener implements View.OnTouchListener {
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
