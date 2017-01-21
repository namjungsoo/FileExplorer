package com.duongame.explorer.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
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

import com.duongame.explorer.R;
import com.duongame.explorer.adapter.ExplorerAdapter;
import com.duongame.explorer.adapter.ExplorerGridAdapter;
import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.adapter.ExplorerListAdapter;
import com.duongame.explorer.bitmap.BitmapCache;
import com.duongame.explorer.manager.ExplorerManager;
import com.duongame.explorer.manager.PositionManager;
import com.duongame.explorer.helper.PreferenceHelper;
import com.duongame.viewer.activity.PdfActivity;
import com.duongame.viewer.activity.PhotoActivity;
import com.duongame.viewer.activity.TextActivity;
import com.duongame.viewer.activity.ZipActivity;

import java.util.ArrayList;

import static com.duongame.explorer.helper.ExtSdCardHelper.getExternalSdCardPath;

/**
 * Created by namjungsoo on 2016-11-23.
 */

public class ExplorerFragment extends BaseFragment {
    private final static String TAG = "ExplorerFragment";

    private final static int MAX_THUMBNAILS = 100;
    private final static int SWITCH_LIST = 0;
    private final static int SWITCH_GRID = 1;

    private int viewType = SWITCH_LIST;

    private ExplorerAdapter adapter;
    private ArrayList<ExplorerItem> fileList;
    private TextView textPath;
    private HorizontalScrollView scrollPath;
    private GridView gridView;
    private ListView listView;
    private AbsListView currentView;
    private ViewSwitcher switcher;
    private View rootView;

    private ImageButton sdcard = null;
    private String extSdCard = null;

//    private Handler handler;
//    private Thread thread;

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        rootView = inflater.inflate(R.layout.fragment_explorer, container, false);
//        rootView = inflater.inflate(R.layout.fragment_explorer, null);

//        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                rootView.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.d(TAG, "onGlobalLayout "+rootView.getWidth() + " " + rootView.getHeight());
//                    }
//                });
//            }
//        });

        initUI();
        initViewType();

        extSdCard = getExternalSdCardPath();
        if(extSdCard != null) {
            if(sdcard != null) {
                sdcard.setVisibility(View.VISIBLE);
            }
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
//        Log.d(TAG, "onResume "+rootView.getWidth() + " " + rootView.getHeight());

        // 밖에 나갔다 들어오면 리프레시함
        refresh();
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

        final LinearLayout view = (LinearLayout) rootView.findViewById(R.id.layout_view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                LinearLayout btn = (LinearLayout) view;
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
                if(extSdCard != null) {
                    updateFileList(extSdCard);
                }
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
        view.setText(getResources().getString(R.string.grid));
        final ImageView image = (ImageView) rootView.findViewById(R.id.image_view);
        image.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.grid, null));

        viewType = SWITCH_LIST;
        currentView = listView;

        moveToSelection(ExplorerManager.getLastPath());
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
        view.setText(getResources().getString(R.string.list));
        final ImageView image = (ImageView) rootView.findViewById(R.id.image_view);
        image.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.list, null));

        viewType = SWITCH_GRID;
        currentView = gridView;

        moveToSelection(ExplorerManager.getLastPath());
    }

    void refreshThumbnail(String path) {
        ArrayList<ExplorerItem> imageList = ExplorerManager.getImageList();
        for (ExplorerItem item : imageList) {
            getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + item.path)));
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

    void onAdapterItemClick(int position) {
        ExplorerItem item = fileList.get(position);
        switch (item.type) {
            case DIRECTORY:
                backupPosition();

                String newPath;
                if (ExplorerManager.getLastPath().equals("/")) {
                    newPath = ExplorerManager.getLastPath() + item.name;
                } else {
                    newPath = ExplorerManager.getLastPath() + "/" + item.name;
                }

                updateFileList(newPath);
                break;
            case IMAGE: {
                backupPosition();

                Intent intent = new Intent(getActivity(), PhotoActivity.class);
                intent.putExtra("path", item.path.substring(0, item.path.lastIndexOf('/')));
                intent.putExtra("name", item.name);
                startActivity(intent);
            }
            break;
            case PDF: {
                backupPosition();

                Intent intent = new Intent(getActivity(), PdfActivity.class);
                intent.putExtra("path", item.path);
                intent.putExtra("name", item.name);
                intent.putExtra("current_page", 0);
                startActivity(intent);

                Log.d(TAG, "onAdapterItemClick pdf");
            }
            break;
            case ZIP: {
                backupPosition();

                Intent intent = new Intent(getActivity(), ZipActivity.class);
                intent.putExtra("path", item.path);
                intent.putExtra("name", item.name);
                intent.putExtra("current_page", 0);
                intent.putExtra("size", item.size);
                startActivity(intent);
            }
            break;
            case TEXT: {
                backupPosition();

                Intent intent = new Intent(getActivity(), TextActivity.class);
                intent.putExtra("path", item.path);
                intent.putExtra("name", item.name);
                startActivity(intent);
            }
            break;
        }
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

                currentView.clearFocus();
//                currentView.requestFocusFromTouch();
            }
        });
    }

