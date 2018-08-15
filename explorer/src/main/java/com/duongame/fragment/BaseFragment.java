package com.duongame.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.duongame.AnalyticsApplication;
import com.duongame.R;
import com.duongame.file.FileExplorer;
import com.duongame.file.LocalExplorer;
import com.duongame.helper.ToastHelper;

/**
 * Created by namjungsoo on 2017-01-02.
 */

public class BaseFragment extends Fragment {
    private final static int TIME_MS = 2000;
    protected final static int CLOUD_LOCAL = 0;
    protected final static int CLOUD_DROPBOX = 1;
    protected final static int CLOUD_GOOGLEDRIVE = 2;

    // Search
    protected AnalyticsApplication application;
    protected FileExplorer fileExplorer;
    protected FileExplorer.Result fileResult;
    protected int cloud = CLOUD_LOCAL;
    private long lastBackPressed = 0;

    public AnalyticsApplication getApplication() {
        return application;
    }

    public FileExplorer getFileExplorer() {
        return fileExplorer;
    }

    public FileExplorer.Result getFileResult() {
        return fileResult;
    }

    public int getCloud() {
        return cloud;
    }

    public long getLastBackPressed() {
        return lastBackPressed;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentActivity activity = getActivity();
        if(activity != null) {
            application = (AnalyticsApplication) activity.getApplication();
        }
        fileExplorer = new LocalExplorer();
    }

    //FIX:
    @Override
    public void onResume() {
        super.onResume();
        if(application == null) {
            FragmentActivity activity = getActivity();
            if(activity != null) {
                application = (AnalyticsApplication) activity.getApplication();
            }
        }

        if(fileExplorer == null) {
            fileExplorer = new LocalExplorer();
        }
    }

    public void onRefresh() {
    }

    public void onBackPressed() {
        long current = System.currentTimeMillis();
        if (lastBackPressed != 0 && current - lastBackPressed < TIME_MS) {// 마지막 누른후 2초 안이면 종료 한다.

            // activity가 null일수 있음
            FragmentActivity activity = getActivity();
            if (activity != null) {
                if (!activity.isFinishing()) {
                    activity.finish();
                }
            }
        } else {// 그게 아니면 한번더 입력을 받는다
            lastBackPressed = current;

            // 토스트를 띄운다.
            FragmentActivity activity = getActivity();
            if (activity != null) {
                ToastHelper.info(activity, R.string.back_pressed);
            }
        }
    }
}
