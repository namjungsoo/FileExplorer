package com.duongame.fileexplorer;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-06.
 */

class ExplorerAdapter extends BaseAdapter {
    private ArrayList<ExplorerFileItem> fileList;
    private Activity context;

    public ExplorerAdapter(Activity context, ArrayList<ExplorerFileItem> fileList) {
        this.context = context;
        this.fileList = fileList;
    }

    @Override
    public int getCount() {
        return fileList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public static class ViewHolder {
        public ImageView icon;
        public TextView name;
        public TextView date;
        public TextView size;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.file_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.icon = (ImageView)convertView.findViewById(R.id.file_icon);
            viewHolder.name = (TextView)convertView.findViewById(R.id.text_name);
            viewHolder.date = (TextView)convertView.findViewById(R.id.text_date);
            viewHolder.size = (TextView)convertView.findViewById(R.id.text_size);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        ExplorerFileItem item = fileList.get(position);

        viewHolder.name.setText(item.name);
        viewHolder.date.setText(item.date);
        viewHolder.size.setText(item.size);
        if(item.type == ExplorerFileItem.FileType.NORMAL)
            viewHolder.icon.setImageResource(R.drawable.file);
        else if(item.type == ExplorerFileItem.FileType.DIRECTORY)
            viewHolder.icon.setImageResource(R.drawable.folder);

        return convertView;
    }

    public void setFileList(ArrayList<ExplorerFileItem> fileList) {
        this.fileList = fileList;
    }
}
