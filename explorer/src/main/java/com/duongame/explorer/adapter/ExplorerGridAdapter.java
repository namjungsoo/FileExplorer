package com.duongame.explorer.adapter;

import android.app.Activity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.duongame.R;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-16.
 */

public class ExplorerGridAdapter extends ExplorerAdapter {
    public ExplorerGridAdapter(Activity context, ArrayList<ExplorerItem> fileList) {
        super(context, fileList);
    }

    public void bindViewHolderExplorer(ExplorerViewHolder viewHolder, int position) {
        ExplorerItem item = fileList.get(position);
        viewHolder.name.setText(item.name);
        viewHolder.icon.setRadiusDp(5);
        viewHolder.iconSmall.setVisibility(View.INVISIBLE);
        viewHolder.position = position;
        item.position = position;

//        item.imageViewRef = new WeakReference<ImageView>(viewHolder.icon);
        setIconDefault(viewHolder, item);
        setIcon(viewHolder, item);
    }

    @Override
    public View inflateLayout(ViewGroup parent) {
        return context.getLayoutInflater().inflate(R.layout.file_grid_item, parent, false);
    }

    @Override
    public int getFirstVisibleItem(RecyclerView recyclerView) {
        GridLayoutManager layoutManager = (GridLayoutManager)recyclerView.getLayoutManager();
        return layoutManager.findFirstVisibleItemPosition();
    }

    @Override
    public int getVisibleItemCount(RecyclerView recyclerView) {
        int first = getFirstVisibleItem(recyclerView);

        GridLayoutManager layoutManager = (GridLayoutManager)recyclerView.getLayoutManager();
        int last = layoutManager.findLastVisibleItemPosition();

        return last - first + 1;
    }
}
