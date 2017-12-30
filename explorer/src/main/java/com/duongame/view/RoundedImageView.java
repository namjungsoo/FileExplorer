package com.duongame.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.duongame.helper.UnitHelper;

/**
 * Created by namjungsoo on 2016-06-19.
 */
public class RoundedImageView extends android.support.v7.widget.AppCompatImageView {
    Path clipPath;

    public RoundedImageView(Context context) {
        super(context);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private int radiusDp = 0;

    public void setRadiusDp(int radius) {
//        if (this.radiusDp != radius) {
//            this.radiusDp = radius;
//            clipPath = null;
//        }
    }

    public int getRadiusDp() {
        return radiusDp;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (radiusDp == 0) {
            super.onDraw(canvas);
            return;
        }

        if (clipPath == null) {
            clipPath = new Path();

            final float radius = UnitHelper.dpToPx(radiusDp);
            final float padding = 0;
            final int w = this.getWidth();
            final int h = this.getHeight();

            clipPath.addRoundRect(new RectF(padding, padding, w - padding, h - padding), radius, radius, Path.Direction.CW);
        }

        // 특정 기기에서 익셉션 발생함
        try {
            canvas.clipPath(clipPath);
            super.onDraw(canvas);
        } catch (UnsupportedOperationException e) {
        }

        try {
            super.onDraw(canvas);
        } catch (Exception e) {
        }
    }
}
