package com.duongame.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

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
import com.duongame.dialog.DeleteDialog;
import com.duongame.dialog.SortDialog;
import com.duongame.helper.AlertHelper;
import com.duongame.helper.AppHelper;
import com.duongame.helper.ExtSdCardHelper;
import com.duongame.helper.FileHelper;
import com.duongame.helper.JLog;
import com.duongame.helper.PreferenceHelper;
import com.duongame.helper.ToastHelper;
import com.duongame.manager.PermissionManager;
import com.duongame.manager.PositionManager;
import com.duongame.view.DividerItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

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
    private DividerItemDecoration itemDecoration = null;

    // 정렬
    private int sortType;
    private int sortDirection;

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_explorer, container, false);

        initUI();
        initBottomUI();
        initViewType();

        PermissionManager.checkStoragePermissions(getActivity());
        sortType = PreferenceHelper.getSortType(getActivity());
        sortDirection = PreferenceHelper.getSortDirection(getActivity());

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
    public void onStart() {
        super.onStart();
        JLog.e(TAG, "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        JLog.e(TAG, "onStop");
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
//        final int position = currentView.getFirstVisiblePosition();
        final int top = getCurrentViewScrollTop();

        new Thread(new Runnable() {
            @Override
            public void run() {
                PreferenceHelper.setLastPosition(getActivity(), position);
                PreferenceHelper.setLastTop(getActivity(), top);
            }
        }).start();
    }

    void initBottomUI() {
        LinearLayout bottom = ((BaseActivity) getActivity()).getBottomUI();
        if (bottom == null)
            return;

        // 삭제 버튼
        Button btnDelete = (Button) bottom.findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    void initUI() {
        switcherContents = (ViewSwitcher) rootView.findViewById(R.id.switcher_contents);
        switcherViewType = (ViewSwitcher) rootView.findViewById(R.id.switcher);
        textPath = (TextView) rootView.findViewById(R.id.text_path);
        scrollPath = (HorizontalScrollView) rootView.findViewById(R.id.scroll_path);
        fileList = new ArrayList<>();

        final ImageButton home = (ImageButton) rootView.findViewById(R.id.btn_home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateFileList(application.getInitialPath());
            }
        });

        final ImageButton up = (ImageButton) rootView.findViewById(R.id.btn_up);
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoUpDirectory();
            }
        });

        sdcard = (ImageButton) rootView.findViewById(R.id.btn_sdcard);
        sdcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (extSdCard != null) {
                    updateFileList(extSdCard);
                }
            }
        });

        textNoFiles = (TextView) rootView.findViewById(R.id.text_no_files);
        permButton = (Button) rootView.findViewById(R.id.btn_permission);
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
        this.viewType = viewType;

        switcherViewType.setDisplayedChild(viewType);

        switch (viewType) {
            case SWITCH_LIST:
                adapter = new ExplorerListAdapter(getActivity(), fileList);
                currentView = (RecyclerView) rootView.findViewById(R.id.list_explorer);
                if (currentView != null) {
                    currentView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    if (itemDecoration == null) {
                        itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
                        currentView.addItemDecoration(itemDecoration);
                    }
                }
                break;
            case SWITCH_GRID:
                adapter = new ExplorerGridAdapter(getActivity(), fileList);
                currentView = (RecyclerView) rootView.findViewById(R.id.grid_explorer);
                if (currentView != null) {
                    currentView.setLayoutManager(new GridLayoutManager(getActivity(), 4));
                }
                break;
        }

        if (adapter != null) {
            adapter.setSelectMode(selectMode);
            adapter.setOnItemClickListener(this);
            // 코믹z가 아닐때 롱클릭 활성화 (임시)
            if (!AppHelper.isComicz(getContext())) {
                adapter.setOnLongItemClickListener(this);
            }
        }

        if (currentView != null) {
            currentView.setAdapter(adapter);
            currentView.addOnScrollListener(new ExplorerScrollListener());
        }

        PreferenceHelper.setViewType(getContext(), viewType);
    }

    // 새로운 파일이 추가 되었을때 스캔을 하라는 의미이다.
    void requestThumbnailScan() {
        //ArrayList<ExplorerItem> imageList = (ArrayList<ExplorerItem>) FileSearcher.getImageList().clone();
        if (searchResult == null || searchResult.imageList == null)
            return;

        ArrayList<ExplorerItem> imageList = (ArrayList<ExplorerItem>) searchResult.imageList.clone();

        FragmentActivity activity = getActivity();
        for (ExplorerItem item : imageList) {
            if (activity == null)
                break;
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
            final String providerName = getContext().getPackageName() + ".provider";
            final Uri apkUri = FileProvider.getUriForFile(getContext(), providerName, new File(item.path));
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } else {
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + item.path), "application/vnd.android.package-archive");
            startActivity(intent);
        }
    }

    void onClickBook(ExplorerItem item) {
        BookLoader.load(getActivity(), item, false);
    }

    void onAdapterItemLongClick(int position) {
        JLog.e(TAG, "onAdapterItemLongClick=" + position);

        ExplorerItem item = fileList.get(position);
        if (item == null)
            return;

        // 이미 선택 모드라면 이름변경을 해줌
        if (selectMode) {
            //renameFileWithDialog
            if (getSelectedFileCount() == 1) {
                renameFileWithDialog(item);
            } else {
                ToastHelper.showToast(getActivity(), R.string.toast_multi_rename_error);
            }
        } else {// 선택 모드로 진입 + 현재 파일 선택
            onSelectMode(item, position);
        }
    }

    int getSelectedFileCount() {
        if (fileList == null)
            return 0;

        int count = 0;
        for (int i = 0; i < fileList.size(); i++) {
            if (fileList.get(i).selected) {
                count++;
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

                PreferenceHelper.setSortType(getActivity(), sortType);
                PreferenceHelper.setSortDirection(getActivity(), sortDirection);

                updateFileList(application.getLastPath());
            }
        });

        dialog.show(getActivity().getFragmentManager(), "sort");
    }

    // Zip 압축을 풀때 기존에 폴더가 있으면 새로운 폴더명으로 풀어준다.
    // 폴더를 생성할때는 새로운 폴더명이 있으면 있다고 확인을 한다.
    void getNewFolderName() {

    }

    public void newFolderWithDialog() {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_rename, null, false);
        final EditText editFileName = (EditText) view.findViewById(R.id.file_name);
        editFileName.setText(R.string.new_folder);

        AlertHelper.showAlert(getActivity(),
                AppHelper.getAppName(getContext()),
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
        if (folder.exists()) {
            ToastHelper.showToast(getActivity(), R.string.toast_error);
        } else {
            folder.mkdir();
            ToastHelper.showToast(getActivity(), R.string.toast_new_folder);
        }

        // 파일 리스트 리프레시를 요청해야함
        onRefresh();
    }

    void renameFileWithDialog(final ExplorerItem item) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_rename, null, false);
        final EditText editFileName = (EditText) view.findViewById(R.id.file_name);
        editFileName.setText(item.name);

        AlertHelper.showAlert(getActivity(),
                AppHelper.getAppName(getContext()),
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
            JLog.e(TAG, item.path + " " + newPath);

            // 파일이름을 변경
            new File(item.path).renameTo(new File(newPath));

            // 성공시 선택 해제 및 파일 정보 변경
            item.selected = false;
            item.name = newName;
            item.path = newPath;

            // 정보 변경 반영
            adapter.notifyItemChanged(item.position);

            ToastHelper.showToast(getActivity(), R.string.toast_file_rename);
        } catch (Exception e) {
            ToastHelper.showToast(getActivity(), R.string.toast_error);
        }
    }

    void onAdapterItemClick(int position) {
        JLog.e(TAG, "onAdapterItemClick=" + position);

        ExplorerItem item = fileList.get(position);
        if (item == null)
            return;

        if (selectMode) {
            onSelectItemClick(item, position);
        } else {
            onRunItemClick(item);
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
        onAdapterItemClick(position);
    }

    @Override
    public void onItemLongClick(int position) {
        onAdapterItemLongClick(position);
    }

    class SearchTask extends AsyncTask<String, Void, Void> {

        boolean pathChanged;
        String path;
        Comparator<ExplorerItem> comparator;

        public SearchTask(boolean pathChanged) {
            this.pathChanged = pathChanged;
        }

        void updateComparator() {
            if (sortDirection == 0) {// ascending
                switch (sortType) {
                    case 0:
                        comparator = new FileHelper.NameAscComparator();
                        break;
                    case 1:
                        comparator = new FileHelper.ExtAscComparator();
                        break;
                    case 2:
                        comparator = new FileHelper.SizeAscComparator();
                        break;
                }
            } else {
                switch (sortType) {// descending
                    case 0:
                        comparator = new FileHelper.NameDescComparator();
                        break;
                    case 1:
                        comparator = new FileHelper.ExtDescComparator();
                        break;
                    case 2:
                        comparator = new FileHelper.SizeDescComparator();
                        break;
                }
            }
        }

        @Override
        protected Void doInBackground(String... params) {
            path = params[0];
            updateComparator();
            searchResult = fileSearcher
                    .setRecursiveDirectory(false)
                    .setExcludeDirectory(false)
                    .setComparator(comparator)
                    .setHiddenFile(false)
                    .setImageListEnable(true)
                    .search(path);
            if (searchResult != null) {
                fileList = searchResult.fileList;
                adapter.setFileList(fileList);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            JLog.e(TAG, "SearchTask onPostExecute");
            if (isCancelled())
                return;

            JLog.e(TAG, "SearchTask isCancelled");
            adapter.notifyDataSetChanged();

            // SearchTask가 resume
            if (pathChanged) {
                if (fileList != null && fileList.size() > 0) {
                    currentView.scrollToPosition(0);
                    currentView.invalidate();
                }
                JLog.e(TAG, "SearchTask pathChanged");
            }

            // 성공했을때 현재 패스를 업데이트
            application.setLastPath(path);
            textPath.setText(path);
            textPath.requestLayout();
            JLog.e(TAG, "SearchTask requestLayout");

            if (switcherContents != null) {
                JLog.e(TAG, "SearchTask switcherContents != null");
                if (fileList == null || fileList.size() <= 0) {
                    switcherContents.setDisplayedChild(1);
                    JLog.e(TAG, "SearchTask setDisplayedChild(1)");

                    // 퍼미션이 있으면 퍼미션 버튼을 보이지 않게 함
                    if (PermissionManager.checkStoragePermissions()) {
                        permButton.setVisibility(GONE);
                        textNoFiles.setVisibility(View.VISIBLE);
                    } else {
                        permButton.setVisibility(View.VISIBLE);
                        textNoFiles.setVisibility(GONE);
                    }
                } else {
                    switcherContents.setDisplayedChild(0);
                    JLog.e(TAG, "SearchTask setDisplayedChild()");
                }
            }
        }
    }

    public void updateFileList(final String path, boolean isPathChanged) {
        JLog.e(TAG, "updateFileList");
        if (adapter == null) {
            return;
        }

        // 선택모드인지 설정해준다.
        adapter.setSelectMode(selectMode);

        JLog.e(TAG, "updateFileList adapter");
        // 썸네일이 꽉찼을때는 비워준다.
        if (BitmapCacheManager.getThumbnailCount() > MAX_THUMBNAILS) {
            BitmapCacheManager.removeAllThumbnails();
        }

        JLog.e(TAG, "updateFileList BitmapCacheManager");
        //FIX:
        //SearchTask task = new SearchTask(isPathChanged(path));
        //task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);
        if (searchTask != null) {
            searchTask.cancel(true);
        }
        searchTask = new SearchTask(isPathChanged);
        searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);
        JLog.e(TAG, "updateFileList executeOnExecutor");

        // 가장 오른쪽으로 스크롤
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
            updateFileList(path, true);
        else
            updateFileList(path, isPathChanged(path));
    }

    private boolean isPathChanged(String path) {
        String currentPath = PreferenceHelper.getLastPath(getContext());
        boolean pathChanged = !currentPath.equals(path);
        return pathChanged;
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
        if (selectMode) {
            onNormalMode();
        } else {
            if (!application.isInitialPath(application.getLastPath())) {
                gotoUpDirectory();
            } else {
                super.onBackPressed();
            }
        }
    }

    public int getViewType() {
        return viewType;
    }

    void onSelectMode(ExplorerItem item, int position) {
        selectMode = true;

        // UI 상태만 리프레시
        // 왜냐하면 전체 체크박스를 나오게 해야 하기 때문이다.
        softRefresh();

        onSelectItemClick(item, position);

        // 하단 UI 표시
        ((BaseActivity) getActivity()).showBottomUI();
    }

    void onNormalMode() {
        selectMode = false;

        // 다시 리프레시를 해야지 체크박스를 새로 그린다.
        softRefresh();

        // 하단 UI 숨김
        ((BaseActivity) getActivity()).hideBottomUI();
    }

    // 이건 뭐지?
    // 아이템이 선택될때마다 선택된 아이템의 갯수를 업데이트하기위해서 여기에다가 모음
    void onSelectItemClick(ExplorerItem item, int position) {
        item.selected = !item.selected;

        // 아이템을 찾아서 UI를 업데이트 해주어야 함
        adapter.notifyItemChanged(position);

        // 선택된 파일 카운트 업데이트
        int count = getSelectedFileCount();
        ((BaseActivity) getActivity()).updateSelectedFileCount(count);
    }

    void onRunItemClick(ExplorerItem item) {
        switch (item.type) {
            case FOLDER:
                onClickDirectory(item);
                break;
            case IMAGE:
                onClickImage(item);
                break;
            case APK:
                onClickApk(item);
                break;

            case PDF:
            case TEXT:
                //TODO: 나중에 읽던 책의 현재위치 이미지의 preview를 만들자.
                onClickBook(item);
                break;

            case ZIP:
                if (AppHelper.isComicz(getActivity())) {
                    onClickBook(item);
                } else {
                    // 파일 탐색기에서는 ZIP파일 관리를 해주자.
                }
                break;
        }
    }

    public void deleteFileWithDialog() {
        int count = getSelectedFileCount();
        // 파일을 삭제할건지 경고
        AlertHelper.showAlert(getActivity(),
                AppHelper.getAppName(getActivity()),
                String.format(getString(R.string.warn_file_delete), count),
                null,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DeleteDialog deleteDialog = new DeleteDialog();
                        deleteDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                onRefresh();
                                //onNormalMode();// 삭제후에는 노말모드로 돌아간다.
                            }
                        });
                        deleteDialog.setFileList(fileList);
                        deleteDialog.show(getActivity().getFragmentManager(), "delete");
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }, null);

    }
}
