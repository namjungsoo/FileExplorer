package com.duongame.db;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.duongame.AnalyticsApplication;
import com.duongame.R;
import com.duongame.activity.viewer.PdfActivity;
import com.duongame.activity.viewer.TextActivity;
import com.duongame.activity.viewer.ZipActivity;
import com.duongame.adapter.ExplorerItem;
import com.duongame.adapter.HistoryRecyclerAdapter;
import com.duongame.bitmap.BitmapCacheManager;
import com.duongame.file.FileHelper;
import com.duongame.helper.AppHelper;
import com.duongame.helper.DateHelper;
import com.duongame.helper.ToastHelper;
import com.duongame.task.thumbnail.LoadThumbnailTask;
import com.duongame.task.thumbnail.LoadZipThumbnailTask;

import java.io.File;
import java.util.ArrayList;

import static com.duongame.bitmap.BitmapCacheManager.getThumbnail;

/**
 * Created by js296 on 2017-08-21.
 */

public class BookLoader {
    private static ArrayList<String> seriesList;
    private static int seriesPosition;

    //TODO: 마지막 책읽기는 comicz만 수행
    // 액티비티 시작할때
    public static boolean openLastBook(Activity context) {
        final Book book = BookDB.getLastBook(context);
        if (book != null && book.percent < 100) {
            // DB에서는 책을 읽어야 하지만, 파일이 삭제된 경우에는 제외한다.
            try {
                File file = new File(book.path);
                if (file.exists()) {
                    BookLoader.loadWithAlert(context, book, true);
                    return true;
                } else {
                    // DB에서 삭제한다.
                    BookDB.clearBook(context, book.path);
                }
            } catch (Exception e) {
                // 아무것도 하지 않고 false 리턴한다.
            }
        }
        return false;
    }

    // 직접 메뉴에서 마지막 책읽기를 선택한 경우
    public static boolean openLastBookDirect(Activity context) {
        final Book book = BookDB.getLastBook(context);
        if (book != null && book.percent < 100) {
            // DB에서는 책을 읽어야 하지만, 파일이 삭제된 경우에는 제외한다.
            try {
                File file = new File(book.path);
                if (file.exists()) {
                    BookLoader.loadContinue(context, book);
                    return true;
                } else {
                    // DB에서 삭제한다.
                    BookDB.clearBook(context, book.path);
                }
            } catch (Exception e) {
                // 아무것도 하지 않고 false 리턴한다.
            }
        }
        ToastHelper.error(context, R.string.msg_no_lastbook);
        return false;
    }

    // 히스토리일 경우는 바로 읽음
    public static void loadContinue(Activity context, Book book) {
        Class<?> cls = null;
        switch (FileHelper.getCompressType(book.path)) {
            case ExplorerItem.COMPRESSTYPE_ZIP:
            case ExplorerItem.COMPRESSTYPE_RAR:
            case ExplorerItem.COMPRESSTYPE_SEVENZIP:
                cls = ZipActivity.class;
                break;
            default:
                if (book.path.endsWith(".pdf")) {
                    cls = PdfActivity.class;
                } else if (FileHelper.isText(book.path)) {
                    cls = TextActivity.class;
                } else {
                    return;
                }
                break;
        }

        if (cls != null) {
            final Intent intent = new Intent(context, cls);
            intent.putExtra("path", book.path);
            intent.putExtra("name", book.name);
            intent.putExtra("current_page", book.current_page);
            intent.putExtra("size", book.size);
            intent.putExtra("extract_file", book.extract_file);
            intent.putExtra("side", book.side);
            context.startActivity(intent);
        }
    }

    // 탐색기에서 클릭하여 로딩할 경우
    private static Intent getIntentNew(final Activity context, ExplorerItem item) {
        Class<?> cls = null;
        switch (FileHelper.getCompressType(item.path)) {
            case ExplorerItem.COMPRESSTYPE_ZIP:
            case ExplorerItem.COMPRESSTYPE_RAR:
            case ExplorerItem.COMPRESSTYPE_SEVENZIP:
                cls = ZipActivity.class;
                break;
            default:
                if (item.path.endsWith(".pdf")) {
                    cls = PdfActivity.class;
                } else if (FileHelper.isText(item.path)) {
                    cls = TextActivity.class;
                } else {
                    return null;
                }
                break;
        }

        if (cls != null) {
            final Intent intent = new Intent(context, cls);
            intent.putExtra("path", item.path);
            intent.putExtra("name", item.name);
            intent.putExtra("current_page", 0);
            intent.putExtra("size", item.size);
            intent.putExtra("current_file", 0);
            intent.putExtra("extract_file", 0);

            AnalyticsApplication application = (AnalyticsApplication) context.getApplication();
            int side = ExplorerItem.SIDE_LEFT;
            if (application != null) {
                if (application.isJapaneseDirection()) {
                    side = ExplorerItem.SIDE_RIGHT;
                }
            }
            intent.putExtra("side", side);
            return intent;
        }
        return null;
    }

