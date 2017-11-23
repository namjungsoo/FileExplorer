package com.duongame.explorer.fragment;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.duongame.R;
import com.duongame.comicz.db.BookLoader;
import com.duongame.explorer.activity.BaseActivity;
import com.duongame.explorer.adapter.ExplorerAdapter;
import com.duongame.explorer.adapter.ExplorerGridAdapter;
import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.adapter.ExplorerListAdapter;
import com.duongame.explorer.adapter.ExplorerScrollListener;
import com.duongame.explorer.bitmap.BitmapCacheManager;
import com.duongame.explorer.helper.ExtSdCardHelper;
import com.duongame.explorer.helper.PreferenceHelper;
import com.duongame.explorer.manager.ExplorerManager;
import com.duongame.explorer.manager.PermissionManager;
import com.duongame.explorer.manager.PositionManager;
import com.duongame.explorer.view.DividerItemDecoration;
import com.duongame.viewer.activity.PhotoActivity;

import java.io.File;
import java.util.ArrayList;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.view.View.GONE;
import static com.duongame.explorer.ExplorerConfig.MAX_THUMBNAILS;

/**
 * Created by namjungsoo on 2016-11-23.
 */

public class ExplorerFragment extends BaseFragment implements ExplorerAdapter.OnItemClickListener {
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
    private RecyclerView gridView;
    private RecyclerView listView;
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

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        if (DEBUG)
            Log.d(TAG, "onCreateView");

        rootView = inflater.inflate(R.layout.fragment_explorer, container, false);

        initUI();
        initViewType();
        PermissionManager.checkStoragePermissions(getActivity());

        extSdCard = ExtSdCardHelper.getExternalSdCardPath();
        if (extSdCard != null) {
            if (sdcard != null) {
                sdcard.setVisibility(View.VISIBLE);
            }
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        BaseActivity activity = (BaseActivity) getActivity();
        if (activity != null) {
            if (!activity.getShowReview()) {
                BookLoader.openLastBook(activity);
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
                updateFileList(null);
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
        viewType = PreferenceHelper.getViewType(getActivity());
        switch (viewType) {
            case SWITCH_LIST:
                switchToList();

                break;
            case SWITCH_GRID:
                switchToGrid();
                break;
        }
    }

    public void switchToList() {
        switcherViewType.setDisplayedChild(SWITCH_LIST);

        adapter = new ExplorerListAdapter(getActivity(), fileList);

        listView = (RecyclerView) rootView.findViewById(R.id.list_explorer);
        listView.setAdapter(adapter);
        listView.addOnScrollListener(new ExplorerScrollListener());
        listView.setLayoutManager(new LinearLayoutManager(getActivity()));
        listView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        adapter.setOnItemClickListener(this);

        viewType = SWITCH_LIST;
        currentView = listView;

        PreferenceHelper.setViewType(getContext(), viewType);
    }

    public void switchToGrid() {
        switcherViewType.setDisplayedChild(SWITCH_GRID);

        adapter = new ExplorerGridAdapter(getActivity(), fileList);

        gridView = (RecyclerView) rootView.findViewById(R.id.grid_explorer);
        gridView.setAdapter(adapter);
        gridView.addOnScrollListener(new ExplorerScrollListener());
        gridView.setLayoutManager(new GridLayoutManager(getActivity(), 4));
        adapter.setOnItemClickListener(this);

        viewType = SWITCH_GRID;
        currentView = gridView;

        PreferenceHelper.setViewType(getContext(), viewType);
    }

    // 새로운 파일이 추가 되었을때 스캔을 하라는 의미이다.
    void refreshThumbnail() {
        ArrayList<ExplorerItem> imageList = (ArrayList<ExplorerItem>) ExplorerManager.getImageList().clone();

        FragmentActivity activity = getActivity();
        for (ExplorerItem item : imageList) {
            if (activity == null)
                break;
            activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + item.path)));
        }
    }

    public void gotoUpDirectory() {
        String path = ExplorerManager.getLastPath();
        path = path.substring(0, path.lastIndexOf('/'));
        if (path.length() == 0) {
            path = "/";
        }

        backupPosition();
        updateFileList(path);
    }

    void onClickDirectory(ExplorerItem item) {
        String newPath;
        if (ExplorerManager.getLastPath().equals("/")) {
            newPath = ExplorerManager.getLastPath() + item.name;
        } else {
            newPath = ExplorerManager.getLastPath() + "/" + item.name;
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

    void onAdapterItemClick(int position) {
        ExplorerItem item = fileList.get(position);
        switch (item.type) {
            case DIRECTORY:
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
            case ZIP:
                //TODO: 나중에 이미지 preview를 만들자.
                onClickBook(item);
                break;
        }
    }

    //TODO: 나중에 구현
    void backupPosition() {
//        PositionManager.setPosition(ExplorerManager.getLastPath(), currentView.getFirstVisiblePosition());
//        PositionManager.setTop(ExplorerManager.getLastPath(), getCurrentViewScrollTop());
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

    class SearchTask extends AsyncTask<String, Void, Void> {

        boolean pathChanged;

        public SearchTask(boolean pathChanged) {
            this.pathChanged = pathChanged;
        }

        @Override
        protected Void doInBackground(String... params) {
            fileList = ExplorerManager.search(params[0]);
            adapter.setFileList(fileList);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // SearchTask가 resume
            if (pathChanged) {
                adapter.notifyDataSetChanged();
                currentView.scrollToPosition(0);
                currentView.invalidate();
            }

            textPath.setText(ExplorerManager.getLastPath());
            textPath.requestLayout();

            if (switcherContents != null) {
                if (fileList == null || fileList.size() <= 0) {
                    switcherContents.setDisplayedChild(1);

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
                }
            }
        }
    }

    public void updateFileList(final String path) {
        if (DEBUG)
            Log.w(TAG, "updateFileList path=" + path);

        if (adapter == null) {
            if (DEBUG)
                Log.w(TAG, "updateFileList adapter==null");
            return;
        }

        // 썸네일이 꽉찼을때는 비워준다.
        if (BitmapCacheManager.getThumbnailCount() > MAX_THUMBNAILS) {
            BitmapCacheManager.recycleThumbnail();
        }

        SearchTask task = new SearchTask(isPathChanged(path));
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);

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
                refreshThumbnail();
            }
        }).start();
    }

    private boolean isPathChanged(String path) {
        String currentPath = PreferenceHelper.getLastPath(getContext());
        boolean pathChanged = !currentPath.equals(path);
        return pathChanged;
    }

    @Override
    public void onRefresh() {
        // 외부 resume시에 들어올수도 있으므로 pref에서 읽는다.
        updateFileList(PreferenceHelper.getLastPath(getContext()));
    }

    @Override
    public void onBackPressed() {
        if (!ExplorerManager.isInitialPath()) {
            gotoUpDirectory();
        } else {
            super.onBackPressed();
        }
    }

    public int getViewType() {
        return viewType;
    }

    public void visibleTest() {
        if (viewType == SWITCH_GRID) {
            GridLayoutManager manager = (GridLayoutManager) gridView.getLayoutManager();
            if (manager != null) {
                int first = manager.findFirstVisibleItemPosition();
                int last = manager.findLastVisibleItemPosition();

                Log.e(TAG, "first=" + first + " last=" + last);
            }
        }
    }
}
