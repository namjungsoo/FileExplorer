package com.duongame.fileexplorer.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.duongame.fileexplorer.ExplorerFileItem;
import com.duongame.fileexplorer.R;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-16.
 */

public class ExplorerGridAdapter extends ExplorerAdapter {
    public ExplorerGridAdapter(Activity context, ArrayList<ExplorerFileItem> fileList) {
        super(context, fileList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.file_grid_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.file_icon);
            viewHolder.name = (TextView) convertView.findViewById(R.id.text_name);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ExplorerFileItem item = fileList.get(position);

        viewHolder.name.setText(item.name);

        if(item.type == ExplorerFileItem.FileType.IMAGE) {
            LoadThumbnailTask task = new LoadThumbnailTask(viewHolder.icon);
            task.execute(item.path);
        }

        setTypeIcon(item.type, viewHolder.icon);
        return convertView;
    }
}
