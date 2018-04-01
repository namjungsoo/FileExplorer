package com.duongame.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;

import com.duongame.R;
import com.duongame.manager.AdBannerManager;
import com.google.android.gms.ads.AdView;

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
        // adPopupView가 이전의 팝업에 붙어 있을경우 처리해줌
        AdView adView = AdBannerManager.getAdPopupView();
        if(adView == null)
            return;

        ViewGroup vg = (ViewGroup) adView.getParent();
        if(vg != null) {
            vg.removeView(adView);
        }

        showAlert(context, title, message, adView, posListener, negListener, keyListener);
    }


    public static void showAlertWithAd(Activity context,
                                       String title,
                                       String message,
                                       DialogInterface.OnClickListener posListener,
                                       DialogInterface.OnKeyListener keyListener,
                                       boolean okOnly) {
        AdView adView = AdBannerManager.getAdPopupView();
        if(adView == null)
            return;

        ViewGroup vg = (ViewGroup) adView.getParent();
        if(vg != null) {
            vg.removeView(adView);
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setView(adView)
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
