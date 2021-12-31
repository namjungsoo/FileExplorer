package com.duongame.task.file

import android.app.Activity
import android.content.DialogInterface
import android.os.AsyncTask
import android.view.View
import com.duongame.R
import com.duongame.adapter.ExplorerItem
import com.duongame.dialog.OverwriteDialog
import com.duongame.dialog.PasteDialog
import com.duongame.file.FileExplorer
import com.duongame.file.FileHelper.BLOCK_SIZE
import com.duongame.file.FileHelper.Progress
import com.duongame.file.FileHelper.getNewFileName
import com.duongame.file.FileHelper.getParentPath
import com.duongame.file.LocalExplorer
import com.duongame.helper.ToastHelper.error
import com.duongame.helper.ToastHelper.info
import org.apache.commons.io.FileUtils
import timber.log.Timber
import java.io.*
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by namjungsoo on 2017-12-29.
 */
class PasteTask(activity: Activity) : AsyncTask<Void?, Progress?, Boolean>() {
    private var fileList: ArrayList<ExplorerItem>? = null
    private var pasteList: ArrayList<ExplorerItem>? = null
    private var capturePath: String? = null
    private var pastePath: String? = null
    private var cut = false
    private var makeCopy = false

    // 생성자에서 사용
    private var dialogWeakReference: WeakReference<PasteDialog>? = null
    private val activityWeakReference: WeakReference<Activity> = WeakReference(activity)
    private var onDismissListener: DialogInterface.OnDismissListener? = null
    private val lock = Object()
    private var applyAll = false
    private var skip = false
    private var cancel = false
    private var cancelled = false
    private var srcNewFolderMap: HashMap<String, String>? = null
    private var createdPathList: ArrayList<String>? = null
    fun setOnDismissListener(listener: DialogInterface.OnDismissListener?) {
        onDismissListener = listener
    }

    // 삭제할 파일과 폴더를 입력해야 한다.
    // 폴더 삭제도 가능한지 확인해봐야 한다.
    fun setFileList(fileList: ArrayList<ExplorerItem>?) {
        this.fileList = fileList
    }

    fun setPath(capturePath: String, pastePath: String) {
        this.capturePath = capturePath
        this.pastePath = pastePath
        if (capturePath == pastePath) {
            makeCopy = true
            srcNewFolderMap = HashMap()
        }
    }

    fun setIsCut(cut: Boolean) {
        this.cut = cut
    }

    override fun onPreExecute() {
        super.onPreExecute()

        createdPathList = ArrayList()
        dialogWeakReference = WeakReference(PasteDialog())
        val dialog = dialogWeakReference!!.get()
        if (dialog != null) {
            // 확인 버튼이 눌려쥐면 task를 종료한다.
            dialog.onPositiveClickListener = DialogInterface.OnClickListener { dialog, which ->
                cancel(
                    true
                )
            }
            dialog.setIsCut(cut)
            dialog.onDismissListener = onDismissListener
            val activity = activityWeakReference.get()
            if (activity != null) {
                dialog.show(activity.fragmentManager, "paste")
            }
        }
    }

    fun searchFile(path: String?): FileExplorer.Result {
        val explorer: FileExplorer = LocalExplorer()
        return explorer.apply {
            isRecursiveDirectory = true
            isHiddenFile = true
            isExcludeDirectory = false
            isImageListEnable = false
        }.search(path)
    }

    fun prepareFolder(result: FileExplorer.Result?, path: String?) {
        // 폴더 하위 파일의 경우에는 폴더 이름과 파일명을 적어줌
        if (result != null && result.fileList != null) {
            for (j in result.fileList.indices) {
                prepareLocalPathToName(result, path, j)
            }
            // 7ztest/_DSC5307.jpg
            pasteList!!.addAll(result.fileList)
        }
    }

    fun prepareLocalPathToName(result: FileExplorer.Result, path: String?, j: Int) {
        // 선택된 폴더의 최상위 폴더의 폴더명을 제외한 나머지가 name임
        val subItem = result.fileList[j]
        if (!subItem.path.startsWith(path!!)) return

        // 상대 패스를 만들어 줌
        // 이거는 Download
        // item.path는 7ztest
        val parentPath = getParentPath(path)

        // 이 결과를 이름에 박아둠
        subItem.name = subItem.path.replace("$parentPath/", "")
    }

