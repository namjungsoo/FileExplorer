package com.duongame.fileexplorer.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.duongame.fileexplorer.ExplorerFileItem;
import com.duongame.fileexplorer.ExplorerSearcher;
import com.duongame.fileexplorer.R;
import com.duongame.fileexplorer.adapter.ExplorerAdapter;
import com.duongame.fileexplorer.adapter.ExplorerGridAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final static int PERMISSION_STORAGE = 1;

    private ExplorerAdapter adapter;
    private ExplorerSearcher searcher;
    private ArrayList<ExplorerFileItem> fileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileList = new ArrayList<>();
        searcher = new ExplorerSearcher();
        adapter = new ExplorerGridAdapter(this, fileList, searcher);
//        ListView listView = (ListView)findViewById(R.id.list_explorer);
//        listView.setAdapter(adapter);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                ExplorerFileItem item = fileList.get(position);
//                if(item.type == ExplorerFileItem.FileType.DIRECTORY) {
//                    String newPath = searcher.getLastPath() + "/" + item.name;
//                    fileList = searcher.search(newPath);
//                    adapter.setFileList(fileList);
//                    adapter.notifyDataSetChanged();
//                }
//            }
//        });
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
        GridView gridView = (GridView)findViewById(R.id.grid_explorer);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ExplorerFileItem item = fileList.get(position);
                if(item.type == ExplorerFileItem.FileType.DIRECTORY) {
                    String newPath = searcher.getLastPath() + "/" + item.name;

                    updateFileList(newPath);
                }
            }
        });

        if(checkStoragePermissions()) {
            updateFileList(null);
        }

    }

    void updateFileList(String path) {
        fileList = searcher.search(path);
        adapter.setFileList(fileList);
        adapter.notifyDataSetChanged();
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

    @Override
    public void onBackPressed() {
        if(!searcher.isInitialPath()) {
            String path = searcher.getLastPath();
            path = path.substring(0,path.lastIndexOf('/'));

            updateFileList(path);
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
