package com.duongame.explorer.adapter;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.duongame.explorer.R;
import com.duongame.explorer.bitmap.BitmapCache;
import com.duongame.explorer.task.thumbnail.LoadPdfThumbnailTask;
import com.duongame.explorer.task.thumbnail.LoadThumbnailTask;
import com.duongame.explorer.task.thumbnail.LoadZipThumbnailTask;
import com.duongame.explorer.view.RoundedImageView;

import java.util.ArrayList;
import java.util.HashMap;

import static com.duongame.explorer.adapter.ExplorerItem.FileType.IMAGE;
import static com.duongame.explorer.bitmap.BitmapCache.getThumbnail;

/**
 * Created by namjungsoo on 2016-11-06.
 */

public abstract class ExplorerAdapter extends BaseAdapter {
    private final static String TAG = "ExplorerAdapter";
    protected ArrayList<ExplorerItem> fileList;
    protected Activity context;

    protected HashMap<ImageView, AsyncTask> taskMap = new HashMap<ImageView, AsyncTask>();

    public ExplorerAdapter(Activity context, ArrayList<ExplorerItem> fileList) {
        this.context = context;
        this.fileList = fileList;
    }

    @Override
    public int getCount() {
        if (fileList == null)
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

    protected static class ViewHolder {
        public ImageView small_icon;
        public RoundedImageView icon;
        public TextView name;
        public TextView date;
        public TextView size;
        public ExplorerItem.FileType type;
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

        ExplorerItem item = fileList.get(position);

        setViewHolder(viewHolder, item);
        setIcon(viewHolder, item);

        return convertView;
    }

    public void setFileList(ArrayList<ExplorerItem> fileList) {
        this.fileList = fileList;
    }

    void setIconImage(final ViewHolder viewHolder, ExplorerItem item) {
        // 현재 아이콘 로딩되던 태스크 취소
        if (taskMap.get(viewHolder.icon) != null)
            taskMap.get(viewHolder.icon).cancel(true);

        final Bitmap bitmap = getThumbnail(item.path);
        if (bitmap == null) {
            viewHolder.icon.setImageResource(android.R.color.transparent);

            final LoadThumbnailTask task = new LoadThumbnailTask(context, viewHolder.icon);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item.path);
            taskMap.put(viewHolder.icon, task);
        } else {
//                Log.w(TAG,"cache hit path="+item.path);
            viewHolder.icon.setImageBitmap(bitmap);
        }
    }

    void setIconPdf(final ViewHolder viewHolder, ExplorerItem item) {
        Log.d(TAG, "setIconPdf=" + item.path);

        // 현재 아이콘 로딩되던 태스크 취소
        if (taskMap.get(viewHolder.icon) != null)
            taskMap.get(viewHolder.icon).cancel(true);

        final Bitmap bitmap = getThumbnail(item.path);
        if (bitmap == null) {
            viewHolder.icon.setImageResource(android.R.color.transparent);

            final LoadPdfThumbnailTask task = new LoadPdfThumbnailTask(context, viewHolder.icon);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item.path);
            taskMap.put(viewHolder.icon, task);
            Log.d(TAG, "setIconPdf=" + item.path + " LoadPdfThumbnailTask");
        } else {// 로딩된 비트맵을 셋팅
            viewHolder.icon.setImageBitmap(bitmap);
        }

    }

    void setIconZip(final ViewHolder viewHolder, ExplorerItem item) {
        // 현재 아이콘 로딩되던 태스크 취소
        if (taskMap.get(viewHolder.icon) != null)
            taskMap.get(viewHolder.icon).cancel(true);

        final Bitmap bitmap = getThumbnail(item.path);
        if (bitmap == null) {
            viewHolder.icon.setImageResource(android.R.color.transparent);

            final LoadZipThumbnailTask task = new LoadZipThumbnailTask(context, viewHolder.icon);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item.path);
            taskMap.put(viewHolder.icon, task);
        } else {
//                Log.w(TAG, "ZIP cache hit path=" + item.path);
            viewHolder.icon.setImageBitmap(bitmap);

            //DEBUG
            //BitmapLoader.writeDebugBitmap(item.path, bitmap);
        }

    }

    void setIconApk(final ViewHolder viewHolder, ExplorerItem item) {
        //TODO: 동적으로 읽기
        Drawable drawable = BitmapCache.getDrawable(item.path);
        if (drawable == null) {
            final PackageManager pm = context.getPackageManager();
            final PackageInfo pi = pm.getPackageArchiveInfo(item.path, 0);
            // the secret are these two lines....
            pi.applicationInfo.sourceDir = item.path;
            pi.applicationInfo.publicSourceDir = item.path;
            drawable = pi.applicationInfo.loadIcon(pm);
            BitmapCache.setDrawable(item.path, drawable);
        }
        //viewHolder.icon.setImageBitmap(drawableToBitmap(drawable));
        viewHolder.icon.setImageDrawable(drawable);
    }


    void setIcon(final ViewHolder viewHolder, ExplorerItem item) {
        if (item.type == IMAGE) {
            setIconImage(viewHolder, item);
        } else if (item.type == ExplorerItem.FileType.ZIP) {
            setIconZip(viewHolder, item);
        } else if (item.type == ExplorerItem.FileType.APK) {
            setIconApk(viewHolder, item);
        } else if (item.type == ExplorerItem.FileType.PDF) {
            setIconPdf(viewHolder, item);
        } else {
            // 이전 타입과 다르게 새 타입이 들어왔다면 업데이트 한다.
            if (viewHolder.type != item.type) {
                setTypeIcon(item.type, viewHolder.icon);
            }
        }

        viewHolder.type = item.type;
    }


    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    void setTypeIcon(ExplorerItem.FileType type, ImageView icon) {
        switch (type) {
            case AUDIO:
            case VIDEO:

                // 구현완료된것들은 주석처리
//            case PDF:
//            case TEXT:
//            case IMAGE:
            case FILE:
                icon.setImageBitmap(BitmapCache.getResourceBitmap(context.getResources(), R.drawable.file));
                break;
            case DIRECTORY:
                icon.setImageBitmap(BitmapCache.getResourceBitmap(context.getResources(), R.drawable.directory));
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
            case TEXT:
                icon.setImageBitmap(BitmapCache.getResourceBitmap(context.getResources(), R.drawable.text));
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

    public abstract void setViewHolder(ViewHolder viewHolder, ExplorerItem item);

    public abstract View inflateLayout(ViewGroup parent);
}
