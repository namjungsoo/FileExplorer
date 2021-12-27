package com.duongame.fragment

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.duongame.App.Companion.instance
import com.duongame.BuildConfig
import com.duongame.ExplorerConfig.MAX_THUMBNAILS
import com.duongame.R
import com.duongame.activity.main.BaseMainActivity
import com.duongame.activity.viewer.PhotoActivity.Companion.getLocalIntent
import com.duongame.activity.viewer.VideoActivity
import com.duongame.adapter.*
import com.duongame.bitmap.BitmapCacheManager.getThumbnailCount
import com.duongame.bitmap.BitmapCacheManager.removeAllThumbnails
import com.duongame.databinding.DialogSingleBinding
import com.duongame.databinding.DialogZipBinding
import com.duongame.databinding.FragmentExplorerBinding
import com.duongame.db.BookLoader.load
import com.duongame.db.BookLoader.openLastBook
import com.duongame.db.ExplorerItemDB.Companion.getInstance
import com.duongame.dialog.SortDialog
import com.duongame.file.FileHelper.filterAudioFileList
import com.duongame.file.FileHelper.filterImageFileList
import com.duongame.file.FileHelper.filterVideoFileList
import com.duongame.file.FileHelper.getNameWithoutTar
import com.duongame.file.FileHelper.getNewFileName
import com.duongame.file.FileHelper.getParentPath
import com.duongame.helper.AlertHelper.showAlert
import com.duongame.helper.AlertHelper.showAlertWithAd
import com.duongame.helper.AppHelper.appName
import com.duongame.helper.AppHelper.isComicz
import com.duongame.helper.ExtSdCardHelper.externalSdCardPath
import com.duongame.helper.PreferenceHelper
import com.duongame.helper.PreferenceHelper.lastCloud
import com.duongame.helper.PreferenceHelper.lastPath
import com.duongame.helper.PreferenceHelper.lastPosition
import com.duongame.helper.PreferenceHelper.lastTop
import com.duongame.helper.ToastHelper.error
import com.duongame.helper.ToastHelper.info
import com.duongame.helper.ToastHelper.showToast
import com.duongame.helper.ToastHelper.success
import com.duongame.helper.ToastHelper.warning
import com.duongame.manager.AdBannerManager.initPopupAd
import com.duongame.manager.PermissionManager.checkStoragePermissions
import com.duongame.manager.PositionManager.getPosition
import com.duongame.manager.PositionManager.getTop
import com.duongame.task.download.CloudDownloadTask
import com.duongame.task.download.DropboxDownloadTask
import com.duongame.task.download.GoogleDriveDownloadTask
import com.duongame.task.file.*
import com.duongame.task.zip.UnzipTask
import com.duongame.task.zip.ZipTask
import com.duongame.view.DividerItemDecoration
import timber.log.Timber
import java.io.File
import java.util.*

/**
 * Created by namjungsoo on 2016-11-23.
 */
