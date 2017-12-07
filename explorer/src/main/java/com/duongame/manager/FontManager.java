package com.duongame.manager;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by js296 on 2017-07-08.
 */

public class FontManager {
    static Typeface nanumMyeongjo;

    public static Typeface getTypeFaceNanumMeyongjo(Context context) {
        if (nanumMyeongjo == null) {
            nanumMyeongjo = Typeface.createFromAsset(context.getAssets(), "fonts/NanumMyeongjo.ttf");
        }
        return nanumMyeongjo;
    }
}
