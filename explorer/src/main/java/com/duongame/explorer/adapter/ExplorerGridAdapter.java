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

        setDefaultIcon(item.type, viewHolder.icon);
        setIcon(viewHolder, item, position);
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

//    @Override
//    public void initViewHolder(ExplorerViewHolder viewHolder, View convertView) {
//        viewHolder.icon = (RoundedImageView) convertView.findViewById(R.id.file_icon);
//        viewHolder.name = (TextView) convertView.findViewById(R.id.text_name);
//        viewHolder.iconSmall = (ImageView) convertView.findViewById(R.id.file_small_icon);
//    }
//
//    @Override
//    public void setViewHolder(ExplorerViewHolder viewHolder, ExplorerItem item) {
//        viewHolder.name.setText(item.name);
//        viewHolder.icon.setRadiusDp(5);
//        viewHolder.iconSmall.setVisibility(View.INVISIBLE);
//    }
}
