package com.duongame.comicz.db;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;

import com.duongame.R;
import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.helper.AlertHelper;
import com.duongame.viewer.activity.PdfActivity;
import com.duongame.viewer.activity.TextActivity;
import com.duongame.viewer.activity.ZipActivity;

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
}
