package com.duongame.comicz.db;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.duongame.R;
import com.duongame.comicz.adapter.HistoryRecyclerAdapter;
import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.bitmap.BitmapCacheManager;
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
    public static boolean openLastBook(Activity context) {
        final Book book = BookDB.getLastBook(context);
        if (book != null && book.percent < 100) {
            BookLoader.loadWithAlert(context, book, true);
            return true;
        }
        return false;
    }

    public static boolean openLastBookDirect(Activity context) {
        final Book book = BookDB.getLastBook(context);
        if (book != null && book.percent < 100) {
            BookLoader.loadContinue(context, book);
            return true;
        }
        return false;
    }

    // 히스토리일 경우는 바로 읽음
    public static void loadContinue(Activity context, Book book) {
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

    private static Intent getIntentNew(final Activity context, ExplorerItem item) {
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
            intent.putExtra("current_file", 0);
            intent.putExtra("extract_file", 0);
            intent.putExtra("side", item.side.getValue());
            return intent;
        }
        return null;
    }

    private static Intent getIntentNew(final Activity context, Book book) {
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
            intent.putExtra("current_page", 0);
            intent.putExtra("size", book.size);
            intent.putExtra("current_file", book.current_file);
            intent.putExtra("extract_file", book.extract_file);
            intent.putExtra("side", book.side.getValue());

            return intent;
        }
        return null;
    }

    private static void loadNew(final Activity context, ExplorerItem item) {
        Intent intent = getIntentNew(context, item);
        context.startActivity(intent);
    }

    private static void loadNew(final Activity context, Book book) {
        Intent intent = getIntentNew(context, book);
        context.startActivity(intent);
    }

    // 탐색기, 검색일 경우 여기서 읽음
    public static void load(final Activity context, final ExplorerItem item, final boolean cancelToRead) {
        final Book book = getHistory(context, item);

        // 새로 페이지 0부터 읽음
        if(book == null) {
            loadNew(context, item);
        } else {
            // 팝업을 띄운다음에 읽자
            loadWithAlert(context, book, cancelToRead);
        }
    }

    private static void updateHistoryItem(Context context, View view, Book book) {
        HistoryRecyclerAdapter.HistoryViewHolder holder = new HistoryRecyclerAdapter.HistoryViewHolder(view);

        BookLoader.updateBookHolder(context, holder, book);
        BookLoader.loadBookBitmap(context, holder, book.path);

        holder.more.setVisibility(View.GONE);
    }

    public static void loadWithAlert(final Activity context, final Book book, final boolean cancelToRead) {
        View view = context.getLayoutInflater().inflate(R.layout.history_item, null, false);
        updateHistoryItem(context, view, book);

        // 이부분은 물어보고 셋팅하자.
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.comicz_name_free))
                .setView(view)
                .setMessage(String.format(context.getString(R.string.msg_last_page), book.current_page + 1))
                .setIcon(R.drawable.comicz)
                // 연속해서 읽을경우
                .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadContinue(context, book);
                    }
                })
                // 연속해서 읽지 않거나, 취소함
                .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!cancelToRead) {// 처음부터 읽음
                            loadNew(context, book);
                        }
                    }
                });
        builder.show();
    }

    private static Book getHistory(Activity context, ExplorerItem item) {
        return BookDB.getBook(context, item.path);
    }

    public static void loadBookBitmap(Context context, HistoryRecyclerAdapter.HistoryViewHolder holder, String path) {
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

    private static String getPageText(Book book) {
        // 압축이 다 풀렸으면, 페이지를 기준으로 한다.
        // 압축파일이 아니라면 둘다 0이다.
        if (book.extract_file == book.total_file) {
            // 텍스트가 아닐경우에는 PDF나 ZIP이다.
            if (book.type != ExplorerItem.FileType.TEXT)
                return (book.current_page + 1) + "/" + book.total_page;
            else {
                return TextBook.getPageText(book);
            }
        } else {
            return (book.current_file + 1) + "/" + book.total_file;
        }
    }

    private static String getPercentText(Book book) {
        return String.valueOf(book.percent) + "%";
    }

    public static void updateBookHolder(Context context, HistoryRecyclerAdapter.HistoryViewHolder holder, Book book) {
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
