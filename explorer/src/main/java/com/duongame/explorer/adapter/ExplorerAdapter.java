package com.duongame.explorer.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.duongame.R;
import com.duongame.explorer.bitmap.BitmapCacheManager;
import com.duongame.explorer.bitmap.BitmapLoader;
import com.duongame.explorer.bitmap.BitmapMessage;
import com.duongame.explorer.task.thumbnail.LoadApkThumbnailTask;
import com.duongame.explorer.task.thumbnail.LoadPdfThumbnailTask;
import com.duongame.explorer.task.thumbnail.LoadVideoThumbnailTask;
import com.duongame.explorer.task.thumbnail.LoadZipThumbnailTask;
import com.duongame.explorer.view.RoundedImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.duongame.explorer.adapter.ExplorerItem.FileType.APK;
import static com.duongame.explorer.adapter.ExplorerItem.FileType.VIDEO;
import static com.duongame.explorer.bitmap.BitmapCacheManager.getDrawable;
import static com.duongame.explorer.bitmap.BitmapCacheManager.getThumbnail;
import static com.duongame.explorer.bitmap.BitmapLoader.loadThumbnail;

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

    private Handler mainHandler;

    private final static int LOAD_BITMAP = 0;
    private final static int LOAD_DRAWABLE = 1;

    private Queue<BitmapMessage> messageQueue = new ConcurrentLinkedQueue<>();

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

        // 받은 메세지로 imageview에 bitmap을 셋팅
        mainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                final BitmapMessage bitmapMessage = (BitmapMessage) msg.obj;
                if (bitmapMessage == null) {
                    Log.e(TAG, "bitmapMessage == null");
                    return;
                }

                if (bitmapMessage.imageView == null) {
                    Log.e(TAG, "bitmapMessage.imageView == null");
                    return;
                }

                if (fileMap == null) {
                    Log.e(TAG, "fileMap == null");
                    return;
                }

                if (!fileMap.containsKey(bitmapMessage.path)) {
                    Log.e(TAG, "!fileMap.containsKey(bitmapMessage.path)");
                    return;
                }

                if (fileMap.get(bitmapMessage.path).imageViewRef == null) {
                    Log.e(TAG, "imageViewRef == null");
                    return;
                }

                if (fileMap.get(bitmapMessage.path).imageViewRef.get() != bitmapMessage.imageView) {
                    Log.e(TAG, "imageViewRef.get() != bitmapMessage.imageView");
                    return;
                }

                if (msg.arg1 == LOAD_BITMAP) {
                    if (bitmapMessage.bitmap == null) {
                        Log.e(TAG, "bitmap == null");
                        return;
                    }

                    bitmapMessage.imageView.setImageBitmap(bitmapMessage.bitmap);
                } else if (msg.arg1 == LOAD_DRAWABLE) {
                    if (bitmapMessage.drawable == null) {
                        Log.e(TAG, "drawable == null");
                        return;
                    }

                    bitmapMessage.imageView.setImageDrawable(bitmapMessage.drawable);
                }
            }
        };

        Log.d(TAG, "Thread Start");
    }

    private void handleBitmapMessage(BitmapMessage bitmapMessage) {
        if (bitmapMessage == null)
            return;

        Message mainMsg = new Message();
        mainMsg.obj = bitmapMessage;
        mainMsg.arg1 = LOAD_BITMAP;
        BitmapLoader.BitmapOrDrawable bod = loadThumbnail(context, bitmapMessage.type, bitmapMessage.path);

        switch (bitmapMessage.type) {
            case APK: {
                mainMsg.arg1 = LOAD_DRAWABLE;
                bitmapMessage.drawable = bod.drawable;
            }
            break;
            case PDF:
            case IMAGE:
            case VIDEO:
            case ZIP:
                bitmapMessage.bitmap = bod.bitmap;
                break;
        }

        mainHandler.sendMessage(mainMsg);
    }

    protected static class ExplorerViewHolder extends RecyclerView.ViewHolder {
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

    void setIconImage(final ExplorerViewHolder explorerViewHolder, ExplorerItem item) {
        final Bitmap bitmap = getThumbnail(item.path);
        if (bitmap == null) {
            explorerViewHolder.icon.setImageResource(R.drawable.file);

            // Glide로 읽자
            Glide.with(context)
                    .load(new File(item.path))
                    .placeholder(R.drawable.file)
                    .centerCrop()
                    .into(explorerViewHolder.icon);
        } else {
            explorerViewHolder.icon.setImageBitmap(bitmap);
        }

    }

    void setIconPdf(final ExplorerViewHolder explorerViewHolder, ExplorerItem item) {
        final Bitmap bitmap = getThumbnail(item.path);
        if (bitmap == null) {
            explorerViewHolder.icon.setImageResource(R.drawable.file);

            LoadPdfThumbnailTask task = new LoadPdfThumbnailTask(context, explorerViewHolder.icon);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item.path);
        } else {// 로딩된 비트맵을 셋팅
            explorerViewHolder.icon.setImageBitmap(bitmap);
        }
    }

    void setIconZip(final ExplorerViewHolder explorerViewHolder, ExplorerItem item) {
        final Drawable drawable = getDrawable(item.path);
        if (drawable == null) {
            explorerViewHolder.icon.setImageResource(R.drawable.zip);

            LoadZipThumbnailTask task = new LoadZipThumbnailTask(context, explorerViewHolder.icon);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item.path);
        } else {
            Log.d(TAG, "setIconZip cache found");
            explorerViewHolder.icon.setImageDrawable(drawable);
        }
    }

    void setIconApk(final ExplorerViewHolder explorerViewHolder, ExplorerItem item) {
        Drawable drawable = getDrawable(item.path);
        if (drawable == null) {
            explorerViewHolder.icon.setImageResource(R.drawable.file);

            LoadApkThumbnailTask task = new LoadApkThumbnailTask(context, explorerViewHolder.icon);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item.path);
        } else {
            explorerViewHolder.icon.setImageDrawable(drawable);
        }
    }

    void setIconVideo(final ExplorerViewHolder explorerViewHolder, ExplorerItem item) {
        final Bitmap bitmap = getThumbnail(item.path);
        if (bitmap == null) {
            explorerViewHolder.icon.setImageResource(R.drawable.file);

            LoadVideoThumbnailTask task = new LoadVideoThumbnailTask(context, explorerViewHolder.icon);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item.path);
        } else {// 로딩된 비트맵을 셋팅
            explorerViewHolder.icon.setImageBitmap(bitmap);
        }
    }

    void setIcon(final ExplorerViewHolder explorerViewHolder, ExplorerItem item) {
        if (item.type == ExplorerItem.FileType.IMAGE) {
            setIconImage(explorerViewHolder, item);
        } else if (item.type == VIDEO) {
            setIconVideo(explorerViewHolder, item);
        } else if (item.type == ExplorerItem.FileType.ZIP) {
            setIconZip(explorerViewHolder, item);
        } else if (item.type == ExplorerItem.FileType.PDF) {
            setIconPdf(explorerViewHolder, item);
        } else if (item.type == ExplorerItem.FileType.APK) {
            setIconApk(explorerViewHolder, item);
        } else {
            // 이전 타입과 다르게 새 타입이 들어왔다면 업데이트 한다.
            //if (explorerViewHolder.type != item.type) {
            setTypeIcon(item.type, explorerViewHolder.icon);
            //}
        }

        explorerViewHolder.type = item.type;
    }

    void setDefaultIcon(ExplorerItem.FileType type, ImageView icon) {
        switch (type) {
            case DIRECTORY:
                icon.setImageBitmap(BitmapCacheManager.getResourceBitmap(context.getResources(), R.drawable.directory));
                break;
            default:
                icon.setImageBitmap(BitmapCacheManager.getResourceBitmap(context.getResources(), R.drawable.file));
                break;
        }
    }

    void setTypeIcon(ExplorerItem.FileType type, ImageView icon) {
        switch (type) {
            case AUDIO:
            case FILE:
                icon.setImageBitmap(BitmapCacheManager.getResourceBitmap(context.getResources(), R.drawable.file));
                break;
            case DIRECTORY:
                icon.setImageBitmap(BitmapCacheManager.getResourceBitmap(context.getResources(), R.drawable.directory));
                break;
            case TEXT:
                icon.setImageBitmap(BitmapCacheManager.getResourceBitmap(context.getResources(), R.drawable.text));
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