    // 기존에 읽던 책을 처음부터 다시 로딩할 경우
    private static Intent getIntentNew(final Activity context, Book book) {
        Class<?> cls = null;
        switch (FileHelper.getCompressType(book.path)) {
            case ExplorerItem.COMPRESSTYPE_ZIP:
            case ExplorerItem.COMPRESSTYPE_RAR:
            case ExplorerItem.COMPRESSTYPE_SEVENZIP:
                cls = ZipActivity.class;
                break;
            default:
                if (book.path.endsWith(".pdf")) {
                    cls = PdfActivity.class;
                } else if (FileHelper.isText(book.path)) {
                    cls = TextActivity.class;
                } else {
                    return null;
                }
                break;
        }

        if (cls != null) {
            final Intent intent = new Intent(context, cls);
            intent.putExtra("path", book.path);
            intent.putExtra("name", book.name);
            intent.putExtra("current_page", 0);
            intent.putExtra("size", book.size);
            intent.putExtra("current_file", book.current_file);
            intent.putExtra("extract_file", book.extract_file);
            intent.putExtra("side", book.side);

            return intent;
        }
        return null;
    }

    private static void loadNew(final Activity context, ExplorerItem item) {
        Intent intent = getIntentNew(context, item);
        if (intent != null)
            context.startActivity(intent);
    }

    private static void loadNew(final Activity context, Book book) {
        Intent intent = getIntentNew(context, book);
        if (intent != null)
            context.startActivity(intent);
    }

    // 탐색기, 검색일 경우 여기서 읽음
    public static void load(final Activity context, final ExplorerItem item, final boolean cancelToRead) {
        final Book book = getHistory(context, item);

        // 새로 페이지 0부터 읽음
        if (book == null) {
            loadNew(context, item);
        } else {
            // 팝업을 띄운다음에 읽자
            loadWithAlert(context, book, cancelToRead);
        }
    }

    private static void updateHistoryItem(Activity context, View view, Book book) {
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
                .setIcon(AppHelper.getIconResId(context))
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

    private static void loadDefaultThumbnail(Activity context, HistoryRecyclerAdapter.HistoryViewHolder holder, String path) {
        if(FileHelper.getCompressType(path) != ExplorerItem.COMPRESSTYPE_OTHER) {
            // 압축파일
            holder.thumb.setImageBitmap(BitmapCacheManager.getResourceBitmap(context.getResources(), R.drawable.ic_file_zip));
        } else {
            // PDF
            holder.thumb.setImageBitmap(BitmapCacheManager.getResourceBitmap(context.getResources(), R.drawable.ic_file_pdf));
        }
    }
    public static void loadBookBitmap(Activity context, HistoryRecyclerAdapter.HistoryViewHolder holder, String path) {
        // zip 파일의 썸네일을 읽자
        if (FileHelper.isText(path)) {
            holder.thumb.setImageBitmap(BitmapCacheManager.getResourceBitmap(context.getResources(), R.drawable.ic_file_txt));
            return;
        }

        loadDefaultThumbnail(context, holder, path);

        AnalyticsApplication application = (AnalyticsApplication)context.getApplication();
        if (application != null && application.isThumbnailDisabled()) {// 썸네일 비활성화라면
            return;
        } else {
            final Bitmap bitmap = getThumbnail(path);
            if (bitmap == null) {
                switch (FileHelper.getCompressType(path)) {
                    case ExplorerItem.COMPRESSTYPE_ZIP:
                    case ExplorerItem.COMPRESSTYPE_SEVENZIP:
                    case ExplorerItem.COMPRESSTYPE_RAR: {
                        final LoadZipThumbnailTask task = new LoadZipThumbnailTask(context, holder.thumb, holder.more);
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);
                    }
                    break;
                    default:
                        if (path.endsWith(".pdf")) {
                            final LoadThumbnailTask task = new LoadThumbnailTask(context, holder.thumb, holder.more, ExplorerItem.FILETYPE_PDF);
                            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);
                        }
                        break;
                }
            } else {
                holder.thumb.setImageBitmap(bitmap);
                BitmapCacheManager.setThumbnail(path, bitmap, holder.thumb);
            }
        }
    }

    private static String getPageText(Book book) {
        // 압축이 다 풀렸으면, 페이지를 기준으로 한다.
        // 압축파일이 아니라면 둘다 0이다.
        if (book.extract_file == book.total_file) {
            // 텍스트가 아닐경우에는 PDF나 ZIP이다.
            if (book.type != ExplorerItem.FILETYPE_TEXT)
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

    public static void updateBookHolder(Activity context, HistoryRecyclerAdapter.HistoryViewHolder holder, Book book) {
        holder.name.setText(book.name);
        holder.size.setText(FileHelper.getMinimizedSize(book.size));
        holder.date.setText(DateHelper.getExplorerDateStringFromDbDateString(book.date));
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
