package com.duongame.comicz.db;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.duongame.R;
import com.duongame.comicz.adapter.HistoryRecyclerAdapter;
import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.bitmap.BitmapCacheManager;
import com.duongame.explorer.helper.AlertHelper;
import com.duongame.explorer.helper.DateHelper;
import com.duongame.explorer.helper.FileHelper;
import com.duongame.explorer.task.thumbnail.LoadPdfThumbnailTask;
import com.duongame.explorer.task.thumbnail.LoadZipThumbnailTask;
import com.duongame.viewer.activity.PdfActivity;
import com.duongame.viewer.activity.TextActivity;
import com.duongame.viewer.activity.ZipActivity;

import static com.duongame.explorer.bitmap.BitmapCacheManager.getThumbnail;
import static com.google.android.gms.internal.zzs.TAG;

/**
 * Created by js296 on 2017-08-21.
 */

public class BookLoader {
    public static void load(Activity context, BookDB.Book book) {
        Class<?> cls = null;
        if (book.path.toLowerCase().endsWith(".zip")) {
            cls = ZipActivity.class;
        } else if (book.path.toLowerCase().endsWith(".pdf")) {
            cls = PdfActivity.class;
        } else if (book.path.toLowerCase().endsWith(".txt")) {
            cls = TextActivity.class;
        }

        if (cls != null) {
            final Intent intent = new Intent(context, cls);
            intent.putExtra("path", book.path);
            intent.putExtra("name", book.name);
            intent.putExtra("current_page", book.current_page);
            intent.putExtra("size", book.size);
            intent.putExtra("extract_file", book.extract_file);
            intent.putExtra("side", book.side.getValue());
            context.startActivity(intent);
        }
    }

    private static void loadInternal(final Activity context, ExplorerItem item) {
        Class<?> cls = null;
        if (item.path.toLowerCase().endsWith(".zip")) {
            cls = ZipActivity.class;
        } else if (item.path.toLowerCase().endsWith(".pdf")) {
            cls = PdfActivity.class;
        } else if (item.path.toLowerCase().endsWith(".txt")) {
            cls = TextActivity.class;
        }
        if (cls != null) {
            final Intent intent = new Intent(context, cls);
            intent.putExtra("path", item.path);
            intent.putExtra("name", item.name);
            intent.putExtra("current_page", 0);
            intent.putExtra("size", item.size);
            intent.putExtra("extract_file", 0);
            intent.putExtra("side", item.side.getValue());
            context.startActivity(intent);
        }
    }

    public static void load(final Activity context, final ExplorerItem item) {
        final BookDB.Book book = getHistory(context, item);
        if(book == null) {
            loadInternal(context, item);
        } else {
            // 팝업을 띄운다음에 읽자
            AlertHelper.showAlert(context, context.getString(R.string.app_name_free), context.getString(R.string.msg_last_page), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    load(context, book);
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    loadInternal(context, item);
                }
            }, null);
        }
    }

    private static BookDB.Book getHistory(Activity context, ExplorerItem item) {
        return BookDB.getBook(context, item.path);
    }

    public static void loadBookBitmap(Context context, HistoryRecyclerAdapter.HistoryViewHolder holder, String path) {
        Log.d(TAG, "loadBookBitmap " + holder.thumb.getWidth() + " " + holder.thumb.getHeight());

        // zip 파일의 썸네일을 읽자
        if (path.toLowerCase().endsWith(".txt")) {
            holder.thumb.setImageBitmap(BitmapCacheManager.getResourceBitmap(context.getResources(), R.drawable.text));
            return;
        }

        final Bitmap bitmap = getThumbnail(path);
        if (bitmap == null) {
            if (path.toLowerCase().endsWith(".zip")) {
                final LoadZipThumbnailTask task = new LoadZipThumbnailTask(context, holder.thumb, holder.more);
                task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, path);
            } else if (path.toLowerCase().endsWith(".pdf")) {
                final LoadPdfThumbnailTask task = new LoadPdfThumbnailTask(context, holder.thumb, holder.more);
                task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, path);
            }
        } else {
            holder.thumb.setImageBitmap(bitmap);
        }
    }

    private static String getPageText(BookDB.Book book) {
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

    private static String getPercentText(BookDB.Book book) {
        return String.valueOf(book.percent) + "%";
    }

    public static void updateBookHolder(Context context, HistoryRecyclerAdapter.HistoryViewHolder holder, BookDB.Book book) {
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
    }
}
