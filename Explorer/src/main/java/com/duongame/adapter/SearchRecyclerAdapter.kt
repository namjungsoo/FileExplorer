package com.duongame.adapter

import com.duongame.file.FileHelper.getMinimizedSize
import com.duongame.helper.PreferenceHelper.thumbnailDisabled
import com.duongame.bitmap.BitmapCacheManager.getThumbnail
import com.duongame.bitmap.BitmapCacheManager.setThumbnail
import com.duongame.helper.UnitHelper.dpToPx
import androidx.recyclerview.widget.RecyclerView
import com.duongame.adapter.SearchRecyclerAdapter.SearchViewHolder
import android.view.ViewGroup
import com.duongame.R
import com.duongame.task.thumbnail.LoadZipThumbnailTask
import android.os.AsyncTask
import com.duongame.task.thumbnail.LoadThumbnailTask
import android.widget.TextView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import java.io.File
import java.lang.NullPointerException
import java.util.ArrayList

/**
 * Created by js296 on 2017-08-19.
 */
// PDF, ZIP, TEXT만 검색될수 있다.
class SearchRecyclerAdapter(
    private val fileList: ArrayList<ExplorerItem>
) : RecyclerView.Adapter<SearchViewHolder>() {
    private var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SearchViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_file_list, viewGroup, false)
        return SearchViewHolder(v)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, i: Int) {
        val item = fileList[i]
        val parentPath = File(item.path).parent
        holder.name.text = item.name
        holder.path.text = parentPath
        holder.date.text = item.date
        holder.size.text = getMinimizedSize(item.size)
        //holder.icon.setRadiusDp(5);
        holder.iconSmall.visibility = View.INVISIBLE
        holder.iconSmall.tag = item.path
        when (item.type) {
            ExplorerItem.FILETYPE_ZIP -> setIconZip(holder, item)
            ExplorerItem.FILETYPE_PDF -> setIconPdf(holder, item)
            ExplorerItem.FILETYPE_TEXT -> setIconText(holder, item)
        }
        holder.itemView.setOnClickListener {
            if (onItemClickListener != null) {
                onItemClickListener!!.onItemClick(i)
            }
        }
    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    private val isThumbnailEnabled: Boolean
        get() = try {
            !thumbnailDisabled
        } catch (e: NullPointerException) {
            true
        }

    private fun setIconZip(holder: SearchViewHolder, item: ExplorerItem) {
        val context = holder.icon.context
        val bitmap = getThumbnail(item.path)
        if (bitmap == null) {
            holder.icon.setImageResource(R.drawable.ic_file_zip)
            if (isThumbnailEnabled) {
                val task = LoadZipThumbnailTask(context, holder.icon, holder.iconSmall)
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item.path)
            }
        } else {
            holder.icon.setImageBitmap(bitmap)
            setThumbnail(item.path, bitmap, holder.icon)
        }
    }

    private fun setIconPdf(holder: SearchViewHolder, item: ExplorerItem) {
        val context = holder.icon.context
        val bitmap = getThumbnail(item.path)
        if (bitmap == null) {
            holder.icon.setImageResource(R.drawable.ic_file_pdf)
            if (isThumbnailEnabled) {
                val task = LoadThumbnailTask(
                    context,
                    holder.icon,
                    holder.iconSmall,
                    ExplorerItem.FILETYPE_PDF
                )
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item.path)
            }
        } else { // 로딩된 비트맵을 셋팅
            holder.icon.setImageBitmap(bitmap)
            setThumbnail(item.path, bitmap, holder.icon)
        }
    }

    private fun setIconText(searchViewHolder: SearchViewHolder, item: ExplorerItem?) {
        searchViewHolder.icon.setImageResource(R.drawable.ic_file_txt)
    }

    class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var iconSmall // 현재 사용안함. 작은 아이콘을 위해서 남겨둠
                : ImageView
        var icon: ImageView
        var name: TextView
        var date: TextView
        var size: TextView
        var path // 추가됨
                : TextView
        var type = 0

        init {
            icon = itemView.findViewById(R.id.file_icon)
            name = itemView.findViewById(R.id.text_name)
            date = itemView.findViewById(R.id.text_date)
            size = itemView.findViewById(R.id.text_size)
            iconSmall = itemView.findViewById(R.id.file_small_icon)
            name.layoutParams.height = dpToPx(20)
            path = itemView.findViewById(R.id.text_path)
            path.visibility = View.VISIBLE
            path.layoutParams.height = dpToPx(20)
            name.maxLines = 1
            name.isSingleLine = true
            name.ellipsize = TextUtils.TruncateAt.MIDDLE
            path.maxLines = 1
            path.isSingleLine = true
            path.ellipsize = TextUtils.TruncateAt.MIDDLE
        }
    }
}