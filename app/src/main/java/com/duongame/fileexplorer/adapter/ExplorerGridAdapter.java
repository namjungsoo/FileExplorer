package com.duongame.fileexplorer.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.duongame.fileexplorer.ExplorerFileItem;
import com.duongame.fileexplorer.ExplorerSearcher;
import com.duongame.fileexplorer.R;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by namjungsoo on 2016-11-16.
 */

public class ExplorerGridAdapter extends ExplorerAdapter {
    public ExplorerGridAdapter(Activity context, ArrayList<ExplorerFileItem> fileList, ExplorerSearcher searcher) {
        super(context, fileList, searcher);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.file_grid_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.icon = (SimpleDraweeView) convertView.findViewById(R.id.file_icon);
            viewHolder.name = (TextView) convertView.findViewById(R.id.text_name);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ExplorerFileItem item = fileList.get(position);

        viewHolder.name.setText(item.name);

        if (item.path == null) {
            item.path = searcher.getLastPath() + "/" + item.name;
            Log.d(TAG, "path=" + item.path);
        }

        LoadBitmapTask task = new LoadBitmapTask(viewHolder.icon);
        task.execute(item.path);

        setTypeIcon(item.type, viewHolder.icon);
        return convertView;
    }
}
