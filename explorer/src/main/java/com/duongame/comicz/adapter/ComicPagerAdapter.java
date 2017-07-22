package com.duongame.comicz.adapter;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.duongame.R;
import com.duongame.comicz.fragment.HistoryFragment;
import com.duongame.comicz.fragment.SearchFragment;
import com.duongame.explorer.fragment.BaseFragment;
import com.duongame.explorer.fragment.ExplorerFragment;

import java.util.HashMap;

/**
 * Created by namjungsoo on 2016. 12. 30..
 */

public class ComicPagerAdapter extends FragmentPagerAdapter {
    private final static boolean DEBUG = false;
    private final static String TAG = "ComicPagerAdapter";
    private final int PAGE_COUNT = 3;

    private Activity context;
    private HashMap<Integer, BaseFragment> fragmentMap = new HashMap<>();
    private int lastPosition = -1;

    public ComicPagerAdapter(FragmentManager fm, Activity context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        //TODO: 나중에 동적으로 수정해야 한다.
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        BaseFragment fragment = null;

        if(DEBUG)
            Log.d(TAG, "getItem=" + position);

        fragment = fragmentMap.get(position);
        if (fragment == null) {
            switch (position) {
                case 0:
                    fragment = new ExplorerFragment();
                    fragmentMap.put(position, fragment);
                    break;
                case 1:
                    fragment = new HistoryFragment();
                    fragmentMap.put(position, fragment);
                    break;
                case 2:
                    fragment = new SearchFragment();
                    fragmentMap.put(position, fragment);
                    break;
            }
        }

        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getResources().getString(R.string.explorer);
            case 1:
                return context.getResources().getString(R.string.history);
            case 2:
                return context.getResources().getString(R.string.search);
        }
        return "";
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);

        if(DEBUG)
            Log.d(TAG, "setPrimaryItem=" + position);

        if(position != lastPosition) {
            lastPosition = position;
            if (fragmentMap.containsKey(position)) {
                final BaseFragment fragment = fragmentMap.get(position);
                fragment.onRefresh();
            }
        }
    }

//    @Override
//    public boolean isViewFromObject(View view, Object object) {
//        return view == object;
//    }
}
