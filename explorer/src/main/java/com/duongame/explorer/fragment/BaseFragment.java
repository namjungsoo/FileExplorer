package com.duongame.explorer.fragment;

import android.support.v4.app.Fragment;

import com.duongame.explorer.R;
import com.duongame.explorer.helper.ToastHelper;

/**
 * Created by namjungsoo on 2017-01-02.
 */

public class BaseFragment extends Fragment {
    long lastBackPressed = 0;
    private final static int TIME_MS = 2000;

    public void onRefresh() {
    }

    public void onBackPressed() {
        long current = System.currentTimeMillis();
        if (lastBackPressed != 0 && current - lastBackPressed < TIME_MS) {// 마지막 누른후 2초 안이면 종료 한다.
            getActivity().finish();
        } else {// 그게 아니면 한번더 입력을 받는다
            lastBackPressed = current;

            // 토스트를 띄운다.
            ToastHelper.showToast(getActivity(), R.string.backpressed);
        }
    }
}
