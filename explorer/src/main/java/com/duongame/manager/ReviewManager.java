package com.duongame.manager;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import com.duongame.BuildConfig;
import com.duongame.R;
import com.duongame.db.BookLoader;
import com.duongame.helper.AlertHelper;
import com.duongame.helper.AppHelper;
import com.duongame.helper.PreferenceHelper;

/**
 * Created by namjungsoo on 2016-05-03.
 */
public class ReviewManager {

    private static int[] reviewIndex = {2, 5, 9};

    public static boolean checkReview(final Activity context) {
        // 리뷰 카운트를 체크하여 리뷰가 안되어 있으면 리뷰를 해줌
        boolean ret = false;
        if (!PreferenceHelper.getReviewed(context)) {
            final int reviewCount = PreferenceHelper.getReviewCount(context) + 1;

            for (int i = 0; i < reviewIndex.length; i++) {
                if (reviewCount == reviewIndex[i]) {// 리뷰할 횟수와 동일하면
                    final String appName = AppHelper.getAppName(context);
                    final String title = String.format(context.getResources().getString(R.string.dialog_review_title), appName);
                    final String content = String.format(context.getResources().getString(R.string.dialog_review_content), appName);
                    final DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                final Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
                                final String packageName = context.getApplicationContext().getPackageName();

                                marketLaunch.setData(Uri.parse("market://details?id=" + packageName));
                                context.startActivity(marketLaunch);

                                PreferenceHelper.setReviewed(context, true);
                            } catch (ActivityNotFoundException e) {// FIX: ActivityNotFoundException
                                final Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
                                final String packageName = context.getApplicationContext().getPackageName();

                                marketLaunch.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
                                context.startActivity(marketLaunch);

                                PreferenceHelper.setReviewed(context, true);
                            }
                        }
                    };

                    final DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 리뷰하기에서 취소를 누르면 마지막 파일이 있는지 없는지를 확인한다.
                            // 왜냐면 앱 시작하기에서 호출되므로
                            if (AppHelper.isComicz(context)) {
                                BookLoader.openLastBook(context);
                            }
                        }
                    };

                    if (BuildConfig.SHOW_AD) {
                        AlertHelper.showAlertWithAd(context,
                                title,
                                content,
                                positiveListener,
                                negativeListener,
                                null);
                        AdBannerManager.initPopupAd(context);// 항상 초기화 해주어야 함
                    } else {
                        AlertHelper.showAlert(context,
                                title,
                                content,
                                null,
                                positiveListener,
                                negativeListener,
                                null);
                    }

                    ret = true;
                    break;
                }
            }

            PreferenceHelper.setReviewCount(context, reviewCount);
        }
        return ret;
    }
}
