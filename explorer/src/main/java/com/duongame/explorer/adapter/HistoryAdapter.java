package com.duongame.explorer.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.duongame.explorer.R;
import com.duongame.explorer.db.BookDB;

import java.util.ArrayList;

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
        ImageView thumbnail;
        TextView name;
        TextView page;
        TextView count;
        TextView extract;
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
            viewHolder = new ViewHolder();
            convertView = context.getLayoutInflater().inflate(R.layout.history_item, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.text_name);
            viewHolder.page = (TextView) convertView.findViewById(R.id.text_page);
            viewHolder.count = (TextView) convertView.findViewById(R.id.text_count);
            viewHolder.extract = (TextView) convertView.findViewById(R.id.text_extract);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final BookDB.Book book = bookList.get(position);
        viewHolder.name.setText(book.name);
        viewHolder.page.setText(String.valueOf(book.page));
        viewHolder.count.setText(String.valueOf(book.count));
        viewHolder.extract.setText(String.valueOf(book.extract));

        return convertView;
    }
}
