package com.duongame.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.duongame.MainApplication;
import com.duongame.BuildConfig;
import com.duongame.R;
import com.duongame.bitmap.BitmapCacheManager;
import com.duongame.bitmap.BitmapLoader;
import com.duongame.fragment.ExplorerFragment;
import com.duongame.helper.JLog;
import com.duongame.task.thumbnail.LoadGifThumbnailTask;
import com.duongame.task.thumbnail.LoadThumbnailTask;
import com.duongame.task.thumbnail.LoadZipThumbnailTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.duongame.bitmap.BitmapCacheManager.getThumbnail;

/**
 * Created by namjungsoo on 2016-11-06.
 */

public class ExplorerAdapter extends RecyclerView.Adapter<ExplorerAdapter.ExplorerViewHolder> {
    private final static String TAG = "ExplorerAdapter";
    private final static boolean DEBUG = false;

    protected List<ExplorerItem> fileList;
    private HashMap<String, ExplorerItem> fileMap;// file path와 file item을 묶어 놓음
    protected Activity context;

    //private boolean selectMode = false;
    private int mode = ExplorerFragment.MODE_NORMAL;

    private int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onLongItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public void setOnLongItemClickListener(OnItemLongClickListener listener) {
        onLongItemClickListener = listener;
    }

    // 썸네일이 있는 파일만 true
    private boolean hasThumbnail(ExplorerItem item) {
        switch (item.type) {
            case ExplorerItem.FILETYPE_APK:
            case ExplorerItem.FILETYPE_ZIP:
            case ExplorerItem.FILETYPE_PDF:
            case ExplorerItem.FILETYPE_IMAGE:
            case ExplorerItem.FILETYPE_VIDEO:
                return true;
            default:
                return false;
        }
    }

    public ExplorerAdapter(Activity context, ArrayList<ExplorerItem> fileList) {
        this.context = context;
        this.fileList = fileList;
    }

    static class ExplorerViewHolder extends RecyclerView.ViewHolder {
        public ImageView iconSmall;// 현재 사용안함. 작은 아이콘을 위해서 남겨둠
        public ImageView icon;
        public TextView name;
        public TextView date;
        public TextView size;
        public CheckBox check;

        public int type;// FileType
        public int position;

