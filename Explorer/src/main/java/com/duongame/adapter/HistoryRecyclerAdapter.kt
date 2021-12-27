package com.duongame.adapter

import android.app.Service
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.duongame.R
import com.duongame.adapter.HistoryRecyclerAdapter.HistoryViewHolder
import com.duongame.db.Book
import com.duongame.db.BookDB.Companion.clearBook
import com.duongame.db.BookLoader
import com.duongame.fragment.BaseFragment

/**
 * Created by js296 on 2017-08-13.
 */
class HistoryRecyclerAdapter(
    private val fragment: BaseFragment?,
    var bookList: ArrayList<Book>?
) : RecyclerView.Adapter<HistoryViewHolder>() {
    private var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val inflater =
            parent.context.getSystemService(Service.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = inflater.inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(v)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        if (bookList != null) {
            val context = holder.thumb.context
            val book = bookList!![position]
            BookLoader.updateBookHolder(context, holder, book)

            // 추가적인 UI 정보
            //holder.position = position
            holder.more.setOnClickListener { v ->
                val popup = PopupMenu(
                    context, v
                )
                val inflater = popup.menuInflater
                inflater.inflate(R.menu.menu_history, popup.menu)
                popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
                    if (v.tag != null) {
                        val path = v.tag as String
                        clearBook(context, path)

                        // 삭제한 이후에는 리프레시를 해주어야 한다.
                        fragment?.onRefresh()
                        return@OnMenuItemClickListener true
                    }
                    false
                })
                popup.show()
            }
            holder.itemView.setOnClickListener {
                if (onItemClickListener != null) {
                    onItemClickListener!!.onItemClick(holder.position)
                }
            }
            BookLoader.loadBookBitmap(context, holder, book.path)
        }
    }

    override fun getItemCount(): Int {
        return if (bookList != null) {
            bookList!!.size
        } else 0
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var thumb: ImageView
        var name: TextView
        var size: TextView
        var date: TextView
        var page: TextView
        var percent: TextView
        var progressBar: ProgressBar
        var more // tag사용중
                : ImageView

        init {
            thumb = itemView.findViewById(R.id.image_thumb)
            //thumb.setRadiusDp(5);
            name = itemView.findViewById(R.id.text_name)
            size = itemView.findViewById(R.id.text_size)
            date = itemView.findViewById(R.id.text_date)
            page = itemView.findViewById(R.id.text_page)
            percent = itemView.findViewById(R.id.text_percent)
            progressBar = itemView.findViewById(R.id.progress_history)
            more = itemView.findViewById(R.id.btn_more)
        }
    }
}