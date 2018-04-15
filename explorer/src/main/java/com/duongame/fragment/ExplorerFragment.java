package com.duongame.fragment;

import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.duongame.AnalyticsApplication;
import com.duongame.R;
import com.duongame.activity.BaseActivity;
import com.duongame.activity.PhotoActivity;
import com.duongame.adapter.ExplorerAdapter;
import com.duongame.adapter.ExplorerGridAdapter;
import com.duongame.adapter.ExplorerItem;
import com.duongame.adapter.ExplorerListAdapter;
import com.duongame.adapter.ExplorerScrollListener;
import com.duongame.bitmap.BitmapCacheManager;
import com.duongame.db.BookLoader;
import com.duongame.dialog.SortDialog;
import com.duongame.helper.AlertHelper;
import com.duongame.helper.AppHelper;
import com.duongame.helper.ExtSdCardHelper;
import com.duongame.helper.FileHelper;
import com.duongame.helper.FileSearcher;
import com.duongame.helper.PreferenceHelper;
import com.duongame.helper.ToastHelper;
import com.duongame.manager.PermissionManager;
import com.duongame.manager.PositionManager;
import com.duongame.task.file.DeleteTask;
import com.duongame.task.file.PasteTask;
import com.duongame.task.zip.UnzipTask;
import com.duongame.task.zip.ZipTask;
import com.duongame.view.DividerItemDecoration;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.view.View.GONE;
import static com.duongame.ExplorerConfig.MAX_THUMBNAILS;

/**
 * Created by namjungsoo on 2016-11-23.
 */

public class ExplorerFragment extends BaseFragment implements ExplorerAdapter.OnItemClickListener, ExplorerAdapter.OnItemLongClickListener {
    private final static String TAG = "ExplorerFragment";
    private final static boolean DEBUG = false;

    public final static int SWITCH_LIST = 0;
    public final static int SWITCH_GRID = 1;

    // 파일 관련
    private ExplorerAdapter adapter;
    private ArrayList<ExplorerItem> fileList;

    // 붙이기 관련
    private ArrayList<ExplorerItem> selectedFileList;
    private boolean cut;

    // 패스 관련
    private TextView textPath;
    private HorizontalScrollView scrollPath;

    // 컨텐츠 관련
    private RecyclerView currentView;
    private View rootView;

    // 뷰 스위처
    private ViewSwitcher switcherViewType;
    private ViewSwitcher switcherContents;
    private Button permButton;
    private TextView textNoFiles;
    private int viewType = SWITCH_LIST;

    // 기타
    private ImageButton sdcard = null;
    private String extSdCard = null;
    private SearchTask searchTask = null;

    // 선택
    private boolean selectMode = false;
    private boolean pasteMode = false;// 붙여넣기 모드는 뒤로가기 버튼이 있고
    private DividerItemDecoration itemDecoration = null;

    private String capturePath;

    // 정렬
    private int sortType;
    private int sortDirection;

    private boolean canClick = true;

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
        initViewType();

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

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        BaseActivity activity = (BaseActivity) getActivity();
        if (activity != null) {
            if (!activity.getShowReview()) {
                if (AppHelper.isComicz(getContext())) {
                    BookLoader.openLastBook(activity);
                }
            }
        }
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

