package com.duongame.explorer.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.duongame.explorer.R;
import com.duongame.explorer.activity.PhotoActivity;
import com.duongame.explorer.activity.ZipActivity;
import com.duongame.explorer.adapter.ExplorerAdapter;
import com.duongame.explorer.adapter.ExplorerFileItem;
import com.duongame.explorer.adapter.ExplorerGridAdapter;
import com.duongame.explorer.adapter.ExplorerListAdapter;
import com.duongame.explorer.bitmap.BitmapCacheManager;
import com.duongame.explorer.helper.ExplorerSearcher;
import com.duongame.explorer.helper.PositionManager;
import com.duongame.explorer.helper.PreferenceHelper;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-23.
 */

public class ExplorerFragment extends Fragment {
    private final static String TAG = "ExplorerFragment";

    private final static int MAX_THUMBNAILS = 100;
    private final static int SWITCH_LIST = 0;
    private final static int SWITCH_GRID = 1;

    private int viewType = SWITCH_LIST;

    private ExplorerAdapter adapter;
    private ArrayList<ExplorerFileItem> fileList;
    private TextView textPath;
    private HorizontalScrollView scrollPath;
    private GridView gridView;
    private ListView listView;
    private AbsListView currentView;
    private ViewSwitcher switcher;
    private View rootView;

    private Handler handler;
    private Thread thread;

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        //setContentView(R.layout.fragment_explorer);
        rootView = inflater.inflate(R.layout.fragment_explorer, container, false);
        handler = new Handler();

        initUI();
        initViewType();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
//        Log.d(TAG, "onPause");

        // 밖에 나갔다 들어오면 리프레시함
        updateFileList(ExplorerSearcher.getLastPath());
    }

    @Override
    public void onPause() {
        super.onPause();

        final int position = currentView.getFirstVisiblePosition();
        final int top = getCurrentViewScrollTop();
//        Log.d(TAG, "onPause position=" + position + " top="+top);
        new Thread(new Runnable() {
            @Override
            public void run() {
                PreferenceHelper.setLastPosition(getActivity(), position);
                PreferenceHelper.setLastTop(getActivity(), top);
            }
        }).start();
    }

    void initUI() {
        switcher = (ViewSwitcher) rootView.findViewById(R.id.switcher);
        textPath = (TextView) rootView.findViewById(R.id.text_path);
        scrollPath = (HorizontalScrollView) rootView.findViewById(R.id.scroll_path);
        fileList = new ArrayList<>();

        final Button view = (Button) rootView.findViewById(R.id.btn_view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button) view;
                if (viewType == SWITCH_LIST) {
                    switchToGrid();
                    PreferenceHelper.setViewType(getActivity(), SWITCH_GRID);
                } else {
                    switchToList();
                    PreferenceHelper.setViewType(getActivity(), SWITCH_LIST);
                }
            }
        });

        final Button home = (Button) rootView.findViewById(R.id.btn_home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateFileList(null);
            }
        });

        final Button up = (Button) rootView.findViewById(R.id.btn_up);
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoUpDirectory();
            }
        });
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
        switcher.setDisplayedChild(SWITCH_LIST);

        adapter = new ExplorerListAdapter(getActivity(), fileList);

        listView = (ListView) rootView.findViewById(R.id.list_explorer);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onAdapterItemClick(position);
            }
        });

        final Button view = (Button) rootView.findViewById(R.id.btn_view);
        view.setText("Grid");

        viewType = SWITCH_LIST;
        currentView = listView;

        moveToSelection(ExplorerSearcher.getLastPath());
    }

    void switchToGrid() {
        switcher.setDisplayedChild(SWITCH_GRID);

        adapter = new ExplorerGridAdapter(getActivity(), fileList);

        gridView = (GridView) rootView.findViewById(R.id.grid_explorer);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onAdapterItemClick(position);
            }
        });

        final Button view = (Button) rootView.findViewById(R.id.btn_view);
        view.setText("List");

        viewType = SWITCH_GRID;
        currentView = gridView;

        moveToSelection(ExplorerSearcher.getLastPath());
    }

    void refreshThumbnail(String path) {
        ArrayList<ExplorerFileItem> imageList = ExplorerSearcher.getImageList();
        for (ExplorerFileItem item : imageList) {
            getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + item.path)));
        }
    }

    public void gotoUpDirectory() {
        String path = ExplorerSearcher.getLastPath();
        path = path.substring(0, path.lastIndexOf('/'));
        if (path.length() == 0) {
            path = "/";
        }
        PositionManager.setPosition(ExplorerSearcher.getLastPath(), currentView.getFirstVisiblePosition());
        PositionManager.setTop(ExplorerSearcher.getLastPath(), getCurrentViewScrollTop());

        updateFileList(path);
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

                Intent intent = new Intent(getActivity(), PhotoActivity.class);
                intent.putExtra("path", item.path.substring(0, item.path.lastIndexOf('/')));
                intent.putExtra("name", item.name);
                startActivity(intent);
            }
            break;
            case ZIP: {
                PositionManager.setPosition(ExplorerSearcher.getLastPath(), currentView.getFirstVisiblePosition());
                PositionManager.setTop(ExplorerSearcher.getLastPath(), getCurrentViewScrollTop());

                Intent intent = new Intent(getActivity(), ZipActivity.class);
                intent.putExtra("path", item.path);
                intent.putExtra("name", item.name);
                intent.putExtra("page", 0);
                startActivity(intent);
            }
            break;
        }
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

