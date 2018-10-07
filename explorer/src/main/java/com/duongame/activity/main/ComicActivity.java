package com.duongame.activity.main;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

import com.duongame.R;
import com.duongame.adapter.ComicPagerAdapter;
import com.duongame.fragment.BaseFragment;
import com.duongame.fragment.ExplorerFragment;
import com.duongame.helper.JLog;

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

        JLog.e("Jungsoo", "initTabs begin");
        initTabs();
        JLog.e("Jungsoo", "initTabs end");
    }

    @Override
    protected int getLayoutResId() {
        //return R.layout.activity_main_comic;
        return R.layout.activity_comic;// navigation drawer 없는 버전 = cloud 지원 안하는 버전
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                pager = findViewById(R.id.pager);
                adapter = new ComicPagerAdapter(getSupportFragmentManager(), ComicActivity.this);
                pager.setAdapter(adapter);
                pager.setOffscreenPageLimit(3);
                TabLayout tab = findViewById(R.id.tab);
                tab.setupWithViewPager(pager);
            }
        }).start();
    }
}
