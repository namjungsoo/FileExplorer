package com.duongame.task.file

import android.app.Activity
import android.content.DialogInterface
import android.os.AsyncTask
import android.view.View
import com.duongame.R
import com.duongame.adapter.ExplorerItem
import com.duongame.dialog.DeleteDialog
import com.duongame.file.FileExplorer
import com.duongame.file.FileHelper.getParentPath
import com.duongame.file.LocalExplorer
import com.duongame.helper.ToastHelper.error
import com.duongame.helper.ToastHelper.success
import java.io.File
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by namjungsoo on 2017-12-29.
 */
class DeleteTask(activity: Activity) : AsyncTask<Void?, Int?, Boolean>() {
    private var fileList: ArrayList<ExplorerItem>? = null
    private var deleteList: ArrayList<ExplorerItem>? = null
    private var dialogWeakReference: WeakReference<DeleteDialog>? = null
    private val activityWeakReference: WeakReference<Activity> = WeakReference(activity)
    private var cancelled = false

    // 생성자에서 사용
    private var onDismissListener: DialogInterface.OnDismissListener? = null
    fun setOnDismissListener(listener: DialogInterface.OnDismissListener?) {
        onDismissListener = listener
    }

    // 삭제할 파일과 폴더를 입력해야 한다.
    // 폴더 삭제도 가능한지 확인해봐야 한다.
    fun setFileList(fileList: ArrayList<ExplorerItem>?) {
        this.fileList = fileList
    }

    override fun onPreExecute() {
        super.onPreExecute()
        dialogWeakReference = WeakReference(DeleteDialog())
        val dialog = dialogWeakReference!!.get()
        if (dialog != null) {
            // 확인 버튼이 눌려쥐면 task를 종료한다.
            dialog.onPositiveClickListener = DialogInterface.OnClickListener { dialog, which ->
                cancel(
                    true
                )
            }
            dialog.onDismissListener = onDismissListener
            val activity = activityWeakReference.get()
            if (activity != null) {
                dialog.show(activity.fragmentManager, "delete")
            }
        }
    }

    protected override fun doInBackground(vararg voids: Void?): Boolean {
        deleteList = ArrayList()
        if (fileList == null) return false
        for (i in fileList!!.indices) {
            val item = fileList!![i]
            if (!item.selected) continue
            if (item.type == ExplorerItem.FILETYPE_FOLDER) {
                // 폴더의 경우 하위 모든 아이템을 찾은뒤에 더한다.
                val explorer: FileExplorer = LocalExplorer()
                val result: FileExplorer.Result = explorer.apply {
                    isRecursiveDirectory = true
                    isHiddenFile = true
                    isExcludeDirectory = false
                    isImageListEnable = false
                }.search(item.path)

                // 폴더 하위 파일의 경우에는 폴더 이름과 파일명을 적어줌
                for (j in result.fileList.indices) {

                    // 선택된 폴더의 최상위 폴더의 폴더명을 제외한 나머지가 name임
                    val subItem = result.fileList[j]
                    if (subItem.path.startsWith(item.path)) {
                        subItem.name = subItem.path.replace(getParentPath(item.path) + "/", "")
                    }
                }
                deleteList!!.addAll(result.fileList)
                deleteList!!.add(item)
            } else {
                deleteList!!.add(item)
            }
        }
        for (i in deleteList!!.indices) {
            // 파일을 하나하나 지운다.
            try {
                if (isCancelled) {
                    cancelled = true
                    return false
                } else {
                    work(deleteList!![i].path)
                    publishProgress(i)
                }
            } catch (e: SecurityException) {
                // 지울수 없는 파일
                return false
            }
        }
        return true
    }

    fun work(path: String?) {
        File(path).delete()
    }

    protected override fun onProgressUpdate(vararg values: Int?) {
        val progress = values[0] ?: return
        val name = deleteList!![progress].name
        val size = deleteList!!.size
        val total = (progress.toFloat() + 1) / size
        val percent = (total * 100).toInt()
        val dialog = dialogWeakReference!!.get()
        if (dialog != null) {
            dialog.fileName!!.text = name
            dialog.eachProgress!!.progress = 100
            dialog.totalProgress!!.progress = percent
            val activity = activityWeakReference.get()
            if (activity != null) {
                dialog.eachText!!.visibility = View.VISIBLE
                dialog.eachText!!.text = String.format(activity.getString(R.string.each_text), 100)
                dialog.totalText!!.visibility = View.VISIBLE
                dialog.totalText!!.text =
                    String.format(
                        activity.getString(R.string.total_text),
                        progress + 1,
                        size,
                        percent
                    )
            }
        }
    }

    override fun onPostExecute(result: Boolean) {
        super.onPostExecute(result)

//        Timber.e("onPostExecute");
        val activity = activityWeakReference.get()
        if (activity != null) {
            if (result) success(activity, R.string.toast_file_delete) else {
                if (cancelled) error(activity, R.string.toast_cancel) else error(
                    activity,
                    R.string.toast_error
                )
            }
        }
        val dialog = dialogWeakReference!!.get()
        dialog?.dismiss()
    }

}