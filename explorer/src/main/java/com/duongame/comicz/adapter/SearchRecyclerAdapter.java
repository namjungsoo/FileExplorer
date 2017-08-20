package com.duongame.comicz.adapter;

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

import com.duongame.R;
import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.bitmap.BitmapCacheManager;
import com.duongame.explorer.helper.FileHelper;
import com.duongame.explorer.task.thumbnail.LoadPdfThumbnailTask;
import com.duongame.explorer.task.thumbnail.LoadZipThumbnailTask;
import com.duongame.explorer.view.RoundedImageView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.duongame.explorer.bitmap.BitmapCacheManager.getDrawable;
import static com.duongame.explorer.bitmap.BitmapCacheManager.getThumbnail;

/**
 * Created by js296 on 2017-08-19.
 */

// PDF, ZIP, TEXT만 검색될수 있다.
public class SearchRecyclerAdapter extends RecyclerView.Adapter<SearchRecyclerAdapter.SearchViewHolder> {
    Activity context;
    ArrayList<ExplorerItem> fileList;

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
    public void onBindViewHolder(SearchViewHolder searchViewHolder, int i) {
        ExplorerItem item = fileList.get(i);
        String parentPath = new File(item.path).getParent();

        searchViewHolder.position = i;
        searchViewHolder.name.setText(item.name);
        searchViewHolder.path.setText(parentPath);
        searchViewHolder.date.setText(item.date);
        searchViewHolder.size.setText(FileHelper.getMinimizedSize(item.size));
        searchViewHolder.icon.setRadiusDp(5);
        searchViewHolder.iconSmall.setVisibility(View.INVISIBLE);

        item.imageViewRef = new WeakReference<ImageView>(searchViewHolder.icon);
        switch(item.type) {
            case ZIP:
                setIconZip(searchViewHolder, item);
                break;
            case PDF:
                setIconPdf(searchViewHolder, item);
                break;
            case TEXT:
                setIconText(searchViewHolder, item);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    void setIconZip(SearchViewHolder searchViewHolder, ExplorerItem item) {
        final Drawable drawable = getDrawable(item.path);
        if (drawable == null) {
            searchViewHolder.icon.setImageResource(R.drawable.zip);

            LoadZipThumbnailTask task = new LoadZipThumbnailTask(context, searchViewHolder.icon);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item.path);
        } else {
            searchViewHolder.icon.setImageDrawable(drawable);
        }
    }

    void setIconPdf(SearchViewHolder searchViewHolder, ExplorerItem item) {
        final Bitmap bitmap = getThumbnail(item.path);
        if (bitmap == null) {
            searchViewHolder.icon.setImageResource(R.drawable.file);

            LoadPdfThumbnailTask task = new LoadPdfThumbnailTask(context, searchViewHolder.icon);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item.path);
        } else {// 로딩된 비트맵을 셋팅
            searchViewHolder.icon.setImageBitmap(bitmap);
        }
    }

    void setIconText(SearchViewHolder searchViewHolder, ExplorerItem item) {
        searchViewHolder.icon.setImageBitmap(BitmapCacheManager.getResourceBitmap(context.getResources(), R.drawable.text));
    }

    protected static class SearchViewHolder extends RecyclerView.ViewHolder {
        public ImageView iconSmall;// 현재 사용안함. 작은 아이콘을 위해서 남겨둠
        public RoundedImageView icon;
        public TextView name;
        public TextView date;
        public TextView size;
        public TextView path;// 추가됨
        public ExplorerItem.FileType type;
        public int position;

        public SearchViewHolder(View itemView) {
            super(itemView);

            icon = (RoundedImageView) itemView.findViewById(R.id.file_icon);
            name = (TextView) itemView.findViewById(R.id.text_name);
            date = (TextView) itemView.findViewById(R.id.text_date);
            size = (TextView) itemView.findViewById(R.id.text_size);
            iconSmall = (ImageView) itemView.findViewById(R.id.file_small_icon);

            path = (TextView) itemView.findViewById(R.id.text_path);

            name.setMaxLines(1);
            name.setSingleLine(true);
            name.setEllipsize(TextUtils.TruncateAt.MIDDLE);
            path.setMaxLines(1);
            path.setSingleLine(true);
            path.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        }
    }
}
