package com.duongame.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.duongame.AnalyticsApplication;
import com.duongame.R;
import com.duongame.helper.FileSearcher;
import com.duongame.helper.ToastHelper;

/**
 * Created by namjungsoo on 2017-01-02.
 */

public class BaseFragment extends Fragment {
    private long lastBackPressed = 0;
    private final static int TIME_MS = 2000;

    // Search
    protected AnalyticsApplication application;
    protected FileSearcher fileSearcher;
    protected FileSearcher.Result searchResult;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (AnalyticsApplication) getActivity().getApplication();
        fileSearcher = new FileSearcher();
    }

    public void onRefresh() {
    }

    public void onBackPressed() {
        long current = System.currentTimeMillis();
        if (lastBackPressed != 0 && current - lastBackPressed < TIME_MS) {// 마지막 누른후 2초 안이면 종료 한다.

            // activity가 null일수 있음
            Activity activity = getActivity();
            if (activity != null) {
                if (!activity.isFinishing()) {
                    activity.finish();
                }
            }
        } else {// 그게 아니면 한번더 입력을 받는다
            lastBackPressed = current;

            // 토스트를 띄운다.
            ToastHelper.showToast(getActivity(), R.string.back_pressed);
        }
    }
}
