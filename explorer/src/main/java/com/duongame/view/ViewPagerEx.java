package com.duongame.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Jungsoo on 2017-10-10.
 */

//java.lang.IllegalArgumentException: pointerIndex out of range
// 위의 문제 때문에 항상 ViewPagerEx를 사용해야 함. ViewPager 대신에
public class ViewPagerEx extends android.support.v4.view.ViewPager {
    public ViewPagerEx(Context context) {
        super(context);
    }

    public ViewPagerEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    // Custom view… overrides onTouchEvent but not performClick
    @Override
    public boolean performClick() {
        return super.performClick();
    }

}
