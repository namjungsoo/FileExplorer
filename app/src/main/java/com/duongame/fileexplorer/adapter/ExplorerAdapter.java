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

import com.duongame.fileexplorer.BitmapCache;
import com.duongame.fileexplorer.ExplorerFileItem;
import com.duongame.fileexplorer.ExplorerSearcher;
import com.duongame.fileexplorer.R;
import com.facebook.drawee.view.SimpleDraweeView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-06.
 */

public class ExplorerAdapter extends BaseAdapter {
    protected ArrayList<ExplorerFileItem> fileList;
    protected Activity context;
    protected ExplorerSearcher searcher;

    public class LoadBitmapTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<SimpleDraweeView> imageViewReference;

        public LoadBitmapTask(SimpleDraweeView imageView) {
            imageViewReference = new WeakReference<SimpleDraweeView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = BitmapCache.getBitmap(params[0]);
            if(bitmap == null) {
                bitmap = getThumbnail(params[0]);
                BitmapCache.setBitmap(params[0], bitmap);
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

    void setTypeIcon(ExplorerFileItem.FileType type, SimpleDraweeView icon) {
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

}
