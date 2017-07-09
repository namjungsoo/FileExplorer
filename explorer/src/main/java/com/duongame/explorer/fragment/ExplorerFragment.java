package com.duongame.explorer.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.duongame.R;
import com.duongame.comicz.db.BookDB;
import com.duongame.explorer.adapter.ExplorerAdapter;
import com.duongame.explorer.adapter.ExplorerGridAdapter;
import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.adapter.ExplorerListAdapter;
import com.duongame.explorer.bitmap.BitmapCacheManager;
import com.duongame.explorer.helper.ExtSdCardHelper;
import com.duongame.explorer.helper.PreferenceHelper;
import com.duongame.explorer.manager.ExplorerManager;
import com.duongame.explorer.manager.PermissionManager;
import com.duongame.explorer.manager.PositionManager;
import com.duongame.viewer.activity.PdfActivity;
import com.duongame.viewer.activity.PhotoActivity;
import com.duongame.viewer.activity.TextActivity;
import com.duongame.viewer.activity.ZipActivity;

import java.io.File;
import java.util.ArrayList;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.view.View.GONE;

/**
 * Created by namjungsoo on 2016-11-23.
 */

public class ExplorerFragment extends BaseFragment {
    private final static String TAG = "ExplorerFragment";

    private final static int MAX_THUMBNAILS = 100;
    private final static int SWITCH_LIST = 0;
    private final static int SWITCH_GRID = 1;

    private int viewType = SWITCH_LIST;

    // 파일 관련
    private ExplorerAdapter adapter;
    private ArrayList<ExplorerItem> fileList;

    // 패스 관련
    private TextView textPath;
    private HorizontalScrollView scrollPath;

    // 컨텐츠 관련
    private GridView gridView;
    private ListView listView;
    private AbsListView currentView;
    private View rootView;

    // 뷰 스위처
    private ViewSwitcher switcherViewType;
    private ViewSwitcher switcherContents;
    private Button permButton;
    private TextView textNoFiles;

    // 기타
    private ImageButton sdcard = null;
    private String extSdCard = null;

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
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

//        final BookDB.Book book = BookDB.getLastBook(getActivity());
//        if(book != null) {
//            // zip파일인가 체크
//            if(book.path.toLowerCase().endsWith(".zip")) {
//                loadLastBookZip(book, false);
//            }
//        }

        return rootView;
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

        final int position = currentView.getFirstVisiblePosition();
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

        final LinearLayout view = (LinearLayout) rootView.findViewById(R.id.layout_view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewType == SWITCH_LIST) {
                    switchToGrid();
                    PreferenceHelper.setViewType(getActivity(), SWITCH_GRID);
                } else {
                    switchToList();
                    PreferenceHelper.setViewType(getActivity(), SWITCH_LIST);
                }
            }
        });

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
        int viewType = PreferenceHelper.getViewType(getActivity());
        switch (viewType) {
            case SWITCH_LIST:
                switchToList();

                break;
            case SWITCH_GRID:
                switchToGrid();
                break;
        }
    }

    void switchToList() {
        switcherViewType.setDisplayedChild(SWITCH_LIST);

        adapter = new ExplorerListAdapter(getActivity(), fileList);

        listView = (ListView) rootView.findViewById(R.id.list_explorer);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onAdapterItemClick(position);
            }
        });
        listView.setOnScrollListener(adapter);
        listView.setOnTouchListener(adapter);

        final Button view = (Button) rootView.findViewById(R.id.btn_view);
        view.setText(getResources().getString(R.string.grid));
        final ImageView image = (ImageView) rootView.findViewById(R.id.image_view);
        image.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.grid, null));

        viewType = SWITCH_LIST;
        currentView = listView;

//        moveToSelection(ExplorerManager.getLastPath());
    }

    void switchToGrid() {
        switcherViewType.setDisplayedChild(SWITCH_GRID);

        adapter = new ExplorerGridAdapter(getActivity(), fileList);

        gridView = (GridView) rootView.findViewById(R.id.grid_explorer);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onAdapterItemClick(position);
            }
        });
        gridView.setOnScrollListener(adapter);
        gridView.setOnTouchListener(adapter);

        final Button view = (Button) rootView.findViewById(R.id.btn_view);
        view.setText(getResources().getString(R.string.list));
        final ImageView image = (ImageView) rootView.findViewById(R.id.image_view);
        image.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.list, null));

        viewType = SWITCH_GRID;
        currentView = gridView;

