package com.duongame.explorer.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.duongame.GlideApp;
import com.duongame.R;
import com.duongame.explorer.bitmap.BitmapCacheManager;
import com.duongame.explorer.task.thumbnail.LoadApkThumbnailTask;
import com.duongame.explorer.task.thumbnail.LoadPdfThumbnailTask;
import com.duongame.explorer.task.thumbnail.LoadVideoThumbnailTask;
import com.duongame.explorer.task.thumbnail.LoadZipThumbnailTask;
import com.duongame.explorer.view.RoundedImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static com.duongame.explorer.adapter.ExplorerItem.FileType.APK;
import static com.duongame.explorer.adapter.ExplorerItem.FileType.VIDEO;
import static com.duongame.explorer.bitmap.BitmapCacheManager.getDrawable;
import static com.duongame.explorer.bitmap.BitmapCacheManager.getThumbnail;

/**
 * Created by namjungsoo on 2016-11-06.
 */

public class ExplorerAdapter extends RecyclerView.Adapter<ExplorerAdapter.ExplorerViewHolder> {
    private final static String TAG = "ExplorerAdapter";
    private final static boolean DEBUG = false;

    protected ArrayList<ExplorerItem> fileList;

    // file path와 file item을 묶어 놓음
    protected HashMap<String, ExplorerItem> fileMap;

    protected Activity context;

//    private Handler mainHandler;

    private final static int LOAD_BITMAP = 0;
    private final static int LOAD_DRAWABLE = 1;

//    private Queue<BitmapMessage> messageQueue = new ConcurrentLinkedQueue<>();

    OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    // 썸네일이 있는 파일만 true
    private boolean hasThumbnail(ExplorerItem item) {
        switch (item.type) {
            case APK:
            case ZIP:
            case PDF:
            case IMAGE:
            case VIDEO:
                return true;
            default:
                return false;
        }
    }

    private boolean checkBodInCache(ExplorerItem item) {
        if (item.type == APK) {
            Drawable drawable = BitmapCacheManager.getDrawable(item.path);
            if (drawable != null)
                return true;
        } else {
            if (hasThumbnail(item)) {
                Bitmap bitmap = BitmapCacheManager.getThumbnail(item.path);
                if (bitmap != null)
                    return true;
            } else {
                return true;
            }
        }
        return false;
    }

    public ExplorerAdapter(Activity context, ArrayList<ExplorerItem> fileList) {
        this.context = context;
        this.fileList = fileList;
    }

    public static class ExplorerViewHolder extends RecyclerView.ViewHolder {
        public ImageView iconSmall;// 현재 사용안함. 작은 아이콘을 위해서 남겨둠
        public RoundedImageView icon;
        public TextView name;
        public TextView date;
        public TextView size;
        public ExplorerItem.FileType type;
        public int position;

        public ExplorerViewHolder(View itemView) {
            super(itemView);
            icon = (RoundedImageView) itemView.findViewById(R.id.file_icon);
            name = (TextView) itemView.findViewById(R.id.text_name);
            date = (TextView) itemView.findViewById(R.id.text_date);
            size = (TextView) itemView.findViewById(R.id.text_size);
            iconSmall = (ImageView) itemView.findViewById(R.id.file_small_icon);
        }
    }

