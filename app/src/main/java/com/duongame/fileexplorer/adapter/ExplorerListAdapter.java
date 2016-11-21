package com.duongame.fileexplorer.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.duongame.fileexplorer.R;
import com.duongame.fileexplorer.bitmap.BitmapCacheManager;
import com.duongame.fileexplorer.helper.FileSizeHelper;
import com.duongame.fileexplorer.view.RoundedImageView;

import java.util.ArrayList;

import static com.duongame.fileexplorer.adapter.ExplorerFileItem.FileType.ZIP;

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

        if (item.type == ExplorerFileItem.FileType.IMAGE) {
            Bitmap bitmap = BitmapCacheManager.getThumbnail(item.path);
            if (bitmap == null) {
                LoadThumbnailTask task = new LoadThumbnailTask(viewHolder.icon);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item.path);
                taskList.add(task);
            } else {
                viewHolder.icon.setImageBitmap(bitmap);
            }
        } else if (item.type == ZIP) {
            Bitmap bitmap = BitmapCacheManager.getThumbnail(item.path);
            if (bitmap == null) {
                LoadZipThumbnailTask task = new LoadZipThumbnailTask(context, viewHolder.icon, viewHolder.small_icon);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item.path);
                taskList.add(task);
            } else {
                viewHolder.icon.setImageBitmap(bitmap);
                // 사용시 느려짐
//                setTypeIcon(ZIP, viewHolder.small_icon);
//                viewHolder.small_icon.setVisibility(View.VISIBLE);
            }
        } else {
            if (viewHolder.type != item.type)
                setTypeIcon(item.type, viewHolder.icon);
        }

        viewHolder.type = item.type;
    }
}