    fun preparePasteList() {
        pasteList = ArrayList()

        // fileList에는 선택된 파일만 들어옴
        for (i in fileList!!.indices) {
            val item = fileList!![i]
            if (item.type == ExplorerItem.FILETYPE_FOLDER) {

                // 폴더의 경우 하위 모든 아이템을 찾은뒤에 더한다.
                val result = searchFile(item.path)
                for (j in result.fileList.indices) {
                    Timber.e("fileList j=" + j + " " + result.fileList[j].path)
                }
                prepareFolder(result, item.path)

                // 폴더 자기 자신도 더함
                // 폴더를 나중에 더할 경우 폴더를 복사할때 에러가 발생함
                pasteList!!.add(item)
            } else {
                pasteList!!.add(item)
            }
        }
    }

    fun processTask(): Boolean {
        for (i in pasteList!!.indices) {
            // 파일을 하나하나 지운다.
            try {
                if (isCancelled) {
                    cancelled = true
                    return false
                } else {
                    if (!process(i, pasteList!![i].path)) {
                        return false
                    }
                }
            } catch (e: SecurityException) {
                // 지울수 없는 파일
                return false
            } catch (e: InterruptedException) {
                // 지울수 없는 파일
                return false
            }
        }
        return true
    }

    protected override fun doInBackground(vararg voids: Void?): Boolean {
        preparePasteList()
        return processTask()
    }

    fun alertOverwrite(path: String?) {
        val activity = activityWeakReference.get()
        activity?.runOnUiThread {
            val dialog = OverwriteDialog()
            dialog.setPath(path)
            dialog.setLock(lock)
            dialog.onFinishListener = object: OverwriteDialog.OnFinishListener {
                override fun onFinish(applyAll: Boolean, skip: Boolean, cancel: Boolean) {
                    this@PasteTask.applyAll = applyAll
                    this@PasteTask.skip = skip
                    this@PasteTask.cancel = cancel
                }
            }
            dialog.show(activity.fragmentManager, "overwrite")
        }
    }

    fun updateProgress(i: Int, percent: Int) {
        // progress를 기입해야 한다.
        val progress = Progress()
        progress.index = i
        progress.percent = percent
        publishProgress(progress)
    }

    @Throws(IOException::class)
    fun processInternal(i: Int, src: File, dest: File?) {
        if (cut) {
            if (src.isDirectory) {
                // src가 디렉토리 이면, 이미 하위 파일은 폴더를 생성하고 옮겼으므로 delete해준다.
                Timber.e("delete folder path=" + src.path)
                src.delete()
                //FileUtils.forceDelete(src);
                Timber.e("complete delete folder path=" + src.path)
            } else {
                Timber.e("move folder path=" + src.path)
                // 이동한다.
                FileUtils.moveFile(src, dest)
                updateProgress(i, 100)
            }
        } else {
            // 복사한다.
//            Timber.e("processInternal copy");
            val inputStream = FileInputStream(src)
            val outputStream = FileOutputStream(dest)
            val buf = ByteArray(BLOCK_SIZE)
            var nRead = 0
            var totalRead: Long = 0
            val srcLength = src.length()
            while (inputStream.read(buf).also { nRead = it } > 0) {
                outputStream.write(buf, 0, nRead)
                totalRead += nRead.toLong()
                val percent = totalRead * 100 / srcLength
                Timber.e("percent=$percent totalRead=$totalRead srcLength=$srcLength")
                updateProgress(i, percent.toInt())
            }
            outputStream.close()
            inputStream.close()
        }
    }

    @Throws(InterruptedException::class)
    fun checkOverwrite(dest: File, destPath: String?): Boolean {
        // 대상 파일이 있는 경우 팝업을 물어보고 지우거나/스킵하거나/덮어씌운다.
        if (!applyAll && dest.exists()) {
            // 팝업을 띄운다.
            // 그리고 대기한다.
            alertOverwrite(destPath)
            synchronized(lock) { lock.wait() }
            if (cancel) {
                return false
            }
            if (!skip) {
                // 파일을 지워 주어야 함
                dest.delete()
            }
        }
        return true
    }

