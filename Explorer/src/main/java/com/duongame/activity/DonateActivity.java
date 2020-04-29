package com.duongame.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.duongame.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DonateActivity extends BaseActivity implements BillingProcessor.IBillingHandler {
    /*
       donate_month_ 매달 x원씩 후원합니다
       2000원: 하단 배너광고 제거
       5000원: 모든 광고 제거

       donate_ 1,3,7,15,30일동안 광고가 제거됩니다 (일회성)
     */

    private final static String TAG = DonateActivity.class.getSimpleName();
    private final static String GP_LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxHFQGSxElswW8fm3xjwdEWmllAJTrMeeTjCuEbYqS0Uwgdz52AEqMOJucoZRxj7M9xZF1swod0fKOOfntlW4ckfF8/lgOaU/1iTmw5pc5sQ3fHI7YlpuQTyL+txWbknhSsJ0zR6urQOZr0eKu8gv+3R7M7Mo22s91mBg25vWfsHncTLa6JOi9Js1Y1KHrRfo1NBnzvZND9nMFmiG9dUqQ6zLlP2s0Ie5gPOz0iOwuPVrZu5iZgVHSpy5/WlYkLHdkCSiH39QSpix9Cq9yhNT4DE0TQvHJR6dwBUsmgCE4Ifc5/2w20/ZNyHSOvPFZPta0pjXKGhhzCVxlvqskhqFJwIDAQAB";
    private BillingProcessor bp;
    public static ArrayList<SkuDetails> products;

    private ArrayList<String> ids = new ArrayList<>(Arrays.asList("donate_1000", "donate_2000", "donate_5000", "donate_7000", "donate_10000",
            "donate_month_2000", "donate_month_5000"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        bp = new BillingProcessor(this, GP_LICENSE_KEY, this);
        Log.e(TAG, "onCreate");
    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {

    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {

    }

    @Override
    public void onBillingInitialized() {
        products = (ArrayList<SkuDetails>) bp.getPurchaseListingDetails(ids);
        List<SkuDetails> details = bp.getSubscriptionListingDetails(ids);

        // Sort ascending order
        Collections.sort(products, new Comparator<SkuDetails>() {
            @Override
            public int compare(SkuDetails o1, SkuDetails o2) {
                if (o1.priceLong > o2.priceLong) {
                    return 1;
                } else if (o1.priceLong < o2.priceLong) {
                    return -1;
                } else return 0;
            }
        });



        Log.e(TAG, "products=" + products);
        Log.e(TAG, "details=" + details);

        for (int i = 0; i < products.size(); i++) {
            Log.e(TAG, "onBillingInitialized products " + products.get(i));
        }

        for (int i = 0; i < details.size(); i++) {
            Log.e(TAG, "onBillingInitialized details " + details.get(i));
        }

    }
}