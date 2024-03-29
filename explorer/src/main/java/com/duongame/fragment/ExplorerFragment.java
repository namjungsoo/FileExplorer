package com.duongame.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.duongame.BuildConfig;
import com.duongame.MainApplication;
import com.duongame.R;
import com.duongame.activity.main.BaseMainActivity;
import com.duongame.activity.viewer.PhotoActivity;
import com.duongame.activity.viewer.VideoActivity;
import com.duongame.adapter.ExplorerAdapter;
import com.duongame.adapter.ExplorerGridAdapter;
import com.duongame.adapter.ExplorerItem;
import com.duongame.adapter.ExplorerListAdapter;
import com.duongame.adapter.ExplorerNarrowAdapter;
import com.duongame.adapter.ExplorerScrollListener;
import com.duongame.bitmap.BitmapCacheManager;
import com.duongame.db.BookLoader;
import com.duongame.db.ExplorerItemDB;
import com.duongame.dialog.SortDialog;
import com.duongame.file.FileHelper;
import com.duongame.helper.AlertHelper;
import com.duongame.helper.AppHelper;
import com.duongame.helper.ExtSdCardHelper;
import com.duongame.helper.PreferenceHelper;
import com.duongame.helper.ToastHelper;
import com.duongame.manager.AdBannerManager;
import com.duongame.manager.PermissionManager;
import com.duongame.manager.PositionManager;
import com.duongame.task.download.CloudDownloadTask;
import com.duongame.task.download.DropboxDownloadTask;
import com.duongame.task.download.GoogleDriveDownloadTask;
import com.duongame.task.file.DeleteTask;
import com.duongame.task.file.DropboxSearchTask;
import com.duongame.task.file.GoogleDriveSearchTask;
import com.duongame.task.file.LocalSearchTask;
import com.duongame.task.file.PasteTask;
import com.duongame.task.zip.UnzipTask;
import com.duongame.task.zip.ZipTask;
import com.duongame.view.DividerItemDecoration;
import com.duongame.view.Indicator;

import java.io.File;
import java.util.ArrayList;

import timber.log.Timber;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static com.duongame.ExplorerConfig.MAX_THUMBNAILS;

/**
 * Created by namjungsoo on 2016-11-23.
 */

public class ExplorerFragment extends BaseFragment implements ExplorerAdapter.OnItemClickListener, ExplorerAdapter.OnItemLongClickListener {
    private final static String TAG = "ExplorerFragment";
    private final static boolean DEBUG = false;

    public final static int SWITCH_LIST = 0;
    public final static int SWITCH_GRID = 1;
    public final static int SWITCH_NARROW = 2;

    public final static int MODE_NORMAL = 0;
    public final static int MODE_SELECT = 1;
    public final static int MODE_PASTE = 2;
    public final static int MODE_PLAYER = 3;

    // Android UI 관련
    // 패스 관련
    private TextView textPath;
    private HorizontalScrollView scrollPath;

    // 컨텐츠 관련
    private RecyclerView currentView;
    private View rootView;

    // 뷰 스위처
    private ViewSwitcher switcherViewType;
    private ViewSwitcher switcherContents;
    private Button permissionButton;
    private TextView textNoFiles;

    // 기타
    private ImageButton home = null;
    private ImageButton up = null;

    private ImageButton sdcard = null;
    private String extSdCard = null;
    private ImageButton dropbox = null;
    private ImageButton googleDrive = null;
    private DividerItemDecoration itemDecoration = null;

    private Indicator storageIndicator = null;

    // Model 관련
    // 파일 관련
    private ExplorerAdapter adapter;
    private ArrayList<ExplorerItem> fileList;

    // 붙이기 관련
    private ArrayList<ExplorerItem> selectedFileList;
    private boolean cut;

    private LocalSearchTask localSearchTask = null;
    private DropboxSearchTask dropboxSearchTask = null;
    private GoogleDriveSearchTask googleDriveSearchTask = null;

    // 선택
//    private boolean selectMode = false;
//    private boolean pasteMode = false;// 붙여넣기 모드는 뒤로가기 버튼이 있고
    private int mode = MODE_NORMAL;

    private String capturePath;

    // 정렬
    private int sortType;
    private int sortDirection;

    private boolean canClick = false;
    private int viewType = SWITCH_LIST;

    private boolean backupDropbox = false;
    private boolean backupGoogleDrive = false;

    public ViewSwitcher getSwitcherContents() {
        return switcherContents;
    }

    public Button getPermissionButton() {
        return permissionButton;
    }

    public ArrayList<ExplorerItem> getFileList() {
        return fileList;
    }

    public TextView getTextPath() {
        return textPath;
    }

    public TextView getTextNoFiles() {
        return textNoFiles;
    }

    public ExplorerAdapter getAdapter() {
        return adapter;
    }

    public void setFileList(ArrayList<ExplorerItem> fileList) {
        this.fileList = fileList;
    }

    private Handler handler;
    protected boolean playerMode = false;

    public boolean isCanClick() {
        return canClick;
    }

    public void setCanClick(boolean canClick) {
        this.canClick = canClick;

        BaseMainActivity activity = (BaseMainActivity) getActivity();
        if (activity != null) {
            ProgressBar progressBar = activity.getProgressBarLoading();
            if (progressBar != null) {
                progressBar.setVisibility(canClick ? View.GONE : View.VISIBLE);
            }
        }
    }

    public int getSortType() {
        return sortType;
    }

    public int getSortDirection() {
        return sortDirection;
    }

