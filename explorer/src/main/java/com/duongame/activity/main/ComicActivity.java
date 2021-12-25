package com.duongame.activity.main;

import android.os.Bundle;

import androidx.viewpager.widget.ViewPager;

import com.duongame.App;
import com.duongame.R;
import com.duongame.adapter.ComicPagerAdapter;
import com.duongame.fragment.BaseFragment;
import com.duongame.fragment.ExplorerFragment;
import com.duongame.helper.AlertHelper;
import com.duongame.helper.AppHelper;
import com.duongame.helper.PreferenceHelper;
import com.duongame.manager.PermissionManager;
import com.google.android.material.tabs.TabLayout;

import timber.log.Timber;

public class ComicActivity extends BaseMainActivity {
    private final static String TAG = ComicActivity.class.getSimpleName();

    // viewpager
    private ViewPager pager;
    private ComicPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 무조건 onCreate 이전에 셋팅 되어야 함
        setTheme(R.style.ExplorerTheme);

        super.onCreate(savedInstanceState);

        Timber.e("initTabs begin");
        initTabs();
        Timber.e("initTabs end");

        if (!PreferenceHelper.INSTANCE.getPermissionAgreed()) {
            AlertHelper.INSTANCE.showAlert(this,
                    AppHelper.INSTANCE.getAppName(),
                    getString(R.string.required_permission),
                    null,
                    (dialog, which) -> {
                        PreferenceHelper.INSTANCE.setPermissionAgreed(true);
                        PermissionManager.checkStoragePermissions(ComicActivity.this);
                    },
                    (dialog, which) -> {
                        try {
                            App.Companion.getInstance().exit(ComicActivity.this);
                        } catch (NullPointerException e) {

                        }
                    },
                    (dialog, keyCode, event) -> false);
        }
    }

    @Override
    protected int getLayoutResId() {
        if (AppHelper.INSTANCE.isPro()) {
            return R.layout.activity_main_comic;
        } else {
            return R.layout.activity_main_comic_ad;
        }
    }

    @Override
    protected int getMenuResId() {
        return R.menu.menu_comicz;
    }

    @Override
    protected ExplorerFragment getExplorerFragment() {
        return (ExplorerFragment) adapter.getItem(0);
    }

    @Override
    protected BaseFragment getCurrentFragment() {
        final int position = pager.getCurrentItem();
        return (BaseFragment) adapter.getItem(position);
    }

    private void initTabs() {
        // 이부분은 쓰레드에서 할 것이 아니다. 성능상의 이점도 없음
        pager = findViewById(R.id.pager);
        adapter = new ComicPagerAdapter(getSupportFragmentManager(), ComicActivity.this);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(3);
        TabLayout tab = findViewById(R.id.tab);
        tab.setupWithViewPager(pager);
    }
}
