package com.duongame.manager;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

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

                    AlertHelper.showAlertWithAd(context,
                            title,
                            content,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        final Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
                                        final String packageName = context.getApplicationContext().getPackageName();

                                        marketLaunch.setData(Uri.parse("market://details?id=" + packageName));
                                        context.startActivity(marketLaunch);

                                        PreferenceHelper.setReviewed(context, true);
                                    } catch(ActivityNotFoundException e) {// FIX: ActivityNotFoundException
                                        final Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
                                        final String packageName = context.getApplicationContext().getPackageName();

                                        marketLaunch.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
                                        context.startActivity(marketLaunch);

                                        PreferenceHelper.setReviewed(context, true);
                                    }
                                }
                            },
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (AppHelper.isComicz(context)) {
                                        BookLoader.openLastBook(context);
                                    }
                                }
                            }, null);
                    AdBannerManager.initPopupAd(context);// 항상 초기화 해주어야 함
                    ret = true;
                    break;
                }
            }

            PreferenceHelper.setReviewCount(context, reviewCount);
        }
        return ret;
    }
}
