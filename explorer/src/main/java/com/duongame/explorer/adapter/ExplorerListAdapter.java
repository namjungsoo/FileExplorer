package com.duongame.explorer.adapter;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.duongame.R;
import com.duongame.explorer.helper.FileHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-16.
 */

public class ExplorerListAdapter extends ExplorerAdapter {
    public ExplorerListAdapter(Activity context, ArrayList<ExplorerItem> fileList) {
        super(context, fileList);
    }

    public void bindViewHolderExplorer(ExplorerViewHolder viewHolder, int position) {
        ExplorerItem item = fileList.get(position);
        viewHolder.name.setText(item.name);
        viewHolder.date.setText(item.date);
        viewHolder.size.setText(FileHelper.getMinimizedSize(item.size));
        viewHolder.icon.setRadiusDp(5);
        viewHolder.iconSmall.setVisibility(View.INVISIBLE);
        viewHolder.position = position;

        item.imageViewRef = new WeakReference<ImageView>(viewHolder.icon);
        setDefaultIcon(item.type, viewHolder.icon);
        setIcon(viewHolder, item);
    }

    @Override
    public View inflateLayout(ViewGroup parent) {
        return context.getLayoutInflater().inflate(R.layout.file_list_item, parent, false);
    }

    @Override
    public int getFirstVisibleItem(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();
        return layoutManager.findFirstVisibleItemPosition();
    }

    @Override
    public int getVisibleItemCount(RecyclerView recyclerView) {
        int first = getFirstVisibleItem(recyclerView);

        LinearLayoutManager layoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();
        int last = layoutManager.findLastVisibleItemPosition();

        return last - first + 1;
    }
}
