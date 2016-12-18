package com.duongame.explorer.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.duongame.explorer.R;

/**
 * Created by namjungsoo on 2016-04-30.
 */
public class AlertManager {
    private static final String TAG="AlertManager";

//    public static void showAlertExit(final Activity context) {
//        AlertManager.showAlert(context, context.getResources().getString(R.string.app_name), context.getString(R.string.exit_message), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        Log.d(TAG, "AlertManager onClick finish");
//                        if(dialog != null)
//                            dialog.dismiss();
//                        context.finish();
//                    }
//                }, null
//        );
//        AdBannerManager.initPopupAd(context);// 항상 초기화 해주어야 함
//    }
//
//    public static void showAlertLastFile(final Activity context) {
//        AlertManager.showAlert(context, context.getResources().getString(R.string.app_name), context.getString(R.string.lastfile), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                if(dialog != null)
//                    dialog.dismiss();
//                LoadManager.loadLastFile(context);
//            }
//        }, null);
//        AdBannerManager.initPopupAd(context);// 항상 초기화 해주어야 함
//    }


    public static void showAlert(Activity context, String title, String message, DialogInterface.OnClickListener posListener, DialogInterface.OnKeyListener keyListener, boolean okOnly) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.comicz)
                .setOnKeyListener(keyListener)
                .setPositiveButton(context.getString(R.string.ok), posListener);

        if(!okOnly) {
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
