package com.duongame.manager

import android.content.Context
import android.graphics.Typeface

/**
 * Created by js296 on 2017-07-08.
 */
object FontManager {
    private var nanumMyeongjo: Typeface? = null

    fun getTypeFaceNanumMeyongjo(context: Context): Typeface? {
        if (nanumMyeongjo == null) {
            nanumMyeongjo = Typeface.createFromAsset(context.assets, "fonts/NanumMyeongjo.ttf")
        }
        return nanumMyeongjo
    }
}