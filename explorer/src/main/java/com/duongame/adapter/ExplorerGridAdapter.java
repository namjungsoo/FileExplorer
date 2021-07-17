package com.duongame.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
        if (viewHolder == null)
            return;

        if(fileList == null)
            return;

        ExplorerItem item = fileList.get(position);
        if (item == null)
            return;

        viewHolder.name.setText(item.name);
        //viewHolder.icon.setRadiusDp(5);
        viewHolder.iconSmall.setVisibility(View.INVISIBLE);
        viewHolder.position = position;
        item.position = position;

        updateCheckBox(viewHolder, item);

        setIconDefault(viewHolder, item);
        setIcon(viewHolder, item);
    }

    @Override
    public View inflateLayout(ViewGroup parent) {
        return context.getLayoutInflater().inflate(R.layout.item_file_grid, parent, false);
    }

    @Override
    public int getFirstVisibleItem(RecyclerView recyclerView) {
        GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        return layoutManager.findFirstVisibleItemPosition();
    }

    @Override
    public int getVisibleItemCount(RecyclerView recyclerView) {
        int first = getFirstVisibleItem(recyclerView);

        GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        int last = layoutManager.findLastVisibleItemPosition();

        return last - first + 1;
    }
}
