package com.duongame.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

import com.duongame.R;
import com.duongame.adapter.ComicPagerAdapter;
import com.duongame.fragment.BaseFragment;

public class ComicActivity extends BaseActivity {
    private final static String TAG = ComicActivity.class.getSimpleName();

    // viewpager
    private ViewPager pager;
    private ComicPagerAdapter adapter;
    private TabLayout tab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initTabs();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_comic;
    }

    @Override
    protected int getMenuResId() {
        return R.menu.menu_comicz;
    }

    @Override
    protected BaseFragment getExplorerFragment() {
        return (BaseFragment) adapter.getItem(0);
    }

    @Override
    protected BaseFragment getCurrentFragment() {
        final int position = pager.getCurrentItem();
        final BaseFragment fragment = (BaseFragment) adapter.getItem(position);
        return fragment;
    }

    private void initTabs() {
        pager = (ViewPager) findViewById(R.id.pager);
        adapter = new ComicPagerAdapter(getSupportFragmentManager(), this);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(3);
        tab = (TabLayout) findViewById(R.id.tab);
        tab.setupWithViewPager(pager);
    }
}