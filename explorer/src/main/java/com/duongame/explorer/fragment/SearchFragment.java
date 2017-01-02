package com.duongame.explorer.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.duongame.explorer.R;

/**
 * Created by namjungsoo on 2016. 12. 30..
 */

public class SearchFragment extends BaseFragment {
    ViewGroup rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_search, container, false);
//        ((TextView) rootView.findViewById(R.id.number)).setText(2 + "");
        return rootView;
    }

}
