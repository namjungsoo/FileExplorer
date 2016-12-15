package com.duongame.explorer.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.duongame.explorer.R;
import com.duongame.explorer.bitmap.BitmapCacheManager;
import com.duongame.explorer.bitmap.BitmapLoader;
import com.duongame.explorer.bitmap.ZipLoader;
import com.duongame.explorer.view.RoundedImageView;

import net.lingala.zip4j.exception.ZipException;

import java.util.ArrayList;
import java.util.HashMap;

import static com.duongame.explorer.adapter.ExplorerFileItem.FileType.IMAGE;
import static com.duongame.explorer.bitmap.BitmapCacheManager.getThumbnail;

/**
 * Created by namjungsoo on 2016-11-06.
 */

public abstract class ExplorerAdapter extends BaseAdapter {
    protected ArrayList<ExplorerFileItem> fileList;
    protected Activity context;
    protected HashMap<ImageView, AsyncTask> taskMap = new HashMap<ImageView, AsyncTask>();

    public class LoadZipThumbnailTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;
        private final Context context;

        public LoadZipThumbnailTask(Context context, ImageView imageView) {
            this.imageView = imageView;
            this.context = context;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            final String path = params[0];

            if(isCancelled())
                return null;
            Bitmap bitmap = getThumbnail(path);
            if(isCancelled())
                return bitmap;
            if (bitmap == null) {
                String image = null;
                try {
                    //image = ZipLoader.getFirstImage(context, path);
                    ZipLoader loader = new ZipLoader();
                    ArrayList<ExplorerFileItem> imageList = loader.load(context, path, null, true);
                    if(imageList != null && imageList.size() > 0) {
                        image = imageList.get(0).path;
                    }

                } catch (ZipException e) {
                    e.printStackTrace();
                }
                if(isCancelled())
                    return bitmap;

                if (image == null) {
                    bitmap = BitmapCacheManager.getResourceBitmap(context.getResources(), R.drawable.zip);
                    if(bitmap != null)
                        BitmapCacheManager.setThumbnail(path, bitmap, imageView);
                }
                else {
                    bitmap = BitmapLoader.decodeSquareThumbnailFromFile(image, 96, false);
                    if(bitmap != null)
                        BitmapCacheManager.setThumbnail(path, bitmap, imageView);
                }
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (imageView != null && bitmap != null) {
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    public class LoadThumbnailTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;
        private String path;

        public LoadThumbnailTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            path = params[0];

            if(isCancelled())
                return null;

            Bitmap bitmap = getThumbnail(path);
            if(isCancelled())
                return bitmap;

            if (bitmap == null) {
                bitmap = BitmapLoader.getThumbnail(context, path, true);
                if(isCancelled())
                    return bitmap;

                if (bitmap == null) {
                    bitmap = BitmapLoader.decodeSquareThumbnailFromFile(path, 96, true);
                }
                if (bitmap != null) {
                    BitmapCacheManager.setThumbnail(path, bitmap, imageView);
                }
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (imageView != null && bitmap != null) {
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                    imageView.setTag(path);
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
        if(fileList == null)
            return 0;

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

    void setIcon(final ViewHolder viewHolder, ExplorerFileItem item) {
        if (item.type == IMAGE) {
            if(taskMap.get(viewHolder.icon) != null)
                taskMap.get(viewHolder.icon).cancel(true);

            final Bitmap bitmap = getThumbnail(item.path);
            if (bitmap == null) {
                viewHolder.icon.setImageResource(android.R.color.transparent);

                LoadThumbnailTask task = new LoadThumbnailTask(viewHolder.icon);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item.path);
                taskMap.put(viewHolder.icon, task);
            }
            else {
//                Log.w(TAG,"cache hit path="+item.path);
                viewHolder.icon.setImageBitmap(bitmap);
            }
        } else if (item.type == ExplorerFileItem.FileType.ZIP) {
            if(taskMap.get(viewHolder.icon) != null)
                taskMap.get(viewHolder.icon).cancel(true);

            final Bitmap bitmap = getThumbnail(item.path);
            if (bitmap == null) {
                viewHolder.icon.setImageResource(android.R.color.transparent);

                LoadZipThumbnailTask task = new LoadZipThumbnailTask(context, viewHolder.icon);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item.path);
                taskMap.put(viewHolder.icon, task);
            }
            else {
//                Log.w(TAG,"cache hit path="+item.path);
                viewHolder.icon.setImageBitmap(bitmap);
            }
        } else if (item.type == ExplorerFileItem.FileType.APK) {
            //TODO: 동적으로 읽기
            Drawable drawable = BitmapCacheManager.getDrawable(item.path);
            if (drawable == null) {
                final PackageManager pm = context.getPackageManager();
                final PackageInfo pi = pm.getPackageArchiveInfo(item.path, 0);
                // the secret are these two lines....
                pi.applicationInfo.sourceDir = item.path;
                pi.applicationInfo.publicSourceDir = item.path;
                drawable = pi.applicationInfo.loadIcon(pm);
                BitmapCacheManager.setDrawable(item.path, drawable);
            }
            viewHolder.icon.setImageDrawable(drawable);
        } else {
            if (viewHolder.type != item.type) {
                setTypeIcon(item.type, viewHolder.icon);
            }
        }

        viewHolder.type = item.type;
    }

    void setTypeIcon(ExplorerFileItem.FileType type, ImageView icon) {
        switch (type) {
            case IMAGE:
                return;
            case PDF:
            case TEXT:
            case FILE:
                icon.setImageBitmap(BitmapCacheManager.getResourceBitmap(context.getResources(), R.drawable.file));
                break;
            case DIRECTORY:
                icon.setImageBitmap(BitmapCacheManager.getResourceBitmap(context.getResources(), R.drawable.directory));
                break;
//            case ZIP:
//                icon.setImageBitmap(getResourceBitmap(context.getResources(), R.drawable.zip));
//                break;
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
        for (AsyncTask task : taskMap.values()) {
            task.cancel(true);
        }
        taskMap.clear();
    }

    public abstract void initViewHolder(ViewHolder viewHolder, View convertView);

    public abstract void setViewHolder(ViewHolder viewHolder, ExplorerFileItem item);

    public abstract View inflateLayout(ViewGroup parent);
}
