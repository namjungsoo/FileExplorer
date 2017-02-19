package com.duongame.comicz.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.duongame.comicz.db.BookDB;
import com.duongame.explorer.R;
import com.duongame.explorer.helper.DateHelper;
import com.duongame.explorer.helper.FileHelper;
import com.duongame.explorer.task.thumbnail.LoadPdfThumbnailTask;
import com.duongame.explorer.task.thumbnail.LoadZipThumbnailTask;
import com.duongame.explorer.view.RoundedImageView;

import java.util.ArrayList;

import static com.duongame.explorer.bitmap.BitmapCache.getThumbnail;

/**
 * Created by namjungsoo on 2017-01-02.
 */

public class HistoryAdapter extends BaseAdapter {
    private final static String TAG = "HistoryAdapter";
    private Activity context;
    private ArrayList<BookDB.Book> bookList;

    public HistoryAdapter(Activity context, ArrayList<BookDB.Book> bookList) {
        this.context = context;
        this.bookList = bookList;
    }

    public void setBookList(ArrayList<BookDB.Book> bookList) {
        this.bookList = bookList;
    }

    public ArrayList<BookDB.Book> getBookList() {
        return bookList;
    }

    private static class ViewHolder {
        RoundedImageView thumb;
        TextView name;
        TextView size;
        TextView date;

        TextView page;
        TextView percent;
        ProgressBar progressBar;
    }

    @Override
    public int getCount() {
        if (bookList != null) {
            return bookList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.history_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.thumb = (RoundedImageView) convertView.findViewById(R.id.image_thumb);
            viewHolder.thumb.setRadiusDp(5);

            viewHolder.name = (TextView) convertView.findViewById(R.id.text_name);
            viewHolder.size = (TextView) convertView.findViewById(R.id.text_size);
            viewHolder.date = (TextView) convertView.findViewById(R.id.text_date);
            viewHolder.page = (TextView) convertView.findViewById(R.id.text_page);
            viewHolder.percent = (TextView) convertView.findViewById(R.id.text_percent);
            viewHolder.progressBar = (ProgressBar) convertView.findViewById(R.id.progress_history);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (bookList != null) {
            final BookDB.Book book = bookList.get(position);

            viewHolder.name.setText(book.name);
            viewHolder.size.setText(FileHelper.getMinimizedSize(book.size));
            viewHolder.date.setText(DateHelper.getDBDate(book.date));
            viewHolder.page.setText(getPageText(book));
            viewHolder.percent.setText(getPercentText(book));
            viewHolder.progressBar.setMax(100);
            viewHolder.progressBar.setProgress(book.percent);
            if(book.percent == 100) {
                viewHolder.progressBar.getProgressDrawable().setColorFilter(Color.YELLOW, android.graphics.PorterDuff.Mode.SRC_IN);
            }
            else {
                viewHolder.progressBar.getProgressDrawable().setColorFilter(
                        ContextCompat.getColor(context, R.color.colorAccent),
                        android.graphics.PorterDuff.Mode.SRC_IN);
            }
            loadBitmap(viewHolder.thumb, book.path);
        }

        return convertView;
    }

    private String getPageText(BookDB.Book book) {
        // 압축이 다 풀렸으면, 페이지를 기준으로 한다.
        if(book.extract_file == book.total_file) {
            return (book.current_page + 1) + "/" + book.total_page;
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
        final Bitmap bitmap = getThumbnail(path);
        if (bitmap == null) {
            if(path.toLowerCase().endsWith(".zip")) {
                final LoadZipThumbnailTask task = new LoadZipThumbnailTask(context, thumb);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);

            } else if(path.toLowerCase().endsWith(".pdf")) {
                final LoadPdfThumbnailTask task = new LoadPdfThumbnailTask(context, thumb);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);
            }
        } else {
            thumb.setImageBitmap(bitmap);
        }
    }
}
