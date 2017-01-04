package com.duongame.explorer.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.duongame.explorer.R;
import com.duongame.explorer.adapter.HistoryAdapter;
import com.duongame.explorer.db.BookDB;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016. 12. 30..
 */

public class HistoryFragment extends BaseFragment {
    private ViewGroup rootView;
    private ListView listView;
    private HistoryAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_history, container, false);
//        ((TextView) rootView.findViewById(R.id.number)).setText(1 + "");
        listView = (ListView) rootView.findViewById(R.id.list_history);
        adapter = new HistoryAdapter(getActivity(), null);
        listView.setAdapter(adapter);

        refresh();
        return rootView;
    }

    @Override
    public void refresh() {
        final ArrayList<BookDB.Book> bookList = BookDB.getBooks(getActivity());
        adapter.setBookList(bookList);
        adapter.notifyDataSetChanged();
    }
}

