package com.duongame.explorer.helper;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import com.duongame.BuildConfig;
import com.duongame.R;

/**
 * Created by 정수 on 2015-11-15.
 */
public class ShortcutHelper {
    public static void checkShortcut(Context context) {
        initShortcut(context);
    }

    private static void initShortcut(final Context context) {
        // 바로가기 중복 생성 방지
        if (!PreferenceHelper.isShortcut(context)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    addShortcut(context);
                }
            }).start();
        }
    }

    private static void addShortcut(Context context) {
        final Intent shortcutIntent = new Intent();
        shortcutIntent.setAction(Intent.ACTION_MAIN);
        shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        shortcutIntent.setClassName(context, context.getClass().getName());
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final Parcelable iconResource = Intent.ShortcutIconResource.fromContext(context, AppHelper.getIconResId(context));

        final Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);

        int resId = 0;
        if(BuildConfig.FLAVOR_project.equals("comicz")) {
            if(BuildConfig.SHOW_AD) {
                resId = R.string.comicz_name_free;
            } else {
                resId = R.string.comicz_name_pro;
            }
        } else if(BuildConfig.FLAVOR_project.equals("file")) {
            if(BuildConfig.SHOW_AD) {
                resId = R.string.file_name_free;
            } else {
                resId = R.string.file_name_pro;
            }
        }

        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getResources().getString(resId));
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
        intent.putExtra("duplicate", false);
        intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        context.sendBroadcast(intent);

        // 바로가기 생성했다고 체크함
        PreferenceHelper.setShortcut(context, true);
    }

}
