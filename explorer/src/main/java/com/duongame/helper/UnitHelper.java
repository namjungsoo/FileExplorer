package com.duongame.helper;

import android.content.res.Resources;

/**
 * Created by namjungsoo on 2016-11-19.
 */

public class UnitHelper {
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
}
