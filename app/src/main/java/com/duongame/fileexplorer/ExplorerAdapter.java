package com.duongame.fileexplorer;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by namjungsoo on 2016-11-06.
 */

class ExplorerAdapter extends BaseAdapter {
    private ArrayList<ExplorerFileItem> fileList;
    private Activity context;
    private ExplorerSearcher searcher;

    public ExplorerAdapter(Activity context, ArrayList<ExplorerFileItem> fileList, ExplorerSearcher searcher) {
        this.searcher = searcher;
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
        public SimpleDraweeView icon;
        public TextView name;
        public TextView date;
        public TextView size;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.file_list_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.icon = (SimpleDraweeView) convertView.findViewById(R.id.file_icon);
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
        viewHolder.size.setText(item.size);

        if (item.path == null) {
            item.path = searcher.getLastPath() + "/" + item.name;
            Log.d(TAG, "path=" + item.path);
        }

        if (item.bitmap != null)
            item.bitmap.recycle();

        item.bitmap = getThumbnail(item.path);
        if (item.bitmap != null) {
            viewHolder.icon.setImageBitmap(item.bitmap);
        } else {
            if (item.type == ExplorerFileItem.FileType.NORMAL)
                viewHolder.icon.setImageResource(R.drawable.file);
            else if (item.type == ExplorerFileItem.FileType.DIRECTORY)
                viewHolder.icon.setImageResource(R.drawable.folder);
        }

        return convertView;
    }

    public void setFileList(ArrayList<ExplorerFileItem> fileList) {
        this.fileList = fileList;
    }

    private Bitmap getThumbnail(String path) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.MediaColumns._ID}, MediaStore.MediaColumns.DATA + "=?",
                new String[]{path}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), id, MediaStore.Images.Thumbnails.MICRO_KIND, null);
        }

        cursor.close();
        return null;
    }
}