    fun processParentFolder(destPath: String?) {
        // 대상 폴더의 상위폴더까지 무조건 생성
        val parentPath = getParentPath(destPath!!)
        val parent = File(parentPath)
        parent.mkdirs()
    }

    fun getRelativePath(srcPath: String): String {
        return srcPath.replace("$capturePath/", "") // 상대적인 패스
    }

    fun processDestPath(relativePath: String): String {
        // 원본의 패스를 없애고 파일명과 확장자만 얻어옴
        //String relativePath = srcPath.replace(capturePath + "/", "");// 상대적인 패스

        // makeCopy일때 상대패스가 폴더를 포함하면
        return if (makeCopy) { // 이때는 pastePath와 capturePath가 같다.
            if (relativePath.contains("/")) {
                // 첫번째 폴더명과 이후의 패스를 분리함
                val subPath = relativePath.substring(relativePath.indexOf("/") + 1)

                // 첫번째 패스만을 골라낸다.
                val copyPath =
                    capturePath + "/" + relativePath.substring(0, relativePath.indexOf("/"))
                val newPath: String?
                if (srcNewFolderMap!!.containsKey(copyPath)) {
                    newPath = srcNewFolderMap!![copyPath]
                } else {
                    // 첫번째 패스의 새로운 패스를 찾는다.
                    newPath = getNewFileName(copyPath)
                    srcNewFolderMap!![copyPath] = newPath
                }

                // 새로운 패스 + 이후의 패스
                "$newPath/$subPath"
            } else {
                getNewFileName("$pastePath/$relativePath")
            }
        } else {
            "$pastePath/$relativePath" // 대상 패스 + 상대적인 패스
        }
    }

    fun getTopPath(relativePath: String): String? {
        val index = relativePath.indexOf("/")
        return if (index == -1) null else relativePath.substring(0, index)
    }

    // srcPath는 pasteList로부터 가져온 패스의 원본
    @Throws(InterruptedException::class)
    fun process(i: Int, srcPath: String): Boolean {
        val src = File(srcPath)
        val relativePath = getRelativePath(srcPath)
        val destPath = processDestPath(relativePath)
        //String destPath = processDestPath(srcPath);

        // top의 폴더가 이미 있을 경우에는 폴더를 새로 만들어줌
        // 그리고 이하의 모든 패스를 변경하여야 함
        val topPath = getTopPath(relativePath)
        if (topPath != null) {
            val pasteTopPath = "$pastePath/$topPath"
            val top = File(pasteTopPath)
            if (top.exists() && top.isDirectory) {
            } else {
                // 존재하지 않으면 내가 만들었다고 체크해둠
                createdPathList!!.add(pasteTopPath)
            }
        }

        // 이건 그냥 모든 폴더를 만들어 주는 것
        processParentFolder(destPath)

        // 내가 만든 폴더에 속하면 overwrite는 테스트 하지 않음
        val dest = File(destPath)

        // top path가 아닌 하위 폴더는 관여 안함
        if (dest.isDirectory && destPath != topPath) {
        } else {
            if (createdPathList!!.indexOf(destPath) == -1) {
                if (!checkOverwrite(dest, destPath)) return false
            }
        }

        // skip이 아니면, 위에서 이미 지웠으니 무조건 overwrite이다.
        if (!skip) {
            try {
                processInternal(i, src, dest)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                return false
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
        }
        return true
    }

    protected override fun onProgressUpdate(vararg values: Progress?) {
        val progress = values[0] ?: return
        val name = pasteList!![progress.index].name
        val size = pasteList!!.size
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
        } else {
//            Timber.e("PasteTask", "dialog is null");
        }
    }

    override fun onPostExecute(result: Boolean) {
        super.onPostExecute(result)

        val activity = activityWeakReference.get()
        if (activity != null) {
            if (result) {
                if (cut) info(activity, R.string.toast_file_paste_cut) else info(
                    activity,
                    R.string.toast_file_paste_copy
                )
            } else {
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