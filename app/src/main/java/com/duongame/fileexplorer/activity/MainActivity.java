package com.duongame.fileexplorer.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.duongame.fileexplorer.R;
import com.duongame.fileexplorer.adapter.ExplorerAdapter;
import com.duongame.fileexplorer.adapter.ExplorerFileItem;
import com.duongame.fileexplorer.adapter.ExplorerGridAdapter;
import com.duongame.fileexplorer.adapter.ExplorerListAdapter;
import com.duongame.fileexplorer.bitmap.BitmapCacheManager;
import com.duongame.fileexplorer.helper.ExplorerSearcher;
import com.duongame.fileexplorer.helper.PositionManager;
import com.duongame.fileexplorer.helper.PreferenceHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    private final static int PERMISSION_STORAGE = 1;
    private final static int MAX_THUMBNAILS = 20;

    private ExplorerAdapter adapter;
    private ArrayList<ExplorerFileItem> fileList;
    private TextView textPath;
    private HorizontalScrollView scrollPath;

    private GridView gridView;
    private ListView listView;
    private AbsListView currentView;

    private ViewSwitcher switcher;

    private final static int SWITCH_LIST = 0;
    private final static int SWITCH_GRID = 1;

    private int viewType = SWITCH_LIST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();

        int viewType = PreferenceHelper.getViewType(this);
        switch (viewType) {
            case SWITCH_LIST:
                switchToList();

                break;
            case SWITCH_GRID:
                switchToGrid();
                break;
        }

        if (checkStoragePermissions()) {
            final String lastPath = PreferenceHelper.getLastPath(MainActivity.this);
            final int position = PreferenceHelper.getLastPosition(MainActivity.this);
            final int top = PreferenceHelper.getLastTop(MainActivity.this);

            Log.d(TAG, "onCreate path=" + lastPath + " position=" + position + " top="+top);

            PositionManager.setPosition(lastPath, position);
            PositionManager.setTop(lastPath, top);

            updateFileList(lastPath);
        }

        // long click