    private void loadFileListFromLocalDB() {
        handler = new Handler();
        // 현재 파일 리스트를 얻어서 바로 셋팅
        Timber.e("ExplorerItemDB begin");

        setCanClick(false);
        new Thread(() -> {
            Timber.e("ExplorerItemDB thread");
            ArrayList<ExplorerItem> fileList = (ArrayList<ExplorerItem>) ExplorerItemDB.Companion.getInstance(getContext()).getDb().explorerItemDao().getItems();
            ArrayList<ExplorerItem> imageList = FileHelper.getImageFileList(fileList);
            ArrayList<ExplorerItem> videoList = FileHelper.getVideoFileList(fileList);
            ArrayList<ExplorerItem> audioList = FileHelper.getAudioFileList(fileList);

            MainApplication app = MainApplication.getInstance(getActivity());
            if (app != null) {
                app.setFileList(fileList);
                app.setImageList(imageList);
                app.setVideoList(videoList);
                app.setAudioList(audioList);
            }

            // DB에 저장된게 있으면 adapter에 적용
            if (fileList.size() > 0) {
                adapter.setFileList(fileList);

                handler.postAtFrontOfQueue(() -> {
                    adapter.notifyDataSetChanged();

                    // 이제 클릭할수 있음
                    // 프로그레스바 안보이기
                    setCanClick(true);
                    Timber.e("ExplorerItemDB end");
                });
            }
        }).start();

    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_explorer, container, false);

        initUI();
        Timber.e("initUI end");

        initViewType();
        Timber.e("initViewType end");

        loadFileListFromLocalDB();

        FragmentActivity activity = getActivity();
        if (activity != null) {
//            PermissionManager.checkStoragePermissions(activity);
            sortType = PreferenceHelper.getSortType(activity);
            sortDirection = PreferenceHelper.getSortDirection(activity);
        }

        extSdCard = ExtSdCardHelper.getExternalSdCardPath();
        if (extSdCard != null) {
            if (sdcard != null) {
                sdcard.setVisibility(View.VISIBLE);
            }
        }

