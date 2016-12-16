package com.duongame.explorer.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.duongame.explorer.R;
import com.duongame.explorer.helper.FileSizeHelper;
import com.duongame.explorer.view.RoundedImageView;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-16.
 */

public class ExplorerListAdapter extends ExplorerAdapter {
    public ExplorerListAdapter(Activity context, ArrayList<ExplorerFileItem> fileList) {
        super(context, fileList);
    }

    @Override
    public View inflateLayout(ViewGroup parent) {
        return context.getLayoutInflater().inflate(R.layout.file_list_item, parent, false);
    }

    @Override
    public void initViewHolder(ViewHolder viewHolder, View convertView) {
        viewHolder.icon = (RoundedImageView) convertView.findViewById(R.id.file_icon);
        viewHolder.name = (TextView) convertView.findViewById(R.id.text_name);
        viewHolder.date = (TextView) convertView.findViewById(R.id.text_date);
        viewHolder.size = (TextView) convertView.findViewById(R.id.text_size);
        viewHolder.small_icon = (ImageView) convertView.findViewById(R.id.file_small_icon);
    }

    @Override
    public void setViewHolder(ViewHolder viewHolder, ExplorerFileItem item) {
        viewHolder.name.setText(item.name);
        viewHolder.date.setText(item.date);
        viewHolder.size.setText(FileSizeHelper.getMinimizedSize(item.size));
        viewHolder.icon.setRadiusDp(5);
        viewHolder.small_icon.setVisibility(View.INVISIBLE);
    }
}