package com.duongame.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016. 12. 30..
 */

public class HistoryFragment extends BaseFragment {
    private final static String TAG = "HistoryFragment";
    private final static boolean DEBUG = false;

    private ViewSwitcher switcherContents;

    private HistoryRecyclerAdapter recyclerAdapter;
    private ArrayList<Book> bookList;
    private boolean isHideCompleted = false;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentActivity activity = getActivity();
        if (activity == null)
            return null;

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_history, container, false);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_history);
        isHideCompleted = PreferenceHelper.getHideCompleted(activity);

        Switch switchHide = rootView.findViewById(R.id.switch_hide);
        switchHide.setChecked(isHideCompleted);
        switchHide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isHideCompleted = isChecked;
                FragmentActivity activity = getActivity();
                if (activity == null)
                    return;
                PreferenceHelper.setHideCompleted(activity, isHideCompleted);
                onRefresh();
            }
        });

        recyclerAdapter = new HistoryRecyclerAdapter(activity, this, null);
        recyclerAdapter.setOnItemClickListener(new HistoryRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (bookList != null) {
                    final Book book = bookList.get(position);
                    FragmentActivity activity = getActivity();
                    if (activity == null)
                        return;
                    BookLoader.loadContinue(activity, book);
                }
            }
        });
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST));
        switcherContents = rootView.findViewById(R.id.switcher_contents);

        return rootView;
    }

    static class RefreshTask extends AsyncTask<Void, Void, Void> {

        WeakReference<HistoryFragment> fragmentWeakReferencea;

        RefreshTask(HistoryFragment fragment) {
            fragmentWeakReferencea = new WeakReference<HistoryFragment>(fragment);
        }

        @Override
        protected Void doInBackground(Void... params) {
            HistoryFragment fragment = fragmentWeakReferencea.get();
            if (fragment == null)
                return null;

            FragmentActivity activity = fragment.getActivity();
            if (activity == null)
                return null;

            fragment.bookList = new ArrayList<>();
            ArrayList<Book> historyList = BookDB.getBooks(activity);

            fragment = fragmentWeakReferencea.get();
            if (fragment == null)
                return null;

            for (int i = 0; i < historyList.size(); i++) {
                Book book = historyList.get(i);
                if (fragment.isHideCompleted) {
                    if (book.percent < 100) {
                        fragment.bookList.add(book);
                    }
                } else {
                    fragment.bookList.add(book);
                }
            }

            if (fragment.recyclerAdapter != null) {
                fragment.recyclerAdapter.setBookList(fragment.bookList);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            HistoryFragment fragment = fragmentWeakReferencea.get();
            if (fragment == null)
                return;

            if (fragment.recyclerAdapter != null)
                fragment.recyclerAdapter.notifyDataSetChanged();

            // 결과가 있을때 없을때를 구분해서 SWICTH 함
            if (fragment.bookList != null && fragment.bookList.size() > 0) {
                if (fragment.switcherContents != null) {
                    fragment.switcherContents.setDisplayedChild(0);
                }
            } else {
                if (fragment.switcherContents != null) {
                    fragment.switcherContents.setDisplayedChild(1);
                }
            }
        }
    }

    @Override
    public void onRefresh() {
        RefreshTask task = new RefreshTask(this);
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
