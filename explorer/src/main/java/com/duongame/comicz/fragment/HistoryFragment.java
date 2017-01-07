package com.duongame.comicz.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.duongame.comicz.adapter.HistoryAdapter;
import com.duongame.comicz.db.BookDB;
import com.duongame.explorer.R;
import com.duongame.explorer.fragment.BaseFragment;
import com.duongame.viewer.activity.ZipActivity;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016. 12. 30..
 */

public class HistoryFragment extends BaseFragment {
    private final static String TAG = "HistoryFragment";
    private ViewGroup rootView;
    private ListView listView;
    private HistoryAdapter adapter;
    private ArrayList<BookDB.Book> bookList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_history, container, false);
//        ((TextView) rootView.findViewById(R.id.number)).setText(1 + "");
        listView = (ListView) rootView.findViewById(R.id.list_history);
        adapter = new HistoryAdapter(getActivity(), null);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(bookList != null) {
                    BookDB.Book book = bookList.get(position);
                    Intent intent = new Intent(getActivity(), ZipActivity.class);
                    intent.putExtra("path", book.path);
                    intent.putExtra("name", book.name);
                    intent.putExtra("page", book.page);
                    intent.putExtra("size", book.size);
                    intent.putExtra("extract", book.extract);
                    startActivity(intent);
                }
            }
        });

        refresh();
        return rootView;
    }

    @Override
    public void refresh() {
        bookList = BookDB.getBooks(getActivity());
        adapter.setBookList(bookList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

}
