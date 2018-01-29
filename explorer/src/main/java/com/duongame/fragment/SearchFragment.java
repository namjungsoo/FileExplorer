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
import com.duongame.adapter.ExplorerItem;
import com.duongame.adapter.SearchRecyclerAdapter;
import com.duongame.db.BookLoader;
import com.duongame.helper.FileSearcher;
import com.duongame.view.DividerItemDecoration;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016. 12. 30..
 */

public class SearchFragment extends BaseFragment {
    private ViewSwitcher switcherContents;
    private RecyclerView recyclerView;

    private Spinner spinnerType;
    private EditText editKeyword;
    private ProgressBar progressBar;
    private ArrayList<ExplorerItem> fileList;
    private SearchRecyclerAdapter adapter;

    private FileSearcher fileSearcher;

    static class SearchTask extends AsyncTask<Void, Void, Boolean> {
        WeakReference<SearchFragment> fragmentWeakReference;
        String keyword;
        ArrayList<String> ext;

        SearchTask(SearchFragment fragment, String keyword, ArrayList<String> ext) {
            fragmentWeakReference = new WeakReference<SearchFragment>(fragment);
            this.keyword = keyword;
            this.ext = ext;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            SearchFragment fragment = fragmentWeakReference.get();
            if (fragment == null) {
                return false;
            }

            fragment.searchResult = fragment.fileSearcher.setKeyword(keyword)
                    .setExtensions(ext)
                    .setRecursiveDirectory(true)
                    .setExcludeDirectory(true)
                    .setImageListEnable(false)
                    .search(fragment.application.getInitialPath());

            fragment = fragmentWeakReference.get();
            if (fragment == null) {
                return false;
            }

            fragment.fileList = fragment.searchResult.fileList;
            if (fragment.fileList != null && fragment.fileList.size() > 0) {
                fragment.adapter = new SearchRecyclerAdapter(fragment.getActivity(), fragment.fileList);
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            SearchFragment fragment = fragmentWeakReference.get();
            if (fragment == null) {
                return;
            }

            if (result) {
                fragment.adapter.setOnItemClickListener(new SearchRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        SearchFragment fragment = fragmentWeakReference.get();
                        if (fragment == null) {
                            return;
                        }
                        if (fragment.fileList != null) {
                            ExplorerItem item = fragment.fileList.get(position);
                            BookLoader.load(fragment.getActivity(), item, false);
                        }
                    }
                });
                fragment.recyclerView.setAdapter(fragment.adapter);

                fragment.switcherContents.setDisplayedChild(0);
            } else {
                fragment.switcherContents.setDisplayedChild(1);
            }
            fragment.progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_search, container, false);
        switcherContents = (ViewSwitcher) rootView.findViewById(R.id.switcher_contents);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_search);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        spinnerType = (Spinner) rootView.findViewById(R.id.spinner_type);
        editKeyword = (EditText) rootView.findViewById(R.id.edit_keyword);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_search);

        Button buttonSearch = (Button) rootView.findViewById(R.id.btn_search);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(editKeyword.getWindowToken(), 0);

                // 대문자 PDF, ZIP, TXT를 소문자로 수정
                String ext = spinnerType.getSelectedItem().toString().toLowerCase();
                String[] exts = ext.split(",");
                ArrayList<String> extList = new ArrayList<>();
                for (int i = 0; i < exts.length; i++) {
                    extList.add("." + exts[i]);
                }

                String keyword = editKeyword.getText().toString();

                SearchTask task = new SearchTask(SearchFragment.this, keyword, extList);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                progressBar.setVisibility(View.VISIBLE);
                switcherContents.setDisplayedChild(0);
            }
        });

        fileSearcher = new FileSearcher();
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
