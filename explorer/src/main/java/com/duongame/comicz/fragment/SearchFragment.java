package com.duongame.comicz.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewSwitcher;

import com.duongame.explorer.R;
import com.duongame.explorer.fragment.BaseFragment;

/**
 * Created by namjungsoo on 2016. 12. 30..
 */

public class SearchFragment extends BaseFragment {
    private ViewGroup rootView;
    private ViewSwitcher switcherContents;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_search, container, false);
        switcherContents = (ViewSwitcher) rootView.findViewById(R.id.switcher_contents);

        onRefresh();
        return rootView;
    }

    @Override
    public void onRefresh() {
        // 현재는 무조건 결과 없음을 리턴함
        if(switcherContents != null) {
            switcherContents.setDisplayedChild(1);
        }
    }
}