//        adapter = new ExplorerGridAdapter(this, fileList);
//        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                CheckBox checkBox = (CheckBox)view.findViewById(R.id.check_file);
//                if(checkBox != null) {
//                    checkBox.setVisibility(View.VISIBLE);
//                }
//
//                return true;
//            }
//        });
//
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onPause");

        // 밖에 나갔다 들어오면 리프레시함
        updateFileList(ExplorerSearcher.getLastPath());
    }

    @Override
    protected void onPause() {
        super.onPause();

        final int position = currentView.getFirstVisiblePosition();
        final int top = getCurrentViewScrollTop();
        Log.d(TAG, "onPause position=" + position + " top="+top);
        new Thread(new Runnable() {
            @Override
            public void run() {
                PreferenceHelper.setLastPosition(MainActivity.this, position);
                PreferenceHelper.setLastTop(MainActivity.this, top);
            }
        }).start();
    }

    void initUI() {
        switcher = (ViewSwitcher) findViewById(R.id.switcher);
        textPath = (TextView) findViewById(R.id.text_path);
        scrollPath = (HorizontalScrollView) findViewById(R.id.scroll_path);
        fileList = new ArrayList<>();

        final Button view = (Button) findViewById(R.id.btn_view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button) view;
                if (viewType == SWITCH_LIST) {
                    switchToGrid();
                    PreferenceHelper.setViewType(MainActivity.this, SWITCH_GRID);
                } else {
                    switchToList();
                    PreferenceHelper.setViewType(MainActivity.this, SWITCH_LIST);
                }
            }
        });

        final Button home = (Button) findViewById(R.id.btn_home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateFileList(null);
            }
        });

        final Button up = (Button) findViewById(R.id.btn_up);
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoUpDirectory();
            }
        });
    }

    void switchToList() {
        switcher.setDisplayedChild(SWITCH_LIST);

        adapter = new ExplorerListAdapter(this, fileList);

        listView = (ListView) findViewById(R.id.list_explorer);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onAdapterItemClick(position);
            }
        });

        final Button view = (Button) findViewById(R.id.btn_view);
        view.setText("Grid");

        viewType = SWITCH_LIST;
        currentView = listView;

        moveToSelection(ExplorerSearcher.getLastPath());
    }

    void switchToGrid() {
        switcher.setDisplayedChild(SWITCH_GRID);

        adapter = new ExplorerGridAdapter(this, fileList);

        gridView = (GridView) findViewById(R.id.grid_explorer);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onAdapterItemClick(position);
            }
        });

        final Button view = (Button) findViewById(R.id.btn_view);
        view.setText("List");

        viewType = SWITCH_GRID;
        currentView = gridView;

        moveToSelection(ExplorerSearcher.getLastPath());
    }

    void refreshThumbnail(String path) {
        ArrayList<ExplorerFileItem> imageList = ExplorerSearcher.getImageList();
        for (ExplorerFileItem item : imageList) {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + item.path)));
        }
    }

    void onAdapterItemClick(int position) {
        ExplorerFileItem item = fileList.get(position);
        switch (item.type) {
            case DIRECTORY:
                String newPath;
                if (ExplorerSearcher.getLastPath().equals("/")) {
                    newPath = ExplorerSearcher.getLastPath() + item.name;
                } else {
                    newPath = ExplorerSearcher.getLastPath() + "/" + item.name;
                }

                PositionManager.setPosition(ExplorerSearcher.getLastPath(), currentView.getFirstVisiblePosition());
                PositionManager.setTop(ExplorerSearcher.getLastPath(), getCurrentViewScrollTop());

                updateFileList(newPath);
                break;
            case IMAGE: {
                PositionManager.setPosition(ExplorerSearcher.getLastPath(), currentView.getFirstVisiblePosition());
                PositionManager.setTop(ExplorerSearcher.getLastPath(), getCurrentViewScrollTop());

                Intent intent = new Intent(MainActivity.this, PhotoActivity.class);
                intent.putExtra("name", item.name);
                startActivity(intent);
            }
            break;
            case ZIP: {
                PositionManager.setPosition(ExplorerSearcher.getLastPath(), currentView.getFirstVisiblePosition());
                PositionManager.setTop(ExplorerSearcher.getLastPath(), getCurrentViewScrollTop());

                Intent intent = new Intent(MainActivity.this, ZipActivity.class);
                intent.putExtra("path", item.path);
                intent.putExtra("page", 0);
                startActivity(intent);
            }
            break;
        }
    }

    int getCurrentViewScrollTop() {
        if(currentView.getChildCount() > 0) {
            return currentView.getChildAt(0).getTop();
        }
        return 0;
    }

    void moveToSelection(String path) {
        final int position = PositionManager.getPosition(path);
        final int top = PositionManager.getTop(path);

        Log.d(TAG, "updateFileList path=" + path + " position=" + position + " top="+top);

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

                currentView.requestFocus();
            }
        });
    }

    void updateFileList(String path) {
        adapter.stopAllTasks();

        if (BitmapCacheManager.getThumbnailCount() > MAX_THUMBNAILS) {
            Log.d(TAG, "recycleThumbnail");
            BitmapCacheManager.recycleThumbnail();
        }

        fileList = ExplorerSearcher.search(path);
        if (fileList != null) {
            adapter.setFileList(fileList);
            adapter.notifyDataSetChanged();
        }

        textPath.setText(ExplorerSearcher.getLastPath());
        textPath.requestLayout();

        // 가장 오른쪽으로 스크롤
        scrollPath.post(new Runnable() {
                            @Override
                            public void run() {
                                scrollPath.fullScroll(View.FOCUS_RIGHT);
                            }
                        }
        );

        moveToSelection(path);

        new Thread(new Runnable() {
            @Override
            public void run() {
                PreferenceHelper.setLastPath(MainActivity.this, ExplorerSearcher.getLastPath());
            }
        }).start();
        refreshThumbnail(path);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        final String read = Manifest.permission.READ_EXTERNAL_STORAGE;
        final String write = Manifest.permission.WRITE_EXTERNAL_STORAGE;

        boolean readEnable = false;
        boolean writeEnable = false;

        for (int i = 0; i < permissions.length; i++) {
            if (read.equals(permissions[i]) && grantResults[i] == 0)
                readEnable = true;
            if (write.equals(permissions[i]) && grantResults[i] == 0)
                writeEnable = true;
        }

        if (readEnable && writeEnable) {
            // 최초 이므로 무조건 null
            updateFileList(null);
        }
    }

    private void gotoUpDirectory() {
        String path = ExplorerSearcher.getLastPath();
        path = path.substring(0, path.lastIndexOf('/'));
        if (path.length() == 0) {
            path = "/";
        }
        PositionManager.setPosition(ExplorerSearcher.getLastPath(), currentView.getFirstVisiblePosition());
        PositionManager.setTop(ExplorerSearcher.getLastPath(), getCurrentViewScrollTop());

        updateFileList(path);
    }

    @Override
    public void onBackPressed() {
        if (!ExplorerSearcher.isInitialPath()) {
            gotoUpDirectory();
        }
    }

    private boolean checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
                return false;
            }
        }
        return true;
    }
}
