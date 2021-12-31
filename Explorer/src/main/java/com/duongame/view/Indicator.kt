package com.duongame.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.duongame.R

class Indicator : View {
    private var targetView: View? = null
    private var targetRect: Rect? = null
    private var paint: Paint? = null

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (targetView == null) return
        if (paint == null) {
            paint = Paint()
            paint!!.color = resources.getColor(R.color.colorAccent)
        }
        if (targetRect == null) {
            targetRect = Rect(targetView!!.left, 0, targetView!!.right, height)
        }
        canvas.drawRect(targetRect!!, paint!!)
    }

    fun setTargetView(view: View?) {
        targetView = view
        targetRect = null
        invalidate()
    }

    // GONE -> VISIBLE로 변경되는 view가 여러개가 있으면 위치가 변경될수 있어서 refresh를 명시적으로 호출해 주어야 한다.
    fun refresh() {
        targetRect = null
        invalidate()
    }
}