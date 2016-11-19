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

import com.duongame.fileexplorer.R;
import com.duongame.fileexplorer.bitmap.BitmapCacheManager;
import com.duongame.fileexplorer.bitmap.BitmapLoader;
import com.duongame.fileexplorer.view.RoundedImageView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.duongame.fileexplorer.bitmap.BitmapCacheManager.getResourceBitmap;

/**
 * Created by namjungsoo on 2016-11-06.
 */

public class ExplorerAdapter extends BaseAdapter {
    protected ArrayList<ExplorerFileItem> fileList;
    protected Activity context;
    protected ArrayList<LoadThumbnailTask> taskList = new ArrayList<LoadThumbnailTask>();

    public class LoadThumbnailTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;

        public LoadThumbnailTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            final String path = params[0];

            Bitmap bitmap = BitmapCacheManager.getThumbnail(path);
            if (bitmap == null) {
                bitmap = getThumbnail(path);
                if(bitmap == null) {
                    //bitmap = BitmapLoader.decodeSampleBitmapFromFile(path, 96, 96);// MICRO_KIND
                    bitmap = BitmapLoader.decodeSquareThumbnailFromFile(path, 96);
                }
                if(bitmap != null) {
                    BitmapCacheManager.setThumbnail(path, bitmap, imageViewReference.get());
                }
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
        public RoundedImageView icon;
        public TextView name;
        public TextView date;
        public TextView size;
        public ExplorerFileItem.FileType type;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    public void setFileList(ArrayList<ExplorerFileItem> fileList) {
        this.fileList = fileList;
    }

    protected Bitmap getThumbnail(String path) {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.MediaColumns._ID},
                MediaStore.MediaColumns.DATA + "=?",
                new String[]{path}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));

            cursor.close();
//            cursor = context.getContentResolver().query(
//                    MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
//                    new String[]{MediaStore.Images.Thumbnails.DATA},
//                    MediaStore.Images.Thumbnails.IMAGE_ID + "=?",
//                    new String[]{String.valueOf(id)}, null);
//            if (cursor != null && cursor.moveToFirst()) {
//                String fullPath = cursor.getString(0);
//                cursor.close();
//                return BitmapFactory.decodeFile(fullPath);
//            }
            return MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), id, MediaStore.Images.Thumbnails.MICRO_KIND, null);
        }

        cursor.close();
        return null;
    }

    void setTypeIcon(ExplorerFileItem.FileType type, ImageView icon) {
        switch(type) {
            case IMAGE:
                return;
            case FILE:
                icon.setImageBitmap(getResourceBitmap(context.getResources(), R.drawable.file));
                break;
            case DIRECTORY:
                icon.setImageBitmap(getResourceBitmap(context.getResources(), R.drawable.directory));
                break;
            case ZIP:
                icon.setImageBitmap(getResourceBitmap(context.getResources(), R.drawable.zip));
                break;
//            case RAR:
//                icon.setImageBitmap(getResourceBitmap(context.getResources(), R.drawable.rar));
//                break;
//            case PDF:
//                icon.setImageBitmap(getResourceBitmap(context.getResources(), R.drawable.pdf));
//                break;
//            case AUDIO:
//                icon.setImageBitmap(getResourceBitmap(context.getResources(), R.drawable.mp3));
//                break;
//            case TEXT:
//                icon.setImageBitmap(getResourceBitmap(context.getResources(), R.drawable.text));
//                break;
            case VIDEO:
                break;
            default:
                return;
        }
    }

    public void stopAllTasks() {
        for (LoadThumbnailTask task : taskList) {
            task.cancel(true);
        }
        taskList.clear();
    }

}
