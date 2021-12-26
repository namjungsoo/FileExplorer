package com.duongame.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import com.duongame.helper.UnitHelper.dpToPx
import androidx.appcompat.widget.AppCompatImageView
import com.duongame.helper.UnitHelper
import android.graphics.RectF
import android.util.AttributeSet
import java.lang.Exception
import java.lang.UnsupportedOperationException

/**
 * Created by namjungsoo on 2016-06-19.
 */
class RoundedImageView : AppCompatImageView {
    var clipPath: Path? = null
    val radiusDp = 0

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    )

    fun setRadiusDp(radius: Int) {
//        if (this.radiusDp != radius) {
//            this.radiusDp = radius;
//            clipPath = null;
//        }
    }

    override fun onDraw(canvas: Canvas) {
        if (radiusDp == 0) {
            super.onDraw(canvas)
            return
        }
        if (clipPath == null) {
            clipPath = Path()
            val radius = dpToPx(radiusDp).toFloat()
            val padding = 0f
            val w = this.width
            val h = this.height
            clipPath!!.addRoundRect(
                RectF(padding, padding, w - padding, h - padding),
                radius,
                radius,
                Path.Direction.CW
            )
        }

        // 특정 기기에서 익셉션 발생함
        try {
            canvas.clipPath(clipPath!!)
            super.onDraw(canvas)
        } catch (e: UnsupportedOperationException) {
        }
        try {
            super.onDraw(canvas)
        } catch (e: Exception) {
        }
    }
}