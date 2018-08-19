package com.duongame.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.duongame.R;

public class Indicator extends View {
    private View targetView;
    private Rect targetRect;
    private Paint paint;

    public Indicator(Context context) {
        super(context);
    }

    public Indicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Indicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (targetView == null)
            return;

        if (paint == null) {
            paint = new Paint();
            paint.setColor(getResources().getColor(R.color.colorAccent));
        }
        if (targetRect == null) {
            targetRect = new Rect(targetView.getLeft(), 0, targetView.getRight(), getHeight());
        }

        canvas.drawRect(targetRect, paint);
    }

    public void setTargetView(View view) {
        targetView = view;
        targetRect = null;
        invalidate();
    }

    // GONE -> VISIBLE로 변경되는 view가 여러개가 있으면 위치가 변경될수 있어서 refresh를 명시적으로 호출해 주어야 한다.
    public void refresh() {
        targetRect = null;
        invalidate();
    }
}