//    private void threadStart() {
//        thread = new Thread(new Runnable() {
//            private void load(ExplorerItem item) {
//                Bitmap bitmap = getThumbnail(item.path);
//
//                if (bitmap == null) {
//                    bitmap = BitmapLoader.getThumbnail(getActivity(), item.path);
//
//                    if (bitmap == null) {
//                        bitmap = BitmapLoader.decodeSquareThumbnailFromFile(item.path, 96);
//                    }
//                    if (bitmap != null) {
//                        BitmapCache.setThumbnail(item.path, bitmap, null);
//                    }
//                }
//            }
//
//            private int findImage(String path) {
//                final ArrayList<ExplorerItem> imageList = ExplorerManager.getImageList();
//                for (int i = 0; i < imageList.size(); i++) {
//                    if (imageList.get(i).path.equals(path))
//                        return i;
//                }
//                return 0;
//            }
//
//            @Override
//            public void run() {
//                final ArrayList<ExplorerItem> newFileList = new ArrayList<>();
//                final ArrayList<ExplorerItem> newImageList = new ArrayList<>();
//
//                newFileList.addAll(fileList);
//                newImageList.addAll(ExplorerManager.getImageList());
//
//                final int position = currentView.getFirstVisiblePosition();
//                final String startPath = newFileList.get(position).path;
//                final int startPosition = findImage(startPath);
//
//                for (int i = startPosition; i < newImageList.size(); i++) {
//                    final ExplorerItem item = newImageList.get(i);
//                    if(Thread.currentThread().isInterrupted())
//                        return;
//                    load(item);
//                }
//                for (int i = 0; i < startPosition; i++) {
//                    final ExplorerItem item = newImageList.get(i);
//                    if(Thread.currentThread().isInterrupted())
//                        return;
//                    load(item);
//                }
//            }
//        });
//        thread.start();
//        Log.d(TAG, "threadStart");
//    }
//
//    private void threadStop() {
//        if (thread != null && thread.isAlive())
//            thread.interrupt();
//        Log.d(TAG, "threadStop");
//    }

    public void updateFileList(String path) {
        if (adapter == null)
            return;
//        Log.d(TAG, "updateFileList start "+rootView.getWidth() + " " + rootView.getHeight());
        adapter.stopAllTasks();

        if (BitmapCache.getThumbnailCount() > MAX_THUMBNAILS) {
//            Log.w(TAG, "recycleThumbnail");
            BitmapCache.recycleThumbnail();
        }

//        threadStop();

        fileList = ExplorerManager.search(path);
        if (fileList != null) {
//            Log.d(TAG, "fileList size="+fileList.size());
            adapter.setFileList(fileList);
            adapter.notifyDataSetChanged();
        }

//        threadStart();

        textPath.setText(ExplorerManager.getLastPath());
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
                PreferenceHelper.setLastPath(getActivity(), ExplorerManager.getLastPath());
            }
        }).start();
        refreshThumbnail(path);
    }

    @Override
    public void refresh() {
        updateFileList(ExplorerManager.getLastPath());
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