        String path = PreferenceHelper.getLastPath(getContext());
        try {
            MainApplication.getInstance(activity).setLastPath(path);
        } catch (NullPointerException e) {

        }
        Timber.e("onCreateView end");
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        BaseMainActivity activity = (BaseMainActivity) getActivity();
        if (activity != null) {
            if (!activity.getShowReview()) {
                if (AppHelper.isComicz(getContext())) {
                    BookLoader.openLastBook(activity);
                }
            }
        }
        Timber.e("onActivityCreated end");
    }

    @Override
    public void onResume() {
        super.onResume();

        // 밖에 나갔다 들어오면 리프레시함
        onRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();

        //TODO: 스크롤 위치 복구해줘야함
        final int position = 0;
        final int top = getCurrentViewScrollTop();

        new Thread(() -> {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                PreferenceHelper.setLastPosition(activity, position);
                PreferenceHelper.setLastTop(activity, top);
            }
        }).start();
    }

    public boolean isPasteMode() {
        return mode == MODE_PASTE;
    }

    void initUI() {
        switcherContents = rootView.findViewById(R.id.switcher_contents);
        switcherViewType = rootView.findViewById(R.id.switcher);
        textPath = rootView.findViewById(R.id.text_path);
        scrollPath = rootView.findViewById(R.id.scroll_path);
        fileList = new ArrayList<>();

        home = rootView.findViewById(R.id.btn_home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cloud = CLOUD_LOCAL;
                try {
                    updateFileList(MainApplication.getInstance(ExplorerFragment.this.getActivity()).getInitialPath());
                } catch (NullPointerException e) {
                }
            }
        });

        up = rootView.findViewById(R.id.btn_up);
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // up으로 갈수있는 조건은 normal, paste 모드이다.
                // 나머지는 normal로 모드를 변경한다.
                if (mode == MODE_NORMAL || mode == MODE_PASTE)
                    gotoUpDirectory();
                else
                    onNormalMode();
            }
        });

        sdcard = rootView.findViewById(R.id.btn_sdcard);
        sdcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (extSdCard != null) {
                    cloud = CLOUD_LOCAL;
                    updateFileList(extSdCard);
                }
            }
        });

        storageIndicator = rootView.findViewById(R.id.storage_indicator);

        dropbox = rootView.findViewById(R.id.btn_dropbox);
        dropbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cloud = CLOUD_DROPBOX;
                updateFileList("/");
            }
        });
        dropbox.setVisibility(backupDropbox ? View.VISIBLE : View.GONE);

        googleDrive = rootView.findViewById(R.id.btn_gdrive);
        googleDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cloud = CLOUD_GOOGLEDRIVE;
                updateFileList("/");
            }
        });
        googleDrive.setVisibility(backupGoogleDrive ? View.VISIBLE : View.GONE);

        storageIndicator.refresh();

        textNoFiles = rootView.findViewById(R.id.text_no_files);
        permissionButton = rootView.findViewById(R.id.btn_permission);
        if (permissionButton != null) {
            permissionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PermissionManager.checkStoragePermissions(getActivity());
                }
            });
        }
    }

    public void updateDropboxUI(boolean show) {
        Timber.e("updateDropboxUI " + show);
        if (dropbox == null) {
            backupDropbox = show;
            return;
        }

        if (show) {
            dropbox.setVisibility(View.VISIBLE);
        } else {
            // 로그아웃인 상황이니 최초로 간다.
            cloud = CLOUD_LOCAL;

            if (dropbox.getVisibility() == View.VISIBLE) {
                try {
                    updateFileList(MainApplication.getInstance(getActivity()).getInitialPath());
                } catch (NullPointerException e) {

                }
                dropbox.setVisibility(View.GONE);
            }
        }

        storageIndicator.refresh();
    }

    public void updateGoogleDriveUI(boolean show) {
        Timber.e("updateGoogleDriveUI " + show);
        if (googleDrive == null) {
            backupGoogleDrive = show;
            return;
        }

        if (show) {
            googleDrive.setVisibility(View.VISIBLE);
        } else {
            // 로그아웃인 상황이니 최초로 간다.
            cloud = CLOUD_LOCAL;

            if (googleDrive.getVisibility() == View.VISIBLE) {
                try {
                    updateFileList(MainApplication.getInstance(getActivity()).getInitialPath());
                } catch (NullPointerException e) {

                }
                googleDrive.setVisibility(View.GONE);
            }
        }

        storageIndicator.refresh();
    }

    void initViewType() {
        changeViewType(PreferenceHelper.getViewType(getActivity()));
    }

    public RecyclerView getCurrentView() {
        return currentView;
    }

    public void changeViewType(int viewType) {
        FragmentActivity activity = getActivity();
        if (activity == null)
            return;

        this.viewType = viewType;

        if (switcherViewType != null)
            switcherViewType.setDisplayedChild(viewType);

        switch (viewType) {
            case SWITCH_LIST:
                synchronized (this) {
                    adapter = new ExplorerListAdapter(activity, fileList);
                }
                currentView = rootView.findViewById(R.id.list_explorer);
                if (currentView != null) {
                    currentView.setLayoutManager(new LinearLayoutManager(activity));
                    if (itemDecoration == null) {
                        itemDecoration = new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST);
                        currentView.addItemDecoration(itemDecoration);
                    }
                }
                break;
            case SWITCH_GRID:
                synchronized (this) {
                    adapter = new ExplorerGridAdapter(activity, fileList);
                }
                currentView = rootView.findViewById(R.id.grid_explorer);
                if (currentView != null) {
                    currentView.setLayoutManager(new GridLayoutManager(activity, 4));
                }
                break;

            case SWITCH_NARROW:
                synchronized (this) {
                    adapter = new ExplorerNarrowAdapter(activity, fileList);
                }
                currentView = rootView.findViewById(R.id.list_explorer);
                if (currentView != null) {
                    currentView.setLayoutManager(new LinearLayoutManager(activity));
                    if (itemDecoration == null) {
                        itemDecoration = new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST);
                        currentView.addItemDecoration(itemDecoration);
                    }
                }
                break;
        }

        if (adapter != null) {
            adapter.setMode(mode);
            adapter.setOnItemClickListener(this);
            adapter.setOnLongItemClickListener(this);
        }

        if (currentView != null) {
            currentView.setAdapter(adapter);
            currentView.addOnScrollListener(new ExplorerScrollListener());
        }

        PreferenceHelper.setViewType(getContext(), viewType);
    }

    // 새로운 파일이 추가 되었을때 스캔을 하라는 의미이다.
    // 쓰레드에서 동작하므로 여러가지 문제점이 생겼다.
    // 일단은 imageList를 복사하고 나서 작업을 시작하자.
    // 중요하지 않은 작업이므로 전체 try-catch를 건다.
    @SuppressWarnings("unchecked")
    void requestThumbnailScan() {
        try {
            if (fileResult == null)
                return;

            ArrayList<ExplorerItem> imageList = fileResult.imageList;
            if (imageList == null)
                return;

            FragmentActivity activity = getActivity();
            for (ExplorerItem item : imageList) {
                if (activity == null)
                    break;
                if (item == null || item.path == null)
                    continue;
                activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + item.path)));
            }
        } catch (Exception e) {

        }
    }

    public void gotoUpDirectory() {
        try {
            String path = MainApplication.getInstance(getActivity()).getLastPath();
            path = path.substring(0, path.lastIndexOf('/'));
            if (path.length() == 0) {
                path = "/";
            }

            backupPosition();
            updateFileList(path);
        } catch (NullPointerException e) {

        }
    }

    void onClickDirectory(ExplorerItem item) {
        try {
            String newPath;
            String lastPath = MainApplication.getInstance(getActivity()).getLastPath();
            if (lastPath.equals("/")) {
                newPath = lastPath + item.name;
            } else {
                newPath = lastPath + "/" + item.name;
            }
            updateFileList(newPath);
        } catch (NullPointerException e) {

        }

    }

    void onClickImage(ExplorerItem item) {
        final Intent intent = PhotoActivity.getLocalIntent(getContext(), item);
        startActivity(intent);
    }

    void onClickVideo(ExplorerItem item) {
        final Intent intent = new Intent(getContext(), VideoActivity.class);
        intent.putExtra("item", item);
        startActivity(intent);
    }

    void onClickAudio(ExplorerItem item) {
        // 현재 화면에서 오디오 플레이를 한다
        // 오디오 리스트를 받아서 리스트에 넣고
        // 플레이를 한다
        BaseMainActivity activity = (BaseMainActivity) getActivity();
        if (activity == null)
            return;

        if (mode != MODE_PLAYER) {
            activity.showPlayerUI();
            mode = MODE_PLAYER;
        }

        ArrayList<ExplorerItem> audioList = MainApplication.getInstance(activity).getAudioList();
        for (int i = 0; i < audioList.size(); i++) {
            if (audioList.get(i).path.equals(item.path)) {
                activity.playAudio(i);
                break;
            }
        }
    }

    void onClickApk(ExplorerItem item) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            FragmentActivity activity = getActivity();
            if (activity == null)
                return;

            //TODO: 외부 SdCard에 있는 설치폴더는 설치가 될것인가?
            // 일단은 exception은 처리하자.
            try {
                final String providerName = activity.getPackageName() + ".provider";
                final Uri apkUri = FileProvider.getUriForFile(activity, providerName, new File(item.path));
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } catch (Exception e) {
                ToastHelper.error(activity, R.string.toast_error);
            }
        } else {
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + item.path), "application/vnd.android.package-archive");
            startActivity(intent);
        }
    }

    boolean checkDownloadOverwrite(final Activity activity, final ExplorerItem item, final CloudDownloadTask task) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(path, item.name);

        // 파일이 있으면 팝업을 통해서 확인해야 함
        if (file.exists()) {
            final String title = AppHelper.getAppName(activity);
            final String content = getString(R.string.msg_overwrite);
            final DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
                @SuppressLint("StringFormatInvalid")
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // 확인을 눌렀으므로 다운로드하여 덮어씌움
                    task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, item);// metadata = fileId
                    ToastHelper.showToast(activity, String.format(getResources().getString(R.string.toast_cloud_download), item.name));

                    dialogInterface.dismiss();
                }
            };
            final DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            };

            if (BuildConfig.SHOW_AD) {
                AlertHelper.showAlertWithAd(activity,
                        title,
                        content,
                        //positive
                        positiveListener,
                        //negative
                        negativeListener,
                        null);
                AdBannerManager.initPopupAd(activity);// 항상 초기화 해주어야 함
            } else {
                AlertHelper.showAlert(activity,
                        title,
                        content,
                        null,
                        //positive
                        positiveListener,
                        //negative
                        negativeListener,
                        null);
            }
            return true;
        } else {
            return false;
        }
    }

    void downloadAndOpen(final Activity activity, final ExplorerItem item, final CloudDownloadTask task) {
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, item);
        ToastHelper.showToast(activity, String.format(getResources().getString(R.string.toast_cloud_download), item.name));
    }

    void onClickBookDropbox(final Activity activity, final ExplorerItem item) {
        // 다운로드를 받은후에 로딩함
        DropboxDownloadTask task = new DropboxDownloadTask(activity, new CloudDownloadTask.Callback() {
            // task 내부에서 toast를 처리해주므로 주석처리함
            @Override
            public void onDownloadComplete(File result) {
                item.path = result.getAbsolutePath();
                BookLoader.load(activity, item, false);
            }

            @Override
            public void onError(Exception e) {
            }
        });

        if (!checkDownloadOverwrite(activity, item, task)) {// overwrite하지 않으면 바로 다운로드
            downloadAndOpen(activity, item, task);
        }
    }

    void onClickBookGoogleDrive(final Activity activity, final ExplorerItem item) {
        // 다운로드를 받은후에 로딩함
        GoogleDriveDownloadTask task = new GoogleDriveDownloadTask(activity, new CloudDownloadTask.Callback() {
            // task 내부에서 toast를 처리해주므로 주석처리함
            @Override
            public void onDownloadComplete(File result) {
                item.path = result.getAbsolutePath();
                BookLoader.load(activity, item, false);
            }

            @Override
            public void onError(Exception e) {
            }
        });

        if (!checkDownloadOverwrite(activity, item, task)) {// overwrite하지 않으면 바로 다운로드
            downloadAndOpen(activity, item, task);
        }
    }

    void onClickBook(final ExplorerItem item) {
        final FragmentActivity activity = getActivity();
        if (activity == null)
            return;

        if (cloud == CLOUD_LOCAL) {
            BookLoader.load(activity, item, false);
        } else if (cloud == CLOUD_DROPBOX) {
            onClickBookDropbox(activity, item);
        } else if (cloud == CLOUD_GOOGLEDRIVE) {
            onClickBookGoogleDrive(activity, item);
        }
    }

    void onClickZip(ExplorerItem item) {
        unzipWithDialog(item);
    }

    void runUnzipTask(ExplorerItem item, String name) {
        try {
            String path = MainApplication.getInstance(getActivity()).getLastPath();
            String targetPath;
            if (name == null) {
                targetPath = path;
            } else {
                targetPath = path + "/" + name;
            }

            FragmentActivity activity = getActivity();
            if (activity == null)
                return;

            UnzipTask task = new UnzipTask(activity);
            task.setPath(targetPath);

            // 여러 파일을 동시에 풀수있도록 함
            // 현재는 1개만 풀수 있음
            ArrayList<ExplorerItem> zipList = new ArrayList<>();
            zipList.add(item);

            task.setFileList(zipList);
            task.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    onRefresh();
                }
            });

            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (NullPointerException e) {

        }
    }

    // 폴더가 필요한 경우(gz,bz2가 아닌 경우)
    boolean isNeedFolder(String path) {
        boolean needFolder = true;
        if (path.endsWith(".gz")) {
            String checkTar = path.replace(".gz", "");
            if (!checkTar.endsWith(".tar"))
                needFolder = false;
        } else if (path.endsWith(".bz2")) {
            String checkTar = path.replace(".bz2", "");
            if (!checkTar.endsWith(".tar"))
                needFolder = false;
        }
        return needFolder;
    }

    void unzipWithDialog(final ExplorerItem item) {
        boolean needFolder = isNeedFolder(item.path);

        if (!needFolder) {
            //TODO: 이거 이상한데...
            runUnzipTask(item, null);
            return;
        }

        FragmentActivity activity = getActivity();
        if (activity == null)
            return;

        View view = activity.getLayoutInflater().inflate(R.layout.dialog_single, null, false);
        final EditText editFileName = view.findViewById(R.id.file_name);

        // zip파일의 이름을 기준으로 함
        String base = item.name.substring(0, item.name.lastIndexOf("."));
        base = FileHelper.getNameWithoutTar(base);

        try {
            // path/zipname 폴더가 있는지 확인
            final String lastPath = MainApplication.getInstance(getActivity()).getLastPath();
            final String newPath = FileHelper.getNewFileName(lastPath + "/" + base);
            String newName = newPath.replace(lastPath + "/", "");

            // 새로나온 폴더의 이름을 edit에 반영함
            editFileName.setText(newName);

            AlertHelper.showAlert(activity,
                    AppHelper.getAppName(activity),
                    getString(R.string.msg_file_unzip),
                    view,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            runUnzipTask(item, editFileName.getText().toString());
                        }
                    }, null, null);
        } catch (NullPointerException e) {

        }
    }

    void onAdapterItemLongClick(int position) {
//        Timber.e("onAdapterItemLongClick=" + position);
        synchronized (this) {
            if (fileList == null)
                return;

            try {// java.lang.IndexOutOfBoundsException
                ExplorerItem item = fileList.get(position);
                if (item == null)
                    return;

                // 이미 선택 모드라면 이름변경을 해줌
                if (mode == MODE_SELECT) {
                    //renameFileWithDialog
                    if (getSelectedFileCount() == 1) {
                        renameFileWithDialog(item);
                    } else {
                        ToastHelper.warning(getActivity(), R.string.toast_multi_rename_error);
                    }
                } else {// 선택 모드로 진입 + 현재 파일 선택
                    onSelectMode(item, position);
                }
            } catch (Exception ignored) {

            }
        }
    }

    int getSelectedFileCount() {
        int count = 0;
        synchronized (this) {
            if (fileList == null)
                return 0;

            for (int i = 0; i < fileList.size(); i++) {
                if (fileList.get(i).selected) {
                    count++;
                }
            }
        }
        return count;
    }

    public void sortFileWithDialog() {
        SortDialog dialog = new SortDialog();
        dialog.setTypeAndDirection(sortType, sortDirection);
        dialog.setOnSortListener(new SortDialog.OnSortListener() {
            @Override
            public void onSort(int type, int dir) {
                sortType = type;
                sortDirection = dir;

                FragmentActivity activity = getActivity();
                PreferenceHelper.setSortType(activity, sortType);
                PreferenceHelper.setSortDirection(activity, sortDirection);

                try {
                    updateFileList(MainApplication.getInstance(getActivity()).getLastPath());
                } catch (NullPointerException e) {

                }
            }
        });

        FragmentActivity activity = getActivity();
        if (activity != null) {
            dialog.show(activity.getFragmentManager(), "sort");
        }

    }

    public void newFolderWithDialog() {
        FragmentActivity activity = getActivity();
        if (activity == null)
            return;

        View view = activity.getLayoutInflater().inflate(R.layout.dialog_single, null, false);
        final EditText editFileName = view.findViewById(R.id.file_name);

        final String base = getString(R.string.new_folder);
        try {
            final String lastPath = MainApplication.getInstance(getActivity()).getLastPath();

            String newName = FileHelper.getNewFileName(lastPath + "/" + base);
            newName = newName.replace(lastPath + "/", "");

            editFileName.setText(newName);

            AlertHelper.showAlert(activity,
                    AppHelper.getAppName(activity),
                    getString(R.string.msg_new_folder),
                    view, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String newFolder = editFileName.getText().toString();
                            newFolder(newFolder);
                        }
                    }
                    , null, null);
        } catch (NullPointerException e) {

        }

    }

    void newFolder(String newFolder) {
        try {
            String lastPath = MainApplication.getInstance(getActivity()).getLastPath();
            File folder = new File(lastPath + "/" + newFolder);

            FragmentActivity activity = getActivity();
            if (activity != null) {
                if (folder.exists()) {
                    ToastHelper.error(activity, R.string.toast_error);
                } else {
                    boolean ret = folder.mkdirs();
                    ToastHelper.success(activity, R.string.toast_new_folder);
                }
            }

            // 파일 리스트 리프레시를 요청해야함
            onRefresh();
        } catch (NullPointerException e) {

        }
    }

    void renameFileWithDialog(final ExplorerItem item) {
        FragmentActivity activity = getActivity();
        if (activity == null)
            return;

        View view = activity.getLayoutInflater().inflate(R.layout.dialog_single, null, false);
        final EditText editFileName = view.findViewById(R.id.file_name);
        editFileName.setText(item.name);

        AlertHelper.showAlert(activity,
                AppHelper.getAppName(activity),
                getString(R.string.msg_file_rename),
                view, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = editFileName.getText().toString();
                        renameFile(item, newName);
                    }
                }
                , null, null);
    }

    // 파일이름을 변경하고 파일리스트에서 정보를 변경해야 한다.
    void renameFile(ExplorerItem item, String newName) {
        try {
            String newPath = FileHelper.getParentPath(item.path) + "/" + newName;

            // 파일이름을 변경
            boolean ret = new File(item.path).renameTo(new File(newPath));

            // 성공시 선택 해제 및 파일 정보 변경
            item.selected = false;
            item.name = newName;
            item.path = newPath;

            // 정보 변경 반영
            adapter.notifyItemChanged(item.position);

            // 파일 선택 갯수 초기화
            updateSelectedFileCount();

            ToastHelper.success(getActivity(), R.string.toast_file_rename);
        } catch (Exception e) {
            ToastHelper.error(getActivity(), R.string.toast_error);
        }
    }

    void onAdapterItemClick(int position) {
        synchronized (this) {
            if (fileList == null)
                return;

            //TODO: 여기서 IndexOutOfBoundsException 발생함. 동기화 문제.
            if (fileList.size() <= position)// 포지션이 이상하면 return
                return;

            ExplorerItem item = fileList.get(position);
            if (item == null)
                return;
            if (mode == MODE_SELECT) {
                onSelectItemClick(item, position);
            } else {
                onRunItemClick(item);
            }
        }
    }

    //TODO: 나중에 구현
    void backupPosition() {
//        PositionManager.setPosition(LocalExplorer.getLastPath(), currentView.getFirstVisiblePosition());
//        PositionManager.setTop(LocalExplorer.getLastPath(), getCurrentViewScrollTop());
    }

    int getCurrentViewScrollTop() {
        if (currentView.getChildCount() > 0) {
            return currentView.getChildAt(0).getTop();
        }
        return 0;
    }

    void moveToSelection(String path) {
        final int position = PositionManager.getPosition(path);
        final int top = PositionManager.getTop(path);

        currentView.clearFocus();

        currentView.post(new Runnable() {
            @Override
            public void run() {
                currentView.requestFocusFromTouch();

                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // only for gingerbread and newer versions
                    currentView.scrollToPosition(position);
                } else {
                    currentView.scrollToPosition(position);
                }

                currentView.clearFocus();
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        if (canClick) {
            onAdapterItemClick(position);
        }
    }

    @Override
    public void onItemLongClick(int position) {
        if (canClick) {
            onAdapterItemLongClick(position);
        }
    }

    public void updateFileList(final String path, boolean isPathChanged) {
        if (adapter == null) {
            return;
        }

        // 선택모드인지 설정해준다.
        adapter.setMode(mode);

        // 썸네일이 꽉찼을때는 비워준다.
        if (BitmapCacheManager.getThumbnailCount() > MAX_THUMBNAILS) {
            BitmapCacheManager.removeAllThumbnails();
        }

        //FIX:
        //LocalSearchTask task = new LocalSearchTask(isPathChanged(path));
        //task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);
        if (localSearchTask != null) {
            localSearchTask.cancel(true);
        }

        // 최초 로딩시에만 적용됨
        boolean disableUpdateCanClick = false;
        if (adapter.getItemCount() > 0) {// 이미 DB에서 데이터를 로딩했으므로 canClick을 업데이트 하지 않
            disableUpdateCanClick = true;
        }
        localSearchTask = new LocalSearchTask(this, isPathChanged, disableUpdateCanClick);
        localSearchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);

        // 패스 UI를 가장 오른쪽으로 스크롤
        // 이동이 완료되기전에 이미 이동한다.
        scrollPath.post(new Runnable() {
                            @Override
                            public void run() {
                                scrollPath.fullScroll(View.FOCUS_RIGHT);
                            }
                        }
        );

        // preference는 쓰레드로 사용하지 않기로 함
        // 현재 패스를 저장
        Timber.e("updateFileList set begin path=" + path + " cloud=" + cloud);
        PreferenceHelper.setLastPath(getActivity(), path);
        PreferenceHelper.setLastCloud(getActivity(), cloud);
        Timber.e("updateFileList set end");

        // 외장 패스인지 체크하여
        boolean isExtSdCard = false;
        if (extSdCard != null && path.startsWith(extSdCard))
            isExtSdCard = true;

        if (isExtSdCard) {
            storageIndicator.setTargetView(sdcard);
        } else {
            storageIndicator.setTargetView(home);
        }

        // 오래 걸림. 이것도 쓰레드로...
        new Thread(this::requestThumbnailScan).start();
    }

    //TODO: 차후에 pull to refresh로 새로고침 해줘야 함
    //HACK: 모두 전체 새로 읽기로 수정함
    public void updateFileList(final String path) {
        //updateFileList(path, isPathChanged(path));
        if (cloud == CLOUD_LOCAL) {
            if (path == null)
                updateFileList(null, true);
            else
                updateFileList(path, isPathChanged(path));
        } else if (cloud == CLOUD_DROPBOX) {
            updateDropboxList(path);
        } else if (cloud == CLOUD_GOOGLEDRIVE) {
            updateGoogleDriveList(path);
        }
    }

    void updateDropboxList(final String path) {
        if (adapter == null) {
            return;
        }

        // 선택모드인지 설정해준다.
        adapter.setMode(mode);

        // 썸네일이 꽉찼을때는 비워준다.
        if (BitmapCacheManager.getThumbnailCount() > MAX_THUMBNAILS) {
            BitmapCacheManager.removeAllThumbnails();
        }

        //FIX:
        //LocalSearchTask task = new LocalSearchTask(isPathChanged(path));
        //task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);
        if (dropboxSearchTask != null) {
            dropboxSearchTask.cancel(true);
        }
        dropboxSearchTask = new DropboxSearchTask(this);
        dropboxSearchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);

        // 패스 UI를 가장 오른쪽으로 스크롤
        // 이동이 완료되기전에 이미 이동한다.
        scrollPath.post(new Runnable() {
                            @Override
                            public void run() {
                                scrollPath.fullScroll(View.FOCUS_RIGHT);
                            }
                        }
        );

        // preference는 쓰레드로 사용하지 않기로 함
        // 현재 패스를 저장
        PreferenceHelper.setLastPath(getActivity(), path);
        PreferenceHelper.setLastCloud(getActivity(), cloud);

        storageIndicator.setTargetView(dropbox);
    }

    void updateGoogleDriveList(final String path) {
        if (adapter == null) {
            return;
        }

        // 선택모드인지 설정해준다.
        adapter.setMode(mode);

        // 썸네일이 꽉찼을때는 비워준다.
        if (BitmapCacheManager.getThumbnailCount() > MAX_THUMBNAILS) {
            BitmapCacheManager.removeAllThumbnails();
        }

        //FIX:
        //LocalSearchTask task = new LocalSearchTask(isPathChanged(path));
        //task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);
        if (googleDriveSearchTask != null) {
            googleDriveSearchTask.cancel(true);
        }
        googleDriveSearchTask = new GoogleDriveSearchTask(this);
        googleDriveSearchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);

        // 패스 UI를 가장 오른쪽으로 스크롤
        // 이동이 완료되기전에 이미 이동한다.
        scrollPath.post(new Runnable() {
                            @Override
                            public void run() {
                                scrollPath.fullScroll(View.FOCUS_RIGHT);
                            }
                        }
        );

        // preference는 쓰레드로 사용하지 않기로 함
        // 현재 패스를 저장
        PreferenceHelper.setLastPath(getActivity(), path);
        PreferenceHelper.setLastCloud(getActivity(), cloud);

        storageIndicator.setTargetView(googleDrive);
    }

    private boolean isPathChanged(String path) {
        String currentPath = PreferenceHelper.getLastPath(getContext());
        return !currentPath.equals(path);
    }

    private void softRefresh() {
        if (adapter != null) {
            adapter.setMode(mode);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRefresh() {
        Timber.e("onRefresh begin");
        // 외부 resume시에 들어올수도 있으므로 pref에서 읽는다.
        int lastCloud = PreferenceHelper.getLastCloud(getContext());
        String lastPath = PreferenceHelper.getLastPath(getContext());
        Timber.e("onRefresh end");

        cloud = lastCloud;
        updateFileList(lastPath);
    }

    @Override
    public void onBackPressed() {
        // Drawer가 열려 있으면 닫아야 함
        BaseMainActivity activity = (BaseMainActivity) getActivity();
        if (activity != null) {
            if (activity.isDrawerOpened()) {
                activity.closeDrawer();
                return;
            }
        }

        // 선택모드이면 선택모드를 취소하는 방향으로
        // 둘다 normal mode로 돌아간다.
        if (mode != MODE_NORMAL) {
            onNormalMode();
        } else {
            try {
                final String lastPath = MainApplication.getInstance(getActivity()).getLastPath();
                if (MainApplication.getInstance(getActivity()).isInitialPath(lastPath)) {// user root일 경우
                    super.onBackPressed();
                } else if (extSdCard != null && extSdCard.equals(lastPath)) {// sd카드 root일 경우
                    super.onBackPressed();
                } else {
                    gotoUpDirectory();
                }
            } catch (NullPointerException e) {

            }
        }
    }

    public int getViewType() {
        return viewType;
    }

    private void exitPlayerMode() {
        if (mode == MODE_PLAYER) {
            ((BaseMainActivity) getActivity()).hidePlayerUI();
        }
    }

    private void onSelectMode(ExplorerItem item, int position) {
        exitPlayerMode();
        mode = MODE_SELECT;

        // UI 상태만 리프레시
        // 왜냐하면 전체 체크박스를 나오게 해야 하기 때문이다.
        softRefresh();

        onSelectItemClick(item, position);

        // 하단 UI 표시
        FragmentActivity activity = getActivity();
        if (activity == null)
            return;
        ((BaseMainActivity) activity).showBottomUI();
    }

    private void onPasteMode() {
        exitPlayerMode();
        mode = MODE_PASTE;

        // 다시 리프레시를 해야지 체크박스를 새로 그린다.
        softRefresh();
    }

    public void onNormalMode() {
        exitPlayerMode();
        mode = MODE_NORMAL;

        // 다시 리프레시를 해야지 체크박스를 새로 그린다.
        softRefresh();

        // 하단 UI 숨김
        FragmentActivity activity = getActivity();
        if (activity == null)
            return;
        ((BaseMainActivity) activity).hideBottomUI();
    }

    // 이건 뭐지?
    // 아이템이 선택될때마다 선택된 아이템의 갯수를 업데이트하기위해서 여기에다가 모음
    private void onSelectItemClick(ExplorerItem item, int position) {
        item.selected = !item.selected;

        // 아이템을 찾아서 UI를 업데이트 해주어야 함
        adapter.notifyItemChanged(position);

        // 선택된 파일 카운트 업데이트
        updateSelectedFileCount();
    }

    private void updateSelectedFileCount() {
        int count = getSelectedFileCount();

        FragmentActivity activity = getActivity();
        if (activity == null)
            return;
        ((BaseMainActivity) activity).updateSelectedFileCount(count);
    }

    private void onRunItemClick(ExplorerItem item) {
        switch (item.type) {
            case ExplorerItem.FILETYPE_FOLDER:
                onNormalMode();
                onClickDirectory(item);
                break;
            case ExplorerItem.FILETYPE_IMAGE:
                onNormalMode();
                onClickImage(item);
                break;
            case ExplorerItem.FILETYPE_APK:
                onNormalMode();
                onClickApk(item);
                break;

            case ExplorerItem.FILETYPE_VIDEO:
                onNormalMode();
                onClickVideo(item);
                break;
            case ExplorerItem.FILETYPE_AUDIO:
                //onNormalMode()을 예외적으로 적용하지 않는다.
                onClickAudio(item);
                break;

            case ExplorerItem.FILETYPE_PDF:
            case ExplorerItem.FILETYPE_TEXT:
                //TODO: 나중에 읽던 책의 현재위치 이미지의 preview를 만들자.
                onNormalMode();
                onClickBook(item);
                break;

            case ExplorerItem.FILETYPE_ZIP:
                onNormalMode();
                if (AppHelper.isComicz(getActivity())) {
                    onClickBook(item);
                } else {
                    // 파일 탐색기에서는 ZIP파일 관리를 해주자.
                    onClickZip(item);
                }
                break;
        }
    }

    public void deleteFileWithDialog() {
        FragmentActivity activity = getActivity();
        if (activity == null)
            return;

        int count = getSelectedFileCount();
        // 파일을 삭제할건지 경고
        AlertHelper.showAlert(activity,
                AppHelper.getAppName(activity),
                String.format(getString(R.string.warn_file_delete), count),
                null,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FragmentActivity activity = getActivity();
                        if (activity == null)
                            return;
                        DeleteTask task = new DeleteTask(activity);
                        task.setFileList(fileList);
                        task.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
//                                Timber.e("onDismiss");
                                onRefresh();
                            }
                        });
                        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }, null);

    }

    public void selectAll() {
        // 전체가 선택된 상태라면 전부 선택 초기화를 해줌
        synchronized (this) {
            if (fileList == null)
                return;

            if (fileList.size() == getSelectedFileCount()) {
                for (int i = 0; i < fileList.size(); i++) {
                    fileList.get(i).selected = false;
                }
                ToastHelper.info(getActivity(), R.string.toast_deselect_all);
            } else {
                for (int i = 0; i < fileList.size(); i++) {
                    fileList.get(i).selected = true;
                }
                ToastHelper.info(getActivity(), R.string.toast_select_all);
            }
        }

        updateSelectedFileCount();

        // 그런다음에 화면 UI를 업데이트를 해준다.
        softRefresh();
    }

    public void captureSelectedFile(boolean cut) {
        // Toast 메세지를 표시
        // 선택된 파일을 목록을 작성
        selectedFileList = new ArrayList<>();
        synchronized (this) {
            if (fileList == null)
                return;

            for (int i = 0; i < fileList.size(); i++) {
                ExplorerItem item = fileList.get(i);
                if (item.selected) {
                    selectedFileList.add(item);
                }
            }
        }
        this.cut = cut;
        try {
            capturePath = MainApplication.getInstance(getActivity()).getLastPath();
        } catch (NullPointerException e) {

        }

        // 붙이기 모드로 바꿈
        onPasteMode();

        FragmentActivity activity = getActivity();
        if (activity != null) {
            ((BaseMainActivity) activity).updatePasteMode();
        }
    }

    void warnMoveToSameLocation() {
        // 이동 불가
        FragmentActivity activity = getActivity();
        AlertHelper.showAlert(activity,
                AppHelper.getAppName(activity),
                getString(R.string.warn_move_same_folder), null, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 동일한 위치이므로 그냥 종료 함
                    }
                }, null, null);
    }

    void runPasteTask(String pastePath) {
        FragmentActivity activity = getActivity();
        if (activity == null)
            return;

        PasteTask task = new PasteTask(activity);
        task.setIsCut(cut);// cut or copy
        task.setFileList(selectedFileList);
        task.setPath(capturePath, pastePath);
        task.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                // 현재 파일 리스트를 업데이트하고, 일반모드로 돌아가야 함
                onRefresh();
                onNormalMode();
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void pasteFileWithDialog() {
        try {
            final String pastePath = MainApplication.getInstance(getActivity()).getLastPath();

            // 복사될 폴더와 이동할 폴더가 같다면
            if (capturePath.equals(pastePath)) {
                if (cut) {
                    warnMoveToSameLocation();
                } else {
                    FragmentActivity activity = getActivity();
                    if (activity == null)
                        return;

                    // 사본 생성
                    AlertHelper.showAlert(activity,
                            AppHelper.getAppName(activity),
                            getString(R.string.warn_copy_same_folder), null, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    runPasteTask(pastePath);
                                }
                            }, null, null);
                }
            } else {
                runPasteTask(pastePath);
            }
        } catch (NullPointerException e) {

        }
    }

    void runZipTask(String name, String ext) {
        // 선택된 파일만 압축할 리스트에 추가해 줌
        ArrayList<ExplorerItem> zipList = new ArrayList<>();
        synchronized (this) {
            if (fileList == null)
                return;

            for (int i = 0; i < fileList.size(); i++) {
                if (fileList.get(i).selected) {
                    zipList.add(fileList.get(i));
                }
            }
        }

        FragmentActivity activity = getActivity();
        if (activity == null)
            return;

        try {
            String path = MainApplication.getInstance(activity).getLastPath();
            String zipPath = path + "/" + name + ext;

            ZipTask task = new ZipTask(activity);
            task.setPath(path);
            task.setPath(zipPath);

            task.setFileList(zipList);
            task.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    onRefresh();
                    onNormalMode();
                }
            });

            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (NullPointerException e) {

        }
    }

    public void zipFileWithDialog() {
        FragmentActivity activity = getActivity();
        if (activity == null)
            return;

        try {
            final String path = MainApplication.getInstance(activity).getLastPath();

            View view = activity.getLayoutInflater().inflate(R.layout.dialog_zip, null, false);
            final EditText editFileName = view.findViewById(R.id.file_name);
            final Spinner spinner = view.findViewById(R.id.zip_type);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    // zip파일의 이름을 현재 패스 기준으로 함
                    String base = path.substring(path.lastIndexOf("/") + 1);
                    String ext = spinner.getSelectedItem().toString();

                    final String lastPath = MainApplication.getInstance(getActivity()).getLastPath();
                    final String newPath = FileHelper.getNewFileName(lastPath + "/" + base + ext);

                    String newName = newPath.replace(lastPath + "/", "");
                    newName = newName.replace(ext, "");

                    // 새로나온 폴더의 이름을 edit에 반영함
                    editFileName.setText(newName);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            AlertHelper.showAlert(activity,
                    AppHelper.getAppName(activity),
                    getString(R.string.msg_file_zip),
                    view,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String name = editFileName.getText().toString();
                            String ext = spinner.getSelectedItem().toString();

                            runZipTask(name, ext);
                        }
                    }, null, null);
        } catch (NullPointerException e) {

        }
    }
}