        public ExplorerViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.file_icon);
            name = itemView.findViewById(R.id.text_name);
            date = itemView.findViewById(R.id.text_date);
            size = itemView.findViewById(R.id.text_size);
            iconSmall = itemView.findViewById(R.id.file_small_icon);
            check = itemView.findViewById(R.id.check_file);
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
        if (holder == null)
            return;

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JLog.e(TAG, "onClick " + holder.position);
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(holder.position);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onLongItemClickListener != null) {
                    onLongItemClickListener.onItemLongClick(holder.position);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        if (fileList == null)
            return 0;

        return fileList.size();
    }

    public void setFileList(List<ExplorerItem> fileList) {
        this.fileList = fileList;
        if (fileList == null)
            return;

        fileMap = new HashMap<>();
        for (ExplorerItem item : fileList) {
            fileMap.put(item.path, item);
        }
    }

    protected void updateCheckBox(ExplorerViewHolder viewHolder, ExplorerItem item) {
        if (getMode() == ExplorerFragment.MODE_SELECT) {
//            JLog.e(TAG, "updateCheckBox position=" + item.position + " item=" + item.hashCode() + " " + item.selected);
            viewHolder.check.setVisibility(View.VISIBLE);
            viewHolder.check.setChecked(item.selected);
        } else {
            item.selected = false;
            viewHolder.check.setVisibility(View.INVISIBLE);
            viewHolder.check.setChecked(false);
        }
    }

    // 이전에는 system thumbnail image를 사용하였으나 이번에는 무조건 glide가 읽음
    void setIconImage(final ExplorerViewHolder viewHolder, final ExplorerItem item) {
        final Bitmap bitmap = getThumbnail(item.path);
        if (bitmap == null) {
            if (item.path.endsWith(".gif")) {
                // gif가 없어서 jpg로 처리함
                viewHolder.icon.setImageResource(R.drawable.ic_file_jpg);

                if (isThumbnailEnabled()) {
                    LoadGifThumbnailTask task = new LoadGifThumbnailTask(context, viewHolder.icon, viewHolder.iconSmall);
                    task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, item.path);
                }
            } else {
                if (item.path.endsWith(".png"))
                    viewHolder.icon.setImageResource(R.drawable.ic_file_png);
                else
                    viewHolder.icon.setImageResource(R.drawable.ic_file_jpg);

                if (isThumbnailEnabled()) {
                    // Glide로 읽자
                    Glide.with(context)
                            .load(new File(item.path))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.ic_file_normal)
                            .centerCrop()
                            .into(new ImageViewTarget<Drawable>(viewHolder.icon) {
                                @Override
                                protected void setResource(@Nullable Drawable resource) {
                                    //FIX: destroyed activity error
                                    if (context.isFinishing())
                                        return;
                                    if (resource == null)
                                        return;

                                    if (item.path.equals(viewHolder.iconSmall.getTag())) {
                                        Bitmap bitmap = BitmapLoader.drawableToBitmap(resource);
                                        getView().setImageBitmap(bitmap);
                                        BitmapCacheManager.setThumbnail(item.path, bitmap, viewHolder.icon);
                                    }
                                }
                            });
                }
            }
        } else {
            viewHolder.icon.setImageBitmap(bitmap);
            BitmapCacheManager.setThumbnail(item.path, bitmap, viewHolder.icon);
        }
    }

    void setIconPdf(final ExplorerViewHolder viewHolder, ExplorerItem item) {
        final Bitmap bitmap = getThumbnail(item.path);
        if (bitmap == null) {
            viewHolder.icon.setImageResource(R.drawable.ic_file_pdf);

            if (isThumbnailEnabled()) {
                LoadThumbnailTask task = new LoadThumbnailTask(context, viewHolder.icon, viewHolder.iconSmall, ExplorerItem.FILETYPE_PDF);
                task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, item.path);
            }
        } else {// 로딩된 비트맵을 셋팅
            viewHolder.icon.setImageBitmap(bitmap);
            BitmapCacheManager.setThumbnail(item.path, bitmap, viewHolder.icon);
        }
    }

    boolean isThumbnailEnabled() {
        try {
            return !MainApplication.getInstance(context).isThumbnailDisabled();
        } catch (NullPointerException e) {
            return true;
        }
    }

    void setIconZip(final ExplorerViewHolder viewHolder, ExplorerItem item) {
        JLog.e(TAG, "setIconZip " + item.path);
        final Bitmap bitmap = getThumbnail(item.path);
        if (bitmap == null) {
            viewHolder.icon.setImageResource(R.drawable.ic_file_zip);

            if (BuildConfig.PREVIEW_ZIP && isThumbnailEnabled()) {
                LoadZipThumbnailTask task = new LoadZipThumbnailTask(context, viewHolder.icon, viewHolder.iconSmall);
                task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, item.path);
            }
        } else {
            viewHolder.icon.setImageBitmap(bitmap);
            BitmapCacheManager.setThumbnail(item.path, bitmap, viewHolder.icon);
        }
    }

    void setIconApk(final ExplorerViewHolder viewHolder, ExplorerItem item) {
        final Bitmap bitmap = getThumbnail(item.path);
        if (bitmap == null) {
            viewHolder.icon.setImageResource(R.drawable.ic_file_apk);

            if (isThumbnailEnabled()) {
                LoadThumbnailTask task = new LoadThumbnailTask(context, viewHolder.icon, viewHolder.iconSmall, ExplorerItem.FILETYPE_APK);
                task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, item.path);
            }
        } else {
            viewHolder.icon.setImageBitmap(bitmap);
            BitmapCacheManager.setThumbnail(item.path, bitmap, viewHolder.icon);
        }
    }

    void setIconVideo(final ExplorerViewHolder viewHolder, ExplorerItem item) {
        final Bitmap bitmap = getThumbnail(item.path);
        if (bitmap == null) {
            if (item.path.endsWith(".mp4"))
                viewHolder.icon.setImageResource(R.drawable.ic_file_mp4);
            else if (item.path.endsWith(".fla"))
                viewHolder.icon.setImageResource(R.drawable.ic_file_fla);
            else
                viewHolder.icon.setImageResource(R.drawable.ic_file_avi);

            if (isThumbnailEnabled()) {
                LoadThumbnailTask task = new LoadThumbnailTask(context, viewHolder.icon, viewHolder.iconSmall, ExplorerItem.FILETYPE_VIDEO);
                task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, item.path);
            }
        } else {// 로딩된 비트맵을 셋팅
            viewHolder.icon.setImageBitmap(bitmap);
            BitmapCacheManager.setThumbnail(item.path, bitmap, viewHolder.icon);
        }
    }

    void setIcon(final ExplorerViewHolder viewHolder, ExplorerItem item) {
        //iconSmall을 icon대신에 tag용으로 사용한다.
        //icon의 tag는 glide가 사용한다.
        viewHolder.iconSmall.setTag(item.path);
        viewHolder.type = item.type;

        if (item.type == ExplorerItem.FILETYPE_IMAGE) {
            setIconImage(viewHolder, item);
        } else if (item.type == ExplorerItem.FILETYPE_VIDEO) {
            setIconVideo(viewHolder, item);
        } else if (item.type == ExplorerItem.FILETYPE_ZIP) {
            setIconZip(viewHolder, item);
        } else if (item.type == ExplorerItem.FILETYPE_PDF) {
            setIconPdf(viewHolder, item);
        } else if (item.type == ExplorerItem.FILETYPE_APK) {
            setIconApk(viewHolder, item);
        } else {
            viewHolder.iconSmall.setTag(null);
            setIconTypeDefault(viewHolder, item);
        }
    }

    void setIconDefault(final ExplorerViewHolder viewHolder, ExplorerItem item) {
        switch (item.type) {
            case ExplorerItem.FILETYPE_FOLDER:
                viewHolder.icon.setImageResource(R.drawable.ic_file_folder);
                break;
            default:
                viewHolder.icon.setImageResource(R.drawable.ic_file_normal);
                break;
        }
    }

    void setIconTypeDefault(final ExplorerViewHolder viewHolder, ExplorerItem item) {
        switch (item.type) {
            case ExplorerItem.FILETYPE_AUDIO:
                viewHolder.icon.setImageResource(R.drawable.ic_file_mp3);
                break;
            case ExplorerItem.FILETYPE_FILE:
                viewHolder.icon.setImageResource(R.drawable.ic_file_normal);
                break;
            case ExplorerItem.FILETYPE_FOLDER:
                viewHolder.icon.setImageResource(R.drawable.ic_file_folder);
                break;
            case ExplorerItem.FILETYPE_TEXT:
                viewHolder.icon.setImageResource(R.drawable.ic_file_txt);
                break;
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
