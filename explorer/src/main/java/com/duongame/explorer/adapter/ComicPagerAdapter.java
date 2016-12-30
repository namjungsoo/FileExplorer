package com.duongame.explorer.adapter;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.duongame.explorer.R;
import com.duongame.explorer.fragment.ExplorerFragment;
import com.duongame.explorer.fragment.HistoryFragment;
import com.duongame.explorer.fragment.SearchFragment;

import java.util.HashMap;

/**
 * Created by namjungsoo on 2016. 12. 30..
 */

public class ComicPagerAdapter extends FragmentStatePagerAdapter {
    private final static String TAG = "ComicPagerAdapter";
    private final int PAGE_COUNT = 3;
    Activity context;
    HashMap<Integer, Fragment> fragmentMap = new HashMap<>();

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
        Fragment fragment = null;
        Log.d(TAG, "getItem=" + position);
        fragment = fragmentMap.get(position);
        if(fragment == null) {
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

//    @Override
//    public boolean isViewFromObject(View view, Object object) {
//        return view == object;
//    }
}
