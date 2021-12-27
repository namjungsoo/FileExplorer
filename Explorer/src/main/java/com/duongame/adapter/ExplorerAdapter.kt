package com.duongame.adapter

import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.ImageViewTarget
import com.duongame.BuildConfig
import com.duongame.R
import com.duongame.adapter.ExplorerAdapter.ExplorerViewHolder
import com.duongame.bitmap.BitmapCacheManager
import com.duongame.bitmap.BitmapLoader
import com.duongame.fragment.ExplorerFragment
import com.duongame.helper.PreferenceHelper.thumbnailDisabled
import com.duongame.task.thumbnail.LoadGifThumbnailTask
import com.duongame.task.thumbnail.LoadThumbnailTask
import com.duongame.task.thumbnail.LoadZipThumbnailTask
import timber.log.Timber
import java.io.File
import java.util.*

/**
 * Created by namjungsoo on 2016-11-06.
 */
abstract class ExplorerAdapter(fileList: ArrayList<ExplorerItem>) :
    RecyclerView.Adapter<ExplorerViewHolder>() {

    var fileList: List<ExplorerItem> = emptyList()
        set(value) {
            field = value
            fileMap.clear()
            for (item in field) {
                fileMap[item.path] = item
            }
        }
    private var fileMap: HashMap<String, ExplorerItem> = hashMapOf()// file path와 file item을 묶어 놓음

    //private boolean selectMode = false;
    var mode = ExplorerFragment.MODE_NORMAL

    private var onItemClickListener: OnItemClickListener? = null
    private var onLongItemClickListener: OnItemLongClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        onItemClickListener = listener
    }

    fun setOnLongItemClickListener(listener: OnItemLongClickListener?) {
        onLongItemClickListener = listener
    }

    // 썸네일이 있는 파일만 true
    private fun hasThumbnail(item: ExplorerItem): Boolean {
        return when (item.type) {
            ExplorerItem.FILETYPE_APK, ExplorerItem.FILETYPE_ZIP, ExplorerItem.FILETYPE_PDF, ExplorerItem.FILETYPE_IMAGE, ExplorerItem.FILETYPE_VIDEO -> true
            else -> false
        }
    }

    class ExplorerViewHolder(itemView: View?) : RecyclerView.ViewHolder(
        itemView!!
    ) {
        var iconSmall: ImageView// 현재 사용안함. 작은 아이콘을 위해서 남겨둠
        var icon: ImageView
        var name: TextView
        var date: TextView
        var size: TextView
        var check: CheckBox
        var type = 0// FileType

        init {
            icon = itemView!!.findViewById(R.id.file_icon)
            name = itemView.findViewById(R.id.text_name)
            date = itemView.findViewById(R.id.text_date)
            size = itemView.findViewById(R.id.text_size)
            iconSmall = itemView.findViewById(R.id.file_small_icon)
            check = itemView.findViewById(R.id.check_file)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExplorerViewHolder {
        val v = inflateLayout(parent)
        return ExplorerViewHolder(v)
    }

    override fun onBindViewHolder(holder: ExplorerViewHolder, position: Int) {
        bindViewHolderExplorer(holder, position)
        holder.itemView.setOnClickListener {
            Timber.e("onClick " + holder.position)
            if (onItemClickListener != null) {
                onItemClickListener!!.onItemClick(holder.position)
            }
        }
        holder.itemView.setOnLongClickListener(OnLongClickListener {
            if (onLongItemClickListener != null) {
                onLongItemClickListener!!.onItemLongClick(holder.position)
                return@OnLongClickListener true
            }
            false
        })
    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    protected fun updateCheckBox(viewHolder: ExplorerViewHolder, item: ExplorerItem) {
        if (mode == ExplorerFragment.MODE_SELECT) {
//            Timber.e("updateCheckBox position=" + item.position + " item=" + item.hashCode() + " " + item.selected);
            viewHolder.check.visibility = View.VISIBLE
            viewHolder.check.isChecked = item.selected
        } else {
            item.selected = false
            viewHolder.check.visibility = View.INVISIBLE
            viewHolder.check.isChecked = false
        }
    }

    // 이전에는 system thumbnail image를 사용하였으나 이번에는 무조건 glide가 읽음
    fun setIconImage(viewHolder: ExplorerViewHolder, item: ExplorerItem) {
        val context = viewHolder.itemView.context
        val bitmap = BitmapCacheManager.getThumbnail(item.path)
        if (bitmap == null) {
            if (item.path.endsWith(".gif")) {
                // gif가 없어서 jpg로 처리함
                viewHolder.icon.setImageResource(R.drawable.ic_file_jpg)
                if (isThumbnailEnabled) {
                    val task = LoadGifThumbnailTask(context, viewHolder.icon, viewHolder.iconSmall)
                    task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, item.path)
                }
            } else {
                if (item.path.endsWith(".png")) viewHolder.icon.setImageResource(R.drawable.ic_file_png) else viewHolder.icon.setImageResource(
                    R.drawable.ic_file_jpg
                )
                if (isThumbnailEnabled) {
                    // Glide로 읽자
                    Glide.with(context)
                        .load(File(item.path))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_file_normal)
                        .centerCrop()
                        .into(object : ImageViewTarget<Drawable?>(viewHolder.icon) {
                            override fun setResource(resource: Drawable?) {
                                //FIX: destroyed activity error
                                //if (context.isFinishing) return
                                if (resource == null) return
                                if (item.path == viewHolder.iconSmall.tag) {
                                    val bitmap = BitmapLoader.drawableToBitmap(resource)
                                    getView().setImageBitmap(bitmap)
                                    BitmapCacheManager.setThumbnail(
                                        item.path,
                                        bitmap,
                                        viewHolder.icon
                                    )
                                }
                            }
                        })
                }
            }
        } else {
            viewHolder.icon.setImageBitmap(bitmap)
            BitmapCacheManager.setThumbnail(item.path, bitmap, viewHolder.icon)
        }
    }

    fun setIconPdf(viewHolder: ExplorerViewHolder, item: ExplorerItem) {
        val context = viewHolder.itemView.context
        val bitmap = BitmapCacheManager.getThumbnail(item.path)
        if (bitmap == null) {
            viewHolder.icon.setImageResource(R.drawable.ic_file_pdf)
            if (isThumbnailEnabled) {
                val task = LoadThumbnailTask(
                    context,
                    viewHolder.icon,
                    viewHolder.iconSmall,
                    ExplorerItem.FILETYPE_PDF
                )
                task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, item.path)
            }
        } else { // 로딩된 비트맵을 셋팅
            viewHolder.icon.setImageBitmap(bitmap)
            BitmapCacheManager.setThumbnail(item.path, bitmap, viewHolder.icon)
        }
    }

    val isThumbnailEnabled: Boolean
        get() = try {
            !thumbnailDisabled
        } catch (e: NullPointerException) {
            true
        }

    fun setIconZip(viewHolder: ExplorerViewHolder, item: ExplorerItem) {
        Timber.e("setIconZip " + item.path)
        val context = viewHolder.itemView.context
        val bitmap = BitmapCacheManager.getThumbnail(item.path)
        if (bitmap == null) {
            viewHolder.icon.setImageResource(R.drawable.ic_file_zip)
            if (BuildConfig.PREVIEW_ZIP && isThumbnailEnabled) {
                val task = LoadZipThumbnailTask(context, viewHolder.icon, viewHolder.iconSmall)
                task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, item.path)
            }
        } else {
            viewHolder.icon.setImageBitmap(bitmap)
            BitmapCacheManager.setThumbnail(item.path, bitmap, viewHolder.icon)
        }
    }

    fun setIconApk(viewHolder: ExplorerViewHolder, item: ExplorerItem) {
        val context = viewHolder.itemView.context
        val bitmap = BitmapCacheManager.getThumbnail(item.path)
        if (bitmap == null) {
            viewHolder.icon.setImageResource(R.drawable.ic_file_apk)
            if (isThumbnailEnabled) {
                val task = LoadThumbnailTask(
                    context,
                    viewHolder.icon,
                    viewHolder.iconSmall,
                    ExplorerItem.FILETYPE_APK
                )
                task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, item.path)
            }
        } else {
            viewHolder.icon.setImageBitmap(bitmap)
            BitmapCacheManager.setThumbnail(item.path, bitmap, viewHolder.icon)
        }
    }

    fun setIconVideo(viewHolder: ExplorerViewHolder, item: ExplorerItem) {
        val context = viewHolder.itemView.context
        val bitmap = BitmapCacheManager.getThumbnail(item.path)
        if (bitmap == null) {
            if (item.path.endsWith(".mp4")) viewHolder.icon.setImageResource(R.drawable.ic_file_mp4) else if (item.path.endsWith(
                    ".fla"
                )
            ) viewHolder.icon.setImageResource(R.drawable.ic_file_fla) else viewHolder.icon.setImageResource(
                R.drawable.ic_file_avi
            )
            if (isThumbnailEnabled) {
                val task = LoadThumbnailTask(
                    context,
                    viewHolder.icon,
                    viewHolder.iconSmall,
                    ExplorerItem.FILETYPE_VIDEO
                )
                task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, item.path)
            }
        } else { // 로딩된 비트맵을 셋팅
            viewHolder.icon.setImageBitmap(bitmap)
            BitmapCacheManager.setThumbnail(item.path, bitmap, viewHolder.icon)
        }
    }

    fun setIcon(viewHolder: ExplorerViewHolder, item: ExplorerItem) {
        //iconSmall을 icon대신에 tag용으로 사용한다.
        //icon의 tag는 glide가 사용한다.
        viewHolder.iconSmall.tag = item.path
        viewHolder.type = item.type
        if (item.type == ExplorerItem.FILETYPE_IMAGE) {
            setIconImage(viewHolder, item)
        } else if (item.type == ExplorerItem.FILETYPE_VIDEO) {
            setIconVideo(viewHolder, item)
        } else if (item.type == ExplorerItem.FILETYPE_ZIP) {
            setIconZip(viewHolder, item)
        } else if (item.type == ExplorerItem.FILETYPE_PDF) {
            setIconPdf(viewHolder, item)
        } else if (item.type == ExplorerItem.FILETYPE_APK) {
            setIconApk(viewHolder, item)
        } else {
            viewHolder.iconSmall.tag = null
            setIconTypeDefault(viewHolder, item)
        }
    }

    fun setIconDefault(viewHolder: ExplorerViewHolder, item: ExplorerItem) {
        when (item.type) {
            ExplorerItem.FILETYPE_FOLDER -> viewHolder.icon.setImageResource(R.drawable.ic_file_folder)
            else -> viewHolder.icon.setImageResource(R.drawable.ic_file_normal)
        }
    }

    fun setIconTypeDefault(viewHolder: ExplorerViewHolder, item: ExplorerItem) {
        when (item.type) {
            ExplorerItem.FILETYPE_AUDIO -> viewHolder.icon.setImageResource(R.drawable.ic_file_mp3)
            ExplorerItem.FILETYPE_FILE -> viewHolder.icon.setImageResource(R.drawable.ic_file_normal)
            ExplorerItem.FILETYPE_FOLDER -> viewHolder.icon.setImageResource(R.drawable.ic_file_folder)
            ExplorerItem.FILETYPE_TEXT -> viewHolder.icon.setImageResource(R.drawable.ic_file_txt)
        }
    }

    open fun bindViewHolderExplorer(viewHolder: ExplorerViewHolder, position: Int) {}
    abstract fun inflateLayout(parent: ViewGroup): View

    open fun getFirstVisibleItem(recyclerView: RecyclerView): Int {
        return 0
    }

    open fun getVisibleItemCount(recyclerView: RecyclerView): Int {
        return 0
    }

    init {
        this.fileList = fileList
    }
}