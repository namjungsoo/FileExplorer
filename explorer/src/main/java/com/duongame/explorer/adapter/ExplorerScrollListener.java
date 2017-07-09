package com.duongame.explorer.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * Created by js296 on 2017-07-09.
 */

public class ExplorerScrollListener extends RecyclerView.OnScrollListener {
    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        Log.d("TAG", "onScrollStateChanged");
        ExplorerAdapter adapter = (ExplorerAdapter) recyclerView.getAdapter();
        adapter.scrollStateChanged(recyclerView, newState);
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

    }
}
