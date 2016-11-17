package com.duongame.fileexplorer.adapter;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.duongame.fileexplorer.bitmap.BitmapCacheManager;
import com.duongame.fileexplorer.ExplorerFileItem;
import com.duongame.fileexplorer.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-06.
 */

public class ExplorerAdapter extends BaseAdapter {
    protected ArrayList<ExplorerFileItem> fileList;
    protected Activity context;
    protected ArrayList<LoadThumbnailTask> taskList = new ArrayList<LoadThumbnailTask> ();

    public class LoadThumbnailTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;

        public LoadThumbnailTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = BitmapCacheManager.getThumbnail(params[0]);
            if(bitmap == null) {
                bitmap = getThumbnail(params[0]);
                BitmapCacheManager.setThumbnail(params[0], bitmap, imageViewReference.get());
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

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
        return null;
    }

    public void setFileList(ArrayList<ExplorerFileItem> fileList) {
        this.fileList = fileList;
    }

    protected Bitmap getThumbnail(String path) {
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

    void setTypeIcon(ExplorerFileItem.FileType type, ImageView icon) {
        if (type == ExplorerFileItem.FileType.FILE)
            icon.setImageResource(R.drawable.file);
        else if (type == ExplorerFileItem.FileType.DIRECTORY)
            icon.setImageResource(R.drawable.directory);

        else if (type == ExplorerFileItem.FileType.ZIP)
            icon.setImageResource(R.drawable.zip);
        else if (type == ExplorerFileItem.FileType.RAR)
            icon.setImageResource(R.drawable.rar);
        else if (type == ExplorerFileItem.FileType.PDF)
            icon.setImageResource(R.drawable.pdf);
        else if (type == ExplorerFileItem.FileType.AUDIO)
            icon.setImageResource(R.drawable.mp3);
        else if (type == ExplorerFileItem.FileType.TEXT)
            icon.setImageResource(R.drawable.text);
    }

    public void stopAllTasks() {
        for(LoadThumbnailTask task : taskList) {
            task.cancel(true);
        }
        taskList.clear();
    }

}
