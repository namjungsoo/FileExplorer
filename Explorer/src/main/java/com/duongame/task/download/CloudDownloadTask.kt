package com.duongame.task.download

import android.app.Activity
import android.content.DialogInterface
import android.os.AsyncTask
import android.view.View
import com.duongame.R
import com.duongame.adapter.ExplorerItem
import com.duongame.dialog.DownloadDialog
import com.duongame.file.FileHelper.Progress
import com.duongame.helper.ToastHelper.error
import com.duongame.helper.ToastHelper.info
import java.io.File
import java.lang.ref.WeakReference

abstract class CloudDownloadTask internal constructor(
    activity: Activity,
    private val mCallback: Callback
) : AsyncTask<ExplorerItem?, Progress?, File?>() {
    interface Callback {
        fun onDownloadComplete(result: File)
        fun onError(e: Exception)
    }

    protected var mException: Exception? = null
    protected var name: String? = null
    protected var cancelled = false

    // 생성자에서 사용
    private var dialogWeakReference: WeakReference<DownloadDialog>? = null
    protected var activityWeakReference: WeakReference<Activity> = WeakReference(activity)

    protected abstract override fun doInBackground(vararg explorerItems: ExplorerItem?): File?

    override fun onPreExecute() {
        super.onPreExecute()
        dialogWeakReference = WeakReference(DownloadDialog())

        // 다이얼로그를 show 하자
        val dialog = dialogWeakReference!!.get()
        if (dialog != null) {
            dialog.onPositiveClickListener = DialogInterface.OnClickListener { dialogInterface, i -> // 버튼이 1개이므로 무조건 취소한다.
                cancel(true)
            }
            val activity = activityWeakReference.get()
            if (activity != null) {
                dialog.show(activity.fragmentManager, "download")
            }
        }
    }

    // 파일번호와 현재 파일의 percent로 progress를 publish 한다.
    fun updateProgress(i: Int, percent: Int) {
        // progress를 기입해야 한다.
        val progress = Progress()
        progress.index = i
        progress.percent = percent
        publishProgress(progress)
    }

    protected override fun onProgressUpdate(vararg values: Progress?) {
        val progress = values[0] ?: return

        // 파일명 (name)

        // 전체 파일 갯수
        val size = 1

        // 전체 파일 리스트 퍼센트
        val totalPercent = progress.index * 100 / size
        val dialog = dialogWeakReference!!.get()
        if (dialog != null) {
            dialog.fileName!!.text = name
            dialog.eachProgress!!.progress = progress.percent
            dialog.totalProgress!!.progress = totalPercent
            val activity = activityWeakReference.get()
            if (activity != null) {
                dialog.eachText!!.visibility = View.VISIBLE
                dialog.eachText!!.text =
                    String.format(activity.getString(R.string.each_text), progress.percent)
                dialog.totalText!!.visibility = View.VISIBLE
                dialog.totalText!!.text = String.format(
                    activity.getString(R.string.total_text),
                    progress.index + 1,
                    size,
                    totalPercent
                )
            }
        }
    }

    override fun onPostExecute(result: File?) {
        super.onPostExecute(result)

        // 정리하고 toast를 띄워주고 팝업을 닫는다.
        val activity = activityWeakReference.get()
        if (activity != null) {
            if (result != null) { // 성공일 경우
                info(activity, R.string.toast_cloud_complete)
            } else {
                // 취소된 경우와 에러가 난 경우가 있다.
                if (cancelled) error(activity, R.string.toast_cancel) else error(
                    activity,
                    R.string.toast_error
                )
            }
        }

        // 팝업을 닫는다.
        val dialog = dialogWeakReference!!.get()
        dialog?.dismiss()
        if (mException != null) {
            mCallback.onError(mException!!)
        } else if (result != null) {
            mCallback.onDownloadComplete(result!!)
        }
    }
}