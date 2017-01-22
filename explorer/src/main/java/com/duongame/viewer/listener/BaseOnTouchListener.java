package com.duongame.viewer.listener;

import android.app.Activity;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

import static com.duongame.viewer.listener.BaseOnTouchListener.Axis.AXIS_X;
import static com.duongame.viewer.listener.BaseOnTouchListener.Axis.AXIS_Y;

/**
 * Created by namjungsoo on 2017-01-22.
 */


// Base OnTouchListener
public abstract class BaseOnTouchListener implements View.OnTouchListener {
    protected enum Axis {
        AXIS_X,
        AXIS_Y,
        AXIS_BOTH
    }

    protected Axis touchAxis = AXIS_X;

    // touch
    protected boolean isBeingDragged = false;
    protected PointF lastMotionPt = new PointF();
    protected PointF initialMotionPt = new PointF();

    // configuration
    protected VelocityTracker velocityTracker = null;
    protected int touchSlop = 0;

    protected void startDragXIfNeeded(MotionEvent ev) {
        final float x = ev.getX(0);
        final float xSignedDiff = x - initialMotionPt.x;
        final float xDiff = Math.abs(xSignedDiff);
        if (xDiff < touchSlop) {
            isBeingDragged = false;
            return;
        }
        isBeingDragged = true;
    }

    protected void startDragYIfNeeded(MotionEvent ev) {
        final float y = ev.getY(0);
        final float ySignedDiff = y - initialMotionPt.y;
        final float yDiff = Math.abs(ySignedDiff);
        if (yDiff < touchSlop) {
            isBeingDragged = false;
            return;
        }
        isBeingDragged = true;
    }

    public BaseOnTouchListener(Activity activity) {
        final ViewConfiguration configuration = ViewConfiguration.get(activity);
        touchSlop = configuration.getScaledTouchSlop() >> 1;
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
//                Log.d(TAG, "onTouch");

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(ev);

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                lastMotionPt.x = initialMotionPt.x = ev.getX(0);
                lastMotionPt.y = initialMotionPt.y = ev.getY(0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (!isBeingDragged) {
                    if(touchAxis == AXIS_X)
                        startDragXIfNeeded(ev);
                    else if(touchAxis == AXIS_Y)
                        startDragYIfNeeded(ev);
                }
                final float x = ev.getX(0);
                final float y = ev.getY(0);
                lastMotionPt.x = x;
                lastMotionPt.y = y;
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                if(handleActionUp())
                    return true;
                break;
            }
        }
        return false;
    }

    protected abstract boolean handleActionUp();
}
