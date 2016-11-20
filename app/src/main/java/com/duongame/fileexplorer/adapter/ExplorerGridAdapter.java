package com.duongame.fileexplorer.adapter;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.duongame.fileexplorer.R;
import com.duongame.fileexplorer.view.RoundedImageView;

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
            viewHolder.icon = (RoundedImageView) convertView.findViewById(R.id.file_icon);
            viewHolder.name = (TextView) convertView.findViewById(R.id.text_name);
            viewHolder.small_icon = (ImageView) convertView.findViewById(R.id.file_small_icon);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ExplorerFileItem item = fileList.get(position);
        viewHolder.name.setText(item.name);
        viewHolder.icon.setRadiusDp(5);
        viewHolder.small_icon.setVisibility(View.INVISIBLE);

        if (item.type == ExplorerFileItem.FileType.IMAGE) {
            LoadThumbnailTask task = new LoadThumbnailTask(viewHolder.icon);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item.path);
            taskList.add(task);
        } else if(item.type == ExplorerFileItem.FileType.ZIP) {
            LoadZipThumbnailTask task = new LoadZipThumbnailTask(context, viewHolder.icon, viewHolder.small_icon);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item.path);
            taskList.add(task);
        } else {
            if (viewHolder.type != item.type)
                setTypeIcon(item.type, viewHolder.icon);
        }

        viewHolder.type = item.type;
        return convertView;
    }
}