//        Log.d(TAG, "updateFileList path=" + path + " position=" + position + " top="+top);

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

//    private void threadStart() {
//        thread = new Thread(new Runnable() {
//            private void load(ExplorerFileItem item) {
//                Bitmap bitmap = getThumbnail(item.path);
//
//                if (bitmap == null) {
//                    bitmap = BitmapLoader.getThumbnail(getActivity(), item.path);
//
//                    if (bitmap == null) {
//                        bitmap = BitmapLoader.decodeSquareThumbnailFromFile(item.path, 96);
//                    }
//                    if (bitmap != null) {
//                        BitmapCacheManager.setThumbnail(item.path, bitmap, null);
//                    }
//                }
//            }
//
//            private int findImage(String path) {
//                final ArrayList<ExplorerFileItem> imageList = ExplorerSearcher.getImageList();
//                for (int i = 0; i < imageList.size(); i++) {
//                    if (imageList.get(i).path.equals(path))
//                        return i;
//                }
//                return 0;
//            }
//
//            @Override
//            public void run() {
//                final ArrayList<ExplorerFileItem> newFileList = new ArrayList<>();
//                final ArrayList<ExplorerFileItem> newImageList = new ArrayList<>();
//
//                newFileList.addAll(fileList);
//                newImageList.addAll(ExplorerSearcher.getImageList());
//
//                final int position = currentView.getFirstVisiblePosition();
//                final String startPath = newFileList.get(position).path;
//                final int startPosition = findImage(startPath);
//
//                for (int i = startPosition; i < newImageList.size(); i++) {
//                    final ExplorerFileItem item = newImageList.get(i);
//                    if(Thread.currentThread().isInterrupted())
//                        return;
//                    load(item);
//                }
//                for (int i = 0; i < startPosition; i++) {
//                    final ExplorerFileItem item = newImageList.get(i);
//                    if(Thread.currentThread().isInterrupted())
//                        return;
//                    load(item);
//                }
//            }
//        });
//        thread.start();
//        Log.d(TAG, "threadStart");
//    }

    private void threadStop() {
        if (thread != null && thread.isAlive())
            thread.interrupt();
        Log.d(TAG, "threadStop");
    }

    public void updateFileList(String path) {
        adapter.stopAllTasks();

        if (BitmapCacheManager.getThumbnailCount() > MAX_THUMBNAILS) {
//            Log.w(TAG, "recycleThumbnail");
            BitmapCacheManager.recycleThumbnail();
        }

//        threadStop();

        fileList = ExplorerSearcher.search(path);
        if (fileList != null) {
//            Log.d(TAG, "fileList size="+fileList.size());
            adapter.setFileList(fileList);
            adapter.notifyDataSetChanged();
        }

//        threadStart();

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
                PreferenceHelper.setLastPath(getActivity(), ExplorerSearcher.getLastPath());
            }
        }).start();
        refreshThumbnail(path);
    }
}
