package com.duongame.fileexplorer.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.duongame.fileexplorer.R;
import com.duongame.fileexplorer.helper.FileSizeHelper;
import com.duongame.fileexplorer.view.RoundedImageView;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-16.
 */

public class ExplorerListAdapter extends ExplorerAdapter {
    public ExplorerListAdapter(Activity context, ArrayList<ExplorerFileItem> fileList) {
        super(context, fileList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.file_list_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.icon = (RoundedImageView) convertView.findViewById(R.id.file_icon);
            viewHolder.name = (TextView) convertView.findViewById(R.id.text_name);
            viewHolder.date = (TextView) convertView.findViewById(R.id.text_date);
            viewHolder.size = (TextView) convertView.findViewById(R.id.text_size);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ExplorerFileItem item = fileList.get(position);
        viewHolder.name.setText(item.name);
        viewHolder.date.setText(item.date);
        viewHolder.size.setText(FileSizeHelper.getMinimizedSize(item.size));
        viewHolder.icon.setRadiusDp(5);

        if (item.type == ExplorerFileItem.FileType.IMAGE) {
            LoadThumbnailTask task = new LoadThumbnailTask(viewHolder.icon);
            task.execute(item.path);
            taskList.add(task);
        } else {
            if(viewHolder.type != item.type)
                setTypeIcon(item.type, viewHolder.icon);
        }

        viewHolder.type = item.type;
        return convertView;
    }
}
