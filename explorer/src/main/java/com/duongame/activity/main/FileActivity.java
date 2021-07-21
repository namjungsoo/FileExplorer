package com.duongame.activity.main;

import android.os.Bundle;

import com.duongame.R;
import com.duongame.fragment.BaseFragment;
import com.duongame.fragment.ExplorerFragment;
import com.duongame.helper.AppHelper;

public class FileActivity extends BaseMainActivity {
    private final static String TAG = FileActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 무조건 onCreate 이전에 셋팅 되어야 함
        setTheme(R.style.ExplorerTheme);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutResId() {
        if (AppHelper.isPro(this)) {
            return R.layout.activity_main_file;
        } else {
            return R.layout.activity_main_file_ad;
        }
    }

    @Override
    protected int getMenuResId() {
        return R.menu.menu_file;
    }

    @Override
    protected ExplorerFragment getExplorerFragment() {
        return (ExplorerFragment) getSupportFragmentManager().getFragments().get(0);
    }

    @Override
    protected BaseFragment getCurrentFragment() {
        return getExplorerFragment();
    }
}
