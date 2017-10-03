package com.duongame.explorer.manager;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import com.duongame.R;
import com.duongame.comicz.db.BookLoader;
import com.duongame.explorer.helper.AlertHelper;
import com.duongame.explorer.helper.PreferenceHelper;

/**
 * Created by namjungsoo on 2016-05-03.
 */
public class ReviewManager {

    private static int[] reviewIndex = {2, 5, 9};

    public static boolean checkReview(final Activity context) {
        // 리뷰 카운트를 체크하여 리뷰가 안되어 있으면 리뷰를 해줌
        if (!PreferenceHelper.getReviewed(context)) {
            final int reviewCount = PreferenceHelper.getReviewCount(context) + 1;

            for (int i = 0; i < reviewIndex.length; i++) {
                if (reviewCount == reviewIndex[i]) {// 리뷰할 횟수와 동일하면
                    AlertHelper.showAlert(context,
                            context.getResources().getString(R.string.dialog_review_title),
                            context.getResources().getString(R.string.dialog_review_content),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
                                    final String packageName = context.getApplicationContext().getPackageName();

                                    marketLaunch.setData(Uri.parse("market://details?id=" + packageName));
                                    context.startActivity(marketLaunch);

                                    PreferenceHelper.setReviewed(context, true);
                                }
                            },
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    BookLoader.openLastBook(context);
                                }
                            }, null);
                    AdBannerManager.initPopupAd(context);// 항상 초기화 해주어야 함
                    break;
                }
            }

            PreferenceHelper.setReviewCount(context, reviewCount);
            return true;
        }
        return false;
    }
}
