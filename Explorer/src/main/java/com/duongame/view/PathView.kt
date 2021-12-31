package com.duongame.view

import android.content.Context
import android.widget.HorizontalScrollView
import android.text.TextUtils
import android.util.AttributeSet

class PathView : HorizontalScrollView {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    var path: String? = null
        private set

    interface OnPathClickListener {
        fun onClickPath(path: String?)
    }

    fun setOnPathClickListener() {}
    fun setPath(path: String) {
        if (TextUtils.isEmpty(path)) return
        if (this.path == path) return
        this.path = path
        updateAllChildViews()
    }

    private fun updateAllChildViews() {
        // 모두 삭제한 후에
        removeAllViews()

        // 새로 등록해준다
        val paths = path!!.split("/").toTypedArray()
    }
}