    @Override
    public ExplorerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflateLayout(parent);
        ExplorerViewHolder holder = new ExplorerViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ExplorerViewHolder holder, int position) {
        bindViewHolderExplorer(holder, position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(holder.position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (fileList == null)
            return 0;

        return fileList.size();
    }

    public void setFileList(ArrayList<ExplorerItem> fileList) {
        this.fileList = fileList;
        fileMap = new HashMap<>();
        for (ExplorerItem item : fileList) {
            fileMap.put(item.path, item);
        }
    }

    void setIconImage(final ExplorerViewHolder viewHolder, final ExplorerItem item) {
        final Bitmap bitmap = getThumbnail(item.path);
        if (bitmap == null) {
            viewHolder.icon.setImageResource(R.drawable.file);

            // Glide로 읽자
            GlideApp.with(context)
                    .load(new File(item.path))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.file)
                    .centerCrop()
                    .into(new ImageViewTarget<Drawable>(viewHolder.icon) {
                        @Override
                        protected void setResource(@Nullable Drawable resource) {
                            if(viewHolder.iconSmall.getTag() == item.path) {
                                getView().setImageDrawable(resource);
                            }
                        }
                    });
        } else {
            viewHolder.icon.setImageBitmap(bitmap);
        }

    }

    void setIconPdf(final ExplorerViewHolder viewHolder, ExplorerItem item) {
        final Bitmap bitmap = getThumbnail(item.path);
        if (bitmap == null) {
            viewHolder.icon.setImageResource(R.drawable.file);

            LoadPdfThumbnailTask task = new LoadPdfThumbnailTask(context, viewHolder.icon, viewHolder.iconSmall);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item.path);
        } else {// 로딩된 비트맵을 셋팅
            viewHolder.icon.setImageBitmap(bitmap);
        }
    }

    void setIconZip(final ExplorerViewHolder viewHolder, ExplorerItem item) {
        final Drawable drawable = getDrawable(item.path);
        if (drawable == null) {
            viewHolder.icon.setImageResource(R.drawable.zip);

            LoadZipThumbnailTask task = new LoadZipThumbnailTask(context, viewHolder.icon, viewHolder.iconSmall);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item.path);
        } else {
            viewHolder.icon.setImageDrawable(drawable);
        }
    }

    void setIconApk(final ExplorerViewHolder viewHolder, ExplorerItem item) {
        final Drawable drawable = getDrawable(item.path);
        if (drawable == null) {
            viewHolder.icon.setImageResource(R.drawable.file);

            LoadApkThumbnailTask task = new LoadApkThumbnailTask(context, viewHolder.icon, viewHolder.iconSmall);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item.path);
        } else {
            viewHolder.icon.setImageDrawable(drawable);
        }
    }

    void setIconVideo(final ExplorerViewHolder viewHolder, ExplorerItem item) {
        final Bitmap bitmap = getThumbnail(item.path);
        if (bitmap == null) {
            viewHolder.icon.setImageResource(R.drawable.file);

            LoadVideoThumbnailTask task = new LoadVideoThumbnailTask(context, viewHolder.icon, viewHolder.iconSmall);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item.path);
        } else {// 로딩된 비트맵을 셋팅
            viewHolder.icon.setImageBitmap(bitmap);
        }
    }

    void setIcon(final ExplorerViewHolder viewHolder, ExplorerItem item) {
        //iconSmall을 icon대신에 tag용으로 사용한다.
        //icon의 tag는 glide가 사용한다.
        viewHolder.iconSmall.setTag(item.path);
        viewHolder.type = item.type;

        if (item.type == ExplorerItem.FileType.IMAGE) {
            setIconImage(viewHolder, item);
        } else if (item.type == VIDEO) {
            setIconVideo(viewHolder, item);
        } else if (item.type == ExplorerItem.FileType.ZIP) {
            setIconZip(viewHolder, item);
        } else if (item.type == ExplorerItem.FileType.PDF) {
            setIconPdf(viewHolder, item);
        } else if (item.type == ExplorerItem.FileType.APK) {
            setIconApk(viewHolder, item);
        } else {
            viewHolder.iconSmall.setTag(null);
            setIconTypeDefault(viewHolder, item);
        }
    }

    void setIconDefault(final ExplorerViewHolder viewHolder, ExplorerItem item) {
        switch (item.type) {
            case DIRECTORY:
                viewHolder.icon.setImageBitmap(BitmapCacheManager.getResourceBitmap(context.getResources(), R.drawable.directory));
                break;
            default:
                viewHolder.icon.setImageBitmap(BitmapCacheManager.getResourceBitmap(context.getResources(), R.drawable.file));
                break;
        }
    }

    void setIconTypeDefault(final ExplorerViewHolder viewHolder, ExplorerItem item) {
        switch (item.type) {
            case AUDIO:
            case FILE:
                viewHolder.icon.setImageBitmap(BitmapCacheManager.getResourceBitmap(context.getResources(), R.drawable.file));
                break;
            case DIRECTORY:
                viewHolder.icon.setImageBitmap(BitmapCacheManager.getResourceBitmap(context.getResources(), R.drawable.directory));
                break;
            case TEXT:
                viewHolder.icon.setImageBitmap(BitmapCacheManager.getResourceBitmap(context.getResources(), R.drawable.text));
                break;
            default:
                return;
        }
    }

    public void bindViewHolderExplorer(ExplorerViewHolder viewHolder, int position) {
    }

    public View inflateLayout(ViewGroup parent) {
        return null;
    }

    public int getFirstVisibleItem(RecyclerView recyclerView) {
        return 0;
    }

    public int getVisibleItemCount(RecyclerView recyclerView) {
        return 0;
    }
}