        final ImageButton home = rootView.findViewById(R.id.btn_home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateFileList(application.getInitialPath());
            }
        });

        final ImageButton up = rootView.findViewById(R.id.btn_up);
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
                    updateFileList(extSdCard);
                }
            }
        });

        textNoFiles = rootView.findViewById(R.id.text_no_files);
        permButton = rootView.findViewById(R.id.btn_permission);
        if (permButton != null) {
            permButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PermissionManager.checkStoragePermissions(getActivity());
                }
            });
        }
    }

    void initViewType() {
        changeViewType(PreferenceHelper.getViewType(getActivity()));
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
    @SuppressWarnings("unchecked")
    void requestThumbnailScan() {
        //ArrayList<ExplorerItem> imageList = (ArrayList<ExplorerItem>) FileSearcher.getImageList().clone();
        if (searchResult == null || searchResult.imageList == null)
            return;

        Object imageListObj = searchResult.imageList.clone();
        if (imageListObj == null)
            return;
        ArrayList<ExplorerItem> imageList;
        if (imageListObj instanceof ArrayList) {
            imageList = (ArrayList<ExplorerItem>) imageListObj;
        } else {
            return;
        }

        FragmentActivity activity = getActivity();
        for (ExplorerItem item : imageList) {
            if (activity == null)
                break;
            if (item == null || item.path == null)
                continue;
            activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + item.path)));
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

    void onClickBook(ExplorerItem item) {
        FragmentActivity activity = getActivity();
        if (activity == null)
            return;
        BookLoader.load(activity, item, false);
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
        if (activity != null)
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
//        PositionManager.setPosition(FileSearcher.getLastPath(), currentView.getFirstVisiblePosition());
//        PositionManager.setTop(FileSearcher.getLastPath(), getCurrentViewScrollTop());
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

    class SearchTask extends AsyncTask<String, Void, Void> {
        WeakReference<ExplorerFragment> fragmentWeakReference;
        boolean pathChanged;
        String path;
        Comparator<ExplorerItem> comparator;

        SearchTask(ExplorerFragment fragment, boolean pathChanged) {
            this.pathChanged = pathChanged;
            fragmentWeakReference = new WeakReference<>(fragment);
        }

        void updateComparator() {
            ExplorerFragment fragment = fragmentWeakReference.get();
            if (fragment == null)
                return;

            if (fragment.sortDirection == 0) {// ascending
                switch (fragment.sortType) {
                    case 0:
                        comparator = new FileHelper.NameAscComparator();
                        break;
                    case 1:
                        comparator = new FileHelper.ExtAscComparator();
                        break;
                    case 2:
                        comparator = new FileHelper.DateAscComparator();
                        break;
                    case 3:
                        comparator = new FileHelper.SizeAscComparator();
                        break;
                }
            } else {
                switch (fragment.sortType) {// descending
                    case 0:
                        comparator = new FileHelper.NameDescComparator();
                        break;
                    case 1:
                        comparator = new FileHelper.ExtDescComparator();
                        break;
                    case 2:
                        comparator = new FileHelper.DateDescComparator();
                        break;
                    case 3:
                        comparator = new FileHelper.SizeDescComparator();
                        break;
                }
            }
        }

        @Override
        protected Void doInBackground(String... params) {
            path = params[0];
            updateComparator();

            ExplorerFragment fragment = fragmentWeakReference.get();
            if (fragment == null)
                return null;

            fragment.searchResult = fragment.fileSearcher
                    .setRecursiveDirectory(false)
                    .setExcludeDirectory(false)
                    .setComparator(comparator)
                    .setHiddenFile(false)
                    .setImageListEnable(true)
                    .search(path);

            fragment = fragmentWeakReference.get();
            if (fragment == null)
                return null;

            if (fragment.searchResult == null) {
                fragment.searchResult = new FileSearcher.Result();
            }
            synchronized (ExplorerFragment.this) {
                fragment.fileList = fragment.searchResult.fileList;
                fragment.application.setImageList(fragment.searchResult.imageList);
                fragment.adapter.setFileList(fragment.fileList);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();// AsyncTask는 아무것도 안함

            canClick = false;// 이제부터 클릭할수 없음
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);// AsyncTask는 아무것도 안함

            if (isCancelled())
                return;

            ExplorerFragment fragment = fragmentWeakReference.get();
            if (fragment == null)
                return;

            fragment.adapter.notifyDataSetChanged();

            // SearchTask가 resume
            if (pathChanged) {
                synchronized (ExplorerFragment.this) {
                    if (fragment.fileList != null && fragment.fileList.size() > 0) {
                        fragment.currentView.scrollToPosition(0);
                        fragment.currentView.invalidate();
                    }
                }
            }

            // 성공했을때 현재 패스를 업데이트
            fragment.application.setLastPath(path);
            fragment.textPath.setText(path);
            fragment.textPath.requestLayout();

            if (fragment.switcherContents != null) {
                if (fragment.fileList == null || fragment.fileList.size() <= 0) {
                    fragment.switcherContents.setDisplayedChild(1);

                    // 퍼미션이 있으면 퍼미션 버튼을 보이지 않게 함
                    if (PermissionManager.checkStoragePermissions()) {
                        fragment.permButton.setVisibility(GONE);
                        fragment.textNoFiles.setVisibility(View.VISIBLE);
                    } else {
                        fragment.permButton.setVisibility(View.VISIBLE);
                        fragment.textNoFiles.setVisibility(GONE);
                    }
                } else {
                    fragment.switcherContents.setDisplayedChild(0);
                }
            }

            canClick = true;
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
        //SearchTask task = new SearchTask(isPathChanged(path));
        //task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);
        if (searchTask != null) {
            searchTask.cancel(true);
        }
        searchTask = new SearchTask(this, isPathChanged);
        searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);

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
        if (path == null)
            updateFileList(null, true);
        else
            updateFileList(path, isPathChanged(path));
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
        // 외부 resume시에 들어올수도 있으므로 pref에서 읽는다.
        updateFileList(PreferenceHelper.getLastPath(getContext()));
    }

    @Override
    public void onBackPressed() {
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
        ((BaseActivity) activity).showBottomUI();
    }

    public void onPasteMode() {
        selectMode = false;
        pasteMode = true;

        // 다시 리프레시를 해야지 체크박스를 새로 그린다.
        softRefresh();
    }

    void onNormalMode() {
        selectMode = false;
        pasteMode = false;

        // 다시 리프레시를 해야지 체크박스를 새로 그린다.
        softRefresh();

        // 하단 UI 숨김
        FragmentActivity activity = getActivity();
        if (activity == null)
            return;
        ((BaseActivity) activity).hideBottomUI();
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
        ((BaseActivity) activity).updateSelectedFileCount(count);
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
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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
            ((BaseActivity) activity).updatePasteMode();
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
        if (activity != null)
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
        if (activity != null)
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
