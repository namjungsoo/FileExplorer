package com.duongame.comicz.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.duongame.R;
import com.duongame.comicz.db.BookDB;
import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.bitmap.BitmapCacheManager;
import com.duongame.explorer.fragment.BaseFragment;
import com.duongame.explorer.helper.DateHelper;
import com.duongame.explorer.helper.FileHelper;
import com.duongame.explorer.task.thumbnail.LoadPdfThumbnailTask;
import com.duongame.explorer.task.thumbnail.LoadZipThumbnailTask;
import com.duongame.explorer.view.RoundedImageView;

import java.util.ArrayList;

import static com.duongame.explorer.bitmap.BitmapCacheManager.getThumbnail;

/**
 * Created by js296 on 2017-08-13.
 */

public class HistoryRecyclerAdapter extends RecyclerView.Adapter<HistoryRecyclerAdapter.HistoryViewHolder> {
    private final static String TAG = "HistoryAdapter";

    private Activity context;
    private BaseFragment fragment;
    private ArrayList<BookDB.Book> bookList;

    public HistoryRecyclerAdapter(Activity context, BaseFragment fragment, ArrayList<BookDB.Book> bookList) {
        this.context = context;
        this.fragment = fragment;
        this.bookList = bookList;
    }

    public void setBookList(ArrayList<BookDB.Book> bookList) {
        this.bookList = bookList;
    }

    public ArrayList<BookDB.Book> getBookList() {
        return bookList;
    }

    OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = context.getLayoutInflater().inflate(R.layout.history_item, parent, false);
        return new HistoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final HistoryViewHolder holder, int position) {
        if (bookList != null) {
            final BookDB.Book book = bookList.get(position);

            holder.name.setText(book.name);
            holder.size.setText(FileHelper.getMinimizedSize(book.size));
            holder.date.setText(DateHelper.getDBDate(book.date));
            holder.page.setText(getPageText(book));
            holder.percent.setText(getPercentText(book));
            holder.progressBar.setMax(100);
            holder.progressBar.setProgress(book.percent);
            if (book.percent == 100) {
                holder.progressBar.getProgressDrawable().setColorFilter(Color.YELLOW, android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                holder.progressBar.getProgressDrawable().setColorFilter(
                        ContextCompat.getColor(context, R.color.colorAccent),
                        android.graphics.PorterDuff.Mode.SRC_IN);
            }
            holder.more.setTag(book.path);
            holder.more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final PopupMenu popup = new PopupMenu(context, v);
                    final MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.menu_history, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (v.getTag() != null) {
                                String path = (String) v.getTag();
                                BookDB.clearBook(context, path);

                                // 삭제한 이후에는 리프레시를 해주어야 한다.
                                if (fragment != null)
                                    fragment.onRefresh();
                                return true;
                            }
                            return false;
                        }
                    });
                    popup.show();
                }
            });

            holder.position = position;
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(holder.position);
                    }
                }
            });

            loadBitmap(holder.thumb, book.path);
        }
    }

    @Override
    public int getItemCount() {
        if (bookList != null) {
            return bookList.size();
        }
        return 0;
    }

    private String getPageText(BookDB.Book book) {
        // 압축이 다 풀렸으면, 페이지를 기준으로 한다.
        // 압축파일이 아니라면 둘다 0이다.
        if (book.extract_file == book.total_file) {
            // 텍스트가 아닐경우에는 PDF나 ZIP이다.
            if (book.type != ExplorerItem.FileType.TEXT)
                return (book.current_page + 1) + "/" + book.total_page;
            else {
                BookDB.TextBook textBook = BookDB.getTextBook(book);
                return textBook.getPageText();
            }
        } else {
            return (book.current_file + 1) + "/" + book.total_file;
        }
    }

    private String getPercentText(BookDB.Book book) {
        return String.valueOf(book.percent) + "%";
    }

    private void loadBitmap(ImageView thumb, String path) {
        Log.d(TAG, "loadBitmap " + thumb.getWidth() + " " + thumb.getHeight());

        // zip 파일의 썸네일을 읽자
        if (path.toLowerCase().endsWith(".txt")) {
            thumb.setImageBitmap(BitmapCacheManager.getResourceBitmap(context.getResources(), R.drawable.text));
            return;
        }

        final Bitmap bitmap = getThumbnail(path);
        if (bitmap == null) {
            if (path.toLowerCase().endsWith(".zip")) {
                final LoadZipThumbnailTask task = new LoadZipThumbnailTask(context, thumb);
                task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, path);
            } else if (path.toLowerCase().endsWith(".pdf")) {
                final LoadPdfThumbnailTask task = new LoadPdfThumbnailTask(context, thumb);
                task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, path);
            }
        } else {
            thumb.setImageBitmap(bitmap);
        }
    }

    protected static class HistoryViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView thumb;
        TextView name;
        TextView size;
        TextView date;

        TextView page;
        TextView percent;
        ProgressBar progressBar;
        ImageView more;
        int position;

        public HistoryViewHolder(View itemView) {
            super(itemView);

            thumb = (RoundedImageView) itemView.findViewById(R.id.image_thumb);
            thumb.setRadiusDp(5);

            name = (TextView) itemView.findViewById(R.id.text_name);
            size = (TextView) itemView.findViewById(R.id.text_size);
            date = (TextView) itemView.findViewById(R.id.text_date);
            page = (TextView) itemView.findViewById(R.id.text_page);
            percent = (TextView) itemView.findViewById(R.id.text_percent);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress_history);

            more = (ImageView) itemView.findViewById(R.id.btn_more);
        }
    }
}
