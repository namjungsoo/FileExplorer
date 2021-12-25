package com.duongame.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.ViewSwitcher;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.duongame.MainApplication;
import com.duongame.R;
import com.duongame.adapter.ExplorerItem;
import com.duongame.adapter.SearchRecyclerAdapter;
import com.duongame.db.BookLoader;
import com.duongame.file.FileExplorer;
import com.duongame.file.LocalExplorer;
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

    private FileExplorer fileExplorer;

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

            FragmentActivity activity = fragment.getActivity();
            if (activity == null) {
                return false;
            }

            try {
                fragment.fileResult = fragment.fileExplorer.setKeyword(keyword)
                        .setExtensions(ext)
                        .setRecursiveDirectory(true)
                        .setExcludeDirectory(true)
                        .setImageListEnable(false)
                        .search(MainApplication.Companion.getInstance().getInitialPath());
            } catch (NullPointerException e) {
                return false;
            }

            //FIX: NPE
            if (fragment.fileResult == null) {
                return false;
            }

            fragment.fileList = fragment.fileResult.fileList;
            if (fragment.fileList != null && fragment.fileList.size() > 0) {
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
                FragmentActivity activity = fragment.getActivity();
                if (activity != null) {
                    fragment.adapter = new SearchRecyclerAdapter(activity, fragment.fileList);
                }
                if (fragment.adapter != null) {
                    fragment.adapter.setOnItemClickListener(new SearchRecyclerAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) {
                            SearchFragment fragment = fragmentWeakReference.get();
                            if (fragment == null) {
                                return;
                            }
                            if (fragment.fileList != null) {
                                ExplorerItem item = fragment.fileList.get(position);
                                FragmentActivity activity = fragment.getActivity();
                                if (activity != null)
                                    BookLoader.load(activity, item, false);
                            }
                        }
                    });
                }
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
        FragmentActivity activity = getActivity();
        if (activity == null)
            return null;

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_search, container, false);
        switcherContents = rootView.findViewById(R.id.switcher_contents);

        recyclerView = rootView.findViewById(R.id.recycler_search);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST));

        spinnerType = rootView.findViewById(R.id.spinner_type);
        editKeyword = rootView.findViewById(R.id.edit_keyword);
        progressBar = rootView.findViewById(R.id.progress_search);

        Button buttonSearch = rootView.findViewById(R.id.btn_search);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentActivity activity = getActivity();
                if (activity == null)
                    return;

                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
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

        fileExplorer = new LocalExplorer();
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
