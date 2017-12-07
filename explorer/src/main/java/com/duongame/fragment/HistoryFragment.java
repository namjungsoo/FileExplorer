package com.duongame.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.ViewSwitcher;

import com.duongame.R;
import com.duongame.adapter.HistoryRecyclerAdapter;
import com.duongame.db.Book;
import com.duongame.db.BookDB;
import com.duongame.db.BookLoader;
import com.duongame.helper.PreferenceHelper;
import com.duongame.view.DividerItemDecoration;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016. 12. 30..
 */

public class HistoryFragment extends BaseFragment {
    private final static String TAG = "HistoryFragment";
    private final static boolean DEBUG = false;

    private ViewGroup rootView;

    private RecyclerView recyclerView;
    private ViewSwitcher switcherContents;
    private Switch switchHide;

    private HistoryRecyclerAdapter recyclerAdapter;
    private ArrayList<Book> bookList;
    private boolean isHideCompleted = false;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_history, container, false);
        //listView = (ListView) rootView.findViewById(R.id.list_history);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_history);
        isHideCompleted = PreferenceHelper.getHideCompleted(getActivity());

        switchHide = (Switch) rootView.findViewById(R.id.switch_hide);
        switchHide.setChecked(isHideCompleted);
        switchHide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isHideCompleted = isChecked;
                PreferenceHelper.setHideCompleted(getActivity(), isHideCompleted);
                onRefresh();
            }
        });

        recyclerAdapter = new HistoryRecyclerAdapter(getActivity(), this, null);
        recyclerAdapter.setOnItemClickListener(new HistoryRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (bookList != null) {
                    final Book book = bookList.get(position);
                    BookLoader.loadContinue(getActivity(), book);
                }
            }
        });
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        switcherContents = (ViewSwitcher) rootView.findViewById(R.id.switcher_contents);

        return rootView;
    }

    class RefreshTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            bookList = new ArrayList<>();
            ArrayList<Book> historyList = BookDB.getBooks(getActivity());

            for (int i = 0; i < historyList.size(); i++) {
                Book book = historyList.get(i);
                if (isHideCompleted) {
                    if (book.percent < 100) {
                        bookList.add(book);
                    }
                } else {
                    bookList.add(book);
                }
            }

            if (recyclerAdapter != null) {
                recyclerAdapter.setBookList(bookList);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (recyclerAdapter != null)
                recyclerAdapter.notifyDataSetChanged();

            // 결과가 있을때 없을때를 구분해서 SWICTH 함
            if (bookList != null && bookList.size() > 0) {
                if (switcherContents != null) {
                    switcherContents.setDisplayedChild(0);
                }
            } else {
                if (switcherContents != null) {
                    switcherContents.setDisplayedChild(1);
                }
            }
        }
    }

    @Override
    public void onRefresh() {
        RefreshTask task = new RefreshTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        onRefresh();
    }
}
