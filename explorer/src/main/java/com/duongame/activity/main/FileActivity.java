package com.duongame.activity.main;

import com.duongame.R;
import com.duongame.fragment.BaseFragment;
import com.duongame.fragment.ExplorerFragment;

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
    protected ExplorerFragment getExplorerFragment() {
        return (ExplorerFragment) getSupportFragmentManager().getFragments().get(0);
    }

    @Override
    protected BaseFragment getCurrentFragment() {
        return getExplorerFragment();
    }
}
