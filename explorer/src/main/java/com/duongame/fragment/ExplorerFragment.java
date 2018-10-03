package com.duongame.fragment;

import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.duongame.AnalyticsApplication;
import com.duongame.R;
import com.duongame.activity.main.BaseMainActivity;
import com.duongame.activity.viewer.PhotoActivity;
import com.duongame.adapter.ExplorerAdapter;
import com.duongame.adapter.ExplorerGridAdapter;
import com.duongame.adapter.ExplorerItem;
import com.duongame.adapter.ExplorerListAdapter;
import com.duongame.adapter.ExplorerScrollListener;
import com.duongame.bitmap.BitmapCacheManager;
import com.duongame.db.BookLoader;
import com.duongame.dialog.SortDialog;
import com.duongame.file.FileHelper;
import com.duongame.helper.AlertHelper;
import com.duongame.helper.AppHelper;
import com.duongame.helper.ExtSdCardHelper;
import com.duongame.helper.JLog;
import com.duongame.helper.PreferenceHelper;
import com.duongame.helper.ToastHelper;
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
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

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

    private ArrayList<ExplorerItem> fileList;

    // 붙이기 관련
    private ArrayList<ExplorerItem> selectedFileList;
    private boolean cut;

    private LocalSearchTask localSearchTask = null;
    private DropboxSearchTask dropboxSearchTask = null;
    private GoogleDriveSearchTask googleDriveSearchTask = null;

    // 선택
    private boolean selectMode = false;
    private boolean pasteMode = false;// 붙여넣기 모드는 뒤로가기 버튼이 있고
    private String capturePath;


    // 정렬
    private int sortType;
    private int sortDirection;

    private boolean canClick = true;
    private int viewType = SWITCH_LIST;

    private boolean backupDropbox = false;
    private boolean backupGoogleDrive = false;


    public boolean isCanClick() {
        return canClick;
    }

    public void setCanClick(boolean canClick) {
        this.canClick = canClick;

        BaseMainActivity activity = (BaseMainActivity) getActivity();
        if(activity != null) {
            ProgressBar progressBar = activity.getProgressBarLoading();
            if(progressBar != null) {
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


    public Tracker getTracker() {
        FragmentActivity activity = getActivity();
        if (activity == null)
            return null;

        Application application = activity.getApplication();
        if (application == null)
            return null;

        return ((AnalyticsApplication) application).getDefaultTracker();
    }

    public void sendEventTracker(Map<String, String> var) {
        Tracker tracker = getTracker();
        if (tracker == null)
            return;

        tracker.send(var);
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_explorer, container, false);

        initUI();
        JLog.e("Jungsoo", "initUI end");

        initViewType();
        JLog.e("Jungsoo", "initViewType end");

        FragmentActivity activity = getActivity();
        if (activity != null) {
            PermissionManager.checkStoragePermissions(activity);
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
        application.setLastPath(path);

        JLog.e("Jungsoo", "onCreateView end");
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
        JLog.e("Jungsoo", "onActivityCreated end");
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

        new Thread(new Runnable() {
            @Override
            public void run() {
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    PreferenceHelper.setLastPosition(activity, position);
                    PreferenceHelper.setLastTop(activity, top);
                }
            }
        }).start();
    }

    public boolean isPasteMode() {
        return pasteMode;
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
                updateFileList(application.getInitialPath());
            }
        });

        up = rootView.findViewById(R.id.btn_up);
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoUpDirectory();
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
        JLog.e("Jungsoo", "updateDropboxUI "+show);
        if (dropbox == null) {
            backupDropbox = show;
            return;
        }

        if (show) {
            dropbox.setVisibility(View.VISIBLE);
        } else {
            dropbox.setVisibility(View.GONE);

            // 로그아웃인 상황이니 최초로 간다.
            cloud = CLOUD_LOCAL;
            updateFileList(application.getInitialPath());
        }

        storageIndicator.refresh();
    }

    public void updateGoogleDriveUI(boolean show) {
        JLog.e("Jungsoo", "updateGoogleDriveUI "+show);
        if (googleDrive == null) {
            backupGoogleDrive = show;
            return;
        }

        if (show) {
            googleDrive.setVisibility(View.VISIBLE);
        } else {
            googleDrive.setVisibility(View.GONE);

            // 로그아웃인 상황이니 최초로 간다.
            cloud = CLOUD_LOCAL;
            updateFileList(application.getInitialPath());
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

        // 이벤트를 보냄
        sendEventTracker(new HitBuilders.EventBuilder().setCategory("UI").setAction("ViewType").setValue(viewType).build());

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
        }

        if (adapter != null) {
            adapter.setSelectMode(selectMode);
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
        String path = application.getLastPath();
        path = path.substring(0, path.lastIndexOf('/'));
        if (path.length() == 0) {
            path = "/";
        }

        backupPosition();
        updateFileList(path);
    }

    void onClickDirectory(ExplorerItem item) {
        String newPath;
        if (application.getLastPath().equals("/")) {
            newPath = application.getLastPath() + item.name;
        } else {
            newPath = application.getLastPath() + "/" + item.name;
        }

        updateFileList(newPath);
    }

    void onClickImage(ExplorerItem item) {
        final Intent intent = PhotoActivity.getLocalIntent(getContext(), item);
        startActivity(intent);
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
        if(file.exists()) {
            AlertHelper.showAlertWithAd(activity,
                    AppHelper.getAppName(activity),
                    getString(R.string.msg_overwrite),

                    //positive
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // 확인을 눌렀으므로 다운로드하여 덮어씌움
                            task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, item);// metadata = fileId
                            ToastHelper.showToast(activity, String.format(getResources().getString(R.string.toast_cloud_download), item.name));

                            dialogInterface.dismiss();
                        }
                    },
                    //negative
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    },null);
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

        if(!checkDownloadOverwrite(activity, item, task)) {// overwrite하지 않으면 바로 다운로드
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

        if(!checkDownloadOverwrite(activity, item, task)) {// overwrite하지 않으면 바로 다운로드
            downloadAndOpen(activity, item, task);
        }
    }

    void onClickBook(final ExplorerItem item) {
        final FragmentActivity activity = getActivity();
        if (activity == null)
            return;

        if(cloud == CLOUD_LOCAL) {
            BookLoader.load(activity, item, false);
        } else if(cloud == CLOUD_DROPBOX) {
            onClickBookDropbox(activity, item);
        } else if(cloud == CLOUD_GOOGLEDRIVE) {
            onClickBookGoogleDrive(activity, item);
        }
    }

    void onClickZip(ExplorerItem item) {
        unzipWithDialog(item);
    }

    void runUnzipTask(ExplorerItem item, String name) {
        String path = application.getLastPath();
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

        // 이벤트를 보냄
        sendEventTracker(new HitBuilders.EventBuilder().setCategory("File").setAction("Unzip").build());
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

        // path/zipname 폴더가 있는지 확인
        final String newPath = FileHelper.getNewFileName(application.getLastPath() + "/" + base);
        String newName = newPath.replace(application.getLastPath() + "/", "");

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

    }

    void onAdapterItemLongClick(int position) {
//        JLog.e(TAG, "onAdapterItemLongClick=" + position);
        synchronized (this) {
            if (fileList == null)
                return;

            ExplorerItem item = fileList.get(position);
            if (item == null)
                return;

            // 이미 선택 모드라면 이름변경을 해줌
            if (selectMode) {
                //renameFileWithDialog
                if (getSelectedFileCount() == 1) {
                    renameFileWithDialog(item);
                } else {
                    ToastHelper.warning(getActivity(), R.string.toast_multi_rename_error);
                }
            } else {// 선택 모드로 진입 + 현재 파일 선택
                onSelectMode(item, position);
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

                updateFileList(application.getLastPath());
            }
        });

        FragmentActivity activity = getActivity();
        if (activity != null) {
            dialog.show(activity.getFragmentManager(), "sort");
        }

        // 이벤트를 보냄
        sendEventTracker(new HitBuilders.EventBuilder().setCategory("File").setAction("Sort").setValue(sortType * 10 + sortDirection).build());
    }

    public void newFolderWithDialog() {
        FragmentActivity activity = getActivity();
        if (activity == null)
            return;

        View view = activity.getLayoutInflater().inflate(R.layout.dialog_single, null, false);
        final EditText editFileName = view.findViewById(R.id.file_name);

        String base = getString(R.string.new_folder);
        String newName = FileHelper.getNewFileName(application.getLastPath() + "/" + base);
        newName = newName.replace(application.getLastPath() + "/", "");

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

    }

    void newFolder(String newFolder) {
        String path = application.getLastPath();
        File folder = new File(path + "/" + newFolder);

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

        // 이벤트를 보냄
        sendEventTracker(new HitBuilders.EventBuilder().setCategory("File").setAction("NewFolder").build());
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

            // 이벤트를 보냄
            sendEventTracker(new HitBuilders.EventBuilder().setCategory("File").setAction("Rename").build());

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
            if (selectMode) {
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
        adapter.setSelectMode(selectMode);

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
        localSearchTask = new LocalSearchTask(this, isPathChanged);
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
        JLog.e("Jungsoo", "updateFileList set begin");
        PreferenceHelper.setLastPath(getActivity(), path);
        PreferenceHelper.setLastCloud(getActivity(), cloud);
        JLog.e("Jungsoo", "updateFileList set end");

        // 외장 패스인지 체크하여
        boolean isExtSdCard = false;
        if(extSdCard != null && path.startsWith(extSdCard))
            isExtSdCard = true;

        if(isExtSdCard) {
            storageIndicator.setTargetView(sdcard);
        } else {
            storageIndicator.setTargetView(home);
        }

        // 오래 걸림. 이것도 쓰레드로...
        new Thread(new Runnable() {
            @Override
            public void run() {
                requestThumbnailScan();
            }
        }).start();
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
        adapter.setSelectMode(selectMode);

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
        adapter.setSelectMode(selectMode);

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
            adapter.setSelectMode(selectMode);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRefresh() {
        JLog.e("Jungsoo", "onRefresh begin");
        // 외부 resume시에 들어올수도 있으므로 pref에서 읽는다.
        int lastCloud = PreferenceHelper.getLastCloud(getContext());
        String lastPath = PreferenceHelper.getLastPath(getContext());
        JLog.e("Jungsoo", "onRefresh end");

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
        if (pasteMode || selectMode) {
            onNormalMode();
        } else {
            if (application == null)
                return;

            if (application.isInitialPath(application.getLastPath())) {// user root일 경우
                super.onBackPressed();
            } else if (extSdCard != null && extSdCard.equals(application.getLastPath())) {// sd카드 root일 경우
                super.onBackPressed();
            } else {
                gotoUpDirectory();
            }
        }
    }

    public int getViewType() {
        return viewType;
    }

    void onSelectMode(ExplorerItem item, int position) {
        selectMode = true;
        pasteMode = false;

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

    public void onPasteMode() {
        selectMode = false;
        pasteMode = true;

        // 다시 리프레시를 해야지 체크박스를 새로 그린다.
        softRefresh();
    }

    public void onNormalMode() {
        selectMode = false;
        pasteMode = false;

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
    void onSelectItemClick(ExplorerItem item, int position) {
        item.selected = !item.selected;

        // 아이템을 찾아서 UI를 업데이트 해주어야 함
        adapter.notifyItemChanged(position);

        // 선택된 파일 카운트 업데이트
        updateSelectedFileCount();
    }

    void updateSelectedFileCount() {
        int count = getSelectedFileCount();

        FragmentActivity activity = getActivity();
        if (activity == null)
            return;
        ((BaseMainActivity) activity).updateSelectedFileCount(count);
    }

    void onRunItemClick(ExplorerItem item) {
        switch (item.type) {
            case ExplorerItem.FILETYPE_FOLDER:
                onClickDirectory(item);
                break;
            case ExplorerItem.FILETYPE_IMAGE:
                onClickImage(item);
                break;
            case ExplorerItem.FILETYPE_APK:
                onClickApk(item);
                break;

            case ExplorerItem.FILETYPE_PDF:
            case ExplorerItem.FILETYPE_TEXT:
                //TODO: 나중에 읽던 책의 현재위치 이미지의 preview를 만들자.
                onClickBook(item);
                break;

            case ExplorerItem.FILETYPE_ZIP:
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
//                                JLog.e(TAG, "onDismiss");
                                onRefresh();
                            }
                        });
                        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

                        // 이벤트를 보냄
                        sendEventTracker(new HitBuilders.EventBuilder().setCategory("File").setAction("Delete").build());
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

        // 이벤트를 보냄
        sendEventTracker(new HitBuilders.EventBuilder().setCategory("File").setAction("SelectAll").build());
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
        capturePath = application.getLastPath();

        // 붙이기 모드로 바꿈
        onPasteMode();

        FragmentActivity activity = getActivity();
        if (activity != null) {
            ((BaseMainActivity) activity).updatePasteMode();
        }

        // 이벤트를 보냄
        if (cut)
            sendEventTracker(new HitBuilders.EventBuilder().setCategory("File").setAction("Cut").build());
        else
            sendEventTracker(new HitBuilders.EventBuilder().setCategory("File").setAction("Copy").build());
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

        // 이벤트를 보냄
        sendEventTracker(new HitBuilders.EventBuilder().setCategory("File").setAction("Paste").build());
    }

    public void pasteFileWithDialog() {
        final String pastePath = application.getLastPath();

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

        String path = application.getLastPath();
        String zipPath = path + "/" + name + ext;

        FragmentActivity activity = getActivity();
        if (activity == null)
            return;

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

        // 이벤트를 보냄
        sendEventTracker(new HitBuilders.EventBuilder().setCategory("File").setAction("Zip").setValue(FileHelper.getCompressType(ext)).build());
    }

    public void zipFileWithDialog() {
        final String path = application.getLastPath();

        FragmentActivity activity = getActivity();
        if (activity == null)
            return;

        View view = activity.getLayoutInflater().inflate(R.layout.dialog_zip, null, false);
        final EditText editFileName = view.findViewById(R.id.file_name);
        final Spinner spinner = view.findViewById(R.id.zip_type);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // zip파일의 이름을 현재 패스 기준으로 함
                String base = path.substring(path.lastIndexOf("/") + 1);
                String ext = spinner.getSelectedItem().toString();
                final String newPath = FileHelper.getNewFileName(application.getLastPath() + "/" + base + ext);

                String newName = newPath.replace(application.getLastPath() + "/", "");
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
    }
}