//        moveToSelection(ExplorerManager.getLastPath());
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

    //TODO: 읽던 파일이면 읽던 페이지로 이동해야 함.
    void onAdapterItemClick(int position) {
        ExplorerItem item = fileList.get(position);
        switch (item.type) {
            case DIRECTORY:
//                backupPosition();

                String newPath;
                if (ExplorerManager.getLastPath().equals("/")) {
                    newPath = ExplorerManager.getLastPath() + item.name;
                } else {
                    newPath = ExplorerManager.getLastPath() + "/" + item.name;
                }

                updateFileList(newPath);
                break;
            case IMAGE: {
//                backupPosition();

                final Intent intent = new Intent(getActivity(), PhotoActivity.class);

                // 풀패스에서 폴더만 떼옴
                intent.putExtra("path", item.path.substring(0, item.path.lastIndexOf('/')));
                intent.putExtra("name", item.name);

                startActivity(intent);
            }
            break;
            case PDF: {
//                backupPosition();

                final Intent intent = new Intent(getActivity(), PdfActivity.class);
                intent.putExtra("path", item.path);
                intent.putExtra("name", item.name);
                intent.putExtra("current_page", 0);
                intent.putExtra("size", item.size);

                startActivity(intent);

                Log.d(TAG, "onAdapterItemClick pdf");
            }
            break;
            case ZIP: {
//                backupPosition();

                final BookDB.Book book = BookDB.getBook(getActivity(), item.path);

                if (book == null) {
                    final Intent intent = new Intent(getActivity(), ZipActivity.class);
                    intent.putExtra("path", item.path);
                    intent.putExtra("name", item.name);
                    intent.putExtra("current_page", 0);
                    intent.putExtra("size", item.size);
                    startActivity(intent);
                } else {
                    loadLastBookZip(book, true);
                }

            }
            break;
            case TEXT: {
//                backupPosition();

                final Intent intent = new Intent(getActivity(), TextActivity.class);
                intent.putExtra("path", item.path);
                intent.putExtra("name", item.name);

                startActivity(intent);
            }
            break;
            case APK: {
//                backupPosition();

                final Intent intent = new Intent(Intent.ACTION_VIEW);
                final String providerName = getContext().getPackageName() + ".provider";
                final Uri apkUri = FileProvider.getUriForFile(getContext(), providerName, new File(item.path));
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);

                startActivity(intent);
            }
            break;
        }
    }

    void loadLastBookZip(final BookDB.Book book, final boolean cancelToRead) {
        final Intent intent = new Intent(getActivity(), ZipActivity.class);
        intent.putExtra("path", book.path);
        intent.putExtra("name", book.name);
        intent.putExtra("size", book.size);
        intent.putExtra("extract_file", book.extract_file);
        intent.putExtra("side", book.side.getValue());

        // 이부분은 물어보고 셋팅하자.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("알림")
                .setMessage(String.format("마지막에 읽던 페이지가 있습니다.\n(페이지: %d)\n계속 읽으시겠습니까?", book.current_page + 1))
                .setIcon(R.drawable.comicz)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        intent.putExtra("current_page", book.current_page);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (cancelToRead) {
                            intent.putExtra("current_page", 0);
                            startActivity(intent);
                        }
                    }
                });

        //TODO: 나중에 이미지 preview를 만들자.
//                    if(book.last_file != null) {
//                        ImageView imageView = new ImageView(getActivity());
//                        imageView.setImageBitmap();
//                    }

        builder.show();
    }

    void backupPosition() {
        PositionManager.setPosition(ExplorerManager.getLastPath(), currentView.getFirstVisiblePosition());
        PositionManager.setTop(ExplorerManager.getLastPath(), getCurrentViewScrollTop());
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
                    currentView.setSelectionFromTop(position, top);
                } else {
                    currentView.setSelection(position);
                }

                currentView.clearFocus();
            }
        });
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
            adapter.notifyDataSetChanged();
            if (pathChanged) {
                listView.setSelection(0);
                listView.invalidate();
            }

            adapter.resumeThread();

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
        Log.w(TAG, "updateFileList path=" + path);
        if (adapter == null) {
            Log.w(TAG, "updateFileList adapter==null");
            return;
        }

        adapter.pauseThread();

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
}
