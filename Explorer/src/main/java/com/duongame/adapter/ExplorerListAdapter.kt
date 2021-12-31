package com.duongame.adapter

import android.app.Service
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.duongame.R
import com.duongame.file.FileHelper.getMinimizedSize

/**
 * Created by namjungsoo on 2016-11-16.
 */
class ExplorerListAdapter(fileList: ArrayList<ExplorerItem>) : ExplorerAdapter(fileList) {
    override fun bindViewHolderExplorer(viewHolder: ExplorerViewHolder, position: Int) {
        val item = fileList[position]
        viewHolder.name.text = item.name
        viewHolder.date.text = item.date
        viewHolder.size.text = getMinimizedSize(item.size)
        //viewHolder.icon.setRadiusDp(5);
        viewHolder.iconSmall.visibility = View.INVISIBLE
        //        viewHolder.position = position;
        item.position = position
        updateCheckBox(viewHolder, item)
        setIconDefault(viewHolder, item)
        setIcon(viewHolder, item)
    }

    override fun inflateLayout(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context).inflate(R.layout.item_file_list, parent, false)
    }

    override fun getFirstVisibleItem(recyclerView: RecyclerView): Int {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager? ?: return 0
        return layoutManager.findFirstVisibleItemPosition()
    }

    override fun getVisibleItemCount(recyclerView: RecyclerView): Int {
        val first = getFirstVisibleItem(recyclerView)
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager? ?: return 0
        val last = layoutManager.findLastVisibleItemPosition()
        return last - first + 1
    }
}