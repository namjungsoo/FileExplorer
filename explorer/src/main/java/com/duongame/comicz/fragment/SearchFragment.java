package com.duongame.comicz.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ViewSwitcher;

import com.duongame.R;
import com.duongame.comicz.adapter.SearchRecyclerAdapter;
import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.fragment.BaseFragment;
import com.duongame.explorer.manager.ExplorerManager;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016. 12. 30..
 */

public class SearchFragment extends BaseFragment {
    private ViewGroup rootView;
    private ViewSwitcher switcherContents;
    private RecyclerView recyclerView;

    Spinner spinnerType;
    Button buttonSearch;
    EditText editKeyword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_search, container, false);
        switcherContents = (ViewSwitcher) rootView.findViewById(R.id.switcher_contents);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_search);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        spinnerType = (Spinner) rootView.findViewById(R.id.spinner_type);
        editKeyword = (EditText) rootView.findViewById(R.id.edit_keyword);

        buttonSearch = (Button) rootView.findViewById(R.id.btn_search);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = "." + spinnerType.getSelectedItem().toString().toLowerCase();
                String keyword = editKeyword.getText().toString();

                ArrayList<ExplorerItem> fileList = ExplorerManager.search(ExplorerManager.getInitialPath(), keyword, type, true, true);
                SearchRecyclerAdapter adapter = new SearchRecyclerAdapter(getActivity(), fileList);
                recyclerView.setAdapter(adapter);
                switcherContents.setDisplayedChild(0);
            }
        });

        return rootView;
    }

    @Override
    public void onRefresh() {
        // 현재는 무조건 결과 없음을 리턴함
        if (switcherContents != null) {
            switcherContents.setDisplayedChild(1);
        }
    }
}
