package com.duongame.activity;

import com.duongame.R;
import com.duongame.fragment.BaseFragment;

public class FileActivity extends BaseActivity {
    private final static String TAG = FileActivity.class.getSimpleName();;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_file;
    }

    @Override
    protected int getMenuResId() {
        return R.menu.menu_file;
    }

    @Override
    protected BaseFragment getExplorerFragment() {
        return (BaseFragment) getSupportFragmentManager().getFragments().get(0);
    }

    @Override
    protected BaseFragment getCurrentFragment() {
        return getExplorerFragment();
    }
}
