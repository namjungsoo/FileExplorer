package com.duongame.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;

import com.duongame.R;
import com.duongame.manager.AdBannerManager;

/**
 * Created by namjungsoo on 2016-04-30.
 */
public class AlertHelper {
    private static final String TAG = "AlertHelper";

    public static void showAlert(Activity context,
                                 String title,
                                 String message,
                                 View view,
                                 DialogInterface.OnClickListener posListener,
                                 DialogInterface.OnClickListener negListener,
                                 DialogInterface.OnKeyListener keyListener) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setView(view)
                .setIcon(AppHelper.getIconResId(context))
                .setOnKeyListener(keyListener)
                .setPositiveButton(context.getString(R.string.ok), posListener)
                .setNegativeButton(context.getString(R.string.cancel), negListener);

        builder.show();

    }

    public static void showAlertWithAd(Activity context,
                                       String title,
                                       String message,
                                       DialogInterface.OnClickListener posListener,
                                       DialogInterface.OnClickListener negListener,
                                       DialogInterface.OnKeyListener keyListener) {
        showAlert(context, title, message, AdBannerManager.getAdPopupView(), posListener, negListener, keyListener);
    }


    public static void showAlertWithAd(Activity context,
                                       String title,
                                       String message,
                                       DialogInterface.OnClickListener posListener,
                                       DialogInterface.OnKeyListener keyListener,
                                       boolean okOnly) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setView(AdBannerManager.getAdPopupView())
                .setIcon(AppHelper.getIconResId(context))
                .setOnKeyListener(keyListener)
                .setPositiveButton(context.getString(R.string.ok), posListener);

        if (!okOnly) {
            builder.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 아무것도 안함

                }
            });
        }

        builder.show();
    }
}
