package com.duongame.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.duongame.AnalyticsApplication;
import com.duongame.R;
import com.duongame.bitmap.BitmapCacheManager;
import com.duongame.file.FileHelper;
import com.duongame.helper.UnitHelper;
import com.duongame.task.thumbnail.LoadThumbnailTask;
import com.duongame.task.thumbnail.LoadZipThumbnailTask;

import java.io.File;
import java.util.ArrayList;

import static com.duongame.bitmap.BitmapCacheManager.getThumbnail;

/**
 * Created by js296 on 2017-08-19.
 */

// PDF, ZIP, TEXT만 검색될수 있다.
public class SearchRecyclerAdapter extends RecyclerView.Adapter<SearchRecyclerAdapter.SearchViewHolder> {
    private Activity context;
    private ArrayList<ExplorerItem> fileList;

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public SearchRecyclerAdapter(Activity context, ArrayList<ExplorerItem> fileList) {
        this.context = context;
        this.fileList = fileList;
    }

    @Override
    public SearchViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = context.getLayoutInflater().inflate(R.layout.file_list_item, viewGroup, false);
        return new SearchViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SearchViewHolder holder, final int i) {
        ExplorerItem item = fileList.get(i);
        String parentPath = new File(item.path).getParent();

        holder.position = i;
        holder.name.setText(item.name);
        holder.path.setText(parentPath);
        holder.date.setText(item.date);
        holder.size.setText(FileHelper.getMinimizedSize(item.size));
        //holder.icon.setRadiusDp(5);
        holder.iconSmall.setVisibility(View.INVISIBLE);
        holder.iconSmall.setTag(item.path);

//        item.imageViewRef = new WeakReference<ImageView>(searchViewHolder.icon);
        switch (item.type) {
            case ExplorerItem.FILETYPE_ZIP:
                setIconZip(holder, item);
                break;
            case ExplorerItem.FILETYPE_PDF:
                setIconPdf(holder, item);
                break;
            case ExplorerItem.FILETYPE_TEXT:
                setIconText(holder, item);
                break;
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(i);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    boolean isThumbnailEnabled() {
        AnalyticsApplication application = (AnalyticsApplication) context.getApplication();
        boolean thumbnail = true;

        // isThumbnail은 disable일때 true이다.
        if (application != null) {
            thumbnail = !application.isThumbnailDisabled();
        }
        return thumbnail;
    }

    void setIconZip(SearchViewHolder holder, ExplorerItem item) {
        final Bitmap bitmap = getThumbnail(item.path);
        if (bitmap == null) {
            holder.icon.setImageResource(R.drawable.ic_file_zip);

            if (isThumbnailEnabled()) {
                LoadZipThumbnailTask task = new LoadZipThumbnailTask(context, holder.icon, holder.iconSmall);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item.path);
            }
        } else {
            holder.icon.setImageBitmap(bitmap);
            BitmapCacheManager.setThumbnail(item.path, bitmap, holder.icon);
        }
    }

    void setIconPdf(SearchViewHolder holder, ExplorerItem item) {
        final Bitmap bitmap = getThumbnail(item.path);
        if (bitmap == null) {
            holder.icon.setImageResource(R.drawable.ic_file_pdf);

            if (isThumbnailEnabled()) {
                LoadThumbnailTask task = new LoadThumbnailTask(context, holder.icon, holder.iconSmall, ExplorerItem.FILETYPE_PDF);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item.path);
            }
        } else {// 로딩된 비트맵을 셋팅
            holder.icon.setImageBitmap(bitmap);
            BitmapCacheManager.setThumbnail(item.path, bitmap, holder.icon);
        }
    }

    void setIconText(SearchViewHolder searchViewHolder, ExplorerItem item) {
        searchViewHolder.icon.setImageResource(R.drawable.ic_file_txt);
    }

    protected static class SearchViewHolder extends RecyclerView.ViewHolder {
        public ImageView iconSmall;// 현재 사용안함. 작은 아이콘을 위해서 남겨둠
        public ImageView icon;
        public TextView name;
        public TextView date;
        public TextView size;
        public TextView path;// 추가됨
        public int type;
        public int position;

        public SearchViewHolder(View itemView) {
            super(itemView);

            icon = itemView.findViewById(R.id.file_icon);
            name = itemView.findViewById(R.id.text_name);
            date = itemView.findViewById(R.id.text_date);
            size = itemView.findViewById(R.id.text_size);
            iconSmall = itemView.findViewById(R.id.file_small_icon);

            name.getLayoutParams().height = UnitHelper.dpToPx(20);
            path = itemView.findViewById(R.id.text_path);
            path.setVisibility(View.VISIBLE);
            path.getLayoutParams().height = UnitHelper.dpToPx(20);

            name.setMaxLines(1);
            name.setSingleLine(true);
            name.setEllipsize(TextUtils.TruncateAt.MIDDLE);
            path.setMaxLines(1);
            path.setSingleLine(true);
            path.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        }
    }
}
