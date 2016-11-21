package com.duongame.fileexplorer.adapter;

import android.app.Activity;
import android.content.Context;
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
import com.duongame.fileexplorer.bitmap.ZipLoader;
import com.duongame.fileexplorer.view.RoundedImageView;

import net.lingala.zip4j.exception.ZipException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.duongame.fileexplorer.adapter.ExplorerFileItem.FileType.ZIP;
import static com.duongame.fileexplorer.bitmap.BitmapCacheManager.getResourceBitmap;

/**
 * Created by namjungsoo on 2016-11-06.
 */

public abstract class ExplorerAdapter extends BaseAdapter {
    protected ArrayList<ExplorerFileItem> fileList;
    protected Activity context;
    protected ArrayList<AsyncTask> taskList = new ArrayList<AsyncTask>();

    public class LoadZipThumbnailTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private final WeakReference<ImageView> smallImageViewReference;
        private final Context context;

        public LoadZipThumbnailTask(Context context, ImageView imageView, ImageView smallImageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
            smallImageViewReference = new WeakReference<ImageView>(smallImageView);
            this.context = context;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            final String path = params[0];

            Bitmap bitmap = BitmapCacheManager.getThumbnail(path);
            if (bitmap == null) {
                String image = null;
                try {
                    image = ZipLoader.getFirstImage(context, path);
                } catch (ZipException e) {
                    e.printStackTrace();
                }

                if (image == null) {
                    return null;
                }

                bitmap = BitmapLoader.decodeSquareThumbnailFromFile(image, 96);
                if (bitmap != null) {
                    BitmapCacheManager.setThumbnail(path, bitmap, imageViewReference.get());
                }
            } else {
//                Log.d("TAG", "getThumbnail OK path="+path);
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
//                setTypeIcon(ZIP, smallImageViewReference.get());
//                smallImageViewReference.get().setVisibility(View.VISIBLE);
            } else {
                setTypeIcon(ZIP, imageViewReference.get());
                smallImageViewReference.get().setVisibility(View.INVISIBLE);
            }
        }
    }

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
                if (bitmap == null) {
                    bitmap = BitmapLoader.decodeSquareThumbnailFromFile(path, 96);
                }
                if (bitmap != null) {
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
        public ImageView small_icon;
        public RoundedImageView icon;
        public TextView name;
        public TextView date;
        public TextView size;
        public ExplorerFileItem.FileType type;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflateLayout(parent);

            viewHolder = new ViewHolder();
            initViewHolder(viewHolder, convertView);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ExplorerFileItem item = fileList.get(position);

        setViewHolder(viewHolder, item);
        setIcon(viewHolder, item);

        return convertView;
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

    void setIcon(ViewHolder viewHolder, ExplorerFileItem item) {
        if (item.type == ExplorerFileItem.FileType.IMAGE) {
            Bitmap bitmap = BitmapCacheManager.getThumbnail(item.path);
            if (bitmap == null) {
                LoadThumbnailTask task = new LoadThumbnailTask(viewHolder.icon);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item.path);
                taskList.add(task);
            } else {
                viewHolder.icon.setImageBitmap(bitmap);
            }
        } else if (item.type == ExplorerFileItem.FileType.ZIP) {
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

    void setTypeIcon(ExplorerFileItem.FileType type, ImageView icon) {
        switch (type) {
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
        for (AsyncTask task : taskList) {
            task.cancel(true);
        }
        taskList.clear();
    }

    public abstract void initViewHolder(ViewHolder viewHolder, View convertView);

    public abstract void setViewHolder(ViewHolder viewHolder, ExplorerFileItem item);

    public abstract View inflateLayout(ViewGroup parent);
}
