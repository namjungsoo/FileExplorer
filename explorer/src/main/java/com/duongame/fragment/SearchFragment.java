package com.duongame.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.ViewSwitcher;

import com.duongame.R;
import com.duongame.adapter.SearchRecyclerAdapter;
import com.duongame.adapter.ExplorerItem;
import com.duongame.db.BookLoader;
import com.duongame.manager.ExplorerManager;
import com.duongame.view.DividerItemDecoration;

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
    ProgressBar progressBar;
    ArrayList<ExplorerItem> fileList;
    SearchRecyclerAdapter adapter;

    class SearchTask extends AsyncTask<Void, Void, Boolean> {
        String keyword;
        String ext;

        public SearchTask(String keyword, String ext) {
            this.keyword = keyword;
            this.ext = ext;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            fileList = ExplorerManager.search(ExplorerManager.getInitialPath(), keyword, ext, true, true);
            if (fileList.size() > 0) {
                adapter = new SearchRecyclerAdapter(getActivity(), fileList);
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result.booleanValue()) {
                adapter.setOnItemClickListener(new SearchRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        if (fileList != null) {
                            ExplorerItem item = fileList.get(position);
                            BookLoader.load(getActivity(), item, false);
                        }
                    }
                });
                recyclerView.setAdapter(adapter);

                switcherContents.setDisplayedChild(0);
            } else {
                switcherContents.setDisplayedChild(1);
            }
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_search, container, false);
        switcherContents = (ViewSwitcher) rootView.findViewById(R.id.switcher_contents);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_search);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        spinnerType = (Spinner) rootView.findViewById(R.id.spinner_type);
        editKeyword = (EditText) rootView.findViewById(R.id.edit_keyword);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_search);

        buttonSearch = (Button) rootView.findViewById(R.id.btn_search);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editKeyword.getWindowToken(), 0);

                String type = "." + spinnerType.getSelectedItem().toString().toLowerCase();
                String keyword = editKeyword.getText().toString();

                SearchTask task = new SearchTask(keyword, type);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                progressBar.setVisibility(View.VISIBLE);
                switcherContents.setDisplayedChild(0);
            }
        });

        return rootView;
    }

    @Override
    public void onRefresh() {
        // 현재는 무조건 결과 없음을 리턴함
        if (switcherContents != null) {
            if (recyclerView.getAdapter() != null && recyclerView.getAdapter().getItemCount() > 0) {
                switcherContents.setDisplayedChild(0);
            } else {
                switcherContents.setDisplayedChild(1);
            }
        }
    }
}