class ExplorerFragment : BaseFragment(), ExplorerAdapter.OnItemClickListener,
    ExplorerAdapter.OnItemLongClickListener {

    // Model 관련
    // 파일 관련
    lateinit var currentView: RecyclerView
    lateinit var adapter: ExplorerAdapter
    var fileList: ArrayList<ExplorerItem> = arrayListOf()

    // 붙이기 관련
    private var selectedFileList: ArrayList<ExplorerItem> = arrayListOf()
    private var cut = false

    private var localSearchTask: LocalSearchTask? = null
    private var dropboxSearchTask: DropboxSearchTask? = null
    private var googleDriveSearchTask: GoogleDriveSearchTask? = null

    // 선택
    //    private boolean selectMode = false;
    //    private boolean pasteMode = false;// 붙여넣기 모드는 뒤로가기 버튼이 있고
    private var mode = MODE_NORMAL
    private lateinit var capturePath: String
    private var extSdCard: String = ""

    // 정렬
    var sortType = 0
        private set
    var sortDirection = 0
        private set
    private var canClick = false
    var viewType = SWITCH_LIST
        private set
    private var backupDropbox = false
    private var backupGoogleDrive = false
    private var handler: Handler? = null
    protected var playerMode = false
    lateinit var binding: FragmentExplorerBinding

    fun isCanClick(): Boolean {
        return canClick
    }

    fun setCanClick(canClick: Boolean) {
        this.canClick = canClick
        val activity = activity as BaseMainActivity?
        if (activity != null) {
            val progressBar = activity.progressBarLoading
            if (progressBar != null) {
                progressBar.visibility = if (canClick) View.GONE else View.VISIBLE
            }
        }
    }

    private fun loadFileListFromLocalDB() {
        handler = Handler()
        // 현재 파일 리스트를 얻어서 바로 셋팅
        Timber.e("ExplorerItemDB begin")
        setCanClick(false)
        Thread {
            Timber.e("ExplorerItemDB thread")
            val fileList =
                getInstance(context!!).db.explorerItemDao().getItems() as ArrayList<ExplorerItem>
            val imageList = filterImageFileList(fileList)
            val videoList = filterVideoFileList(fileList)
            val audioList = filterAudioFileList(fileList)
            val app = instance
            app.fileList = fileList
            app.imageList = imageList
            app.videoList = videoList
            app.audioList = audioList

            // DB에 저장된게 있으면 adapter에 적용
            if (fileList.size > 0) {
                adapter?.fileList = fileList
                handler?.postAtFrontOfQueue {
                    adapter?.notifyDataSetChanged()

                    // 이제 클릭할수 있음
                    // 프로그레스바 안보이기
                    setCanClick(true)
                    Timber.e("ExplorerItemDB end")
                }
            }
        }.start()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_explorer, container, false)

        initUI()
        Timber.e("initUI end")
        initViewType()
        Timber.e("initViewType end")
        loadFileListFromLocalDB()
        sortType = PreferenceHelper.sortType
        sortDirection = PreferenceHelper.sortDirection
        extSdCard = externalSdCardPath
        if (extSdCard != null) {
            binding.btnSdcard.visibility = View.VISIBLE
        }
        val path = lastPath
        instance.lastPath = path
        Timber.e("onCreateView end")
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val activity = activity as BaseMainActivity?
        if (activity != null) {
            if (!activity.showReview) {
                if (isComicz) {
                    openLastBook(activity)
                }
            }
        }
        Timber.e("onActivityCreated end")
    }

    override fun onResume() {
        super.onResume()

        // 밖에 나갔다 들어오면 리프레시함
        onRefresh()
    }

    override fun onPause() {
        super.onPause()

        //TODO: 스크롤 위치 복구해줘야함
        val position = 0
        val top = currentViewScrollTop
        Thread {
            val activity = activity
            if (activity != null) {
                lastPosition = position
                lastTop = top
            }
        }.start()
    }

    val isPasteMode: Boolean
        get() = mode == MODE_PASTE

    private fun initUI() {
        binding.btnHome.setOnClickListener(View.OnClickListener {
            cloud = CLOUD_LOCAL
            try {
                updateFileList(instance.initialPath)
            } catch (e: NullPointerException) {
            }
        })
        binding.btnUp.setOnClickListener { // up으로 갈수있는 조건은 normal, paste 모드이다.
            // 나머지는 normal로 모드를 변경한다.
            if (mode == MODE_NORMAL || mode == MODE_PASTE) gotoUpDirectory() else onNormalMode()
        }
        binding.btnSdcard.setOnClickListener {
            cloud = CLOUD_LOCAL
            updateFileList(extSdCard)
        }
        binding.btnDropbox.setOnClickListener {
            cloud = CLOUD_DROPBOX
            updateFileList("/")
        }
        binding.btnDropbox.visibility = if (backupDropbox) View.VISIBLE else View.GONE

        binding.btnGdrive.setOnClickListener {
            cloud = CLOUD_GOOGLEDRIVE
            updateFileList("/")
        }
        binding.btnGdrive.visibility = if (backupGoogleDrive) View.VISIBLE else View.GONE

        binding.storageIndicator.refresh()
        binding.btnPermission.setOnClickListener { checkStoragePermissions(activity) }
    }

    fun updateDropboxUI(show: Boolean) {
        Timber.e("updateDropboxUI $show")

        if (show) {
            binding.btnDropbox.visibility = View.VISIBLE
        } else {
            // 로그아웃인 상황이니 최초로 간다.
            cloud = CLOUD_LOCAL
            if (binding.btnDropbox.visibility == View.VISIBLE) {
                updateFileList(instance.initialPath)
                binding.btnDropbox.visibility = View.GONE
            }
        }
        binding.storageIndicator.refresh()
    }

    fun updateGoogleDriveUI(show: Boolean) {
        Timber.e("updateGoogleDriveUI $show")

        if (show) {
            binding.btnGdrive.visibility = View.VISIBLE
        } else {
            // 로그아웃인 상황이니 최초로 간다.
            cloud = CLOUD_LOCAL
            if (binding.btnGdrive.visibility == View.VISIBLE) {
                updateFileList(instance.initialPath)
                binding.btnGdrive.visibility = View.GONE
            }
        }
        binding.storageIndicator.refresh()
    }

    private fun initViewType() {
        changeViewType(PreferenceHelper.viewType)
    }

    fun changeViewType(viewType: Int) {
        val activity = activity ?: return
        this.viewType = viewType
        binding.switcher.displayedChild = viewType
        when (viewType) {
            SWITCH_LIST -> {
                synchronized(this) { adapter = ExplorerListAdapter(fileList) }

                currentView = binding.listExplorer
                currentView.layoutManager = LinearLayoutManager(activity)
                currentView.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST))
            }
            SWITCH_GRID -> {
                synchronized(this) { adapter = ExplorerGridAdapter(fileList) }

                currentView = binding.gridExplorer
                currentView.layoutManager = GridLayoutManager(activity, 4)
            }
            SWITCH_NARROW -> {
                synchronized(this) { adapter = ExplorerNarrowAdapter(fileList) }

                currentView = binding.listExplorer
                currentView.layoutManager = LinearLayoutManager(activity)
                currentView.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST))
            }
        }
        adapter.mode = mode
        adapter.setOnItemClickListener(this)
        adapter.setOnLongItemClickListener(this)

        currentView.adapter = adapter
        currentView.addOnScrollListener(ExplorerScrollListener())
        PreferenceHelper.viewType = viewType
    }

    // 새로운 파일이 추가 되었을때 스캔을 하라는 의미이다.
    // 쓰레드에서 동작하므로 여러가지 문제점이 생겼다.
    // 일단은 imageList를 복사하고 나서 작업을 시작하자.
    // 중요하지 않은 작업이므로 전체 try-catch를 건다.
    fun requestThumbnailScan() {
        try {
            val fileResult = fileResult ?: return
            val imageList = fileResult.imageList
            val activity = activity
            for (item in imageList) {
                if (activity == null) break
                if (item == null) continue
                activity.sendBroadcast(
                    Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.parse("file://" + item.path)
                    )
                )
            }
        } catch (e: Exception) {
        }
    }

    fun gotoUpDirectory() {
        try {
            var path = instance.lastPath
            path = path!!.substring(0, path.lastIndexOf('/'))
            if (path.isEmpty()) {
                path = "/"
            }
            backupPosition()
            updateFileList(path)
        } catch (e: NullPointerException) {
        }
    }

    fun onClickDirectory(item: ExplorerItem) {
        try {
            val newPath: String
            val lastPath = instance.lastPath
            newPath = if (lastPath == "/") {
                lastPath + item.name
            } else {
                lastPath + "/" + item.name
            }
            updateFileList(newPath)
        } catch (e: NullPointerException) {
        }
    }

    fun onClickImage(item: ExplorerItem?) {
        val intent = getLocalIntent(context, item!!)
        startActivity(intent)
    }

    fun onClickVideo(item: ExplorerItem?) {
        val intent = Intent(context, VideoActivity::class.java)
        intent.putExtra("item", item)
        startActivity(intent)
    }

    fun onClickAudio(item: ExplorerItem) {
        // 현재 화면에서 오디오 플레이를 한다
        // 오디오 리스트를 받아서 리스트에 넣고
        // 플레이를 한다
        val activity = activity as BaseMainActivity? ?: return
        if (mode != MODE_PLAYER) {
            activity.showPlayerUI()
            mode = MODE_PLAYER
        }
        val audioList = instance.audioList
        for (i in audioList!!.indices) {
            if (audioList[i].path == item.path) {
                activity.playAudio(i)
                break
            }
        }
    }

    fun onClickApk(item: ExplorerItem) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val intent = Intent(Intent.ACTION_VIEW)
            val activity = activity ?: return

            //TODO: 외부 SdCard에 있는 설치폴더는 설치가 될것인가?
            // 일단은 exception은 처리하자.
            try {
                val providerName = activity.packageName + ".provider"
                val apkUri = FileProvider.getUriForFile(activity, providerName, File(item.path))
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)
            } catch (e: Exception) {
                error(activity, R.string.toast_error)
            }
        } else {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(
                Uri.parse("file://" + item.path),
                "application/vnd.android.package-archive"
            )
            startActivity(intent)
        }
    }

    fun checkDownloadOverwrite(
        activity: Activity?,
        item: ExplorerItem,
        task: CloudDownloadTask
    ): Boolean {
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(path, item.name)

        // 파일이 있으면 팝업을 통해서 확인해야 함
        return if (file.exists()) {
            val title = appName
            val content = getString(R.string.msg_overwrite)
            val positiveListener =
                DialogInterface.OnClickListener { dialogInterface, i -> // 확인을 눌렀으므로 다운로드하여 덮어씌움
                    task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, item) // metadata = fileId
                    showToast(
                        activity,
                        String.format(resources.getString(R.string.toast_cloud_download), item.name)
                    )
                    dialogInterface.dismiss()
                }
            val negativeListener =
                DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() }
            if (BuildConfig.SHOW_AD) {
                showAlertWithAd(
                    activity!!,
                    title,
                    content,  //positive
                    positiveListener,  //negative
                    negativeListener,
                    null
                )
                initPopupAd(activity) // 항상 초기화 해주어야 함
            } else {
                showAlert(
                    activity!!,
                    title,
                    content,
                    null,  //positive
                    positiveListener,  //negative
                    negativeListener,
                    null
                )
            }
            true
        } else {
            false
        }
    }

    fun downloadAndOpen(activity: Activity?, item: ExplorerItem, task: CloudDownloadTask) {
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, item)
        showToast(
            activity,
            String.format(resources.getString(R.string.toast_cloud_download), item.name)
        )
    }

    fun onClickBookDropbox(activity: Activity?, item: ExplorerItem) {
        // 다운로드를 받은후에 로딩함
        val activity = activity ?: return
        val task = DropboxDownloadTask(activity, object : CloudDownloadTask.Callback {
            // task 내부에서 toast를 처리해주므로 주석처리함
            override fun onDownloadComplete(result: File) {
                item.path = result.absolutePath
                load(activity, item, false)
            }

            override fun onError(e: Exception) {}
        })
        if (!checkDownloadOverwrite(activity, item, task)) { // overwrite하지 않으면 바로 다운로드
            downloadAndOpen(activity, item, task)
        }
    }

    fun onClickBookGoogleDrive(activity: Activity?, item: ExplorerItem) {
        // 다운로드를 받은후에 로딩함
        val activity = activity ?: return
        val task = GoogleDriveDownloadTask(activity, object : CloudDownloadTask.Callback {
            // task 내부에서 toast를 처리해주므로 주석처리함
            override fun onDownloadComplete(result: File) {
                item.path = result.absolutePath
                load(activity, item, false)
            }

            override fun onError(e: Exception) {}
        })
        if (!checkDownloadOverwrite(activity, item, task)) { // overwrite하지 않으면 바로 다운로드
            downloadAndOpen(activity, item, task)
        }
    }

    fun onClickBook(item: ExplorerItem) {
        val activity = activity ?: return
        if (cloud == CLOUD_LOCAL) {
            load(activity, item, false)
        } else if (cloud == CLOUD_DROPBOX) {
            onClickBookDropbox(activity, item)
        } else if (cloud == CLOUD_GOOGLEDRIVE) {
            onClickBookGoogleDrive(activity, item)
        }
    }

    fun onClickZip(item: ExplorerItem) {
        unzipWithDialog(item)
    }

    fun runUnzipTask(item: ExplorerItem, name: String?) {
        try {
            val path = instance.lastPath
            val targetPath: String?
            targetPath = if (name == null) {
                path
            } else {
                "$path/$name"
            }
            val activity = activity ?: return
            val task = UnzipTask(activity)
            task.setPath(targetPath)

            // 여러 파일을 동시에 풀수있도록 함
            // 현재는 1개만 풀수 있음
            val zipList = ArrayList<ExplorerItem>()
            zipList.add(item)
            task.setFileList(zipList)
            task.setOnDismissListener { onRefresh() }
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        } catch (e: NullPointerException) {
        }
    }

    // 폴더가 필요한 경우(gz,bz2가 아닌 경우)
    fun isNeedFolder(path: String): Boolean {
        var needFolder = true
        if (path.endsWith(".gz")) {
            val checkTar = path.replace(".gz", "")
            if (!checkTar.endsWith(".tar")) needFolder = false
        } else if (path.endsWith(".bz2")) {
            val checkTar = path.replace(".bz2", "")
            if (!checkTar.endsWith(".tar")) needFolder = false
        }
        return needFolder
    }

    fun unzipWithDialog(item: ExplorerItem) {
        val needFolder = isNeedFolder(item.path)
        if (!needFolder) {
            //TODO: 이거 이상한데...
            runUnzipTask(item, null)
            return
        }
        val activity = activity ?: return
        val binding: DialogSingleBinding = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_single, null, false)

        // zip파일의 이름을 기준으로 함
        var base = item.name.substring(0, item.name.lastIndexOf("."))
        base = getNameWithoutTar(base)
        try {
            // path/zipname 폴더가 있는지 확인
            val lastPath = instance.lastPath
            val newPath = getNewFileName("$lastPath/$base")
            val newName = newPath.replace("$lastPath/", "")

            // 새로나온 폴더의 이름을 edit에 반영함
            binding.fileName.setText(newName)
            showAlert(activity,
                appName,
                getString(R.string.msg_file_unzip),
                view,
                { dialogInterface, i -> runUnzipTask(item, binding.fileName.text.toString()) },
                null,
                null
            )
        } catch (e: NullPointerException) {
        }
    }

    fun onAdapterItemLongClick(position: Int) {
//        Timber.e("onAdapterItemLongClick=" + position);
        synchronized(this) {
            if (fileList == null) return
            try { // java.lang.IndexOutOfBoundsException
                val item = fileList!![position] ?: return

                // 이미 선택 모드라면 이름변경을 해줌
                if (mode == MODE_SELECT) {
                    //renameFileWithDialog
                    if (selectedFileCount == 1) {
                        renameFileWithDialog(item)
                    } else {
                        warning(activity, R.string.toast_multi_rename_error)
                    }
                } else { // 선택 모드로 진입 + 현재 파일 선택
                    onSelectMode(item, position)
                }
            } catch (ignored: Exception) {
            }
        }
    }

    val selectedFileCount: Int
        get() {
            var count = 0
            synchronized(this) {
                for (i in fileList.indices) {
                    if (fileList[i].selected) {
                        count++
                    }
                }
            }
            return count
        }

    fun sortFileWithDialog() {
        val dialog = SortDialog()
        dialog.setTypeAndDirection(sortType, sortDirection)
        dialog.onSortListener = object: SortDialog.OnSortListener {
            override fun onSort(type: Int, dir: Int) {
                sortType = type
                sortDirection = dir
                val activity = activity
                PreferenceHelper.sortType = sortType
                PreferenceHelper.sortDirection = sortDirection
                try {
                    updateFileList(instance.lastPath)
                } catch (e: NullPointerException) {
                }
            }
        }

        val activity = activity
        if (activity != null) {
            dialog.show(activity.fragmentManager, "sort")
        }
    }

    fun newFolderWithDialog() {
        val activity = activity ?: return
        val binding: DialogSingleBinding = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_single, null, false)
        val base = getString(R.string.new_folder)
        try {
            val lastPath = instance.lastPath
            var newName = getNewFileName("$lastPath/$base")
            newName = newName.replace("$lastPath/", "")
            binding.fileName.setText(newName)
            showAlert(activity,
                appName,
                getString(R.string.msg_new_folder),
                view, { dialog, which ->
                    val newFolder = binding.fileName.text.toString()
                    newFolder(newFolder)
                }, null, null)
        } catch (e: NullPointerException) {
        }
    }

    fun newFolder(newFolder: String) {
        try {
            val lastPath = instance.lastPath
            val folder = File("$lastPath/$newFolder")
            val activity = activity
            if (activity != null) {
                if (folder.exists()) {
                    error(activity, R.string.toast_error)
                } else {
                    val ret = folder.mkdirs()
                    success(activity, R.string.toast_new_folder)
                }
            }

            // 파일 리스트 리프레시를 요청해야함
            onRefresh()
        } catch (e: NullPointerException) {
        }
    }

    fun renameFileWithDialog(item: ExplorerItem) {
        val activity = activity ?: return
        val binding: DialogSingleBinding = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_single, null, false)
        binding.fileName.setText(item.name)
        showAlert(activity,
            appName,
            getString(R.string.msg_file_rename),
            view, { dialog, which ->
                val newName = binding.fileName.text.toString()
                renameFile(item, newName)
            }, null, null)
    }

    // 파일이름을 변경하고 파일리스트에서 정보를 변경해야 한다.
    fun renameFile(item: ExplorerItem, newName: String) {
        try {
            val newPath = getParentPath(item.path) + "/" + newName

            // 파일이름을 변경
            val ret = File(item.path).renameTo(File(newPath))

            // 성공시 선택 해제 및 파일 정보 변경
            item.selected = false
            item.name = newName
            item.path = newPath

            // 정보 변경 반영
            adapter!!.notifyItemChanged(item.position)

            // 파일 선택 갯수 초기화
            updateSelectedFileCount()
            success(activity, R.string.toast_file_rename)
        } catch (e: Exception) {
            error(activity, R.string.toast_error)
        }
    }

    fun onAdapterItemClick(position: Int) {
        synchronized(this) {

            //TODO: 여기서 IndexOutOfBoundsException 발생함. 동기화 문제.
            if (fileList.size <= position) // 포지션이 이상하면 return
                return
            val item = fileList[position] ?: return
            if (mode == MODE_SELECT) {
                onSelectItemClick(item, position)
            } else {
                onRunItemClick(item)
            }
        }
    }

    //TODO: 나중에 구현
    fun backupPosition() {
//        PositionManager.setPosition(LocalExplorer.getLastPath(), currentView.getFirstVisiblePosition());
//        PositionManager.setTop(LocalExplorer.getLastPath(), getCurrentViewScrollTop());
    }

    private val currentViewScrollTop: Int
        get() = if (currentView.childCount > 0) {
            currentView.getChildAt(0).top
        } else 0

    fun moveToSelection(path: String?) {
        val position = getPosition(path!!)
        val top = getTop(path)
        currentView.clearFocus()
        currentView.post {
            currentView.requestFocusFromTouch()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // only for gingerbread and newer versions
                currentView.scrollToPosition(position)
            } else {
                currentView.scrollToPosition(position)
            }
            currentView.clearFocus()
        }
    }

    override fun onItemClick(position: Int) {
        if (canClick) {
            onAdapterItemClick(position)
        }
    }

    override fun onItemLongClick(position: Int) {
        if (canClick) {
            onAdapterItemLongClick(position)
        }
    }

    fun updateFileList(path: String, isPathChanged: Boolean) {
        // 선택모드인지 설정해준다.
        adapter.mode = mode

        // 썸네일이 꽉찼을때는 비워준다.
        if (getThumbnailCount() > MAX_THUMBNAILS) {
            removeAllThumbnails()
        }

        //FIX:
        //LocalSearchTask task = new LocalSearchTask(isPathChanged(path));
        //task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);
        localSearchTask?.cancel(true)

        // 최초 로딩시에만 적용됨
        var disableUpdateCanClick = false
        if (adapter.itemCount > 0) { // 이미 DB에서 데이터를 로딩했으므로 canClick을 업데이트 하지 않
            disableUpdateCanClick = true
        }
        localSearchTask = LocalSearchTask(this, isPathChanged, disableUpdateCanClick)
        localSearchTask?.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path)

        // 패스 UI를 가장 오른쪽으로 스크롤
        // 이동이 완료되기전에 이미 이동한다.
        binding.scrollPath.post { binding.scrollPath.fullScroll(View.FOCUS_RIGHT) }

        // preference는 쓰레드로 사용하지 않기로 함
        // 현재 패스를 저장
        Timber.e("updateFileList set begin path=$path cloud=$cloud")
        lastPath = path
        lastCloud = cloud
        Timber.e("updateFileList set end")

        // 외장 패스인지 체크하여
        var isExtSdCard = false
        if (extSdCard != null && path!!.startsWith(extSdCard!!)) isExtSdCard = true
        if (isExtSdCard) {
            binding.storageIndicator.setTargetView(binding.btnSdcard)
        } else {
            binding.storageIndicator.setTargetView(binding.btnHome)
        }

        // 오래 걸림. 이것도 쓰레드로...
        Thread { requestThumbnailScan() }.start()
    }

    //TODO: 차후에 pull to refresh로 새로고침 해줘야 함
    //HACK: 모두 전체 새로 읽기로 수정함
    fun updateFileList(path: String) {
        //updateFileList(path, isPathChanged(path));
        if (cloud == CLOUD_LOCAL) {
            if (path.isEmpty())
                updateFileList("", true)
            else
                updateFileList(path, isPathChanged(path))
        } else if (cloud == CLOUD_DROPBOX) {
            updateDropboxList(path)
        } else if (cloud == CLOUD_GOOGLEDRIVE) {
            updateGoogleDriveList(path)
        }
    }

    fun updateDropboxList(path: String) {
        // 선택모드인지 설정해준다.
        adapter.mode = mode

        // 썸네일이 꽉찼을때는 비워준다.
        if (getThumbnailCount() > MAX_THUMBNAILS) {
            removeAllThumbnails()
        }

        //FIX:
        //LocalSearchTask task = new LocalSearchTask(isPathChanged(path));
        //task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);
        dropboxSearchTask?.cancel(true)

        dropboxSearchTask = DropboxSearchTask(this)
        dropboxSearchTask?.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path)

        // 패스 UI를 가장 오른쪽으로 스크롤
        // 이동이 완료되기전에 이미 이동한다.
        binding.scrollPath.post { binding.scrollPath.fullScroll(View.FOCUS_RIGHT) }

        // preference는 쓰레드로 사용하지 않기로 함
        // 현재 패스를 저장
        lastPath = path
        lastCloud = cloud
        binding.storageIndicator.setTargetView(binding.btnDropbox)
    }

    fun updateGoogleDriveList(path: String) {
        // 선택모드인지 설정해준다.
        adapter.mode = mode

        // 썸네일이 꽉찼을때는 비워준다.
        if (getThumbnailCount() > MAX_THUMBNAILS) {
            removeAllThumbnails()
        }

        //FIX:
        //LocalSearchTask task = new LocalSearchTask(isPathChanged(path));
        //task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);
        googleDriveSearchTask?.cancel(true)
        googleDriveSearchTask = GoogleDriveSearchTask(this)
        googleDriveSearchTask?.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path)

        // 패스 UI를 가장 오른쪽으로 스크롤
        // 이동이 완료되기전에 이미 이동한다.
        binding.scrollPath.post { binding.scrollPath.fullScroll(View.FOCUS_RIGHT) }

        // preference는 쓰레드로 사용하지 않기로 함
        // 현재 패스를 저장
        lastPath = path
        lastCloud = cloud
        binding.storageIndicator.setTargetView(binding.btnGdrive)
    }

    private fun isPathChanged(path: String): Boolean {
        val currentPath = lastPath
        return currentPath != path
    }

    private fun softRefresh() {
        adapter.mode = mode
        adapter.notifyDataSetChanged()
    }

    override fun onRefresh() {
        Timber.e("onRefresh begin")
        // 외부 resume시에 들어올수도 있으므로 pref에서 읽는다.
        val lastCloud = lastCloud
        val lastPath = lastPath
        Timber.e("onRefresh end")
        cloud = lastCloud
        updateFileList(lastPath)
    }

    override fun onBackPressed() {
        // Drawer가 열려 있으면 닫아야 함
        val activity = activity as BaseMainActivity?
        if (activity != null) {
            if (activity.isDrawerOpened) {
                activity.closeDrawer()
                return
            }
        }

        // 선택모드이면 선택모드를 취소하는 방향으로
        // 둘다 normal mode로 돌아간다.
        if (mode != MODE_NORMAL) {
            onNormalMode()
        } else {
            try {
                val lastPath = instance.lastPath
                if (instance.isInitialPath(lastPath)) { // user root일 경우
                    super.onBackPressed()
                } else if (extSdCard != null && extSdCard == lastPath) { // sd카드 root일 경우
                    super.onBackPressed()
                } else {
                    gotoUpDirectory()
                }
            } catch (e: NullPointerException) {
            }
        }
    }

    private fun exitPlayerMode() {
        if (mode == MODE_PLAYER) {
            (activity as BaseMainActivity?)!!.hidePlayerUI()
        }
    }

    private fun onSelectMode(item: ExplorerItem, position: Int) {
        exitPlayerMode()
        mode = MODE_SELECT

        // UI 상태만 리프레시
        // 왜냐하면 전체 체크박스를 나오게 해야 하기 때문이다.
        softRefresh()
        onSelectItemClick(item, position)

        // 하단 UI 표시
        val activity = activity ?: return
        (activity as BaseMainActivity).showBottomUI()
    }

    private fun onPasteMode() {
        exitPlayerMode()
        mode = MODE_PASTE

        // 다시 리프레시를 해야지 체크박스를 새로 그린다.
        softRefresh()
    }

    fun onNormalMode() {
        exitPlayerMode()
        mode = MODE_NORMAL

        // 다시 리프레시를 해야지 체크박스를 새로 그린다.
        softRefresh()

        // 하단 UI 숨김
        val activity = activity ?: return
        (activity as BaseMainActivity).hideBottomUI()
    }

    // 이건 뭐지?
    // 아이템이 선택될때마다 선택된 아이템의 갯수를 업데이트하기위해서 여기에다가 모음
    private fun onSelectItemClick(item: ExplorerItem, position: Int) {
        item.selected = !item.selected

        // 아이템을 찾아서 UI를 업데이트 해주어야 함
        adapter.notifyItemChanged(position)

        // 선택된 파일 카운트 업데이트
        updateSelectedFileCount()
    }

    private fun updateSelectedFileCount() {
        val count = selectedFileCount
        val activity = activity ?: return
        (activity as BaseMainActivity).updateSelectedFileCount(count)
    }

    private fun onRunItemClick(item: ExplorerItem) {
        when (item.type) {
            ExplorerItem.FILETYPE_FOLDER -> {
                onNormalMode()
                onClickDirectory(item)
            }
            ExplorerItem.FILETYPE_IMAGE -> {
                onNormalMode()
                onClickImage(item)
            }
            ExplorerItem.FILETYPE_APK -> {
                onNormalMode()
                onClickApk(item)
            }
            ExplorerItem.FILETYPE_VIDEO -> {
                onNormalMode()
                onClickVideo(item)
            }
            ExplorerItem.FILETYPE_AUDIO ->                 //onNormalMode()을 예외적으로 적용하지 않는다.
                onClickAudio(item)
            ExplorerItem.FILETYPE_PDF, ExplorerItem.FILETYPE_TEXT -> {
                //TODO: 나중에 읽던 책의 현재위치 이미지의 preview를 만들자.
                onNormalMode()
                onClickBook(item)
            }
            ExplorerItem.FILETYPE_ZIP -> {
                onNormalMode()
                if (isComicz) {
                    onClickBook(item)
                } else {
                    // 파일 탐색기에서는 ZIP파일 관리를 해주자.
                    onClickZip(item)
                }
            }
        }
    }

    fun deleteFileWithDialog() {
        val activity = activity ?: return
        val count = selectedFileCount
        // 파일을 삭제할건지 경고
        showAlert(activity,
            appName, String.format(getString(R.string.warn_file_delete), count),
            null,
            DialogInterface.OnClickListener { dialog, which ->
                val activity = getActivity() ?: return@OnClickListener
                val task = DeleteTask(activity)
                task.setFileList(fileList)
                task.setOnDismissListener { //                                Timber.e("onDismiss");
                    onRefresh()
                }
                task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
            }, { dialog, which -> }, null
        )
    }

    fun selectAll() {
        // 전체가 선택된 상태라면 전부 선택 초기화를 해줌
        synchronized(this) {
            if (fileList.size == selectedFileCount) {
                for (i in fileList.indices) {
                    fileList[i].selected = false
                }
                info(activity, R.string.toast_deselect_all)
            } else {
                for (i in fileList.indices) {
                    fileList[i].selected = true
                }
                info(activity, R.string.toast_select_all)
            }
        }
        updateSelectedFileCount()

        // 그런다음에 화면 UI를 업데이트를 해준다.
        softRefresh()
    }

    fun captureSelectedFile(cut: Boolean) {
        // Toast 메세지를 표시
        // 선택된 파일을 목록을 작성
        selectedFileList = ArrayList()
        synchronized(this) {
            for (i in fileList.indices) {
                val item = fileList[i]
                if (item.selected) {
                    selectedFileList.add(item)
                }
            }
        }
        this.cut = cut
        capturePath = instance.lastPath.toString()

        // 붙이기 모드로 바꿈
        onPasteMode()
        val activity = activity
        if (activity != null) {
            (activity as BaseMainActivity).updatePasteMode()
        }
    }

    fun warnMoveToSameLocation() {
        // 이동 불가
        val activity = activity ?: return
        showAlert(activity,
            appName,
            getString(R.string.warn_move_same_folder),
            null,
            DialogInterface.OnClickListener { dialogInterface, i ->
                // 동일한 위치이므로 그냥 종료 함
            },
            null,
            null
        )
    }

    fun runPasteTask(pastePath: String) {
        val activity = activity ?: return
        val task = PasteTask(activity)
        task.setIsCut(cut) // cut or copy
        task.setFileList(selectedFileList)
        task.setPath(capturePath, pastePath)
        task.setOnDismissListener { // 현재 파일 리스트를 업데이트하고, 일반모드로 돌아가야 함
            onRefresh()
            onNormalMode()
        }
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun pasteFileWithDialog() {
        try {
            val pastePath = instance.lastPath.toString()

            // 복사될 폴더와 이동할 폴더가 같다면
            if (capturePath == pastePath) {
                if (cut) {
                    warnMoveToSameLocation()
                } else {
                    val activity = activity ?: return

                    // 사본 생성
                    showAlert(activity,
                        appName,
                        getString(R.string.warn_copy_same_folder),
                        null,
                        { dialogInterface, i -> runPasteTask(pastePath) },
                        null,
                        null
                    )
                }
            } else {
                runPasteTask(pastePath)
            }
        } catch (e: NullPointerException) {
        }
    }

    fun runZipTask(name: String, ext: String) {
        // 선택된 파일만 압축할 리스트에 추가해 줌
        val zipList = ArrayList<ExplorerItem>()
        synchronized(this) {
            for (i in fileList.indices) {
                if (fileList[i].selected) {
                    zipList.add(fileList[i])
                }
            }
        }
        val activity = activity ?: return
        try {
            val path = instance.lastPath
            val zipPath = "$path/$name$ext"
            val task = ZipTask(activity)
            task.setPath(path)
            task.setPath(zipPath)
            task.setFileList(zipList)
            task.setOnDismissListener {
                onRefresh()
                onNormalMode()
            }
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        } catch (e: NullPointerException) {
        }
    }

    fun zipFileWithDialog() {
        val activity = activity ?: return
        try {
            val path = instance.lastPath
            val binding: DialogZipBinding = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_zip, null, false)

            binding.zipType.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View,
                    i: Int,
                    l: Long
                ) {
                    // zip파일의 이름을 현재 패스 기준으로 함
                    val base = path!!.substring(path.lastIndexOf("/") + 1)
                    val ext = binding.zipType.selectedItem.toString()
                    val lastPath = instance.lastPath
                    val newPath = getNewFileName("$lastPath/$base$ext")
                    var newName = newPath.replace("$lastPath/", "")
                    newName = newName.replace(ext, "")

                    // 새로나온 폴더의 이름을 edit에 반영함
                    binding.fileName.setText(newName)
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            }
            showAlert(activity,
                appName,
                getString(R.string.msg_file_zip),
                view,
                { dialogInterface, i ->
                    val name = binding.fileName.text.toString()
                    val ext = binding.zipType.selectedItem.toString()
                    runZipTask(name, ext)
                }, null, null
            )
        } catch (e: NullPointerException) {
        }
    }

    companion object {
        const val SWITCH_LIST = 0
        const val SWITCH_GRID = 1
        const val SWITCH_NARROW = 2
        const val MODE_NORMAL = 0
        const val MODE_SELECT = 1
        const val MODE_PASTE = 2
        const val MODE_PLAYER = 3
    }
}