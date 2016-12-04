package com.duongame.fileexplorer.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.duongame.explorer.fragment.ExplorerFragment;
import com.duongame.explorer.helper.ExplorerSearcher;
import com.duongame.explorer.helper.PositionManager;
import com.duongame.explorer.helper.PreferenceHelper;
import com.duongame.fileexplorer.R;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    private final static int PERMISSION_STORAGE = 1;

    ExplorerFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // fragment 만들고 붙여주어야 함
//        fragment = new ExplorerFragment();

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
    protected void onStart() {
        super.onStart();

        FragmentManager fm = getSupportFragmentManager();
        fragment = (ExplorerFragment)fm.findFragmentById(R.id.fragment_explorer);

        if (checkStoragePermissions()) {
            final String lastPath = PreferenceHelper.getLastPath(MainActivity.this);
            final int position = PreferenceHelper.getLastPosition(MainActivity.this);
            final int top = PreferenceHelper.getLastTop(MainActivity.this);

//            Log.d(TAG, "onCreate path=" + lastPath + " position=" + position + " top="+top);

            PositionManager.setPosition(lastPath, position);
            PositionManager.setTop(lastPath, top);

            fragment.updateFileList(lastPath);
        }
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
            fragment.updateFileList(null);
        }
    }

    @Override
    public void onBackPressed() {
        if (!ExplorerSearcher.isInitialPath()) {
            fragment.gotoUpDirectory();
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
