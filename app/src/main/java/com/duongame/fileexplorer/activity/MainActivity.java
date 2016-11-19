package com.duongame.fileexplorer.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.duongame.fileexplorer.adapter.ExplorerFileItem;
import com.duongame.fileexplorer.helper.ExplorerSearcher;
import com.duongame.fileexplorer.helper.PreferenceHelper;
import com.duongame.fileexplorer.R;
import com.duongame.fileexplorer.adapter.ExplorerAdapter;
import com.duongame.fileexplorer.adapter.ExplorerGridAdapter;
import com.duongame.fileexplorer.adapter.ExplorerListAdapter;
import com.duongame.fileexplorer.bitmap.BitmapCacheManager;
import com.duongame.fileexplorer.bitmap.ZipLoader;

import net.lingala.zip4j.exception.ZipException;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final static int PERMISSION_STORAGE = 1;
    private final static int MAX_THUMBNAILS = 100;

    private ExplorerAdapter adapter;
    private ArrayList<ExplorerFileItem> fileList;
    private SwipeRefreshLayout refresh;
    private TextView textPath;
    private HorizontalScrollView scrollPath;

    private GridView gridView;
    private ListView listView;
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
        switch(viewType) {
            case SWITCH_LIST:
                switchToList();

                break;
            case SWITCH_GRID:
                switchToGrid();
                break;
        }

        if (checkStoragePermissions()) {
            final String lastPath = PreferenceHelper.getLastPath(MainActivity.this);
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

    void initUI() {
        switcher = (ViewSwitcher) findViewById(R.id.switcher);
        textPath = (TextView) findViewById(R.id.text_path);
        scrollPath = (HorizontalScrollView) findViewById(R.id.scroll_path);
        fileList = new ArrayList<>();

        final Button view = (Button) findViewById(R.id.btn_view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button)view;
                if(viewType == SWITCH_LIST) {
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

        refresh = (SwipeRefreshLayout) findViewById(R.id.refresh_list);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshThumbnail(ExplorerSearcher.getLastPath());
                updateFileList(ExplorerSearcher.getLastPath());
                refresh.setRefreshing(false);
            }
        });

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
    }

    void switchToGrid() {
        switcher.setDisplayedChild(SWITCH_GRID);

        refresh = (SwipeRefreshLayout) findViewById(R.id.refresh_grid);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshThumbnail(ExplorerSearcher.getLastPath());
                updateFileList(ExplorerSearcher.getLastPath());
                refresh.setRefreshing(false);
            }
        });

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
    }

    void refreshThumbnail(String path) {
        ArrayList<ExplorerFileItem> imageList = ExplorerSearcher.getImageList();
        for(ExplorerFileItem item : imageList) {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+ item.path)));
        }
    }

    void onAdapterItemClick(int position) {
        ExplorerFileItem item = fileList.get(position);
        switch(item.type) {
            case DIRECTORY:
                String newPath;
                if(ExplorerSearcher.getLastPath().equals("/")) {
                    newPath = ExplorerSearcher.getLastPath() + item.name;
                } else {
                    newPath = ExplorerSearcher.getLastPath() + "/" + item.name;
                }

                updateFileList(newPath);
                break;
            case IMAGE:
                Intent intent = new Intent(MainActivity.this, PhotoActivity.class);
                intent.putExtra("name", item.name);
                startActivity(intent);
                break;
            case ZIP:
                try {
                    ZipLoader.load(item.path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    void updateFileList(String path) {
        adapter.stopAllTasks();

        if (BitmapCacheManager.getThumbnailCount() > MAX_THUMBNAILS)
            BitmapCacheManager.recycleThumbnail();

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

        new Thread(new Runnable() {
            @Override
            public void run() {
                PreferenceHelper.setLastPath(MainActivity.this, ExplorerSearcher.getLastPath());
            }
        }).start();
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
            updateFileList(null);
        }
    }

    private void gotoUpDirectory() {
        String path = ExplorerSearcher.getLastPath();
        path = path.substring(0, path.lastIndexOf('/'));
        if (path.length() == 0) {
            path = "/";
        }
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
