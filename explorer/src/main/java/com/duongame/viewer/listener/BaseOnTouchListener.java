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
    private final static String TAG = BaseOnTouchListener.class.getSimpleName();
    private final static boolean DEBUG = false;

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

    public boolean handleTouch(View v, MotionEvent ev) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(ev);

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                lastMotionPt.x = initialMotionPt.x = ev.getX(0);
                lastMotionPt.y = initialMotionPt.y = ev.getY(0);
                return true;
                //break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (!isBeingDragged) {
                    if (touchAxis == AXIS_X) {
                        startDragXIfNeeded(ev);
                    }
                    else if (touchAxis == AXIS_Y) {
                        startDragYIfNeeded(ev);
                    }
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

                // 내가 캡쳐 했으면 true
                if (handleActionUp()) {
                    return true;
                }
                break;
            }
        }

        // 하위 뷰에게 전달하려면 false
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        return handleTouch(v, ev);
    }

    protected abstract boolean handleActionUp();
}
