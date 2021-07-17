package com.duongame.adapter;

import android.app.Activity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.duongame.R;
import com.duongame.db.Book;
import com.duongame.db.BookDB;
import com.duongame.db.BookLoader;
import com.duongame.fragment.BaseFragment;

import java.util.ArrayList;

/**
 * Created by js296 on 2017-08-13.
 */

public class HistoryRecyclerAdapter extends RecyclerView.Adapter<HistoryRecyclerAdapter.HistoryViewHolder> {
    private final static String TAG = "HistoryAdapter";

    private Activity context;
    private BaseFragment fragment;
    private ArrayList<Book> bookList;

    public HistoryRecyclerAdapter(Activity context, BaseFragment fragment, ArrayList<Book> bookList) {
        this.context = context;
        this.fragment = fragment;
        this.bookList = bookList;
    }

    public void setBookList(ArrayList<Book> bookList) {
        this.bookList = bookList;
    }

    public ArrayList<Book> getBookList() {
        return bookList;
    }

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = context.getLayoutInflater().inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final HistoryViewHolder holder, int position) {
        if (bookList != null) {
            final Book book = bookList.get(position);

            BookLoader.updateBookHolder(context, holder, book);

            // 추가적인 UI 정보
            holder.position = position;
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
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(holder.position);
                    }
                }
            });

            BookLoader.loadBookBitmap(context, holder, book.path);
        }
    }

    @Override
    public int getItemCount() {
        if (bookList != null) {
            return bookList.size();
        }
        return 0;
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumb;
        public TextView name;
        public TextView size;
        public TextView date;

        public TextView page;
        public TextView percent;
        public ProgressBar progressBar;
        public ImageView more;// tag사용중
        public int position;

        public HistoryViewHolder(View itemView) {
            super(itemView);

            thumb = itemView.findViewById(R.id.image_thumb);
            //thumb.setRadiusDp(5);

            name = itemView.findViewById(R.id.text_name);
            size = itemView.findViewById(R.id.text_size);
            date = itemView.findViewById(R.id.text_date);
            page = itemView.findViewById(R.id.text_page);
            percent = itemView.findViewById(R.id.text_percent);
            progressBar = itemView.findViewById(R.id.progress_history);

            more = itemView.findViewById(R.id.btn_more);
        }
    }
}
